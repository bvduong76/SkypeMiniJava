/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
public class DatabaseAccess {
    
    private static DatabaseAccess INSTANCE = new DatabaseAccess();
    public static DatabaseAccess getInstace(){return INSTANCE;}
    
    private Connection connection;
    private DatabaseAccess(){
       try{
            connection= ConnectionUtils.getConnection(); 
       } catch (ClassNotFoundException | SQLException ex) {
            connection=null;
            
       }
    }
   //Kiểm tra đã kết nối tới CSDL thành công hay chưa
   public boolean connected(){
       return connection!=null;
   }
   public void close(){
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
   //======================================================================
           
   //===========User===============================
   public boolean addUser(String username, String password, String fullName, boolean gender, String status){
    String sql = "insert into [User]  values('"+username+"'"+
                                              ",'"+password+"'"+
                                              ",N'"+fullName+"'"+
                                              ","+(gender?"1":"0")+
                                              ",'"+status+"'"+
                                              ")";
     
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
   }
   
    public User getUserInfo(String username){
        User user=null;
        String sql = "Select * from [User] where Username= '"+username+"'";
        try {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            if(rs.next()){
                user=new User(username);
                user.setFullName(rs.getString("FullName"));
                user.setPassword(rs.getString("Password"));     
                user.setGender(rs.getBoolean("Gender"));
                user.setStatus(rs.getString("Status"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }
    
    public List<User> getAllUser(){
       List<User> result = new ArrayList<>();
        try {
            
            String sql = "Select * from [User]";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                User user= new User(rs.getString("Username"));
                user.setFullName(rs.getString("FullName"));
                user.setPassword(rs.getString("Password"));
                user.setGender(rs.getBoolean("Gender"));
                user.setStatus(rs.getString("Status"));
                result.add(user);
            } 
        } catch (SQLException ex) {
        }
        return result;
   }
      
    public boolean removeUser(String username){
    String sql = "delete from [User]  where Username='"+username+"'";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
   }
    
    public boolean updateUserInfo(User user){
        String sql = "update  [User]  set "+" FullName= N'"+user.getFullName()+"'"+
                                              ",Gender= "+(user.isGender()?"1":"0")+
                                              ",Password='"+user.getPassword()+"'"+
                                              ",Status= '"+user.getStatus()+"'"+
                                              " where Username= '"+user.getUserName()+ "'";
     
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
    }
    
    public boolean updateUserPassword(String username, String oldPassword, String password){
        String sql = "update  [User]  set "+" Password= '"+password+"'"+
                                            " where Username= '" + username +"' and Password= '"+oldPassword+"'";
     
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
    }
    public User login(String username, String password){
        User user=null;
        String sql = "Select * from [User] where Username= '"+username+"' and Password= '"+password+"'";
        try {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            if(rs.next()){
                user=new User(username);
                user.setFullName(rs.getString("FullName"));
                user.setGender(rs.getBoolean("Gender"));
                user.setGender(rs.getBoolean("Gender"));
                user.setStatus(rs.getString("Status"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }
   //=========================================================================
    
   //============FRIENDS=====================================================
    public List<User> getFriendList(String username){
       List<User> result = new ArrayList<>();
       List<String> listUsername = new ArrayList<>();
        try {
            String sql = "Select * from Friend where Username= '"+username+"'";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                listUsername.add(rs.getString("FriendUsername"));
            }
            for(String friendUsername: listUsername){
                User user=getUserInfo(friendUsername);
                if(user!=null)
                    result.add(user);
            }
        } catch (SQLException ex) {
        }
                    
        return result;
   }
    
    public boolean addFriend(String username, String friendUsername){
        String sql = "insert into Friend  values('"+username+"'"+
                                              ",'"+friendUsername+"'"+
                                              ")";
     
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
    }
    
    public boolean removeFriend(String username, String friendUsername){
    String sql = "delete from Friend  where Username='"+username+"' and FriendUsername ='"+friendUsername+"'";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
   }
    
    public boolean isExistFriend(String username, String friendUsername){
        try {
            String sql = "Select * from Friend where Username='"+username+"' and FriendUsername ='"+friendUsername+"'";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            return rs.next();
        } catch (SQLException ex) {
            return false;
        }
   }
        
       //============FRIENDS=====================================================
    public List<FriendRequest> getFriendRequestList(String username){
       List<FriendRequest> result = new ArrayList<>();
        try {
            String sql = "Select * from FriendRequest where FriendUsername= '"+username+"'";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                FriendRequest fr= new FriendRequest(rs.getString("Username"),username);
                result.add(fr);
            }
        } catch (SQLException ex) {
        }
                    
        return result;
   }
    
    public boolean addFriendRequest(String username, String friendUsername){
        if(username.equals(friendUsername))
            return false;
        String sql = "insert into FriendRequest  values('"+username+"'"+
                                              ",'"+friendUsername+"'"+
                                              ")";
     
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
    }
    
    public boolean removeFriendRequest(String username, String friendUsername){
    String sql = "delete from FriendRequest  where Username='"+username+"' and FriendUsername ='"+friendUsername+"'";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
   }
   
      //===========Room===============================
   public boolean addRoom(String roomId, String title, String type){
    String sql = "insert into Room  values('"+roomId+"'"+
                                              ",N'"+title+"'"+
                                                ",'"+type+"'"+
                                              ")";
     
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;
   }
   public boolean addMemberToRoom(String roomId, String username){
    String sql = "insert into MembersOfRoom  values('"+roomId+"'"+
                                              ",'"+username+"'"+
                                              ")";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
           System.out.println(ex.toString());
       }
       return false;
   }
   public boolean removeMemberFromRoom(String roomId, String username){
          String sql = "delete from MembersOfRoom  where RoomId = '"+roomId+"'"+
                                              " and Username ='"+username+"'";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false; 
   }
   
   public boolean removeAllMembersFroomRoom(String roomId){
       String sql = "delete from MembersOfRoom  where RoomId = '"+roomId+"'";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;     
   }
              
   public List<User> getAllMembersOfRoom(String roomId){
       List<User> result = new ArrayList<>();
       String sql = "Select * from MembersOfRoom where RoomId= '"+roomId+"'";
       try {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while(rs.next()){
                String username= rs.getString("Username");
                User user=getUserInfo(username);
                if(user!=null)
                    result.add(user);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
       return result;
   }

   
   public List<Message> getAllMessagesOfRoom(String roomId){
       List<Message> result = new ArrayList<>();
       String sql = "Select * from Message where RoomId= '"+roomId+"' order by time asc";
            try {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while(rs.next()){
                String username= rs.getString("Username");
                User user=getUserInfo(username);
                String messageId=rs.getString("MessageId");
                String type = rs.getString("Type");
                Date time= rs.getDate("Time");
                String content = rs.getString("Content");
                Message msg= new Message(messageId,username, time.toString(), type, content);
                result.add(0,msg);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
       return result;
   }
   
    public boolean removeAllMessagesFroomRoom(String roomId){
       String sql = "delete from Message  where RoomId = '"+roomId+"'";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;     
   }
   
    public Room getRoom(String roomId, String owner){
        Room room=null;
        String sql = "Select * from Room where RoomId= '"+roomId+"'";
        try { 
            ResultSet rs = connection.createStatement().executeQuery(sql);
            if(rs.next()){
                List<User> members= getAllMembersOfRoom(roomId);
                List<Message> msgs= getAllMessagesOfRoom(roomId);
                 String type= rs.getString("Type");
                if(type.equals("PRIVATE")){
                    if(owner.equals(members.get(0).getUserName()))
                        room=new TwoUserRoom(roomId,members.get(1));
                    else
                        room=new TwoUserRoom(roomId,members.get(0));
                }else{
                    String title= rs.getString("Title");
                    room=new GroupRoom(roomId, title);
                    ((GroupRoom)room).setListUser(members);
                }
                room.setListMessage(msgs);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return room;
    }
    
//    public List<Room> getAllRoom(){
//        List<Room> result = new ArrayList<>();
//        try {
//            String sql = "Select * from Room";
//            ResultSet rs = connection.createStatement().executeQuery(sql);
//            while (rs.next()) {
//                Room room= getRoom(rs.getString("RoomId"));
//                if(room!=null)
//                    result.add(room);
//            } 
//        } catch (SQLException ex) {
//        }
//        return result;
//    }
    
    public List<Room> getAllRoomOfUser(String username){
        List<Room> result = new ArrayList<>();
        try {
            String sql = "Select * from MembersOfRoom where Username='"+username+"'";
            ResultSet rs = connection.createStatement().executeQuery(sql);
            while (rs.next()) {
                Room room = getRoom(rs.getString("RoomId"),username);
                if(room!=null){
                    result.add(room);
                }
            } 
        } catch (SQLException ex) {
        }
        return result;
    }
    
    public boolean removeRoom(String roomId){
        removeAllMembersFroomRoom(roomId);
        removeAllMessagesFroomRoom(roomId);
        String sql = "delete from Room  where RoomId = '"+roomId+"'";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;    
    }
    
    //====================================================
    public boolean addMessage(Message msg, String roomId){
        String sql = "insert into Message  values('"+msg.getMessageId()+"'"+
                                              ",'"+msg.getUserName()+"'"+
                                              ",'"+roomId+"'"+
                                              ",'"+msg.getTime().toString()+"'"+
                                              ",'"+msg.getType()+"'"+
                                              ",'"+msg.getContent().toString()+"'"+
                                              ")";
       try {
          return connection.createStatement().executeUpdate(sql)!=0;
       } catch (SQLException ex) {
       }
       return false;    
    }
}

