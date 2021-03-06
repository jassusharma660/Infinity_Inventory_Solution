/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainPackage;

import java.awt.Color;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Jassu Sharma
 */
public class RegisterForm extends javax.swing.JFrame {
    int mouseX, mouseY;
    
    /**
     * Creates new form RegsiterForm
     */
    public RegisterForm() {
        initComponents();
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
        mainFrame = new javax.swing.JPanel();
        titleBar = new javax.swing.JPanel();
        close = new javax.swing.JLabel();
        minimise = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        registerPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        regError = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        usernameField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        passwordField = new javax.swing.JPasswordField();
        jLabel14 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        rpasswordField = new javax.swing.JPasswordField();
        jLabel13 = new javax.swing.JLabel();
        genderFemaleField = new javax.swing.JRadioButton();
        genderMaleField = new javax.swing.JRadioButton();
        jPanel12 = new javax.swing.JPanel();
        loginButton = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        registerButton = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        fullnameField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Infinity by Jassu Sharma | Register");
        setUndecorated(true);

        mainFrame.setBackground(new java.awt.Color(255, 255, 255));
        mainFrame.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153), 2));

        titleBar.setBackground(new java.awt.Color(255, 255, 255));
        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                titleBarMouseDragged(evt);
            }
        });
        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                titleBarMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                titleBarMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                titleBarMouseReleased(evt);
            }
        });
        titleBar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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
        titleBar.add(close, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 0, 46, -1));

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
        titleBar.add(minimise, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 0, 46, -1));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel1.setText("Infinity Business Solution Tool");
        titleBar.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 0, 220, 30));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/titleIcon.png"))); // NOI18N
        titleBar.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 40, 30));

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setLayout(new java.awt.CardLayout());

        registerPanel.setBackground(new java.awt.Color(255, 255, 255));
        registerPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel10.setText("Register your account");
        registerPanel.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 350, 51));

        regError.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        regError.setForeground(new java.awt.Color(232, 17, 35));
        registerPanel.add(regError, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, 320, 20));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel11.setText("Username");
        registerPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, 141, 25));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        usernameField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        usernameField.setForeground(new java.awt.Color(51, 51, 51));
        usernameField.setBorder(null);
        usernameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(usernameField)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(usernameField, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        registerPanel.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 200, 319, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel12.setText("Password");
        registerPanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 250, 141, 25));

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        passwordField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        passwordField.setForeground(new java.awt.Color(51, 51, 51));
        passwordField.setBorder(null);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        registerPanel.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 280, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel14.setText("Repeat Password");
        registerPanel.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 330, 141, 25));

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));
        jPanel14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        rpasswordField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        rpasswordField.setForeground(new java.awt.Color(51, 51, 51));
        rpasswordField.setBorder(null);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rpasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rpasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        registerPanel.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 360, -1, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel13.setText("Gender");
        registerPanel.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 410, 141, 25));

        buttonGroup1.add(genderFemaleField);
        genderFemaleField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        genderFemaleField.setText("Female");
        genderFemaleField.setIconTextGap(7);
        registerPanel.add(genderFemaleField, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 440, 90, -1));

        buttonGroup1.add(genderMaleField);
        genderMaleField.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        genderMaleField.setText("Male");
        genderMaleField.setIconTextGap(7);
        registerPanel.add(genderMaleField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 440, 80, -1));

        jPanel12.setBackground(new java.awt.Color(0, 0, 0));

        loginButton.setBackground(new java.awt.Color(0, 0, 0));
        loginButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        loginButton.setForeground(new java.awt.Color(255, 255, 255));
        loginButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loginButton.setText("Login");
        loginButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(221, 221, 221)));
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loginButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loginButton, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loginButton, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
        );

        registerPanel.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 480, -1, -1));

        jPanel13.setBackground(new java.awt.Color(204, 204, 204));

        registerButton.setBackground(new java.awt.Color(204, 204, 204));
        registerButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        registerButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        registerButton.setText("Register");
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                registerButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerButtonMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(registerButton, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(registerButton, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
        );

        registerPanel.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 480, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel15.setText("Full Name");
        registerPanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 100, 141, 25));

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        fullnameField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        fullnameField.setForeground(new java.awt.Color(51, 51, 51));
        fullnameField.setBorder(null);
        fullnameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullnameFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fullnameField)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fullnameField, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
        );

        registerPanel.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 130, 319, -1));

        mainPanel.add(registerPanel, "card3");

        javax.swing.GroupLayout mainFrameLayout = new javax.swing.GroupLayout(mainFrame);
        mainFrame.setLayout(mainFrameLayout);
        mainFrameLayout.setHorizontalGroup(
            mainFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titleBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        mainFrameLayout.setVerticalGroup(
            mainFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainFrameLayout.createSequentialGroup()
                .addComponent(titleBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void titleBarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMouseDragged
        int moveX, moveY;
        moveX = evt.getXOnScreen();
        moveY = evt.getYOnScreen();
        this.setLocation(moveX-mouseX, moveY-mouseY);
    }//GEN-LAST:event_titleBarMouseDragged

    private void titleBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMouseClicked

    }//GEN-LAST:event_titleBarMouseClicked

    private void titleBarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMousePressed
        mouseX = evt.getX();
        mouseY = evt.getY();
    }//GEN-LAST:event_titleBarMousePressed

    private void titleBarMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMouseReleased

    }//GEN-LAST:event_titleBarMouseReleased

    private void registerButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registerButtonMouseExited
        registerButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    }//GEN-LAST:event_registerButtonMouseExited

    private void registerButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registerButtonMouseEntered
        registerButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    }//GEN-LAST:event_registerButtonMouseEntered

    private void registerButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_registerButtonMouseClicked
        String username = usernameField.getText();
        String fullname = fullnameField.getText();
        
        String password = new String(passwordField.getPassword());
        String rpassword = new String(rpasswordField.getPassword());
        Boolean validated = false; // Validation failed by default
        String regex = "[a-zA-Z0-9_]+";
        String name_regex = "[a-zA-Z\\s]+";
        Pattern pattern = Pattern.compile(regex);
                
        Matcher matcher = pattern.matcher(username);
        
        try{
            Connection con;
            Boolean adminExist = null;
            
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql:///newDB","root","");
            //Starting validation

            if(fullname.isEmpty() || username.isEmpty() || password.isEmpty()) //Checking if fullname, username and password is empty
            regError.setText("Fullname, Username or Password can not be empty.");
            else if(fullname.length()<5 || fullname.length()>=30) // checking length of fullname
            regError.setText("Fullname must be 5-30 characters long.");
            else if(!fullname.matches(name_regex)) // checking if the username matches the regex pattern
            regError.setText("Full name can only contain spaces and alphabets.");
            else if(username.length()<5 || username.length()>=20) // checking length of username
            regError.setText("Username must be 5-20 characters long.");
            else if(!matcher.matches()) // checking if the username matches the regex pattern
            regError.setText("Username can only contain Alphanumric and _ ");
            else if(password.length()<5 || password.length()>=20) // checking length of password
            regError.setText("Password must be 5-20 characters long.");
            else if(!password.equals(rpassword)) // checking repeat password and password
            regError.setText("Password and repeat password must match.");
            else if(!genderMaleField.isSelected() && !genderFemaleField.isSelected())
            regError.setText("Gender is required!");
            else
            {
                // CHECK IF THE USERNAME EXIST
                Statement chkUser = con.createStatement();
                String sql = "SELECT userid FROM userDBForJavaApp WHERE userid='"+username+"'";

                ResultSet rs = chkUser.executeQuery(sql);
                if(rs.next())
                    regError.setText("Username already exists!");
                else {
                    validated = true; //True indicates that the form is successfully validated
                    regError.setText("");
                }
                sql = "SELECT userid FROM userDBForJavaApp WHERE role='ADMIN'";
                rs = chkUser.executeQuery(sql);
                adminExist = rs.next();
            }

            if(validated) {
                // Enter the data into database
                String gender = genderMaleField.isSelected()? "Male":"Female";
                String role = adminExist?"NO":"ADMIN";
                fullname = fullname.substring(0,1).toUpperCase() + fullname.substring(1).toLowerCase(); //Autocapitalize
               
                PreparedStatement p = con.prepareStatement("INSERT INTO userDBForJavaApp VALUES(?,?,?,?,?)");
                p.setString(1, username);
                p.setString(2, fullname);
                p.setString(3, password);
                p.setString(4, gender);                
                p.setString(5, role);

                int i = p.executeUpdate();
                if(i>0){
                    //Now show sucess message and switch back to login screen
                    JOptionPane.showMessageDialog(null, "Success! Please login now.");
                    LoginForm lgn = new LoginForm();
                    lgn.setVisible(true);
                    lgn.pack();
                    this.dispose();
                }
                else
                JOptionPane.showMessageDialog(null,"Error registring user!");

            }
        }
        catch(ClassNotFoundException e)
        {
            regError.setText("Some error occured! Please try again.");
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,"Error registring user!");
        }
    }//GEN-LAST:event_registerButtonMouseClicked

    private void loginButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginButtonMouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_loginButtonMouseExited

    private void loginButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginButtonMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_loginButtonMouseEntered

    private void loginButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginButtonMouseClicked
        // TODO add your handling code here:
        LoginForm lgn = new LoginForm();
        lgn.setVisible(true);
        lgn.pack();
        this.dispose();
    }//GEN-LAST:event_loginButtonMouseClicked

    private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameFieldActionPerformed

    private void fullnameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullnameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fullnameFieldActionPerformed

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
            java.util.logging.Logger.getLogger(RegisterForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegisterForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegisterForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegisterForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RegisterForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel close;
    private javax.swing.JTextField fullnameField;
    private javax.swing.JRadioButton genderFemaleField;
    private javax.swing.JRadioButton genderMaleField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel loginButton;
    private javax.swing.JPanel mainFrame;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel minimise;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel regError;
    private javax.swing.JLabel registerButton;
    private javax.swing.JPanel registerPanel;
    private javax.swing.JPasswordField rpasswordField;
    private javax.swing.JPanel titleBar;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
