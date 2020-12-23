/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p_client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Dylan
 */
public class fileListHandler {
    
    File f;
    
    public fileListHandler() {
        try {
            f = new File("files.txt");
            if(!f.exists()) {
                f.createNewFile();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    public boolean addFile(File f) {
        try {
            Files.write(Paths.get("files.txt"), (f.getPath() + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
        return true;
    }
    
    public void removeFile(String f) {
        try {
            String newFile = "";
            List<String> fileList = Files.readAllLines(Paths.get("files.txt"));
            for(String s2 : fileList) {
                if (!s2.endsWith(f))
                    newFile += s2 + System.getProperty("line.separator");
            }

            Files.write(Paths.get("files.txt"), newFile.getBytes(), StandardOpenOption.WRITE);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    public List<File> loadFiles() {
        List<File> fileList = new ArrayList<>();
        try {
            System.out.println("Reading file list from files.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String read;
            while((read = br.readLine()) != null) {
                fileList.add(new File(read));
            }
            br.close();
        } catch (Exception e) {
            handleException(e);
        }
        return fileList;
    }
    
    private void handleException(Exception e) {
        System.err.println("File Host Server: " + e.toString());
    }
}
