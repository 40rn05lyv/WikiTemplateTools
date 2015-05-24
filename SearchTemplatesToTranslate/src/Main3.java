import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Main3 {

    public static Connection outputConn;

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            outputConn = DriverManager.getConnection("jdbc:sqlite:ru_templates.sqlite");
            String selectQuery = "SELECT id, template_title, links_with_uk_count, links_without_uk_count, red_links_count FROM Templates ORDER BY template_title ASC;";
            PreparedStatement stat = outputConn.prepareStatement(selectQuery);
            ResultSet set = stat.executeQuery();
            while (set.next()) {
                int id = set.getInt(1);
                String title = set.getString(2);
                int linksWithUk = set.getInt(3);
                int linksWithoutUk = set.getInt(4);
                int redLinks = set.getInt(5);
                double stats = 0;
                if (linksWithUk > 0) {
                    stats = (double) linksWithUk / (double) (linksWithUk + linksWithoutUk + redLinks) * 100;
                }
                System.out.println(title + "\t" + linksWithUk + "\t" + linksWithoutUk + "\t" + redLinks + "\t" + String.format("%.2f", stats));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                outputConn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
