package nz.ac.canterbury.seng302.homehelper.exceptions.renovation;

import java.util.ArrayList;

public class InvitationException extends IllegalArgumentException {

    private ArrayList<String> emails;
    private ArrayList<String> messages;

    public InvitationException() {
        this.emails = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<String> emails) {
        this.emails = emails;
    }

    public void addEmail(String email) {
        this.emails.add(email);
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

}
