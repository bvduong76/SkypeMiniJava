/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

/**
 *
 * @author NQH
 */
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioReceiver {

    private IncomingSoundListener isl = new IncomingSoundListener();
    private OutgoingSoundListener osl = new OutgoingSoundListener();

    AudioFormat format = getAudioFormat();
    InputStream is;
//    String serverName = "192.168.1.45";
    String serverName = "localhost";
    private ServerSocket serverSocket;
    private Socket listenerSocket;
    private Socket senderSocket;

    int port = 3456;
    boolean inVoice = true;
    boolean outVoice = true;

    public AudioReceiver(String host) throws IOException {
        serverSocket= new ServerSocket(3457);
        try {
            System.out.println("Connecting to server:" + host + " Port:" + port);
            senderSocket = new Socket(host, port);
            listenerSocket = serverSocket.accept();
            
            System.out.println("Connected to: " + listenerSocket.getInetAddress().getHostAddress());

            new Thread() {
                @Override
                public void run() {
                    isl.runListener();
                }
            }.start();

            new Thread() {
                @Override
                public void run() {
                    osl.runSender();
                }
            }.start();

        } catch (IOException ex) {
            Logger.getLogger(AudioReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void stop() {
        inVoice = false;
        outVoice = false;
        try {
            senderSocket.close();
            listenerSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(AudioReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }

    class IncomingSoundListener {

        byte[] buffer = new byte[1024];
        byte[] buffer2 = new byte[1024];

        public void runListener() {
            try {

                DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                speaker.open(format);
                speaker.start();
                System.out.println("Listening for incoming audio.");
                while (inVoice) {
                    is = listenerSocket.getInputStream();
//                    byte[] data = readFully(is);
                    int bytesRead = 0;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                        AudioInputStream ais = new AudioInputStream(bais, format, bytesRead);
                        if ((bytesRead = ais.read(buffer2)) != -1) {
                            speaker.write(buffer2, 0, bytesRead);
                            //bais.reset();
                        }
                        bais.close();
                    }
                }
                speaker.drain();
                speaker.close();
                System.out.println("Stopped listening to incoming audio.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class OutgoingSoundListener {

        public void runSender() {
            try {
                System.out.println("Listening from mic.");
                DataOutputStream out = new DataOutputStream(senderSocket.getOutputStream());
                DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(micInfo);
                mic.open(format);
                System.out.println("Mic open.");
                byte tmpBuff[] = new byte[2048];
                mic.start();
                while (outVoice) {
//                    System.out.println("Reading from mic.");
                    int count = mic.read(tmpBuff, 0, tmpBuff.length);
                    if (count > 0) {
//                        System.out.println("Writing buffer to server.");
                        out.write(tmpBuff, 0, count);
                    }
                }
                mic.drain();
                mic.close();
                System.out.println("Stopped listening from mic.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
