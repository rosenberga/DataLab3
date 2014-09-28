package pack0;

import java.io.*;
import java.net.*;

public class UdpServer {
	public static void main(String[] args){
		DatagramSocket serverSocket = null;
		int port;
		
		InetAddress hostAddress;
		try {
			hostAddress = InetAddress.getLocalHost();
			System.out.println("The host address is: " + hostAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.out.println("GET THE FUCK OUTTA HERE!");
		}
		
		try {
			port = Integer.parseInt(args[0]);
			serverSocket = new DatagramSocket(9876);
		
			while(true){
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
					String sentence = new String(receivePacket.getData());
					System.out.println("RECEIVED: " + sentence);
					InetAddress IPAddress = receivePacket.getAddress();
					String newMessage = sentence +"\n";
					sendData = newMessage.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("This is fucking hopeless. Please kill yourself.");
				}
			}
		} catch (SocketException e) {
			System.out.println("Could not do shit, that makes me angry. DIE!");
			e.printStackTrace();
		} finally{
			if(serverSocket != null) {
				serverSocket.close();
			}
		}
	}
}
