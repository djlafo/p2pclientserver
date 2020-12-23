/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dylan
 */

import javax.swing.DefaultListModel;
import java.util.HashMap;
import java.util.List;

public class formUpdater extends Thread {
    
    int ms;
    hostServer hs;
    Thread t;
    DefaultListModel display;
    
    public formUpdater(int ms, hostServer hs, DefaultListModel display) {
        this.ms = ms;
        this.hs = hs;
        this.display = display;
    }
    
    public void setPeriod(int ms) {
        this.ms = ms;
    }
    
    public void startUpdater() {
        t = new Thread(this);
        t.start();
    }
    
    public void stopUpdater() {
        t.interrupt();
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                HashMap<String, List<String>> fileToHost = hs.getFileToHostMapping();
                display.clear();
                fileToHost.forEach((host, files) -> { 
                    display.addElement(host);
                    for (String s : files) {
                        display.addElement("\t" + s);
                    }
                });
                Thread.sleep(ms);
            } catch (InterruptedException ie) {
                
            }
        }
    }
}
