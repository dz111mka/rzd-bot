package ru.chepikov.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.chepikov.model.DescriptionCarrier;
import ru.chepikov.repository.DescriptionCarrierRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DescriptionCarrierServiceTest {

    @Mock
    private DescriptionCarrierRepository repository;

    @InjectMocks
    private DescriptionCarrierService service;

    @Test
    void findAll_shouldReturnEmptyListWhenNoData() {
        // when
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // then
        List<DescriptionCarrier> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnListWhenDataIsFound() {
        // given
        DescriptionCarrier descriptionCarrier1 = new DescriptionCarrier("luxury", "test");
        DescriptionCarrier descriptionCarrier2 = new DescriptionCarrier("luxury", "test");

        // when
        when(repository.findAll()).thenReturn(List.of(descriptionCarrier1, descriptionCarrier2));

        // then
        List<DescriptionCarrier> result = service.findAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(repository, times(1)).findAll();
    }


}
