/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dylan
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class hostServer extends Thread {
    
    enum commands {
        add,
        remove,
        find,
        leave
    }
    
    ServerSocket ss;
    int port;
    HashMap<String, List<String>> fileToHost;
    // synchronized variable for fileToHost
    final Object o = new Object();
    
    public hostServer(int port) {
        this.port = port;
        fileToHost = new HashMap<>();
    }
    
    public hostServer() {
        fileToHost = new HashMap<>();
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void startServer() {
        start();
    }
    
    public void stopServer() {
        interrupt();
    }
    
    private String handleCommand(String[] command, String host) {
        commands c = commands.valueOf(command[0]);
        switch (c) {
            case add:
                if(command.length == 3) {
                    List<String> fileList = getFilesForHost(host+":"+command[2]);
                    synchronized(o) {
                        fileList.add(command[1]);
                    }
                    System.out.println("Added file " + command[1]);
                    return "ok";
                }
                break;
            case remove:
                if (command.length == 3) {
                    List<String> fileList = getFilesForHost(host+":"+command[2]);
                    synchronized(o) {
                        fileList.remove(command[1]);
                        if(fileList.isEmpty())
                            fileToHost.remove(host+":"+command[2]);
                    }
                    System.out.println("Removed file " + command[1]);
                    return "ok";
                }
                break;
            case find:
                if(command.length == 2)
                    return findHostWithFile(command[1]);
                break;
            case leave:
                if(command.length == 2) {
                    synchronized(o) {
                        fileToHost.remove(host+":"+command[1]);
                    }
                    return "ok";
                }
                break;
        }
        System.out.println("Invalid request");
        return "invalid";
    }
    
    private List<String> getFilesForHost(String host) {
        if (!fileToHost.containsKey(host))
            fileToHost.put(host, new ArrayList<>());
        return fileToHost.get(host);
    }
    
    private String findHostWithFile(String file) {
        String file2 = file.toLowerCase();
        Set<String> keys = fileToHost.keySet();
        String hosts = "";
        for (String k : keys) {
            List<String> files = fileToHost.get(k);
            for(String f : files) {
                if(f.toLowerCase().contains(file2)) {
                    hosts += k + ":" + f + "/";
                }
            }
        }
        return hosts;
    }
    
    public HashMap<String, List<String>> getFileToHostMapping() {
        synchronized (o) {
            return new HashMap<>(fileToHost);
        }
    }
    
    @Override
    public void run() {
        try {
            ss = new ServerSocket(port);
            System.out.println("Server online on port " + ss.getLocalPort());
            while(true) {
                Socket s = ss.accept();
                s.setSoTimeout(2000);
                System.out.println("Connection from " + s.getInetAddress().getHostAddress());
                
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String[] command = br.readLine().split(":");
                
                if(command.length > 0) {
                    System.out.println(commands.valueOf(command[0]) + " request");

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    String response = handleCommand(command, s.getInetAddress().getHostAddress());
                    bw.write(response + "\n");
                    bw.flush();

                    System.out.println("Response " + response);
                    bw.close();
                }
                br.close();
                s.close();
            }
        } catch(Exception e) {
            handleError(e);
        }
    }
    
    private void handleError(Exception e) {
        System.out.println(e.toString());
    }
}
