package m;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.TerminalBuilder;

import com.dosse.upnp.UPnP;

public class Server {
	
	private static byte[] so = new byte[32];
	private static boolean exit;
	private static boolean upnp;
	private static SecretKeySpec key;
	private static String pass;
	private static InetAddress ip = InetAddress.getLoopbackAddress();
	private static final LineReader keyb = initReader();
		
	public static void MAKE_KEY(boolean posoli) throws Exception {
		if(posoli) SecureRandom.getInstance("SHA1PRNG").nextBytes(so);
		SecretKeyFactory fct = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec ks = new PBEKeySpec(pass.toCharArray(),so,65537,256);
		key = new SecretKeySpec(fct.generateSecret(ks).getEncoded(),"AES");
	}
	
	public static String ENCODE(String in) throws Exception {
		Cipher sifr = Cipher.getInstance("AES/CFB8/NoPadding");
		sifr.init(Cipher.ENCRYPT_MODE,key); //AES/CBC/PKCS5Padding
		return Base64.getEncoder().encodeToString(merge(sifr.getIV(),sifr.doFinal(in.getBytes())));
	}
	
	public static String DECODE(String out) {
		try {
			byte[] raw = Base64.getDecoder().decode(out);
			byte[] iv = Arrays.copyOf(raw, 16);
			Cipher sifr = Cipher.getInstance("AES/CFB8/NoPadding");
			sifr.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(iv));
			return new String(sifr.doFinal(Arrays.copyOfRange(raw, 16, raw.length)));
		}
		catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		if(keyb==null) System.exit(0);
		int mode=0;
		int mode2=0;
		int port=0;
		while(true) {
			try {
				mode = Integer.parseInt(keyb.readLine("[1].Server [2].Client ? "));
			} catch (NumberFormatException e) {
				continue;
			}
			if(mode==1||mode==2) break;
		}
		if(mode==2) {
			while(true) {
				try {
					String tmp[]=keyb.readLine("[IP:port] ? ").split(":");
					port=Integer.parseUnsignedInt(tmp[1]);
					if(port>=65535) continue;
					ip=InetAddress.getByName(tmp[0]);
				}
				catch (ArrayIndexOutOfBoundsException | NumberFormatException | UnknownHostException e) {
					if(e instanceof UnknownHostException) 
						System.out.println("Unknown Host!");
					continue;
				}
				break;
			}
			
		}
		else {
			while(true) {
				try {
					mode2 = Integer.parseInt(keyb.readLine("[1].Internet [2].LAN ? "));
				} catch (NumberFormatException e) {
					continue;
				}
				if(mode2==1||mode2==2) break;
			}
			port = (int)(Math.random()*64510+1025);
			if(mode2==1) {
				if(upnp=UPnP.isUPnPAvailable()) {
					ip = InetAddress.getByName(UPnP.getExternalIP());
					UPnP.openPortTCP(port);
				} else {
					System.out.println("UPnP not available!\nCannot open port!");
					getExternalIP();
				}
			}
			else ip = InetAddress.getLocalHost();
			
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ip.getHostAddress()+':'+port),null);
			System.out.println('['+ip.getHostAddress()+':'+port+']');
		}
		pass = keyb.readLine("[Password] ? ",'*');
		MAKE_KEY(true);
		if(mode==1) {
			final ServerSocket ss = new ServerSocket(port);
			System.out.println("[Listening...]");
			while(!exit) {
					new Thread(new ServerThread(ss.accept())).start();
			}
			ss.close();
		}else try{
			new Client(ip,port);
		} catch (Exception e) {
			if(e instanceof UserInterruptException)
				System.out.println("Interrupted!");
		    else if (e instanceof ConnectException)
		    	System.out.println("Connection refused!");
		    else e.printStackTrace();
		}
		if(mode2==1&&upnp) {
			System.out.println("[Closing port...]");
			UPnP.closePortTCP(port);
		}
		System.exit(0);
	}
	
	public static String getPass() {
		return pass;
	}
	
	public static void EXIT() {
		exit=true;
		System.exit(0);
	}

	public static void setSo(byte[] so) {
		Server.so = so;
	}

	public static byte[] getSo() {
		return so;
	}
	public static SecretKeySpec getKey() {
		return key;
	}
	
	public static LineReader getKeyb() {
		return keyb;
	}

	public static byte[] merge(byte[] a,byte[] b) {
		byte[] rez = Arrays.copyOf(a, a.length+b.length);
		System.arraycopy(b, 0, rez, a.length, b.length);
		return rez;
		
	}
	
	private static LineReader initReader() {
		Logger lgr = Logger.getLogger("org.jline");
		ConsoleHandler hnd = new ConsoleHandler();
		hnd.setLevel(Level.FINE);
		lgr.addHandler(hnd);
		lgr.setLevel(Level.FINE);
		lgr.setUseParentHandlers(false);
		try {
			return LineReaderBuilder.builder().terminal(TerminalBuilder.builder().system(true).jna(false).build()).build();
		} catch (IOException e) {
			System.out.println("Init error!"+e.toString());
			return null;
		}
	}
	
	private static void getExternalIP() {
		try (java.util.Scanner s = new java.util.Scanner(new java.net.URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A")) {
			ip = InetAddress.getByName(s.next());
		} catch (java.io.IOException e) {
			System.out.println("IP detection error!\n"+e.toString());
		}

	}
	
}
