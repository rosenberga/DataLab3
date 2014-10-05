package pack0;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;

public class DnsResolver {
	private int port;
	private static final String rootServerFile = "RootFile.txt";
	private Map<String, Map<String, Cache>> serverCache;
	private DatagramSocket serverSocket;
	private Cache serverToAsk;
	private byte[] serverData;
	private String askedSite;
	private String finalIp;
	private Cache answer;
	private printCache pc;

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

				// serverToAsk will be assigned to
				// the best server to ask the question
				serverToAsk = null;
				
				// check to see if we have cached the value before
				if (!inCache(receivePacket)) {
					finalIp = askServer(receivePacket, serverToAsk);
				}
				
				// send to user

				// TODO have a thread that print out basic info on cache
				// each time we update cache
			}
		} catch (Exception e) {
			// close the server socket
			serverSocket.close();
			return;
		}
	}

	private String askServer(DatagramPacket receivePacket, Cache server) throws UnknownHostException {
		// if you have to ask more than one server, then
		// call their function recursively
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
	
		System.out.println(serverToAsk.getIpAddress());
		sendData = receivePacket.getData();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(server.getIpAddress()),53);
		
		try {
			serverSocket.send(sendPacket);
			// TODO
			// print out basic send information
		} catch (IOException e) {
			System.out.println("Couldn't Send");
			e.printStackTrace();
		}

		DatagramPacket fromServerToAsk = new DatagramPacket(receiveData, receiveData.length);	
		try{
			serverSocket.receive(fromServerToAsk);
			// TODO
			// print out basic receive information
		} catch (IOException e) {
			System.out.println("Couldn't Receive");
			e.printStackTrace();
		}
		
		boolean answer = decodeMessage(fromServerToAsk);
		cacheMessage(fromServerToAsk);
		
		if(answer) {
			// we have our answer, send it back to user
		} else {
			// we do not have our answer, send packet to next server
		}	
		return null;
	}

	private void cacheMessage(DatagramPacket fromServerToAsk) {
		// TODO Auto-generated method stub
		
	}

	private boolean decodeMessage(DatagramPacket fromServerToAsk) {
		// TODO Auto-generated method stub
		return false;
	}

	/** Parse the IP Address returned from the DNS Response **/
	/*
	private String parseIPFromResponse(DatagramPacket receivePacket) throws UnknownHostException{
		byte[] data = receivePacket.getData();
		char[] hexC = Hex.encodeHex(data);
		
		//May need to set this up so the value isn't hard coded, may not be the same every time...I don't know. 
		int index = 536;
		String temp = "";
		
		//Get the relevant hex values to parse the IP from
		for(int i = 0; i < 8; i++){
			temp += hexC[index+i];
		}

		InetAddress result = InetAddress.getByAddress(DatatypeConverter.parseHexBinary(temp));
	
		return result.toString();
	}
	*/

	private boolean inCache(DatagramPacket receivePacket) {
		// declare variables needed
		byte[] data = receivePacket.getData();
		char[] hexC = Hex.encodeHex(data);
		
		int index = 24;
		String site = "";

		// loop through hexC getting values for the site
		// break when there are no more values to get
		do {
			String hexS = hexC[index++] + "" + hexC[index++];
			Long next = Long.parseLong(hexS, 16);
			if(next == 0) {
				break;
			}
			for (int i = 0; i < next; i++) {
				String h = hexC[index++] + "" + hexC[index++];
				
				site += hexToASCII(h);
			}
			site += ".";
		} while (true);	
		site = site.substring(0,site.length()-1);
		askedSite = site;
		System.out.println(askedSite);

		String[] siteParts = site.split("\\.");
		int times = 0;
		
		while(true) {
			
			// build the check string
			String check = "";
			for(int i = times; i >= 0 && siteParts.length-1-i>=0; i--) {
				check += siteParts[siteParts.length-1-i]+".";
			} 
			check.substring(0,check.length()-1);
			
			if(serverCache.containsKey(check.toUpperCase())) {
				times++;
				serverToAsk = serverCache.get(check).get(check);
				if(times == siteParts.length)  {
					finalIp = serverToAsk.getIpAddress();
					answer = serverToAsk;
					return true;
				}
			} else {
				// make sure that it is at least set to root
				if (serverToAsk == null) {
					Map<String, Cache> roots = serverCache.get("ROOT");
					String[] keys = roots.keySet().toArray(new String[0]);
					serverToAsk = roots.get(keys[0]);
				}
				break;
			}
		}
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
					root.put(name.toUpperCase(), c);
				}
			}
		}
		br.close();

		serverCache.put("ROOT", root);
		
		pc = new printCache(serverCache);
		pc.start();
	}

	private String hexToASCII(String hexValue) {
		StringBuilder output = new StringBuilder("");
		for (int i = 0; i < hexValue.length(); i += 2) {
			String str = hexValue.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}
		return output.toString();
	}
	
	private class printCache extends Thread {
		private Map<String, Map<String, Cache>> currentCache;
		private String fileName;
		public printCache(Map<String, Map<String,Cache>> cache) {
			currentCache = cache;
			fileName = "CurrentCache.txt";
		}
		public void run() {
			
		}
	}
}