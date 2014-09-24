package pack0;

import java.io.*;
import java.net.*;

public class UdpServer {
	public static void main(String[] args){
		System.out.println("START!");
		DatagramSocket serverSocket;
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
			serverSocket = new DatagramSocket(port);
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
		
			while(true){
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
					String sentence = new String(receivePacket.getData());
					System.out.println("RECEIVED: " + sentence);
					InetAddress IPAddress = receivePacket.getAddress();
					sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
					System.out.println(new String(sendPacket.getData(),"UTF-8"));
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("THis is fucking hopeless. Please kill yourself.");
				}
			}
		} catch (SocketException e) {
			System.out.println("Could not do shit, that makes me angry. DIE!");
			e.printStackTrace();
		}
	}
}