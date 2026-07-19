package ru.chepikov.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface BotMessageSender {

    void sendMessage(long chatId, String text);

    void sendHtmlMessage(long chatId, String text);

    void sendMessageWithKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard);
}
