package http_Proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;

public class RequestHandler implements Runnable {
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
	public RequestHandler(Socket c, int id) {
		this.clientSocket = c;
		this.id = id;
		System.out.println("Client Starter Id:" + id);
	}

	@Override
	public void run() {
		int c;
		System.out.println("Client:" + id);

		try {
			// Recieve connection from the client
			// ---------------------------------
			inFromClient = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			outToClient = new DataOutputStream(clientSocket.getOutputStream());
			
			while ((c = inFromClient.read()) != -1) {
				clientRequest += (char) c;
				if (clientRequest.contains("\r\n\r\n")) {
					break;
				}
			}

			String[] lines = clientRequest.split("\r\n");
			/*
			 * System.out.println("---------------Request["+lines.length+
			 * "]------------"); for (String string : lines) {
			 * System.out.println(string); }
			 * System.out.println("---------------Response-----------");
			 */

			String tockens[] = lines[0].split(" ");
			URL url = new URL(tockens[1]);// Takes just the url

			// Open connection with the server
			serverSocket = new Socket(url.getHost(), 80);
			
			outToServer = new DataOutputStream(serverSocket.getOutputStream());
			//outToServer.writeBytes(clientRequest);
			inFromServer = new BufferedReader(new InputStreamReader(
					serverSocket.getInputStream()));
			
			while ((c = inFromServer.read()) != -1) {
				serverResponse += (char) c;
				if (serverResponse.contains("\r\n\r\n")) {
					break;
				}
			}
			serverResponse += inFromServer.readLine();
			outToClient.writeBytes(serverResponse);
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				serverSocket.close();
				clientSocket.close();
				System.out.println("closed:" + id);
			} catch (IOException e) {

			}
		}
	}

}
