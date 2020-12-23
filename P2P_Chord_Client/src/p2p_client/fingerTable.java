/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p_client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
/**
 *
 * @author Dylan
 */
public class fingerTable {
    
    fileHostServer.host[] table;
    int myId;
    int maxNodes;
    fileHostServer fhs;
    
    public fingerTable(int maxSize, int myId, fileHostServer.host successor, fileHostServer fhs) {
        table = new fileHostServer.host[log(maxSize, 2)];
        table[0] = successor;
        this.myId = myId;
        this.maxNodes = maxSize;
        this.fhs = fhs;
    }
    
    public synchronized void refresh() {
        System.out.println("*******************");
        System.out.println("Finger[0]:"+table[0].id);
        for(int i=1; i<table.length; i++) {
            table[i] = getNodeFor(i);
            System.out.println("Finger[" + i + "]:"+table[i].id);
        }
        System.out.println("*******************");
    }
    
    int pow(int no, int exp) {
        int o = 1;
        for(int i=0; i<exp; i++) {
            o *= no;
        }
        return o;
    }
    
    public void setSuccessor(fileHostServer.host suc) {
        table[0] = suc;
    }
    
    public fileHostServer.host getSuccessor() {
        return table[0];
    }
    
    private fileHostServer.host getNodeFor(int index) {
        fileHostServer.host lastHost = table[index-1];
        try {
            String[] current = {table[index-1].ip, table[index-1].port + "", table[index-1].id + ""};
            int targetID = (pow(2,index) + myId) % maxNodes;
            int currentID = (pow(2, index-1) + myId) % maxNodes;
            int originalID = currentID;
            while(inLoop(originalID, (pow(2,index)-pow(2,index-1)), currentID)) {
                lastHost = fhs.new host(current[0], Integer.parseInt(current[1]), Integer.parseInt(current[2]));
                Socket s = new Socket(lastHost.ip, lastHost.port);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                
                bw.write(fileHostServer.commands.successorQuery + "");
                bw.newLine();
                bw.flush();
                
                current = br.readLine().split(":");
                currentID = Integer.parseInt(current[2]);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return lastHost;
    }
    
    private boolean inLoop(int a, int range, int no) {
        int current = a;
        for(int i=0; i<=range; i++) {
            if(current==no)
                return true;
            current = (current+1) % maxNodes;
        }
        return false;
    }
    
    public synchronized fileHostServer.host closestTo(int ID) {
        int index = 0;
        for(int i=0; i<table.length; i++) {
            if(table[i].id < ID) {
                index = i;
            }
        }
        return table[index];
    }
    
    static int log(int x, int base)
    {
        return (int) (Math.log(x) / Math.log(base));
    }
}
