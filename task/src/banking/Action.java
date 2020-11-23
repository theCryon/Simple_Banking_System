package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Action {
    private Scanner scanner = new Scanner(System.in);
    private ArrayList<Account> accounts = new ArrayList<>();

    public void menu(Connection con) {
        createTable(con);
        while (true) {
            printMenu();
            switch (scanner.nextInt()) {
                case 1:
                    Account account = new Account(generateCardNumber(), generateCardPIN(), 0);
                    accounts.add(account);
                    updateDataBase(con, account);
                    printAccountCreated(account.getCardNumber(), account.getCardPIN());
                    break;
                case 2:
                    loginIntoAccount(con);
                    break;
                case 0:
                    System.out.println("Bye!");
                    return;
                default:
                    break;
            }
        }
    }

    public void printMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    public void printAccountCreated(String cardNumber, String cardPIN) {
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(cardPIN);
    }

    public String generateCardPIN() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(4);
        for (int i = 0; i < stringBuilder.capacity(); i++) {
            stringBuilder.append(random.nextInt(9));
        }
        return stringBuilder.toString();
    }

    public String generateCardNumber() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        String BIN = "400000";
        stringBuilder.append(BIN);
        for (int i = 0; i < 9; i++) {
            stringBuilder.append(random.nextInt(9));
        }
        int controlNumber = 0;
        int tmp;
        for (int i = 0; i < 15; i++) {
            tmp = (i % 2 != 0)
                    ? Integer.parseInt(String.valueOf(stringBuilder.charAt(i)))
                    : Integer.parseInt(String.valueOf(stringBuilder.charAt(i))) * 2;
            tmp = tmp > 9 ? tmp - 9 : tmp;
            controlNumber += tmp;
        }
        int checksum = 0;
        if (controlNumber % 10 != 0) {
            while ((controlNumber + checksum) % 10 != 0) {
                checksum++;
            }
        }
        stringBuilder.append(checksum);
        return stringBuilder.toString();
    }
    
    public void loginIntoAccount(Connection con) {
        System.out.println("Enter your card number:");
        String cardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String cardPIN = scanner.next();
        if (compareDataFromDatabase(con, cardNumber, cardPIN)) {
            for (Account acc : accounts) {
                if (acc.getCardNumber().equals(cardNumber) && acc.getCardPIN().equals(cardPIN)) {
                    accountMenu(acc);
                    return;
                } else {
                    Account newAcc = new Account(cardNumber, cardPIN, 0);
                    accountMenu(newAcc);
                }
            }
        }
        System.out.println("Wrong card number or PIN!");
    }

    public void accountMenu(Account account) {
        System.out.println("You have successfully logged in!");
        while (true) {
            System.out.println("1. Balance");
            System.out.println("2. Log out");
            System.out.println("0. Exit");
            switch (scanner.nextInt()) {
                case 1:
                    System.out.println("Balance: " + account.getBalance());
                    break;
                case 2:
                    return;
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
            }
        }
    }

    public void createTable(Connection con) {
        try (Statement statement = con.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS card (" +
                                      "id INTEGER PRIMARY KEY," +
                                      "number TEXT," +
                                      "pin TEXT," +
                                      "balance INTEGER DEFAULT 0 " +
                                      ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDataBase(Connection con, Account account) {
        String sql = "INSERT INTO card(number, pin) VALUES (?,?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, account.getCardNumber());
            pstmt.setString(2, account.getCardPIN());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String extractFromDataBase(Connection con, String column, String value) {
        String sql = "SELECT " + column + " FROM card WHERE " + column + " == ?;";
        String result = null;
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result = rs.getString(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean compareDataFromDatabase(Connection con, String accountNumber, String accountPIN) {
        String[] results = new String[2];
        results[0] = extractFromDataBase(con, "number", accountNumber);
        results[1] = extractFromDataBase(con, "pin", accountPIN);
        return accountNumber.equals(results[0]) && accountPIN.equals(results[1]);
    }
}
