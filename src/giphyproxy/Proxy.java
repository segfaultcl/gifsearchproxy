package giphyproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.util.concurrent.RateLimiter;

public class Proxy implements Runnable {
	private final Socket clientSocket;
	private final Pattern CONNECT_PATTERN = Pattern.compile("CONNECT (.+):(.+) HTTP/(1[.][01])", Pattern.CASE_INSENSITIVE);
	private final AllowedURLs allowedURLs;
	private final RateLimiter rateLimiter;

	public Proxy(Socket clientSocket, AllowedURLs allowedURLs, RateLimiter rateLimiter) {
		this.clientSocket = clientSocket;
		this.allowedURLs = allowedURLs;
		this.rateLimiter = rateLimiter;
	}
	
	private class SocketForwarder implements Runnable {
		private final InputStream input;
		private final OutputStream output;
		
		public SocketForwarder(InputStream input, OutputStream output) {
			this.input = input;
			this.output = output;
		}
		
		public void forward() {
			int bytesRead = 0;
			byte[] inBytes = new byte[4096];

			do {
				try {
					bytesRead = input.read(inBytes);

					//System.out.println(new String(inBytes));

					if (bytesRead > 0) {
						output.write(inBytes, 0, bytesRead);
						output.flush();
					}
					
					Arrays.fill(inBytes, (byte) 0);
				} catch (IOException e) {
					// Socket has closed
					return;
				}
			} while (bytesRead > 0);
		}

		@Override
		public void run() {
			forward();
		}
	}
	
	@Override
	public void run() {
		Socket serverSocket = null;


		try {
			boolean permitAcquired = rateLimiter.tryAcquire(5, TimeUnit.SECONDS);
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			OutputStreamWriter output = new OutputStreamWriter(clientSocket.getOutputStream());

			if (!permitAcquired) {
				output.write("HTTP/1.0 503 Service Unavailable\r\n\r\n");
				output.flush();
				
				clientSocket.close();
				return;
			}

			StringBuilder builder = new StringBuilder(); 

			do {
			    String line = input.readLine(); 

			    if (line.equals("")) { 
			    	break;
			    }

			    builder.append(line).append("\n");
			} while (true);

			String request = builder.toString();
			System.out.println("Request: " + request);
			Matcher matcher = CONNECT_PATTERN.matcher(request);
	        if (matcher.find()) {
	        	try {
	        		String remoteHost = matcher.group(1);
	        		int remotePort = Integer.parseInt(matcher.group(2));
	        		System.out.println("Connection to " + remoteHost + ":" + remotePort + " requested.");
	        		
	        		if (!allowedURLs.validURL(remoteHost)) {
	        			System.out.println(remoteHost + " is not a whitelisted address.");

	        			output.write("HTTP/" + matcher.group(3) + " 502 Bad Gateway\r\n\r\n");
	        			output.flush();
	        			
	        			return;
	        		}
	        		
	        		serverSocket = new Socket(remoteHost, remotePort);
	        		
	        		output.write("HTTP/" + matcher.group(3) + " 200 Connection established\r\n\r\n");
	        		output.flush();
	        		
	        		SocketForwarder serverInputToClient = new SocketForwarder(serverSocket.getInputStream(), clientSocket.getOutputStream());
	        		SocketForwarder clientInputToServer = new SocketForwarder(clientSocket.getInputStream(), serverSocket.getOutputStream());
	        		Thread serverInputToClientThread = new Thread(serverInputToClient);
	        		serverInputToClientThread.start();
	        		clientInputToServer.run();
	        	} catch (IOException | NumberFormatException e) {
	        		output.write("HTTP/" + matcher.group(3) + " 502 Bad Gateway\r\n\r\n");
	        		output.flush();
	        	}
	        } else {
	        	System.out.println("Received not implement request type.");
        		output.write("HTTP/1.1 501 Not Implemented\r\n\r\n");
        		output.flush();
	        }
		} catch (IOException e) {
			System.out.println("Socket operation failed. " + e.toString());
		} finally {
			try {
				if (clientSocket != null) {
					clientSocket.close();
				}

				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				System.out.println("Failed to close socket. " + e.toString());
			}
			
		}
	}
}
