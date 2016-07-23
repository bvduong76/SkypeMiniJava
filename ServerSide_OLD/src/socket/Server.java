/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket;

import database.DatabaseAccess;
import java.net.*;
import java.io.*;
import global.Global;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.FriendRequest;
import objects.User;
import objects.messages.Message;
import objects.room.GroupRoom;
import objects.room.Room;
import objects.room.TwoUserRoom;

/**
 *
 * @author NQH
 */
public class Server {

    Thread serverThread;
    ServerSocket serverSocket;
    Map<String, SocketThread> clientMap = new HashMap<>();

    List<User> onlineList = new ArrayList<>();
    Map<String, String> filePath = new HashMap<>();

    public void start() throws IOException, ClassNotFoundException {
        serverSocket = new ServerSocket(Global.port);

        System.out.println("Server is starting at: " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        do {
            SocketThread thread = null;
            try {
                Socket clientSocket = serverSocket.accept(); //synchronous
                System.out.println("New client access: " + clientSocket.getPort());

                //Xử lý khi có socket kết nối tới ở đây....
                //Tạo tạo 1 thread để xử lý cho client này
                thread = new SocketThread(clientSocket);
                //Khai báo các handle cho thread
                declareHandleForThread(thread);

                //chạy thread
                thread.start();

            } catch (IOException ex) {
            }
        } while (true);
    }

    //Khai báo các handle cho thread
    private void declareHandleForThread(SocketThread thread) {
        thread.declareHandle("login", loginFunction);
        thread.declareHandle("register", registerFunction);
        thread.declareHandle("sendRequest", requestFunction);
        thread.declareHandle("acceptRequest", acceptFunction);
        thread.declareHandle("rejectRequest", rejectFunction);
        thread.declareHandle("SignOut", signOutFunction);
        thread.declareHandle("sendPrivateMsg", sendPrivateMsg);
        thread.declareHandle("updateProfile", updateProfile);
        thread.declareHandle("search", searchFunction);
        thread.declareHandle("newGroup", newGroup);
        thread.declareHandle("addMem", addMem);
        thread.declareHandle("sendGroupMsg", sendGroupMsg);
        thread.declareHandle("getMems", getMems);
        thread.declareHandle("leave", leaveFunction);
        thread.declareHandle("clear", clearFunction);
        thread.declareHandle("SendFile", SendFileFunction);
        thread.declareHandle("statusChange", statusFunction);
        thread.declareHandle("getStatus", getStatusFunction);
        thread.declareHandle("DownLoadFile", downloadFileFunction);

        thread.declareHandle("call", callFunction);
        thread.declareHandle("endCall", endCallFunction);
    }
    private SocketMessageHandle endCallFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            try {
                String username = (String) params[0];
                SocketThread clientThread = clientMap.get(username);
                if (clientThread != null) {
                    clientThread.sendMessage("endCall");
                    System.out.println(thread.getAdress());
                }
            } catch (Exception e) {
            }
        }
    };

    private SocketMessageHandle callFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            try {
                String username = (String) params[0];
                SocketThread clientThread = clientMap.get(username);
                if (clientThread != null) {
                    clientThread.sendMessage("call", thread.getAdress());
                    System.out.println(thread.getAdress());
                }
            } catch (Exception e) {
            }
        }
    };

    private SocketMessageHandle downloadFileFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            try {
                String roomId = (String) params[0];
                String messageId = (String) params[1];
                int filePort = (int) params[2];
                Socket socketFile = new Socket(thread.getAdress(), filePort);
                System.out.println("Ok da vao");
                String path = "./File/" + roomId + "/" + messageId;
                System.out.println(path);
                File f = new File(path);
                if (f.exists()) {
                    System.out.println("----Create new socket to download");
                    InputStream in = new FileInputStream(f);
                    OutputStream out = socketFile.getOutputStream();
                    byte[] bytes = new byte[16 * 1024];
                    int count;
                    while ((count = in.read(bytes)) > 0) {
                        out.write(bytes, 0, count);
                    }
                    out.close();
                    in.close();
                    socketFile.close();
                    System.out.println("Download complete");
                }
                socketFile.close();
            } catch (Exception e) {
            }
        }
    };

    private SocketMessageHandle SendFileFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            // Lay thong tin nguoi gui, nguoi nhan, ten file

            String roomId = (String) params[0];
            Message fileMsg = (Message) params[1];
            int filePort = (int) params[2];
            System.out.println("start check");
            System.out.println("->" + fileMsg.getContent());
            File dirForTransfer = new File("./File");
            if (!dirForTransfer.exists()) {
                dirForTransfer.mkdir();
            }
            File dir = new File("./File/" + roomId);
            if (!dir.exists()) {
                dir.mkdir();
            }
            // Gui cho nguoi nhan 1 thong bao
            new Thread() {
                @Override
                public void run() {
                    try {
                        System.out.println("Ok da vao");
                        Socket socketFile = new Socket(thread.getAdress(), filePort);
                        InputStream in = socketFile.getInputStream();
                        System.out.println("Da set socker xong");
                        File f = new File("./File/" + roomId + "/" + fileMsg.getMessageId());

                        OutputStream out = new FileOutputStream(f);
                        byte[] bytes = new byte[16 * 1024];
                        int count;
                        while ((count = in.read(bytes)) > 0) {
                            out.write(bytes, 0, count);
                        }
                        out.close();
                        in.close();
                        socketFile.close();
                        
                        DatabaseAccess.getInstace().addMessage(fileMsg, roomId);
                        Room room = DatabaseAccess.getInstace().getRoom(roomId, thread.getUsername());
                        List<User> listUser = DatabaseAccess.getInstace().getAllMembersOfRoom(roomId);
                        for (User user : listUser) {
                            SocketThread userThread = clientMap.get(user.getUserName());
                            if (userThread != null) {
                                if (room.getType().equals("PRIVATE")) {
                                    userThread.sendMessage("sendPrivateMsg", fileMsg);
                                } else {
                                    userThread.sendMessage("sendGroupMsg", fileMsg, roomId);
                                }
                            }
                        }
                        System.out.println("Send  complete");
                    } catch (Exception e) {
                    }
                }
            }.start();
        }
    };
    private SocketMessageHandle getStatusFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            String username = (String) params[0];
            String result = "Offline";
            for (User e : onlineList) {
                if (e.getUserName().equals(username)) {
                    result = e.getStatus();
                }
            }
            thread.sendMessage("getStatusRS", result);
        }
    };
    private SocketMessageHandle statusFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            String userChange = (String) params[0];
            String stt = (String) params[1];
            //statusMap.replace(userChange, stt);
            System.out.println(userChange + " change status to " + stt);
            User change = null;
            for (User us : onlineList) {
                if (us.getUserName().equals(userChange)) {
                    us.setStatus(stt);
                    change = us;
                }
            }

            thread.sendMessage("statusChangeRS", userChange, stt);
            for (User username : change.getFriendList()) {

                SocketThread friendThread = clientMap.get(username.getUserName());
                if (friendThread != null) {
                    friendThread.sendMessage("statusChangeRS", userChange, stt);
                }

            }

        }
    };

    private SocketMessageHandle clearFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            String roomId = (String) params[0];
            String username = (String) params[1];
            DatabaseAccess.getInstace().removeAllMessagesFroomRoom(roomId);
            List<User> users = DatabaseAccess.getInstace().getAllMembersOfRoom(roomId);
            for (int i = 0; i < users.size(); i++) {
                if (!users.get(i).getUserName().equals(username)) {
                    SocketThread t = clientMap.get(users.get(i).getUserName());
                    if (t != null) {
                        t.sendMessage("clear", roomId);
                    }
                }
            }
        }

    };

    private SocketMessageHandle leaveFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            String roomid = (String) params[0];
            String username = (String) params[1];
            DatabaseAccess.getInstace().removeMemberFromRoom(roomid, username);
        }

    };

    private SocketMessageHandle getMems = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            String roomId = (String) params[0];
            String owner = (String) params[1];
            Room room = DatabaseAccess.getInstace().getRoom(roomId, owner);
            List<User> mems = ((GroupRoom) room).getListUser();
            thread.sendMessage("getMems", mems);
        }

    };

    private SocketMessageHandle sendPrivateMsg = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            Message msg = (Message) params[0];
            String roomId = (String) params[1];
            String friendName = (String) params[2];

            DatabaseAccess.getInstace().addMessage(msg, roomId);
            SocketThread t = clientMap.get(friendName);
            if (t != null) {
                t.sendMessage("sendPrivateMsg", msg);
            }
        }

    };

    private SocketMessageHandle sendGroupMsg = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            Message msg = (Message) params[0];
            String roomId = (String) params[1];
            List<User> users = DatabaseAccess.getInstace().getAllMembersOfRoom(roomId);

            DatabaseAccess.getInstace().addMessage(msg, roomId);
            for (int i = 0; i < users.size(); i++) {
                SocketThread t = clientMap.get(users.get(i).getUserName());
                if (t != null) {
                    t.sendMessage("sendGroupMsg", msg, roomId);
                    System.out.println("Selec" + roomId);
                }
            }
        }
    };

    private SocketMessageHandle addMem = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            GroupRoom room = (GroupRoom) params[0];
            String friendname = (String) params[1];
            DatabaseAccess.getInstace().addMemberToRoom(room.getRoomId(), friendname);
            SocketThread t = clientMap.get(friendname);
            if (t != null) {
                t.sendMessage("addMemRS", room);
            }
        }
    };

    private SocketMessageHandle newGroup = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            GroupRoom newRoom = (GroupRoom) params[0];
            User user = (User) params[1];
            DatabaseAccess.getInstace().addRoom(newRoom.getRoomId(), newRoom.getTitle(), newRoom.getType());
            DatabaseAccess.getInstace().addMemberToRoom(newRoom.getRoomId(), user.getUserName());
        }
    };

    private SocketMessageHandle searchFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            String username = (String) params[0];
            User user = DatabaseAccess.getInstace().getUserInfo(username);
            thread.sendMessage("searchRS", user);
        }

    };

    private SocketMessageHandle updateProfile = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            User user = (User) params[0];
            byte[] img = (byte[]) params[1];
            DatabaseAccess.getInstace().updateUserInfo(user);
            System.out.println("Update thành công !!!");
            String filePath = "./Avatar/" + user.getUserName() + "/" + "avatar.jpg";
            try {
                Files.write(new File(filePath).toPath(), img);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    };

    private SocketMessageHandle rejectFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            User user = (User) params[0];
            String friendname = (String) params[1];
            DatabaseAccess.getInstace().removeFriendRequest(friendname, user.getUserName());
            thread.sendMessage("rejectRS", friendname);

        }
    };

    private SocketMessageHandle signOutFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            User user = (User) params[0];
            String username = user.getUserName();
            thread.setUsername(null);
            clientMap.remove(username);
            for (User s : onlineList) {
                if (s.getUserName().equals(username)) {
                    onlineList.remove(s);
                    break;
                }
            }

            String stt = "Offline";

            System.out.println(stt);
            for (User u : user.getFriendList()) {
                SocketThread friendThread = clientMap.get(u.getUserName());
                if (friendThread != null) {
                    friendThread.sendMessage("statusChangeRS", username, stt);
                }
            }
        }
    };

    private SocketMessageHandle acceptFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            User user = (User) params[0];
            String friendname = (String) params[1];

            String roomId = UUID.randomUUID().toString();
            TwoUserRoom room = new TwoUserRoom(roomId, user);

            DatabaseAccess.getInstace().addFriend(user.getUserName(), friendname);
            DatabaseAccess.getInstace().addFriend(friendname, user.getUserName());

            DatabaseAccess.getInstace().removeFriendRequest(friendname, user.getUserName());

            DatabaseAccess.getInstace().addRoom(roomId, "", room.getType());
            DatabaseAccess.getInstace().addMemberToRoom(roomId, user.getUserName());
            DatabaseAccess.getInstace().addMemberToRoom(roomId, friendname);
            User friend = DatabaseAccess.getInstace().getUserInfo(friendname);
            thread.sendMessage("acceptRS", friend, room);

            SocketThread friendThread = clientMap.get(friendname);
            if (friendThread != null) {
                friendThread.sendMessage("acceptRS", user, room);
            }

        }
    };

    private SocketMessageHandle requestFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            String username = (String) params[0];
            String friendUsername = (String) params[1];
            boolean res = DatabaseAccess.getInstace().addFriendRequest(username, friendUsername);
            thread.sendMessage("requestRS", res);

            SocketThread friendThread = clientMap.get(friendUsername);
            if (friendThread != null) {
                friendThread.sendMessage("friend_request", username);
            }

        }
    };

    private SocketMessageHandle registerFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {
            User newUser = (User) params[0];
            byte[] imageArray = (byte[]) params[1];

            if (newUser == null) {
                System.out.println("fail");
            }

            boolean res = DatabaseAccess.getInstace().addUser(newUser.getUserName(), newUser.getPassword(), newUser.getFullName(), newUser.isGender(), newUser.getStatus());
            thread.sendMessage("registerRS", res, newUser, imageArray);

            if (res == true) {
                File dir1 = new File("./Avatar");
                if (!dir1.exists()) {
                    dir1.mkdir();
                }

                File dir = new File("./Avatar/" + newUser.getUserName());
                if (!dir.exists()) {
                    dir.mkdir();
                }
                String filePath = "./Avatar/" + newUser.getUserName() + "/" + "avatar.jpg";
                try {
                    Files.write(new File(filePath).toPath(), imageArray);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    private SocketMessageHandle loginFunction = new SocketMessageHandle() {
        @Override
        public void Handle(SocketThread thread, Object... params) {

            String username = (String) params[0];
            char[] password = (char[]) params[1];
            User aUser = DatabaseAccess.getInstace().login(username, String.valueOf(password));
            if (aUser == null) {
                System.out.println("fail");
            } else {
                System.out.println(aUser.getUserName());
                System.out.println(aUser.getPassword());
                thread.setUsername(aUser.getUserName());
                String filePath = "./Avatar/" + aUser.getUserName() + "/" + "avatar.jpg";
                byte[] imageArray = null;
                try {
                    imageArray = Files.readAllBytes(new File(filePath).toPath());
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                List<User> friendList = DatabaseAccess.getInstace().getFriendList(aUser.getUserName());
                aUser.setFriendList(friendList);
                List<User> allUser = DatabaseAccess.getInstace().getAllUser();

                List<Room> listRoom = DatabaseAccess.getInstace().getAllRoomOfUser(username);
                List<byte[]> images = new ArrayList<>();
                for (int i = 0; i < allUser.size(); i++) {
                    try {
                        String fp = "./Avatar/" + allUser.get(i).getUserName() + "/" + "avatar.jpg";
                        byte[] img = Files.readAllBytes(new File(fp).toPath());
                        images.add(img);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                List<FriendRequest> FriendRequests = DatabaseAccess.getInstace().getFriendRequestList(username);
                thread.sendMessage("loginRs", aUser, imageArray, allUser, listRoom, FriendRequests, onlineList, images);

                clientMap.put(aUser.getUserName(), thread);
                aUser.setStatus("Online");
                onlineList.add(aUser);

            }
            User change = null;
            String stt = "Online";
            for (User us : onlineList) {
                if (us.getUserName().equals(username)) {
                    us.setStatus(stt);
                    change = us;
                }
            }

            try {
                for (User u : change.getFriendList()) {

                    SocketThread friendThread = clientMap.get(u.getUserName());
                    if (friendThread != null) {
                        friendThread.sendMessage("statusChangeRS", username, stt);
                    }
                }
            } catch (Exception e) {
                System.out.println("Cannot get frriend list");
            }

        }
    };
}
