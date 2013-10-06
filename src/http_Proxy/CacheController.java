package http_Proxy;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

public class CacheController {

    private static CacheController singleCache = null;
    Hashtable<String, byte[]> cache = new Hashtable<>();
    String dir = System.getProperty("user.dir");
    Path cacheFilePath = FileSystems.getDefault().getPath(dir, "cache.txt");
    
    private CacheController() {
        File f = cacheFilePath.toFile();
        if (!f.exists()) {
            try {
                f.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                ObjectOutputStream oos=new ObjectOutputStream(fos);
                oos.writeObject(cache);
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static synchronized CacheController getInstance() {
        if (singleCache == null) {
            singleCache = new CacheController();
            return singleCache;
        } else {
            return singleCache;
        }
    }

    public byte[] getValue(String url) throws IOException, ClassNotFoundException {
            File f = cacheFilePath.toFile();
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fos);
            Object obj = ois.readObject();
            if (obj instanceof Hashtable)
            {
                cache = (Hashtable<String, byte[]>) obj;
            }
            ois.close();
            System.out.println("Returned form Cache:"+url);
            return cache.get(url);
    }

    public boolean hasValue(String url) throws IOException, SAXException, ClassNotFoundException {
            File f = cacheFilePath.toFile();
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            if (obj instanceof Hashtable)
            {
                cache = (Hashtable<String, byte[]>) obj;
            }
            ois.close();
            
            boolean result=cache.containsKey(url);
            return result;
    }

    public synchronized void add(String key, byte[] value) throws FileNotFoundException, IOException {
        FileOutputStream fos = null;        
            File f = cacheFilePath.toFile();
            fos = new FileOutputStream(f);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            cache.put(key, value);
            oos.writeObject(cache);
            oos.close();
            System.out.println("File Added:"+key);
    }
    
    //Update the value in the file of the given key(first parameter) with the second parameter
    public synchronized void update(String key, byte[] value) throws IOException, ClassNotFoundException{          
            
            
            //load hashtable from file
            File f = cacheFilePath.toFile();
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            if (obj instanceof Hashtable)
            {
                cache = (Hashtable<String, byte[]>) obj;
                
            }
            ois.close();
            
            //Update hashtable
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            cache.put(key, value);
            oos.writeObject(cache);
            oos.close();
            System.out.println("File Updated:"+key);
    }

}
