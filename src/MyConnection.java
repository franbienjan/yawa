import java.io.*;
import java.net.*;

public class MyConnection {
	
	Socket socket;
	
	public MyConnection (Socket s) {
		socket = s;
	}
	
	public boolean sendMessage (String msg) {
		try {
			OutputStream a = socket.getOutputStream();
			OutputStreamWriter b = new OutputStreamWriter(a);
			PrintWriter c = new PrintWriter(b);
			c.println(msg);
			c.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public String getMessage () {
		String msg = "";
		try {
			InputStream a = socket.getInputStream();
			InputStreamReader b = new InputStreamReader(a);
			BufferedReader c = new BufferedReader(b);
			msg = c.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return msg;
	}	
	
}