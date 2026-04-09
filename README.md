🔒 SecureVault
Project Description
An offline, local desktop application built to securely store passwords and encrypt sensitive files. It features a dual-vault system with a built-in "Panic Mode" to protect against forced access, ensuring complete privacy with zero cloud dependency.

What We Used
Language: Core Java (JDK 17+)

GUI: Java Swing

Database: SQLite & JDBC

File Processing: Java NIO (Atomic Operations)

Security/Crypto: javax.crypto (AES-128 Encryption) & java.security (SHA-256 Hashing)

How It Works
Authentication: Master and Panic passwords are one-way hashed using SHA-256.

Panic Mode: Entering the Decoy/Panic password loads a secondary, empty database view to protect real data from forced exposure.

Password Manager: Uses SQL PreparedStatement to securely CRUD credentials into a local .db file.

File Locker: Uses AES-128 to atomically scramble files in RAM. Once encrypted into a .locked file, the original unprotected file is permanently deleted from the disk. Features live throughput and execution time metrics.

How to Run
1. Prerequisites

Install Java Development Kit (JDK) 17 or higher.

Download the SQLite JDBC driver (sqlite-jdbc.jar) and place it in your project's lib/ folder.

2. Compile
Open your terminal in the project root folder and run:

Bash
javac -d bin -cp "lib/sqlite-jdbc.jar" src/com/vault/*/*.java
3. Run

Bash
java -cp "bin;lib/sqlite-jdbc.jar" com.vault.main.App
