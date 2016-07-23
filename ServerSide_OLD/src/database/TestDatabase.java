/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import objects.User;
import objects.messages.Message;
import objects.room.Room;

/**
 *
 * @author NQH
 */
public class TestDatabase {


//    // Test Connection ...
//    public static void main(String[] args)  throws SQLException,
//            ClassNotFoundException {
//        DatabaseAccess db = DatabaseAccess.getInstace();
//        System.out.println("Get connection ... ");
//        
////
////        User newUser = new User("Duyen1244", "123", "A"
////                    , true);
////        boolean res = DatabaseAccess.getInstace().addUser(newUser.getUserName(), newUser.getPassword(), newUser.getFullName()
////                    , newUser.isGender(), newUser.getStatus());
////        System.out.println(res);
//        
//        List<User> listUser = db.getAllUser();
//        for (User user : listUser) {
//            user.printUser();
//        }
//        
////        User auser = db.getUserInfo("Duyen124");
////        auser.printUser();
////        System.out.println("Friend");
////        List<User> friend = db.getFriendList("Duyen124");
////        for (User user : friend) {
////            user.printUser();
////        }
//        
////        List<Room> listRoom = db.getAllRoom();
////        for (Room room: listRoom) {
////            System.out.println("--------------------");
////            System.out.println(db.removeRoom(room.getRoomId()));
////        }
//////
////        List<User> listUser = db.getFriendList("hung");
////        for (User u : listUser) {
////            System.out.println("--------------------");
////            System.out.println(u.getUserName());
////            System.out.println(u.getFullName());
////            System.out.println(u.isGender() ? "Nam" : "Nữ");
////            System.out.println(u.getStatus());
////        }
////
////        db.close();
//    }
            
}
