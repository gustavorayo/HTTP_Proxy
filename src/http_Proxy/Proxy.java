
package http_Proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
		Server s=new Server();
		s.start();

	}

}
