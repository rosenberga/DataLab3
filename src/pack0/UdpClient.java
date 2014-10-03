package pack0;

import java.io.*;
import java.net.*;

public class UdpClient {
	public static void main(String[] args) {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			System.out.print("Enter port ");
			String portS = inFromUser.readLine();
			while (true) {
				DatagramSocket clientSocket = new DatagramSocket();
				clientSocket.setSoTimeout(9000);
				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				System.out.print("Enter a website: ");
				String data = inFromUser.readLine();
				if (data.equalsIgnoreCase("quit")) {
					clientSocket.close();
					break;
				} else {
					data = data + "\n";
				}
				sendData = data.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, Integer.parseInt(portS));
				clientSocket.send(sendPacket);
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				try {
					clientSocket.receive(receivePacket);
					String message = new String(receivePacket.getData());
					System.out.println("FROM SERVER:" + message);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					clientSocket.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}