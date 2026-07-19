package ru.chepikov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chepikov.model.Station;
import ru.chepikov.model.TrainSubscription;
import ru.chepikov.model.dto.route.RouteDto;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SubscriptionChecker {

    private static final int SUBSCRIPTION_BATCH_SIZE = 100;

    private final SubscriptionService subscriptionService;
    private final StationService stationService;
    private final TrainApiService trainApiService;
    private final ObjectMapper objectMapper;
    private final RzdBot rzdBot;
    private final RzdTicketLinkService rzdTicketLinkService;

    private final ThreadPoolTaskExecutor subscriptionCheckExecutor;
    private final AtomicBoolean checkingInProgress = new AtomicBoolean(false);

    public SubscriptionChecker(SubscriptionService subscriptionService,
                               StationService stationService,
                               TrainApiService trainApiService,
                               ObjectMapper objectMapper,
                               RzdBot rzdBot,
                               RzdTicketLinkService rzdTicketLinkService,
                               @Qualifier("subscriptionCheckExecutor") ThreadPoolTaskExecutor subscriptionCheckExecutor) {
        this.subscriptionService = subscriptionService;
        this.stationService = stationService;
        this.trainApiService = trainApiService;
        this.objectMapper = objectMapper;
        this.rzdBot = rzdBot;
        this.rzdTicketLinkService = rzdTicketLinkService;
        this.subscriptionCheckExecutor = subscriptionCheckExecutor;
    }

    @Scheduled(fixedDelayString = "${rzd.subscription-check.fixed-delay-ms:60000}")
    public void checkAllSubscriptions() {
        if (!checkingInProgress.compareAndSet(false, true)) {
            log.debug("Subscription check skipped because previous cycle is still running");
            return;
        }

        try {
            int pageNumber = 0;
            Page<TrainSubscription> page;

            do {
                page = subscriptionService.findPage(PageRequest.of(pageNumber, SUBSCRIPTION_BATCH_SIZE));
                List<Future<?>> futures = new ArrayList<>();
                for (TrainSubscription subscription : page.getContent()) {
                    futures.add(subscriptionCheckExecutor.submit(() -> checkSubscriptionSafely(subscription)));
                }
                waitForBatch(futures);
                pageNumber++;
            } while (page.hasNext());
        } finally {
            checkingInProgress.set(false);
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

    private void waitForBatch(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Subscription check task failed: {}", e.getMessage(), e);
            }
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
            if (subscriptionService.updateHashcodeIfCurrent(subscription, routeHash)) {
                rzdBot.sendHtmlMessage(subscription.getUserId(), formatNotification(route, origin, destination));
            } else {
                log.debug("Subscription {} was already updated by another worker", subscription.getId());
            }
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
