package com.kissweb.webserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple web-server
 * 
 * @author Shamik Maity
 * <b>Email: shamik.maity@gmail.com</b>
 */


public class WebServer {

	private ServerSocket server_socket;
	private String http_root_dir;
	private int port;

	private Thread main_thread;
	private boolean isRunning;

	private RequestStructure request = new RequestStructure();

	private class RequestStructure {
		public String request_type;
		public String request;
		public String protocol;
		public String protocol_version;
		public String host;
		public String user_agent;

		public String toString() {
			return ("request_type = " + request_type + "\nrequest = " + request
					+ "\nprotocol = " + protocol + "\nprotocol_version = "
					+ protocol_version + "\nhost = " + host + "\nuser_agent = " + user_agent);
		}

	}

	/**
	 * Constructor: that accept the port and HTTP Root location
	 * 
	 * @param port
	 * @param httpRoot
	 */
	public WebServer(int port, String httpRoot) {
		http_root_dir = httpRoot;
		this.port = port;
	}

	/**
	 * 
	 * @param request
	 */
	private void parseRequestLine(String request) {
		String[] tokens = request.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("GET")) {
				this.request.request_type = "GET";
				i++;
				this.request.request = (tokens[i].trim().equals("/")) ? "/index.html"
						: tokens[i];
			} else if (tokens[i].startsWith("HTTP")) {
				this.request.protocol = "HTTP";
				this.request.protocol_version = tokens[i].substring(5);
			}
		}
	}

	private void processRequest(Socket accept) {
		BufferedReader in = null;
		BufferedWriter out = null;
		BufferedReader file = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					accept.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(
					accept.getOutputStream()));

			String request_line = null;
			while (!in.ready())
				;
			while (in.ready() && (request_line = in.readLine()) != null) {
				System.out.println(request_line);
				parseRequestLine(request_line);
			}

			File file_with_httpRootDir = new File(http_root_dir + request.request);

			String mime_type = "text/plain";
			if (request.request.endsWith(".html")) {
				mime_type = "text/html";
			} else if (request.request.endsWith(".jpg")) {
				mime_type = "image/jpg";
			} else if (request.request.endsWith(".js")) {
				mime_type = "text/javascript";
			}

			if (file_with_httpRootDir.exists()) {
				if (request.request_type == "GET") {
					out.write("HTTP/1.0 200 OK\n");
					out.write("Content-type: " + mime_type + "\n\n");
					file = new BufferedReader(new InputStreamReader(
							new FileInputStream(file_with_httpRootDir)));
					request_line = null;
					while (!file.ready())
						;
					while (file.ready()
							&& (request_line = file.readLine()) != null) {
						out.write(request_line);
					}

				}
			} else {
				out.write("HTTP/1.0 200 OK\n");
				out.write("Content-type: " + mime_type + "\n\n");
				out.write("404: not found: " + request.request);
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				if (file != null) {
					file.close();
					file = null;
				}
				if (out != null) {
					out.close();
					out = null;
				}
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (IOException e) {
				System.err.println(e);
			}

		}

	}

	/**
	 * Creates the server socket and starts the server.
	 *  
	 * @throws IOException
	 */
	public void start() throws IOException {
		System.out.println("WebServer started ...");
		System.out.println("http://localhost:" + port + "/");
		server_socket = new ServerSocket(port);
		isRunning = true;
		main_thread = new Thread(new Runnable() {

			public void run() {
				try {
					while (isRunning) {
						processRequest(server_socket.accept());
					}
					server_socket.close();
					server_socket = null;
				} catch (IOException e) {
					System.err.println(e);
				}
			}

		});
		main_thread.start();
	}

	/**
	 * Stops the web-server thread
	 * 
	 * @throws IOException
	 */
	public void stop() throws IOException {
		isRunning = false;
		if (!server_socket.isClosed()) {
			server_socket.close();
		}
	}

	public static void main(String[] arguments) {
		if (arguments.length < 2) {
			System.out
					.println("usage: java -jar WebServer.jar <port> <http root directory>");
		} else {
			WebServer server = new WebServer(Integer.parseInt(arguments[0]),
					arguments[1]);
			try {
				server.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}