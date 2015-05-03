package org.wikipedia.api.db;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wikipedia.templates.find.interwiki.db.TemplateInterwikiStorage;

public class QueryHelper {

    private static final int NAMESPACE_TEMPLATE = 10;

    // @formatter:off
    static String linksHereWithoutInterwikiQuery = 
            "SELECT page.page_id, page.page_title "
            + "FROM ( "
            + "SELECT page.page_id, page.page_title "
            + "FROM templatelinks INNER JOIN page ON templatelinks.tl_from=page.page_id "
            + "WHERE templatelinks.tl_namespace=10 AND templatelinks.tl_title=? AND page.page_namespace=10 "
            + ") page LEFT JOIN langlinks ON page.page_id=langlinks.ll_from "
            + "WHERE langlinks.ll_from IS NULL;";
    // @formatter:on

    public static List<String> getLinksHereTemplatesWithLangLinks(String lang, String templateTitle) throws UnsupportedEncodingException {
        List<String> list = new ArrayList<String>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(linksHereWithoutInterwikiQuery);
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
        }
        return list;
    }

    // @formatter:off
    static String doesTemplateExistQuery = 
            "SELECT 1 "
            + "FROM page "
            + "WHERE page_title=? AND page_namespace= " + NAMESPACE_TEMPLATE + ";";
    // @formatter:on

    // TODO: can be very slow!
    public static boolean doesTemplateExist(String lang, String pageTitle) {
        long start = System.currentTimeMillis();
        System.out.println("doesTemplateExist: " + lang + ", " + pageTitle);
        boolean hasTemplate = false;
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(doesTemplateExistQuery);
            st.setString(1, pageTitle);
            ResultSet set = st.executeQuery();
            hasTemplate = set.next();
        } catch (SQLException s) {
            System.err.println(s);
        }
        long end = System.currentTimeMillis();
        System.out.println("doesTemplateExist: " + (end - start) + " ms.");
        return hasTemplate;
    }

    // @formatter:off
    static String findAllTemplatesWithLangLinkQuery = 
            "SELECT sub.tl_title, langlinks.ll_lang, langlinks.ll_title "
            + "FROM ("
                + "SELECT tl_title "
                + "FROM templatelinks INNER JOIN page ON tl_from=page_id "
                + "WHERE page_title=? AND page_namespace=0 AND tl_namespace=10"
            + ") sub INNER JOIN page ON sub.tl_title=page.page_title LEFT JOIN langlinks ON page.page_id=langlinks.ll_from "
            + "WHERE page_namespace=10;";
    // @formatter:on
    
    // @formatter:off
    static String findTemplatesWithLangFilter = 
            "SELECT sub.tl_title "
            + "FROM ("
                + "SELECT tl_title "
                + "FROM templatelinks INNER JOIN page ON tl_from=page_id "
                + "WHERE page_title=? AND page_namespace=0 AND tl_namespace=10"
            + ") sub INNER JOIN page ON sub.tl_title=page_title LEFT JOIN langlinks ON page_id=ll_from "
            + "WHERE page_namespace=10 "
            + "GROUP BY sub.tl_title "
            + "HAVING MAX(CASE WHEN ll_lang=? THEN 1 ELSE 0 END) = 0;";
    // @formatter:on
    

    // Map of templates to its langlinks
    public static List<String> findTemplatesWithLangFilter(String lang, String article, String bannedLang) {
        article = fixArticleName(article);
        List<String> result = new ArrayList<String>();
        try {
            Connection conn = ConnectionFactory.getConnection(lang);
            PreparedStatement st = conn.prepareStatement(findTemplatesWithLangFilter);
            st.setString(1, article);
            st.setString(2, bannedLang);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String tl_title = new String(set.getBytes("sub.tl_title"), "UTF-8");
                result.add(tl_title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    private static String fixArticleName(String name) {
        return name.replaceAll(" ", "_");
    }

    static String findLangLinks1 = "SELECT * FROM langlinks WHERE ll_from IN (";
    static String findLangLinks2 = ");";

    public static List<Pair<String, String>> findLangLinks(String lang, Set<Integer> transcludedIds) {
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        try {
            String query = findLangLinks1 + StringUtils.join(transcludedIds, ",") + findLangLinks2;
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(query);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String lllang = set.getString("ll_lang");
                String lltitle = new String(set.getBytes("ll_title"), "UTF-8");
                result.add(Pair.of(lllang, lltitle));
            }
        } catch (SQLException s) {
            System.err.println(s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    static String findTranscludedInArticlesAndLangLinksFullQuery = "SELECT page_title, ll_lang, ll_title "
            + "FROM templatelinks INNER JOIN page ON tl_from=page_id INNER JOIN langlinks ON page_id=ll_from "
            + "WHERE tl_title=? AND tl_namespace=10 AND page_namespace=0;";

    public static List<Pair<String, String>> findTranscludedInArticlesAndLangLinksFull(String pageLang, String templateTitle) {
        if (templateTitle.startsWith("Template:")) {
            throw new IllegalArgumentException("Wrong template title: " + templateTitle);
        }
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(pageLang).prepareStatement(
                    findTranscludedInArticlesAndLangLinksFullQuery);
            st.setString(1, templateTitle);
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String page_title = new String(set.getBytes("page_title"), "UTF-8");
                String ll_lang = set.getString("ll_lang");
                String ll_title = new String(set.getBytes("ll_title"), "UTF-8");
                result.add(Pair.of(ll_lang, ll_title));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    // @formatter:off
    static String findAllTemplatesQuery = 
            "SELECT sub.tl_title, langlinks.ll_lang, langlinks.ll_title "
            + "FROM ("
                + "SELECT tl_title "
                + "FROM templatelinks INNER JOIN page ON tl_from=page_id "
                + "WHERE page_title=? AND page_namespace=0 AND tl_namespace=10"
            + ") sub INNER JOIN page ON sub.tl_title=page_title LEFT JOIN langlinks ON page_id=ll_from "
            + "WHERE page_namespace=10;";
    // @formatter:on
    
    // @return list of all templates
    public static Set<String> findAllTemplates(TemplateInterwikiStorage interwikiStorage, String lang, String article) {
        article = fixArticleName(article);
        Set<String> result = new HashSet<String>();
        try {
            Connection conn = ConnectionFactory.getConnection(lang);
            PreparedStatement st = conn.prepareStatement(findAllTemplatesQuery);
            st.setString(1, article);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String tl_title = new String(set.getBytes("sub.tl_title"), "UTF-8");
                String ll_lang = set.getString("langlinks.ll_lang");
                String ll_title = null;
                if (set.getBytes("langlinks.ll_title")!=null) {
                    ll_title = new String(set.getBytes("langlinks.ll_title"), "UTF-8");
                }
                interwikiStorage.addInterwiki(lang, tl_title, ll_lang, ll_title);
                result.add(tl_title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


}