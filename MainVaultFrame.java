package com.vault.ui;

import com.vault.core.CryptoUtil;
import com.vault.db.DatabaseConnection;
import java.awt.*;
import java.io.*;
import java.security.Key;
import java.sql.*;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainVaultFrame extends JFrame {
    private boolean isPanicMode;
    private DefaultTableModel tableModel;
    private JTable passwordTable;
    
    private DefaultTableModel fileTableModel;
    private JTable fileTable;
    
    private JLabel executionTimeLabel;

    public MainVaultFrame(boolean isPanicMode) {
        this.isPanicMode = isPanicMode;

        setTitle("SecureVault - Dashboard " + (isPanicMode ? "[DECOY MODE]" : "[SECURE]"));
        setSize(850, 600); // Made slightly wider to fit the new text
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        new File("VaultFiles").mkdir(); 

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("🔑 Passwords", createPasswordPanel());
        tabbedPane.addTab("📁 File Locker", createFilePanel());
        
        if (!isPanicMode) {
            tabbedPane.addTab("⚙️ Settings", createSettingsPanel());
        }

        add(tabbedPane);
        loadPasswords(); 
    }

    // ==========================================
    // MODULE 1: THE PASSWORD MANAGER
    // ==========================================
    // ANANYA 's
   

    // ==========================================
    // MODULE 2: THE FILE LOCKER (FULL METRICS UPGRADE)
    // ==========================================
    // BOOMIKA 's

    // ==========================================
    // MODULE 3: SETTINGS
    // ==========================================
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton resetBtn = new JButton("⚠️ Reset Master & Panic Passwords");
        resetBtn.setBackground(Color.RED);
        resetBtn.setForeground(Color.WHITE);

        resetBtn.addActionListener(e -> resetPasswords());
        panel.add(resetBtn);
        return panel;
    }

    private void resetPasswords() {
        JPasswordField oldMasterField = new JPasswordField();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Enter CURRENT Master Password:", oldMasterField}, "Security Check", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT master_hash FROM users LIMIT 1");
            
            if (rs.next() && CryptoUtil.hashPassword(new String(oldMasterField.getPassword())).equals(rs.getString("master_hash"))) {
                JPasswordField newMasterField = new JPasswordField();
                JPasswordField newPanicField = new JPasswordField();
                if (JOptionPane.showConfirmDialog(this, new Object[]{"New Master Password:", newMasterField, "New Panic Password:", newPanicField}, "Set New Passwords", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    String newMaster = new String(newMasterField.getPassword());
                    String newPanic = new String(newPanicField.getPassword());

                    if (newMaster.isEmpty() || newPanic.isEmpty() || newMaster.equals(newPanic)) {
                        JOptionPane.showMessageDialog(this, "Invalid Passwords!");
                        return;
                    }

                    PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET master_hash=?, panic_hash=?");
                    pstmt.setString(1, CryptoUtil.hashPassword(newMaster));
                    pstmt.setString(2, CryptoUtil.hashPassword(newPanic));
                    pstmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Passwords updated! Logging out...");
                    this.dispose(); 
                    new LoginFrame().setVisible(true); 
                }
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect Password!");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
