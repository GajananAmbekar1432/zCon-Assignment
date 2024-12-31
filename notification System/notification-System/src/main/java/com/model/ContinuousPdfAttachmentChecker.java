package com.model;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;



public class ContinuousPdfAttachmentChecker {

   
    public static void main(String[] args) {

        
        String host = "imap.gmail.com";
        String username = "gajananambekarwork@gmail.com";
        String password = "kvmnocihhgbxqftq";
        int pollingInterval = 15000; // 15 seconds

        String recipientPhoneNumber = "whatsapp:+919075094966"; // Replace with the recipient's phone number

        // Set to store processed message IDs
        Set<String> processedMessageIds = new HashSet<>();

        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", host);
        properties.put("mail.imaps.port", "993");

        try {
            Session session = Session.getDefaultInstance(properties, null);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            while (true) {
                try {
                    Folder inbox = store.getFolder("INBOX");
                    inbox.open(Folder.READ_ONLY);

                    Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                    System.out.println("Unread messages: " + messages.length);

                    for (Message message : messages) {
                        String messageId = ((MimeMessage) message).getMessageID();

                        // Skip already processed messages
                        if (processedMessageIds.contains(messageId)) {
                            continue;
                        }

                        // Add the message ID to the processed set
                        processedMessageIds.add(messageId);

                        System.out.println("Subject: " + message.getSubject());

                        // Extract sender details
                        Address[] fromAddresses = message.getFrom();
                        String sender = (fromAddresses != null && fromAddresses.length > 0)
                                ? ((InternetAddress) fromAddresses[0]).getAddress()
                                : "Unknown Sender";

                        if (message.isMimeType("multipart/*")) {
                            Multipart multipart = (Multipart) message.getContent();

                            for (int i = 0; i < multipart.getCount(); i++) {
                                BodyPart bodyPart = multipart.getBodyPart(i);

                                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                                        bodyPart.getFileName() != null) {

                                    String fileName = bodyPart.getFileName();
                                    String contentType = bodyPart.getContentType();

                                    if ((fileName != null && fileName.toLowerCase().endsWith(".pdf")) ||
                                            (contentType != null && contentType.toLowerCase().contains("application/pdf"))) {
                                        System.out.println("Found a PDF attachment: " + fileName);

                                        // Create notification message
                                        String notificationMessage = String.format(
                                                "New email from %s with a PDF attachment: %s",
                                                sender, fileName
                                        );

                                        // Send notification using Twilio
                                        TwilioNotificationSender.sendNotification(recipientPhoneNumber, notificationMessage);
                                    }
                                }
                            }
                        }
                    }

                    inbox.close(false);
                    System.out.println("Waiting for the next check...");
                    Thread.sleep(pollingInterval);
                } catch (Exception e) {
                    System.err.println("Error during email check: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
