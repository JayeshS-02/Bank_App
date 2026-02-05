package com.bank.presentation;

//This class serves as the presentation layer for the banking application.

import com.bank.models.Transaction;
import com.bank.service.BankService;
import com.bank.service.BankServiceImpl;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.*;

import java.util.Scanner;

/**
 * BankingApp - CLI/Driver for manual testing of the banking system Demonstrates
 * all operations: deposit, withdraw, transfer, balance check, transaction
 * history
 */
public class BankingApp {

	private static BankService bankService;
	private static Scanner scanner;

	public static void main(String[] args) {
		System.out.println("==============================================");
		System.out.println("   BANK SERVER APP ");
		System.out.println("==============================================\n");

		// Initialize the banking service with 10 accounts (A001-A010)
		bankService = new BankServiceImpl();
		scanner = new Scanner(System.in);
		

		// MINIMAL CHECK: this should NOT throw if seeding worked
		try {
		    double bal = bankService.getBalance("A001");
		    System.out.println("DEBUG: A001 exists. Balance = " + bal);
		} catch (Exception e) {
		    System.out.println("DEBUG: Seeding failed or wrong service in use. " + e.getMessage());
		}


		System.out.println("✓ System initialized with 10 accounts:");
		System.out.println("  Account IDs: A001, A002, A003, A004, A005, A006, A007, A008, A009, A010");
		System.out.println("  Initial Balance: ₹ 0.00 for all accounts\n");

		// Main menu loop
		boolean running = true;
		while (running) {
			running = showMenuAndExecute();
		}

		scanner.close();
		System.out.println("\nThank you for using Bank Server App!");
	}

	private static boolean showMenuAndExecute() {
		System.out.println("\n==============================================");
		System.out.println("            BANKING OPERATIONS MENU");
		System.out.println("==============================================");
		System.out.println("1. Check Balance");
		System.out.println("2. Deposit Money");
		System.out.println("3. Withdraw Money");
		System.out.println("4. Transfer Money");
		System.out.println("5. View Last 10 Transactions");
		System.out.println("0. Exit");
		System.out.println("==============================================");
		System.out.print("Enter your choice: ");

		try {
			int choice = Integer.parseInt(scanner.nextLine().trim());
			System.out.println();

			switch (choice) {
			case 1:
				checkBalance();
				break;
			case 2:
				depositMoney();
				break;
			case 3:
				withdrawMoney();
				break;
			case 4:
				transferMoney();
				break;
			case 5:
				viewTransactions();
				break;
			case 0:
				return false;
			default:
				System.out.println("Invalid choice! Please try again.");
			}
		} catch (NumberFormatException e) {
			System.out.println("Invalid input! Please enter a number.");
		}

		return true;
	}

	private static void checkBalance() {
		System.out.print("Enter Account ID (e.g., A001): ");
		String accountId = scanner.nextLine().trim();

		try {
			double balance = bankService.getBalance(accountId);
			System.out.println("✓ Account: " + accountId);
			System.out.println("✓ Current Balance: ₹" + String.format("%.2f", balance));
		} catch (InvalidAccountException e) {
			System.out.println(" Error: " + e.getMessage());
		}
	}

//    Tried to validate account before entering money
    private static void depositMoney() {
        System.out.print("Enter Account ID (e.g., A001): ");
        String accountId = scanner.nextLine().trim();

        try {
            // VALIDATE FIRST
            bankService.getBalance(accountId);
        } catch (InvalidAccountException e) {
            System.out.println("Error: " + e.getMessage());
            return;  // STOP immediately
        }

        System.out.print("Enter amount to deposit: ₹");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            double newBalance = bankService.deposit(accountId, amount);
            System.out.println("✓ Deposit successful!");
            System.out.println("✓ Amount deposited: ₹" + String.format("%.2f", amount));
            System.out.println("✓ New balance: ₹" + String.format("%.2f", newBalance));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format!");
        } catch (NegativeOrZeroAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

//    Tried to validate account before entering money
    private static void withdrawMoney() {
        System.out.print("Enter Account ID (e.g., A001): ");
        String accountId = scanner.nextLine().trim();

        try {
            bankService.getBalance(accountId);  // validate first
        } catch (InvalidAccountException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        System.out.print("Enter amount to withdraw: ₹");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            double newBalance = bankService.withdraw(accountId, amount);
            System.out.println("✓ Withdrawal successful!");
            System.out.println("✓ Amount withdrawn: ₹" + amount);
            System.out.println("✓ New balance: ₹" + newBalance);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void transferMoney() {
        // 1) Read and validate source
        System.out.print("Enter Source Account ID (e.g., A001): ");
        String fromAccount = scanner.nextLine().trim();

        if (fromAccount.isEmpty()) {
            System.out.println("Error: Source account ID cannot be blank.");
            return;
        }

        try {
            // Will throw InvalidAccountException if not found
            bankService.getBalance(fromAccount);
        } catch (InvalidAccountException e) {
            System.out.println("Error: " + e.getMessage());
            return; // stop immediately
        }

        // 2) Read destination, validate existence, and short-circuit if same as source
        System.out.print("Enter Destination Account ID (e.g., A002): ");
        String toAccount = scanner.nextLine().trim();

        if (toAccount.isEmpty()) {
            System.out.println("Error: Destination account ID cannot be blank.");
            return;
        }

        // EARLY SAME-ACCOUNT CHECK (case-insensitive; change to equals if you prefer strict)
        if (fromAccount.equalsIgnoreCase(toAccount)) {
            System.out.println("Error: Source and destination cannot be the same (" + fromAccount + ").");
            return; // stop here; do not ask for amount
        }

        try {
            // This will validate destination exists before asking amount
            bankService.getBalance(toAccount);
        } catch (InvalidAccountException e) {
            System.out.println("Error: " + e.getMessage());
            return; // stop immediately
        }

        // 3) It will now ask for amount
        System.out.print("Enter amount to transfer: ₹");
        String input = scanner.nextLine().trim();

        final double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format!");
            return;
        }

        // 4) Execute transfer
        try {
            bankService.transfer(fromAccount, toAccount, amount);
            System.out.println("✓ Transfer successful!");
            System.out.println("✓ " + fromAccount + " → " + toAccount);
        } catch (NegativeOrZeroAmountException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SameAccountTransferException e) {
            // This should not occur now due to our early check, but keep it as a safety net.
            System.out.println("Error: " + e.getMessage());
        } catch (InvalidAccountException e) {
            // Safety net: in case accounts were removed between validation and transfer.
            System.out.println("Error: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

	private static void viewTransactions() {
		System.out.print("Enter Account ID (e.g., A001): ");
		String accountId = scanner.nextLine().trim();

		try {
			Transaction[] transactions = bankService.getLast10Transactions(accountId);

			if (transactions.length == 0) {
				System.out.println("ℹ No transactions found for account: " + accountId);
				return;
			}

			System.out.println("\n=== Last " + transactions.length + " Transaction(s) for " + accountId + " ===");
			System.out.println("─".repeat(80));

			for (int i = 0; i < transactions.length; i++) {
				Transaction t = transactions[i];
				System.out.println((i + 1) + ". " + t.getType() + " | Amount: ₹" + String.format("%.2f", t.getAmount())
						+ " | Balance After: " + String.format("%.2f", t.getBalanceAfter()) + " | Time: "
						+ t.getTimestamp());
				if (t.getTargetAccountId() != null) {
					System.out.println("   → To: " + t.getTargetAccountId());
				}
			}
			System.out.println("─".repeat(80));
		} catch (InvalidAccountException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

}
