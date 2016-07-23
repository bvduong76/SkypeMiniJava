/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects.room;

import objects.User;

/**
 *
 * @author Pham Thi Cam Duyen
 */
public class TwoUserRoom extends Room{
    public User peerUser;

    public TwoUserRoom(String roomId, User peerUser) {
//        super(peerUser.getUserName(),"PRIVATE");
        super(roomId,"PRIVATE");
        this.peerUser = peerUser;
    }

    public User getPeerUser() {
        return peerUser;
    }

    public void setPeerUser(User peerUser) {
        this.peerUser = peerUser;
    }
    
}
