package ee.ajapaik.util;

import java.security.MessageDigest;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=Digester">Kaido Kalda</a>
 */
public class Digester {
	
	public interface DataCallback {
		int getByte();
	}
	
	public static String digestToString(String s) {
		return new String( encode(  digestToBytes(s) ) );
	}
	
	public static byte[] digestToBytes(String s) {
		try {
			MessageDigest d = MessageDigest.getInstance("md5");
			return d.digest( s.getBytes("UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String digestToString(byte[] b) {
		try {
			MessageDigest d = MessageDigest.getInstance("md5");
			return new String( encode( d.digest(b) ));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String digestToString(DataCallback callback) {
		try {
			MessageDigest d = MessageDigest.getInstance("md5");
			
			int b; 
			while((b = callback.getByte()) != -1) {
				d.update((byte)b);
			}
			
			return new String( encode( d.digest() ));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static char[] encode(byte bytes[]) {
		int nBytes = bytes.length;
		char result[] = new char[2 * nBytes];
		int j = 0;
		for (int i = 0; i < nBytes; i++) {
			result[j++] = HEX[(240 & bytes[i]) >>> 4];
			result[j++] = HEX[15 & bytes[i]];
		}

		return result;
	}
	
	private static final char HEX[] = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}
