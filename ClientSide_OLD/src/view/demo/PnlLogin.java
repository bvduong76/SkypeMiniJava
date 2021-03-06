/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view.demo;

import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import objects.FriendRequest;
import objects.User;
import objects.room.Room;
import socket.Client;
import socket.SocketMessageHandle;
import static view.demo.Program.client;
import static view.demo.Program.gridBag;
import static view.demo.Program.p2;
import static view.demo.PnlMain.allRoom;
import static view.demo.PnlMain.allUser;
import static view.demo.Program.friendRequests;

public class PnlLogin extends javax.swing.JPanel {

    /**
     * Creates new form Login
     */
    
    public PnlLogin() {
        initComponents();
        txtUsername_login.setBackground(new Color(0x0,true));
        txtPassword_login.setBackground(new Color(0x0,true));
        TextPrompt tpUsername = new TextPrompt("Username", txtUsername_login);
        tpUsername.setForeground(new Color(204,204,204));
        TextPrompt tpPassword = new TextPrompt("Password", txtPassword_login);
        tpPassword.setForeground(new Color(204,204,204));
        lblCreateAcc.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnLogin = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblCreateAcc = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(102, 102, 102));
        setPreferredSize(new java.awt.Dimension(683, 482));

        btnLogin.setBackground(new java.awt.Color(34, 77, 142));
        btnLogin.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("Sign In");
        btnLogin.setBorder(null);
        btnLogin.setBorderPainted(false);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        txtUsername_login.setForeground(new java.awt.Color(255, 255, 255));
        txtUsername_login.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/Pass.png"))); // NOI18N

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/User.png"))); // NOI18N

        txtPassword_login.setForeground(new java.awt.Color(255, 255, 255));
        txtPassword_login.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));

        lblCreateAcc.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        lblCreateAcc.setForeground(new java.awt.Color(255, 255, 255));
        lblCreateAcc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCreateAcc.setText("Create new account");
        lblCreateAcc.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCreateAccMouseClicked(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/Hello.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(299, 299, 299)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(192, 192, 192)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtUsername_login, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(192, 192, 192)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtPassword_login, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(272, 272, 272)
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(230, 230, 230)
                        .addComponent(lblCreateAcc, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(252, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUsername_login, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPassword_login, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblCreateAcc)
                .addContainerGap(132, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed

        System.out.println("Sending to server");
        String userName = txtUsername_login.getText();
        client.declareHandle("loginRs", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                User us = (User) params[0];
                if(us == null)
                    JOptionPane.showMessageDialog(null,"Username or password is not valid !!!");
                if(us != null){
                    byte[] imageArray = (byte[]) params[1];
                    PnlMain.allUser = (List<User>) params[2];
                    PnlMain.allRoom = (List<Room>) params[3];
                    friendRequests =  (List<FriendRequest>) params[4];
                    List<byte[]> imgARRAY = (List<byte[]>) params[6];
                    try {
                        
                        PnlLogin.this.setVisible(false);
                        Program.currentUser = us;
                        Program.currentAvatar = imageArray;
                        Program.currentFriends = us.getFriendList();
                        
                        File dirForAvatar = new File("./Avatar");
                        if (!dirForAvatar.exists()) 
                            dirForAvatar.mkdir();
                        for(int i = 0; i < imgARRAY.size(); i ++){
                            File dir = new File("./Avatar/"+allUser.get(i).getUserName());
                            if(!dir.exists()){
                                dir.mkdir();
                            }
                            String filePath = "./Avatar/"+ allUser.get(i).getUserName() + "/" + "avatar.jpg";
                            try {
                                Files.write(new File(filePath).toPath(), imgARRAY.get(i));
                            } catch (IOException ex) {
                                Logger.getLogger(PnlRegister.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        Program.p2 = new PnlMain();
                        Program.pnlDynamic.add(p2, gridBag);
                        Program.p2.setVisible(true);
                        
                        Program.menuHome.add(Program.itemSignout);
                    } catch (IOException ex) {
                        Logger.getLogger(PnlLogin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
         try {
            client.sendMessage("login",txtUsername_login.getText(),txtPassword_login.getPassword());
        } catch (IOException ex) {
            Logger.getLogger(PnlLogin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    private void lblCreateAccMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCreateAccMouseClicked
        // TODO add your handling code here:
        this.setVisible(false);
        Program.p3.setVisible(true);
    }//GEN-LAST:event_lblCreateAccMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblCreateAcc;
    public static final javax.swing.JPasswordField txtPassword_login = new javax.swing.JPasswordField();
    public static final javax.swing.JTextField txtUsername_login = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables
}
