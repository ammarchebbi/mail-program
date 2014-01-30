package mailprogram;


import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store; 
import javax.mail.Transport;
import javax.mail.UIDFolder.FetchProfileItem;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

public class Mail {
    public static HashMap<String, Session> mail_incoming = new HashMap<>();
    public static HashMap<String, Session> mail_outgoing = new HashMap<>();
    public static HashMap<String, Store> inbox = new HashMap<>();
    public static HashMap<String, Transport> outbox = new HashMap<>();

    public static String active_username = new String();
    public static Session active_incoming, active_outgoing;
    public static Folder active_inbox, active_folder;
    public static Transport active_outbox;
    public static Message[] active_folder_messages;

    public static boolean userExists(String username) {
        return mail_incoming.containsKey(username);
    }
    
    // Set the active user session.
    public static void setActiveUser(String username) {
        if (active_username.equals(username)) return;
        active_username = username;
        try {
            active_incoming = mail_incoming.get(active_username);
            active_outgoing = mail_outgoing.get(active_username);
            active_inbox = inbox.get(active_username).getDefaultFolder();
            active_outbox = outbox.get(active_username);
            active_folder = null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Set Active User Exception:\n" + e);
        }
    }

    public static void writeUsers() {
        Properties users = new Properties();
        for (Map.Entry<String, Session> entry : mail_incoming.entrySet()) {
            
        }
    }
            
    
    // Open an incoming mail session and store it within the incoming session map.
    public static boolean openMailIncoming(String protocol, String hostname, String username, String password) {
        if (mail_incoming.containsKey(username)) return true;
        Properties props = new Properties();
        props.put("mail.store.protocol", protocol);
        props.put("mail." + protocol + ".timeout", "5000");
        if (protocol.equals("pop3s")) props.put("mail.pop3.disablecapa", "true");
        Session session;
        Store store;
        try {
            session = Session.getInstance(props, null);
            store = session.getStore();
            store.connect(hostname, username, password);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Incoming Exception: " + hostname + " " + protocol + "\n" + e);
            return false;
        }
        mail_incoming.put(username, session);
        inbox.put(username, store);
        return true;
    }
	
    // Open an outgoing mail session and store it within the outgoing session map.
    public static boolean openMailOutgoing(String protocol, String hostname, String username, String password) {
        if (mail_outgoing.containsKey(username)) return true;
        Properties props = new Properties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.from", username);
        props.put("mail.smtp.starttls.enable", "true");
        Session session;
        Transport transport;
        try {
            session = Session.getInstance(props, null);
            transport = session.getTransport();
            transport.connect(hostname, username, password);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Outgoing Exception: " + hostname + "\n" + e);
            return false;
        }
        mail_outgoing.put(username, session);
        outbox.put(username, transport);
        return true;
    }
    
    public static void closeConnections() {
        try {
            for (Store i : inbox.values()) {
                if (!i.isConnected()) continue;
                i.close();
            }
            for (Transport i : outbox.values()) {
                if (!i.isConnected()) continue;
                i.close();
            }
        } catch (Exception e) {
            System.out.println("Failed to close connections.");
        }
    }
	
    // Open a folder from the active inbox, close the folder already open.
    /*public static boolean openFolder(String folder_name) {
        try {
            Folder folder = active_inbox.getFolder(folder_name);
            try {
                folder.open(Folder.READ_WRITE);
            } catch (Exception e) {
                folder.open(Folder.READ_ONLY);
            }
            if (active_folder != null) active_folder.close(false);
            active_folder = folder;			
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Open Folder Exception:\n" + e);
            return false;
        }
        return true;
    }*/
    public static boolean holdsMessages(Folder folder) {
        try {
            return (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
        } catch (Exception e) {};
        return false;
    }
    public static void openDefaultFolder() {
        try {
            if (active_folder != null) {
                if (holdsMessages(active_folder)) active_folder.close(false);
            }
        } catch (Exception e) {
            
        }
        active_folder = null;
    }
    public static boolean openFolder(String folder_name) {
        String string = "";
        try {
            Folder folder = (active_folder == null ? active_inbox : active_folder).getFolder(folder_name);
            string = folder.getName();
            if (holdsMessages(folder)) {
                try {
                    folder.open(Folder.READ_WRITE);
                } catch (Exception e) {
                    folder.open(Folder.READ_ONLY);
                }
            }
            if (active_folder != null) {
                if (holdsMessages(active_folder)) active_folder.close(false);
            }
            active_folder = folder;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Open Folder Exception " + string + ":\n" + e);
            return false;
        }
        return true;
    }
    public static boolean openDirectory(String directory) {
        String[] folders = directory.split("/");
        for (int i = folders.length - 1; i >= 0; --i) {
            if (!openFolder(folders[i])) return false;
        }
        return true;
    }
    public static Message[] getMessages(String folderName) {
        openDefaultFolder();
        openDirectory(folderName);
        try {
            if (!active_folder.isOpen()) return null; // If this folder doesn't hold messages, return null.
            Message[] messages = active_folder.getMessages();//inbox.getMessages(start + 1, total);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfileItem.FLAGS);
            fp.add(FetchProfileItem.CONTENT_INFO);
            fp.add("X-mailer");

            active_folder.fetch(messages, fp); // Load the profile of the messages in 1 fetch.
            return active_folder_messages = messages;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error getting messages!\n" + e);
            return null;
        }
    }
    public static boolean replyTo(Message msg, String body) {
        try {
            Message replyMsg = msg.reply(true);
            replyMsg.setContent(body, "text/html");
            active_outbox.sendMessage(replyMsg, replyMsg.getAllRecipients());
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error replying to message!\n" + e);
        }
        return false;
    }
    public static boolean sendTo(InternetAddress[] recipients, String subject, String body) {
        try {
            Message msg = new MimeMessage(active_outgoing);
            msg.setSubject(subject);
            msg.setContent(body, "text/html");
            if (!active_outbox.isConnected()) active_outbox.connect();
            active_outbox.sendMessage(msg, recipients);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error sending message!\n" + e);
        }
        return false;
    }
    public static String getMsgText(Message message) {
        try {
            String content_type = message.getContentType().toLowerCase();
            //message.writeTo(System.out);
            if (content_type.contains("plain") || content_type.contains("html")) {
                return message.getContent().toString();
            }
            else if (content_type.contains("multipart")) {
                Multipart parts = (Multipart)message.getContent();
                String data = "";
                for (int i = 0; i < parts.getCount(); ++i) {
                  BodyPart p = parts.getBodyPart(i);
                  content_type = p.getContentType().toLowerCase();
                  if (content_type.contains("html")) {
                      return p.getContent().toString();
                  }
                  else if (content_type.contains("plain")) {
                      data = p.getContent().toString();
                  }
                  else {
                      System.out.println(content_type);
                  }
                }
                return data;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Get Message Text Exception:\n" + e);
        }
        return "";
    }
    // Send mail to multiple addresses.
    public static void sendMail(Address[] addresses, String subject, String text) {
        try {
                Message message = new MimeMessage(active_outgoing);
                message.setFrom();
                message.setSentDate(new Date());
                message.setSubject(subject);
                message.setText(text);
                active_outbox.sendMessage(message, addresses);
        } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Sending Exception: " + e);
        }
    }
	
    // Send mail to the n address.
    public static void sendMail(String address, String subject, String text) {
        Address[] addresses = new InternetAddress[1];
        try {
                addresses[0] = new InternetAddress(address);
        } catch (Exception e) {

        }
        sendMail(addresses, subject, text);
    }
	
    // Delete mail
    public static void deleteMail(int index) {

    }
}
