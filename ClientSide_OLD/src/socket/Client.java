/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket;

import global.Global;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 *
 * @author NQH
 */
public class Client {

    Thread thread;
    public ObjectInputStream is;
    public ObjectOutputStream os;
    Socket socket;
    Map<String, SocketMessageHandle> handleMap = new HashMap<>();
    public ServerSocket serverSocketFile ;
    public int filePort=4444;
    public void start() {
        try {
            socket = new Socket(Global.SERVER_HOST, Global.PORT);
            while(true){
                try{
                    serverSocketFile = new ServerSocket(filePort);
                    break;
                }catch(Exception e){
                    ++filePort;
                }
            }
            
            //khai báo các handle xử lý message
            declareHandleForThread(socket);
            
            System.out.println(socket.getPort());
            os = new ObjectOutputStream(socket.getOutputStream());
            os.flush();
            is = new ObjectInputStream(socket.getInputStream());
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            SocketMessage sMsg = (SocketMessage) is.readObject();
                            handleMessage(sMsg.flag, sMsg.args);
                        } catch (Exception ex) {
                        }
                    }
                }
            });
            thread.start();

        } catch (IOException e) {
            System.out.println("There're some error");
        }
    }

    synchronized public void sendMessage(String flag, Object... params) throws IOException {
        SocketMessage sMsg = new SocketMessage(flag, params);
        os.writeObject(sMsg);
        os.flush();
    }

    public void close() throws IOException {
        socket.close();
        os.close();
        is.close();
    }

    private void handleMessage(String flag, Object... params) {
        SocketMessageHandle handle = handleMap.get(flag);
        if(handle != null)
            handle.Handle(params);
    }

    public void declareHandle(String flag, SocketMessageHandle handle) {
        handleMap.put(flag, handle);
    }

//ví dụ declare
    private void declareHandleForThread(Socket socket) {
        declareHandle("receive", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                //In kết quả nhận đc
                System.out.println((int) params[0]);
            }
        });
        
        declareHandle("gui_lai_client", new SocketMessageHandle() {
            @Override
            public void Handle(Object... params) {
                //In kết quả nhận đc
                System.out.println((int) params[0]);
            }
        });
    }

}
