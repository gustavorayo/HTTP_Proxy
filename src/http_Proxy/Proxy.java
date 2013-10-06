
package http_Proxy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Gustavo Rayo
 *
 */
public class Proxy {

	/**
	 * @param args<=URL
	 * 
	 */
	public static void main(String[] args) {
            	int PORT=8080;
                int MAXCLIENT=200;
		ExecutorService executor=Executors.newFixedThreadPool(MAXCLIENT);
		try {
			ServerSocket socket=new ServerSocket(PORT);
			while(true){
				Socket client = socket.accept();
				executor.execute(new RequestHandler(client));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
