package ru.chepikov.state_machine;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

import java.util.EnumSet;

@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class UserInputStateMachineConfig extends StateMachineConfigurerAdapter<BotStates, BotEvents> {

    private final Action<BotStates, BotEvents> dateAction;
    private final Action<BotStates, BotEvents> originAction;
    private final Action<BotStates, BotEvents> destinationAction;

    @Override
    public void configure(StateMachineConfigurationConfigurer<BotStates, BotEvents> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(new StateMachineListenerAdapter<>());
    }

    @Override
    public void configure(StateMachineStateConfigurer<BotStates, BotEvents> states) throws Exception {
        states
                .withStates()
                .initial(BotStates.START)
                .states(EnumSet.allOf(BotStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BotStates, BotEvents> transitions) throws Exception {
        transitions
                .withExternal()
                .source(BotStates.WAITING_FOR_DATE)
                .target(BotStates.WAITING_FOR_ORIGIN)
                .event(BotEvents.RECEIVE_DATE)
                .action(dateAction)
                .and()

                .withExternal()
                .source(BotStates.WAITING_FOR_ORIGIN)
                .target(BotStates.WAITING_FOR_DESTINATION)
                .event(BotEvents.RECEIVE_ORIGIN)
                .action(originAction)
                .and()

                .withExternal()
                .source(BotStates.WAITING_FOR_DESTINATION)
                .target(BotStates.START)
                .event(BotEvents.RECEIVE_DESTINATION)
                .action(destinationAction)
                .and()

                .withExternal()
                .source(BotStates.WAITING_FOR_DATE)
                .target(BotStates.START)
                .event(BotEvents.CANCEL)
                .and()

                .withExternal()
                .source(BotStates.WAITING_FOR_ORIGIN)
                .target(BotStates.START)
                .event(BotEvents.CANCEL)
                .and()

                .withExternal()
                .source(BotStates.WAITING_FOR_DESTINATION)
                .target(BotStates.START)
                .event(BotEvents.CANCEL);
    }
}