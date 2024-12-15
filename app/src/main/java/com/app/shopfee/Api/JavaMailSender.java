package com.app.shopfee.Api;

import android.os.AsyncTask;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class JavaMailSender extends AsyncTask<Void, Void, Boolean> {

    private final String email;
    private final String subject;
    private final String message;

    public JavaMailSender(String email, String subject, String message) {
        this.email = email;
        this.subject = subject;
        this.message = message;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        final String username = "trillrooftop@gmail.com";
        final String password = "pivwlapdpjbkhcve";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            Transport.send(mimeMessage);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            System.out.println("Email sent successfully.");
        } else {
            System.out.println("Failed to send email.");
        }
    }
}
