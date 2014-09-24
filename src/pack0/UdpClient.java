package pack0;

import java.io.*;
import java.net.*;

public class UdpClient {
	public static void main(String[] args) {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String ip;
		try {
			ip = inFromUser.readLine();
			String portS = inFromUser.readLine();
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(ip);
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			String data = inFromUser.readLine();
			sendData = data.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(portS));
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			String acheivementGet = new String(receivePacket.getData());
			System.out.println("FROM SERVER:" + acheivementGet);
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}