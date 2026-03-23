package com.vault.ui;

import com.vault.core.CryptoUtil;
import com.vault.db.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    private boolean isFirstTimeSetup = false;

    public LoginFrame() {
        checkIfFirstTime();
        setTitle(isFirstTimeSetup ? "SecureVault - Setup" : "SecureVault - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPasswordField masterField = new JPasswordField();
        JPasswordField panicField = new JPasswordField();
        
        panel.add(new JLabel(isFirstTimeSetup ? "Create Master Password:" : "Enter Password:"));
        panel.add(masterField);

        if (isFirstTimeSetup) {
            panel.add(new JLabel("Create Panic (Decoy) Password:"));
            panel.add(panicField);
        } else {
            panel.add(new JLabel("")); // Spacer
            panel.add(new JLabel("")); // Spacer
        }

        JButton actionBtn = new JButton(isFirstTimeSetup ? "Save & Setup" : "Unlock");
        actionBtn.addActionListener(e -> {
            if (isFirstTimeSetup) registerUser(new String(masterField.getPassword()), new String(panicField.getPassword()));
            else attemptLogin(new String(masterField.getPassword()));
        });

        panel.add(actionBtn);
        add(panel);
    }

    private void checkIfFirstTime() {
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) isFirstTimeSetup = true;
        } catch (Exception e) { e.printStackTrace(); }
    }
private void registerUser(String master, String panic) {
        if (master.isEmpty() || panic.isEmpty() || master.equals(panic)) {
            JOptionPane.showMessageDialog(this, "Passwords cannot be empty or identical!");
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (master_hash, panic_hash) VALUES (?, ?)");
            pstmt.setString(1, CryptoUtil.hashPassword(master));
            pstmt.setString(2, CryptoUtil.hashPassword(panic));
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Setup Complete! Restarting...");
            this.dispose();
            new LoginFrame().setVisible(true);
        } catch (Exception e) { 
            // THIS WILL NOW SHOW YOU THE EXACT ERROR IN A POPUP!
            JOptionPane.showMessageDialog(this, "DATABASE ERROR: " + e.getMessage());
            e.printStackTrace(); 
        }
    }
   
   
   




   


   
   
   
   
   
   

    private void attemptLogin(String inputPass) {
        String hashedInput = CryptoUtil.hashPassword(inputPass);
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery("SELECT master_hash, panic_hash FROM users LIMIT 1");
            if (rs.next()) {
                if (hashedInput.equals(rs.getString("master_hash"))) {
                    this.dispose();
                    new MainVaultFrame(false).setVisible(true);
                } else if (hashedInput.equals(rs.getString("panic_hash"))) {
                    this.dispose();
                    new MainVaultFrame(true).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect Password!");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}