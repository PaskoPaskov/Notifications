import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    public static void main(String[] args) {

        AppEnvironment appEnvironment = new AppEnvironment();
        Path configPath = appEnvironment.getConfigPathFile();
        Path logPath = appEnvironment.getLogPathFile();
        Path oldAdvertisementPath = appEnvironment.getOldAdvertisementsPathFile();

        LogErrors logErrors = new LogErrors(logPath);
        ScheduledExecutorService scheduledExecutorService =  Executors.newSingleThreadScheduledExecutor();
        ConfigManager configManager = new ConfigManager(logErrors, configPath);
        SetupWizard setupWizard = new SetupWizard(configManager, logErrors);
        TrayManager trayManager = new TrayManager(logErrors, scheduledExecutorService, configManager, setupWizard);
        Application application = new Application(configManager, setupWizard, trayManager, logErrors, oldAdvertisementPath, scheduledExecutorService);

        application.start(args);
    }
}
