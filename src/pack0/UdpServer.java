package pack0;

import java.io.*;
import java.net.*;

public class UdpServer {
	private int port;
	private DatagramSocket serverSocket;
	public static void main(String[] args){
		new UdpServer();
	}	
	public UdpServer() {
		InetAddress hostAddress;
		try {
			hostAddress = InetAddress.getLocalHost();
			System.out.println("The host address is: " + hostAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.out.println("Failed to get host address");
		}
		
		try {
			port = 9876;
			serverSocket = new DatagramSocket(port);
		
			while(true){
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
					String sentence = new String(receivePacket.getData());
					if(sentence.equalsIgnoreCase("quit")) {
						if(serverSocket != null) {
							serverSocket.close();
						}
						break;
					}
					System.out.println("RECEIVED: " + sentence);
					Dig d = new Dig(sentence, receivePacket.getAddress());
					d.run();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("IOException");
				}
			}
		} catch (SocketException e) {
			System.out.println("Socket Exception");
			e.printStackTrace();
		}
	}
	private class Dig extends Thread {
		private String request;
		private String command;
		private BufferedReader reader;
		private StringBuilder sb;
		private String result;
		private InetAddress ip;
		private byte[] sendData;
		
		public Dig(String r, InetAddress ipA) {
			request = r;
			char[] chars = request.toCharArray();
			command = "dig ";
			for(int i = 0; i < chars.length; i++){
				char c = chars[i];
				if(c != '\0') {
					command = command + c;
				}
			}
			
			sendData = new byte[1024];
			ip = ipA;
		}
		
		public void run() {
			try{
				Process p = Runtime.getRuntime().exec(command);
				p.waitFor();
				reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";
				sb = new StringBuilder();
				while((line = reader.readLine()) != null) {
					sb.append(line +"\n");
				}
				result = sb.toString();
				sendData = result.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
				serverSocket.send(sendPacket);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
