package malictus.musicgamejukebox.qb;

public class QBHash {

	long[] crc_tab = new long[256];

	public QBHash() {
		initCRC32();
	}

	public static void main(String[] args) {
		QBHash hash = new QBHash();
		System.out.println(Long.toHexString(hash.hashit("one")));
	}

	private void initCRC32() {
		long crc, poly;
		int i, j;
		poly = 0xEDB88320L;
		for (i = 0; i < 256; i++) {
			crc = i;
			for (j = 8; j > 0; j--) {
				if ((crc & 1) == 1) {
					crc = (crc >> 1) ^ poly;
				} else {
					crc >>= 1;
				}
			}
			crc_tab[i] = crc;
		}
	}

	public long hashit(String val) {
		long crc = 0xFFFFFFFF;
		for (long i = 0; i < val.length(); i++) {
			crc = ((crc >> 8) & 0x00FFFFFF) ^ crc_tab[(int)(crc ^ (long)val.charAt((int)i)) & 0xFF];
		}
		return crc;
	}

}
