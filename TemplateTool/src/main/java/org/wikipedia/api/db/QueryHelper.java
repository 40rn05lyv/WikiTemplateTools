package org.wikipedia.api.db;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wikipedia.api.Constants;
import org.wikipedia.api.PageInterwikiStorage;
import org.wikipedia.api.PageUtils;
import org.wikipedia.api.WikitextHelper;
import org.wikipedia.api.http.ApiHelper;

import com.google.common.base.Optional;

public class QueryHelper {

    private static final int NAMESPACE_TEMPLATE = 10;

    // @formatter:off
    static String linksHereWithoutInterwikiQuery = 
            "SELECT page.page_title "
            + "FROM ( "
            + "SELECT page.page_id, page.page_title "
            + "FROM templatelinks INNER JOIN page ON templatelinks.tl_from=page.page_id "
            + "WHERE templatelinks.tl_namespace=10 AND templatelinks.tl_title=? AND page.page_namespace=10 "
            + ") page LEFT JOIN langlinks ON page.page_id=langlinks.ll_from "
            + "WHERE langlinks.ll_from IS NULL "
            + "LIMIT ? OFFSET ?;";
    // @formatter:on

    public static List<String> getLinksHereTemplatesWithoutInterwiki(String lang, String templateTitle, int offset, int limit) {
        templateTitle = PageUtils.toDBView(templateTitle);
        List<String> list = new ArrayList<String>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(linksHereWithoutInterwikiQuery);
            st.setString(1, templateTitle);
            st.setInt(2, limit);
            st.setInt(3, offset);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String title = new String(set.getBytes("page.page_title"), "UTF-8");
                list.add(title);
                System.out.println(title);
            }
        } catch (SQLException s) {
            System.err.println(s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> getLinksHereTemplatesWithoutInterwiki(String lang, String parentTemplate) {
        return getLinksHereTemplatesWithoutInterwiki(lang, parentTemplate, 0, Integer.MAX_VALUE);
    }

    // @formatter:off
    static String getAllTemplatesWithoutInterwikiQuery = 
            "SELECT page_title "
            + "FROM page LEFT JOIN langlinks ON page_id=ll_from "
            + "WHERE page_namespace=10 AND ll_from IS NULL "
            + "LIMIT ? OFFSET ?;";
    // @formatter:on

    public static List<String> getAllTemplatesWithoutInterwiki(String lang, int offset, int limit) {
        List<String> list = new ArrayList<String>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(getAllTemplatesWithoutInterwikiQuery);
            st.setInt(1, limit);
            st.setInt(2, offset);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String title = new String(set.getBytes("page.page_title"), "UTF-8");
                list.add(title);
                System.out.println(title);
            }
        } catch (SQLException s) {
            System.err.println(s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> getAllTemplatesWithoutInterwiki(String lang) {
        return getAllTemplatesWithoutInterwiki(lang, 0, Integer.MAX_VALUE);
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
        pageTitle = PageUtils.toDBView(pageTitle);
        pageTitle = PageUtils.removeNamespace(pageTitle);
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
        templateTitle = PageUtils.toDBView(templateTitle);
        templateTitle = PageUtils.removeNamespace(templateTitle);
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
    public static Set<String> findAllTemplates(PageInterwikiStorage interwikiStorage, String lang, String article) {
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
                if (set.getBytes("langlinks.ll_title") != null) {
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

    static String findAllLinksAndLangLinksQuery = "SELECT pl_title, ll_lang, ll_title "
            + "FROM page INNER JOIN pagelinks ON page_id=pl_from INNER JOIN langlinks ON page_id=ll_from "
            + "WHERE page_title=? AND page_namespace=10 AND pl_namespace=0;";

    private static List<Pair<String, String>> findAllLinksAndLangLinks(String lang, String templateTitle) {
        templateTitle = PageUtils.toDBView(templateTitle);
        templateTitle = PageUtils.removeNamespace(templateTitle);
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(findTranscludedInArticlesAndLangLinksFullQuery);
            st.setString(1, templateTitle);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String pl_title = new String(set.getBytes("pl_title"), "UTF-8");
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
    private static String findAllLinksQuery = "SELECT pl_title " 
            + "FROM page INNER JOIN pagelinks ON page_id=pl_from "
            + "WHERE page_title=? AND page_namespace=? AND pl_namespace=?;";
    // @formatter:on

    private static Set<String> findAllLinks(String lang, String page, int pageNamespace, int linksNamespace) {
        page = PageUtils.toDBView(page);
        page = PageUtils.removeNamespace(page);
        Set<String> result = new HashSet<String>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(findAllLinksQuery);
            st.setString(1, page);
            st.setInt(2, pageNamespace);
            st.setInt(3, linksNamespace);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String pl_title = new String(set.getBytes("pl_title"), "UTF-8");
                result.add(pl_title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Set<String> findPureLinksInTemplate(String lang, String template, int linksNamespace) {
        Set<String> allLinks = findAllLinks(lang, template, Constants.NAMESPACE_TEMPLATE, linksNamespace);
        Optional<String> expandedTemplate = ApiHelper.expandTemplate(lang, template);
        if (expandedTemplate.isPresent()) {
            Set<String> expandedLinks = WikitextHelper.getLinks(expandedTemplate.get());
            
            Set<String> allLinksConverted = new HashSet<String>();
            for (String link: allLinks) {
                link = PageUtils.toNormalView(link);
                link = PageUtils.addNamespace(lang, link, linksNamespace);
                allLinksConverted.add(PageUtils.toNormalView(link));
            }
            
            Set<String> expandedLinksConverted = new HashSet<String>();
            for (String link: expandedLinks) {
                link = PageUtils.toNormalView(link);
                link = PageUtils.addNamespace(lang, link, linksNamespace);
                expandedLinksConverted.add(PageUtils.toNormalView(link));
            }
            
            allLinksConverted.retainAll(expandedLinksConverted);
            return allLinksConverted;
        }
        return new HashSet<String>();
    }

    // @formatter:off
    private static final String findLangLinksQuery_MultiplePages = 
            "SELECT page_title, ll_lang, ll_title "
            + "FROM page INNER JOIN langlinks ON page_id=ll_from "
            + "WHERE page_title IN (%s) AND page_namespace=?;";
    // @formatter:on

    public static PageInterwikiStorage findLangLinks(String pageLang, Set<String> pages, int pagesNamespace, PageInterwikiStorage storage) {
        if (pages == null || pages.isEmpty()) {
            return storage;
        }

        // Converting
        Set<String> convertedPages = new HashSet<String>();
        for (String page: pages) {
            page = PageUtils.toDBView(page);
            page = PageUtils.removeNamespace(page);
            convertedPages.add(page);
        }

        try {
            String query = String.format(findLangLinksQuery_MultiplePages, preparePlaceHolders(convertedPages.size()));
            PreparedStatement st = ConnectionFactory.getConnection(pageLang).prepareStatement(query);
            int i = 1;
            for (String page : convertedPages) {
                st.setString(i++, page);
            }
            st.setInt(i++, pagesNamespace);
            System.out.println("Querying " + pageLang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String page_title = new String(set.getBytes("page_title"), "UTF-8");
                String ll_lang = set.getString("ll_lang");
                String ll_title = new String(set.getBytes("ll_title"), "UTF-8");
                storage.addInterwiki(pageLang, page_title, ll_lang, ll_title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return storage;
    }

    public static String preparePlaceHolders(int length) {
        StringBuilder builder = new StringBuilder(length * 2 - 1);
        for (int i = 0; i < length; i++) {
            if (i > 0)
                builder.append(',');
            builder.append('?');
        }
        return builder.toString();
    }

    // @formatter:off
    private static final String findLangLinksQuery_SinglePage = 
            "SELECT page_title, ll_lang "
            + "FROM page INNER JOIN langlinks ON page_id=ll_from "
            + "WHERE page_title=? AND page_namespace=?;";
    // @formatter:on

    // NOTE: it is better to use another findLangLinks implementation 
    public static Map<String, String> findLangLinks(String lang, String page, String namespace) {
        page = PageUtils.toDBView(page);
        page = PageUtils.removeNamespace(page);
        Map<String, String> result = new HashMap<String, String>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(findLangLinksQuery_SinglePage);
            st.setString(1, page);
            st.setString(2, namespace);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String page_title = new String(set.getBytes("page_title"), "UTF-8");
                String ll_lang = set.getString("ll_lang");
                result.put(ll_lang, page_title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }    
    
    // @formatter:off
    private static final String findAllTemplatesThatLinksToPageQuery = 
            "SELECT page_title "
            + "FROM page INNER JOIN pagelinks ON page_id=pl_from "
            + "WHERE page_namespace=10 AND pl_title=? AND pl_namespace=?;";
    // @formatter:on
    
    public static Set<String> findAllTemplatesThatHaveLinksToPage(String lang, String page, int pagesNamespace) {
        page = PageUtils.toDBView(page);
        page = PageUtils.removeNamespace(page);
        Set<String> result = new HashSet<String>();
        try {
            PreparedStatement st = ConnectionFactory.getConnection(lang).prepareStatement(findAllTemplatesThatLinksToPageQuery);
            int i=1;
            st.setString(i++, page);
            st.setInt(i++, pagesNamespace);
            System.out.println("Querying " + lang + ": " + st.toString());
            ResultSet set = st.executeQuery();
            while (set.next()) {
                String page_title = new String(set.getBytes("page_title"), "UTF-8");
                result.add(page_title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

}