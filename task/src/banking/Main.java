package banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Action logic = new Action();
        if (args[0].equals("-fileName")) {
            String url = "jdbc:sqlite:" + args[1];
            try (Connection con = DriverManager.getConnection(url)) {
                logic.menu(con);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No database file specified.");
        }



        //go.menu();
        //logic.dataBaseOperations(args[2]);

//        try (Connection con = dataSource.getConnection()) {
//            try (Statement statement = con.createStatement()) {
//                try (ResultSet greatHouses = statement.executeQuery("SELECT * FROM HOUSES")) {
//                    while (greatHouses.next()) {
//
//                    }
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
}