/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p_client;

/**
 *
 * @author Dylan
 */

import java.io.File;
import java.net.ServerSocket;
import java.util.List;
import java.util.ArrayList;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class fileHostServer extends Thread {
    
    enum commands {
        add,
        remove,
        find,
        leave
    }
    
    ServerSocket ss;
    int localPort;
    String serverIP;
    int serverPort;
    List<File> files;
    
    
    public fileHostServer(int localPort, String serverIP, int serverPort) {
        this.localPort = localPort;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        files = new ArrayList<>();
    }
    
    public boolean addFile(File f) {
        try {
            Socket s = new Socket(serverIP, serverPort);
            
            System.out.println("Connection made to main server");
            
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            
            bw.write(commands.add + ":" + f.getName() + ":" + localPort + "\n");
            bw.flush();
            
            System.out.println("Sent add file request");
            
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String response = br.readLine();
          
            System.out.println("Received response " + response);
            
            br.close();
            bw.close();
            s.close();
            
            files.add(f);
            
            return true;
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }
    
    public boolean removeFile(String s) {
        for (File f : files) {
            if (f.getName().equals(s)) {
                files.remove(f);
                break;
            }
        }
        
        try {
            Socket sock = new Socket(serverIP, serverPort);
            
            System.out.println("Connection made to main server");
            
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            
            bw.write(commands.remove + ":" + s + ":" + localPort + "\n");
            bw.flush();
            
            System.out.println("Sent remove file request");
            
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String response = br.readLine();
            
            System.out.println("Received response " + response);
            
            br.close();
            bw.close();
            sock.close();
            
            return true;
        } catch (Exception e) {
            handleException(e);
        }
        return false;
    }
    
    public static String searchForFile(String host, int port, String file) {
        String response = "";
        try {
            Socket s = new Socket(host, port);
            
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write(commands.find + ":" + file + "\n");
            bw.flush();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            do {
                response += br.readLine();
            } while (br.ready());
            
            bw.close();
            br.close();
            s.close();
            
        } catch (Exception e) {
            handleException(e);
        }
        return response;
    }
    
    @Override
    public void run() {
        try {
            ss = new ServerSocket(localPort);
            localPort = ss.getLocalPort();
            System.out.println("Server online on port " + ss.getLocalPort());
            while(true) {
                Socket s = ss.accept();
                s.setSoTimeout(2000);
                System.out.println("Connection from " + s.getInetAddress().getHostName());
                
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String file = br.readLine();
                
                System.out.println("File " + file + " requested");
                boolean sending = false;
                for (File f : files) {
                    if (f.getName().equals(file)) {
                        fileTransfer ft = new fileTransfer(s, f);
                        ft.start();
                        sending = true;
                        break;
                    }
                }
                
                if(!sending) {
                    s.close();
                    System.out.println("File not on server. Closing connection");
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    public void requestLeave() {
        try {
            Socket s = new Socket(serverIP, serverPort);
            
            System.out.println("Connection made to main server");
            
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            
            bw.write(commands.leave + ":" + localPort + "\n");
            bw.flush();
            
            System.out.println("Sent leave request");
            
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String response = br.readLine();
          
            System.out.println("Received response " + response);
            
            br.close();
            bw.close();
            s.close();
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    public void stopServer() {
        try {
            interrupt();
            ss.close();
            System.out.println("Server offline");
            requestLeave();
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    public static void handleException(Exception e) {
        System.err.println("File Host Server: " + e.toString());
    }
    
}
