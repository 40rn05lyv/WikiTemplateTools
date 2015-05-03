package example.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectionFactory {

    private static Session session;
    private static String dbUser = "u12033";
    private static String dbPasswd = "eenuoshohtoughuk";
    private static int LPORT_START = 4711;

    private static Map<String, Connection> connectionMap = new HashMap<String, Connection>();

    public static Connection getConnection(String lang) {
        lang = lang.replaceAll("-", "_");
        Connection con = connectionMap.get(lang);
        try {
            if (con != null && !con.isClosed()) {
                return con;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        try {
            String rhost = lang + "wiki.labsdb";
            int lport = getFreeLPort();
            int rport = 3306;
            int assinged_port = session.setPortForwardingL(lport, rhost, rport);
            System.out.println("localhost:" + assinged_port + " -> " + rhost + ":" + rport);
            String url = "jdbc:mysql://127.0.0.1:" + lport + "/";
            String db = lang + "wiki_p";
            con = DriverManager.getConnection(url + db + "?useUnicode=true&characterEncoding=UTF-8", dbUser, dbPasswd);
            connectionMap.put(lang, con);
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
    
    private static synchronized int getFreeLPort() {
        return LPORT_START++;
    }

    public static void init() throws ClassNotFoundException {
        String user = "kanzat";
        String password = "futurama";
        String host = "tools-dev.wmflabs.org";
        int port = 22;
        try {
            JSch jsch = new JSch();
            jsch.addIdentity("C:\\Users\\Andrii\\privatekey.ppk", "futurama");
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Establishing Connection...");
            session.connect();
        } catch (Exception e) {
            System.err.print(e);
        }
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
    }

    public static void stop() {
        System.out.println("Stopping connections...");
        for (Connection con: connectionMap.values()) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Disconnecting ssh session...");
        session.disconnect();
    }

}
