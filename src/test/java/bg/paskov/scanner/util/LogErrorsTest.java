package bg.paskov.scanner.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogErrorsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateLogFileIfNotExists() {
        Path logFile = tempDir.resolve("test.log");
        LogErrors logger = new LogErrors(logFile);

        logger.log("TestModule", "INFO", "Test Message", null);

        assertTrue(Files.exists(logFile));
    }

    @Test
    void shouldWriteMessageToLogFile() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        LogErrors logger = new LogErrors(logFile);

        logger.log("TestModule", "INFO", "Test Message", null);

        List<String> lines = Files.readAllLines(logFile);
        assertTrue(lines.stream().anyMatch(line -> line.contains("[TestModule] [INFO] Test Message")));
    }

    @Test
    void shouldWriteExceptionDetailsWhenExceptionIsProvided() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        LogErrors logger = new LogErrors(logFile);

        Exception exception = new RuntimeException("something went wrong");
        logger.log("TestModule", "ERROR", "Test Message", exception);

        List<String> lines = Files.readAllLines(logFile);
        assertTrue(lines.stream().anyMatch(line -> line.contains("something went wrong")));
    }

    @Test
    void shouldAppendToExistingFile() throws Exception {
        Path logFile = tempDir.resolve("test.log");
        LogErrors logger = new LogErrors(logFile);

        logger.log("TestModule", "INFO", "First Message", null);
        logger.log("TestModule", "INFO", "Second Message", null);

        List<String> lines = Files.readAllLines(logFile);

        assertEquals(4, lines.size());

    }


}
