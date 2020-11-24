package banking;

import org.sqlite.SQLiteDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Action logic = new Action();
        if (args[0].equals("-fileName")) {
            String url = "jdbc:sqlite:" + args[1];
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);
            try (Connection con = dataSource.getConnection()) {
                con.setAutoCommit(false);
                logic.menu(con);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No database file specified.");
        }
    }
}