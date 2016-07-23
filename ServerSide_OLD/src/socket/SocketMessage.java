/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket;

import java.io.Serializable;

/**
 *
 * @author NQH
 */
public class SocketMessage implements Serializable{
    public String flag;
    public Object[] args;
        public SocketMessage(String flag, Object[] args) {
        this.flag = flag;
        this.args = args;
    }
}
