package http_Proxy;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LogController {
    Path logFile;
    String dir = System.getProperty("user.dir");
    
    
    public synchronized void write(String date, String browserIP, String URL,int size) {
        try {
            browserIP=browserIP.replace("/", "");
            logFile = FileSystems.getDefault().getPath(dir, "proxy.log");
            String logLine = date+ " " + browserIP + " " + URL +" "+size+ "\r\n";
            Files.write(logFile, logLine.getBytes(), CREATE, APPEND);
        } catch (IOException ex) {
            Logger.getLogger(LogController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
