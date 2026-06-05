package bg.paskov.scanner.service;

import bg.paskov.scanner.model.Advertisement;
import bg.paskov.scanner.notification.Notifiable;
import bg.paskov.scanner.util.LogErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MobileBgScannerTest {
    @TempDir
    Path tempDir;

    @Mock
    Notifiable notifiable;

    @Mock
    LogErrors logErrors;

    private MobileBgScanner mobileBgScanner;
    private Path tempOldAdvertisementsPath;

    @BeforeEach
    void setUp() {
        tempOldAdvertisementsPath = tempDir.resolve("old_advertisements.csv");
        mobileBgScanner = new MobileBgScanner("https://example.com", notifiable, logErrors, tempOldAdvertisementsPath);
    }

    private void invokeLoadOldAdvertisements(MobileBgScanner scanner) throws Exception {
        Method method = MobileBgScanner.class.getDeclaredMethod("loadOldAdvertisements");
        method.setAccessible(true);
        method.invoke(scanner);
    }

    private void invokeCompareOldAndNewAdvertisements(MobileBgScanner scanner) throws Exception {
        Method method = MobileBgScanner.class.getDeclaredMethod("compareOldAndNewAdvertisements");
        method.setAccessible(true);
        method.invoke(scanner);
    }

    private void invokeSaveNewAdvertisements(MobileBgScanner scanner) throws Exception {
        Method method = MobileBgScanner.class.getDeclaredMethod("saveNewAdvertisements");
        method.setAccessible(true);
        method.invoke(scanner);
    }

    @SuppressWarnings("unchecked")
    private Set<Advertisement> getSetField(String fieldName) throws Exception {
        Field field = MobileBgScanner.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Set<Advertisement>) field.get(mobileBgScanner);
    }

    @Test
    void loadOldAdvertisementsShouldDoNothingWhenFileIsMissing() throws Exception {
        invokeLoadOldAdvertisements(mobileBgScanner);

        verifyNoInteractions(logErrors);
        assertTrue(getSetField("oldAdvertisements").isEmpty());
    }

    @Test
    void loadOldAdvertisementsShouldLoadAllFromFile() throws Exception {
        Files.writeString(tempOldAdvertisementsPath, "111,https://example.com/obiava-111,Opel Astra");

        invokeLoadOldAdvertisements(mobileBgScanner);

        assertEquals(1, getSetField("oldAdvertisements").size());
    }

    @Test
    void loadOldAdvertisementsShouldLoadAllLinesFromFile() throws Exception {
        Files.writeString(tempOldAdvertisementsPath, "111,https://example.com/obiava-111,Opel Astra\n" +
                "112,https://example.com/obiava-112,Opel Kadet");

        invokeLoadOldAdvertisements(mobileBgScanner);

        assertEquals(2, getSetField("oldAdvertisements").size());
    }

    @Test
    void loadOldAdvertisementsShouldSkipMalformedLines() throws Exception {
        Files.writeString(tempOldAdvertisementsPath, "111 Opel Astra,https://example.com/obiava-111");

        invokeLoadOldAdvertisements(mobileBgScanner);

        assertTrue(getSetField("oldAdvertisements").isEmpty());
        verify(logErrors).log(anyString(), eq("WARN"), anyString(), isNull());
    }

    @Test
    void saveNewAdvertisementsShouldCreateFileWithCorrectContent() throws Exception {
        getSetField("newAdvertisements").add(new Advertisement("Opel Astra", "https://example.com/obiava-111", "111"));
        getSetField("newAdvertisements").add(new Advertisement("BMW 320", "https://example.com/obiava-222", "222"));

        invokeSaveNewAdvertisements(mobileBgScanner);

        List<String> actualLines = Files.readAllLines(tempOldAdvertisementsPath);

        List<String> expectedLines = List.of(
                "111,https://example.com/obiava-111,Opel Astra",
                "222,https://example.com/obiava-222,BMW 320"
        );

        assertEquals(expectedLines, actualLines);
    }

    @Test
    void saveNewAdvertisementsShouldCreateEmptyFileWhenNoAds() throws Exception {

        invokeSaveNewAdvertisements(mobileBgScanner);

        assertTrue(Files.exists(tempOldAdvertisementsPath));
        assertEquals(0, Files.size(tempOldAdvertisementsPath));
    }

    @Test
    void compareShouldNotNotifyWhenNoNewAds() throws Exception {
        getSetField("newAdvertisements").add(new Advertisement("Opel Astra", "https://example.com/obiava-111", "111"));
        getSetField("oldAdvertisements").add(new Advertisement("Opel Astra", "https://example.com/obiava-111", "111"));

        invokeCompareOldAndNewAdvertisements(mobileBgScanner);

        verify(notifiable, never()).sendNotification(anyString(), anyString());
    }

    @Test
    void compareShouldNotifyWhenNewAdIsFound() throws Exception {
        getSetField("newAdvertisements").add(new Advertisement("Opel Astra", "https://example.com/obiava-111", "111"));

        invokeCompareOldAndNewAdvertisements(mobileBgScanner);

        verify(notifiable).sendNotification(anyString(), anyString());

    }

}
