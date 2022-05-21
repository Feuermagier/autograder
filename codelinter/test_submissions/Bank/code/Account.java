public class Account {
    private final int id;
    private final String firstname;
    private final String lastname;
    private double balance;

    public Account(int id, String firstname, String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public int getId() {
        return this.id;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public double getBalance() {
        return this.balance;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    @Override
    public boolean equals(Object other) {
        return this.id == ((Account) other).id;
    }
}