package com.bank.presentation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLI tests for BankingApp WITHOUT reflection and WITHOUT mocking.
 * Each test:
 *  - Scripts user input via System.setIn (choices + data),
 *  - Captures console via System.setOut,
 *  - Invokes BankingApp.main(...),
 *  - Asserts on output.
 *
 * Preconditions to pass:
 *  1) BankingApp compiles (replace any '&lt;' with '<' in for-loops).
 *  2) BankServiceImpl initializes 10 accounts A001..A010 on startup (via constructor/init()).
 *  3) Repository appendTransaction delegates to Account.appendTransaction(...) to avoid NPE on withdrawals.
 *  4) Exceptions/messages roughly match the substrings we assert (e.g., "Error:", "Deposit successful").
 */
public class BankingAppTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void runWithScript(String script) {
        System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
        BankingApp.main(new String[0]);
    }

    private String console() {
        return out.toString(StandardCharsets.UTF_8);
    }

    // -----------------------
    // Menu shell / generic
    // -----------------------

    @Test
    @DisplayName("Exit immediately (choice 0)")
    void exitImmediately() {
        runWithScript("0\n");
        String o = console();
        assertTrue(o.contains("BANK SERVER APP"));
        assertTrue(o.contains("BANKING OPERATIONS MENU"));
        assertTrue(o.contains("Thank you for using Bank Server App!"));
    }

    @Test
    @DisplayName("Invalid non-numeric choice prints hint")
    void invalidNonNumericChoice() {
        runWithScript("abc\n0\n");
        String o = console();
        assertTrue(o.contains("Invalid input! Please enter a number."));
        assertTrue(o.contains("Thank you for using Bank Server App!"));
    }

    @Test
    @DisplayName("Invalid numeric choice prints 'Invalid choice'")
    void invalidNumericChoice() {
        runWithScript("9\n0\n");
        String o = console();
        assertTrue(o.contains("Invalid choice! Please try again."));
    }

    // -----------------------
    // 1. Check Balance
    // -----------------------

    @Test
    @DisplayName("Check Balance - success for A001")
    void checkBalanceSuccess() {
        runWithScript("1\nA001\n0\n");
        String o = console();
        assertTrue(o.contains("Enter Account ID"));
        assertTrue(o.contains("Current Balance: ₹"));
    }

    @Test
    @DisplayName("Check Balance - invalid account")
    void checkBalanceInvalidAccount() {
        runWithScript("1\nA999\n0\n");
        String o = console();
        assertTrue(o.contains("Error:"), "Should show an error message for invalid account");
    }

    @Test
    @DisplayName("Check Balance - blank id (edge)")
    void checkBalanceBlankId() {
        runWithScript("1\n   \n0\n");
        String o = console();
        assertTrue(o.contains("Error:"), "Blank/whitespace id should be rejected");
    }

    // -----------------------
    // 2. Deposit
    // -----------------------

    @Test
    @DisplayName("Deposit - success")
    void depositSuccess() {
        runWithScript(String.join("\n",
                "2", "A001", "100",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Deposit successful"));
        assertTrue(o.contains("Amount deposited: ₹100.00"));
        assertTrue(o.contains("New balance: ₹"));
    }

    @Test
    @DisplayName("Deposit - invalid account (validate first, no amount prompt)")
    void depositInvalidAccount() {
        runWithScript(String.join("\n",
                "2", "A999",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Invalid account should show error");
        assertFalse(o.contains("Enter amount to deposit"),
                "Should not ask for amount if account validation failed");
    }

    @Test
    @DisplayName("Deposit - zero amount (edge)")
    void depositZeroAmount() {
        runWithScript(String.join("\n",
                "2", "A001", "0",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Zero amount should be rejected");
    }

    @Test
    @DisplayName("Deposit - negative amount")
    void depositNegativeAmount() {
        runWithScript(String.join("\n",
                "2", "A001", "-50",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Negative amount should be rejected");
    }

    @Test
    @DisplayName("Deposit - invalid amount format")
    void depositInvalidAmountFormat() {
        runWithScript(String.join("\n",
                "2", "A001", "abc",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Invalid amount format!"));
    }

    // -----------------------
    // 3. Withdraw
    // -----------------------

    @Test
    @DisplayName("Withdraw - success (deposit first in same session)")
    void withdrawSuccess() {
        runWithScript(String.join("\n",
                "2", "A001", "200",   // deposit
                "3", "A001", "50",    // withdraw
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Withdrawal successful"));
        assertTrue(o.contains("Amount withdrawn: ₹50"));
        assertTrue(o.contains("New balance: ₹"));
    }

    @Test
    @DisplayName("Withdraw - invalid account (validate first)")
    void withdrawInvalidAccount() {
        runWithScript(String.join("\n",
                "3", "A999",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Invalid account should be reported");
        assertFalse(o.contains("Enter amount to withdraw"),
                "Should not ask amount after invalid account");
    }

    @Test
    @DisplayName("Withdraw - insufficient funds")
    void withdrawInsufficientFunds() {
        runWithScript(String.join("\n",
                "3", "A001", "999999",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Should print error");
        assertTrue(o.toLowerCase().contains("insufficient"), "Should mention insufficient funds");
    }

    @Test
    @DisplayName("Withdraw - invalid amount format")
    void withdrawInvalidAmountFormat() {
        runWithScript(String.join("\n",
                "3", "A001", "abc",
                "0", ""
        ));
        String o = console();
        assertFalse(o.contains("Invalid amount format!"));
    }

    // -----------------------
    // 4. Transfer
    // -----------------------

    @Test
    @DisplayName("Transfer - success (deposit to A001 then transfer to A002)")
    void transferSuccess() {
        runWithScript(String.join("\n",
                "2", "A001", "500",    // deposit to fund source
                "4", "A001", "A002", "120",  // transfer 120
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Transfer successful"));
        assertTrue(o.contains("A001 → A002") || o.contains("A001 \u2192 A002"));
    }

    @Test
    @DisplayName("Transfer - invalid source account")
    void transferInvalidSource() {
        runWithScript(String.join("\n",
                "4", "AXYZ",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Should fail fast on invalid source");
        assertFalse(o.contains("Enter Destination Account ID"),
                "Should not prompt destination after invalid source");
    }

    @Test
    @DisplayName("Transfer - invalid destination account")
    void transferInvalidDestination() {
        runWithScript(String.join("\n",
                "4", "A001", "B999",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Invalid destination should be reported");
        assertFalse(o.contains("Enter amount to transfer"),
                "Should not ask for amount after invalid destination");
    }

    @Test
    @DisplayName("Transfer - same-account edge case")
    void transferSameAccount() {
        runWithScript(String.join("\n",
                "4", "A001", "A001",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Same-account transfer must be rejected");
    }

    @Test
    @DisplayName("Transfer - invalid amount format")
    void transferInvalidAmountFormat() {
        runWithScript(String.join("\n",
                "4", "A001", "A002", "abc",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Invalid amount format!"));
    }

    @Test
    @DisplayName("Transfer - insufficient funds")
    void transferInsufficientFunds() {
        runWithScript(String.join("\n",
                "4", "A001", "A002", "999999",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Should report an error");
        assertTrue(o.toLowerCase().contains("insufficient"));
    }

    // -----------------------
    // 5. View Last 10 Transactions
    // -----------------------

    @Test
    @DisplayName("Transactions - none yet for a fresh account")
    void viewTransactionsNone() {
        runWithScript(String.join("\n",
                "5", "A003",
                "0", ""
        ));
        String o = console();
        assertTrue(o.toLowerCase().contains("no transactions"),
                "Should indicate no transactions found");
    }

    @Test
    @DisplayName("Transactions - after deposit and withdraw, entries print")
    void viewTransactionsAfterActivity() {
        runWithScript(String.join("\n",
                "2", "A004", "150",  // deposit
                "3", "A004", "40",   // withdraw
                "5", "A004",         // view
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Last"), "Should print last transactions section");
        assertTrue(o.contains("DEPOSIT") || o.contains("WITHDRAWAL"),
                "Should print transaction types");
        assertTrue(o.contains("Amount: ₹"), "Should show amounts");
    }

    @Test
    @DisplayName("Transactions - invalid account")
    void viewTransactionsInvalidAccount() {
        runWithScript(String.join("\n",
                "5", "ZZZZ",
                "0", ""
        ));
        String o = console();
        assertTrue(o.contains("Error:"), "Invalid account should be reported");
    }
}