package com.shamik.webserver;

/**
 * @author shamikm
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyWebServer extends Thread {

	ServerSocket listen_socket;
	String httpRootDir;

	public MyWebServer(String port, String httpRoot) {
		try {
			// set instance variables from constructor args
			int servPort = Integer.parseInt(port);
			httpRootDir = httpRoot;

			// create new ServerSocket
			listen_socket = new ServerSocket(servPort);

		} catch (IOException e) {
			System.err.println(e);
		}

		// Start running Server thread
		this.start();
	}

	public void run() {
		try {
			while (true) {
				// listen for a request. When a request comes in,
				// accept it, then create a Connection object to
				// service the request and go back to listening on
				// the port.

				Socket client_socket = listen_socket.accept();
				System.out.println("connection request received");
				Connection c = new Connection(client_socket, httpRootDir);
				System.out.println("The thread is alive - " + c.isAlive());
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	// simple "main" procedure -- create a TeenyWeb object from cmd-line args
	public static void main(String[] argv) {
		if (argv.length < 2) {
			System.out
					.println("usage: java -jar MyWebServer.jar <port> <http root directory>");
			return;
		}
		new MyWebServer(argv[0], argv[1]);
	}
}