package bg.paskov.scanner.ui;

import bg.paskov.scanner.config.ConfigManager;
import bg.paskov.scanner.util.LogErrors;

import java.awt.*;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrayManager {
    ScheduledExecutorService scheduledExecutorService;
    ConfigManager configManager;
    SetupWizard setupWizard;
    private TrayIcon trayIcon;
    private final LogErrors logErrors;

    public TrayManager(LogErrors logErrors, ScheduledExecutorService executor, ConfigManager configManager, SetupWizard setupWizard) {
        this.logErrors = logErrors;
        this.scheduledExecutorService = executor;
        this.configManager = configManager;
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


            trayIcon = new TrayIcon(image, "bg.paskov.scanner.service.MobileBgScanner", menu);
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);

        } catch (Exception e) {
            logErrors.log("bg.paskov.scanner.ui.TrayManager", "ERROR", "Failed to init tray", e);
        }
    }

    // Restart the application with the specified command-line argument
    private void reconfigureApp() {
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

