/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view.demo;

import audio.AudioReceiver;
import audio.AudioCaller;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import objects.FriendRequest;
import objects.User;
import objects.messages.Message;
import objects.room.GroupRoom;
import objects.room.Room;
import objects.room.TwoUserRoom;
import socket.SocketMessageHandle;
import static view.demo.Program.client;
import static view.demo.Program.currentUser;
import static view.demo.Program.currentFriends;
import static view.demo.Program.friendRequests;

public class PnlMain extends javax.swing.JPanel {

    private Map<String, ImageIcon> imageMap = new HashMap<>();
    DefaultListModel<String> modelFriends = new DefaultListModel<>();
    DefaultListModel<String> modelAllUser = new DefaultListModel<>();
    DefaultListModel<String> modelAllRequest = new DefaultListModel<>();
    DefaultListModel<Message> modelChat = new DefaultListModel<>();
    DefaultListModel<String> modelGroup = new DefaultListModel<>();
    static DefaultListModel<String> modelMember = new DefaultListModel<>();

    static List<User> allUser;
    static List<Room> allRoom;
    private List<GroupRoom> allGroupRoom = new ArrayList<>();
    static Map<String, Room> twoUserRoomMap = new HashMap<>();
    static Map<String, GroupRoom> groupRoomMap = new HashMap<>();

    private String friendFile = null;

    public User selectedFriend;
    static Room selectedRoom;
    private byte[] temp;
    AudioCaller audioServer;
    AudioReceiver audioClient;

    public PnlMain() throws IOException {

        initComponents();
        lblName.setCursor(new Cursor(Cursor.HAND_CURSOR));
        customUser();
        // Truyền dữ liệu cho cái model từ các list tương ứng 
        modelFriends = createModelFriends(Program.currentUser.getFriendList());
        modelAllUser = createmodelAllUser(allUser);
        modelAllRequest = createModelRequest(friendRequests);
        modelGroup = createGroupModel(allRoom);

        // Set lần lượt từng model cho các list
        listFriends.setModel(modelFriends);
        listFriends.setCellRenderer(new CustomFriendList());
        lstRequest.setModel(modelAllRequest);
        lstRequest.setCellRenderer(new CustomFriendList());
        listGroup.setModel(modelGroup);
        listGroup.setCellRenderer(new CustomGroupList());
        listChatBox.setModel(modelChat);
        listChatBox.setCellRenderer(new ChatListCellRender());

//        if (allRoom.size() > 0) {
//            selectedRoom = allRoom.get(0);
//            loadMessages(selectedRoom);
//        }
        for (Room r : allRoom) {
            if (r.getType().equals("PRIVATE")) {
                TwoUserRoom room = (TwoUserRoom) r;
                twoUserRoomMap.put(room.getPeerUser().getUserName(), r);
            }
        }

        if (Program.currentUser.getFriendList().size() > 0) {
            listFriends.setSelectedIndex(0);
            lblFriendAva.setIcon(imageMap.get(Program.currentUser.getFriendList().get(0).getUserName()));
            lblFriendName.setText(Program.currentUser.getFriendList().get(0).getUserName());
            btnAddFriend.setEnabled(false);
            selectedRoom = twoUserRoomMap.get(Program.currentUser.getFriendList().get(0).getUserName());
            loadMessages(selectedRoom);
            setStatus();
        }

        client.declareHandle("clear", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                String roomId = (String) params[0];
                for (Room r : allRoom) {
                    if (r.getRoomId().equals(roomId)) {
                        r.getListMessage().clear();
                        break;
                    }
                }
                Room r = groupRoomMap.get(roomId);
                if (r != null) {
                    r.getListMessage().clear();
                }

                if (roomId.equals(selectedRoom.getRoomId())) {
                    modelChat.clear();
                }
            }
        });

        client.declareHandle("addMemRS", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                Room room = (Room) params[0];
                modelGroup.addElement(((GroupRoom) room).getTitle());
                allGroupRoom.add((GroupRoom) room);
                groupRoomMap.put(room.getRoomId(), (GroupRoom) room);
            }
        });

        client.declareHandle("sendPrivateMsg", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                Message msg = (Message) params[0];
                Room room = twoUserRoomMap.get(msg.getUserName());
                room.addMessage(msg);
                if (room == selectedRoom) {
                    modelChat.addElement(msg);
                }
            }
        });

        client.declareHandle("sendGroupMsg", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                Message msg = (Message) params[0];
                String roomId = (String) params[1];
                Room room = groupRoomMap.get(roomId);
                room.addMessage(msg);
                if (room == selectedRoom) {
                    modelChat.addElement(msg);
                }
            }
        });

        client.declareHandle("acceptRS", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                User friend = (User) params[0];
                TwoUserRoom room = (TwoUserRoom) params[1];
                room.setListMessage(new ArrayList<>());
                room.setPeerUser(friend);
                friend.printUser();
                try {
                    addToModel(friend, modelFriends);
                    Program.currentUser.getFriendList().add(friend);
                    removeFromRequest(friend.getUserName(), modelAllRequest);
                    allRoom.add(room);
                    twoUserRoomMap.put(friend.getUserName(), room);

                } catch (IOException ex) {
                    Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        client.declareHandle("friend_request", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                String friendUsername = (String) params[0];
                FriendRequest friendRequest = new FriendRequest(friendUsername, currentUser.getUserName());

                modelAllRequest.addElement(friendUsername);
                friendRequests.add(friendRequest);
            }
        });

        client.declareHandle("rejectRS", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                String friendUsername = (String) params[0];
                removeFromRequest(friendUsername, modelAllRequest);
            }
        });
        client.declareHandle("getMems", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                List<User> lst = (List<User>) params[0];
                if (lst != null) {
                    try {
                        modelMember = createModelFriends(lst);
                    } catch (IOException ex) {
                        Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    MemOfRoom.members.setModel(modelMember);
                    MemOfRoom.members.setCellRenderer(new CustomFriendList());
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            new MemOfRoom().setVisible(true);
                        }
                    });
                }
            }
        });
        client.declareHandle("statusChangeRS", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                String userChange = (String) params[0];
                String stt = (String) params[1];
                System.out.println(currentUser.getUserName() + ":" + userChange + " change status to " + stt);

                setStatus();

            }
        });

        client.declareHandle("haveAFile", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {

                String username = (String) params[0];
                int reply = JOptionPane.showConfirmDialog(null, "You have a file from " + username, "File Transfer", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    try {
                        client.sendMessage("haveAFileRS", "Yes");
                    } catch (IOException ex) {
                        Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        client.sendMessage("haveAFileRS", "No");
                    } catch (IOException ex) {
                        Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        client.declareHandle("getStatusRS", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                String status = (String) params[0];
                System.out.println("Get status: " + status);
                ImageIcon newIcon = new ImageIcon("src/view/Images/" + status + ".png");
                lblStatus.setIcon(newIcon);
                lblStatus.setText(status);

            }
        });

        client.declareHandle("call", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                try {
                    audioClient = new AudioReceiver((String) params[0]);
                } catch (IOException ex) {
                    Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        client.declareHandle("endCall", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                if (audioServer != null) {
                    audioServer.stop();
                    audioServer = null;
                }

                if (audioClient != null) {
                    audioClient.stop();
                    audioClient = null;
                }
            }
        });

    }

    private void setStatus() {

        String username = lblFriendName.getText();
        try {
            client.sendMessage("getStatus", username);
        } catch (IOException ex) {
            Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Tao model va put vao groupMap, allGroupRoom
    public DefaultListModel createGroupModel(List<Room> rooms) {
        if (rooms != null || rooms.size() > 0) {
            for (Room room : rooms) {
                if (room.getType().equals("GROUP")) {
                    GroupRoom roomGroup = (GroupRoom) room;
                    modelGroup.addElement(roomGroup.getTitle());
                    groupRoomMap.put(roomGroup.getRoomId(), roomGroup);
                    allGroupRoom.add(roomGroup);
                }
            }
        }
        return modelGroup;
    }

    // remove currentUser ra khỏi list allUser để không hiện thị user hiên tại trong list tìm kiếm
    public void removeCurrentUser() {
        for (User aUser : allUser) {
            if (aUser.getUserName().equals(currentUser.getUserName())) {
                allUser.remove(aUser);
                break;
            }
        }
    }

    // Lấy ví trị của friendname request trong list request
    public int getIdxFromRequestName(List<FriendRequest> fr, String friendname) {
        for (int i = 0; i < fr.size(); i++) {
            if (fr.get(i).getUsername().equals(friendname)) {
                return i;
            }
        }
        return -1;
    }

    public void removeFromRequest(String friendname, DefaultListModel model) {
        int idx = getIdxFromRequestName(friendRequests, friendname);
        if (idx >= 0) {
            model.removeElementAt(idx);
            friendRequests.remove(idx);
        }
    }

    // thêm một username vào model của  friend list
    public void addToModel(User aUser, DefaultListModel model) throws IOException {
        model.addElement(aUser.getUserName());
    }

    // tạo model add tất cả các user vào 
    // tạo imapeMap là map chứa hình của tất cả các user và được lấy theo key là username của user đó
    public DefaultListModel createmodelAllUser(List<User> users) throws IOException {
        DefaultListModel model = new DefaultListModel();
        if (users.size() > 0) {
            for (User aUser : users) {
                model.addElement(aUser.getUserName());
                BufferedImage masked = roundedImageLb("./Avatar/" + aUser.getUserName() + "/avatar.jpg");
                imageMap.put(aUser.getUserName(), new ImageIcon(masked.getScaledInstance(40, 40, masked.SCALE_SMOOTH)));
            }
        }
        return model;
    }

    // tạo model cho friendlist
    public DefaultListModel<String> createModelFriends(List<User> users) throws IOException {
        DefaultListModel<String> model = new DefaultListModel<>();
        if (users.size() > 0) {
            for (User aUser : users) {
                model.addElement(aUser.getUserName());
            }
        }
        return model;
    }

    // tạo model cho friendRequest
    public DefaultListModel createModelRequest(List<FriendRequest> friendRequests) throws IOException {
        DefaultListModel model = new DefaultListModel();
        if (friendRequests.size() > 0) {
            for (FriendRequest aFriendRequest : friendRequests) {
                model.addElement(aFriendRequest.getUsername());
            }
        }
        return model;
    }

    private class ChatListCellRender implements ListCellRenderer<Message> {

        Font font = new Font("helvitica", Font.BOLD, 14);

        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index, boolean isSelected, boolean cellHasFocus) {
            if (PnlMain.this.selectedRoom == null) {
                return null;
            }
            int max_width = 100;
            int max_heigh = 100;
            JPanel result = new JPanel(new BorderLayout());
            if (value.getUserName().equals(currentUser.getUserName())) {
                try {
                    BufferedImage masked = roundedImageLb("./Avatar/" + currentUser.getUserName() + "/avatar.jpg");
                    JLabel labelAva = new JLabel(new ImageIcon(masked.getScaledInstance(20, 20, masked.SCALE_SMOOTH)));
                    JLabel textLabel = null;
                    if (value.getType().equals("STRING")) {
                        textLabel = new JLabel(value.getContent() + " :" + value.getUserName(), SwingConstants.RIGHT);
                        textLabel.setFont(font);
                        result.add(labelAva, BorderLayout.EAST);
                        result.add(textLabel, BorderLayout.CENTER);
                    } else if (value.getType().equals("IMAGE")) {

                        byte[] imgArray = Base64.getDecoder().decode(value.getContent());

                        InputStream in = new ByteArrayInputStream(imgArray);
                        BufferedImage masked1 = ImageIO.read(in);
                        textLabel = new JLabel("  ", SwingConstants.RIGHT);

                        float scale1 = masked1.getWidth() / (float) max_width;
                        float scale2 = masked1.getHeight() / (float) max_heigh;
                        float scale = scale1 > scale2 ? scale1 : scale2;

                        if (scale > 1) {
                            textLabel.setIcon(new ImageIcon(masked1.getScaledInstance((int) (masked1.getWidth() / scale), (int) (masked1.getHeight() / scale), masked.SCALE_SMOOTH)));
                        } else {
                            textLabel.setIcon(new ImageIcon(masked1.getScaledInstance(masked1.getWidth(), masked1.getHeight(), masked.SCALE_SMOOTH)));
                        }
                        result.add(labelAva, BorderLayout.EAST);
                        result.add(textLabel, BorderLayout.CENTER);
                    } else if (value.getType().equals("FILE")) {
                        textLabel = new JLabel(value.getContent() + "  <-- FILE :" + value.getUserName(), SwingConstants.RIGHT);
                        textLabel.setFont(font);
                        textLabel.setForeground(Color.BLUE);
                        result.add(labelAva, BorderLayout.EAST);
                        result.add(textLabel, BorderLayout.CENTER);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    BufferedImage masked = roundedImageLb("./Avatar/" + value.getUserName() + "/avatar.jpg");
                    JLabel labelAva = new JLabel(new ImageIcon(masked.getScaledInstance(20, 20, masked.SCALE_SMOOTH)));
                    JLabel textLabel = null;
                    if (value.getType().equals("STRING")) {
                        textLabel = new JLabel(value.getUserName() + " :" + value.getContent(), SwingConstants.LEFT);
                        textLabel.setFont(font);
                        result.add(labelAva, BorderLayout.WEST);
                        result.add(textLabel, BorderLayout.CENTER);
                    } else if (value.getType().equals("IMAGE")) {

                        byte[] imgArray = Base64.getDecoder().decode(value.getContent());

                        InputStream in = new ByteArrayInputStream(imgArray);
                        BufferedImage masked1 = ImageIO.read(in);
                        textLabel = new JLabel("  ", SwingConstants.LEFT);

                        float scale1 = masked1.getWidth() / (float) max_width;
                        float scale2 = masked1.getHeight() / (float) max_heigh;
                        float scale = scale1 > scale2 ? scale1 : scale2;

                        if (scale > 1) {
                            textLabel.setIcon(new ImageIcon(masked1.getScaledInstance((int) (masked1.getWidth() / scale), (int) (masked1.getHeight() / scale), masked.SCALE_SMOOTH)));
                        } else {
                            textLabel.setIcon(new ImageIcon(masked1.getScaledInstance(masked1.getWidth(), masked1.getHeight(), masked.SCALE_SMOOTH)));
                        }
                        result.add(labelAva, BorderLayout.WEST);
                        result.add(textLabel, BorderLayout.CENTER);
                    } else if (value.getType().equals("FILE")) {
                        textLabel = new JLabel(value.getUserName() + ": FILE --> " + value.getContent(), SwingConstants.LEFT);
                        textLabel.setFont(font);
                        textLabel.setForeground(Color.BLUE);
                        result.add(labelAva, BorderLayout.WEST);
                        result.add(textLabel, BorderLayout.CENTER);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return result;
        }
    }

    // Tạo render cho các friendlist
    public class CustomFriendList extends DefaultListCellRenderer {

        Font font = new Font("helvitica", Font.BOLD, 24);

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setIcon(imageMap.get((String) value));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            return label;
        }
    }

    public class CustomGroupList extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            BufferedImage masked = null;
            try {
                masked = roundedImageLb("src/view/Images/Group-icon.png");
            } catch (IOException ex) {
                Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            label.setIcon(new ImageIcon(masked.getScaledInstance(40, 40, masked.SCALE_SMOOTH)));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            return label;
        }
    }

    // custom 1 số component cũng như thực hiện load hình cho user khi khởi tạo lúc vào màn hình chính
    public void customUser() throws IOException {
        if (Program.currentUser != null && Program.currentAvatar != null) {
            String filePath = "./Avatar/" + Program.currentUser.getUserName() + "/" + "avatar.jpg";
            BufferedImage masked = roundedImageLb(filePath);
            lblAvatar.setIcon(new ImageIcon(masked.getScaledInstance(60, 60, masked.SCALE_SMOOTH)));
            lblName.setText(Program.currentUser.getUserName());

            TextPrompt temp = new TextPrompt("Type your message", txtInPutField);
            txtInPutField.setForeground(new Color(0, 0, 0));
            TextPrompt tpSearch = new TextPrompt("Search", txtSearch);
            tpSearch.setForeground(new Color(51, 153, 255));
        }
    }

    // custom ảnh tròn
    public BufferedImage roundedImageLb(String filename) throws IOException {
        BufferedImage master = ImageIO.read(new File(filename));

        int diameter = Math.min(master.getWidth(), master.getHeight());
        BufferedImage mask = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = mask.createGraphics();
        applyQualityRenderingHints(g2d);
        g2d.fillOval(0, 0, diameter - 1, diameter - 1);
        g2d.dispose();

        BufferedImage masked = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        g2d = masked.createGraphics();
        applyQualityRenderingHints(g2d);
        int x = (diameter - master.getWidth()) / 2;
        int y = (diameter - master.getHeight()) / 2;
        g2d.drawImage(master, x, y, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
        g2d.drawImage(mask, 0, 0, null);
        g2d.dispose();

        return masked;
    }

    public void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        pnlContainer = new javax.swing.JPanel();
        lblFriendAva = new javax.swing.JLabel();
        lblFriendName = new javax.swing.JLabel();
        btnAddFriend = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listChatBox = new javax.swing.JList();
        btnAccept = new javax.swing.JButton();
        btnReject = new javax.swing.JButton();
        btnAddMem = new javax.swing.JButton();
        btnCall = new javax.swing.JButton();
        btnDecline = new javax.swing.JButton();
        btnLeave = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        lblStatus = new javax.swing.JLabel();
        status = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listFriends = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        listGroup = new javax.swing.JList<>();
        btnNewGroup = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstRequest = new javax.swing.JList<>();
        AddImage = new javax.swing.JButton();
        AddFile = new javax.swing.JButton();
        txtInPutField = new javax.swing.JTextField();

        setBackground(new java.awt.Color(204, 204, 204));

        lblAvatar.setBackground(new java.awt.Color(204, 255, 255));
        lblAvatar.setMaximumSize(new java.awt.Dimension(80, 80));
        lblAvatar.setMinimumSize(new java.awt.Dimension(80, 80));
        lblAvatar.setPreferredSize(new java.awt.Dimension(80, 80));

        lblName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblName.setForeground(new java.awt.Color(0, 0, 204));
        lblName.setText("Your name");
        lblName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNameMouseClicked(evt);
            }
        });

        txtSearch.setBackground(new Color(0,0,0,0));
        txtSearch.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(255, 255, 255)));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/search1.png"))); // NOI18N
        btnSearch.setEnabled(false);
        btnSearch.setBackground(new Color(0x0,true));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        pnlContainer.setBackground(new java.awt.Color(255, 255, 255));

        lblFriendAva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/User-small-icon.png"))); // NOI18N

        lblFriendName.setFont(new java.awt.Font("Times New Roman", 1, 17)); // NOI18N
        lblFriendName.setForeground(new java.awt.Color(0, 0, 153));
        lblFriendName.setText("Friend");
        lblFriendName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFriendNameMouseClicked(evt);
            }
        });

        btnAddFriend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/user-add-icon.png"))); // NOI18N
        btnAddFriend.setToolTipText("Add friend");
        btnAddFriend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFriendActionPerformed(evt);
            }
        });

        listChatBox.setBackground(new java.awt.Color(204, 204, 204));
        listChatBox.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listChatBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listChatBoxMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(listChatBox);

        btnAccept.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        btnAccept.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/Accept-icon.png"))); // NOI18N
        btnAccept.setText("Accept");
        btnAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAcceptActionPerformed(evt);
            }
        });

        btnReject.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        btnReject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/Button-Close-icon.png"))); // NOI18N
        btnReject.setText("Reject");
        btnReject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRejectActionPerformed(evt);
            }
        });

        btnAddMem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/Actions-list-add-user-icon.png"))); // NOI18N
        btnAddMem.setToolTipText("Add Member");
        btnAddMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMemActionPerformed(evt);
            }
        });

        btnCall.setBackground(new java.awt.Color(255, 255, 255));
        btnCall.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/phone-call.png"))); // NOI18N
        btnCall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCallActionPerformed(evt);
            }
        });

        btnDecline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/phone-call-reject-icon.png"))); // NOI18N
        btnDecline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeclineActionPerformed(evt);
            }
        });

        btnLeave.setFont(new java.awt.Font("Tahoma", 3, 11)); // NOI18N
        btnLeave.setText("Leave ");
        btnLeave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLeaveActionPerformed(evt);
            }
        });

        btnClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/Actions-edit-clear-icon.png"))); // NOI18N
        btnClear.setToolTipText("clear Chat History");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        lblStatus.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblStatus.setForeground(new java.awt.Color(0, 0, 255));
        lblStatus.setText("Status");

        javax.swing.GroupLayout pnlContainerLayout = new javax.swing.GroupLayout(pnlContainer);
        pnlContainer.setLayout(pnlContainerLayout);
        pnlContainerLayout.setHorizontalGroup(
            pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContainerLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContainerLayout.createSequentialGroup()
                .addGroup(pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlContainerLayout.createSequentialGroup()
                        .addComponent(lblFriendAva, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAccept)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnReject))
                    .addGroup(pnlContainerLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlContainerLayout.createSequentialGroup()
                                .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlContainerLayout.createSequentialGroup()
                                .addComponent(lblFriendName, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addComponent(btnCall, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDecline, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLeave, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlContainerLayout.createSequentialGroup()
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddMem, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAddFriend, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(52, 52, 52))
        );
        pnlContainerLayout.setVerticalGroup(
            pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContainerLayout.createSequentialGroup()
                .addGroup(pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlContainerLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAccept)
                            .addComponent(btnReject)
                            .addComponent(btnLeave, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblFriendAva, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnAddMem)
                        .addComponent(btnAddFriend, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlContainerLayout.createSequentialGroup()
                        .addComponent(lblFriendName, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblStatus))
                    .addComponent(btnCall)
                    .addComponent(btnDecline)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        status.setForeground(new java.awt.Color(51, 51, 51));
        status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Online", "Away", "Invisible" }));
        status.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        listFriends.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listFriendsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(listFriends);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Friends", jPanel2);

        listGroup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listGroupMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(listGroup);

        btnNewGroup.setText("New Group");
        btnNewGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewGroupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(74, 74, 74)
                .addComponent(btnNewGroup)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNewGroup)
                .addGap(0, 0, 0))
        );

        jTabbedPane1.addTab("Group", jPanel3);

        lstRequest.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstRequestMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstRequest);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Request", jPanel1);

        AddImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/Apps-Gallery-icon.png"))); // NOI18N
        AddImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddImageActionPerformed(evt);
            }
        });

        AddFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/add-file-icon.png"))); // NOI18N
        AddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddFileActionPerformed(evt);
            }
        });

        txtInPutField.setForeground(new java.awt.Color(51, 153, 255));
        txtInPutField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtInPutFieldKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(lblAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(lblName))
                            .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(AddImage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AddFile, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(txtInPutField, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(pnlContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(AddImage)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtInPutField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(AddFile, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addComponent(lblName)
                                .addGap(6, 6, 6)
                                .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(23, 23, 23)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        if (txtSearch.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "You need to enter information to search !!!");
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        if (!txtSearch.getText().equals("")) {
            btnSearch.setIcon(new ImageIcon(getClass().getResource("/view/Images/search2.png")));
            btnSearch.setEnabled(true);
            btnAddFriend.setVisible(true);
            btnAccept.setVisible(false);
            btnReject.setVisible(false);

            lstRequest.setModel(modelAllUser);
            if (modelAllUser.indexOf(txtSearch.getText()) != -1) {
                lstRequest.setSelectedIndex(modelAllUser.indexOf(txtSearch.getText()));
                int index = modelAllUser.indexOf((String) txtSearch.getText());
                int num;
                if (index + 500 > allUser.size()) {
                    num = allUser.size() - index - 1;
                } else {
                    num = 500;
                }
                lstRequest.getFixedCellHeight();
//                lstRequest.scrollRectToVisible(listFriends.getCellBounds(index, index + num));
                lstRequest.ensureIndexIsVisible(index);
                lstRequest.setSelectedIndex(index);
                lstRequest.getFirstVisibleIndex();
            }

        } else {
            btnSearch.setIcon(new ImageIcon(getClass().getResource("/view/Images/search1.png")));
            btnSearch.setEnabled(false);
            btnAddFriend.setVisible(false);
            lstRequest.setModel(modelAllRequest);
        }
    }//GEN-LAST:event_txtSearchKeyReleased

    // Kiểm tra 1 user có phải là bạn của user chính hay chưa 
    public boolean isFriend(List<User> users, User aFriend) {
        if (users.size() > 0) {
            for (User user : users) {
                if (user.getUserName().equals(aFriend.getUserName())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    // Kiếm tra đã gửi request cho User này hay chưa 
    public boolean isSendRQ(List<FriendRequest> fr, String friendName) {
        if (fr.size() > 0) {
            for (int i = 0; i < fr.size(); i++) {
                if (fr.get(i).getUsername().equals(friendName)) {
                    return true; // đã gửi rồi
                }
            }
            return false;
        }
        return false;
    }

    private void listFriendsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listFriendsMouseClicked
        int idx = listFriends.getSelectedIndex();

        if (idx >= 0) {
            if (listFriends.getModel() == modelFriends) {
                selectedFriend = currentFriends.get(idx);
                btnAddFriend.setVisible(false);
                selectedRoom = twoUserRoomMap.get(selectedFriend.getUserName());
                loadMessages(selectedRoom);
            } else {
                selectedFriend = allUser.get(idx);
                if (!isFriend(currentFriends, selectedFriend)) {
                    btnAddFriend.setVisible(true);
                }
            }
            lblFriendAva.setIcon(imageMap.get(selectedFriend.getUserName()));
            lblFriendName.setText(selectedFriend.getUserName());
            setStatus();
        }
    }//GEN-LAST:event_listFriendsMouseClicked

    private void loadMessages(Room room) {
        modelChat.clear();
        List<Message> listMsg = room.getListMessage();
        if (listMsg == null) {
            System.out.println("list null");
        } else {
            for (Message msg : listMsg) {
                modelChat.add(0, msg);
            }
        }
    }

    private void txtInPutFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtInPutFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!txtInPutField.getText().equals("")) {
                if (selectedRoom.getType().equals("PRIVATE")) {
                    try {
                        Message newMsg = new Message(UUID.randomUUID().toString(), currentUser.getUserName(), getCurrentTime(), "STRING", txtInPutField.getText());
                        PnlMain.this.selectedRoom.addMessage(newMsg);
                        modelChat.addElement(newMsg);
                        client.sendMessage("sendPrivateMsg", newMsg, selectedRoom.getRoomId(), ((TwoUserRoom) selectedRoom).getPeerUser().getUserName());
                        System.out.println(((TwoUserRoom) selectedRoom).getPeerUser().getUserName());
                        txtInPutField.setText("");
                    } catch (IOException ex) {
                        Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        Message newMsg = new Message(UUID.randomUUID().toString(), currentUser.getUserName(), getCurrentTime(), "STRING", txtInPutField.getText());
                        client.sendMessage("sendGroupMsg", newMsg, selectedRoom.getRoomId());
                        txtInPutField.setText("");
                    } catch (IOException ex) {
                        Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_txtInPutFieldKeyPressed

    private void btnAddFriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFriendActionPerformed
        if (selectedFriend != null) {
            btnAddFriend.setEnabled(false);
            client.declareHandle("requestRS", new SocketMessageHandle() {
                @Override
                public void Handle(Object... params) {
                    boolean res = (boolean) params[0];
                }
            });
            try {
                client.sendMessage("sendRequest", currentUser.getUserName(), selectedFriend.getUserName());
            } catch (IOException ex) {
                Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnAddFriendActionPerformed

    // Lấy thời gian hiện tại để gửi cùng message
    public String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    private void AddImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddImageActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Insert Image");

        int choose = -1;
        JPanel p = new JPanel();
        p.add(fc);
        choose = fc.showOpenDialog(p);
        byte[] imgArray = null;
        if (choose == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fc.getSelectedFile();
                if (selectedFile != null) {
                    temp = Files.readAllBytes(selectedFile.toPath());
                    imgArray = Files.readAllBytes(selectedFile.toPath());
                    String contentStr = Base64.getEncoder().encodeToString(imgArray);
                    Message newMsg = new Message(UUID.randomUUID().toString(), currentUser.getUserName(), getCurrentTime(), "IMAGE", contentStr);
                    if (selectedRoom.getType().equals("PRIVATE")) {
                        client.sendMessage("sendPrivateMsg", newMsg, selectedRoom.getRoomId(), ((TwoUserRoom) selectedRoom).getPeerUser().getUserName());
                        selectedRoom.addMessage(newMsg);
                        modelChat.addElement(newMsg);
                    } else {
                        client.sendMessage("sendGroupMsg", newMsg, selectedRoom.getRoomId(), ((GroupRoom) selectedRoom).getListUser(), currentUser.getUserName());
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(PnlRegister.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_AddImageActionPerformed

    public User getUserFromName(List<User> allUser, String username) {
        for (User user : allUser) {
            if (user.getUserName().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private void lstRequestMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstRequestMouseClicked
        int idx = lstRequest.getSelectedIndex();

        if (idx >= 0) {
            if (lstRequest.getModel() == modelAllRequest) {
                String friendName = friendRequests.get(idx).getUsername();
                selectedFriend = getUserFromName(allUser, friendName);
                btnAddFriend.setVisible(false);
                btnAccept.setVisible(true);
                btnReject.setVisible(true);
            }
            if (lstRequest.getModel() == modelAllUser) {
                selectedFriend = allUser.get(idx);
                btnAddFriend.setVisible(true);
                btnAddFriend.setEnabled(true);
                btnAccept.setVisible(false);
                btnReject.setVisible(false);
            }
            lblFriendAva.setIcon(imageMap.get(selectedFriend.getUserName()));
            lblFriendName.setText(selectedFriend.getUserName());
            selectedFriend.printUser();
        }
    }//GEN-LAST:event_lstRequestMouseClicked

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        if (jTabbedPane1.getSelectedIndex() == 0) {
            btnAccept.setVisible(false);
            btnReject.setVisible(false);
            btnAddMem.setVisible(false);
            btnAddFriend.setVisible(false);
            btnClear.setVisible(true);
            btnCall.setVisible(true);
            btnDecline.setVisible(true);
            btnLeave.setVisible(false);
            if (currentFriends.size() > 0) {
                listFriends.setSelectedIndex(0);
                lblFriendAva.setIcon(imageMap.get(currentFriends.get(0).getUserName()));
                lblFriendName.setText(currentFriends.get(0).getUserName());
                selectedRoom = twoUserRoomMap.get(currentFriends.get(0));
                selectedFriend = currentFriends.get(0);
                lblFriendName.setToolTipText(currentFriends.get(0).getUserName());
            } else {
                lblFriendName.setText("Friendname");
                lblFriendAva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/User-small-icon.png")));
            }
            lblFriendName.setCursor(null);
        }
        if (jTabbedPane1.getSelectedIndex() == 1) {
            btnAccept.setVisible(false);
            btnReject.setVisible(false);
            btnAddMem.setVisible(true);
            btnAddFriend.setVisible(false);
            btnCall.setVisible(false);
            btnDecline.setVisible(false);
            btnClear.setVisible(true);
            btnLeave.setVisible(true);

            if (allGroupRoom.size() > 0) {
                listGroup.setSelectedIndex(0);
                lblFriendAva.setIcon(new ImageIcon("src/view/Images/Group-icon.png"));
                selectedRoom = (Room) allGroupRoom.get(0);
                loadMessages(selectedRoom);
                lblFriendName.setText(allGroupRoom.get(0).getTitle());
                lblFriendName.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }
        if (jTabbedPane1.getSelectedIndex() == 2) {
            btnAccept.setVisible(true);
            btnReject.setVisible(true);
            btnAddMem.setVisible(false);
            btnAddFriend.setVisible(true);
            btnCall.setVisible(false);
            btnDecline.setVisible(false);
            btnClear.setVisible(false);
            btnLeave.setVisible(false);
            if (friendRequests.size() > 0) {
                lstRequest.setSelectedIndex(0);
                lblFriendAva.setIcon(imageMap.get(friendRequests.get(0).getUsername()));
                lblFriendName.setText(friendRequests.get(0).getUsername());
                btnAccept.setVisible(true);
                btnReject.setVisible(true);
            } else {
                lblFriendName.setText("Friendname");
                lblFriendAva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/User-small-icon.png")));
                btnAccept.setVisible(false);
                btnReject.setVisible(false);
            }
            lblFriendName.setCursor(null);
        }
        setStatus();
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void btnAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAcceptActionPerformed

        try {
            if (lstRequest.getSelectedIndex() >= 0) {
                client.sendMessage("acceptRequest", currentUser, friendRequests.get(lstRequest.getSelectedIndex()).getUsername());
            }
        } catch (IOException ex) {
            Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAcceptActionPerformed

    private void btnRejectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRejectActionPerformed
        try {
            if (lstRequest.getSelectedIndex() >= 0) {
                client.sendMessage("rejectRequest", currentUser, friendRequests.get(lstRequest.getSelectedIndex()).getUsername());
            }
        } catch (IOException ex) {
            Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        btnAccept.setVisible(true);
        btnReject.setVisible(true);
        if (friendRequests.size() > 0) {
            lstRequest.setSelectedIndex(0);
            lblFriendAva.setIcon(imageMap.get(friendRequests.get(0).getUsername()));
            lblFriendName.setText(friendRequests.get(0).getUsername());
            btnAccept.setVisible(true);
            btnReject.setVisible(true);
        } else {
            lblFriendName.setText("Friend 's username");
            lblFriendAva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/Images/User-small-icon.png")));
            btnAccept.setVisible(false);
            btnReject.setVisible(false);
        }
    }//GEN-LAST:event_btnRejectActionPerformed

    private void lblNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNameMouseClicked

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new ProfileFrame().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }//GEN-LAST:event_lblNameMouseClicked

    // Them Room
    private void btnNewGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewGroupActionPerformed
        String name = JOptionPane.showInputDialog(null, "Enter name of Group", "New group ", JOptionPane.INFORMATION_MESSAGE);
        if (name != null && !name.equals("")) {
            GroupRoom newRoom = new GroupRoom(UUID.randomUUID().toString(), name);
            try {
                client.sendMessage("newGroup", newRoom, currentUser);
            } catch (IOException ex) {
                Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            modelGroup.addElement(name);
            groupRoomMap.put(newRoom.getRoomId(), newRoom);
            allGroupRoom.add(newRoom);
        }
    }//GEN-LAST:event_btnNewGroupActionPerformed

    // Kiem tra mot user co nam trong List User ko ?
    public boolean isInListUser(User aUser, List<User> user) {
        for (int i = 0; i < user.size(); i++) {
            if (aUser.getUserName().equals(user.get(i).getUserName())) {
                return true;
            }
        }
        return false;
    }

    // Tra ve list nhung nguoi ban khong nam trong Group Room
    public List<User> availableForGroup(List<User> friends, GroupRoom room) {
        List<User> mems = room.getListUser();
        List<User> rs = new ArrayList<>();
        for (int i = 0; i < friends.size(); i++) {
            if (!isInListUser(friends.get(i), mems)) {
                rs.add(friends.get(i));
            }
        }
        return rs;
    }

    private void btnAddMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMemActionPerformed
        try {
            List<User> mems = availableForGroup(currentFriends, (GroupRoom) selectedRoom);
            modelMember = createModelFriends(mems);
        } catch (IOException ex) {
            Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        AddMemberFrame.lstMem.setModel(modelMember);
        AddMemberFrame.lstMem.setCellRenderer(new CustomFriendList());
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AddMemberFrame().setVisible(true);
            }
        });

    }//GEN-LAST:event_btnAddMemActionPerformed

    private void listGroupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listGroupMouseClicked
        int idx = listGroup.getSelectedIndex();
        if (idx >= 0) {
            selectedRoom = allGroupRoom.get(listGroup.getSelectedIndex());
            loadMessages(selectedRoom);
            lblFriendName.setText(((GroupRoom) selectedRoom).getTitle());
        }
        lblFriendAva.setIcon(new ImageIcon("src/view/Images/Group-icon.png"));
    }//GEN-LAST:event_listGroupMouseClicked

    private void lblFriendNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFriendNameMouseClicked
        if (jTabbedPane1.getSelectedIndex() == 1) {
            try {
                client.sendMessage("getMems", selectedRoom.getRoomId(), currentUser.getUserName());
            } catch (IOException ex) {
                Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_lblFriendNameMouseClicked

    private void btnLeaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLeaveActionPerformed
        if (selectedRoom.getType().equals("GROUP")) {
            try {
                client.sendMessage("leave", selectedRoom.getRoomId(), currentUser.getUserName());
                int idx = listGroup.getSelectedIndex();
                modelGroup.removeElementAt(idx);
                allGroupRoom.remove(idx);
                groupRoomMap.remove(selectedRoom.getRoomId());
            } catch (IOException ex) {
                Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        lblFriendName.setText("Room's Name");

    }//GEN-LAST:event_btnLeaveActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        if (selectedRoom != null) {
            try {
                client.sendMessage("clear", selectedRoom.getRoomId(), currentUser.getUserName());
                modelChat.clear();
                selectedRoom.getListMessage().clear();
            } catch (IOException ex) {
                Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnClearActionPerformed

    Object sendFileSyncFlag = new Object();
    private void AddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddFileActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select file");
        int choose = -1;
        JPanel p = new JPanel();
        p.add(fc);
        choose = fc.showOpenDialog(p);
        byte[] imgArray = null;
        if (choose == JFileChooser.APPROVE_OPTION) {
//            try {
            File selectedFile = fc.getSelectedFile();
            if (selectedFile != null) {
                System.out.println("---Send---");
                Room room = selectedRoom;
                final String roomId = selectedRoom.getRoomId();
                final String fileName = selectedFile.getName();
                final String absPath = selectedFile.getAbsolutePath();
                new Thread() {
                    @Override
                    public void run() {
                        synchronized (sendFileSyncFlag) {
                            try {
                                Message fileMsg = new Message(UUID.randomUUID().toString(), currentUser.getUserName(), "", "FILE", fileName);
                                client.sendMessage("SendFile", roomId, fileMsg, client.filePort);
                                Socket socketFile = client.serverSocketFile.accept();

                                System.out.println(absPath);
                                friendFile = lblFriendName.getText();
                                File file = new File(absPath);
                                if (file.exists()) {
                                    byte[] bytes = new byte[16 * 1024];
                                    System.out.println("Start sending.....");
                                    InputStream in = new FileInputStream(file);
                                    OutputStream out = socketFile.getOutputStream();
                                    int len;
                                    try {
                                        while ((len = in.read(bytes)) > 0) {
                                            out.write(bytes, 0, len);
                                        }
                                    } catch (Exception ex) {
                                        System.out.println("Lỗi chỗ này");
                                    }
                                    out.close();
                                    in.close();
                                    socketFile.close();
                                    room.addMessage(fileMsg);
                                    if (selectedRoom == room) {
                                        modelChat.addElement(fileMsg);
                                    }
                                }

                            } catch (Exception ex) {
                                System.out.println("Lỗi chỗ này");
                            }
                        }
                    }
                }.start();
            }
        }
    }//GEN-LAST:event_AddFileActionPerformed

    private void statusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusActionPerformed
        // TODO add your handling code here:
        System.out.println(status.getSelectedItem().toString());
        try {
            client.sendMessage("statusChange", currentUser.getUserName(), status.getSelectedItem().toString());
        } catch (IOException ex) {
            Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_statusActionPerformed

    Object downloadFileSyncFlag = new Object();
    private void listChatBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listChatBoxMouseClicked
        // TODO add your handling code here:
        int index = selectedRoom.getListMessage().size() - listChatBox.getSelectedIndex() - 1;
        Message msg = selectedRoom.getListMessage().get(index);
        if (!msg.getType().equals("FILE")) {
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(msg.getContent()));
        int choose = fc.showSaveDialog(null);
        File f = null;
        if (choose == JFileChooser.APPROVE_OPTION) {
            f = fc.getSelectedFile();
            System.out.println(f.getPath());
            System.out.println("Create dir to client! ");
            String path = "./File/" + currentUser.getUserName() + "/" + msg.getContent();
            File file = new File(f.getPath());
            System.out.println("OK");
            byte[] bytes = new byte[16 * 1024];
            System.out.println("Start sending.....");

            new Thread() {
                @Override
                public void run() {
                    synchronized (downloadFileSyncFlag) {
                        try {
                            client.sendMessage("DownLoadFile", selectedRoom.getRoomId(), msg.getMessageId(), client.filePort);
                            Socket socketFile = client.serverSocketFile.accept();
                            OutputStream out = new FileOutputStream(file);
                            InputStream in = socketFile.getInputStream();
                            int len;
                            try {
                                while ((len = in.read(bytes)) > 0) {
                                    out.write(bytes, 0, len);
                                }
                                System.out.println("Download xong");
                            } catch (Exception ex) {
                                System.out.println("Lỗi chỗ này 1");
                            }

                            out.close();
                            in.close();
                            socketFile.close();

                        } catch (IOException ex) {
                            Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }.start();
        }
    }//GEN-LAST:event_listChatBoxMouseClicked

    private void btnCallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCallActionPerformed
        // TODO add your handling code here:
        if (audioServer == null && audioClient == null) {
            if (selectedRoom.getType().equals("PRIVATE")) {
                try {
                    client.sendMessage("call", ((TwoUserRoom) selectedRoom).getPeerUser().getUserName());
                    audioServer = new AudioCaller();
                } catch (IOException ex) {
                    Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_btnCallActionPerformed

    private void btnDeclineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeclineActionPerformed
        // TODO add your handling code here:
        if (selectedRoom.getType().equals("PRIVATE")) {
            try {
                client.sendMessage("endCall", ((TwoUserRoom) selectedRoom).getPeerUser().getUserName());
            } catch (IOException ex) {
                Logger.getLogger(PnlMain.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (audioServer != null) {
                audioServer.stop();
                audioServer = null;
            }

            if (audioClient != null) {
                audioClient.stop();
                audioClient = null;
            }
        }
    }//GEN-LAST:event_btnDeclineActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddFile;
    private javax.swing.JButton AddImage;
    private javax.swing.JButton btnAccept;
    private javax.swing.JButton btnAddFriend;
    private javax.swing.JButton btnAddMem;
    private javax.swing.JButton btnCall;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDecline;
    private javax.swing.JButton btnLeave;
    private javax.swing.JButton btnNewGroup;
    private javax.swing.JButton btnReject;
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    public static final javax.swing.JLabel lblAvatar = new javax.swing.JLabel();
    private javax.swing.JLabel lblFriendAva;
    private javax.swing.JLabel lblFriendName;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JList listChatBox;
    private javax.swing.JList<String> listFriends;
    private javax.swing.JList<String> listGroup;
    private javax.swing.JList<String> lstRequest;
    private javax.swing.JPanel pnlContainer;
    private javax.swing.JComboBox<String> status;
    private javax.swing.JTextField txtInPutField;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
