/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects.room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import objects.User;
import objects.messages.Message;

/**
 *
 * @author Pham Thi Cam Duyen
 */
public abstract class Room implements Serializable{
    protected List<Message> listMessage;
    protected String roomId;
    protected String type;
    public Room(String roomId, String type){
        this.roomId=roomId;
        this.type=type;
        this.listMessage= new ArrayList<>();
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public void addMessage(Message msg){
        listMessage.add(0,msg);
    }

    public Room() {
        listMessage = new ArrayList<Message>();
    }
    public List<Message> getListMessage() {
        return listMessage;
    }

    public void setListMessage(List<Message> listMessage) {
        this.listMessage = listMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
}
