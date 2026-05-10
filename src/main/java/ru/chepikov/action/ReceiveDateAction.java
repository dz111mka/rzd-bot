package ru.chepikov.action;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import ru.chepikov.state_machine.BotEvents;
import ru.chepikov.state_machine.BotStates;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component("dateAction")
public class ReceiveDateAction implements Action<BotStates, BotEvents> {

    @Override
    public void execute(StateContext<BotStates, BotEvents> context) {
        String message = context.getExtendedState().get("message", String.class);
        
        context.getExtendedState().getVariables().put("error", false);
        
        try {
            LocalDate date = LocalDate.parse(message);
            context.getExtendedState().getVariables().put("parsedDate", date);
            context.getExtendedState().getVariables().put("result", "Введите станцию отправления:");
        } catch (DateTimeParseException e) {
            context.getExtendedState().getVariables().put("error", true);
            context.getExtendedState().getVariables().put("result", "Неверный формат даты. Пример: 2024-12-25");
        }
    }
}