package m;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;


public class Client {
		
	public Client(InetAddress ip,int port) throws Exception {
		
		final Socket sock = new Socket(ip,port);		
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()),true);
		final BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		
		Server.setSo(Base64.getDecoder().decode(in.readLine().getBytes()));
		Server.MAKE_KEY(false);
		if(Server.DECODE(in.readLine()).equals(Server.getPass())) {
			System.out.println("OK!");
			out.println(Server.ENCODE("OK!"));	
			String msg = "";
			final Ispisivac ispis = new Ispisivac(in);
			final Thread thrd = new Thread(ispis); thrd.start();
			while(true) {
				msg=Server.getKeyb().readLine();
				if(thrd.isAlive())
					out.println(Server.ENCODE(msg));
				else{
					System.out.println("Connection lost!");
					Server.EXIT(); //break
				}
				if(msg.equals(":exit")||msg.equals(":quit")) Server.EXIT();
			}
			//ispis.exit();
		}
		else {
			System.out.println("WRONG!");
			System.exit(0);
		}
		sock.close();
    }
	
	/*
	Cipher sifr_out = Cipher.getInstance("AES/CFB8/NoPadding");
	sifr_out.init(Cipher.ENCRYPT_MODE,M.getKey());
	IvParameterSpec iv = new IvParameterSpec(sifr_out.getIV());
	Cipher sifr_in = Cipher.getInstance("AES/CFB8/NoPadding");
	sifr_out.init(Cipher.DECRYPT_MODE,M.getKey(),iv);
	
	PrintWriter out = 
			new PrintWriter(new OutputStreamWriter(new CipherOutputStream(Base64.getEncoder().wrap(sock.getOutputStream()),sifr_out)),true);
	BufferedReader in = 
			new BufferedReader(new InputStreamReader(new CipherInputStream(Base64.getDecoder().wrap(sock.getInputStream()),sifr_in)));
	
	sock.getOutputStream().write(Base64.getEncoder().encode(M.merge(iv.getIV(),M.getSo())));
	out.println(pass);
	String msg = in.readLine();
	System.out.println(msg);
	if(!msg.equals("OK!")) {
		sock.close(); in.close(); out.close();
		return;
	}
	*/

}
