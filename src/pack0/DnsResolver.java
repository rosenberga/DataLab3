package pack0;

import java.io.*;
import java.net.*;
import java.util.*;

public class DnsResolver {
	private int port;
	private static final String rootServerFile = "RootFile.txt";
	private Map<String, Map<String,Cache>> serverCache;
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
		serverCache = new HashMap<String, Map<String,Cache>>();
		readRootFile();
		// set up server socket and receive packet
		serverSocket = new DatagramSocket(this.port);
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		// wait to receive a packet
		serverSocket.receive(receivePacket);
		
	}
	
	private void readRootFile() throws IOException {
		String line;
		Map<String, Cache> root = new HashMap<String, Cache>();
		BufferedReader br = new BufferedReader(new FileReader(rootServerFile));

		while ((line = br.readLine()) != null) {
			if(line.substring(0,1).equalsIgnoreCase(".")) {
				line = br.readLine();
				if(line != null) {
					line = line.replaceAll("\\s+", "%");
					String[] parts = line.split("%");
					int time = Integer.parseInt(parts[1]);
					String name = parts[0];
					String ip = parts[2];
					Cache c = new Cache(time,ip,name);
					root.put(name, c);
				}
			}
		}
		br.close();
		
		serverCache.put("root", root);
	}
}
