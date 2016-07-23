/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import java.io.Serializable;

/**
 *
 * @author NQH
 */
public class FriendRequest implements Serializable{
    String username;
    String friendUsername;

    public FriendRequest(String username, String friendUsername) {
        this.username = username;
        this.friendUsername = friendUsername;
    }

    public String getUsername() {
        return username;
    }

    public String getFriendUsername() {
        return friendUsername;
    }
    
    
}
