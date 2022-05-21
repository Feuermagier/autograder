import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();

        Scanner scanner = new Scanner(System.in);
        boolean quit = false;
        while (!quit) {
            String[] parts = scanner.nextLine().split(" ", -1);
            String argumentPart = parts.length > 1 ? parts[1] : "";
            String[] arguments = argumentPart.split(";", -1);
            switch (parts[0]) {
                case "add-account": {
                    Account account = bank.addAccount(arguments[0], arguments[1]);
                    System.out.println(account.getId());
                    break;
                }
                case "get-accounts": {
                    ArrayList<Account> accounts = bank.getAccounts(arguments[0]);
                    if (accounts.isEmpty()) {
                        System.out.println("No accounts found");
                    } else {
                        for (Account account : accounts) {
                            System.out.println(account.getId() + ";" + account.getFirstname() + ";" + account.getLastname() + ";" + account.getBalance());
                        }
                    }
                    break;
                }
                case "deposit": {
                    int id = Integer.parseInt(arguments[0]);
                    double amount = Double.parseDouble(arguments[1]);
                    Account account = bank.getAccount(id);
                    account.deposit(amount);
                    System.out.println(account.getBalance());
                    break;
                }
                case "quit": {
                    quit = true;
                    break;
                }
                default: {
                    System.out.println("Unknown command");
                }
            }
        }
        scanner.close();
    }
}