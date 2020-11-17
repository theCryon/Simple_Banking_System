package banking;

public class Account {
    private String cardNumber;
    private String cardPIN;
    private double balance;

    public Account() {
    }

    public Account(String cardNumber, String cardPIN, double balance) {
        this.cardNumber = cardNumber;
        this.cardPIN = cardPIN;
        this.balance = balance;
    }

    public String getCardPIN() {
        return cardPIN;
    }

    public void setCardPIN(String cardPIN) {
        this.cardPIN = cardPIN;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double accountBalance) {
        this.balance = accountBalance;
    }
}

