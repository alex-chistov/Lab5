package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BookServiceGUI extends JFrame {
    private DBManager dbManager;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private String currentDbName;
    private String currentTableName;
    private String currentUsername;
    private String currentPassword;

    private JButton btnCreateDB;
    private JButton btnDropDB;
    private JButton btnCreateTable;
    private JButton btnClearTable;
    private JButton btnAddBook;
    private JButton btnUpdateBook;
    private JButton btnDeleteBook;

    private DefaultTableModel tableModel;

    public BookServiceGUI() {
        setTitle("Книжный сервис");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel loginPanel = createLoginPanel();
        JPanel operationsPanel = createOperationsPanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(operationsPanel, "operations");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");

        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblDb = new JLabel("База данных:");
        JTextField tfDb = new JTextField("bookservice", 15);

        JLabel lblRole = new JLabel("Роль:");
        JRadioButton rbAdmin = new JRadioButton("Admin");
        JRadioButton rbGuest = new JRadioButton("Guest");
        rbAdmin.setSelected(true);
        ButtonGroup bgRole = new ButtonGroup();
        bgRole.add(rbAdmin);
        bgRole.add(rbGuest);
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.add(rbAdmin);
        rolePanel.add(rbGuest);

        JLabel lblPassword = new JLabel("Пароль:");
        JPasswordField pfPassword = new JPasswordField(15);
        JButton btnLogin = new JButton("Войти");

        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblDb, gbc);
        gbc.gridx = 1;
        panel.add(tfDb, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblRole, gbc);
        gbc.gridx = 1;
        panel.add(rolePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblPassword, gbc);
        gbc.gridx = 1;
        panel.add(pfPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String dbName = tfDb.getText().trim();
            String username = rbAdmin.isSelected() ? "admin" : "guest";
            String password = new String(pfPassword.getPassword());
            try {
                currentDbName = dbName;
                currentUsername = username;
                currentPassword = password;
                dbManager = new DBManager(dbName, username, password);
                dbManager.getConnection().close();
                dbManager.initProcedures();
                JOptionPane.showMessageDialog(this, "Подключение успешно");
                if (currentUsername.equalsIgnoreCase("guest")) {
                    updateOperationsPanelForGuest();
                }
                cardLayout.show(mainPanel, "operations");
                refreshBookTable("");
            } catch (SQLException ex) {
                if (ex.getMessage().contains("does not exist")) {
                    int response = JOptionPane.showConfirmDialog(
                            this,
                            "Database \"" + dbName + "\" does not exist. Create it automatically?",
                            "Database Not Found",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (response == JOptionPane.YES_OPTION) {
                        try {
                            DBManager tempManager = new DBManager("postgres", username, password);
                            tempManager.createDatabase(dbName);
                            dbManager = new DBManager(dbName, username, password);
                            dbManager.getConnection().close();
                            dbManager.initProcedures();
                            currentDbName = dbName;
                            JOptionPane.showMessageDialog(this, "Database created successfully. Connection successful.");
                            if (currentUsername.equalsIgnoreCase("guest")) {
                                updateOperationsPanelForGuest();
                            }
                            cardLayout.show(mainPanel, "operations");
                            refreshBookTable("");
                        } catch (SQLException ex2) {
                            JOptionPane.showMessageDialog(this, "Error creating database: " + ex2.getMessage());
                        } catch (Exception ex2) {
                            JOptionPane.showMessageDialog(this, "Error initializing procedures: " + ex2.getMessage());
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error initializing procedures: " + ex.getMessage());
            }
        });

        return panel;
    }

    private void updateOperationsPanelForGuest() {
        if (btnCreateDB != null) btnCreateDB.setEnabled(false);
        if (btnDropDB != null) btnDropDB.setEnabled(false);
        if (btnCreateTable != null) btnCreateTable.setEnabled(false);
        if (btnClearTable != null) btnClearTable.setEnabled(false);
        if (btnAddBook != null) btnAddBook.setEnabled(false);
        if (btnUpdateBook != null) btnUpdateBook.setEnabled(false);
        if (btnDeleteBook != null) btnDeleteBook.setEnabled(false);
    }

    private void refreshBookTable(String titleFilter) {
        try {
            List<Book> books = dbManager.searchBookByTitle(currentTableName, titleFilter);
            tableModel.setRowCount(0);
            for (Book b : books) {
                tableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getPublisher(), b.getYear()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private JPanel createOperationsPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel dbPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());

        JLabel lblNewDB = new JLabel("Имя базы:");
        JTextField tfNewDB = new JTextField(currentDbName != null ? currentDbName : "bookservice", 10);
        JLabel lblNewTable = new JLabel("Имя таблицы:");
        JTextField tfNewTable = new JTextField("books", 10);
        currentTableName = tfNewTable.getText().trim();

        btnCreateDB = new JButton("Создать БД");
        btnDropDB = new JButton("Удалить БД");
        btnCreateTable = new JButton("Создать таблицу");
        btnClearTable = new JButton("Очистить таблицу");

        if (currentUsername != null && currentUsername.equalsIgnoreCase("guest")) {
            btnCreateDB.setEnabled(false);
            btnDropDB.setEnabled(false);
            btnCreateTable.setEnabled(false);
            btnClearTable.setEnabled(false);
        }

        topPanel.add(lblNewDB);
        topPanel.add(tfNewDB);
        topPanel.add(btnCreateDB);
        topPanel.add(btnDropDB);
        topPanel.add(lblNewTable);
        topPanel.add(tfNewTable);
        topPanel.add(btnCreateTable);
        topPanel.add(btnClearTable);

        dbPanel.add(topPanel, BorderLayout.NORTH);

        btnCreateDB.addActionListener(e -> {
            try {
                String newDb = tfNewDB.getText().trim();
                dbManager.createDatabase(newDb);
                dbManager = new DBManager(newDb, currentUsername, currentPassword);
                dbManager.initProcedures();
                currentDbName = newDb;
                JOptionPane.showMessageDialog(this, "База данных создана и процедуры инициализированы");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error initializing procedures: " + ex.getMessage());
            }
        });

        btnDropDB.addActionListener(e -> {
            try {
                String newDb = tfNewDB.getText().trim();
                dbManager.dropDatabase(newDb);
                JOptionPane.showMessageDialog(this, "База данных удалена");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnCreateTable.addActionListener(e -> {
            try {
                String tableName = tfNewTable.getText().trim();
                currentTableName = tableName;
                dbManager.createTable(tableName);
                JOptionPane.showMessageDialog(this, "Таблица создана");
                refreshBookTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnClearTable.addActionListener(e -> {
            try {
                String tableName = tfNewTable.getText().trim();
                currentTableName = tableName;
                dbManager.clearTable(tableName);
                JOptionPane.showMessageDialog(this, "Таблица очищена");
                refreshBookTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        JPanel bookPanel = new JPanel(new BorderLayout());
        JPanel topBookPanel = new JPanel(new FlowLayout());
        btnAddBook = new JButton("Добавить книгу");
        btnUpdateBook = new JButton("Обновить книгу");
        btnDeleteBook = new JButton("Удалить книгу по Title");
        JButton btnSearchBook = new JButton("Найти книгу по Title");
        topBookPanel.add(btnAddBook);
        topBookPanel.add(btnUpdateBook);
        topBookPanel.add(btnDeleteBook);
        topBookPanel.add(btnSearchBook);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Данные книги"));
        JTextField tfId = new JTextField();
        JTextField tfTitle = new JTextField();
        JTextField tfAuthor = new JTextField();
        JTextField tfPublisher = new JTextField();
        JTextField tfYear = new JTextField();
        inputPanel.add(new JLabel("ID (для обновления):"));
        inputPanel.add(tfId);
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(tfTitle);
        inputPanel.add(new JLabel("Author:"));
        inputPanel.add(tfAuthor);
        inputPanel.add(new JLabel("Publisher:"));
        inputPanel.add(tfPublisher);
        inputPanel.add(new JLabel("Year:"));
        inputPanel.add(tfYear);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Title", "Author", "Publisher", "Year"}, 0);
        JTable resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        bookPanel.add(topBookPanel, BorderLayout.NORTH);
        bookPanel.add(centerPanel, BorderLayout.CENTER);

        btnAddBook.addActionListener(e -> {
            try {
                String tableName = currentTableName;
                String title = tfTitle.getText().trim();
                String author = tfAuthor.getText().trim();
                String publisher = tfPublisher.getText().trim();
                int year = Integer.parseInt(tfYear.getText().trim());
                dbManager.addBook(tableName, title, author, publisher, year);
                JOptionPane.showMessageDialog(this, "Книга добавлена");
                refreshBookTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid year format");
            }
        });

        btnSearchBook.addActionListener(e -> {
            String title = tfTitle.getText().trim();
            refreshBookTable(title);
        });

        btnUpdateBook.addActionListener(e -> {
            try {
                String tableName = currentTableName;
                int id = Integer.parseInt(tfId.getText().trim());
                String title = tfTitle.getText().trim();
                String author = tfAuthor.getText().trim();
                String publisher = tfPublisher.getText().trim();
                int year = Integer.parseInt(tfYear.getText().trim());
                dbManager.updateBook(tableName, id, title, author, publisher, year);
                JOptionPane.showMessageDialog(this, "Книга обновлена");
                refreshBookTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid numeric format");
            }
        });

        btnDeleteBook.addActionListener(e -> {
            try {
                String tableName = currentTableName;
                String title = tfTitle.getText().trim();
                dbManager.deleteBookByTitle(tableName, title);
                JOptionPane.showMessageDialog(this, "Книга удалена");
                refreshBookTable("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        tabbedPane.addTab("База данных", dbPanel);
        tabbedPane.addTab("Книги", bookPanel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }
}
