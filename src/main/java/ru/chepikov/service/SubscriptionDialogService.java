package ru.chepikov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.chepikov.model.Station;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.state_machine.BotEvents;
import ru.chepikov.state_machine.BotStates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionDialogService {

    private static final String STATION_CALLBACK_PREFIX = "STATION:";
    private static final String ORIGIN_STEP = "ORIGIN";
    private static final String DESTINATION_STEP = "DESTINATION";

    private final SubscriptionService subscriptionService;
    private final StationService stationService;
    private final StateMachineFactory<BotStates, BotEvents> stateMachineFactory;

    private final Map<Long, TrainSubscription> pendingSubscriptions = new ConcurrentHashMap<>();
    private final Map<Long, StateMachine<BotStates, BotEvents>> userStateMachines = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    public void start(long chatId, BotMessageSender sender) {
        withUserLock(chatId, () -> {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            StateMachine<BotStates, BotEvents> sm = stateMachineFactory.getStateMachine();

            resetState(sm, BotStates.WAITING_FOR_DATE);
            userStateMachines.put(chatId, sm);

            sender.sendMessage(chatId, "Введите дату в формате YYYY-MM-DD (например, " + today + "):");
        });
    }

    public void cancel(long chatId, BotMessageSender sender) {
        withUserLock(chatId, () -> {
            userStateMachines.remove(chatId);
            pendingSubscriptions.remove(chatId);
            sender.sendMessage(chatId, "Операция отменена.");
        });
    }

    public void handleUserInput(long chatId, String input, BotMessageSender sender) {
        withUserLock(chatId, () -> handleUserInputLocked(chatId, input, sender));
    }

    public boolean handleCallback(long chatId, String data, BotMessageSender sender) {
        if (!data.startsWith(STATION_CALLBACK_PREFIX)) {
            return false;
        }

        withUserLock(chatId, () -> handleStationChoice(chatId, data, sender));
        return true;
    }

    private void handleUserInputLocked(long chatId, String input, BotMessageSender sender) {
        StateMachine<BotStates, BotEvents> sm = userStateMachines.get(chatId);
        if (sm == null) {
            return;
        }

        BotStates state = sm.getState().getId();
        if (state == BotStates.START) {
            sender.sendMessage(chatId, "Нажмите /subscribe для начала.");
            return;
        }

        if (state == BotStates.WAITING_FOR_ORIGIN || state == BotStates.WAITING_FOR_DESTINATION) {
            handleStationInput(chatId, sm, state, input, sender);
            return;
        }

        sm.getExtendedState().getVariables().put("message", input);
        boolean accepted = sm.sendEvent(getEventForState(state));
        if (!accepted) {
            return;
        }

        sendActionResult(chatId, sm, sender);
        handleDateResult(chatId, sm, state);
    }

    private void handleDateResult(long chatId, StateMachine<BotStates, BotEvents> sm, BotStates state) {
        if (state != BotStates.WAITING_FOR_DATE) {
            return;
        }

        LocalDate date = sm.getExtendedState().get("parsedDate", LocalDate.class);
        Boolean error = sm.getExtendedState().get("error", Boolean.class);
        if (date != null && !Boolean.TRUE.equals(error)) {
            createPendingSubscription(chatId, date);
        } else if (Boolean.TRUE.equals(error)) {
            resetState(sm, BotStates.WAITING_FOR_DATE);
        }
    }

    private void handleStationInput(long chatId,
                                    StateMachine<BotStates, BotEvents> sm,
                                    BotStates state,
                                    String input,
                                    BotMessageSender sender) {
        List<Station> stations = stationService.searchByName(input, 8);

        if (stations.isEmpty()) {
            sender.sendMessage(chatId, "Станция не найдена. Попробуйте уточнить название.");
            return;
        }

        Optional<Station> exactMatch = stations.stream()
                .filter(station -> station.getName().equalsIgnoreCase(input.trim()))
                .findFirst();

        if (exactMatch.isPresent()) {
            acceptStationInput(chatId, sm, state, exactMatch.get().getName(), sender);
            return;
        }

        if (stations.size() == 1) {
            acceptStationInput(chatId, sm, state, stations.get(0).getName(), sender);
            return;
        }

        showStationOptions(chatId, state, stations, sender);
    }

    private void showStationOptions(long chatId, BotStates state, List<Station> stations, BotMessageSender sender) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        String step = state == BotStates.WAITING_FOR_ORIGIN ? ORIGIN_STEP : DESTINATION_STEP;

        for (Station station : stations) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(station.getName())
                    .callbackData(STATION_CALLBACK_PREFIX + step + ":" + station.getId())
                    .build();
            rows.add(List.of(button));
        }

        keyboard.setKeyboard(rows);
        sender.sendMessageWithKeyboard(chatId, "Выберите станцию:", keyboard);
    }

    private void handleStationChoice(long chatId, String data, BotMessageSender sender) {
        String[] parts = data.split(":");
        if (parts.length != 3) {
            return;
        }

        StateMachine<BotStates, BotEvents> sm = userStateMachines.get(chatId);
        if (sm == null) {
            return;
        }

        BotStates state = sm.getState().getId();
        if ((ORIGIN_STEP.equals(parts[1]) && state != BotStates.WAITING_FOR_ORIGIN)
                || (DESTINATION_STEP.equals(parts[1]) && state != BotStates.WAITING_FOR_DESTINATION)) {
            sender.sendMessage(chatId, "Этот выбор уже неактуален. Продолжите текущий шаг.");
            return;
        }

        Station station = stationService.findById(Integer.valueOf(parts[2]));
        acceptStationInput(chatId, sm, state, station.getName(), sender);
    }

    private void acceptStationInput(long chatId,
                                    StateMachine<BotStates, BotEvents> sm,
                                    BotStates state,
                                    String stationName,
                                    BotMessageSender sender) {
        sm.getExtendedState().getVariables().put("message", stationName);

        boolean accepted = sm.sendEvent(getEventForState(state));
        if (!accepted) {
            return;
        }

        sendActionResult(chatId, sm, sender);

        Boolean error = sm.getExtendedState().get("error", Boolean.class);
        if (state == BotStates.WAITING_FOR_DESTINATION && !Boolean.TRUE.equals(error)) {
            completeSubscription(chatId, sm);
        }
    }

    private void sendActionResult(long chatId, StateMachine<BotStates, BotEvents> sm, BotMessageSender sender) {
        String result = sm.getExtendedState().get("result", String.class);
        if (result != null) {
            sender.sendMessage(chatId, result);
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

    private void completeSubscription(long chatId, StateMachine<BotStates, BotEvents> sm) {
        TrainSubscription sub = pendingSubscriptions.get(chatId);
        if (sub == null) {
            log.warn("Pending subscription not found for {}", chatId);
            return;
        }

        String origin = sm.getExtendedState().get("originStation", String.class);
        String destination = sm.getExtendedState().get("destinationStation", String.class);

        sub.setOriginStation(origin);
        sub.setDestinationStation(destination);

        if (subscriptionService.saveIfAbsent(sub).isEmpty()) {
            log.info("Subscription already exists: {}", sub);
        }
        pendingSubscriptions.remove(chatId);

        log.info("Subscription saved: {}", sub);
    }

    private void resetState(StateMachine<BotStates, BotEvents> sm, BotStates state) {
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(accessor -> accessor.resetStateMachine(
                new DefaultStateMachineContext<>(state, null, null, null)
        ));
        sm.start();
    }

    private void withUserLock(long chatId, Runnable action) {
        ReentrantLock lock = userLocks.computeIfAbsent(chatId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
}
