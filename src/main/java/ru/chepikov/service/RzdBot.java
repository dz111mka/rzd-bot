package ru.chepikov.service;

import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RzdBot extends TelegramLongPollingBot implements BotMessageSender {

    private final BotConfig botConfig;
    private final SubscriptionService subscriptionService;
    private final CarTypeService carTypeService;
    private final SubscriptionDialogService subscriptionDialogService;

    public RzdBot(BotConfig botConfig,
                  SubscriptionService subscriptionService,
                  CarTypeService carTypeService,
                  SubscriptionDialogService subscriptionDialogService) {
        this.botConfig = botConfig;
        this.subscriptionService = subscriptionService;
        this.carTypeService = carTypeService;
        this.subscriptionDialogService = subscriptionDialogService;

        registerCommands();
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

    private void registerCommands() {
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

    private void handleMessage(String text, long chatId) {
        switch (text) {
            case "/start" -> sendMessage(chatId, "Привет! Я бот для отслеживания билетов РЖД.");
            case "/unsubscribe" -> showSubscriptions(chatId);
            case "/car_types" -> showCarTypes(chatId);
            case "/subscribe" -> subscriptionDialogService.start(chatId, this);
            case "/cancel" -> subscriptionDialogService.cancel(chatId, this);
            default -> subscriptionDialogService.handleUserInput(chatId, text, this);
        }
    }

    private void handleCallback(CallbackQuery callback) {
        String data = callback.getData();
        long chatId = callback.getMessage().getChatId();

        if (subscriptionDialogService.handleCallback(chatId, data, this)) {
            return;
        }

        if (data.startsWith("REMOVE_SUB:")) {
            UUID subscriptionId = UUID.fromString(data.split(":")[1]);
            subscriptionService.deleteById(subscriptionId);
            showSubscriptions(chatId);
        }
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
        return "Удалить %s | %s -> %s".formatted(
                sub.getDepartureDate(),
                sub.getOriginStation(),
                sub.getDestinationStation()
        );
    }

    @Override
    public void sendMessage(long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            log.warn("Не удалось отправить сообщение: {}", e.getMessage());
        }
    }

    @Override
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

    @Override
    public void sendMessageWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
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
}