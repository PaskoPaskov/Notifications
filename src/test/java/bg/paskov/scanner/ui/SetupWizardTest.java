package bg.paskov.scanner.ui;

import bg.paskov.scanner.config.ConfigManager;
import bg.paskov.scanner.util.LogErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SetupWizardTest {
    @Mock
    private ConfigManager configManager;

    @Mock
    private LogErrors logErrors;

    private SetupWizard setupWizard;

    @BeforeEach
    void setUp() {
        setupWizard = new SetupWizard(configManager, logErrors);
    }

    @Test
    void runIfNeededShouldSkipWhenConfigIsValid() {

        when(configManager.isValidConfiguration()).thenReturn(true);

        setupWizard.runIfNeeded();

        verify(configManager, times(1)).isValidConfiguration();

        verify(configManager, never()).setProperties(anyString(), anyString());
    }

    @Test
    void runIfNeededShouldNotSaveWhenConfigIsValid() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        setupWizard.runIfNeeded();

        verify(configManager, never()).save();
    }

}
