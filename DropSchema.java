import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropSchema {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/campus360";
        String user = "postgres";
        String password = "Root123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            stmt.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;");
            System.out.println("Dropped and recreated schema public.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
