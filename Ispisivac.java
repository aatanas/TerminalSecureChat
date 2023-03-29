package m;

import java.io.BufferedReader;
import java.io.IOException;

public class Ispisivac implements Runnable {
	
	private final BufferedReader in;
	private volatile boolean exit;
	
	public Ispisivac(BufferedReader in) {
		this.in = in;
	}

	@Override
	public void run() {
		String msg="";
		while(!exit) {
			try {
				msg=Server.DECODE(in.readLine());
				if(msg.equals(":exit")||msg.equals(":quit")) {
					Server.getKeyb().printAbove("Disconnect!\nPress any key to continue...");
					msg=null;
				}
				if(msg==null) break;
				Server.getKeyb().printAbove(msg);
			} catch (NullPointerException | IOException e) {
				if(e instanceof IOException)
					e.printStackTrace();
				break;
			}
		}
		exit=true;
	}
	
	public boolean isStopped() {
		return exit;
	}
	
	public void exit() {
		exit=true;
	}

}
