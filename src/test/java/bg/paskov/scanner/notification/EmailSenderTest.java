package bg.paskov.scanner.notification;

import bg.paskov.scanner.util.LogErrors;
import bg.paskov.scanner.util.RetryExecutor;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailSenderTest {
    private EmailSender emailSender;

    @Mock
    private LogErrors logErrors;

    private Session session;

    @BeforeEach
    void setUp() {
        session = Session.getInstance(new Properties());
    }

    private EmailSender createSpyEmailSender(TypesNotifire type) {
        return spy(new EmailSender(
                "from@example.com", "secret", "to@example.com",
                type, logErrors, session
        ));
    }

    @Test
    void sendNotificationNoRetryShouldCallDoSendOnce() throws MessagingException {
        emailSender = createSpyEmailSender(TypesNotifire.NO_RETRY);

        doNothing().when(emailSender).submitToSmtp(any(Message.class));

        emailSender.sendNotification("Subject", "Body");

        verify(emailSender).submitToSmtp(any(Message.class));
    }

    @Test
    void sendNotificationNoRetryShouldThrowWhenDoSendFails() throws MessagingException {
        emailSender = createSpyEmailSender(TypesNotifire.NO_RETRY);

        doThrow(new MessagingException("SMTP error")).when(emailSender).submitToSmtp(any(Message.class));

        assertThrows(RuntimeException.class, () -> emailSender.sendNotification("Subject", "Body"));
    }

    @Test
    void sendNotificationNoRetryShouldLogErrorWhenDoSendFails() throws MessagingException {
        emailSender = createSpyEmailSender(TypesNotifire.NO_RETRY);

        doThrow(new MessagingException("log error")).when(emailSender).submitToSmtp(any(Message.class));
        try {
            emailSender.sendNotification("Subject", "Body");
        } catch (RuntimeException ignored) {
        }


        verify(logErrors).log(anyString(), anyString(), anyString(), any(MessagingException.class));
    }

    @Test
    void sendNotificationWithRetryShouldDelegateToExecutor() {
        emailSender = createSpyEmailSender(TypesNotifire.WITH_RETRY);

        try (var mockedExecutor = mockStatic(RetryExecutor.class)) {

            emailSender.sendNotification("Subject", "Body");

            mockedExecutor.verify(() -> RetryExecutor.execute(any(Runnable.class), eq(5), eq(2_000L), eq("bg.paskov.scanner.notification.EmailSender"), eq("Send email"), eq(logErrors)), times(1));
        }
    }

    @Test
    void sendNotificationWithRetryShouldSucceedOnSecondAttempt() throws MessagingException {
        emailSender = createSpyEmailSender(TypesNotifire.WITH_RETRY);
        // First attempt fails, second attempt succeeds
        doThrow(new MessagingException("SMTP error"))
                .doNothing()
                .when(emailSender).submitToSmtp(any(Message.class));

        emailSender.sendNotification("Subject", "Body");

        // Should be called exactly twice — once unsuccessfully and once successfully
        verify(emailSender, times(2)).submitToSmtp(any(Message.class));
    }

    @Test
    void testConnectionShouldCallDoSendOnce() throws MessagingException {
        emailSender = createSpyEmailSender(TypesNotifire.WITH_RETRY);
        doNothing().when(emailSender).submitToSmtp(any(Message.class));

        emailSender.testConnection();

        verify(emailSender, times(1)).submitToSmtp(any(Message.class));
    }


    @Test
    void testConnectionShouldPropagateExceptionWhenDoSendFails() throws MessagingException {
        emailSender = createSpyEmailSender(TypesNotifire.WITH_RETRY);
        doThrow(new MessagingException("SMTP error"))
                .when(emailSender).submitToSmtp(any(Message.class));

        assertThrows(MessagingException.class, () -> emailSender.testConnection());
    }
}
