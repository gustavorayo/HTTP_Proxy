package http_Proxy;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;
public class RequestHandler implements Runnable{
	Socket clientSocket;
	Socket serverSocket;
	
	int id;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	BufferedReader inFromServer;
	DataOutputStream outToServer;
	String clientRequest = "";
	String clientResponse = "";
	String serverRequest = "";
	String serverResponse = "";
	
	Path logFile;
	
	//List<String> cache=new ArrayList<String>();
	
	final int BUFFERSIZE=10000;
	public RequestHandler(Socket c, int id) {
		this.clientSocket = c;
		this.id = id;
		System.out.println("Client Starter Id:" + id);
	}

	@Override
	public void run() {
		String	x= System.getProperty("user.dir");
		logFile=FileSystems.getDefault().getPath(x, "proxy.log");
		System.out.println("Client:" + id);
		CacheController cache=CacheController.getInstance();
		try {
			
			inFromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			outToClient = new DataOutputStream(clientSocket.getOutputStream());
			
			byte by[]=new byte[BUFFERSIZE];
			InputStream is=clientSocket.getInputStream();
			int index1;//=is.read(by, 0, BUFFERSIZE);
			boolean endOfRequest=false;
			
			do{
				index1=is.read(by, 0, BUFFERSIZE);
				clientRequest+=new String(by);
				if(clientRequest.contains("\r\n\r\n")){
					//clientRequest=clientRequest.replaceAll("HTTP/1.1", "HTTP/1.0");
					endOfRequest=true;
				}
				
			}while( endOfRequest==false);
			//End of client Request
			
			
			
			String[] lines = clientRequest.split("\r\n");
			
			 System.out.println("---------------Request["+lines.length+
			 "]------------"); for (String string : lines) {
			 System.out.println(string); }
			 System.out.println("---------------Response-----------");
			 

			String tockens[] = lines[0].split(" ");
			URL url = new URL(tockens[1]);// Takes just the url
			if(tockens[0].equals("GET")){
				
			String requestToServer="GET "+url.getPath()+" HTTP/1.0\r\n\r\n";
			System.out.println("request: "+requestToServer);
			
			// Open connection with the server
			serverSocket = new Socket(url.getHost(), 80);
			outToServer = new DataOutputStream(serverSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(
					serverSocket.getInputStream()));
			
			if(cache.hasValue(url.getPath()))
			{
				byte[] y=cache.getValue(url.getPath()).getBytes();
				System.out.println("In cache....");
				outToClient.write(y,0,y.length);
				outToClient.flush();
				
			}else{
			
			outToServer.write(requestToServer.getBytes(),0,requestToServer.getBytes().length);
			outToServer.flush();
			
			
			InputStream is1=serverSocket.getInputStream();
			//int index1=0;
			List<Byte> bts=new ArrayList<Byte>();
			
			String serResponse="";
			do{
				//c=is1.read();
				byte by1[]=new byte[BUFFERSIZE];
				index1=is1.read(by1, 0, BUFFERSIZE);//the read method blocks until data is available;
				//by1.toString();
				serResponse+=new String(by1);
			}while(index1!=-1);
			
			String linesServer[]=serResponse.split("\r\n");
			String firstLineTockens[]=linesServer[0].split(" ");
			
			String logLine=firstLineTockens[1]+" "+clientSocket.getInetAddress()+" "+url+"\r\n";
			Files.write(logFile,logLine.getBytes(),CREATE,APPEND);
			cache.add(url.getFile(), serResponse);
			System.out.println(cache.getValue(url.getPath()));
			byte[] y=serResponse.getBytes();
			System.out.println(serverResponse);
			outToClient.write(y,0,y.length);
			outToClient.flush();
			}
			}
			else{
				String methodNotSupporte="HTTP/1.0 501 Not Implemented\r\n\r\n";
				System.out.println(methodNotSupporte);
				byte[] y=methodNotSupporte.getBytes();
				outToClient.write(y,0,y.length);
				outToClient.flush();
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				if(serverSocket!=null){
					serverSocket.close();
				}
				clientSocket.close();
				System.out.println("closed:" + id);
			} catch (IOException e) {

			}
		}
		
		
	}



	
	
}
