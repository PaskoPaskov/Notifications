package bg.paskov.scanner.ui;

import bg.paskov.scanner.config.ConfigManager;
import bg.paskov.scanner.notification.EmailSender;
import bg.paskov.scanner.notification.TypesNotifire;
import bg.paskov.scanner.util.LogErrors;

import javax.swing.*;

public class SetupWizard {
    LogErrors logErrors;
    private final ConfigManager configManager;

    public SetupWizard(ConfigManager configManager, LogErrors logErrors) {
        this.configManager = configManager;
        this.logErrors = logErrors;
    }

    // Run the setup wizard only if the configuration is incomplete
    public void runIfNeeded() {
        if (configManager.isValidConfiguration()) {
            return; // Configuration already completed
        }

        showDialog();
    }

    // Display the initial configuration dialog
    private void showDialog() {
        JTextField fromEmailField = new JTextField();
        JTextField toEmailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField urlField = new JTextField();
        JTextField intervalField = new JTextField();

        Object[] form = {
                "From email:", fromEmailField,
                "To email:", toEmailField,
                "Gmail app password:", passwordField,
                "Mobile.bg URL:", urlField,
                "Scan interval (days):", intervalField
        };

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    form,
                    "Initial Setup",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                // Exit application if user cancels the setup
                System.exit(0);
            }

            if (validateAndSave(
                    fromEmailField.getText(),
                    toEmailField.getText(),
                    new String(passwordField.getPassword()),
                    urlField.getText(),
                    intervalField.getText()
            )) {
                break;
            }
        }
    }

    // Validate user input and persist configuration if valid
    private boolean validateAndSave(
            String from,
            String to,
            String password,
            String url,
            String interval
    ) {
        // Ensure all required fields are provided
        if (from.isBlank() || to.isBlank() || password.isBlank() || url.isBlank()) {
            showError("All fields are required.");
            return false;
        }

        // Validate that scan interval is a positive number
        int seconds;
        try {
            seconds = Integer.parseInt(interval);
            if (seconds <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showError("Scan interval must be a positive number.");
            return false;
        }

        // Verify email configuration by sending a test email
        try {
            EmailSender tester = new EmailSender(
                    from,
                    password,
                    to,
                    TypesNotifire.NO_RETRY,
                    logErrors
            );

            tester.testConnection();

        } catch (Exception e) {
            showError(
                    "Failed to send test email.\n\n" +
                            "Please check:\n" +
                            "- Gmail App Password\n" +
                            "- Email addresses\n" +
                            "- Internet connection\n\n" +
                            "Details: " + e.getMessage()
            );
            return false;
        }

        // Persist configuration only after successful validation
        configManager.setProperties("email.from", from);
        configManager.setProperties("email.to", to);
        configManager.setProperties("email.password", password);
        configManager.setProperties("scan.url", url);
        configManager.setProperties("scan.interval.seconds", String.valueOf(seconds));
        configManager.setProperties("notification.retry", "true");

        configManager.save();
        return true;
    }

    // Show an error dialog with the given message
    private void showError(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Configuration Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
