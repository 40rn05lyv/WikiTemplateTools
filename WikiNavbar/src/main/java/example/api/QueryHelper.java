package example.api;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class QueryHelper {
    static int lport;
    static String rhost;
    static int rport;

    static Connection con = null;
    static {
        try {
            go();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("An example for updating a Row from Mysql Database!");
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:" + lport + "/";
        String db = "ukwiki_p";
        String dbUser = "u12033";
        String dbPasswd = "eenuoshohtoughuk";
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url + db + "?useUnicode=true&characterEncoding=UTF-8", dbUser, dbPasswd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Session session;

    public static void go() {
        String user = "kanzat";
        String password = "futurama";
        String host = "tools-login.wmflabs.org";
        int port = 22;
        try {
            JSch jsch = new JSch();
            jsch.addIdentity("C:\\Users\\Andrii\\privatekey.ppk", "futurama");
            session = jsch.getSession(user, host, port);
            lport = 4711;
            rhost = "ukwiki.labsdb";
            rport = 3306;
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Establishing Connection...");
            session.connect();
            int assinged_port = session.setPortForwardingL(lport, rhost, rport);
            System.out.println("localhost:" + assinged_port + " -> " + rhost + ":" + rport);
        } catch (Exception e) {
            System.err.print(e);
        }
    }

    static String linksHereWithoutInterwikiQuery = "SELECT page.page_id, page.page_title " + "FROM ( "
            + "SELECT page.page_id, page.page_title " + "FROM templatelinks INNER JOIN page ON templatelinks.tl_from=page.page_id "
            + "WHERE templatelinks.tl_namespace=10 AND templatelinks.tl_title=? AND page.page_namespace=10 "
            + ") page LEFT JOIN langlinks ON page.page_id=langlinks.ll_from " + "WHERE langlinks.ll_from IS NULL;";

    public static List<String> getLinksHereTemplatesWithLangLinks(String templateTitle) throws UnsupportedEncodingException {
        List<String> list = new ArrayList<String>();
        try {
            PreparedStatement st = con.prepareStatement(linksHereWithoutInterwikiQuery);
            int i = 1;
            st.setString(i++, templateTitle);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String title = new String(set.getBytes("page.page_title"), "UTF-8");
                list.add(title);
                System.out.println(title);
            }
        } catch (SQLException s) {
            System.err.println(s);
        } finally {
            session.disconnect();
        }
        return list;
    }

    public static void stop() {
        session.disconnect();
    }

}