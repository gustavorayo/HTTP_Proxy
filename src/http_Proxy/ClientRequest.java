package http_Proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRequest extends Observable implements Runnable {
	Socket client;
	BufferedReader inFromClient;
	InputStream inputStream;
	DataOutputStream outToClient;
	int index=0;
	final int BUFFERSIZE=10000;
	String clientRequest="";
	int type=-1;
	public ClientRequest(Socket c,int t){
		this.client=c;
		this.type=t;
	}
	public void run() {
		try {
			inputStream=client.getInputStream();
			outToClient = new DataOutputStream(client.getOutputStream());
			byte by[]=new byte[BUFFERSIZE];
			do{
				index=inputStream.read(by, 0, BUFFERSIZE);
				clientRequest+=new String(by);
				if(clientRequest.contains("\r\n\r\n")){
					setChanged();
					notifyObservers(clientRequest);
				}
			}while(index!=-1);
		} catch (IOException e) {
			
		}
	}
	
	public int getType(){
		return type;
	}
	
/*	@Override
		Queue<String> clientRequests=new LinkedList<String>();
	Queue<String> serverResponses=new LinkedList<String>();
	
	public void update(Observable arg0, Object arg1) {
		ClientRequest cr=(ClientRequest)arg0;
		if(cr.getType()==1){
			clientRequests.add((String)arg1);
		}else
		{
			serverResponses.add((String)arg1);
		}
		
				ClientRequest cr=new ClientRequest(clientSocket,1);
		ClientRequest sr=new ClientRequest(serverSocket,2);
		ExecutorService executor=Executors.newFixedThreadPool(10);
		
		cr.addObserver(this);
		Thread thread = new Thread(cr);
        thread.start();
        
		while(true){
			//System.out.println(clientRequests.peek());
			if(clientRequests.size()>0){
				System.out.println(clientRequests.poll());
				
			}
			if(serverResponses.size()>0){
				System.out.println(serverResponses.poll());
			}
		}
	}*/

}
