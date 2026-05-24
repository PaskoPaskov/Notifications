package bg.paskov.scanner.ui;

import bg.paskov.scanner.config.ConfigManager;
import bg.paskov.scanner.util.LogErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrayManagerTest {

    @Mock
    private LogErrors logErrors;
    @Mock
    private ScheduledExecutorService scheduledExecutorService;
    @Mock
    private ConfigManager configManager;
    @Mock
    private SetupWizard setupWizard;

    private TrayManager trayManager;

    // Helper method to invoke the private stopScheduler() method via reflection.
    private void invokeStopScheduler(TrayManager instance) throws Exception {
        Method method = TrayManager.class.getDeclaredMethod("stopScheduler");
        method.setAccessible(true);
        method.invoke(instance);
    }

    // Helper method to invoke the private reconfigureApp() method via reflection.
    private void invokeReconfigureApp(TrayManager instance) throws Exception {
        Method method = TrayManager.class.getDeclaredMethod("reconfigureApp");
        method.setAccessible(true);
        method.invoke(instance);
    }

    @BeforeEach
    void setUp() {
        trayManager = new TrayManager(logErrors, scheduledExecutorService, configManager, setupWizard);
    }

    @Test
    void initShouldNotThrowInHeadlessEnvironment() {
        assertDoesNotThrow(() -> trayManager.init());
    }

    @Test
    void stopSchedulerShouldShutdownGracefullyWhenTerminatesInTime() throws Exception {
        when(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(true);

        invokeStopScheduler(trayManager);

        verify(scheduledExecutorService).shutdown();
        verify(scheduledExecutorService).awaitTermination(10, TimeUnit.SECONDS);
        verify(scheduledExecutorService, never()).shutdownNow();
    }

    @Test
    void stopSchedulerShouldForceStopWhenNotTerminatingInTime() throws Exception {
        when(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(false);

        invokeStopScheduler(trayManager);

        verify(scheduledExecutorService).shutdown();
        verify(scheduledExecutorService).awaitTermination(10, TimeUnit.SECONDS);
        verify(scheduledExecutorService).shutdownNow();
    }

    @Test
    void stopSchedulerShouldForceStopOnInterruptedException() throws Exception {
        when(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        invokeStopScheduler(trayManager);

        verify(scheduledExecutorService).shutdownNow();
    }

    @Test
    void stopSchedulerShouldDoNothingWhenExecutorIsNull() throws Exception {
        TrayManager managerWithNullExecutor = new TrayManager(logErrors, null, configManager, setupWizard);

        invokeStopScheduler(managerWithNullExecutor);

        // Null executor — nothing should be invoked on the mock
        verifyNoInteractions(scheduledExecutorService);
    }

    @Test
    void reconfigureAppShouldStopScheduler() throws Exception {
        when(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(true);

        invokeReconfigureApp(trayManager);

        verify(scheduledExecutorService).shutdown();
    }

    @Test
    void reconfigureAppShouldClearConfiguration() throws Exception {
        when(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(true);

        invokeReconfigureApp(trayManager);

        verify(configManager).clearProperties();
    }
    @Test
    void reconfigureAppShouldRunSetupWizard() throws Exception {
        when(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(true);

        invokeReconfigureApp(trayManager);

        verify(setupWizard).runIfNeeded();
    }

    @Test
    void reconfigureAppShouldRunSetupWizardAfterClearingConfig() throws Exception {
        when(scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(true);

        invokeReconfigureApp(trayManager);

        // Verify invocation order: clearProperties() must be called before runIfNeeded()
        var inOrder = inOrder(configManager, setupWizard);
        inOrder.verify(configManager).clearProperties();
        inOrder.verify(setupWizard).runIfNeeded();
    }
}