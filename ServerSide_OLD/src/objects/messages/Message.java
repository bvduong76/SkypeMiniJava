/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects.messages;

import java.io.Serializable;

/**
 *
 * @author Pham Thi Cam Duyen
 */
public class Message implements Serializable{
    private String messageId;
    private String userName;
    private String time;
    private String type;
    private String content;

    public Message(String messageId, String userName, String time, String type, String content) {
        this.messageId = messageId;
        this.userName = userName;
        this.time = time;
        this.type = type;
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    

}
