import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

public class Substitute implements SymCipher {

	private byte[] key;

	public Substitute() {
		key = new byte[256];
		Random rand = new Random();
		ArrayList<Byte> temp = new ArrayList<Byte>();
		for (int i = 0; i < 256; i++) {
			temp.add(new Byte((byte) i));
		}
		//Randomizes bits
		Collections.shuffle(temp);
		for (int i = 0; i < key.length; i++) {
			key[i] = temp.get(i);
		}
	}

	public Substitute(byte[] bytes) {
		key = new byte[256];

		//copies the parameter into key
		for (int i = 0; i < key.length; i++) {
			key[i] = bytes[i];
		}
	} 
	// Return an array of bytes that represent the key for the cipher
	public byte [] getKey() {
		return key;
	}
	
	// Encode the string using the key and return the result as an array of
	// bytes.  Note that you will need to convert the String to an array of bytes
	// prior to encrypting it.  Also note that String S could have an arbitrary
	// length, so your cipher may have to "wrap" when encrypting.
	public byte [] encode(String S) {
		byte[] str = S.getBytes();
		byte[] encoded = new byte[str.length];
		for (int i = 0; i < str.length; i++) {
			//Turns negative bytes to ints
			int k = str[i] & 0xFF;
			encoded[i] = key[k];
		}
	  return encoded;
	}
	
	// Decrypt the array of bytes and generate and return the corresponding String.
	public String decode(byte [] bytes){
		byte[] decoded = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			int chr = 0;
			for (int j = 0; j < key.length; j++) {
				if (key[j] == bytes[i]) {
					chr = j;
					break;
				}
			}
			decoded[i] = (byte) chr;
		}
		return new String(decoded);
	}
}