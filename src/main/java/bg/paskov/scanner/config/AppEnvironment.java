package bg.paskov.scanner.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppEnvironment {

    private final Path appScannerDir;

    public AppEnvironment() {
        // Create application directory inside the user's home folder
        this.appScannerDir = Paths.get(
                System.getProperty("user.home"),
                ".scanner"
        );

        try {
            Files.createDirectories(appScannerDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create app directory", e);
        }
    }

    public Path getConfigPathFile() {
        return appScannerDir.resolve("config.properties");
    }

    public Path getLogPathFile() {
        return appScannerDir.resolve("error.log");
    }

    public Path getOldAdvertisementsPathFile() {
        return appScannerDir.resolve("old.csv");
    }
}
