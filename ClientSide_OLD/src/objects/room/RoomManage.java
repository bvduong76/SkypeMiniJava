/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects.room;

import java.util.List;

/**
 *
 * @author Pham Thi Cam Duyen
 */
public class RoomManage {
    public Room getRoomById(List<Room> rooms,String roomId){
        for (Room room : rooms) {
            if(room.getRoomId().equals(roomId))
                return room;
        }
        return null;
    }
}
