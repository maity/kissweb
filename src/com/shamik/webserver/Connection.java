package com.shamik.webserver;

/**
 * @author shamikm
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

//The Connection class -- this is where HTTP requests are serviced

class Connection extends Thread {
	protected Socket client;
	// protected DataInputStream in;
	protected BufferedReader in;
	protected PrintStream out;
	String httpRootDir;
	String requestedFile;

	public Connection(Socket client_socket, String httpRoot) {
		// set instance variables from args
		httpRootDir = httpRoot;
		client = client_socket;

		// create input and output streams for conversation with client

		try {
			// in = new DataInputStream(client.getInputStream());
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			out = new PrintStream(client.getOutputStream());
		} catch (IOException e) {
			System.err.println(e);
			try {
				client.close();
			} catch (IOException e2) {
			}
			;
			return;
		}
		// start this object's thread -- the rest of the action
		// takes place in the run() method, which is called by
		// the thread.
		this.start();

	}

	public void run() {
		String line = null; // read buffer
		String req = null; // first line of request
		// OutputStream os;

		try {
			// read HTTP request -- the request comes in
			// on the first line, and is of the form:
			// GET <filename> HTTP/1.x

			req = in.readLine();

			// loop through and discard rest of request
			line = req;
			while (line.length() > 0) {
				line = in.readLine();
			}

			// parse request -- get filename
			StringTokenizer st = new StringTokenizer(req);
			// discard first token ("GET")
			st.nextToken();
			requestedFile = st.nextToken();

			// read in file

			// create File object
			File f = new File(httpRootDir + requestedFile);
			// check to see if file exists
			if (!f.canRead()) {
				sendResponseHeader("text/plain");
				sendString("404: not found: " + requestedFile);
				return;
			}

			// send response
			sendResponseHeader("text/html");

			// read in file
			FileInputStream fis = new FileInputStream(f);
			// DataInputStream fdis = new DataInputStream(fis);
			BufferedReader fdis = new BufferedReader(new InputStreamReader(fis));

			// send file to client
			line = fdis.readLine();
			while (line != null && line.length() > 0) {
				sendString(line);
				line = fdis.readLine();
			}

		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
			}
			;

		}

	}

	// send a HTTP header to the client
	// The first line is a status message from the server to the client.
	// The second line holds the mime type of the document
	void sendResponseHeader(String type) {
		out.println("HTTP/1.0 200 OK");
		out.println("Content-type: " + type + "\n\n");
	}

	// write a string to the client.
	void sendString(String str) {
		out.print(str);
	}

}