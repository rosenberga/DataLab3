package pack0;

import java.io.*;
import java.net.*;

public class UdpClient {
	public static void main(String[] args) {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		String ip;
		try {
			System.out.print("Enter ip address: ");
			ip = inFromUser.readLine();
			System.out.print("Enter port ");
			String portS = inFromUser.readLine();
			while (true) {
				DatagramSocket clientSocket = new DatagramSocket();
				clientSocket.setSoTimeout(9000);
				InetAddress IPAddress = InetAddress.getByName(ip);
				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				System.out.print("Enter a message ");
				String data = inFromUser.readLine();
				if(data.equalsIgnoreCase("quit")) {
					clientSocket.close();
					break;
				}
				sendData = data.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, Integer.parseInt(portS));
				clientSocket.send(sendPacket);
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				clientSocket.receive(receivePacket);
				String message = new String(receivePacket.getData());
				System.out.println("FROM SERVER:" + message);
				clientSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}