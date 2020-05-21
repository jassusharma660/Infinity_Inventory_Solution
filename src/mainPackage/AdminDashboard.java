/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainPackage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.codec.language.Metaphone;

/**
 *
 * @author Jassu Sharma
 */
public class AdminDashboard extends javax.swing.JFrame {

    int mouseX, mouseY;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
    int taskBarSize = scnMax.bottom;

    //For notification
    JPanel[] notificationPanels, peoplePanels;
    int currentIinNotification = 0, currentIinPeople = 0;
    String currentTab = "dashboard";
    String userid, fullname, gender, role;

    /**
     * Creates new form Dashboard
     */
    DefaultTableModel modelCategory, modelTransaction;
    StockTableModel modelStocks;

    public AdminDashboard() {
        screenSize.height -= taskBarSize - getHeight();
        screenSize.width -= getWidth();
        initComponents();
        modelCategory = (DefaultTableModel) categoryStatTable.getModel();
        modelTransaction = (DefaultTableModel) transactionStatTable.getModel();
        /*
        
            THIS IS JUST A TEST CASE 
            REMOVE AFTER DEV PHASE IS OVER
        
         */
        //session("jassusharma", "Jassu sharma", "Male", "ADMIN");
    }

    public void session(String u, String f, String g, String r) {
        int adminC = 0, billC = 0, unconfirmedC = 0;
        userid = u;
        fullname = f;
        gender = g;
        role = r;

        fullnameLabel.setText(fullname);
        roleLabel.setText("(" + role + ")");
        savedFullname.setText(fullname);
        savedGender.setText(gender);

        //Prepare the dashboard users stats
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            Statement s = con.createStatement();
            String sql = "SELECT userid,fullname,gender,role FROM userDBForJavaApp";

            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                switch (rs.getString("role")) {
                    case "ADMIN":
                        adminC++;
                        break;
                    case "BILL":
                        billC++;
                        break;
                    case "NO":
                        unconfirmedC++;
                        break;
                }
            }
            adminCount.setText("" + adminC);
            billCount.setText("" + billC);
            unconfirmedCount.setText("" + unconfirmedC);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured!" + e);
        }

        showCategoryDataInDashboard();
        showProductsDataInStocks(null);
        showTransactionStatInStocks();
    }

    private ArrayList<Item> itemList() {
        ArrayList<Item> itemsList= new ArrayList<Item>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            Statement s = con.createStatement();
            String sql = "SELECT * FROM productDBForJavaApp ORDER BY quantity ASC";
            ResultSet rs = s.executeQuery(sql);
            Item items;
            while(rs.next()) {
                items = new Item(rs.getString("lastModified"), rs.getString("pId"), rs.getString("pName"), rs.getString("pDesc"), rs.getInt("quantity"), rs.getDouble("sellingPrice"), rs.getBytes("pImage"));
                itemsList.add(items);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured!" + e);
        }
        return itemsList;
    }
    
    private void showTransactionStatInStocks() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB","root","");
            PreparedStatement p = con.prepareStatement("SELECT * FROM billDBForJavaApp ORDER BY billId DESC");
            PreparedStatement p1 = con.prepareStatement("SELECT pId,quantity,lastModified,SUM(sellingPrice) AS totalSellingPrice FROM billItemDBForJavaApp WHERE billId = ?");
            PreparedStatement p2 = con.prepareStatement("SELECT SUM(actualPrice * ?) AS totalPurchasePrice FROM productDBForJavaApp WHERE pId = ?");
            ResultSet rs = p.executeQuery();
            ResultSet rs1, rs2;
            double netIncome = 0.0, netCost = 0.0, profit = 0.0;
            while(rs.next()) {
                p1.setString(1,rs.getString("billId"));
                rs1 = p1.executeQuery();
                while(rs1.next()) {
                    p2.setString(1, rs1.getString("quantity"));
                    p2.setString(2, rs1.getString("pId"));
                    rs2 = p2.executeQuery();
                    if(rs2.next()) {
                        netIncome += rs2.getDouble("totalPurchasePrice");
                        netCost += rs1.getDouble("totalSellingPrice");
                        if(netIncome > netCost)
                            profit = ((netIncome - netCost)/netIncome) * 100;
                        else
                            profit = ((netCost - netIncome)/netCost) * 100;
                        profit = Math.round(profit * 100.0) / 100.0;

                        modelTransaction.insertRow(modelTransaction.getRowCount(), new Object[]{rs.getString("billId"),netIncome,profit+"%",rs.getString("userid"),rs1.getString("lastModified")});
                    }
                }                
            }
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured"+e);
        }
    }
    
    private void showProductsDataInStocks(ArrayList <Item> list) {
        if(list == null) list = itemList();
        String[] columnName = {"Modified Date","Code","Name","Description","In Stock","Selling Price","Image"};
        Object[][] rows = new Object[list.size()][7];
        for(int i = 0; i < list.size(); i++) {
            rows[i][0] = list.get(i).dateModified;
            rows[i][1] = list.get(i).code;
            rows[i][2] = list.get(i).name;
            rows[i][3] = list.get(i).description;
            rows[i][4] = list.get(i).inStock;
            rows[i][5] = list.get(i).sellingPrice;
            if(list.get(i).image != null) {
                /*
                    Re-sizing acc. to ratio
                */
                byte[] img = list.get(i).image;
                double imgHeight = new ImageIcon(img).getImage().getHeight(this),
                imgWidth = new ImageIcon(img).getImage().getWidth(this),
                labelHeight = 100,
                labelWidth = 100;
                int newHeight = 1, newWidth = 1;
                if(imgHeight < labelHeight) {
                    newHeight = (int) (imgHeight * (labelWidth / imgWidth)) / 2;
                    newWidth = (int) labelWidth / 2;
                }
                else {
                    newWidth = (int) (imgWidth * (labelHeight / imgHeight)) / 2;
                    newHeight = (int) labelHeight / 2;
                }
                
                ImageIcon image = new ImageIcon(new ImageIcon(img).getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
                rows[i][6] = image;
            }
            else {
                rows[i][6] = null;
            }
        }
        modelStocks = new StockTableModel(rows, columnName);
        stockTable.setModel(modelStocks);
        stockTable.setRowHeight(100);
        stockTable.getColumnModel().getColumn(6).setPreferredWidth(100);
    }
    
    private void showCategoryDataInDashboard() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            Statement s = con.createStatement();
            String sqlCategory = "SELECT * FROM categoryDBForJavaApp";
            String sqlProduct = null;
            ResultSet rs = s.executeQuery(sqlCategory);

            Statement s1 = con.createStatement();
            ResultSet rs1;

            String categoryId, categoryDesc = null;

            while (rs.next()) {
                categoryId = rs.getString("categoryId");
                categoryDesc = rs.getString("categoryDesc");

                sqlProduct = "SELECT COUNT(*) FROM productDBForJavaApp WHERE categoryId = '" + categoryId + "'";
                rs1 = s1.executeQuery(sqlProduct);

                while (rs1.next()) {
                    modelCategory.insertRow(modelCategory.getRowCount(), new Object[]{categoryId, categoryDesc, rs1.getString(1)});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured!" + e);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        sideBar = new javax.swing.JPanel();
        fullnameLabel = new javax.swing.JLabel();
        roleLabel = new javax.swing.JLabel();
        notificationIcon = new javax.swing.JLabel();
        peopleIcon = new javax.swing.JLabel();
        settingIcon = new javax.swing.JLabel();
        stocksOption = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        dashboardOption = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        aboutOption = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        logoutNote = new javax.swing.JPanel();
        logoutNote.setVisible(false);
        jLabel7 = new javax.swing.JLabel();
        logoutOption = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        titleBar = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        close = new javax.swing.JLabel();
        minimise = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        dashboardPanel = new javax.swing.JPanel();
        tabContentLabel2 = new javax.swing.JLabel();
        staffStatLabel = new javax.swing.JLabel();
        staffStat = new javax.swing.JPanel();
        adminCount = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        billCount = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        unconfirmedCount = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        categoriesStatLabel = new javax.swing.JLabel();
        categoriesStat = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        categoryStatTable = new javax.swing.JTable();
        categoriesStatLabel1 = new javax.swing.JLabel();
        categoriesStat1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        transactionStatTable = new javax.swing.JTable();
        stocksPanel = new javax.swing.JPanel();
        tabContentLabel3 = new javax.swing.JLabel();
        addProduct = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        addCategory = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        stockTable = new javax.swing.JTable();
        reloadStock = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        searchProduct = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        searchButton = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        editBtn = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        deleteBtn = new javax.swing.JLabel();
        aboutPanel = new javax.swing.JPanel();
        tabContentLabel4 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        peoplePanel = new javax.swing.JPanel();
        tabContentLabel7 = new javax.swing.JLabel();
        peopleArea = new javax.swing.JPanel();
        settingPanel = new javax.swing.JPanel();
        tabContentLabel8 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        genderMaleField = new javax.swing.JRadioButton();
        genderFemaleField = new javax.swing.JRadioButton();
        fullnameField = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        passwordField = new javax.swing.JPasswordField();
        saveInfo = new javax.swing.JLabel();
        saveError = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        savedGender = new javax.swing.JLabel();
        savedFullname = new javax.swing.JLabel();
        label51 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        notificationPanel = new javax.swing.JPanel();
        tabContentLabel10 = new javax.swing.JLabel();
        notificationArea = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Infinity By Jassu Sharma | Dashboard");
        setUndecorated(true);
        setPreferredSize(screenSize);

        mainPanel.setBackground(new java.awt.Color(57, 56, 54));

        sideBar.setBackground(new java.awt.Color(57, 56, 54));
        sideBar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        fullnameLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        fullnameLabel.setForeground(new java.awt.Color(255, 255, 255));
        fullnameLabel.setText("Full name");
        sideBar.add(fullnameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 131, 17));

        roleLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        roleLabel.setForeground(new java.awt.Color(153, 153, 153));
        roleLabel.setText("(ROLE)");
        sideBar.add(roleLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 81, -1));

        notificationIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        notificationIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/notification.png"))); // NOI18N
        notificationIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        notificationIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                notificationIconMouseClicked(evt);
            }
        });
        sideBar.add(notificationIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 90, -1, -1));

        peopleIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        peopleIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/people.png"))); // NOI18N
        peopleIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        peopleIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                peopleIconMouseClicked(evt);
            }
        });
        sideBar.add(peopleIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        settingIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        settingIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settings.png"))); // NOI18N
        settingIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settingIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingIconMouseClicked(evt);
            }
        });
        sideBar.add(settingIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 90, -1, -1));

        stocksOption.setBackground(new java.awt.Color(57, 56, 54));
        stocksOption.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        stocksOption.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stocksOptionMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                stocksOptionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                stocksOptionMouseExited(evt);
            }
        });

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stock.png"))); // NOI18N
        jLabel2.setText("Stocks");
        jLabel2.setIconTextGap(10);

        javax.swing.GroupLayout stocksOptionLayout = new javax.swing.GroupLayout(stocksOption);
        stocksOption.setLayout(stocksOptionLayout);
        stocksOptionLayout.setHorizontalGroup(
            stocksOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stocksOptionLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        stocksOptionLayout.setVerticalGroup(
            stocksOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        sideBar.add(stocksOption, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 310, 210, 50));

        dashboardOption.setBackground(new java.awt.Color(57, 56, 54));
        dashboardOption.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dashboardOption.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dashboardOptionMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dashboardOptionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dashboardOptionMouseExited(evt);
            }
        });

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(153, 153, 153));
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dashboard.png"))); // NOI18N
        jLabel3.setText("Dashboard");
        jLabel3.setIconTextGap(10);

        javax.swing.GroupLayout dashboardOptionLayout = new javax.swing.GroupLayout(dashboardOption);
        dashboardOption.setLayout(dashboardOptionLayout);
        dashboardOptionLayout.setHorizontalGroup(
            dashboardOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardOptionLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        dashboardOptionLayout.setVerticalGroup(
            dashboardOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        sideBar.add(dashboardOption, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 230, 210, 50));

        aboutOption.setBackground(new java.awt.Color(57, 56, 54));
        aboutOption.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        aboutOption.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                aboutOptionMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                aboutOptionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                aboutOptionMouseExited(evt);
            }
        });

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(153, 153, 153));
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about.png"))); // NOI18N
        jLabel6.setText("About");
        jLabel6.setIconTextGap(10);

        javax.swing.GroupLayout aboutOptionLayout = new javax.swing.GroupLayout(aboutOption);
        aboutOption.setLayout(aboutOptionLayout);
        aboutOptionLayout.setHorizontalGroup(
            aboutOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutOptionLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        aboutOptionLayout.setVerticalGroup(
            aboutOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        sideBar.add(aboutOption, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 390, -1, 50));

        logoutNote.setBackground(new java.awt.Color(57, 56, 54));

        jLabel7.setBackground(new java.awt.Color(57, 56, 54));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logoutNote.png"))); // NOI18N

        javax.swing.GroupLayout logoutNoteLayout = new javax.swing.GroupLayout(logoutNote);
        logoutNote.setLayout(logoutNoteLayout);
        logoutNoteLayout.setHorizontalGroup(
            logoutNoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logoutNoteLayout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        logoutNoteLayout.setVerticalGroup(
            logoutNoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logoutNoteLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        sideBar.add(logoutNote, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 560, 200, 70));

        logoutOption.setBackground(new java.awt.Color(57, 56, 54));
        logoutOption.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoutOption.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutOptionMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutOptionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutOptionMouseExited(evt);
            }
        });

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(153, 153, 153));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logout.png"))); // NOI18N
        jLabel5.setText("LOGOUT");
        jLabel5.setIconTextGap(10);

        javax.swing.GroupLayout logoutOptionLayout = new javax.swing.GroupLayout(logoutOption);
        logoutOption.setLayout(logoutOptionLayout);
        logoutOptionLayout.setHorizontalGroup(
            logoutOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logoutOptionLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );
        logoutOptionLayout.setVerticalGroup(
            logoutOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logoutOptionLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        sideBar.add(logoutOption, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, -1, -1));

        titleBar.setBackground(new java.awt.Color(41, 41, 41));
        titleBar.setAlignmentX(0.0F);
        titleBar.setAlignmentY(0.0F);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(50, 179, 96));
        jLabel1.setText("Infinity Business Solution Tool");

        close.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeMouseExited(evt);
            }
        });

        minimise.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        minimise.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/minimise.png"))); // NOI18N
        minimise.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                minimiseMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                minimiseMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                minimiseMouseExited(evt);
            }
        });

        javax.swing.GroupLayout titleBarLayout = new javax.swing.GroupLayout(titleBar);
        titleBar.setLayout(titleBarLayout);
        titleBarLayout.setHorizontalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, titleBarLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(minimise)
                .addGap(0, 0, 0)
                .addComponent(close)
                .addGap(0, 0, 0))
        );
        titleBarLayout.setVerticalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(close, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(minimise, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        content.setBackground(new java.awt.Color(255, 255, 255));
        content.setLayout(new java.awt.CardLayout());

        dashboardPanel.setBackground(new java.awt.Color(255, 255, 255));

        tabContentLabel2.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel2.setText("DASHBOARD");

        staffStatLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        staffStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        staffStatLabel.setText("STAFF STATISTICS");

        staffStat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        adminCount.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        adminCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        adminCount.setText("0");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText("ADMIN(S)");

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel10.setText("No. of admins");

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("BILLING");

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("No. of people in billing staff");

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        billCount.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        billCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        billCount.setText("0");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("UNCONFIRMED");

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("No. of request unconfirmed");

        jPanel4.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        unconfirmedCount.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        unconfirmedCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        unconfirmedCount.setText("0");

        jPanel11.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        jPanel14.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout staffStatLayout = new javax.swing.GroupLayout(staffStat);
        staffStat.setLayout(staffStatLayout);
        staffStatLayout.setHorizontalGroup(
            staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(staffStatLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(adminCount, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addComponent(billCount, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(unconfirmedCount, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(56, 56, 56))
        );
        staffStatLayout.setVerticalGroup(
            staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, staffStatLayout.createSequentialGroup()
                .addContainerGap(52, Short.MAX_VALUE)
                .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(staffStatLayout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel15))
                            .addComponent(unconfirmedCount, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(staffStatLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel12))
                            .addComponent(billCount, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(staffStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(staffStatLayout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel10))
                            .addComponent(adminCount, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(47, 47, 47))
        );

        categoriesStatLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        categoriesStatLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        categoriesStatLabel.setText("CATALOGUE  BY CATEGORIES");

        categoriesStat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        categoryStatTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CategoryID", "CategoryName", "Products"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(categoryStatTable);

        javax.swing.GroupLayout categoriesStatLayout = new javax.swing.GroupLayout(categoriesStat);
        categoriesStat.setLayout(categoriesStatLayout);
        categoriesStatLayout.setHorizontalGroup(
            categoriesStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
        );
        categoriesStatLayout.setVerticalGroup(
            categoriesStatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
        );

        categoriesStatLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        categoriesStatLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        categoriesStatLabel1.setText("LIST OF TRANSACTION");

        categoriesStat1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        transactionStatTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "BillId", "Total Amount", "Profit", "Billed By", "Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(transactionStatTable);

        javax.swing.GroupLayout categoriesStat1Layout = new javax.swing.GroupLayout(categoriesStat1);
        categoriesStat1.setLayout(categoriesStat1Layout);
        categoriesStat1Layout.setHorizontalGroup(
            categoriesStat1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
        );
        categoriesStat1Layout.setVerticalGroup(
            categoriesStat1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout dashboardPanelLayout = new javax.swing.GroupLayout(dashboardPanel);
        dashboardPanel.setLayout(dashboardPanelLayout);
        dashboardPanelLayout.setHorizontalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardPanelLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(dashboardPanelLayout.createSequentialGroup()
                        .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(categoriesStatLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(categoriesStat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(categoriesStatLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(categoriesStat, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, dashboardPanelLayout.createSequentialGroup()
                        .addComponent(tabContentLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(staffStatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(staffStat, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(201, Short.MAX_VALUE))
        );
        dashboardPanelLayout.setVerticalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardPanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(tabContentLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(dashboardPanelLayout.createSequentialGroup()
                        .addComponent(staffStatLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(staffStat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(categoriesStatLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(categoriesStat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dashboardPanelLayout.createSequentialGroup()
                        .addComponent(categoriesStatLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(categoriesStat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(93, Short.MAX_VALUE))
        );

        content.add(dashboardPanel, "card2");

        stocksPanel.setBackground(new java.awt.Color(255, 255, 255));

        tabContentLabel3.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel3.setText("STOCKS");

        addProduct.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        addProduct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addProductMouseClicked(evt);
            }
        });

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("ADD PRODUCT");

        javax.swing.GroupLayout addProductLayout = new javax.swing.GroupLayout(addProduct);
        addProduct.setLayout(addProductLayout);
        addProductLayout.setHorizontalGroup(
            addProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addProductLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .addContainerGap())
        );
        addProductLayout.setVerticalGroup(
            addProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        addCategory.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        addCategory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addCategoryMouseClicked(evt);
            }
        });

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("ADD CATEGORY");

        javax.swing.GroupLayout addCategoryLayout = new javax.swing.GroupLayout(addCategory);
        addCategory.setLayout(addCategoryLayout);
        addCategoryLayout.setHorizontalGroup(
            addCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addCategoryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .addContainerGap())
        );
        addCategoryLayout.setVerticalGroup(
            addCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        stockTable.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        stockTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Modified Date", "Code", "Name", "Description", "In Stock", "Selling Price", "Image"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        stockTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(stockTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        );

        reloadStock.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        reloadStock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reloadStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reload.png"))); // NOI18N
        reloadStock.setText("Reload");
        reloadStock.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        reloadStock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reloadStockMouseClicked(evt);
            }
        });
        reloadStock.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                reloadStockKeyPressed(evt);
            }
        });

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel21.setText("Search Category");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(102, 102, 102));
        jLabel23.setText("Max 40 chars");

        jPanel18.setBackground(new java.awt.Color(255, 255, 255));
        jPanel18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        searchProduct.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        searchProduct.setForeground(new java.awt.Color(51, 51, 51));
        searchProduct.setBorder(null);
        searchProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchProductActionPerformed(evt);
            }
        });
        searchProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                searchProductKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        jPanel19.setBackground(new java.awt.Color(204, 204, 204));

        searchButton.setBackground(new java.awt.Color(0, 0, 0));
        searchButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        searchButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        searchButton.setText("Search");
        searchButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(160, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 153, 102), 2));

        editBtn.setBackground(new java.awt.Color(0, 153, 102));
        editBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        editBtn.setForeground(new java.awt.Color(0, 153, 102));
        editBtn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        editBtn.setText("EDIT");
        editBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editBtnMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 0, 0), 2));

        deleteBtn.setBackground(new java.awt.Color(0, 0, 0));
        deleteBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        deleteBtn.setForeground(new java.awt.Color(204, 0, 0));
        deleteBtn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        deleteBtn.setText("DELETE");
        deleteBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteBtnMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(deleteBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(deleteBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout stocksPanelLayout = new javax.swing.GroupLayout(stocksPanel);
        stocksPanel.setLayout(stocksPanelLayout);
        stocksPanelLayout.setHorizontalGroup(
            stocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stocksPanelLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addGroup(stocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stocksPanelLayout.createSequentialGroup()
                        .addGroup(stocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(stocksPanelLayout.createSequentialGroup()
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(stocksPanelLayout.createSequentialGroup()
                                .addComponent(tabContentLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(236, 236, 236)
                                .addComponent(addProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(addCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(reloadStock, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)))
                .addContainerGap(95, Short.MAX_VALUE))
        );
        stocksPanelLayout.setVerticalGroup(
            stocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stocksPanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(stocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tabContentLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(stocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(addProduct, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addCategory, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(reloadStock, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(stocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        content.add(stocksPanel, "card4");

        aboutPanel.setBackground(new java.awt.Color(255, 255, 255));

        tabContentLabel4.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel4.setText("ABOUT");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setText("This software provides the solution for small businesses by solving the billing problem. ");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setText("If you were doing everything manually before then it is a good start.");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setText("Just set up infinity(The software) on a cash counter computer and manager all your inventory. ");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setText("What can you do?");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel17.setText("You can manage your inventory, sales, stocks and your staff.");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("Code managed and developed by Jassu Sharma");

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/github.png"))); // NOI18N
        jLabel18.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goToGithub(evt);
            }
        });

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/linkedin.png"))); // NOI18N
        jLabel29.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel29.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goToLinkedin(evt);
            }
        });

        jLabel31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/jassusharma.png"))); // NOI18N

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel8)
                        .addComponent(tabContentLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel16)
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addComponent(jLabel31)
                        .addGap(33, 33, 33)
                        .addComponent(jLabel18)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel29)))
                .addContainerGap(2576, Short.MAX_VALUE))
        );
        aboutPanelLayout.setVerticalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(tabContentLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGap(36, 36, 36)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addGap(79, 79, 79)
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel29)
                            .addComponent(jLabel18))))
                .addGap(27, 27, 27)
                .addComponent(jLabel19)
                .addContainerGap(2959, Short.MAX_VALUE))
        );

        jLabel18.getAccessibleContext().setAccessibleName("");

        content.add(aboutPanel, "card5");

        peoplePanel.setBackground(new java.awt.Color(41, 41, 41));

        tabContentLabel7.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel7.setForeground(new java.awt.Color(50, 179, 96));
        tabContentLabel7.setText("Users");

        peopleArea.setBackground(new java.awt.Color(41, 41, 41));
        peopleArea.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        javax.swing.GroupLayout peoplePanelLayout = new javax.swing.GroupLayout(peoplePanel);
        peoplePanel.setLayout(peoplePanelLayout);
        peoplePanelLayout.setHorizontalGroup(
            peoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peoplePanelLayout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addGroup(peoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(peopleArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(peoplePanelLayout.createSequentialGroup()
                        .addComponent(tabContentLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 855, Short.MAX_VALUE)))
                .addContainerGap())
        );
        peoplePanelLayout.setVerticalGroup(
            peoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(peoplePanelLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(tabContentLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(peopleArea, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        content.add(peoplePanel, "card5");

        settingPanel.setBackground(new java.awt.Color(41, 41, 41));

        tabContentLabel8.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel8.setForeground(new java.awt.Color(50, 179, 96));
        tabContentLabel8.setText("Settings");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Full Name");

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 317, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("Password");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("Gender");

        genderMaleField.setBackground(new java.awt.Color(41, 41, 41));
        buttonGroup1.add(genderMaleField);
        genderMaleField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        genderMaleField.setForeground(new java.awt.Color(255, 255, 255));
        genderMaleField.setText("Male");
        genderMaleField.setIconTextGap(7);

        genderFemaleField.setBackground(new java.awt.Color(41, 41, 41));
        buttonGroup1.add(genderFemaleField);
        genderFemaleField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        genderFemaleField.setForeground(new java.awt.Color(255, 255, 255));
        genderFemaleField.setText("Female");
        genderFemaleField.setIconTextGap(7);

        fullnameField.setBackground(new java.awt.Color(41, 41, 41));
        fullnameField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        fullnameField.setForeground(new java.awt.Color(50, 179, 96));
        fullnameField.setBorder(null);
        fullnameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullnameFieldActionPerformed(evt);
            }
        });

        jPanel15.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 317, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );

        passwordField.setBackground(new java.awt.Color(41, 41, 41));
        passwordField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        passwordField.setForeground(new java.awt.Color(50, 179, 96));
        passwordField.setBorder(null);

        saveInfo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        saveInfo.setForeground(new java.awt.Color(255, 255, 255));
        saveInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        saveInfo.setText("Save");
        saveInfo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        saveInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        saveInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveInfoMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveInfoMouseEntered(evt);
            }
        });

        saveError.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        saveError.setForeground(new java.awt.Color(255, 255, 255));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(50, 179, 96));
        jLabel26.setText("Note : Only fill the info you want to change.");

        savedGender.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        savedGender.setForeground(new java.awt.Color(102, 102, 102));
        savedGender.setText("Gender");

        savedFullname.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        savedFullname.setForeground(new java.awt.Color(102, 102, 102));
        savedFullname.setText("Fullname");

        label51.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        label51.setForeground(new java.awt.Color(102, 102, 102));
        label51.setText("*********");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel28.setText("Change your profile details.");

        jPanel16.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout settingPanelLayout = new javax.swing.GroupLayout(settingPanel);
        settingPanel.setLayout(settingPanelLayout);
        settingPanelLayout.setHorizontalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(fullnameField)
                                            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(saveInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(settingPanelLayout.createSequentialGroup()
                                        .addComponent(genderMaleField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(genderFemaleField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(55, 55, 55)
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(savedFullname, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(savedGender, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label51, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addComponent(tabContentLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(saveError, javax.swing.GroupLayout.PREFERRED_SIZE, 719, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(490, Short.MAX_VALUE))
        );
        settingPanelLayout.setVerticalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tabContentLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveError, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fullnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(savedFullname, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(label51, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(genderMaleField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(genderFemaleField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(savedGender, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(saveInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(209, Short.MAX_VALUE))
        );

        content.add(settingPanel, "card5");

        notificationPanel.setBackground(new java.awt.Color(41, 41, 41));

        tabContentLabel10.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel10.setForeground(new java.awt.Color(50, 179, 96));
        tabContentLabel10.setText("Notifications");

        notificationArea.setBackground(new java.awt.Color(41, 41, 41));
        notificationArea.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        javax.swing.GroupLayout notificationPanelLayout = new javax.swing.GroupLayout(notificationPanel);
        notificationPanel.setLayout(notificationPanelLayout);
        notificationPanelLayout.setHorizontalGroup(
            notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notificationPanelLayout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addGroup(notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(notificationPanelLayout.createSequentialGroup()
                        .addComponent(tabContentLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(notificationArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        notificationPanelLayout.setVerticalGroup(
            notificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notificationPanelLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(tabContentLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(notificationArea, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        content.add(notificationPanel, "card5");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(sideBar, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titleBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sideBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(titleBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void closeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseClicked
        System.exit(0);
    }//GEN-LAST:event_closeMouseClicked

    private void closeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseEntered
        close.setIcon(new ImageIcon(getClass().getResource("/images/closeClicked.png")));
    }//GEN-LAST:event_closeMouseEntered

    private void closeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseExited
        close.setIcon(new ImageIcon(getClass().getResource("/images/close.png")));
    }//GEN-LAST:event_closeMouseExited

    private void minimiseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimiseMouseClicked
        this.setExtendedState(ICONIFIED);
    }//GEN-LAST:event_minimiseMouseClicked

    private void minimiseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimiseMouseEntered
        minimise.setIcon(new ImageIcon(getClass().getResource("/images/minimiseClicked.png")));
    }//GEN-LAST:event_minimiseMouseEntered

    private void minimiseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimiseMouseExited
        minimise.setIcon(new ImageIcon(getClass().getResource("/images/minimise.png")));
    }//GEN-LAST:event_minimiseMouseExited

    private void dashboardOptionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardOptionMouseEntered
        dashboardOption.setBackground(java.awt.Color.decode("#1e1e1e"));
    }//GEN-LAST:event_dashboardOptionMouseEntered

    private void stocksOptionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stocksOptionMouseEntered
        stocksOption.setBackground(java.awt.Color.decode("#1e1e1e"));
    }//GEN-LAST:event_stocksOptionMouseEntered

    private void aboutOptionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutOptionMouseEntered
        aboutOption.setBackground(java.awt.Color.decode("#1e1e1e"));
    }//GEN-LAST:event_aboutOptionMouseEntered

    private void dashboardOptionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardOptionMouseExited
        dashboardOption.setBackground(java.awt.Color.decode("#393836"));
    }//GEN-LAST:event_dashboardOptionMouseExited

    private void stocksOptionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stocksOptionMouseExited
        stocksOption.setBackground(java.awt.Color.decode("#393836"));
    }//GEN-LAST:event_stocksOptionMouseExited

    private void aboutOptionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutOptionMouseExited
        aboutOption.setBackground(java.awt.Color.decode("#393836"));
    }//GEN-LAST:event_aboutOptionMouseExited

    private void dashboardOptionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardOptionMouseClicked
        // Switch tab
        dashboardPanel.setVisible(true);
        stocksPanel.setVisible(false);
        aboutPanel.setVisible(false);
        peoplePanel.setVisible(false);
        settingPanel.setVisible(false);
        notificationPanel.setVisible(false);
    }//GEN-LAST:event_dashboardOptionMouseClicked

    private void stocksOptionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stocksOptionMouseClicked
        // Switch tab
        dashboardPanel.setVisible(false);
        stocksPanel.setVisible(true);
        aboutPanel.setVisible(false);
        peoplePanel.setVisible(false);
        settingPanel.setVisible(false);
        notificationPanel.setVisible(false);
        
        showProductsDataInStocks(null);
    }//GEN-LAST:event_stocksOptionMouseClicked

    private void aboutOptionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutOptionMouseClicked
        // Switch tab
        dashboardPanel.setVisible(false);
        stocksPanel.setVisible(false);
        aboutPanel.setVisible(true);
        peoplePanel.setVisible(false);
        settingPanel.setVisible(false);
        notificationPanel.setVisible(false);
    }//GEN-LAST:event_aboutOptionMouseClicked

    private void logoutOptionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutOptionMouseEntered
        logoutNote.setVisible(true);
    }//GEN-LAST:event_logoutOptionMouseEntered

    private void logoutOptionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutOptionMouseExited
        logoutNote.setVisible(false);
    }//GEN-LAST:event_logoutOptionMouseExited

    private void logoutOptionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutOptionMouseClicked
        LoginForm lgn = new LoginForm();
        lgn.setVisible(true);
        lgn.pack();
        this.dispose();
    }//GEN-LAST:event_logoutOptionMouseClicked

    private void peopleIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_peopleIconMouseClicked
        // Switch tab
        dashboardPanel.setVisible(false);
        stocksPanel.setVisible(false);
        aboutPanel.setVisible(false);
        settingPanel.setVisible(false);
        notificationPanel.setVisible(false);

        peoplePanel.setVisible(true);
        peoplePanel.revalidate();
        peoplePanel.repaint();

        currentTab = "people";

        try {

            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            Statement s = con.createStatement();
            String sql = "SELECT userid,fullname,gender,role FROM userDBForJavaApp";

            ResultSet rs = s.executeQuery(sql);

            peoplePanels = new JPanel[10];
            JLabel[] peopleLabels = new JLabel[10];
            JLabel[] peopleHeadLabels = new JLabel[5];
            peopleArea.removeAll();

            String[] headLabels = new String[]{"USERID", "NAME", "GENDER", "ROLE", "ACTION"};

            for (int k = 0; k < 5; k++) {
                peopleHeadLabels[k] = new JLabel(headLabels[k]);
                peopleHeadLabels[k].setFont(new java.awt.Font("Segoe UI", 1, 15));
                peopleHeadLabels[k].setForeground(new java.awt.Color(100, 100, 100));
                peopleArea.add(peopleHeadLabels[k], new org.netbeans.lib.awtextra.AbsoluteConstraints(120 * k, 0, 300, 30));
            }
            int i = 1;
            while (rs.next()) {

                if (!rs.getString("role").equals(new String("NO")) && !rs.getString("userid").equals(userid)) {
                    currentIinPeople = i;

                    peoplePanels[i] = new JPanel();
                    peoplePanels[i].setBackground(new java.awt.Color(41, 41, 41));
                    peoplePanels[i].setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
                    peoplePanels[i].setVisible(true);

                    for (int j = 0; j < 4; j++) {
                        peopleLabels[i] = new JLabel(rs.getString(j + 1));
                        peopleLabels[i].setFont(new java.awt.Font("Segoe UI", 1, 13));
                        peopleLabels[i].setForeground(new java.awt.Color(255, 255, 255));
                        peoplePanels[i].add(peopleLabels[i], new org.netbeans.lib.awtextra.AbsoluteConstraints(120 * j, 0, 300, 30));
                    }

                    // Delete button
                    JPanel peopleButtonPanel = new JPanel();

                    peopleButtonPanel.setSize(25, 60);
                    peopleButtonPanel.setBackground(new java.awt.Color(198, 40, 40));
                    peopleButtonPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

                    peopleButtonPanel.setName(rs.getString("userid"));
                    peopleButtonPanel.setBackground(new java.awt.Color(198, 40, 40));
                    peopleButtonPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    peopleButtonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            deleteUserMouseClicked(evt);
                        }
                    });

                    JLabel peopleDeleteButtonLabel = new JLabel("DELETE");
                    peopleDeleteButtonLabel.setFont(new java.awt.Font("Segoe UI", 1, 12));
                    peopleDeleteButtonLabel.setForeground(new java.awt.Color(255, 255, 255));
                    peopleButtonPanel.add(peopleDeleteButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 60, 25));

                    peoplePanels[i].add(peopleButtonPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 5, 60, 25));
                    //Delete Button ends
                    /*
                    if(rs.getString("role").equals(new String("ADMIN")))
                    {
                        // Delete button
                        JPanel peopleButtonPanel = new JPanel();

                        peopleButtonPanel.setSize(25, 60);
                        peopleButtonPanel.setBackground(new java.awt.Color(198,40,40));
                        peopleButtonPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

                        peopleButtonPanel.setName(rs.getString("userid"));
                        peopleButtonPanel.setBackground(new java.awt.Color(198,40,40));
                        peopleButtonPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                        peopleButtonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent evt) {
                                deleteUserMouseClicked(evt);
                            }
                        });

                        JLabel peopleDeleteButtonLabel = new JLabel("DELETE");
                        peopleDeleteButtonLabel.setFont(new java.awt.Font("Segoe UI", 1, 12));
                        peopleDeleteButtonLabel.setForeground(new java.awt.Color(255, 255, 255));
                        peopleButtonPanel.add(peopleDeleteButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10,0,60,25));

                        peoplePanels[i].add(peopleButtonPanel,new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 5, 60, 25));
                        // Bill Button ends
                    }*/
                    peopleArea.add(peoplePanels[i], new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50 * i, 800, 30));
                    peoplePanels[i].setVisible(true);

                    i++;
                    if (i > 10) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured! Please try again." + e);
        }
    }//GEN-LAST:event_peopleIconMouseClicked

    private void settingIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingIconMouseClicked
        // Switch tab
        dashboardPanel.setVisible(false);
        stocksPanel.setVisible(false);
        aboutPanel.setVisible(false);
        peoplePanel.setVisible(false);
        settingPanel.setVisible(true);
        notificationPanel.setVisible(false);
    }//GEN-LAST:event_settingIconMouseClicked

    private void notificationIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_notificationIconMouseClicked
        // Switch tab
        dashboardPanel.setVisible(false);
        stocksPanel.setVisible(false);
        aboutPanel.setVisible(false);
        peoplePanel.setVisible(false);
        settingPanel.setVisible(false);

        notificationPanel.setVisible(true);
        notificationPanel.revalidate();
        notificationPanel.repaint();

        currentTab = "notification";

        try {

            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            Statement s = con.createStatement();
            String sql = "SELECT * FROM userDBForJavaApp";

            ResultSet rs = s.executeQuery(sql);

            notificationPanels = new JPanel[7];
            JLabel[] notificationLabels = new JLabel[7];
            notificationArea.removeAll();

            int i = 0;
            while (rs.next()) {

                // <fullname> has requested to complete registration process
                // <list><button>
                if (rs.getString("role").equals(new String("NO"))) {
                    currentIinNotification = i;

                    // Create a message
                    notificationLabels[i] = new JLabel(rs.getString("fullname") + " has requested you to complete registration process!");
                    notificationLabels[i].setFont(new java.awt.Font("Segoe UI", 0, 15));
                    notificationLabels[i].setForeground(new java.awt.Color(255, 255, 255));

                    //Create a panel and add label and buttons
                    notificationPanels[i] = new JPanel();
                    notificationPanels[i].setBackground(Color.black);
                    notificationPanels[i].setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
                    notificationPanels[i].add(notificationLabels[i], new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, 700, 47));
                    notificationPanels[i].setVisible(true);

                    // Create a (admin) (billing) (reject)
                    JPanel notificationAdminButtonPanel = new JPanel();
                    JPanel notificationBillButtonPanel = new JPanel();
                    JPanel notificationRejectButtonPanel = new JPanel();

                    notificationAdminButtonPanel.setSize(100, 200);
                    notificationAdminButtonPanel.setBackground(new java.awt.Color(50, 179, 96));
                    notificationAdminButtonPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

                    notificationBillButtonPanel.setSize(100, 200);
                    notificationBillButtonPanel.setBackground(new java.awt.Color(255, 152, 0));
                    notificationBillButtonPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

                    notificationRejectButtonPanel.setSize(100, 200);
                    notificationRejectButtonPanel.setBackground(new java.awt.Color(198, 40, 40));
                    notificationRejectButtonPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

                    JLabel notificationAdminButtonLabel = new JLabel("+ ADMIN");
                    notificationAdminButtonLabel.setFont(new java.awt.Font("Segoe UI", 1, 12));
                    notificationAdminButtonPanel.setName(rs.getString("userid"));
                    notificationAdminButtonPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    notificationAdminButtonPanel.add(notificationAdminButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 80, 30));
                    notificationAdminButtonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            setAsAdminMouseClicked(evt);
                        }
                    });

                    JLabel notificationBillButtonLabel = new JLabel("+ BILL");
                    notificationBillButtonLabel.setFont(new java.awt.Font("Segoe UI", 1, 12));
                    notificationBillButtonPanel.setName(rs.getString("userid"));
                    notificationBillButtonPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    notificationBillButtonPanel.add(notificationBillButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 0, 80, 30));
                    notificationBillButtonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            setAsBillMouseClicked(evt);
                        }
                    });

                    JLabel notificationRejectButtonLabel = new JLabel("- REJECT");
                    notificationRejectButtonLabel.setFont(new java.awt.Font("Segoe UI", 1, 12));
                    notificationRejectButtonPanel.setName(rs.getString("userid"));
                    notificationRejectButtonPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    notificationRejectButtonPanel.add(notificationRejectButtonLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 0, 80, 30));
                    notificationRejectButtonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            deleteUserMouseClicked(evt);
                        }
                    });

                    notificationPanels[i].add(notificationAdminButtonPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 80, 30));
                    notificationPanels[i].add(notificationBillButtonPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 10, 80, 30));
                    notificationPanels[i].add(notificationRejectButtonPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 10, 80, 30));

                    notificationArea.add(notificationPanels[i], new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70 * i, 800, 50));

                    i++;
                    if (i > 6) {
                        break;
                    }
                }
            }
            if (i == 0) {
                // Create a message
                notificationLabels[i] = new JLabel("No new notification!");
                notificationLabels[i].setFont(new java.awt.Font("Segoe UI", 0, 15));
                notificationLabels[i].setForeground(new java.awt.Color(255, 255, 255));

                //Create a panel and add label and buttons
                notificationPanels[i] = new JPanel();
                notificationPanels[i].setBackground(Color.black);
                notificationPanels[i].setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
                notificationPanels[i].add(notificationLabels[i], new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, 500, 50));
                notificationArea.add(notificationPanels[i], new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70 * i, 800, 50));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured! Please try again." + e);
        }
    }//GEN-LAST:event_notificationIconMouseClicked

    private void fullnameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullnameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fullnameFieldActionPerformed

    private void saveInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveInfoMouseClicked
        // save information
        String gndr = null;
        String fname = fullnameField.getText();
        String pwd = new String(passwordField.getPassword());

        String name_regex = "^([a-zA-Z0-9_])+(\\s[a-zA-Z]+)$";
        Pattern namePattern = Pattern.compile(name_regex);
        Matcher nameMatcher = namePattern.matcher(fname);

        Boolean catchFlag = true;

        int conditionCode = 0;

        try {

            Connection con;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            PreparedStatement p = null;

            //Starting validation
            if (!fname.isEmpty()) {
                if (fname.length() < 5 || fname.length() >= 30) // checking length of fullname
                {
                    saveError.setText("Fullname must be 5-30 characters long.");
                } else if (!nameMatcher.matches()) // checking if the username matches the regex pattern
                {
                    saveError.setText("Full name can only contain spaces and alphabets.");
                } else {
                    fname = fname.substring(0, 1).toUpperCase() + fname.substring(1).toLowerCase(); //Autocapitalize
                    conditionCode += 1;
                }
            }
            if (!pwd.isEmpty()) {
                if (pwd.length() < 5 || pwd.length() >= 20) // checking length of password
                {
                    saveError.setText("Password must be 5-20 characters long.");
                } else {
                    conditionCode += 2;
                }
            }
            if (genderMaleField.isSelected() || genderFemaleField.isSelected()) {
                gndr = genderMaleField.isSelected() ? "Male" : "Female";
                conditionCode += 3;
            }

            /*
                0 - Error: nothing 
                1 - Change name
                2 - Change password
                3 - Change gender
                6 - Change everything
             */
            switch (conditionCode) {
                case 1:
                    p = con.prepareStatement("UPDATE userDBForJavaApp SET fullname = ? WHERE userid = ?");
                    p.setString(1, fname);
                    p.setString(2, userid);
                    break;
                case 2:
                    p = con.prepareStatement("UPDATE userDBForJavaApp SET password = ? WHERE userid = ?");
                    p.setString(1, pwd);
                    p.setString(2, userid);
                    break;
                case 3:
                    p = con.prepareStatement("UPDATE userDBForJavaApp SET gender = ? WHERE userid = ?");
                    p.setString(1, gndr);
                    p.setString(2, userid);
                    break;
                case 6:
                    p = con.prepareStatement("UPDATE userDBForJavaApp SET fullname = ?, password = ?,gender = ? WHERE userid = ?");
                    p.setString(1, fname);
                    p.setString(2, pwd);
                    p.setString(3, gndr);
                    p.setString(4, userid);
                    break;
                default:
                    catchFlag = false;
            }
            if (catchFlag) {
                int i = p.executeUpdate();
                if (i > 0) {
                    JOptionPane.showMessageDialog(null, "Details updated!");
                    fullnameField.setText("");
                    passwordField.setText("");
                    buttonGroup1.clearSelection();
                    saveError.setText("");
                } else {
                    JOptionPane.showMessageDialog(null, "Error updating data!");
                }
            }

        } catch (Exception e) {
            saveError.setText("Some error occured! Please try again." + e);
        }

    }//GEN-LAST:event_saveInfoMouseClicked

    private void saveInfoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveInfoMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_saveInfoMouseEntered

    private void addProductMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addProductMouseClicked
        AddProduct ap = new AddProduct();
        ap.setVisible(true);
    }//GEN-LAST:event_addProductMouseClicked

    private void addCategoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addCategoryMouseClicked
        AddCategory ac = new AddCategory();
        ac.setVisible(true);
    }//GEN-LAST:event_addCategoryMouseClicked

    private void reloadStockKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_reloadStockKeyPressed

    }//GEN-LAST:event_reloadStockKeyPressed

    private void reloadStockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reloadStockMouseClicked
        showProductsDataInStocks(null);
    }//GEN-LAST:event_reloadStockMouseClicked

    private void searchData() {
        
        ArrayList<Item> itemsList= new ArrayList<>();
        String searchText = searchProduct.getText();
        
        if(!searchText.equals("")) {
            modelStocks.emptyData();
            Metaphone m = new Metaphone();
            searchText = m.metaphone(searchText);
            
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
                PreparedStatement s = con.prepareStatement("SELECT * FROM productDBForJavaApp WHERE searchPhrase LIKE ? OR categoryId LIKE ? ORDER BY quantity DESC");
                s.setString(1, "%"+searchText+"%");
                s.setString(2, "%"+searchText+"%");
                ResultSet rs = s.executeQuery();
                Item items;
                while(rs.next()) {
                    items = new Item(rs.getString("lastModified"), rs.getString("pId"), rs.getString("pName"), rs.getString("pDesc"), rs.getInt("quantity"), rs.getDouble("sellingPrice"), rs.getBytes("pImage"));
                    itemsList.add(items);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Some error occured!" + e);
            }
            showProductsDataInStocks(itemsList);
        }
        else {
            showProductsDataInStocks(null);
        }
    }
    private void searchProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchProductActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchProductActionPerformed

    private void searchProductKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchProductKeyPressed
        if(evt.getExtendedKeyCode() == 10) {
            searchData();
        }
    }//GEN-LAST:event_searchProductKeyPressed

    private void searchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseClicked
        // TODO add your handling code here:
        searchData();
    }//GEN-LAST:event_searchButtonMouseClicked

    private void searchButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_searchButtonMouseEntered

    private void searchButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_searchButtonMouseExited

    private void deleteBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteBtnMouseClicked
        String id = modelStocks.getValueAt(stockTable.getSelectedRow() , 1).toString();
        if(id != null) {
            switch(JOptionPane.showConfirmDialog(null, "Product with ID "+id+" will be delete!")) {
                //0 - Clicked YES delete
                case 0:
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB","root","");
                            PreparedStatement p = con.prepareStatement("DELETE FROM productDBForJavaApp WHERE pId = ?");
                            p.setString(1, id);
                            int i = p.executeUpdate();
                            if(i>0) {
                                showProductsDataInStocks(null);
                            }
                        }
                        catch(Exception e) {JOptionPane.showMessageDialog(null, "Some error occurred!");}
                        break;
                //1 - Clicked NO delete
                case 1:;
                //2 - Canceled
                case 2:;
                default:break;
            }
        }
    }//GEN-LAST:event_deleteBtnMouseClicked

    private void editBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editBtnMouseClicked
        String id = modelStocks.getValueAt(stockTable.getSelectedRow() , 1).toString();
        if(id != null) {
            AddProduct ap = new AddProduct(id);
            ap.setVisible(true);
        }
    }//GEN-LAST:event_editBtnMouseClicked

    private void goToGithub(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goToGithub
        try {
            String url = "https://github.com/jassusharma660/";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {}
    }//GEN-LAST:event_goToGithub

    private void goToLinkedin(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goToLinkedin
        try {
            String url = "https://www.linkedin.com/in/jassusharma660";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {}
    }//GEN-LAST:event_goToLinkedin

    private void setAsAdminMouseClicked(java.awt.event.MouseEvent evt) {
        JPanel jpanel = (JPanel) evt.getSource();
        String username = jpanel.getName();
        try {
            Connection con;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            PreparedStatement p = con.prepareStatement("UPDATE userDBForJavaApp SET role = 'ADMIN' WHERE userid=?");
            p.setString(1, username);
            int i = p.executeUpdate();
            if (i > 0) {
                JOptionPane.showMessageDialog(null, username + " made admin!");
                notificationPanels[currentIinNotification].setVisible(false);
                notificationPanel.revalidate();
                notificationPanel.repaint();
                notificationIconMouseClicked(evt);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured!!" + e);
        }
    }

    private void setAsBillMouseClicked(java.awt.event.MouseEvent evt) {
        JPanel jpanel = (JPanel) evt.getSource();
        String username = jpanel.getName();
        try {
            Connection con;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            PreparedStatement p = con.prepareStatement("UPDATE userDBForJavaApp SET role = 'BILL' WHERE userid=?");
            p.setString(1, username);
            int i = p.executeUpdate();
            if (i > 0) {
                JOptionPane.showMessageDialog(null, username + " accepted for billing!");
                notificationPanels[currentIinNotification].setVisible(false);
                notificationPanel.revalidate();
                notificationPanel.repaint();
                notificationIconMouseClicked(evt);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occured!!" + e);
        }
    }

    private void deleteUserMouseClicked(java.awt.event.MouseEvent evt) {
        JPanel jpanel = (JPanel) evt.getSource();
        String username = jpanel.getName();
        try {
            Connection con;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql:///newDB", "root", "");
            PreparedStatement p = con.prepareStatement("DELETE FROM userDBForJavaApp WHERE userid=?");
            p.setString(1, username);
            int i = p.executeUpdate();
            if (i > 0) {
                switch (currentTab) {
                    case "notification":
                        JOptionPane.showMessageDialog(null, "Request for " + username + " is rejected!");
                        notificationPanels[currentIinNotification].setVisible(false);
                        System.out.println("n:" + currentIinNotification);
                        notificationPanel.revalidate();
                        notificationPanel.repaint();
                        notificationIconMouseClicked(evt);
                        break;
                    case "people":
                        JOptionPane.showMessageDialog(null, username + " removed!");
                        System.out.println("p:" + currentIinPeople);
                        peoplePanels[currentIinPeople].setVisible(false);
                        peoplePanel.revalidate();
                        peoplePanel.repaint();
                        peopleIconMouseClicked(evt);
                        break;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "p:Some error occured!!" + e);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aboutOption;
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JPanel addCategory;
    private javax.swing.JPanel addProduct;
    private javax.swing.JLabel adminCount;
    private javax.swing.JLabel billCount;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel categoriesStat;
    private javax.swing.JPanel categoriesStat1;
    private javax.swing.JLabel categoriesStatLabel;
    private javax.swing.JLabel categoriesStatLabel1;
    private javax.swing.JTable categoryStatTable;
    private javax.swing.JLabel close;
    private javax.swing.JPanel content;
    private javax.swing.JPanel dashboardOption;
    private javax.swing.JPanel dashboardPanel;
    private javax.swing.JLabel deleteBtn;
    private javax.swing.JLabel editBtn;
    private javax.swing.JTextField fullnameField;
    private javax.swing.JLabel fullnameLabel;
    private javax.swing.JRadioButton genderFemaleField;
    private javax.swing.JRadioButton genderMaleField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel label51;
    private javax.swing.JPanel logoutNote;
    private javax.swing.JPanel logoutOption;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel minimise;
    private javax.swing.JPanel notificationArea;
    private javax.swing.JLabel notificationIcon;
    private javax.swing.JPanel notificationPanel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JPanel peopleArea;
    private javax.swing.JLabel peopleIcon;
    private javax.swing.JPanel peoplePanel;
    private javax.swing.JLabel reloadStock;
    private javax.swing.JLabel roleLabel;
    private javax.swing.JLabel saveError;
    private javax.swing.JLabel saveInfo;
    private javax.swing.JLabel savedFullname;
    private javax.swing.JLabel savedGender;
    private javax.swing.JLabel searchButton;
    private javax.swing.JTextField searchProduct;
    private javax.swing.JLabel settingIcon;
    private javax.swing.JPanel settingPanel;
    private javax.swing.JPanel sideBar;
    private javax.swing.JPanel staffStat;
    private javax.swing.JLabel staffStatLabel;
    private javax.swing.JTable stockTable;
    private javax.swing.JPanel stocksOption;
    private javax.swing.JPanel stocksPanel;
    private javax.swing.JLabel tabContentLabel10;
    private javax.swing.JLabel tabContentLabel2;
    private javax.swing.JLabel tabContentLabel3;
    private javax.swing.JLabel tabContentLabel4;
    private javax.swing.JLabel tabContentLabel7;
    private javax.swing.JLabel tabContentLabel8;
    private javax.swing.JPanel titleBar;
    private javax.swing.JTable transactionStatTable;
    private javax.swing.JLabel unconfirmedCount;
    // End of variables declaration//GEN-END:variables

    private static class Item {
        
        String dateModified = null, code = null, name = null, description = null; 
        int inStock = 0;
        double sellingPrice = 0;
        byte[] image = null;
        
        private Item(String date, String c, String n, String d, int i, double s, byte[] img) {
            dateModified = date; code = c; name = n; description = d; inStock = i; sellingPrice = s; image = img;
        }
    }
}
