package http_Proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	int PORT=8080;
	int cont=0;
	
	public void start(){
		ExecutorService executor=Executors.newFixedThreadPool(100);
		try {
			ServerSocket socket=new ServerSocket(PORT);
			while(true){
				Socket client = socket.accept();
				executor.execute(new RequestHandler(client,cont++));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	
	

}
