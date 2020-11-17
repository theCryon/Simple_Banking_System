package banking;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Action {
    private Scanner scanner = new Scanner(System.in);
    private ArrayList<Account> accounts = new ArrayList<>();

    public void menu() {
        while (true) {
            printMenu();
            switch (scanner.nextInt()) {
                case 1:
                    Account account = new Account(generateCardNumber(), generateCardPIN(), 0);
                    accounts.add(account);
                    printAccountCreated(account.getCardNumber(), account.getCardPIN());
                    break;
                case 2:
                    loginIntoAccount();
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
        StringBuilder stringBuilder = new StringBuilder("400000");
        for (int i = 0; i < 10; i++) {
            stringBuilder.append(random.nextInt(9));
        }
        return stringBuilder.toString();
    }

    public void loginIntoAccount() {
        System.out.println("Enter your card number:");
        String cardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String cardPIN = scanner.next();
        for (Account acc : accounts) {
            if (acc.getCardNumber().equals(cardNumber) && acc.getCardPIN().equals(cardPIN)) {
                accountMenu(acc);
                return;
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
}
