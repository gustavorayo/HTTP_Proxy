package http_Proxy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.SAXException;

public class RequestHandler implements Runnable {

    Socket clientSocket;
    Socket serverSocket;

    InputStream inFromClient;
    DataOutputStream outToClient;
    InputStream inFromServer;
    DataOutputStream outToServer;
    
    LogController proxylog=new LogController();
    CacheController cache = CacheController.getInstance();
    final int BUFFERSIZE = 1024 * 4;
    final String PROTOCOL="HTTP/1.0";
    final String SPACE=" ";
    final String METHOD="GET";
    final int NOTMODIFIED=304;
    
    public RequestHandler(Socket c) {
        this.clientSocket = c;
    }

    @Override
    public void run() {
        try {
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            inFromClient = clientSocket.getInputStream();              
            String clientRequest=ReadFromClient();
            String method=getMethod(clientRequest);
            
            
            if (method.equals(METHOD)) {
                URL url = getURL(clientRequest);
                String requestToServer = METHOD +SPACE+url.getPath()+SPACE+PROTOCOL+"\r\n\r\n";
                serverSocket = new Socket(url.getHost(), 80);
                
                outToServer = new DataOutputStream(serverSocket.getOutputStream());
                inFromServer = serverSocket.getInputStream();
                
                if (cache.hasValue(url.toString())) {
                    System.out.println("Request in chahe");
                    String date= getDate(new String(cache.getValue(url.toString()))) ;
                    
                    String condicionalRequest="";
                    condicionalRequest+=METHOD +SPACE+url.getPath()+SPACE+PROTOCOL+"\r\n";
                    condicionalRequest+="If-Modified-Since: "+date+"\r\n";
                    condicionalRequest+="\r\n\r\n";
                    
                    writeToServer(condicionalRequest);
                    ByteArrayOutputStream aBOS=ReadFromServer();
                    
                    String serResponse="";
                    int length;
                    
                    if(isModified(aBOS.toString())){
                        
                        writeToClient(aBOS.toByteArray());
                        cache.update(url.toString(), aBOS.toByteArray());
                        serResponse=aBOS.toString();
                        length=getLength(serResponse);
                        System.out.println("Date in Cache:"+date+"\t Date in Server "+getDate(serResponse));
                        System.out.println("File modified after:"+date);
                    }else{
                        System.out.println("File Not Modified");
                        writeToClient(cache.getValue(url.toString()));
                        serResponse=cache.getValue(url.toString()).toString();
                        length=getLength(serResponse);
                    }
                    proxylog.write(getDate(serResponse), clientSocket.getInetAddress().toString(), url.toString(), length);
                    
                } else {
                    writeToServer(requestToServer);
                    ByteArrayOutputStream x=ReadFromServer();
                    String serResponse=x.toString();
                    int length=getLength(serResponse);
                    proxylog.write(getDate(serResponse), clientSocket.getInetAddress().toString(), url.toString(), length);
                    cache.add(url.toString(), x.toByteArray());
                    writeToClient(x.toByteArray());
                }
            } else {
                String methodNotSupported = "HTTP/1.0 501 Not Implemented\r\n\r\n";
                writeToClient(methodNotSupported.getBytes());
                System.out.println("Method Not Implemented");
            }
        } catch (IOException e) {
        } catch (SAXException | ClassNotFoundException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                clientSocket.close();
            } catch (IOException e) {

            }
        }

    }
    
    private void writeToClient(byte[] response){
        try {
            outToClient.write(response, 0, response.length);
            outToClient.flush();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String ReadFromClient(){
        boolean endOfRequest = false;
        byte by[] = new byte[BUFFERSIZE];
        String clientRequest = "";
       do {
            try {
                inFromClient.read(by, 0, BUFFERSIZE);
                clientRequest += new String(by);
                if (clientRequest.contains("\r\n\r\n")) {
                    endOfRequest = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (endOfRequest == false);
        return clientRequest;
    }
    
    private void writeToServer(String requestToServer){
        try {
            outToServer.write(requestToServer.getBytes(), 0, requestToServer.getBytes().length);
            outToServer.flush();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private ByteArrayOutputStream ReadFromServer(){
        ByteArrayOutputStream response=new ByteArrayOutputStream();
        try {
            byte by1[] = new byte[BUFFERSIZE];
            int nextByte;
            nextByte=inFromServer.read(by1, 0, BUFFERSIZE);//the read method blocks until data is available;
            while(nextByte!=-1){
                response.write(by1,0,nextByte);
                nextByte=inFromServer.read(by1, 0, BUFFERSIZE);
            }
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    private URL getURL(String clientRequest){
        URL url=null;
        try {
            clientRequest=clientRequest.replaceFirst(":[0-9]* ", " ");
            String[] lines = clientRequest.split("\r\n");
            String tockens[] = lines[0].split("(\\s)");
            url = new URL(tockens[1]);
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return url;
    }
    
    private String getMethod(String clientRequest) {
        String tockens[] = clientRequest.split(" ");
        return tockens[0];
    }

    private int getLength(String serResponse){
        int length=-1;
        Matcher mcher=Pattern.compile("Content-Length:\\s\\w*").matcher(serResponse);
        boolean r=mcher.find();
        if(r){
          length=Integer.parseInt(mcher.group().replaceFirst("Content-Length:\\s", ""));
        }
        return length;
    }
    
    private String getDate(String serResponse){
        String date="";
        Matcher mcher=Pattern.compile("Date:\\s([a-zA-Z]){3},(\\w|\\s|:)*([A-Z]){3}",Pattern.UNIX_LINES).matcher(serResponse);
        boolean r=mcher.find();
        if(r){
          date=mcher.group().replaceFirst("Date:\\s", "");
        }
        return date;
    }

    private boolean isModified(String response) {
        String tockens[] = response.split(" ");
        int codeResponse=Integer.parseInt(tockens[1]);
        boolean result=(codeResponse!=NOTMODIFIED);
        return result;
    }
}
