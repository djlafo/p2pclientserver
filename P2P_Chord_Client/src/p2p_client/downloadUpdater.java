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

import javax.swing.DefaultListModel;

public class downloadUpdater extends Thread {
    
    fileDownload fd;
    DefaultListModel model;
    int ms;
    
   public downloadUpdater(fileDownload fd, DefaultListModel model, int ms) {
       this.fd = fd;
       this.model = model;
       this.ms = ms;
   } 
   
   @Override
   public void run() {
       try {
           String last;
            do {
                last = fd.getFileName() + " " + String.format("%.2g%n", fd.getPercentDownloaded());
                model.addElement(last);
                Thread.sleep(ms);
                model.removeElement(last);
            } while(fd.getPercentDownloaded() < 1 && fd.isAlive());
            
       } catch (Exception e) {
           System.err.println("Download updater: " + e.toString());
       }
   }
}
