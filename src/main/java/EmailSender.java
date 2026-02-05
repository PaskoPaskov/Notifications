import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender implements Notifiable {

    private final String fromEmail;
    private final String appPassword;
    private final Session session;
    private final String toEmail;
    private final TypesNotifire typesNotifire;
    private final LogErrors logErrors;

    public EmailSender(String fromEmail, String appPassword, String toEmail, TypesNotifire typesNotifire, LogErrors logErrors) {
        this.fromEmail = fromEmail;
        this.appPassword = appPassword;
        this.session = createSession();
        this.toEmail = toEmail;
        this.typesNotifire = typesNotifire;
        this.logErrors = logErrors;
    }

    // Create and configure a mail session using Gmail SMTP
    private Session createSession() {
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });
    }

    // Send a single email message
    private void sendEmail(String to, String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent!");
        } catch (MessagingException e) {
            System.err.println("[EMAIL ERROR] неуспешно изпращане: " + e.getMessage());
            logErrors.log("EmailSender", "ERROR", "Failed to send email", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendNotification(String title, String message) {

        Runnable sendAction = () -> sendEmail(toEmail, title, message);

        // Send immediately or retry based on notification type
        if (typesNotifire == TypesNotifire.NO_RETRY) {
            sendAction.run();
            return;
        }

        // Retry sending with exponential backoff
        RetryExecutor.execute(
                sendAction,
                5,
                2_000,
                "EmailSender",
                "Send email",
                logErrors
        );
    }

    // Test email configuration by sending a test message
    public void testConnection() throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
        );
        message.setSubject("MobileBgScanner – test email");
        message.setText(
                "This is a test email.\n\n" +
                        "Your configuration is valid and email notifications will work."
        );

        Transport.send(message);
    }


}
