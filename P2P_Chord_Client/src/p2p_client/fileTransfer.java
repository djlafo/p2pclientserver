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

import java.net.Socket;
import java.io.FileInputStream;
import java.io.File;
import java.io.OutputStream;

public class fileTransfer extends Thread {
    
    Socket s;
    File f;
    
    public fileTransfer(Socket s, File f) {
        this.s = s;
        this.f = f;
    }
    
    @Override
    public void run() {
        try {
            FileInputStream fi = new FileInputStream(f);
            OutputStream os = s.getOutputStream();
           
            os.write((f.length() + "\n").getBytes());
            os.flush();
            
            System.out.println("File length sent, now uploading file");
            
            int read = 0;
            byte[] b = new byte[1024];
            while ((read = fi.read(b, 0, b.length)) > 0) {
                os.write(b, 0, read);
            }
            os.flush();
            
            System.out.println("File uploaded complete");
            
            fi.close();
            os.close();
            s.close();
            
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
