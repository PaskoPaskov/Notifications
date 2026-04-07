package bg.paskov.scanner.config;

//import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AppEnvironmentTest {

    private final AppEnvironment appEnvironment = new AppEnvironment();

    @Test
    void configPathShouldEndWithCorrectFileName() {
        Path path = appEnvironment.getConfigPathFile();
        assertTrue(path.endsWith("config.properties"));
    }

    @Test
    void configPathsShouldBeAbsolute() {
        Path path = appEnvironment.getConfigPathFile();
      assertTrue(path.isAbsolute());
    }

    @Test
    void logPathShouldEndWithCorrectFileName() {
        Path path = appEnvironment.getLogPathFile();
        assertTrue(path.endsWith("error.log"));
    }

    @Test
    void logPathsShouldBeAbsolute() {
        Path path = appEnvironment.getLogPathFile();
        assertTrue(path.isAbsolute());
    }

    @Test
    void oldAdvertisementsPathShouldEndWithCorrectFileName() {
        Path path = appEnvironment.getOldAdvertisementsPathFile();
        assertTrue(path.endsWith("old.csv"));
    }

    @Test
    void oldAdvertisementsPathsShouldBeAbsolute() {
        Path path = appEnvironment.getOldAdvertisementsPathFile();
        assertTrue(path.isAbsolute());
    }


}
