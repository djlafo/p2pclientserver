/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p_client;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 *
 * @author Dylan
 */
public class keyUpdater extends Thread {
    
    DefaultListModel keyModel;
    fileHostServer fhs;
    
    public keyUpdater(DefaultListModel keyModel, fileHostServer fhs) {
        this.keyModel = keyModel;
        this.fhs = fhs;
    }
    
    @Override
    public void run() {
        while(true) {
            keyModel.clear();
            keyModel.addElement("Keys:");
            List<String> cop = new ArrayList(fhs.keys);
            for(String s : cop) {
                keyModel.addElement(s + ":" + Math.abs(s.hashCode()%10));
            }
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
}
