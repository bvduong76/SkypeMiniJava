/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects.room;

import java.util.ArrayList;
import java.util.List;
import objects.User;

/**
 *
 * @author Pham Thi Cam Duyen
 */
public class GroupRoom extends Room{
    private String title;
    private List<User> listUser;
    public GroupRoom(String roomId, String title){
        super(roomId,"GROUP");
        this.title=title;
        listUser=new ArrayList<>();
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<User> getListUser() {
        return listUser;
    }

    public void setListUser(List<User> listUser) {
        this.listUser = listUser;
    }
    
    public void addUser(User user){
        this.listUser.add(user);
    }
    
    public void removeUser(String username){
        for(User user:listUser){
            if(user.getUserName().equals(username)){
                listUser.remove(user);
                break;
            }
        }
    }
    
    
    
}
