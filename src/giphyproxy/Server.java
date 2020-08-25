package giphyproxy;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {
	private final int port;
	private final AllowedURLs allowedURLs = new AllowedURLs();
	private ExecutorService threadPool = Executors.newFixedThreadPool(50);
	
	public Server(int port) {
		this.port = port;
	}

	public void run() {
		SSLServerSocket serverSocket = null;
		try {
			SSLServerSocketFactory sslssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			serverSocket = (SSLServerSocket) sslssf.createServerSocket(port);

			while (true) {
				SSLSocket socket  = (SSLSocket) serverSocket.accept();

	            threadPool.execute(new Proxy(socket, allowedURLs));
			}

		} catch (IOException e) {
			System.out.println("Server cannot bind to port " + port + ". Exiting.");
			System.out.println(e.toString());
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.out.println("Failed to close socket. " + e.toString());
				}
			}
		}
	}


	public static void main(String[] args) throws IOException {
		int port = 7427;
		
		if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);    
			} catch (NumberFormatException e) {
				System.out.println(args[0] + " is not a valid port. Defaulting to port " + port);
			}
			
			if (port < 1024 || port > 65535) {
				port = 8443;
				System.out.println(args[0] + " is not a valid port. Defaulting to port " + port);
			}
		}
		
		new Server(port).run();
	}
}