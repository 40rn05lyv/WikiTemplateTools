import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.api.Constants;
import org.wikipedia.api.PageInterwikiStorage;
import org.wikipedia.api.UnifiedPage;
import org.wikipedia.api.db.ConnectionFactory;
import org.wikipedia.api.db.QueryHelper;

public class Main {

    public static Connection outputConn;

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            outputConn = DriverManager.getConnection("jdbc:sqlite:ru_templates_23_05_15.sqlite");
            ConnectionFactory.init();
            Map<Integer, String> templates = QueryHelper.findAllTemplatesWithoutInterwikiInSpecifiedLanguage("ru", "Навигационная_таблица",
                    "uk");
            int i = 0;
            templates = clearExisting(templates);
            for (Entry<Integer, String> entry : templates.entrySet()) {
                System.out.println("Processing " + i++ + " page.");
                Set<String> links = QueryHelper.findAllLinks("ru", entry.getValue(), Constants.NAMESPACE_TEMPLATE,
                        Constants.NAMESPACE_ARTICLE);
                PageInterwikiStorage storage = new PageInterwikiStorage(Constants.NAMESPACE_ARTICLE);
                QueryHelper.findLangLinks("ru", links, Constants.NAMESPACE_ARTICLE, storage);
                int totalCount = storage.getPages().size();
                int pagesWithUkCount = 0;
                Set<String> pagesWithUk = new TreeSet<String>();
                Set<String> pagesWithoutUk = new TreeSet<String>();
                for (UnifiedPage page : storage.getPages()) {
                    if (page.has("uk")) {
                        pagesWithUkCount++;
                        pagesWithUk.add(page.getOne("ru"));
                    } else {
                        pagesWithoutUk.add(page.getOne("ru"));
                    }
                }
                int percent = (int) ((double) pagesWithUkCount / (double) totalCount * 100);
                System.out.println("Page \"" + entry.getValue() + "\" has " + percent + "% (" + pagesWithUkCount + "/" + totalCount + ")");
                System.out.println("Pages with uk: ");
                System.out.println(pagesWithUk);
                System.out.println("Pages without uk: ");
                System.out.println(pagesWithoutUk);
                links.removeAll(pagesWithoutUk);
                links.removeAll(pagesWithUk);
                addToFile(entry.getKey(), entry.getValue(), pagesWithUk, pagesWithoutUk, links);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ConnectionFactory.stop();
            } finally {
                try {
                    outputConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static final String selectQuery = "SELECT id FROM Templates;";

    public static Map<Integer, String> clearExisting(Map<Integer, String> templates) {
        try {
            PreparedStatement stat = outputConn.prepareStatement(selectQuery);
            ResultSet set = stat.executeQuery();
            while (set.next()) {
                int id = set.getInt(1);
                templates.remove(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return templates;
    }

    public static final String insertQuery = "INSERT INTO Templates(id, template_title, links_with_uk_count, links_without_uk_count, red_links_count, links_with_uk, links_without_uk, red_links, insert_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    public static void addToFile(int pageId, String pageTitle, Set<String> linksWithUk, Set<String> linksWithoutUk, Set<String> redLinks) {
        try {
            PreparedStatement stat = outputConn.prepareStatement(insertQuery);
            int i = 1;
            stat.setInt(i++, pageId);
            stat.setString(i++, pageTitle);
            stat.setInt(i++, linksWithUk.size());
            stat.setInt(i++, linksWithoutUk.size());
            stat.setInt(i++, redLinks.size());
            stat.setString(i++, StringUtils.join(linksWithUk, "\n"));
            stat.setString(i++, StringUtils.join(linksWithoutUk, "\n"));
            stat.setString(i++, StringUtils.join(redLinks, "\n"));
            stat.setLong(i++, System.currentTimeMillis());
            stat.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
