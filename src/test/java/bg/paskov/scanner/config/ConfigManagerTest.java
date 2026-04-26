package bg.paskov.scanner.config;

import bg.paskov.scanner.util.LogErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigManagerTest {

    @TempDir
    Path tempDir;

    private Path pathConfigFile;
    private LogErrors logErrors;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        pathConfigFile = tempDir.resolve("config.properties");
        logErrors = new LogErrors(tempDir.resolve("error.log"));
        configManager = new ConfigManager(logErrors, pathConfigFile);
    }

    @Test
    void initShouldNotThrowWhenFileIsMissing() {
        assertDoesNotThrow(() -> configManager.init());
    }

    @Test
    void initShouldLoadPropertiesFromFile() throws Exception {
        Files.writeString(pathConfigFile, "email.from=test@example.com\n");

        configManager.init();

        assertEquals("test@example.com", configManager.getConfigValue("email.from"));
    }

    @Test
    void initShouldLoadAllProperties() throws Exception {
        Files.writeString(pathConfigFile,
                "email.from=from@example.com\n" +
                        "email.to=to@example.com\n" +
                        "email.password=secret\n" +
                        "scan.url=https://example.com\n"
        );

        configManager.init();

        assertEquals("from@example.com", configManager.getConfigValue("email.from"));
        assertEquals("to@example.com", configManager.getConfigValue("email.to"));
        assertEquals("secret", configManager.getConfigValue("email.password"));
        assertEquals("https://example.com", configManager.getConfigValue("scan.url"));
    }

    @Test
    void isValidConfigurationShouldReturnFalseWhenEmpty() {
        assertFalse(configManager.isValidConfiguration());
    }

    @Test
    void isValidConfigurationShouldReturnFalseWhenPartiallyFilled() {
        configManager.setProperties("email.from", "from@example.com");
        configManager.setProperties("email.to", "to@example.com");
        // email.password and scan.url are missing

        assertFalse(configManager.isValidConfiguration());
    }

    @Test
    void isValidConfigurationShouldReturnTrueWhenAllFieldsPresent() {
        configManager.setProperties("email.from",     "from@example.com");
        configManager.setProperties("email.to",       "to@example.com");
        configManager.setProperties("email.password", "secret");
        configManager.setProperties("scan.url",       "https://example.com");

        assertTrue(configManager.isValidConfiguration());
    }

    @Test
    void getConfigValueShouldReturnNullForMissingKey() {
        assertNull(configManager.getConfigValue("non.existent.key"));
    }

    @Test
    void setAndGetPropertyShouldWork() {
        configManager.setProperties("some.key", "some.value");

        assertEquals("some.value", configManager.getConfigValue("some.key"));
    }

    @Test
    void setPropertiesShouldOverwriteExistingValue() {
        configManager.setProperties("some.key", "first");
        configManager.setProperties("some.key", "second");

        assertEquals("second", configManager.getConfigValue("some.key"));
    }

    @Test
    void getIntShouldReturnParsedValue() {
        configManager.setProperties("scan.interval.days", "7");

        assertEquals(7, configManager.getInt("scan.interval.days", 1));
    }

    @Test
    void getIntShouldReturnDefaultWhenKeyMissing() {
        assertEquals(3, configManager.getInt("scan.interval.days", 3));
    }

    @Test
    void saveShouldCreateConfigFile() {
        configManager.setProperties("email.from", "from@example.com");

        configManager.save();

        assertTrue(Files.exists(pathConfigFile));
    }

    @Test
    void saveAndInitRoundTripShouldPreserveValues() {
        configManager.setProperties("email.from",     "from@example.com");
        configManager.setProperties("email.to",       "to@example.com");
        configManager.setProperties("email.password", "secret");
        configManager.setProperties("scan.url",       "https://example.com");
        configManager.save();

        // Create a fresh instance and load from the same file
        ConfigManager freshManager = new ConfigManager(logErrors, pathConfigFile);
        freshManager.init();

        assertEquals("from@example.com",   freshManager.getConfigValue("email.from"));
        assertEquals("to@example.com",     freshManager.getConfigValue("email.to"));
        assertEquals("secret",             freshManager.getConfigValue("email.password"));
        assertEquals("https://example.com", freshManager.getConfigValue("scan.url"));
    }

    @Test
    void clearPropertiesShouldDeleteFile() {
        configManager.setProperties("email.from", "from@example.com");
        configManager.save();
        assertTrue(Files.exists(pathConfigFile)); // sanity check

        configManager.clearProperties();

        assertFalse(Files.exists(pathConfigFile));
    }

    @Test
    void clearPropertiesShouldClearMemory() {
        configManager.setProperties("email.from",     "from@example.com");
        configManager.setProperties("email.password", "secret");

        configManager.clearProperties();

        assertNull(configManager.getConfigValue("email.from"));
        assertNull(configManager.getConfigValue("email.password"));
    }

    @Test
    void clearPropertiesShouldNotThrowWhenFileAlreadyMissing() {
        // No save() called, so the file was never created
        assertDoesNotThrow(() -> configManager.clearProperties());
    }

    @Test
    void isValidConfigurationShouldReturnFalseAfterClear() {
        configManager.setProperties("email.from",     "from@example.com");
        configManager.setProperties("email.to",       "to@example.com");
        configManager.setProperties("email.password", "secret");
        configManager.setProperties("scan.url",       "https://example.com");

        configManager.clearProperties();

        assertFalse(configManager.isValidConfiguration());
    }
}