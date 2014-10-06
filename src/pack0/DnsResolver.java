package pack0;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class DnsResolver {
	private static final String rootServerFile = "RootFile.txt";
	private Map<String, Map<String, Cache>> serverCache;
	private DatagramSocket serverSocket;
	private DatagramSocket otherSocket;
	private Cache serverToAsk;
	private byte[] serverData;
	private String askedSite;
	private String finalIp;
	private Cache answer;
	private printCache pc;

	private int sendPort = 53;
	private int recPort;

	public static void main(String[] args) {
		try {
			new DnsResolver(args[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DnsResolver(String port) throws IOException {
		recPort = Integer.parseInt(port);
		// set up basic cache
		serverCache = new HashMap<String, Map<String, Cache>>();
		readRootFile();

		// set up server socket and receive packet
		serverSocket = new DatagramSocket(recPort);
		try {
			while (true) {
				byte[] receiveData = new byte[512];
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
				byte[] toSend = null;
				// check to see if we have cached the value before
				if (!inCache(receivePacket)) {
					finalIp = askServer(receivePacket, serverToAsk);
					toSend = answer.getData();
				} else {
					try {
						toSend = getId(receivePacket, serverToAsk.getData());
					} catch (DecoderException e) {
						e.printStackTrace();
					}
				}

				// send to user
				DatagramPacket sendPacket = new DatagramPacket(toSend,
						toSend.length, receivePacket.getAddress(),
						receivePacket.getPort());
				serverSocket.send(sendPacket);
			}
		} catch (Exception e) {
			e.printStackTrace();
			serverSocket.close();
			return;
		}
	}

	private String askServer(DatagramPacket receivePacket, Cache server)
			throws UnknownHostException {
		// if you have to ask more than one server, then
		// call their function recursively
		byte[] sendData = new byte[512];
		byte[] receiveData = new byte[512];

		sendData = receivePacket.getData();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, InetAddress.getByName(server.getIpAddress()),
				sendPort);
		try {
			serverSocket.send(sendPacket);
			// TODO
			// print out basic send information
		} catch (IOException e) {
			System.out.println("Couldn't Send");
			e.printStackTrace();
		}

		DatagramPacket fromServerToAsk = new DatagramPacket(receiveData,
				receiveData.length);
		try {
			serverSocket.receive(fromServerToAsk);
			// TODO
			// print out basic receive information
		} catch (IOException e) {
			System.out.println("Couldn't Receive");
			e.printStackTrace();
		}

		boolean answer = decodeMessage(fromServerToAsk);

		if (answer) {
			return serverToAsk.getIpAddress();
		} else {
			return askServer(receivePacket, serverToAsk);
		}
	}

	private byte[] getId(DatagramPacket receivePacket, byte[] data) throws DecoderException {
		char[] d = Hex.encodeHex(data);
		char[] c = Hex.encodeHex(receivePacket.getData());
		int index = 0;
		for(index = 0; index < 4; index++) {
			d[index] = c[index];
		}
		return Hex.decodeHex(d);
	}

	private boolean decodeMessage(DatagramPacket data) {
		// TODO Auto-generated method stub
		char[] c = Hex.encodeHex(data.getData());
		int index = 0;
		/*
		 * for(int i = 0; i < c.length; i++) { System.out.print(c[i] + "");
		 * if((i+1)%2==0) { System.out.print(" "); } if((i+1)%36 == 0) {
		 * System.out.println(""); } }
		 */
		// read through flags
		String transId = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];
		String flagsId = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];
		String questRRS = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];
		String answerRRs = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];
		String authRRS = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];

		String additRRS = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];
		// System.out.println("\n"+transId + " " + flagsId);
		// System.out.println(questRRS + " " + answerRRs + " " + authRRS + " " +
		// additRRS);
		// read through questions
		do {
			String hexS = c[index++] + "" + c[index++];
			Long next = Long.parseLong(hexS, 16);
			if (next == 0) {
				break;
			}
			for (int i = 0; i < next; i++) {
				index++;
				index++;
			}
		} while (true);

		// System.out.println("After Question:" + index+"");

		String qType = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];
		String qClass = c[index++] + "" + c[index++] + "" + c[index++] + ""
				+ c[index++];
		int beforeAuth = index;
		// System.out.println("\n"+qType+ " "+qClass);
		// read through answers
		if (Long.parseLong(answerRRs, 16) > 0) {
			String name = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++];
			String aType = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++];
			String aClass = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++];
			String aTime = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++] + "" + c[index++];
			int timeToLive = (int) Long.parseLong(aTime, 16);
			String aLen = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++];
			int len = (int) Long.parseLong(aLen, 16);
			String aIP = "";
			for (int i = 0; i < len; i++) {
				String hex = c[index++] + "" + c[index++];
				int value = (int) Long.parseLong(hex, 16);
				aIP += value;
				if (i + 1 != len) {
					aIP += '.' + "";
				}
			}

			int pIndex = (int) Long.parseLong(name.substring(2), 16) * 2;
			String aName = "";
			do {
				String hexS = c[pIndex++] + "" + c[pIndex++];

				if (hexS.equals("c0")) {
					String n = c[pIndex++] + "" + c[pIndex++];
					pIndex = (int) Long.parseLong(n, 16) * 2;
				} else {
					Long next = Long.parseLong(hexS, 16);
					if (next == 0) {
						break;
					}
					for (int i = 0; i < next; i++) {
						String h = c[pIndex++] + "" + c[pIndex++];

						aName += hexToASCII(h);
					}
					aName += ".";
				}
			} while (true);
			aName = aName.substring(0, aName.length() - 1);

			Cache c1 = new Cache(timeToLive, aIP, aName, data.getData());
			answer = c1;

			String cName = askedSite;

			if (serverCache.containsKey(cName.toUpperCase())) {
				Map<String, Cache> map = serverCache.get(cName.toUpperCase());
				map.put(aName.toUpperCase(), c1);
				serverCache.put(cName.toUpperCase(), map);
			} else {
				HashMap<String, Cache> map = new HashMap<String, Cache>();
				map.put(aName.toUpperCase(), c1);
				serverCache.put(cName.toUpperCase(), map);
			}
			
			pc = new printCache(serverCache);
			pc.start();

			return true;
		} else {
			// read through authoritative section

			// get name so we can put it into the map
			String name = "";
			Long auth = ((Long.parseLong(authRRS, 16) * 2) - 1);
			String point = "";
			int count = 0;
			while (true) {
				String bits = c[index++] + "" + c[index++];
				if (bits.equals("c0")) {
					count++;
				}
				if (count == 1 && bits.equals("c0")) {
					point = c[index++] + "" + c[index++];
					int pointIndex = 2 * (int) Long.parseLong(point, 16);
					do {
						String hexS = c[pointIndex++] + "" + c[pointIndex++];
						Long next = Long.parseLong(hexS, 16);
						if (next == 0) {
							break;
						}
						for (int i = 0; i < next; i++) {
							String h = c[pointIndex++] + "" + c[pointIndex++];

							name += hexToASCII(h);
						}
						name += ".";
					} while (true);
					name = name.substring(0, name.length() - 1);
				}
				if (bits.equals("c0")) {
					if ((c[index++] + "" + c[index++]).equals("2c")) {
						break;
					}
				}
			}
			index -= 4;

			String nP = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++];
			String aType = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++];
			String aClass = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++];
			String ttl = c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++] + "" + c[index++] + "" + c[index++] + ""
					+ c[index++] + "" + c[index++];

			int timeToLive = (int) Long.parseLong(ttl, 16);

			String dataLength = c[index++] + "" + c[index++] + "" + c[index++]
					+ "" + c[index++];
			int len = (int) Long.parseLong(dataLength, 16);
			String aIP = "";
			for (int i = 0; i < len; i++) {
				String hex = c[index++] + "" + c[index++];
				int value = (int) Long.parseLong(hex, 16);
				aIP += value;
				if (i + 1 != len) {
					aIP += '.' + "";
				}
			}

			int pIndex = (int) Long.parseLong(nP.substring(2), 16) * 2;
			String aName = "";
			do {
				String hexS = c[pIndex++] + "" + c[pIndex++];

				if (hexS.equals("c0")) {
					String n = c[pIndex++] + "" + c[pIndex++];
					pIndex = (int) Long.parseLong(n, 16) * 2;
				} else {
					Long next = Long.parseLong(hexS, 16);
					if (next == 0) {
						break;
					}
					for (int i = 0; i < next; i++) {
						String h = c[pIndex++] + "" + c[pIndex++];

						aName += hexToASCII(h);
					}
					aName += ".";
				}
			} while (true);
			aName = aName.substring(0, aName.length() - 1);

			Cache c1 = new Cache(timeToLive, aIP, aName, data.getData());
			//System.out.println("name " + name.toUpperCase());
			//System.out.println("aname " + aName.toUpperCase());
			if (serverCache.containsKey(name.toUpperCase())) {
				Map<String, Cache> map = serverCache.get(name.toUpperCase());
				map.put(aName.toUpperCase(), c1);
				serverCache.put(name.toUpperCase(), map);
			} else {
				HashMap<String, Cache> map = new HashMap<String, Cache>();
				map.put(aName.toUpperCase(), c1);
				serverCache.put(name.toUpperCase(), map);
			}
			pc = new printCache(serverCache);
			 pc.start();
			serverToAsk = c1;

			return false;
		}
	}

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
			if (next == 0) {
				break;
			}
			for (int i = 0; i < next; i++) {
				String h = hexC[index++] + "" + hexC[index++];

				site += hexToASCII(h);
			}
			site += ".";
		} while (true);
		site = site.substring(0, site.length() - 1);
		askedSite = site;

		String[] siteParts = site.split("\\.");
		int times = 0;
		while (true) {

			// build the check string
			String check = "";
			for (int i = times; i >= 0 && siteParts.length - 1 - i >= 0; i--) {
				check += siteParts[siteParts.length - 1 - i] + ".";
			}
			check = check.substring(0, check.length() - 1);
			if (serverCache.containsKey(check.toUpperCase())) {
				times++;

				Map<String, Cache> roots = serverCache.get(check.toUpperCase());
				String[] keys = roots.keySet().toArray(new String[0]);
				serverToAsk = roots.get(keys[0]);

				if (times == siteParts.length) {
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
		byte[] data = receivePacket.getData();
		data[2] = 0;
		return data;
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

		public printCache(Map<String, Map<String, Cache>> cache) {
			currentCache = cache;
			fileName = "CurrentCache.txt";
		}

		public void run() {
			
			String[] keys = currentCache.keySet().toArray(new String[0]);
			/*for (int i = 0; i < keys.length; i++) {
				Map<String, Cache> map = currentCache.get(keys[i]);
				String[] k = map.keySet().toArray(new String[0]);
				for (int j = 0; j < k.length; j++) {
					Cache c = map.get(k[j]);
					System.out.println(c.getDetails());
				}
			}
			
			for(int i = 0; i < keys.length; i++) {
				System.out.println(keys[i]);
			}
			*/
		}
	}
}