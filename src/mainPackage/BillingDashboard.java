/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainPackage;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import static java.lang.Integer.parseInt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.codec.language.Metaphone;

/**
 *
 * @author Jassu Sharma
 */
public class BillingDashboard extends javax.swing.JFrame {
    
    int mouseX, mouseY;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
    int taskBarSize = scnMax.bottom;
    
    String userid, fullname, gender, role;
    String billId = null, customerName = "NONE";
    ArrayList<BillItems> billList; 
    
    DefaultTableModel modelProducts, modelBill;
    /**
     * Creates new form BillingDashboard
     */
    public BillingDashboard() {
        screenSize.height -= taskBarSize - getHeight();
        screenSize.width -= getWidth();
        initComponents();
        modelProducts = (DefaultTableModel)productTable.getModel();
        modelBill = (DefaultTableModel)billTable.getModel();
        //session("jassusharma", "Jassu sharma", "Male", "BILL");
    }
    
    public void session(String u, String f, String g, String r)
    {
        userid = u; fullname = f; gender = g; role = r;
  
        fullnameLabel.setText(fullname);
        roleLabel.setText("("+role+")");
        fullnameLabel1.setText(fullname);
        roleLabel1.setText("("+role+")");
        savedFullname.setText(fullname);
        savedGender.setText(gender);
        
        newBill();
        populateProductTable(null);
    }
    
    /**
     * This allow to populate table initially 
     * and also when something is searched 
     * @param search
     */
    public void populateProductTable(String search) {
        modelProducts.setRowCount(0);
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///newDB","root","");
            PreparedStatement p;
            if(search == null) {
                p = con.prepareStatement("SELECT * FROM productDBForJavaApp");
            }
            else {
                Metaphone m = new Metaphone();
                search = m.metaphone(search);
                p = con.prepareStatement("SELECT * FROM productDBForJavaApp WHERE pId LIKE ? OR searchPhrase LIKE ? ORDER BY pName DESC");
                p.setString(1, "%"+search+"%");
                p.setString(2, "%"+search+"%");
            }
            String pId = null, pName = null;
            int qnty = 0;
            ResultSet rs = p.executeQuery();
            while(rs.next()) {
                qnty = rs.getInt("quantity");
                if(qnty < 1) continue;
                pId = rs.getString("pId");
                pName = rs.getString("pName");
                modelProducts.insertRow(modelProducts.getRowCount(), new Object[]{pId,pName,qnty});
            }
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Some error occurred!"+e);
        }
    }
    
    public void newBill() {
        
        billList = new ArrayList<BillItems>();
        
        //Clear Inputs
        customerNameInput.setText("");
        customerContactInput.setText("");
        searchProduct1.setText("");
        //Clear the current tables
        if(modelBill.getRowCount() != 0)
            modelBill.setRowCount(0);
        populateProductTable(null);
        //Current date
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	LocalDate localDate = LocalDate.now();
        dateToday.setText(dtf.format(localDate));
        //Generate an invoice no.
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	LocalDateTime localDate1 = LocalDateTime.now();
        billId = dtf1.format(localDate1);
        billNo.setText(billId);
    }
    
    private void addItemToBill() {
        if(productTable.getSelectedRow() >= 0) {
            int stocksAvailable = (int) productTable.getValueAt(productTable.getSelectedRow(), 2);
            int qntyInput = 0;
            if(!quantityInput.getText().isEmpty()) {
                qntyInput = parseInt(quantityInput.getText());
                if(qntyInput > 0) {
                    if(stocksAvailable >= qntyInput) {
                        String id = productTable.getValueAt(productTable.getSelectedRow(), 0).toString();
                            try {
                                Class.forName("com.mysql.jdbc.Driver");
                                Connection con = DriverManager.getConnection("jdbc:mysql:///newDB","root","");
                                PreparedStatement p = con.prepareStatement("SELECT * FROM productDBForJavaApp WHERE pId = ?");
                                p.setString(1, id);
                                ResultSet rs = p.executeQuery();
                                BillItems items;
                                while(rs.next()) {
                                    // billId, userid, soldTo, contact, pId, sellingPrice, quantity;
                                    String sTo, cntct, qnty;
                                    double sellingPrice, discount, gst, sPrice;
                                   
                                    sellingPrice = rs.getDouble("sellingPrice");
                                    discount = rs.getDouble("discount");
                                    gst = rs.getDouble("gst");

                                    sTo = customerNameInput.getText();
                                    cntct = customerContactInput.getText();

                                    gst = (gst * ((sellingPrice)/100));
                                    discount = (discount * ((sellingPrice)/100));
                                    sPrice = (sellingPrice + gst) - discount;
                                    sPrice *= qntyInput;

                                    items = new BillItems(billId, userid, sTo, cntct, id, sPrice, qntyInput);
                                    billList.add(items);

                                    modelBill.insertRow(modelBill.getRowCount(), new Object[]{id, rs.getString("pName"), qntyInput, sellingPrice, gst, discount, sPrice});
                                    billTable.setRowHeight(60);
                                }
                            }
                            catch(Exception e) {
                                JOptionPane.showMessageDialog(null, "Some error occurred!");
                        }
                    }
                    else JOptionPane.showMessageDialog(null, "Required quantity is more than the available stocks!");
                }
                else JOptionPane.showMessageDialog(null, "Required quantity must be more than 0!");
            }
        }
    }
    
    private void removeFromTheTable() {
        try{
            if(productTable.getSelectedRow() > 0) {
                int rowNo = billTable.getSelectedRow();
                modelBill.removeRow(rowNo);
            }
        }
        catch(Exception e) {}
    }
    
    private void saveBillToDB() {
        if(billList.size()>0) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql:///newDB","root","");
                PreparedStatement bill = con.prepareStatement("INSERT INTO billDBForJavaApp(billId, userid, soldTo, contact) VALUES(?,?,?,?)");
                PreparedStatement billData = con.prepareStatement("INSERT INTO billItemDBForJavaApp(billId, pId, quantity, sellingPrice) VALUES(?,?,?,?)");
                PreparedStatement itemQnty = con.prepareStatement("UPDATE productDBForJavaApp SET quantity = GREATEST(0, quantity - ?) WHERE pId = ?");
                
                bill.setString(1, billList.get(0).billId);
                bill.setString(2, billList.get(0).userid);
                bill.setString(3, billList.get(0).soldTo);
                bill.setString(4, billList.get(0).contact);
                int billExe = bill.executeUpdate(), billDatExe = 0, itemQtyExe = 0;
                
                for(int i = 0; i < billList.size(); i++) {
                    itemQnty.setInt(1, billList.get(i).quantity);
                    itemQnty.setString(2, billList.get(i).pId);
                    itemQtyExe = itemQnty.executeUpdate();
                    
                    billData.setString(1, billList.get(i).billId);
                    billData.setString(2, billList.get(i).pId);
                    billData.setInt(3, billList.get(i).quantity);
                    billData.setDouble(4, billList.get(i).sellingPrice);
                    
                    billDatExe = billData.executeUpdate();
                }
                if(billExe > 0 && billDatExe >0 && itemQtyExe > 0) {
                    JOptionPane.showMessageDialog(null, "Success!");
                    newBill();
                }
            }
            catch(Exception e) {
                JOptionPane.showMessageDialog(null,"Some error occurred!"+e);
            }
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
        titleBar = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        close = new javax.swing.JLabel();
        minimise = new javax.swing.JLabel();
        content = new javax.swing.JPanel();
        dashboardPanel = new javax.swing.JPanel();
        tabContentLabel3 = new javax.swing.JLabel();
        logoutButton = new javax.swing.JLabel();
        homeIcon = new javax.swing.JLabel();
        settingIcon = new javax.swing.JLabel();
        fullnameLabel = new javax.swing.JLabel();
        roleLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        billNoLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        billNo = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        billNoLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        customerNameInput = new javax.swing.JTextField();
        billNoLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        customerContactInput = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        searchProduct1 = new javax.swing.JTextField();
        jPanel20 = new javax.swing.JPanel();
        searchButton = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        productTable = new javax.swing.JTable();
        jPanel24 = new javax.swing.JPanel();
        addToBill = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        quantityInput = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        billTable = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        removeFromBill = new javax.swing.JLabel();
        jPanel27 = new javax.swing.JPanel();
        saveBill = new javax.swing.JLabel();
        dateToday = new javax.swing.JLabel();
        createNewBill = new javax.swing.JPanel();
        billNo1 = new javax.swing.JLabel();
        settingPanel = new javax.swing.JPanel();
        tabContentLabel4 = new javax.swing.JLabel();
        logoutButton1 = new javax.swing.JLabel();
        homeIcon1 = new javax.swing.JLabel();
        settingIcon1 = new javax.swing.JLabel();
        fullnameLabel1 = new javax.swing.JLabel();
        roleLabel1 = new javax.swing.JLabel();
        saveError = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        savedFullname = new javax.swing.JLabel();
        fullnameField = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        label51 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        passwordField = new javax.swing.JPasswordField();
        jLabel27 = new javax.swing.JLabel();
        genderMaleField = new javax.swing.JRadioButton();
        genderFemaleField = new javax.swing.JRadioButton();
        savedGender = new javax.swing.JLabel();
        saveInfo = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(screenSize);

        mainPanel.setBackground(new java.awt.Color(41, 41, 41));

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        tabContentLabel3.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel3.setText("DASHBOARD");

        logoutButton.setBackground(new java.awt.Color(255, 255, 255));
        logoutButton.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        logoutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blackLogout.png"))); // NOI18N
        logoutButton.setText("Logout");
        logoutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoutButton.setIconTextGap(10);
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutButtonMouseClicked(evt);
            }
        });

        homeIcon.setBackground(new java.awt.Color(255, 255, 255));
        homeIcon.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        homeIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blackDashboard.png"))); // NOI18N
        homeIcon.setText("Home");
        homeIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homeIcon.setIconTextGap(10);
        homeIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homeIconMouseClicked(evt);
            }
        });

        settingIcon.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        settingIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        settingIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settings.png"))); // NOI18N
        settingIcon.setText("Settings");
        settingIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settingIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingIconMouseClicked(evt);
            }
        });

        fullnameLabel.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        fullnameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fullnameLabel.setText("Full name");

        roleLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        roleLabel.setForeground(new java.awt.Color(51, 51, 51));
        roleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        roleLabel.setText("(ROLE)");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        billNoLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        billNoLabel.setText("Bill No.");

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        billNo.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(billNo, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(billNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(billNoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(billNoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)), "Customer", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(51, 51, 51))); // NOI18N

        billNoLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        billNoLabel1.setText("Customer Name :");

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        customerNameInput.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        customerNameInput.setBorder(null);
        customerNameInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerNameInputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(customerNameInput, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(customerNameInput)
        );

        billNoLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        billNoLabel2.setText("Contact :     +91");

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        customerContactInput.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        customerContactInput.setBorder(null);
        customerContactInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerContactInputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(customerContactInput, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(customerContactInput, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(billNoLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(billNoLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(billNoLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(billNoLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true), "Search Product", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(51, 51, 51))); // NOI18N

        jPanel17.setBackground(new java.awt.Color(255, 255, 255));

        jPanel19.setBackground(new java.awt.Color(255, 255, 255));
        jPanel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        searchProduct1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        searchProduct1.setForeground(new java.awt.Color(51, 51, 51));
        searchProduct1.setBorder(null);
        searchProduct1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchProduct1ActionPerformed(evt);
            }
        });
        searchProduct1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                searchProduct1KeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchProduct1)
                .addContainerGap())
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchProduct1, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        jPanel20.setBackground(new java.awt.Color(204, 204, 204));

        searchButton.setBackground(new java.awt.Color(0, 0, 0));
        searchButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        searchButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        searchButton.setText("Search");
        searchButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        searchButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
        );

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(102, 102, 102));
        jLabel17.setText("Search by product code or name");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel16.setText("Search Product");

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        productTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Code", "Product Name", "Quantity"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(productTable);

        jPanel24.setBackground(new java.awt.Color(204, 204, 204));

        addToBill.setBackground(new java.awt.Color(0, 0, 0));
        addToBill.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        addToBill.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        addToBill.setText("<< ADD TO BILL");
        addToBill.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        addToBill.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addToBill.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addToBillMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addToBillMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addToBillMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addToBill, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addToBill, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel25.setBackground(new java.awt.Color(255, 255, 255));
        jPanel25.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        quantityInput.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        quantityInput.setForeground(new java.awt.Color(51, 51, 51));
        quantityInput.setText("1");
        quantityInput.setBorder(null);
        quantityInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantityInputActionPerformed(evt);
            }
        });
        quantityInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                quantityInputKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(quantityInput, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(quantityInput)
        );

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel18.setText("Quantity");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)), "Bill", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 11), new java.awt.Color(51, 51, 51))); // NOI18N

        billTable.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        billTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Item Code", "Item Name", "Qty", "Price", "Tax", "Discount", "Net Amount"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        billTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(billTable);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jPanel23.setBackground(new java.awt.Color(204, 204, 204));

        removeFromBill.setBackground(new java.awt.Color(0, 0, 0));
        removeFromBill.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        removeFromBill.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        removeFromBill.setText("Remove");
        removeFromBill.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        removeFromBill.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        removeFromBill.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                removeFromBillMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                removeFromBillMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                removeFromBillMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(removeFromBill, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(removeFromBill, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
        );

        jPanel27.setBackground(new java.awt.Color(0, 153, 102));

        saveBill.setBackground(new java.awt.Color(0, 153, 102));
        saveBill.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        saveBill.setForeground(new java.awt.Color(255, 255, 255));
        saveBill.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        saveBill.setText("Save");
        saveBill.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        saveBill.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        saveBill.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveBillMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBillMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBillMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(saveBill, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(saveBill, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(49, Short.MAX_VALUE))
        );

        dateToday.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        dateToday.setText("DD/MM/YYYY");

        createNewBill.setBackground(new java.awt.Color(255, 255, 255));
        createNewBill.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        createNewBill.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                createNewBillMouseClicked(evt);
            }
        });

        billNo1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        billNo1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        billNo1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reload.png"))); // NOI18N
        billNo1.setText("New Bill");

        javax.swing.GroupLayout createNewBillLayout = new javax.swing.GroupLayout(createNewBill);
        createNewBill.setLayout(createNewBillLayout);
        createNewBillLayout.setHorizontalGroup(
            createNewBillLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(createNewBillLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(billNo1, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addContainerGap())
        );
        createNewBillLayout.setVerticalGroup(
            createNewBillLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(billNo1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout dashboardPanelLayout = new javax.swing.GroupLayout(dashboardPanel);
        dashboardPanel.setLayout(dashboardPanelLayout);
        dashboardPanelLayout.setHorizontalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dashboardPanelLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, dashboardPanelLayout.createSequentialGroup()
                        .addComponent(tabContentLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(dateToday, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(roleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fullnameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, dashboardPanelLayout.createSequentialGroup()
                        .addComponent(homeIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(settingIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(logoutButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(createNewBill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(67, 67, 67))
        );
        dashboardPanelLayout.setVerticalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dashboardPanelLayout.createSequentialGroup()
                        .addComponent(fullnameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roleLabel))
                    .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(tabContentLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dateToday, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(createNewBill, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(homeIcon)
                        .addComponent(settingIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(logoutButton)))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53))
        );

        content.add(dashboardPanel, "card2");

        settingPanel.setBackground(new java.awt.Color(255, 255, 255));

        tabContentLabel4.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        tabContentLabel4.setText("SETTINGS");

        logoutButton1.setBackground(new java.awt.Color(255, 255, 255));
        logoutButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        logoutButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blackLogout.png"))); // NOI18N
        logoutButton1.setText("Logout");
        logoutButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoutButton1.setIconTextGap(10);
        logoutButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutButton1MouseClicked(evt);
            }
        });

        homeIcon1.setBackground(new java.awt.Color(255, 255, 255));
        homeIcon1.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        homeIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/blackDashboard.png"))); // NOI18N
        homeIcon1.setText("Home");
        homeIcon1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homeIcon1.setIconTextGap(10);
        homeIcon1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                homeIcon1MouseClicked(evt);
            }
        });

        settingIcon1.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        settingIcon1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        settingIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/settings.png"))); // NOI18N
        settingIcon1.setText("Settings");
        settingIcon1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settingIcon1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingIcon1MouseClicked(evt);
            }
        });

        fullnameLabel1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        fullnameLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fullnameLabel1.setText("Full name");

        roleLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        roleLabel1.setForeground(new java.awt.Color(51, 51, 51));
        roleLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        roleLabel1.setText("(ROLE)");

        saveError.setBackground(new java.awt.Color(255, 0, 0));
        saveError.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        saveError.setForeground(new java.awt.Color(255, 0, 0));

        jLabel24.setBackground(new java.awt.Color(0, 0, 0));
        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel24.setText("Full Name");

        jPanel13.setBackground(new java.awt.Color(0, 0, 0));

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

        savedFullname.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        savedFullname.setForeground(new java.awt.Color(102, 102, 102));
        savedFullname.setText("Fullname");

        fullnameField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        fullnameField.setForeground(new java.awt.Color(50, 179, 96));
        fullnameField.setBorder(null);
        fullnameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullnameFieldActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel25.setText("Password");

        label51.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        label51.setForeground(new java.awt.Color(102, 102, 102));
        label51.setText("*********");

        jPanel15.setBackground(new java.awt.Color(0, 0, 0));

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

        passwordField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        passwordField.setForeground(new java.awt.Color(50, 179, 96));
        passwordField.setBorder(null);

        jLabel27.setBackground(new java.awt.Color(0, 0, 0));
        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel27.setText("Gender");

        genderMaleField.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(genderMaleField);
        genderMaleField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        genderMaleField.setText("Male");
        genderMaleField.setIconTextGap(7);

        genderFemaleField.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(genderFemaleField);
        genderFemaleField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        genderFemaleField.setText("Female");
        genderFemaleField.setIconTextGap(7);

        savedGender.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        savedGender.setForeground(new java.awt.Color(102, 102, 102));
        savedGender.setText("Gender");

        saveInfo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        saveInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        saveInfo.setText("Save");
        saveInfo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        saveInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        saveInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveInfoMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveInfoMouseEntered(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(50, 179, 96));
        jLabel26.setText("Note : Only fill the info you want to change.");

        javax.swing.GroupLayout settingPanelLayout = new javax.swing.GroupLayout(settingPanel);
        settingPanel.setLayout(settingPanelLayout);
        settingPanelLayout.setHorizontalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addComponent(tabContentLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(fullnameLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                                    .addComponent(roleLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addComponent(homeIcon1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(settingIcon1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(logoutButton1)))
                        .addGap(67, 67, 67))
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(settingPanelLayout.createSequentialGroup()
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(settingPanelLayout.createSequentialGroup()
                                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(fullnameField)
                                            .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(55, 55, 55)
                                        .addComponent(savedFullname, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(settingPanelLayout.createSequentialGroup()
                                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(55, 55, 55)
                                        .addComponent(label51, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(settingPanelLayout.createSequentialGroup()
                                        .addComponent(genderMaleField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(genderFemaleField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(savedGender, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(saveError, javax.swing.GroupLayout.PREFERRED_SIZE, 719, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(479, Short.MAX_VALUE))
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(saveInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        settingPanelLayout.setVerticalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(fullnameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(roleLabel1))
                    .addGroup(settingPanelLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(tabContentLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(homeIcon1)
                    .addComponent(settingIcon1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logoutButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveError, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addGap(38, 38, 38)
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(315, Short.MAX_VALUE))
        );

        content.add(settingPanel, "card2");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titleBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
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

    private void settingIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingIconMouseClicked
        // Switch tab
        dashboardPanel.setVisible(false);
        settingPanel.setVisible(true);
    }//GEN-LAST:event_settingIconMouseClicked

    private void logoutButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButtonMouseClicked
        LoginForm lgn = new LoginForm();
        lgn.setVisible(true);
        lgn.pack();
        this.dispose();
    }//GEN-LAST:event_logoutButtonMouseClicked

    private void homeIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeIconMouseClicked
        // Switch tab
        dashboardPanel.setVisible(true);
        settingPanel.setVisible(false);
    }//GEN-LAST:event_homeIconMouseClicked

    private void fullnameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullnameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fullnameFieldActionPerformed

    private void logoutButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutButton1MouseClicked
        LoginForm lgn = new LoginForm();
        lgn.setVisible(true);
        lgn.pack();
        this.dispose();
    }//GEN-LAST:event_logoutButton1MouseClicked

    private void settingIcon1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingIcon1MouseClicked
        // Switch tab
        dashboardPanel.setVisible(false);
        settingPanel.setVisible(true);
    }//GEN-LAST:event_settingIcon1MouseClicked

    private void homeIcon1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homeIcon1MouseClicked
        // Switch tab
        dashboardPanel.setVisible(true);
        settingPanel.setVisible(false);
    }//GEN-LAST:event_homeIcon1MouseClicked

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

        try{

            Connection con;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql:///newDB","root","");
            PreparedStatement p = null;

            //Starting validation

            if(!fname.isEmpty()) {
                if(fname.length()<5 || fname.length()>=30) // checking length of fullname
                saveError.setText("Fullname must be 5-30 characters long.");
                else if(!nameMatcher.matches()) // checking if the username matches the regex pattern
                saveError.setText("Full name can only contain spaces and alphabets.");
                else {
                    fname = fname.substring(0,1).toUpperCase() + fname.substring(1).toLowerCase(); //Autocapitalize
                    conditionCode += 1;
                }
            }
            if(!pwd.isEmpty()) {
                if(pwd.length()<5 || pwd.length()>=20) // checking length of password
                saveError.setText("Password must be 5-20 characters long.");
                else conditionCode += 2;
            }
            if(genderMaleField.isSelected() || genderFemaleField.isSelected()) {
                gndr = genderMaleField.isSelected()? "Male":"Female";
                conditionCode += 3;
            }

            /*
            0 - Error: nothing
            1 - Change name
            2 - Change password
            3 - Change gender
            6 - Change everything
            */
            switch(conditionCode) {
                case 1: p = con.prepareStatement("UPDATE userDBForJavaApp SET fullname = ? WHERE userid = ?");
                p.setString(1,fname);
                p.setString(2, userid);
                break;
                case 2: p = con.prepareStatement("UPDATE userDBForJavaApp SET password = ? WHERE userid = ?");
                p.setString(1,pwd);
                p.setString(2, userid);
                break;
                case 3: p = con.prepareStatement("UPDATE userDBForJavaApp SET gender = ? WHERE userid = ?");
                p.setString(1, gndr);
                p.setString(2, userid);
                break;
                case 6: p = con.prepareStatement("UPDATE userDBForJavaApp SET fullname = ?, password = ?,gender = ? WHERE userid = ?");
                p.setString(1, fname);
                p.setString(2, pwd);
                p.setString(3, gndr);
                p.setString(4, userid);
                break;
                default: catchFlag = false;
            }
            if(catchFlag) {
                int i = p.executeUpdate();
                if(i>0){
                    JOptionPane.showMessageDialog(null,"Details updated!");
                    fullnameField.setText("");
                    passwordField.setText("");
                    buttonGroup1.clearSelection();
                    saveError.setText("");
                }
                else
                JOptionPane.showMessageDialog(null,"Error updating data!");
            }

        }
        catch(Exception e)
        {
            saveError.setText("Some error occured! Please try again."+e);
        }
    }//GEN-LAST:event_saveInfoMouseClicked

    private void saveInfoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveInfoMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_saveInfoMouseEntered

    private void createNewBillMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createNewBillMouseClicked
        newBill();
    }//GEN-LAST:event_createNewBillMouseClicked

    private void customerNameInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerNameInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_customerNameInputActionPerformed

    private void customerContactInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerContactInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_customerContactInputActionPerformed

    private void searchProduct1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchProduct1ActionPerformed
    
    }//GEN-LAST:event_searchProduct1ActionPerformed

    private void searchProduct1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchProduct1KeyPressed
        if(evt.getExtendedKeyCode() == 10) {
            if(!searchProduct1.getText().isEmpty()) populateProductTable(searchProduct1.getText());
            else populateProductTable(null);
        }
        
    }//GEN-LAST:event_searchProduct1KeyPressed

    private void searchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseClicked
        if(!searchProduct1.getText().isEmpty()) populateProductTable(searchProduct1.getText());
        else populateProductTable(null);
    }//GEN-LAST:event_searchButtonMouseClicked

    private void searchButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_searchButtonMouseEntered

    private void searchButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_searchButtonMouseExited

    private void removeFromBillMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeFromBillMouseClicked
        removeFromTheTable();
    }//GEN-LAST:event_removeFromBillMouseClicked

    private void removeFromBillMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeFromBillMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_removeFromBillMouseEntered

    private void removeFromBillMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeFromBillMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_removeFromBillMouseExited

    private void addToBillMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addToBillMouseClicked
        addItemToBill();
    }//GEN-LAST:event_addToBillMouseClicked
    
    private void addToBillMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addToBillMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_addToBillMouseEntered

    private void addToBillMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addToBillMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_addToBillMouseExited

    private void quantityInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantityInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quantityInputActionPerformed

    private void quantityInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quantityInputKeyPressed
        if(evt.getKeyCode() == 10)
            addItemToBill();
    }//GEN-LAST:event_quantityInputKeyPressed

    private void saveBillMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBillMouseClicked
        saveBillToDB();
    }//GEN-LAST:event_saveBillMouseClicked

    private void saveBillMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBillMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_saveBillMouseEntered

    private void saveBillMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBillMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_saveBillMouseExited
  
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
            java.util.logging.Logger.getLogger(BillingDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BillingDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BillingDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BillingDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BillingDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addToBill;
    private javax.swing.JLabel billNo;
    private javax.swing.JLabel billNo1;
    private javax.swing.JLabel billNoLabel;
    private javax.swing.JLabel billNoLabel1;
    private javax.swing.JLabel billNoLabel2;
    private javax.swing.JTable billTable;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel close;
    private javax.swing.JPanel content;
    private javax.swing.JPanel createNewBill;
    private javax.swing.JTextField customerContactInput;
    private javax.swing.JTextField customerNameInput;
    private javax.swing.JPanel dashboardPanel;
    private javax.swing.JLabel dateToday;
    private javax.swing.JTextField fullnameField;
    private javax.swing.JLabel fullnameLabel;
    private javax.swing.JLabel fullnameLabel1;
    private javax.swing.JRadioButton genderFemaleField;
    private javax.swing.JRadioButton genderMaleField;
    private javax.swing.JLabel homeIcon;
    private javax.swing.JLabel homeIcon1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel label51;
    private javax.swing.JLabel logoutButton;
    private javax.swing.JLabel logoutButton1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel minimise;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTable productTable;
    private javax.swing.JTextField quantityInput;
    private javax.swing.JLabel removeFromBill;
    private javax.swing.JLabel roleLabel;
    private javax.swing.JLabel roleLabel1;
    private javax.swing.JLabel saveBill;
    private javax.swing.JLabel saveError;
    private javax.swing.JLabel saveInfo;
    private javax.swing.JLabel savedFullname;
    private javax.swing.JLabel savedGender;
    private javax.swing.JLabel searchButton;
    private javax.swing.JTextField searchProduct1;
    private javax.swing.JLabel settingIcon;
    private javax.swing.JLabel settingIcon1;
    private javax.swing.JPanel settingPanel;
    private javax.swing.JLabel tabContentLabel3;
    private javax.swing.JLabel tabContentLabel4;
    private javax.swing.JPanel titleBar;
    // End of variables declaration//GEN-END:variables

    private static class BillItems {
        
        String billId = null, userid = null, soldTo = null, contact = null, pId = null;
        int quantity;
        Double sellingPrice;
        // billId, userid, soldTo, contact, pId, sellingPrice, quantity;
        
        public BillItems(String b, String u, String s, String c, String p, Double sP, int q) {
            billId = b;
            userid = u;
            soldTo = s;
            contact = c;
            pId = p;
            sellingPrice = sP;
            quantity = q;
        }
    }
}
