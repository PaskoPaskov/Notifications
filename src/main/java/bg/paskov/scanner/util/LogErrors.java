package bg.paskov.scanner.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogErrors {

    private static final long MAX_LOG_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_ROTATED_FILES = 3;
    private final Path logFilePath;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogErrors(Path logFilePath) {
        this.logFilePath = logFilePath;
    }

    // Write a log entry with optional exception details
    public synchronized void log(
            String module,
            String level,
            String message,
            Exception e
    ) {
        try {
            rotateIfNeeded();

            try (BufferedWriter writer = Files.newBufferedWriter(
                    logFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            )) {
                writeLine(writer, "[" + level + "] [" + module + "] " + message);

                if (e != null) {
                    writeLine(writer, "Exception: " + e.getClass().getName());
                    writeLine(writer, "Details: " + e.getMessage());
                }

                writeLine(writer, "-----------------------------------------");
            }

        } catch (IOException ignored) {
            // Last line of defense: logging must never crash the application
        }
    }

    // Rotate log files when maximum size is exceeded
    private void rotateIfNeeded() throws IOException {
        if (!Files.exists(logFilePath)) {
            return;
        }

        if (Files.size(logFilePath) < MAX_LOG_FILE_SIZE) {
            return;
        }

        for (int i = MAX_ROTATED_FILES; i >= 1; i--) {
            Path source = logFilePath.resolveSibling("error.log." + i);
            Path target = logFilePath.resolveSibling("error.log." + (i + 1));

            if (Files.exists(source)) {
                if (i == MAX_ROTATED_FILES) {
                    Files.delete(source);
                } else {
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        Files.move(
                logFilePath,
                logFilePath.resolveSibling("error.log.1"),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    // Write a single formatted log line
    private void writeLine(BufferedWriter writer, String text) throws IOException {
        writer.write(LocalDateTime.now().format(formatter));
        writer.write(" ");
        writer.write(text);
        writer.newLine();
    }
}

