package http_Proxy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    int id;
    InputStream inFromClient;
    DataOutputStream outToClient;
    BufferedReader inFromServer;
    DataOutputStream outToServer;
    String clientRequest = "";
    String clientResponse = "";
    String serverRequest = "";
    String serverResponse = "";
    
    LogController proxylog=new LogController();

    
    final int BUFFERSIZE = 1024 * 4;

    public RequestHandler(Socket c, int id) {
        this.clientSocket = c;
        this.id = id;
    }

    @Override
    public void run() {
        CacheController cache = CacheController.getInstance();
        boolean endOfRequest = false;
        byte by[] = new byte[BUFFERSIZE];
        int nextByte;
        
        try {
            
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            inFromClient = clientSocket.getInputStream();        
            
            do {
                inFromClient.read(by, 0, BUFFERSIZE);
                clientRequest += new String(by);
                if (clientRequest.contains("\r\n\r\n")) {
                    endOfRequest = true;
                }
            } while (endOfRequest == false);
            
            String[] lines = clientRequest.split("\r\n");
            /*System.out.println("---------------Request[" + lines.length
                    + "]------------");
            for (String string : lines) {
                System.out.println(string);
            }
            System.out.println("---------------Response-----------");*/
            
            String tockens[] = lines[0].split(" ");
            URL url = new URL(tockens[1]);
            if (tockens[0].equals("GET")) {
                String requestToServer = "GET " + url.getPath() + " HTTP/1.0\r\n\r\n";
                System.out.print(requestToServer);
                serverSocket = new Socket(url.getHost(), 80);
                outToServer = new DataOutputStream(serverSocket.getOutputStream());
                if (cache.hasValue(url.getPath())) {
                    byte[] y = cache.getValue(url.getPath()).getBytes();
                    proxylog.write(clientRequest, serverRequest, clientRequest, id);
                    System.out.println("In cache....");
                    outToClient.write(y, 0, y.length);
                    outToClient.flush();
                } else {

                    outToServer.write(requestToServer.getBytes(), 0, requestToServer.getBytes().length);
                    outToServer.flush();
                    InputStream is1 = serverSocket.getInputStream();
                    String serResponse = "";               
                    ByteArrayOutputStream x=new ByteArrayOutputStream();
                    byte by1[] = new byte[BUFFERSIZE];
                    nextByte=is1.read(by1, 0, BUFFERSIZE);//the read method blocks until data is available;
                    while(nextByte!=-1){
                        x.write(by1,0,nextByte);
                        nextByte=is1.read(by1, 0, BUFFERSIZE);
                    }
                    serResponse=x.toString();
                    Matcher mcher=Pattern.compile("Content-Length:\\s\\w*").matcher(serResponse);
                    boolean r=mcher.find();
                    int length=-1;
                    if(r){
                      length=Integer.parseInt(mcher.group().replaceFirst("Content-Length:\\s", ""));
                    }
                    String serverLines[]=serResponse.split("\r\n");
                    String tockensFirstLine[]=serverLines[0].split(" ");
                    byte c[] = x.toByteArray( );
                    proxylog.write(tockensFirstLine[1], clientSocket.getInetAddress().toString(), url.getFile(), length);
                    
                    cache.add(url.getFile(), serResponse);
                    OutputStream os=clientSocket.getOutputStream();
                    
                    x.writeTo(os);
                    x.flush();
                }
            } else {
                String methodNotSupported = "HTTP/1.0 501 Not Implemented\r\n\r\n";
                System.out.println(methodNotSupported);
                byte[] message = methodNotSupported.getBytes();
                outToClient.write(message, 0, message.length);
                outToClient.flush();
            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (SAXException ex) {
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

}
