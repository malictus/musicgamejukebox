package malictus.musicgamejukebox.dtb;

/**
 * DecryptionTable
 * This class represents a decryption table for a DTB file. This file is based heavily on the KrpTable
 * class in the Karaoke Revolution customs program KRMaker.
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class DecryptionTable {

	private int[] table;
	private int in;
	private int out;

	public DecryptionTable(int key) {
		table = new int[0x100];
		in = 0x67;
		out = 0;
		int eax = key;
		int edx, ebx;

		for (int i = 0; i < table.length; i++) {
			eax = eax * 0x41c64e6d;
			eax += 0x3039;
			edx = eax;
			eax = eax * 0x41c64e6d;
			eax += 0x3039;
			ebx = eax;
			edx = (edx >> 0x10) & 0x0000ffff;
			ebx = ebx & 0x7fff0000;
			ebx = ebx | edx;
			table[i] = ebx;
		}
	}

	private byte decrypt(byte b) {
		int val = table[out];
		int val2 = table[in];
		val2 = val2 ^ val;
		table[out] = val2;
		in = (in + 1) % 0xf9;
		out = (out + 1) % 0xf9;
		b = (byte) (b^val2);
		return (byte) b;
	}

	private byte encrypt(byte b) {
		int val = table[in];
		int val2 = table[out];
		val2 = val2 ^ val;
		table[out] = val2;
		out = (out + 1) % 0xf9;
		in = (in + 1) % 0xf9;
		b = (byte) (b^val2);
		return (byte) b;
	}

	public void decrypt(byte[] data) {
		int counter = 0;
		while (counter < data.length) {
			data[counter] = decrypt(data[counter]);
			counter = counter + 1;
		}
		return;
	}

	public void encrypt(byte[] data) {
		int counter = 0;
		while (counter < data.length) {
			data[counter] = encrypt(data[counter]);
			counter = counter + 1;
		}
		return;
	}
}