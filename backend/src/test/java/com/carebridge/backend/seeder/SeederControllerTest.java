package com.carebridge.backend.seeder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeederControllerTest {

    @Mock
    private SeederService seederService;

    @InjectMocks
    private SeederController controller;

    @Test
    void seedRepository_ShouldReturnMessageAndCallService() {
        ResponseEntity<String> response = controller.seedRepository();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Repository successfully seeded with 15 fully assigned patients.", response.getBody());
        verify(seederService, times(1)).seedRepository();
    }

    @Test
    void unseedRepository_ShouldReturnMessageAndCallService() {
        ResponseEntity<String> response = controller.unseedRepository();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("All data has been cleared from the repository.", response.getBody());
        verify(seederService, times(1)).unseedRepository();
    }

    @Test
    void startLoop_ShouldReturnMessageAndCallService() {
        ResponseEntity<String> response = controller.startLoop();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Async loop started! WebSockets should start receiving data.", response.getBody());
        verify(seederService, times(1)).startLoop();
    }

    @Test
    void stopLoop_ShouldReturnMessageAndCallService() {
        ResponseEntity<String> response = controller.stopLoop();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Async loop stopped.", response.getBody());
        verify(seederService, times(1)).stopLoop();
    }
}

