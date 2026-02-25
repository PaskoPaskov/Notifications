package bg.paskov.scanner;

import bg.paskov.scanner.config.ConfigManager;
import bg.paskov.scanner.notification.EmailSender;
import bg.paskov.scanner.notification.Notifiable;
import bg.paskov.scanner.notification.TypesNotifire;
import bg.paskov.scanner.service.MobileBgScanner;
import bg.paskov.scanner.service.SiteScanner;
import bg.paskov.scanner.ui.SetupWizard;
import bg.paskov.scanner.ui.TrayManager;
import bg.paskov.scanner.util.LogErrors;

import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private final ConfigManager configManager;
    private final SetupWizard setupWizard;
    private final TrayManager trayManager;
    private final LogErrors logErrors;
    private final Path oldAdvertisementPath;
    private final ScheduledExecutorService scheduledExecutorService;

    public Application(ConfigManager configManager, SetupWizard setupWizard, TrayManager trayManager, LogErrors logErrors, Path oldAdvertisementPath, ScheduledExecutorService scheduledExecutorService) {
        this.configManager = configManager;
        this.setupWizard = setupWizard;
        this.trayManager = trayManager;
        this.logErrors = logErrors;
        this.oldAdvertisementPath = oldAdvertisementPath;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public void start(String[] args) {

        configManager.init();

        if (isReconfigure(args)) {
            configManager.clearProperties();
        }

        // Run initial setup wizard if configuration is missing
        setupWizard.runIfNeeded();

        if (!configManager.isValidConfiguration()) {
            logErrors.log(
                    "bg.paskov.scanner.Application",
                    "INFO",
                    "bg.paskov.scanner.Application terminated: configuration not completed",
                    null
            );
            System.exit(0);
        }

        trayManager.init();
        startMobileBgScanner();
    }

    private boolean isReconfigure(String[] args) {
        return args.length > 0 && "--reconfigure".equals(args[0]);
    }

    private void startMobileBgScanner() {

        String from = configManager.getConfigValue("email.from");
        String to = configManager.getConfigValue("email.to");
        String password = configManager.getConfigValue("email.password");
        String url = configManager.getConfigValue("scan.url");

        int interval = configManager.getInt("scan.interval.days", 1);

        Notifiable notifier =
                new EmailSender(from, password, to, TypesNotifire.WITH_RETRY, logErrors);

        SiteScanner mobileBgScanner =
                new MobileBgScanner(url, notifier, logErrors, oldAdvertisementPath);

        // Schedule periodic scanning at fixed intervals
        scheduledExecutorService.scheduleAtFixedRate(
                mobileBgScanner::scan,
                0,
                interval,
                TimeUnit.DAYS
        );
    }

}



