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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;

public class fileDownload extends Thread {
    
    String host;
    int port;
    String file;
    
    long fileLength;
    final Object o = new Object();
    float percentDownloaded;
    
    
    public fileDownload(String host, int port, String file) {
        this.host = host;
        this.port = port;
        this.file = file;
    }
    
    public float getPercentDownloaded() {
        synchronized(o) {
            return percentDownloaded;
        }
    }
    
    public String getFileName() {
        return file;
    }
    
    @Override
    public void run() {
        File f = new File(file);
        try {
            f.createNewFile();
            Socket s = new Socket(host, port);
            
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            FileOutputStream fo = new FileOutputStream(f);
            InputStream is = s.getInputStream();
            
            bw.write(fileHostServer.commands.download + ":" + file);
            bw.newLine();
            bw.flush();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            fileLength = Long.parseLong(br.readLine());
            
            long downloaded = 0;
            byte[] c = new byte[1024];
            int read = 0;
            while ((read = is.read(c, 0, c.length)) > 0) {
                fo.write(c, 0, read);
                downloaded += read;
                synchronized(o) {
                    percentDownloaded = (float)downloaded / fileLength;
                }
            }
            percentDownloaded = 1f;
            fo.flush();
            
            br.close();
            bw.close();
            fo.close();
            is.close();
            s.close();
        } catch (NumberFormatException nf) {
            if(f.exists())
                f.delete();
            System.err.println("File Downloader: file length from server was either null or invalid");
        } catch (Exception ex) {
            if(f.exists())
                f.delete();
            System.err.println("File Downloader: " + ex.toString());
        }
    }
}
