package bg.paskov.scanner.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RetryExecutorTest {

    @TempDir
    private Path tempDir;
    private LogErrors logger;

    @BeforeEach
    void setUp() {
        logger = new LogErrors(tempDir.resolve("test.log"));
    }

    @Test
    void shouldExecuteActionSuccessfullyOnFirstAttempt() {
        int[] callCount = {0};
        Runnable action = () -> callCount[0]++;

        RetryExecutor.execute(action, 3, 1, "TestModule", "TestOperation", logger);

        assertEquals(1, callCount[0]);
    }

    @Test
    void shouldStopAfterMaxAttempts() {
        int[] callCount = {0};
        Runnable action = () -> {
            callCount[0]++;
            throw new RuntimeException();
        };

        RetryExecutor.execute(action, 3, 1, "TestModule", "TestOperation", logger);

        assertEquals(3, callCount[0]);
    }

    @Test
    void shouldRetryAndSucceedAfterFailures() {
        int[] succeedCount = {0};
        int[] stopCount = {0};
        Runnable action = () -> {
            if (stopCount[0] == 0 || stopCount[0] == 1) {
                stopCount[0]++;
                throw new RuntimeException();

            }else {
                succeedCount[0]++;
            }

        };

        RetryExecutor.execute(action, 3, 1, "TestModule", "TestOperation", logger);

        assertEquals(2, stopCount[0]);
        assertEquals(1, succeedCount[0]);
    }


    @Test
    void shouldLogPermanentFailure() throws Exception {
        Runnable action = () -> { throw new RuntimeException(); };

        RetryExecutor.execute(action, 2, 1, "Mod", "Op", logger);

        String content = Files.readString(tempDir.resolve("test.log"));
        assertTrue(content.contains("permanently failed after retries"));
    }


}

