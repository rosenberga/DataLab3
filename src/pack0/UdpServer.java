package pack0;

import java.io.*;
import java.net.*;

public class UdpServer {
	public static void main(String[] args){
		System.out.println("START!");
		DatagramSocket serverSocket;
		int port;
		
		try {
			port = Integer.parseInt(args[0]);
			serverSocket = new DatagramSocket(port);
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
		
			while(true){
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
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