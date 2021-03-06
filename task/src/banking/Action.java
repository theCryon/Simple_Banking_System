package banking;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

class Action {
    private final Scanner scanner = new Scanner(System.in);

    void menu(Connection con) {
        createTable(con);
        while (true) {
            printMenu();
            switch (scanner.next()) {
                case "1":
                    Account acc = createAccount();
                    updateAccountToDatabase(con, acc);
                    printAccountCreated(acc.getCardNumber(), acc.getCardPIN());
                    break;
                case "2":
                    loginIntoAccount(con);
                    break;
                case "0":
                    System.out.println("Bye!");
                    return;
                default:
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    private void printAccountMenu() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    String generateCardPIN() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(4);
        for (int i = 0; i < stringBuilder.capacity(); i++) {
            stringBuilder.append(random.nextInt(9));
        }
        return stringBuilder.toString();
    }

    String generateCardNumber() {
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

    public boolean checkForLuhn(String cardNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append(cardNumber);
        int tmp;
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            tmp = (i % 2 != 0)
                    ? Integer.parseInt(String.valueOf(sb.charAt(i)))
                    : Integer.parseInt(String.valueOf(sb.charAt(i))) * 2;
            tmp = tmp > 9 ? tmp - 9 : tmp;
            sum += tmp;
        }
        int checksum = Integer.parseInt(String.valueOf(sb.charAt(15)));
        return (sum + checksum) % 10 == 0;
    }

    private Account createAccount() {
        Account acc = new Account();
        acc.setCardNumber(generateCardNumber());
        acc.setCardPIN(generateCardPIN());
        acc.setBalance(0);
        return acc;
    }

    private void printAccountCreated(String cardNumber, String cardPIN) {
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(cardPIN);
    }

    private void loginIntoAccount(Connection con) {
        System.out.println("Enter your card number:");
        String cardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String cardPIN = scanner.next();
        if (checkValidLogin(con, cardNumber, cardPIN)) {
            Account newAcc = new Account(cardNumber, cardPIN, 0);
            accountMenu(con, newAcc);
        }
        System.out.println("Wrong card number or PIN!");
    }

    private void accountMenu(Connection con, Account acc) {
        System.out.println("You have successfully logged in!");
        while (true) {
            printAccountMenu();
            switch (scanner.next()) {
                case "1":
                    acc.setBalance(getBalanceFromDataBase(con, acc));
                    System.out.println("Balance: " + acc.getBalance());
                    break;
                case "2":
                    System.out.println("Enter income: ");
                    addIncome(con, acc, scanner.nextDouble());
                    System.out.println("Income was added!");
                    break;
                case "3":
                    transfer(con, acc);
                    break;
                case "4":
                    closeAccount(con, acc);
                    return;
                case "5":
                    return;
                case "0":
                    System.out.println("Bye!");
                    System.exit(0);
            }
        }
    }

    private void createTable(Connection con) {
        /*TODO CHANGE balance type*/
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

    private void updateAccountToDatabase(Connection con, Account account) {
        String sql = "INSERT INTO card(number, pin) VALUES (?,?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, account.getCardNumber());
            pstmt.setString(2, account.getCardPIN());
            pstmt.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            try {
                System.err.println("Couldn't update the account to the database. Rolling back changes.");
                con.rollback();
            } catch (SQLException rollbackE) {
                rollbackE.printStackTrace();
            }
        }
    }

    //Here would be great to use generics when I learn them.
    private String extractFromDataBase(Connection con, String column, String value) {
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

    private double extractBalanceFromDataBase(Connection con, String number) {
        String sql = "SELECT balance FROM card WHERE number == ?;";
        double result = 0.0;
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, number);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result = rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private double getBalanceFromDataBase(Connection con, Account acc) {
        double balance = 0.0;
        String sql = "SELECT balance FROM card WHERE number == ? AND pin == ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, acc.getCardNumber());
            pstmt.setString(2, acc.getCardPIN());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                balance = rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    private boolean checkValidLogin(Connection con, String accountNumber, String accountPIN) {
        String[] results = new String[2];
        results[0] = extractFromDataBase(con, "number", accountNumber);
        results[1] = extractFromDataBase(con, "pin", accountPIN);
        return accountNumber.equals(results[0]) && accountPIN.equals(results[1]);
    }

    private void addIncome(Connection con, Account acc, double balance) {
        String sql = "UPDATE card SET balance = balance + ? WHERE number == ? AND pin == ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, (int) balance);
            pstmt.setString(2, acc.getCardNumber());
            pstmt.setString(3, acc.getCardPIN());
            pstmt.executeUpdate();
            con.commit();
            acc.setBalance(balance);
        } catch (SQLException e) {
            try {
                System.err.println("Couldn't add income. Rolling back changes");
                con.rollback();
            } catch (SQLException rollbackE) {
                rollbackE.printStackTrace();
            }
        }
    }

    private void closeAccount(Connection con, Account acc) {
        String sql = "DELETE FROM card WHERE number == ? AND pin == ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, acc.getCardNumber());
            pstmt.setString(2, acc.getCardPIN());
            pstmt.executeUpdate();
            con.commit();
            System.out.println("The account has been closed!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void transfer(Connection con, Account acc) {
        String targetAccount;
        System.out.println("Transfer \n Enter card number: ");
        targetAccount = scanner.next();
        if (targetAccount.equals(extractFromDataBase(con, "number", targetAccount))) {
            System.out.println("Enter how much money you want to transfer: ");
            double transferAmount = scanner.nextDouble();

            if (transferAmount < extractBalanceFromDataBase(con, acc.getCardNumber())) {
                String sqlGiver = "UPDATE card SET balance = balance - ? WHERE number == ?;";
                String sqlRecipient = "UPDATE card SET balance = balance + ? WHERE number == ?;";
                try (PreparedStatement pstmtG = con.prepareStatement(sqlGiver);
                     PreparedStatement pstmtR = con.prepareStatement(sqlRecipient)) {
                    pstmtG.setInt(1, (int) transferAmount);
                    pstmtG.setString(2, acc.getCardNumber());
                    pstmtG.executeUpdate();

                    pstmtR.setInt(1, (int) transferAmount);
                    pstmtR.setString(2, targetAccount);
                    pstmtR.executeUpdate();
                    con.commit();
                } catch (SQLException e) {
                    try {
                        System.err.println("Couldn't transfer money. Rolling back changes.");
                        con.rollback();
                    } catch (SQLException rollbackE) {
                        rollbackE.printStackTrace();
                    }
                }
            } else {
                System.out.println("Not enough money!");
            }
        } else if(!checkForLuhn(targetAccount)) {
            System.out.println("Probably you made mistake in the card number. Please try again!");
        } else {
            System.out.println("Such a card does not exist.");
        }

    }
}
