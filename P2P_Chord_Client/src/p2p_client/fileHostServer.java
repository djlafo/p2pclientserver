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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

public class fileHostServer extends Thread {
    
    public class host {
        public String ip;
        public int port;
        public int id;
        
        public host(String ip, int port, int id) {
            this.ip = ip;
            this.port = port;
            this.id = id;
        }
    };
    
    class request extends Thread{
        
        Socket s;
        
        public request(Socket s) {
            this.s = s;
        }
        
        void threadPrint(String s) {
            System.out.println("Thread " + this.getId() + ": " + s);
        }
        
        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String[] req = br.readLine().split(":");
                handle(req, br);
                br.close();
                s.close();
            } catch (Exception e) {
                handleException(e);
            }
        }
        
        synchronized void handle(String[] req, BufferedReader br) {
            switch(commands.valueOf(req[0])) {
                    case join:
                        threadPrint("Join request received.");
                        try {
                            if(ft.getSuccessor().id - id > 1 || (ft.getSuccessor().id - id < 0 && id != (maxNodes - 1)) || (ft.getSuccessor().id - id) == 0) {
                                
                                int newId;
                                if(ft.getSuccessor().id == 0) {
                                    if(ft.getSuccessor().id - id == 0) {
                                        newId = 1;
                                    } else if(id != (maxNodes -1)) {
                                        newId = id + 1;
                                    } else {
                                        newId = maxNodes - 1;
                                    }
                                } else {
                                    newId = ft.getSuccessor().id - 1;
                                }
                                
                                BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                bw2.write(ft.getSuccessor().ip + ":" + ft.getSuccessor().port + ":" + ft.getSuccessor().id);
                                bw2.newLine();
                                bw2.write(newId + "");
                                bw2.newLine();
                                bw2.write(id + "");
                                bw2.newLine();
                                bw2.flush();
                                bw2.close();
                                
                                if(ft.getSuccessor().id - id == 0) {
                                    predecessor = new host(s.getInetAddress().getHostAddress(), Integer.parseInt(req[1]), newId);
                                } else {
                                    Socket s2 = new Socket(ft.getSuccessor().ip, ft.getSuccessor().port);
                                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s2.getOutputStream()));
                                    bw.write(commands.updatePredecessor + ":" + s.getInetAddress().getHostAddress() + ":" + req[1] + ":" + newId);
                                    bw.newLine();
                                    bw.close();
                                    s2.close();
                                }
                                
                                ft.setSuccessor(new host(s.getInetAddress().getHostAddress(), Integer.parseInt(req[1]), newId));
                                
                                printStatus();
                            } else {
                                BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                bw2.write("ask"+":"+ft.getSuccessor().ip + ":" + ft.getSuccessor().port);
                                bw2.newLine();
                                bw2.flush();
                                bw2.close();
                            }
                        } catch (Exception e) {
                            handleException(e);
                        }
                        break;
                    case leave:
                        threadPrint("Leave request received.");
                        ft.setSuccessor(new host(req[1], Integer.parseInt(req[2]), Integer.parseInt(req[3])));
                        threadPrint("New successor id " + ft.getSuccessor().id + " at " + ft.getSuccessor().ip + " on port " + ft.getSuccessor().port);
                        try {
                            int keyLength = Integer.parseInt(br.readLine());
                            for(int i=0; i<keyLength; i++) {
                                String[] readKey = br.readLine().split(":");
                                addKey(readKey[0], readKey[1], Integer.parseInt(readKey[2]), Integer.parseInt(readKey[3]));
                            }
                        } catch (Exception e) {
                            handleException(e);
                        }
                        printStatus();
                        break;
                    case updatePredecessor:
                        threadPrint("Update Predecessor requested received");
                        predecessor = new host(req[1], Integer.parseInt(req[2]), Integer.parseInt(req[3]));
                        printStatus();
                        break;
                    case keyAdd:
                        threadPrint("Add Key request received");
                        if(req.length > 4) {
                            addKey(req[1], req[2], Integer.parseInt(req[3]), Integer.parseInt(req[4]));
                        } else {
                            addKey(req[1], s.getInetAddress().getHostAddress(), Integer.parseInt(req[2]), Integer.parseInt(req[3]));
                        }
                        break;
                    case keyRemove:
                        threadPrint("Remove key request received");
                        removeKey(req[1], Integer.parseInt(req[2]));
                        break;
                    case findFile:
                        try {
                            threadPrint("Received find request for " + req[1]);
                            
                            List<String> results = searchKeysForFile(req[1]);

                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                            bw.write(results.size() + "");
                            bw.newLine();
                            for(String s : results) {
                                bw.write(s);
                                bw.newLine();
                            }
                            bw.write(ft.getSuccessor().ip + ":" + ft.getSuccessor().port + ":" + ft.getSuccessor().id);
                            bw.newLine();
                            bw.flush();
                            
                            bw.close();
                        } catch (Exception e) {
                            handleException(e);
                        }
                        break;
                    case download:
                        threadPrint("Download request received for file " + req[1]);
                        for(File f : files) {
                            if(f.getName().equals(req[1])) {
                                fileTransfer ft = new fileTransfer(s, f);
                                ft.start();
                                try {
                                    ft.join();
                                } catch (Exception e) {
                                    handleException(e);
                                }
                                break;
                            }
                        }
                        break;
                    case successorQuery:
                        try {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                            bw.write(ft.getSuccessor().ip + ":" + ft.getSuccessor().port + ":" + ft.getSuccessor().id);
                            bw.newLine();
                            bw.flush();
                            bw.close();
                        } catch (Exception e) {
                            handleException(e);
                        }
                        break;
                    case updateFinger:
                        
                        int initialReq = Integer.parseInt(req[1]);
                        if(initialReq!=id) {
                            threadPrint("Received update finger table request from id " + initialReq);
                            ft.refresh();
                            notifySuccessor(initialReq);
                        }
                        break;
                    default:
                        threadPrint("Invalid Request.");
                        break;
                }
        }
    };
    
    enum commands {
        join,
        leave,
        updatePredecessor,
        keyAdd,
        keyRemove,
        findFile,
        download,
        successorQuery,
        updateFinger
    }
    
    ServerSocket ss;
    
    int id;
    int maxNodes;
    host predecessor;
    
    List<File> files;
    
    List<String> keys;
    List<host> keyHosts;
    
    fingerTable ft;
    
    public fileHostServer(int port, int maxNodes) {
        try {
            this.maxNodes = maxNodes;
            files = new ArrayList<>();
            keys = new ArrayList<>();
            keyHosts = new ArrayList<>();
            System.out.println("Starting server...");
            ss = new ServerSocket(port);
            System.out.println("Server started on port " + ss.getLocalPort());
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    public void addFile(File f) {
        addKey(f.getName(), "127.0.0.1", ss.getLocalPort(), id);
        files.add(f);
    }
    
    int getHash(String s) {
        return Math.abs(s.hashCode()%10);
    }
    
    public void addKey(String key, String ip, int port, int oId) {
        int fKey = getHash(key);
        System.out.println("File " + key + " fKey " + fKey + " ip " + ip + " port " + port + " id " + oId);
        if((fKey > this.id && fKey < ft.getSuccessor().id) || (ft.getSuccessor().id < this.id && fKey > this.id)) {
            keys.add(key);
            keyHosts.add(new host(ip, port, oId));
        } else {
            try {
                host best = ft.closestTo(fKey);
                System.out.println("Closest finger to file hash " + fKey + " is host ID "+best.id);
                Socket s2 = new Socket(best.ip, best.port);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s2.getOutputStream()));
                if(ip.equalsIgnoreCase("127.0.0.1")) {
                    bw.write(commands.keyAdd + ":" + key + ":" + port + ":" + oId);
                } else {
                    bw.write(commands.keyAdd + ":" + key + ":" + ip + ":" + port + ":" + oId);
                }
                bw.newLine();
                bw.flush();
                bw.close();
                s2.close();
            } catch (Exception e) {
                handleException(e);
            }
        }
    }
    
    public void removeFile(String file) {
        removeKey(file, id);
    }
    
    public void removeKey(String file, int oId) {
        System.out.println("File " + file + " id " + oId);
        boolean removed = false;
        for(int i=0; i<keys.size(); i++) {
            if(keys.get(i).equals(file) && keyHosts.get(i).id == oId) {
                keys.remove(i);
                keyHosts.remove(i);
                removed = true;
                break;
            }
        }
        if(!removed) {
            System.out.println("Forwarding delete request");
            try {
                Socket s = new Socket(ft.getSuccessor().ip, ft.getSuccessor().port);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                bw.write(commands.keyRemove + ":" + file + ":" + oId);
                bw.newLine();
                bw.flush();
                bw.close();
                s.close();
            } catch (Exception e) {
                handleException(e);
            }
        }
    } 
    
    List<String> searchKeysForFile(String f) {
        List<String> results = new ArrayList<>();
        for(int i=0; i<keys.size(); i++) {
            if(keys.get(i).toLowerCase().contains(f.toLowerCase()))
                results.add(keys.get(i)+":"+keyHosts.get(i).ip + ":" + keyHosts.get(i).port);
        }
        return results;
    }
    
    List<String> findFile(String f) {
        List<String> results = searchKeysForFile(f);
        int hash = getHash(f);
        host best = ft.closestTo(hash);
        System.out.println("String input hash " + hash+ ", best finger " + best.id);
        String[] next = {best.ip, ""+best.port, best.id+""};
        try {
            while(Integer.parseInt(next[2])!=id) {
                System.out.println("Contacting " + next[0] + ":" + Integer.parseInt(next[1]) + " for file query");
                Socket s = new Socket(next[0], Integer.parseInt(next[1]));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                
                bw.write(commands.findFile + ":" + f);
                bw.newLine();
                bw.flush();
                
                int noResults = Integer.parseInt(br.readLine());
                for(int i=0; i<noResults; i++) {
                    String reply = br.readLine();
                    reply = reply.replace("127.0.0.1", next[0]);
                    results.add(reply);
                }
                
                next = br.readLine().split(":");
            }
        } catch (Exception e) {
            handleException(e);
        }
        System.out.println("Finished searching");
        return results;
    }
    
    public void startChord() {
        id = 0;
        ft = new fingerTable(maxNodes,id, new host("127.0.0.1", ss.getLocalPort(), 0), this);
        start();
    }
    
    public void joinChord(String ip, int port) {
        try {
            Socket s = new Socket(ip, port);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write(commands.join + ":" + ss.getLocalPort());
            bw.newLine();
            bw.flush();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String[] suc = br.readLine().split(":");
            
            if(suc[0].equalsIgnoreCase("ask")) {
                joinChord(suc[1], Integer.parseInt(suc[2]));
            } else {
                id = Integer.parseInt(br.readLine());
                predecessor = new host(ip, port, Integer.parseInt(br.readLine()));
                ft = new fingerTable(maxNodes, id, (new host(suc[0], Integer.parseInt(suc[1]), Integer.parseInt(suc[2]))), this);
                printStatus();
                notifySuccessor(id);
                ft.refresh();
                start();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    void notifySuccessor(int oId) {
        try {
            Socket s = new Socket(ft.getSuccessor().ip, ft.getSuccessor().port);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write(commands.updateFinger + ":" + oId);
            bw.newLine();
            bw.flush();
            bw.close();
            s.close();
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    void printStatus() {
        System.out.println("*************************");
        System.out.println("Host id " + id);
        System.out.println("Successor at " + ft.getSuccessor().ip + " port " + ft.getSuccessor().port + " id " + ft.getSuccessor().id);
        System.out.println("Predecessor at " + predecessor.ip + " port " + predecessor.port + " id " + predecessor.id);
        System.out.println("*************************");
    }
    
    @Override
    public void run() {
        System.out.println("Starting server thread...");
        try {
            while(true) {
                Socket s = ss.accept();
                request r = new request(s);
                r.start();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    public void leaveChord() {
        try {
            System.out.println("Attempting to leave chord loop...");
            if(predecessor != null) {
                Socket s2 = new Socket(ft.getSuccessor().ip, ft.getSuccessor().port);
                BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(s2.getOutputStream()));
                bw2.write(commands.updatePredecessor + ":" + predecessor.ip + ":" + predecessor.port + ":" + predecessor.id);
                bw2.newLine();
                bw2.flush();
                bw2.close();
                s2.close();
                
                Socket s = new Socket(predecessor.ip, predecessor.port);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                bw.write(commands.leave + ":" + ft.getSuccessor().ip + ":" + ft.getSuccessor().port + ":" + ft.getSuccessor().id);
                bw.newLine();
                bw.write(keys.size() + "");
                bw.newLine();
                for(int i=0; i<keys.size(); i++) {
                    bw.write(keys.get(i) + ":" + keyHosts.get(i).ip + ":" + keyHosts.get(i).port + ":" + keyHosts.get(i).id);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
                s.close();
            }
            
            ss.close();
            System.out.println("Server closed");
            interrupt();
        } catch (Exception e) {
            handleException(e);
        }
    }
     
    static void handleException(Exception e) {
        System.out.println(e.getCause());
    }
}
