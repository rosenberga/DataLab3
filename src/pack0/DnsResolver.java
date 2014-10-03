package pack0;

import java.io.*;
import java.net.*;
import java.util.*;

public class DnsResolver {
	private int port;
	private static final String rootServerFile = "RootFile.txt";
	private Map<String, Map<String, Cache>> serverCache;
	private DatagramSocket serverSocket;

	public static void main(String[] args) {
		try {
			new DnsResolver(args[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DnsResolver(String port) throws IOException {

		// set up basic cache
		this.port = Integer.parseInt(port);
		serverCache = new HashMap<String, Map<String, Cache>>();
		readRootFile();
		// set up server socket and receive packet
		serverSocket = new DatagramSocket(this.port);
		try {
			while (true) {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);

				// wait to receive a packet
				System.out.println("Waiting for client to connect");
				serverSocket.receive(receivePacket);

				// flip the recursive bit
				receivePacket.setData(flipRec(receivePacket));

				// check to see if we have cached the value before
				if (!inCache(receivePacket)) {
					askServer(receivePacket);
				}

				// TODO
				// send to user
				// need to flip the bytes again to 80 before you send the
				// response back to dig

			}
		} catch (Exception e) {
			// close the server socket
			serverSocket.close();
			return;
		}
	}

	private void askServer(DatagramPacket receivePacket) {
		// TODO Auto-generated method stub

	}

	private boolean inCache(DatagramPacket receivePacket) {
		// TODO Auto-generated method stub
		return false;
	}

	private byte[] flipRec(DatagramPacket receivePacket) {
		// set the recursion desired bit to 0
		byte[] flip = receivePacket.getData();
		flip[2] = 0;
		return flip;

	}

	private void readRootFile() throws IOException {
		String line;
		Map<String, Cache> root = new HashMap<String, Cache>();
		BufferedReader br = new BufferedReader(new FileReader(rootServerFile));

		while ((line = br.readLine()) != null) {
			if (line.substring(0, 1).equalsIgnoreCase(".")) {
				line = br.readLine();
				if (line != null) {
					line = line.replaceAll("\\s+", "%");
					String[] parts = line.split("%");
					int time = Integer.parseInt(parts[1]);
					String name = parts[0];
					String ip = parts[3];
					Cache c = new Cache(time, ip, name);
					root.put(name, c);
				}
			}
		}
		br.close();

		serverCache.put("root", root);
	}
}
