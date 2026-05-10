package ru.chepikov.action;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import ru.chepikov.state_machine.BotEvents;
import ru.chepikov.state_machine.BotStates;

@Component("originAction")
public class ReceiveOriginAction implements Action<BotStates, BotEvents> {

    @Override
    public void execute(StateContext<BotStates, BotEvents> context) {
        String station = context.getExtendedState().get("message", String.class);
        
        context.getExtendedState().getVariables().put("originStation", station);
        context.getExtendedState().getVariables().put("result", "Введите станцию прибытия:");
    }
}