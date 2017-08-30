package logic;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Kazbah
 */
public class EmailSender {
    public static boolean sendMail(String host, String user, String from, String password, String subject, String message, String to[]){
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.user",user);
        props.put("mail.password", password);
        //props.put("mail.smtp.port",587);
        props.put("mail.smtp.auth","true");
        Session session = Session.getDefaultInstance(props);
        MimeMessage mimeMessage = new MimeMessage(session);
        try{
            mimeMessage.setFrom(new InternetAddress(from));

            for(int i=0; i<to.length; i++)
            {
            	mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
            }
          
            
            // add Subject
            mimeMessage.setSubject(subject);
            // set message to mimeMessage
            mimeMessage.setText(message);
            Transport transport = session.getTransport("smtp");
            transport.connect(host,user,password);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            transport.close();
            return true;
        }catch(MessagingException me){
            me.printStackTrace();
           
        }
        return false;
    }
}