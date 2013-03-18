import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.String;

class chatthread extends Thread {
	
	Socket socket = null;						//socket for connection
	MyConnection conn = null;					//connection communicator
	int tag = -1;								//1 or 2, player tag
	chatthread[] Players = null;
	boolean ready;
	
	public chatthread (Socket socket, int tag, chatthread[] Players) {
		this.socket = socket;
		this.tag = tag;
		this.Players = Players;
		this.conn = new MyConnection (socket);
		ready = false;
	}
	
	public void run() {
		
		String msg;
		
		//Give players their tag number
		Players[tag].conn.sendMessage("TAG: " + tag);
		
		//Players wait for START Signal
		do {
			msg = conn.getMessage();
			if (msg.startsWith("READY: ")) {
				int id = msg.charAt(7) - 48;
				if (id == tag) {
					ready = true;
				}
			}
		} while (!ready || !Players[1-tag].ready);
		
		System.out.println("Let the games begin! ");
		Players[0].conn.sendMessage("START");
		Players[1].conn.sendMessage("START");
		
		while (true) {
			
			msg = conn.getMessage();
			//System.out.println("Message: " + msg);
			Players[0].conn.sendMessage(msg);
			Players[1].conn.sendMessage(msg);
		}
	}
}

public class MyServer {
	
	ServerSocket ssocket = null;
	Socket socket = null;
	chatthread[] Player = new chatthread[2];		//accessible to all
	Timer timer;
	TimerTask ttask;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new MyServer();
	}
	
	public void updateAll() {
		System.out.println("DITO NA TAYO");
		Player[0].conn.sendMessage("CHIPSCREEN 0");
		Player[1].conn.sendMessage("CHIPSCREEN 0");
	}
	
	public MyServer() {
		
		
		try {
			ssocket = new ServerSocket(8888);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println("Waiting for player 1!");
			socket = ssocket.accept();
			Player[0] = new chatthread(socket, 0, Player);
			
			System.out.println("Waiting for player 2!");
			socket = ssocket.accept();
			Player[1] = new chatthread(socket, 1, Player);
			
			timer = new Timer();
			ttask = new TimerTask() {
				public void run() {
					//send notification to all (about chipSelectionScreen part)
					updateAll();
				}
			};
			timer.schedule(ttask, 10000, 10000);
			
			Player[0].start();
			Player[1].start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
