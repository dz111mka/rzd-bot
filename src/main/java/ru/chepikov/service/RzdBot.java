package ru.chepikov.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
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
import ru.chepikov.state_machine.BotEvents;
import ru.chepikov.state_machine.BotStates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RzdBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final SubscriptionService subscriptionService;
    private final CarTypeService carTypeService;
    private final StateMachineFactory<BotStates, BotEvents> stateMachineFactory;
    private final Map<Long, TrainSubscription> pendingSubscriptions = new ConcurrentHashMap<>();
    private final Map<Long, StateMachine<BotStates, BotEvents>> userStateMachines = new ConcurrentHashMap<>();

    public RzdBot(BotConfig botConfig,
                  SubscriptionService subscriptionService, CarTypeService carTypeService, 
                  StateMachineFactory<BotStates, BotEvents> stateMachineFactory) {
        this.botConfig = botConfig;
        this.subscriptionService = subscriptionService;
        this.carTypeService = carTypeService;
        this.stateMachineFactory = stateMachineFactory;
        
        List<BotCommand> commands = List.of(
            new BotCommand("/start", "Начальное сообщение"),
            new BotCommand("/subscribe", "Подписаться на маршрут"),
            new BotCommand("/unsubscribe", "Отписаться от маршрута"),
            new BotCommand("/car_types", "Информация о типах вагонов"),
            new BotCommand("/cancel", "Отменить операцию")
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
            case "/cancel" -> cancelSubscription(chatId);
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

        StateMachine<BotStates, BotEvents> sm = stateMachineFactory.getStateMachine();

        sm.stop();

        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {
            accessor.resetStateMachine(
                    new DefaultStateMachineContext<>(
                            BotStates.WAITING_FOR_DATE,
                            null,
                            null,
                            null
                    )
            );
        });

        sm.start();

        userStateMachines.put(chatId, sm);

        sendMessage(chatId,
                "Введите дату в формате YYYY-MM-DD (например, " + today + "):");
    }

    private void cancelSubscription(long chatId) {
        userStateMachines.remove(chatId);
        pendingSubscriptions.remove(chatId);
        sendMessage(chatId, "Операция отменена.");
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
        StateMachine<BotStates, BotEvents> sm = userStateMachines.get(chatId);
        if (sm == null) return;

        BotStates state = sm.getState().getId();

        if (state == BotStates.START) {
            sendMessage(chatId, "Нажмите /subscribe для начала.");
            return;
        }

        sm.getExtendedState().getVariables().put("message", input);
        
        BotEvents event = getEventForState(state);
        boolean accepted = sm.sendEvent(event);
        
        if (accepted) {
            String result = sm.getExtendedState().get("result", String.class);
            Boolean error = sm.getExtendedState().get("error", Boolean.class);

            if (result != null) {
                sendMessage(chatId, result);
            }
            
            if (state == BotStates.WAITING_FOR_DATE) {
                LocalDate date = sm.getExtendedState().get("parsedDate", LocalDate.class);
                if (date != null && !Boolean.TRUE.equals(error)) {
                    createPendingSubscription(chatId, date);
                } else if (Boolean.TRUE.equals(error)) {
                    resetState(sm, BotStates.WAITING_FOR_DATE);
                }
            }

            if (state == BotStates.WAITING_FOR_DESTINATION && !Boolean.TRUE.equals(error)) {
                completeSubscription(chatId, sm);
            }
        }
    }

    private BotEvents getEventForState(BotStates state) {
        return switch (state) {
            case WAITING_FOR_DATE -> BotEvents.RECEIVE_DATE;
            case WAITING_FOR_ORIGIN -> BotEvents.RECEIVE_ORIGIN;
            case WAITING_FOR_DESTINATION -> BotEvents.RECEIVE_DESTINATION;
            default -> null;
        };
    }

    private void createPendingSubscription(long chatId, LocalDate date) {
        TrainSubscription sub = new TrainSubscription();
        sub.setUserId(chatId);
        sub.setDepartureDate(date);
        sub.setHashcode(0);
        pendingSubscriptions.put(chatId, sub);
    }

    private void resetState(StateMachine<BotStates, BotEvents> sm, BotStates state) {
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(accessor -> accessor.resetStateMachine(
                new DefaultStateMachineContext<>(state, null, null, null)
        ));
        sm.start();
    }

    public void sendMessage(long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            log.warn("Не удалось отправить сообщение: {}", e.getMessage());
        }
    }

    public void sendHtmlMessage(long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("HTML")
                    .disableWebPagePreview(true)
                    .build());
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

    private void completeSubscription(long chatId,
                                      StateMachine<BotStates, BotEvents> sm) {

        TrainSubscription sub = pendingSubscriptions.get(chatId);

        if (sub == null) {
            log.warn("Pending subscription not found for {}", chatId);
            return;
        }

        String origin = sm.getExtendedState()
                .get("originStation", String.class);

        String destination = sm.getExtendedState()
                .get("destinationStation", String.class);

        sub.setOriginStation(origin);
        sub.setDestinationStation(destination);

        subscriptionService.save(sub);

        pendingSubscriptions.remove(chatId);

        log.info("Subscription saved: {}", sub);
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
