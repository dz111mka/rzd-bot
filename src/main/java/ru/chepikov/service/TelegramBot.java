package ru.chepikov.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
import ru.chepikov.exception.StationNotFoundException;
import ru.chepikov.model.JobOfCheck;
import ru.chepikov.model.StationInfo;
import ru.chepikov.model.dto.RouteDto;
import ru.chepikov.repository.JobOfCheckRepository;
import ru.chepikov.repository.StationInfoRepository;
import ru.chepikov.util.DescriptionCarrier;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;

    private final RZDService rzdService;

    private final StationInfoRepository stationInfoRepository;

    private final JobOfCheckRepository jobOfCheckRepository;

    private final ObjectMapper objectMapper;

    private Map<Long, String> userStates = new HashMap<>();

    private Map<Long, JobOfCheck> userJobOfCheck = new HashMap<>();

    public TelegramBot(BotConfig config, RZDService rzdService, StationInfoRepository stationInfoRepository, JobOfCheckRepository jobOfCheckRepository, ObjectMapper objectMapper) {
        this.config = config;
        this.rzdService = rzdService;
        this.stationInfoRepository = stationInfoRepository;
        this.jobOfCheckRepository = jobOfCheckRepository;
        this.objectMapper = objectMapper;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начальное сообщение"));
        listOfCommands.add(new BotCommand("/check", "Начать отслеживать"));
        listOfCommands.add(new BotCommand("/uncheck", "Перестать отслеживать"));
        listOfCommands.add(new BotCommand("/about_carrier", "Информация о типах вагонов"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: {}", e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start" -> sendMessage(chatId, "Привет " + update.getMessage().getChat().getFirstName());
                case "/uncheck" -> uncheckRoute(chatId);
                case "ЗДАРОВА ДАУН" -> sendMessage(chatId, "ЗДАРОВА ОЛЕГ");
                case "/check" -> {
                    sendMessage(chatId, "Введите дату в формате YYYY-MM-DD (например, 2024-12-30):");
                    userStates.put(chatId, "WAITING_FOR_DATE");
                }
                case "/about_carrier"  -> {
                    sendMessage(chatId, DescriptionCarrier.reservedSeat);
                    sendMessage(chatId, DescriptionCarrier.compartment);
                    sendMessage(chatId, DescriptionCarrier.luxury);
                }
                default -> handleUserInput(chatId, messageText);
            }
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();

            if (data.startsWith("REMOVE_JOB:")) {
                UUID jobId = UUID.fromString(data.split(":")[1]);
                removeJob(callbackQuery.getMessage().getChatId(), jobId);
            }
        }
    }

    private void handleUserInput(long chatId, String input) {
        String state = userStates.get(chatId);

        if ("WAITING_FOR_DATE".equals(state)) {
            LocalDate date;
            try {
                date = LocalDate.parse(input);

                sendMessage(chatId, "Введите станцию отправления:");
                userJobOfCheck.put(chatId, new JobOfCheck(null, (int) chatId, null, null, date, 0));
                userStates.put(chatId, "WAITING_FOR_ORIGIN");
            } catch (DateTimeParseException e) {
                sendMessage(chatId, "Некорректный формат даты. Пожалуйста, попробуйте еще раз.");
            }
        } else if ("WAITING_FOR_ORIGIN".equals(state)) {
            JobOfCheck userInput = userJobOfCheck.get(chatId);
            userInput.setOriginStation(input);
            sendMessage(chatId, "Введите станцию прибытия:");
            userStates.put(chatId, "WAITING_FOR_DESTINATION");
        } else if ("WAITING_FOR_DESTINATION".equals(state)) {
            JobOfCheck jobOfCheck = userJobOfCheck.get(chatId);
            jobOfCheck.setDestinationStation(input);

            saveJob(chatId, jobOfCheck);
        }
    }

    private void saveJob(long chatId, JobOfCheck userInput) {
        JobOfCheck newJob = new JobOfCheck();
        newJob.setDepartureDate(userInput.getDepartureDate());
        newJob.setOriginStation(userInput.getOriginStation());
        newJob.setDestinationStation(userInput.getDestinationStation());
        newJob.setUserId((int) chatId);
        newJob.setHashcode(0);

        jobOfCheckRepository.save(newJob);

        sendMessage(chatId, "Маршрут добавлен для отслеживания!");

        userStates.remove(chatId);
        userJobOfCheck.remove(chatId);
    }

    private void removeJob(long chatId, UUID jobId) {
        jobOfCheckRepository.deleteById(jobId);
        uncheckRoute(chatId);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.warn("Сообщение не отправлено");
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    private void uncheckRoute(long chatId) {
        List<JobOfCheck> jobOfCheckList = jobOfCheckRepository.findByUserId(chatId);
        SendMessage message = new SendMessage();
        message.setText("Что Вы хотите перестать отслеживать изменить");
        message.setChatId(chatId);

        if (jobOfCheckList.isEmpty()) {
            message.setText("У вас нет активных маршрутов для отслеживания.");
        } else {
            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

            for (JobOfCheck jobOfCheck : jobOfCheckList) {
                List<InlineKeyboardButton> rowInLine = new ArrayList<>();
                InlineKeyboardButton removeButton = new InlineKeyboardButton();
                removeButton.setText("Удалить " + jobOfCheck.getDepartureDate().toString() + "\nИз: " + jobOfCheck.getOriginStation() + "\nВ: " + jobOfCheck.getDestinationStation());
                removeButton.setCallbackData("REMOVE_JOB:" + jobOfCheck.getId()); // Добавляем ID задания
                rowInLine.add(removeButton);
                rowsInLine.add(rowInLine);
            }
            keyboardMarkup.setKeyboard(rowsInLine);
            message.setReplyMarkup(keyboardMarkup);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void jobToCheckRequests() throws JsonProcessingException, TelegramApiException {
        SendMessage message = new SendMessage();
        List<JobOfCheck> list = jobOfCheckRepository.findAll();
        for (JobOfCheck job : list) {
            Optional<StationInfo> originStationOptional = stationInfoRepository.findByName(job.getOriginStation());
            Optional<StationInfo> destinationStationOptional = stationInfoRepository.findByName(job.getDestinationStation());

            if (originStationOptional.isPresent() && destinationStationOptional.isPresent()) {
                Integer originalCode = originStationOptional.get().getId();
                Integer destinationCode = destinationStationOptional.get().getId();
                LocalDate departureDate = job.getDepartureDate();

                String string = rzdService.fetchTrainPrices(originalCode, destinationCode, departureDate);
                RouteDto routeDto = objectMapper.readValue(string, RouteDto.class);
                routeDto.setDate(job.getDepartureDate());

                if (job.getHashcode() != routeDto.hashCode()) {
                    sendMessage(job.getUserId(), routeDto.toString());
                    job.setHashcode(routeDto.hashCode());
                    jobOfCheckRepository.save(job);
                }
            } else {
                String originStationName = job.getOriginStation();
                String destinationStationName = job.getDestinationStation();
                String errorMessage = String.format("Station not found: %s, %s",
                        originStationOptional.isPresent() ? "" : originStationName,
                        destinationStationOptional.isPresent() ? "" : destinationStationName);

                log.error(errorMessage);
                throw new StationNotFoundException(errorMessage);
            }
        }
    }
}
