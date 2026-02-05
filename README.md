🏦 Banking Service (In‑Memory • Arrays • TDD • JUnit 5)
A clean, test‑driven, in‑memory banking service built in Java using arrays (no DB).
It supports Balance, Deposit, Withdraw, Transfer, and Last 10 Transactions (newest‑first), with clear domain exceptions and a CLI for manual testing.

✨ Features

🔹 10 pre‑created accounts at startup (A001…A010), each mapped 1:1 to a Customer
💰 Balance / Deposit / Withdraw / Transfer, with strict input validation
🧾 Transaction history: last 10 items, newest‑first (includes balanceAfter)
🧪 TDD-first with JUnit 5 (happy, boundary, edge, and negative cases)
🧱 Repository implemented with arrays (per assignment requirement)
🖥️ CLI (BankingApp) for quick manual runs
🛡️ Consistent error handling via domain exceptions:
    InvalidAccountException
    NegativeOrZeroAmountException
    InsufficientFundsException
    SameAccountTransferException
    DuplicateCustomerAccountException




🧩 Architecture
src/
├─ presentation/
│  └─ BankingApp.java               # CLI runner
├─ service/
│  ├─ BankService.java              # Service API
│  └─ BankServiceImpl.java          # Business logic + seeding
├─ repository/
│  ├─ BankRepository.java           # Repo contract
│  └─ BankRepositoryImpl.java       # Array-backed storage
├─ models/
│  ├─ Customer.java
│  ├─ Account.java
│  └─ Transaction.java              # + enum TransactionType
├─ exceptions/
│  ├─ InvalidAccountException.java
│  ├─ InsufficientFundsException.java
│  ├─ NegativeOrZeroAmountException.java
│  ├─ SameAccountTransferException.java
│  └─ DuplicateCustomerAccountException.java
└─ util/
   ├─ SampleDataUtil.java           # A001..A010 with initial balance = 0.0
   └─ ValidationUtil.java           # Validations + latest10 helper
