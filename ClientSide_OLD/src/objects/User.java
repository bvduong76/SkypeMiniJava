/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pham Thi Cam Duyen
 */

public class User implements Serializable{

    public User() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private String userName;
    private String fullName;
    private String password;
    private boolean gender;
    private List<User> friendList;
    private String status;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(String userName) {
        this.userName = userName;
        this.password = "";
        this.fullName = "";
        this.gender = true;
        this.friendList = new ArrayList<User>();
        this.status = "OFFLINE";
    }
    
    public User(String userName,String password) {
        this.userName = userName;
        this.password = password;
        this.fullName = "";
        this.gender = true;
        this.friendList = new ArrayList<User>();
        this.status = "OFFLINE";
    }

    public User(String userName, String fullName, String password, boolean gender) {
        this.userName = userName;
        this.fullName = fullName;
        this.password = password;
        this.gender = gender;
        this.friendList = new ArrayList<User>();
        this.status = "OFFLINE";
    }

    public void addFriend(User user){
        friendList.add(user);
    }
    
    public void removeFriend(User user){
        friendList.remove(user);
    }
    
    public User getFriend(String userName){
        for(User user: friendList){
            if(user.getUserName().equals(userName)){
                return user;
            }
        }
        return null;
    }
    
    
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public List<User> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void printUser(){
        System.out.println(this.userName + "," + this.fullName + "," + this.password + "," + 
                this.gender + "," + this.status);
    }
}
