/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import objects.User;
import objects.room.Room;
import objects.room.TwoUserRoom;

/**
 *
 * @author Pham Thi Cam Duyen
 */
public class AddRoomDemo {
    
    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }
    
//    public static void main(String[] args) {
////        User user1 = DatabaseAccess.getInstace().getUserInfo("Duyen95");
////        user1.printUser();
////        
////        User user2 = DatabaseAccess.getInstace().getUserInfo("Duong123");
////        user2.printUser();
////        
////        TwoUserRoom room = new TwoUserRoom(user2);
////        String roomId = UUID.randomUUID().toString();
////        
////        System.out.println(roomId);
////        System.out.println(room.getType());
////        
////        DatabaseAccess3.getInstace().addRoom(roomId,"room",room.getType());
////        DatabaseAccess3.getInstace().addMemberToRoom(roomId, user1.getUserName());
////        DatabaseAccess3.getInstace().addMemberToRoom(roomId,user2.getUserName());
//        
//        List<Room> listRoom = DatabaseAccess3.getInstace().getAllRoomOfUser("Duyen95");
//        for (Room r : listRoom) {
//            System.out.println(r.getRoomId() + ";" + r.getType() + ";" );
//        } 
//        List<Room> listRoom1 = DatabaseAccess3.getInstace().getAllRoomOfUser("Duong123");
//        for (Room r : listRoom1) {
//            System.out.println(r.getRoomId() + ";" + r.getType() + ";" );
//        } 
//        String s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
//        System.out.println(s);
//    }
    
    
}
