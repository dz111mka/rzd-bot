package ru.chepikov.state_machine.action;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import ru.chepikov.state_machine.BotEvents;
import ru.chepikov.state_machine.BotStates;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReceiveDateActionTest {

    private final ReceiveDateAction action = new ReceiveDateAction();

    @Test
    void storesParsedDateForValidInput() {
        Map<Object, Object> variables = executeWithMessage("2026-08-15");

        assertThat(variables.get("error")).isEqualTo(false);
        assertThat(variables.get("parsedDate")).isEqualTo(LocalDate.of(2026, 8, 15));
    }

    @Test
    void marksErrorForInvalidInput() {
        Map<Object, Object> variables = executeWithMessage("15.08.2026");

        assertThat(variables.get("error")).isEqualTo(true);
        assertThat(variables).doesNotContainKey("parsedDate");
    }

    private Map<Object, Object> executeWithMessage(String message) {
        Map<Object, Object> variables = new HashMap<>();
        variables.put("message", message);

        ExtendedState extendedState = mock(ExtendedState.class);
        when(extendedState.get("message", String.class)).thenReturn(message);
        when(extendedState.getVariables()).thenReturn(variables);

        StateContext<BotStates, BotEvents> context = stateContext();
        when(context.getExtendedState()).thenReturn(extendedState);

        action.execute(context);

        return variables;
    }

    @SuppressWarnings("unchecked")
    private StateContext<BotStates, BotEvents> stateContext() {
        return mock(StateContext.class);
    }
}
