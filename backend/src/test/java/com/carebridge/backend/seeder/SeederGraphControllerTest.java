package com.carebridge.backend.seeder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeederGraphControllerTest {

    @Mock
    private SeederService seederService;

    @InjectMocks
    private SeederGraphController controller;

    @Test
    void seedRepository_ShouldDelegateToService() {
        Boolean result = controller.seedRepository();

        assertTrue(result);
        verify(seederService, times(1)).seedRepository();
    }

    @Test
    void unseedRepository_ShouldDelegateToService() {
        Boolean result = controller.unseedRepository();

        assertTrue(result);
        verify(seederService, times(1)).unseedRepository();
    }

    @Test
    void startSeederLoop_ShouldDelegateToService() {
        Boolean result = controller.startSeederLoop();

        assertTrue(result);
        verify(seederService, times(1)).startLoop();
    }

    @Test
    void stopSeederLoop_ShouldDelegateToService() {
        Boolean result = controller.stopSeederLoop();

        assertTrue(result);
        verify(seederService, times(1)).stopLoop();
    }
}

