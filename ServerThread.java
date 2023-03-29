package m;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

import org.jline.reader.UserInterruptException;


public class ServerThread implements Runnable{
	
	private final Socket sock;
	
	public ServerThread(Socket sock) {
		this.sock = sock;
	}

	@Override
	public void run() {
		try {
			final PrintWriter	out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()),true);
			final BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
			out.println(Base64.getEncoder().encodeToString(Server.getSo()));
			out.println(Server.ENCODE(Server.getPass()));	
			String msg = Server.DECODE(in.readLine());
			if(msg.equals("OK!")) {
				System.out.println(msg);			
				final Ispisivac ispis = new Ispisivac(in);
				final Thread thrd = new Thread(ispis); thrd.start();
				while(true) {
					msg=Server.getKeyb().readLine();
					if(thrd.isAlive()) {
						out.println(Server.ENCODE(msg));
					}
					else{
						System.out.println("Connection lost!");
						break;
					}
					if(msg.equals(":exit")||msg.equals(":quit")) {
						Server.EXIT();
					}
				}
			}
			else System.out.println("WRONG KEY!");
		} catch (Exception e){
			if(e instanceof UserInterruptException) 
				System.out.println("Interrupted!");
			else System.out.println("Error!\n"+e.toString());
		}
		try{ sock.close(); }
		catch(Exception e2) { e2.printStackTrace(); } 
		System.out.println("[Listening...]");
	}
	
	/*
	byte[] raw = new byte[48];
	sock.getInputStream().read(raw);
	raw = Base64.getDecoder().decode(raw);
	IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(raw, 16));
	M.setSo(Arrays.copyOfRange(raw, 16, 48));
	M.MAKE_KEY(pass);
	
	Cipher sifr_out = Cipher.getInstance("AES/CFB8/NoPadding");
	sifr_out.init(Cipher.ENCRYPT_MODE,M.getKey(),iv);
	Cipher sifr_in = Cipher.getInstance("AES/CFB8/NoPadding");
	sifr_out.init(Cipher.DECRYPT_MODE,M.getKey(),iv);
	
				
	out = new PrintWriter(new OutputStreamWriter(new CipherOutputStream(Base64.getEncoder().wrap(sock.getOutputStream()),sifr_out)),true);
	in = new BufferedReader(new InputStreamReader(new CipherInputStream(Base64.getDecoder().wrap(sock.getInputStream()),sifr_in)));
	
	if(in.readLine().equals(pass)) {
		System.out.println("OK!");
		out.println("OK!");
		break;
	}*/
}
	
