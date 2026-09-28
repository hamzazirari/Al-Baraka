
import config.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.getConnection();
        DatabaseConnection.closeConnection();
    }
}