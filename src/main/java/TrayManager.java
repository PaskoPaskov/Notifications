import java.awt.*;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrayManager {
    private TrayIcon trayIcon;
    private LogErrors logErrors;
    ScheduledExecutorService scheduledExecutorService;
    ConfigManager configManager;
    SetupWizard setupWizard;

    public TrayManager(LogErrors logErrors, ScheduledExecutorService executor, ConfigManager configManager, SetupWizard setupWizard) {
        this.logErrors = logErrors;
        this.scheduledExecutorService = executor;
        this. configManager = configManager;
        this.setupWizard = setupWizard;
    }

    // Initialize system tray icon and menu
    public void init() {
        if (!SystemTray.isSupported()) {
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit()
                    .getImage(TrayManager.class.getResource("/icon.png"));

            PopupMenu menu = new PopupMenu();

            MenuItem reconfigure = new MenuItem("Reconfigure");
            reconfigure.addActionListener(e -> reconfigureApp());

            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(e -> {
                stopScheduler();
                System.exit(0);
            });

            menu.add(reconfigure);
            menu.addSeparator();
            menu.add(exit);


            trayIcon = new TrayIcon(image, "MobileBgScanner", menu);
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);

        } catch (Exception e) {
            logErrors.log("TrayManager", "ERROR", "Failed to init tray", e);
        }
    }

    // Restart the application with the specified command-line argument
    private void reconfigureApp() {
//        if (!isRunningFromExe()) {
//            if (trayIcon != null) {
//                trayIcon.displayMessage(
//                        "Restart unavailable",
//                        "Restart works only in installed version.",
//                        TrayIcon.MessageType.INFO
//                );
//                return;
//            }
//        }


        stopScheduler();
        configManager.clearProperties();
        setupWizard.runIfNeeded();

    }

    // Gracefully stop the scheduled executor service
    private void stopScheduler() {
        if (scheduledExecutorService == null) return;

        scheduledExecutorService.shutdown();
        try {
            if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Check whether the application is running from a Windows executable
//    private boolean isRunningFromExe() {
//        String path = getExecutablePath();
//        return path.toLowerCase().endsWith(".exe");
//    }

    // Resolve the absolute path of the current executable or JAR
    private String getExecutablePath() {
        try {
            return new File(
                    TrayManager.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve executable path", e);
        }
    }
}

