package ru.chepikov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chepikov.model.Station;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.model.dto.route.RouteDto;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class SubscriptionChecker {

    private final SubscriptionService subscriptionService;
    private final StationService stationService;
    private final TrainApiService trainApiService;
    private final ObjectMapper objectMapper;
    private final RzdBot rzdBot;
    private final RzdTicketLinkService rzdTicketLinkService;

    private final TaskExecutor subscriptionCheckExecutor;

    public SubscriptionChecker(SubscriptionService subscriptionService,
                               StationService stationService,
                               TrainApiService trainApiService,
                               ObjectMapper objectMapper,
                               RzdBot rzdBot,
                               RzdTicketLinkService rzdTicketLinkService,
                               @Qualifier("subscriptionCheckExecutor") TaskExecutor subscriptionCheckExecutor) {
        this.subscriptionService = subscriptionService;
        this.stationService = stationService;
        this.trainApiService = trainApiService;
        this.objectMapper = objectMapper;
        this.rzdBot = rzdBot;
        this.rzdTicketLinkService = rzdTicketLinkService;
        this.subscriptionCheckExecutor = subscriptionCheckExecutor;
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void checkAllSubscriptions() {
        List<TrainSubscription> subscriptions = subscriptionService.findAll();

        for (TrainSubscription subscription : subscriptions) {
            subscriptionCheckExecutor.execute(() -> checkSubscriptionSafely(subscription));
        }
    }

    @Async("scheduledDeleting")
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredSubscriptions() {
        subscriptionService.deleteExpiredData();
    }

    private void checkSubscriptionSafely(TrainSubscription subscription) {
        try {
            checkSubscription(subscription);
        } catch (Exception e) {
            log.error("Subscription check failed for {}: {}", subscription.getId(), e.getMessage(), e);
        }
    }

    private void checkSubscription(TrainSubscription subscription) throws Exception {
        Station origin = stationService.findByName(subscription.getOriginStation());
        Station destination = stationService.findByName(subscription.getDestinationStation());

        String json = trainApiService.getTrainPrices(
                origin.getId(),
                destination.getId(),
                subscription.getDepartureDate()
        );

        RouteDto route = objectMapper.readValue(json, RouteDto.class);
        route.setDate(subscription.getDepartureDate());

        int routeHash = route.toString().hashCode();
        if (!Objects.equals(subscription.getHashcode(), routeHash)) {
            rzdBot.sendHtmlMessage(subscription.getUserId(), formatNotification(route, origin, destination));
            subscription.setHashcode(routeHash);
            subscriptionService.save(subscription);
        }
    }

    private String formatNotification(RouteDto route, Station origin, Station destination) {
        return route.toFormattedString(train -> rzdTicketLinkService.buildSearchResultsUrl(
                    origin,
                    destination,
                    route.getDate(),
                    train.getTrainNumber()
        ));
    }
}
