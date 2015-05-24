package example.my.servlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

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
        String db = "enwiki_p";
        String dbUser = "u12033";
        String dbPasswd = "eenuoshohtoughuk";
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url + db, dbUser, dbPasswd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void go() {
        String user = "kanzat";
        String password = "futurama";
        String host = "tools-login.wmflabs.org";
        int port = 22;
        try {
            JSch jsch = new JSch();
            jsch.addIdentity("C:\\Users\\Andrii\\privatekey.ppk", "futurama");
            Session session = jsch.getSession(user, host, port);
            lport = 4711;
            rhost = "enwiki.labsdb";
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
    
    static String linksHereQuery = 
            "SELECT inn.page_id, inn.page_title, langlinks.ll_lang "
            + "FROM ("
                + "SELECT * "
                + "FROM templatelinks INNER JOIN page ON templatelinks.tl_from=page.page_id "
                + "WHERE templatelinks.tl_namespace=10 AND templatelinks.tl_title= ? AND page.page_namespace=10 "
                + "LIMIT ? "
            + ") inn INNER JOIN langlinks ON inn.page_id=langlinks.ll_from;";

    public static Pages getLinksHereTemplatesWithLangLinks(String templateTitle, int limit) {
        Pages result = new Pages();
        try {
            PreparedStatement st = con.prepareStatement(linksHereQuery);
            int i=1;
            st.setString(i++, templateTitle);
            st.setInt(i++, limit);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                int id = set.getInt("inn.page_id");
                String title = set.getString("inn.page_title");
                String lang = set.getString("langlinks.ll_lang");
                result.addTitle(id, title);
                result.addLang(id, lang);
            }
        } catch (SQLException s) {
            System.err.println(s);
        }
        return result;
    }

    static String linksQuery1 = 
            "SELECT page.page_id, pagelinks.pl_title"
            + "FROM page INNER JOIN pagelinks ON page.page_id=pagelinks.pl_from"
            + "WHERE page.page_id IN (";
    static String linksQuery2 = ");";
    
    public static Pages getLinksForPages(Pages filteredTemplates) {
        String pageIds = StringUtils.join(filteredTemplates.getPageIds(), ",");
        String linksQuery = linksQuery1 + pageIds + linksQuery2;
        try {
            PreparedStatement st = con.prepareStatement(linksQuery);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                int id = set.getInt("page.page_id");
                String linkTitle = set.getString("pagelinks.pl_title");
                filteredTemplates.addLink(id, linkTitle);
            }
        } catch (SQLException s) {
            System.err.println(s);
        }
        return filteredTemplates;
    }
    
}