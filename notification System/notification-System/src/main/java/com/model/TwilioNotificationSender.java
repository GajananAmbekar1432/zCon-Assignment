package com.model;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

public class TwilioNotificationSender {
    public static final String ACCOUNT_SID = "AC372e3f99eac248781ca09d8b8447e09a";
    public static final String AUTH_TOKEN = "9dcc2355f7b4e7d77889f38994124f43";
    public static final String TWILIO_PHONE_NUMBER = "whatsapp:+14155238886";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendNotification(String recipientPhoneNumber, String messageContent) {
        try {
            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(recipientPhoneNumber),
                    new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
                    messageContent
            ).create();

            System.out.println("Notification sent: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}

