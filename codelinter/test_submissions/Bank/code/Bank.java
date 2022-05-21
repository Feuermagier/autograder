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

    /**
     * Returns the account with the specified id
     * @param id The id
     * @return The account
     */
    public Account getAccount(int id) {
        if (id >= this.accounts.size()) {
            return null;
        }
        return this.accounts.get(id);
    }
}