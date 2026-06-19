package bg.paskov.scanner;

import bg.paskov.scanner.config.ConfigManager;
import bg.paskov.scanner.ui.SetupWizard;
import bg.paskov.scanner.ui.TrayManager;
import bg.paskov.scanner.util.LogErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationTest {
    @TempDir
    private Path tempDir;

    @Mock
    private ConfigManager configManager;

    @Mock
    private SetupWizard setupWizard;

    @Mock
    private TrayManager trayManager;

    @Mock
    private LogErrors logErrors;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    private Path oldAdvertisementPath;
    private Application application;

    @BeforeEach
    void setUp() {
        oldAdvertisementPath = tempDir.resolve("old.csv");
        application = spy(new Application(configManager, setupWizard, trayManager, logErrors, oldAdvertisementPath, scheduledExecutorService));
        // doNothing().when(application).exitApplication();
    }

    @Test
    void startShouldInitializeConfig() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[]{});

        verify(configManager).init();
    }

    @Test
    void startShouldRunSetupWizard() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[]{});

        verify(setupWizard).runIfNeeded();
    }

    @Test
    void startShouldInitTrayManagerWhenConfigIsValid() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[]{});

        verify(trayManager).init();
    }

    @Test
    void startShouldNotExitWhenConfigIsValid() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[]{});

        verify(application, never()).exitApplication();
    }

    @Test
    void startShouldFollowCorrectOrder() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[]{});

        var inOrder = inOrder(configManager, setupWizard, trayManager, scheduledExecutorService);
        inOrder.verify(configManager).init();
        inOrder.verify(setupWizard).runIfNeeded();
        inOrder.verify(configManager).isValidConfiguration();
        inOrder.verify(trayManager).init();
        inOrder.verify(scheduledExecutorService).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void startShouldExitWhenConfigIsInvalid() {
        when(configManager.isValidConfiguration()).thenReturn(false);
        doNothing().when(application).exitApplication();

        application.start(new String[]{});

        verify(application).exitApplication();
    }

    @Test
    void startShouldNotInitTrayManagerWhenConfigIsInvalid() {
        when(configManager.isValidConfiguration()).thenReturn(false);
        doNothing().when(application).exitApplication();

        application.start(new String[]{});

        verify(trayManager, never()).init();
    }

    @Test
    void startShouldLogWhenConfigIsInvalid() {
        when(configManager.isValidConfiguration()).thenReturn(false);
        doNothing().when(application).exitApplication();

        application.start(new String[]{});

        verify(logErrors).log(eq("bg.paskov.scanner.Application"), eq("INFO"), anyString(), isNull());
    }

    @Test
    void startShouldClearConfigWhenReconfigureArgPassed() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[] {"--reconfigure"});

        verify(configManager).clearProperties();
    }

    @Test
    void startShouldNotClearConfigWhenNoArgsPassed() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[] {});

        verify(configManager, never()).clearProperties();
    }

    @Test
    void startShouldNotClearConfigWhenUnknownArgPassed() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[] {"--unknown"});

        verify(configManager, never()).clearProperties();
    }

    @Test
    void startShouldClearConfigBeforeSetupWizardWhenReconfigure() {
        when(configManager.isValidConfiguration()).thenReturn(true);

        application.start(new String[] {"--reconfigure"});

      var inOrder =  inOrder(configManager, setupWizard);
      inOrder.verify(configManager).clearProperties();
      inOrder.verify(setupWizard).runIfNeeded();
    }

    @Test
    void startShouldScheduleMobileBgScanner() {
        when(configManager.isValidConfiguration()).thenReturn(true);
        when(configManager.getConfigValue("email.from")).thenReturn("from@test.com");
        when(configManager.getConfigValue("email.to")).thenReturn("to@test.com");
        when(configManager.getConfigValue("email.password")).thenReturn("pass");
        when(configManager.getConfigValue("scan.url")).thenReturn("http://test.com");
        when(configManager.getInt("scan.interval.days", 1)).thenReturn(2);

        application.start(new String[]{});

        verify(scheduledExecutorService).scheduleAtFixedRate(
                any(Runnable.class),
                eq(0L),
                eq(2L),
                eq(java.util.concurrent.TimeUnit.DAYS)
        );
    }
}
