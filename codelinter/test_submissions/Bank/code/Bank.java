import java.util.ArrayList;

public class Bank {
    private ArrayList<Account> accounts = new ArrayList<>();

    public Account addAccount(String firstname, String lastname) {
        Account account = new Account(this.accounts.size(), firstname, lastname);
        this.accounts.add(account);
        return account;
    }

    public ArrayList<Account> getAccounts(String lastname) {
        ArrayList<Account> result = new ArrayList<>();
        for (Account account : this.accounts) {
            if (account.getLastname().equals(lastname)) {
                result.add(account);
            }
        }
        return result;
    }

    public Account getAccount(int id) {
        return this.accounts.get(id);
    }
}