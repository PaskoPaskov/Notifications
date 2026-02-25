package bg.paskov.scanner.service;

import bg.paskov.scanner.model.Advertisement;
import bg.paskov.scanner.notification.Notifiable;
import bg.paskov.scanner.util.LogErrors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class MobileBgScanner implements SiteScanner {

    private final Set<Advertisement> oldAdvertisements = new LinkedHashSet<>();
    private final Set<Advertisement> newAdvertisements = new LinkedHashSet<>();
    private final String url;
    private final Notifiable notifier;
    private boolean hasNewAdvertisements;
    private final LogErrors logErrors;
    private final Path oldAdvertisementsPath;


    public MobileBgScanner(String url, Notifiable notifier, LogErrors logErrors, Path oldAdvertisementsPath) {
        this.url = url;
        this.notifier = notifier;
        this.logErrors = logErrors;
        this.oldAdvertisementsPath = oldAdvertisementsPath;
    }

    // Download HTML from the target URL and select relevant advertisement elements
    private Elements downloadHTML() {
        Elements elements;

        try {
            // Using default Jsoup timeout (30s), sufficient for typical scan intervals
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0") // simulate a browser
                    .get();

            // Select all divs with class "item" excluding "fakti" class
            elements = doc.select("div.item:not(.fakti)");
            return elements;
        } catch (IOException e) {
            logErrors.log("bg.paskov.scanner.service.MobileBgScanner", "Error", "Problem downloading HTML", e);
            System.err.println("[ERROR] Проблем при сваляне на HTML: " + e.getMessage());
            return new Elements();
        }
    }

    // Extract advertisements from HTML and populate the newAdvertisements set
    private void addAdvertisements() {
        Elements htmlElements = downloadHTML();

        for (Element ad : htmlElements) {
            Element linkElement = ad.selectFirst(".zaglavie a"); // first link inside ad title
            if (linkElement != null) {
                String title = linkElement.text();
                String link = linkElement.absUrl("href");
                // Extract numeric ID from the link
                String id = link.replaceAll(".*obiava-(\\d+).*", "$1");

                Advertisement advertisement = new Advertisement(title, link, id);

                newAdvertisements.add(advertisement);
            }
        }
    }

    // Load previously scanned advertisements from the CSV file
    private void loadOldAdvertisements() {
        if (!Files.exists(oldAdvertisementsPath)) {
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(oldAdvertisementsPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);

                if (parts.length != 3) {
                    logErrors.log(
                            "bg.paskov.scanner.service.MobileBgScanner",
                            "WARN",
                            "Skipping malformed line in old advertisements file: " + line,
                            null
                    );
                    continue;
                }

                oldAdvertisements.add(
                        new Advertisement(
                                parts[1], // title
                                parts[2], // link
                                parts[0]  // id
                        )
                );
            }
        } catch (IOException e) {
            logErrors.log(
                    "bg.paskov.scanner.service.MobileBgScanner",
                    "ERROR",
                    "Failed to read old advertisements file",
                    e
            );
        }
    }

    // Compare new advertisements with the old ones and notify about new ads
    private void compareOldAndNewAdvertisements() {
        StringBuilder message = new StringBuilder();
        for (Advertisement advertisement : newAdvertisements) {
            if (!oldAdvertisements.contains(advertisement)) {
                // New advertisement detected
                message.append(advertisement.getTitle())
                        .append("\n")
                        .append(advertisement.getLink())
                        .append("\n\n");

                hasNewAdvertisements = true;
            }
        }

        if (message.length() > 0) {
            notifier.sendNotification("New advertisement found", message.toString());
        }
    }

    // Save the current set of advertisements as the new "old" file
    private void saveNewAdvertisements() {
        try (BufferedWriter writer = Files.newBufferedWriter(oldAdvertisementsPath)) {
            for (Advertisement advertisement : newAdvertisements) {
                writer.write(
                        advertisement.getId() + "," +
                                advertisement.getTitle() + "," +
                                advertisement.getLink()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            logErrors.log(
                    "bg.paskov.scanner.service.MobileBgScanner",
                    "ERROR",
                    "Failed to save advertisements",
                    e
            );
        }
    }

    @Override
    public void scan() {
        newAdvertisements.clear();
        oldAdvertisements.clear();
        hasNewAdvertisements = false;

        addAdvertisements();
        loadOldAdvertisements(); // Read previously stored ads
        compareOldAndNewAdvertisements(); // Compare old vs new ads by ID
        saveNewAdvertisements();  // Persist new ads

        printNewAdvertisements();
    }

    @Override
    public void printNewAdvertisements() {
        if (hasNewAdvertisements) {
            for (Advertisement advertisements : newAdvertisements) {
                System.out.println("New advertisement: " + advertisements.getId() + ", " + advertisements.getTitle() + ", " + advertisements.getLink());
            }
        } else {
            System.out.println("No new advertisements");
        }
    }
}
