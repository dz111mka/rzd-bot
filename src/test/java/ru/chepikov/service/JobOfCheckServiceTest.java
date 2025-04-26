package ru.chepikov.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.chepikov.model.JobOfCheck;
import ru.chepikov.repository.JobOfCheckRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobOfCheckServiceTest {

    @InjectMocks
    JobOfCheckService service;

    @Mock
    JobOfCheckRepository repository;


    @Test
    void findAllJobs_ShouldReturnAllJobs() {
        // Arrange
        JobOfCheck job1 = new JobOfCheck();
        JobOfCheck job2 = new JobOfCheck();
        when(repository.findAll()).thenReturn(List.of(job1, job2));

        // Act
        List<JobOfCheck> result = service.findAllJobs();

        // Assert
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    /*@Test
    void findByUserId_ShouldReturnJobsForUser() {
        // Arrange
        long userId = 1L;
        JobOfCheck job1 = new JobOfCheck();
        job1.setUserId(userId);
        JobOfCheck job2 = new JobOfCheck();
        job2.setUserId(userId);
        when(repository.findByUserId(userId)).thenReturn(List.of(job1, job2));

        // Act
        List<JobOfCheck> result = service.findByUserId(userId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(job -> job.getUserId() == userId));
        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    void save_ShouldReturnSavedJob() {
        // Arrange
        JobOfCheck jobToSave = new JobOfCheck();
        jobToSave.setUserId(1L);
        when(repository.save(jobToSave)).thenReturn(jobToSave);

        // Act
        JobOfCheck result = service.save(jobToSave);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(repository, times(1)).save(jobToSave);
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        // Arrange
        UUID id = UUID.randomUUID();
        doNothing().when(repository).deleteById(id);

        // Act
        service.deleteById(id);

        // Assert
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void deleteById_ShouldNotThrowWhenJobNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        doNothing().when(repository).deleteById(nonExistentId);

        // Act & Assert
        assertDoesNotThrow(() -> service.deleteById(nonExistentId));
        verify(repository, times(1)).deleteById(nonExistentId);
    }*/

}