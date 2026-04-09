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
    //ANMOL 'S
