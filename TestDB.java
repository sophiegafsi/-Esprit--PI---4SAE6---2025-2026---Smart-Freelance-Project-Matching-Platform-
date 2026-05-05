import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3307/recompense_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
        try {
            Connection conn = DriverManager.getConnection(url, "root", "");
            System.out.println("SUCCESS! recompense_db connected/created.");
            conn.close();
            
            Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost:3307/evaluationdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC", "root", "");
            System.out.println("SUCCESS! evaluationdb connected/created.");
            conn2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
