package ru.chepikov.state_machine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class BotStateMachineConfig extends EnumStateMachineConfigurerAdapter<BotStates, BotEvents> {

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
                .source(BotStates.START).target(BotStates.WAITING_FOR_DATE)
                .event(BotEvents.RECEIVE_DATE)
                .and()
                .withExternal()
                .source(BotStates.WAITING_FOR_DATE).target(BotStates.WAITING_FOR_ORIGIN)
                .event(BotEvents.RECEIVE_ORIGIN)
                .and()
                .withExternal()
                .source(BotStates.WAITING_FOR_ORIGIN).target(BotStates.WAITING_FOR_DESTINATION)
                .event(BotEvents.RECEIVE_DESTINATION)
                .and()
                .withExternal()
                .source(BotStates.WAITING_FOR_DESTINATION).target(BotStates.START)
                .event(BotEvents.CANCEL);
    }
}
