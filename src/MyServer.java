import java.io.*;
import java.net.*;
import java.lang.String;

public class MyServer {
	
	private static class chatthread extends Thread {
		
		Socket socket = null;						//socket for connection
		MyConnection conn = null;					//connection communicator
		int tag = -1;								//1 or 2, player tag
		chatthread[] Players = null;
		
		public chatthread (Socket socket, int tag, chatthread[] Players) {
			this.socket = socket;
			this.tag = tag;
			this.Players = Players;
		}
		
		public void run() {
			conn = new MyConnection (socket);
			
			//Preliminary stuff here (to set-up client class)
<<<<<<< HEAD
			Players[tag].conn.sendMessage("TAG: " + tag);
=======
			//Players[tag].conn.sendMessage("!!!TAG: " + tag);
>>>>>>> 4b9330a2b30f4c851101079f51f0a96d1f79e074
			
			while (true) {
				
				String happy = conn.getMessage();
				System.out.println("What is happiness? " + happy);
				Players[0].conn.sendMessage(happy);
				Players[1].conn.sendMessage(happy);
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ServerSocket ssocket = null;
		Socket socket = null;
		chatthread[] Player = new chatthread[2];		//accessible to all
		
		try {
			ssocket = new ServerSocket(8888);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
<<<<<<< HEAD
=======
		
>>>>>>> 4b9330a2b30f4c851101079f51f0a96d1f79e074
			System.out.println("Waiting for player 1!");
			socket = ssocket.accept();
			Player[0] = new chatthread(socket, 0, Player);
			
			System.out.println("Waiting for player 2!");
			socket = ssocket.accept();
			Player[1] = new chatthread(socket, 1, Player);
			
			Player[0].start();
			Player[1].start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
