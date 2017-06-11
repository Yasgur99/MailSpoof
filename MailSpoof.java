package mailspoof;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

/**
 * @author yasgur99
 */

public class MailSpoof {

    public static void main(String[] args) {
        System.out.println("Welcome to MailSpoof\nEmails should be in the form: joshmo@domain.com\n");

        System.out.print("Enter the email you want to sent from: ");
        Scanner sc = new Scanner(System.in);
        String mailFrom = sc.nextLine();
        System.out.print("\nEnter the email you want to send to: ");
        String rcptTo = sc.nextLine();
        System.out.print("\nEnter the subject (Leave empty for no subject): ");
        String subject = sc.nextLine();
        System.out.println("\nEnter the body of your message and finish with a \'.\' on its own line to finish writing data:\n");
        StringBuilder sb = new StringBuilder();
        String line;
        do {
            line = sc.nextLine().trim();
            sb.append(line + "\r\n");
        } while (!line.equals("."));

        String data = "Date: " + new Date().toString() + "\r\nTo: " + rcptTo + "\r\nFrom: " + mailFrom + "\r\nSubject: " + subject + sb.toString();

        try {
            List<String> mxRecords = lookupMailHost(rcptTo.split("@")[1]);
            if (mxRecords == null)
                return;
            for (String server : mxRecords) {
                if (send(server, mailFrom, rcptTo, data))
                    break;
            }
        } catch (NamingException ex) {

        }
    }

    public static List<String> lookupMailHost(String domain) throws NamingException {
        Attributes attributes = new InitialDirContext().getAttributes("dns:/" + domain, new String[]{"MX"});
        Attribute attributeMX = attributes.get("MX");
        if (attributeMX == null) {
            return null;
        }

        String[][] pvhn = new String[attributeMX.size()][2];
        for (int i = 0; i < attributeMX.size(); i ++)
            pvhn[i] = ("" + attributeMX.get(i)).split("\\s+");

        Arrays.sort(pvhn, (String[] o1, String[] o2) -> (Integer.parseInt(o1[0]) - Integer.parseInt(o2[0])));

        List<String> sortedHostNames = new ArrayList<>();
        for (int i = 0; i < pvhn.length; i ++)
            if (pvhn[i][1].endsWith("."))
                sortedHostNames.add(pvhn[i][1].substring(0, pvhn[i][1].length() - 1));
            else
                sortedHostNames.add(pvhn[i][1]);

        return sortedHostNames;
    }

    public static boolean send(String mailServer, String mailFrom, String rcptTo, String data) {
        try {
            Socket socket = new Socket(mailServer, 25);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(
                            socket.getOutputStream()));
            System.out.println(reader.read());
            writer.write("HELO " + mailFrom.split("@")[1] + "\r\n");
            writer.flush();
            System.out.println(reader.read());
            writer.write("MAIL FROM: <" + mailFrom + ">\r\n");
            writer.flush();
            System.out.println(reader.read());
            writer.write("RCPT TO: <" + rcptTo + ">\r\n");
            writer.flush();
            System.out.println(reader.read());
            writer.write("DATA\r\n");
            writer.flush();
            System.out.println(reader.read());
            writer.write(data + "\r\n");
            writer.flush();
            System.out.println(reader.read());
            writer.write("QUIT\r\n");
            writer.flush();
            System.out.println(reader.read());
            return true;
        } catch (IOException ex) {
        }
        System.out.println("[!!]Error sending mail[!!]");
        return false;
    }
}

