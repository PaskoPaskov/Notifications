package bg.paskov.scanner.config;

import bg.paskov.scanner.util.LogErrors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {

    private final Properties properties = new Properties();
    private final Path configPath;
    private final LogErrors logErrors;

    public ConfigManager(LogErrors logErrors, Path configPath) {
        this.logErrors = logErrors;
        this.configPath = configPath;
    }

    // Initialize configuration by loading properties if file exists
    public void init() {
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                properties.load(in);
            } catch (IOException e) {
                logErrors.log("bg.paskov.scanner.config.ConfigManager", "ERROR", "Failed to load config", e);
            }
        }
    }

    // Check if configuration contains all required fields
    public boolean isValidConfiguration() {
        return getConfigValue("email.from") != null
                && getConfigValue("email.to") != null
                && getConfigValue("email.password") != null
                && getConfigValue("scan.url") != null;
    }

    // Save configuration to file
    public void save() {
        try (OutputStream out = Files.newOutputStream(configPath)) {
            properties.store(out, "bg.paskov.scanner.service.MobileBgScanner configuration");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    // Setters
    public void setProperties(String key, String value) {
        properties.setProperty(key, value);
    }

    // Getters
    public String getConfigValue(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key, int defaultValue) {
        String value = getConfigValue(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    // Clear configuration and delete file
    public void clearProperties() {
        try {
            Files.deleteIfExists(configPath);
            properties.clear();

            logErrors.log(
                    "bg.paskov.scanner.config.ConfigManager",
                    "INFO",
                    "Configuration cleared",
                    null
            );

        } catch (IOException e) {
            logErrors.log(
                    "bg.paskov.scanner.config.ConfigManager",
                    "ERROR",
                    "Failed to clear configuration",
                    e
            );
        }
    }

}
