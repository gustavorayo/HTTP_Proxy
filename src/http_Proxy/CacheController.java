package http_Proxy;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class CacheController {

    private static CacheController singleCache = null;
    Map<String, String> cache = new HashMap<String, String>();//It has to be global.
    String x = System.getProperty("user.dir");
    Path cacheFilePath = FileSystems.getDefault().getPath(x, "cache.xml");
    DocumentBuilderFactory factory
            = DocumentBuilderFactory.newInstance();

    private CacheController() {
        File f = cacheFilePath.toFile();
        if (!f.exists()) {
            try {
                f.createNewFile();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document testDoc = builder.newDocument();
                Element cacheElement = testDoc.createElement("cache");
                cacheElement.setTextContent("");
                cacheElement.setAttribute("id", "root");
                testDoc.appendChild(cacheElement);
                DOMSource source = new DOMSource(testDoc);

                PrintStream ps = new PrintStream(f);
                StreamResult result = new StreamResult(ps);

                TransformerFactory transformerFactory = TransformerFactory
                        .newInstance();
                
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty("encoding", "ISO-8859-1");
                transformer.transform(source, result);

            } catch (IOException ex) {
                Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
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

    public String getValue(String url) {
        return cache.get(url);
    }

    public boolean hasValue(String url) throws IOException, SAXException {
        DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
//    try {
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse(new File("test.xml"));
//        
//    } catch (ParserConfigurationException ex) {
//        return false;
//    }
        return cache.containsKey(url);
    }

    public synchronized void add(String key, String value) {
        try {
            File f = cacheFilePath.toFile();
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            FileReader freader=new FileReader(f);
            
            Document doc = builder.parse(new FileInputStream(f));
            
            Element newResponse=doc.createElement("response");
            newResponse.setAttribute("id", key);
            Element urlElement = doc.createElement("url");
            urlElement.setTextContent(key);
            Element contentElement = doc.createElement("content");
            contentElement.setTextContent(new String(value.getBytes(), "ISO-8859-1"));
            //contentElement.setTextContent(value);
            
            
            Element root=doc.getDocumentElement();
            root.appendChild(newResponse);
            newResponse.appendChild(urlElement);
            newResponse.appendChild(contentElement);
            
            
            DOMSource source = new DOMSource(doc);
            
            PrintStream ps = new PrintStream(f);
            StreamResult result = new StreamResult(ps);
            
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            transformer.transform(source, result);
            
            cache.put(key, value);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CacheController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Update the value in the file of the given key(first parameter) with the second parameter
    public synchronized void update(String key, String value){
        
        
    }

}
