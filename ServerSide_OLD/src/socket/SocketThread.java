/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author NQH
 */
public class SocketThread extends Thread {

    private Socket socket;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private Map<String, SocketMessageHandle> handleMap = new HashMap<>();
    String username = null;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAdress() {
        if (socket == null) {
            return null;
        }
        InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        InetAddress inetAddress = socketAddress.getAddress();
        return inetAddress.getHostAddress();
    }

    public SocketThread(Socket socket) throws IOException {
        this.socket = socket;
        os = new ObjectOutputStream(socket.getOutputStream());
        os.flush();
        is = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        while (true) {
            try {
                SocketMessage sMsg = (SocketMessage) is.readObject();
                handleMessage(sMsg.flag, sMsg.args);
            } catch (IOException ex) {
                System.out.println("client out");
                 break;
                //client out
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void handleMessage(String flag, Object... params) {
        SocketMessageHandle handle = handleMap.get(flag);
        if(handle!=null)
            handle.Handle(this, params);
    }

    public void declareHandle(String flag, SocketMessageHandle handle) {
        handleMap.put(flag, handle);
    }

    synchronized public void sendMessage(String flag, Object... params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketMessage sMsg = new SocketMessage(flag, params);
                    os.writeObject(sMsg);
                    os.flush();
                } catch (IOException ex) {
                    Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
}
