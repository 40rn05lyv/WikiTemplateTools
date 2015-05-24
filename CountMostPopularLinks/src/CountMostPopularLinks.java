import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CountMostPopularLinks {
    
    private static final String CountMostPopularArticlesQuery = 
            "SELECT pl_title, COUNT(*) "
            + "FROM pagelinks INNER JOIN page ON pl_from=page_id "
            + "WHERE pl_namespace=0 AND pl_from_namespace=0 "
            + "GROUP BY pl_title "
            + "ORDER BY COUNT(*) DESC "
            + "LIMIT ?";
    
    public static void main(String[] args) {
        try {
            ConnectionFactory.init();
            Connection ukdb = ConnectionFactory.getConnection("uk");
            PreparedStatement statement = ukdb.prepareStatement(CountMostPopularArticlesQuery);
            statement.setInt(1, 5000); // limit
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String article = new String(result.getBytes("pl_title"), "UTF-8");
                int count = result.getInt(2);
                System.out.println(article + "\t\t\t" + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            ConnectionFactory.stop();
        }
    }

}
