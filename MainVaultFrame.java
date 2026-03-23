package com.vault.ui;

import com.vault.db.DatabaseConnection;
import com.vault.core.CryptoUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class MainVaultFrame extends JFrame {
    private boolean isPanicMode;
    private DefaultTableModel tableModel;
    private JTable passwordTable;
    
    private DefaultTableModel fileTableModel;
    private JTable fileTable;

    public MainVaultFrame(boolean isPanicMode) {
        this.isPanicMode = isPanicMode;

        setTitle("SecureVault - Dashboard " + (isPanicMode ? "[DECOY MODE]" : "[SECURE]"));
        setSize(800, 600);
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
    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"Website/App", "Username", "Password"};
        tableModel = new DefaultTableModel(columns, 0);
        passwordTable = new JTable(tableModel);
        panel.add(new JScrollPane(passwordTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("+ Add");
        JButton editBtn = new JButton("✏️ Edit");
        JButton deleteBtn = new JButton("🗑️ Delete");
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddPasswordDialog());
        editBtn.addActionListener(e -> showEditPasswordDialog());
        deleteBtn.addActionListener(e -> deleteSelectedPassword());

        return panel;
    }

    private void loadPasswords() {
        tableModel.setRowCount(0); 
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT website, username, password FROM passwords WHERE is_decoy = ?");
            pstmt.setBoolean(1, isPanicMode);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("website"));
                row.add(rs.getString("username"));
                row.add(rs.getString("password"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAddPasswordDialog() {
        JTextField siteField = new JTextField();
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] message = {"Website:", siteField, "Username:", userField, "Password:", passField};

        if (JOptionPane.showConfirmDialog(this, message, "Add Credential", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO passwords (website, username, password, is_decoy) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, siteField.getText());
                pstmt.setString(2, userField.getText());
                pstmt.setString(3, new String(passField.getPassword()));
                pstmt.setBoolean(4, isPanicMode); 
                pstmt.executeUpdate();
                loadPasswords(); 
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void showEditPasswordDialog() {
        int selectedRow = passwordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a password to edit!");
            return;
        }

        String oldSite = tableModel.getValueAt(selectedRow, 0).toString();
        String oldUser = tableModel.getValueAt(selectedRow, 1).toString();
        String oldPass = tableModel.getValueAt(selectedRow, 2).toString();

        JTextField siteField = new JTextField(oldSite);
        JTextField userField = new JTextField(oldUser);
        JTextField passField = new JTextField(oldPass); 

        Object[] message = {"Website:", siteField, "Username:", userField, "Password:", passField};

        if (JOptionPane.showConfirmDialog(this, message, "Edit Credential", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("UPDATE passwords SET website=?, username=?, password=? WHERE website=? AND username=? AND is_decoy=?");
                pstmt.setString(1, siteField.getText());
                pstmt.setString(2, userField.getText());
                pstmt.setString(3, passField.getText());
                pstmt.setString(4, oldSite);
                pstmt.setString(5, oldUser);
                pstmt.setBoolean(6, isPanicMode);
                pstmt.executeUpdate();
                loadPasswords(); 
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void deleteSelectedPassword() {
        int selectedRow = passwordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a password to delete!");
            return;
        }

        String site = tableModel.getValueAt(selectedRow, 0).toString();
        String user = tableModel.getValueAt(selectedRow, 1).toString();

        if (JOptionPane.showConfirmDialog(this, "Delete " + site + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM passwords WHERE website=? AND username=? AND is_decoy=?");
                pstmt.setString(1, site);
                pstmt.setString(2, user);
                pstmt.setBoolean(3, isPanicMode);
                pstmt.executeUpdate();
                loadPasswords();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // MODULE 2: THE FILE LOCKER (NIO ATOMIC FIX)
    // ==========================================
    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Vault Files", "Status"};
        fileTableModel = new DefaultTableModel(columns, 0);
        fileTable = new JTable(fileTableModel);
        panel.add(new JScrollPane(fileTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton importBtn = new JButton("➕ Import & Lock New File");
        JButton toggleBtn = new JButton("🔄 Toggle Lock / Unlock");

        buttonPanel.add(importBtn);
        buttonPanel.add(toggleBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                processFile(true, chooser.getSelectedFile());
            }
        });

        toggleBtn.addActionListener(e -> {
            int selectedRow = fileTable.getSelectedRow();
            if (selectedRow != -1) {
                String fileName = fileTableModel.getValueAt(selectedRow, 0).toString();
                File fileToProcess = new File("VaultFiles/" + fileName);
                boolean isCurrentlyLocked = fileName.endsWith(".locked");
                processFile(!isCurrentlyLocked, fileToProcess); 
            } else {
                JOptionPane.showMessageDialog(this, "Please select a file from the table first!");
            }
        });

        loadFileTable(); 
        return panel;
    }

    private void loadFileTable() {
        fileTableModel.setRowCount(0);
        File folder = new File("VaultFiles");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String status = file.getName().endsWith(".locked") ? "🔒 Encrypted" : "🔓 Unlocked";
                    fileTableModel.addRow(new Object[]{file.getName(), status});
                }
            }
        }
    }

    private void processFile(boolean encrypting, File inputFile) {
        if (inputFile == null || !inputFile.exists()) return;

        String newName = encrypting ? inputFile.getName() + ".locked" : inputFile.getName().replace(".locked", "");
        File outputFile = new File("VaultFiles/" + newName);

        try {
            String keyString = "SecureVaultKey12"; 
            Key secretKey = new SecretKeySpec(keyString.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(encrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey);

            // The OneDrive Fix: We load the whole file instantly instead of using breakable streams
            byte[] inputBytes = java.nio.file.Files.readAllBytes(inputFile.toPath());
            byte[] outputBytes = cipher.doFinal(inputBytes);
            java.nio.file.Files.write(outputFile.toPath(), outputBytes);

            inputFile.delete(); 

            JOptionPane.showMessageDialog(this, "File successfully " + (encrypting ? "Locked" : "Unlocked") + "!");
            loadFileTable(); 
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "File Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

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