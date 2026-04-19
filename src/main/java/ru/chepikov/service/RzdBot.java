package ru.chepikov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.chepikov.config.BotConfig;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.model.dto.RouteDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class RzdBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final TrainApiService trainApiService;
    private final StationService stationService;
    private final SubscriptionService subscriptionService;
    private final CarTypeService carTypeService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final Map<Long, UserInputState> userState = new HashMap<>();
    private final Map<Long, TrainSubscription> pendingSubscriptions = new HashMap<>();

    public RzdBot(BotConfig botConfig, TrainApiService trainApiService, StationService stationService,
                  SubscriptionService subscriptionService, CarTypeService carTypeService, ObjectMapper objectMapper) {
        this.botConfig = botConfig;
        this.trainApiService = trainApiService;
        this.stationService = stationService;
        this.subscriptionService = subscriptionService;
        this.carTypeService = carTypeService;
        this.objectMapper = objectMapper;
        
        List<BotCommand> commands = List.of(
            new BotCommand("/start", "Начальное сообщение"),
            new BotCommand("/subscribe", "Подписаться на маршрут"),
            new BotCommand("/unsubscribe", "Отписаться от маршрута"),
            new BotCommand("/car_types", "Информация о типах вагонов")
        );
        
        try {
            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при настройке команд бота: {}", e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update.getMessage().getText(), update.getMessage().getChatId());
        }
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleMessage(String text, long chatId) {
        switch (text) {
            case "/start" -> sendMessage(chatId, "Привет! Я бот для отслеживания билетов РЖД.");
            case "/unsubscribe" -> showSubscriptions(chatId);
            case "/car_types" -> showCarTypes(chatId);
            case "/subscribe" -> startSubscription(chatId);
            default -> handleUserInput(chatId, text);
        }
    }

    private void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        long chatId = callback.getMessage().getChatId();

        if (data.startsWith("REMOVE_SUB:")) {
            UUID subscriptionId = UUID.fromString(data.split(":")[1]);
            subscriptionService.deleteById(subscriptionId);
            showSubscriptions(chatId);
        }
    }

    private void startSubscription(long chatId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        sendMessage(chatId, "Введите дату в формате YYYY-MM-DD (например, " + today + "):");
        userState.put(chatId, UserInputState.WAITING_FOR_DATE);
    }

    private void showCarTypes(long chatId) {
        carTypeService.findAll().forEach(carType -> sendMessage(chatId, carType.toString()));
    }

    private void showSubscriptions(long chatId) {
        List<TrainSubscription> subscriptions = subscriptionService.findByUserId(chatId);
        
        if (subscriptions.isEmpty()) {
            sendMessage(chatId, "У вас нет активных подписок.");
            return;
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (TrainSubscription sub : subscriptions) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(subInlineLabel(sub))
                .callbackData("REMOVE_SUB:" + sub.getId())
                .build();
            rows.add(List.of(button));
        }
        keyboard.setKeyboard(rows);

        sendMessageWithKeyboard(chatId, "Выберите подписку для удаления:", keyboard);
    }

    private String subInlineLabel(TrainSubscription sub) {
        return "Удалить %s | %s → %s".formatted(
            sub.getDepartureDate(),
            sub.getOriginStation(),
            sub.getDestinationStation()
        );
    }

    private void handleUserInput(long chatId, String input) {
        UserInputState state = userState.get(chatId);
        if (state == null) return;

        switch (state) {
            case WAITING_FOR_DATE -> handleDateInput(chatId, input);
            case WAITING_FOR_ORIGIN -> handleOriginInput(chatId, input);
            case WAITING_FOR_DESTINATION -> handleDestinationInput(chatId, input);
        }
    }

    private void handleDateInput(long chatId, String input) {
        try {
            LocalDate date = LocalDate.parse(input);
            TrainSubscription sub = new TrainSubscription();
            sub.setUserId((int) chatId);
            sub.setDepartureDate(date);
            sub.setContentHash(0);
            pendingSubscriptions.put(chatId, sub);
            userState.put(chatId, UserInputState.WAITING_FOR_ORIGIN);
            sendMessage(chatId, "Введите станцию отправления:");
        } catch (DateTimeParseException e) {
            sendMessage(chatId, "Неверный формат даты. Пример: 2024-12-25");
        }
    }

    private void handleOriginInput(long chatId, String input) {
        TrainSubscription sub = pendingSubscriptions.get(chatId);
        sub.setOriginStation(input);
        userState.put(chatId, UserInputState.WAITING_FOR_DESTINATION);
        sendMessage(chatId, "Введите станцию прибытия:");
    }

    private void handleDestinationInput(long chatId, String input) {
        TrainSubscription sub = pendingSubscriptions.get(chatId);
        sub.setDestinationStation(input);
        
        subscriptionService.save(sub);
        log.info("Подписка создана: {} → {} ({})", 
            sub.getOriginStation(), sub.getDestinationStation(), sub.getDepartureDate());
        
        sendMessage(chatId, "Подписка создана!");
        userState.remove(chatId);
        pendingSubscriptions.remove(chatId);
    }

    @Async("scheduledChecking")
    @Scheduled(cron = "*/5 * * * *")
    public void checkAllSubscriptions() {
        List<TrainSubscription> subscriptions = subscriptionService.findAll();
        
        for (TrainSubscription sub : subscriptions) {
            executorService.submit(() -> {
                try {
                    checkSubscription(sub);
                } catch (Exception e) {
                    log.error("Ошибка проверки подписки {}: {}", sub.getId(), e.getMessage());
                }
            });
        }
    }

    private void checkSubscription(TrainSubscription sub) throws Exception {
        var origin = stationService.findByName(sub.getOriginStation());
        var destination = stationService.findByName(sub.getDestinationStation());

        if (origin == null || destination == null) {
            log.warn("Станция не найдена для подписки {}", sub.getId());
            return;
        }

        String json = trainApiService.getTrainPrices(origin.getId(), destination.getId(), sub.getDepartureDate());
        RouteDto route = objectMapper.readValue(json, RouteDto.class);
        route.setDate(sub.getDepartureDate());

        int routeHash = route.toString().hashCode();
        if (!Objects.equals(sub.getContentHash(), routeHash)) {
            sendMessage(sub.getUserId(), route.toString());
            sub.setContentHash(routeHash);
            subscriptionService.save(sub);
        }
    }

    @Async("scheduledDeleting")
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredSubscriptions() {
        subscriptionService.deleteExpiredData();
    }

    private void sendMessage(long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            log.warn("Не удалось отправить сообщение: {}", e.getMessage());
        }
    }

    private void sendMessageWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).replyMarkup(keyboard).build());
        } catch (TelegramApiException e) {
            log.warn("Не удалось отправить сообщение: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    enum UserInputState {
        WAITING_FOR_DATE,
        WAITING_FOR_ORIGIN,
        WAITING_FOR_DESTINATION
    }
}