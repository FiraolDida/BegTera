package com.quebix.bunachat.Model;

public class MatchNotification {
    String to, message, from;

    public MatchNotification() {
    }

    public MatchNotification(String to, String from) {
        this.to = to;
        this.from = from;
    }

    public MatchNotification(String to, String message, String from) {
        this.to = to;
        this.message = message;
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
