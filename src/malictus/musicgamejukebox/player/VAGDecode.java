package malictus.musicgamejukebox.player;

/**
 * Decode a block of VAG-encoded audio data, and return the PCM version.
 */
public class VAGDecode {

	double vagstate1 = 0;
	double vagstate2 = 0;

	final static double[][] filter =  {
		{ 0.0, 0.0 },
		{ 60.0 / 64.0,  0.0 },
		{ 115.0 / 64.0, -52.0 / 64.0 },
		{ 98.0 / 64.0, -55.0 / 64.0 },
		{ 122.0 / 64.0, -60.0 / 64.0 }
	};

	public VAGDecode() {}

	short[] decodeVAGBlock(byte[] in) throws Exception {
		return decodeVAGBlock(in, null);
	}

	short[] decodeVAGBlock(byte[] in, Integer flagcheck) throws Exception {
		double[] s = new double[2];
		s[0] = vagstate1;
		s[1] = vagstate2;
		int	predictor = highnibble(in[0]);
		int	shift = lownibble(in[0]);
		int flag = in[1];
		if (flagcheck != null) {
			if (flag != flagcheck) {
				//probably eof
				throw new Exception();
			}
		}
		if (predictor > 4) {
			throw new Exception();
		}
		double[] samples = new double[28];
		short[] outshorts = new short[28];
		//we now have in and out ready to go
        for ( int i = 0; i < 28; i += 2 ) {
        	int numb = (i/2) + 2;
            int ss = ( in[numb] & 0xf ) << 12;
            if ( (ss & 0x8000) != 0 )
                ss |= 0xffff0000;
            samples[i] = (double) ( ss >> shift  );
            ss = ( in[numb] & 0xf0 ) << 8;
            if ( (ss & 0x8000) != 0 )
                ss |= 0xffff0000;
            samples[i+1] = (double) ( ss >> shift  );
        }
        for ( int i = 0; i < 28; i++ ) {
            samples[i] = samples[i] + s[0] * filter[predictor][0] + s[1] * filter[predictor][1];
            s[1] = s[0];
			s[0] = samples[i];
			outshorts[i] = (short) ( samples[i] + 0.5 );
        }
        vagstate1 = s[0];
        vagstate2 = s[1];
		return outshorts;
	}

	private int highnibble(int a) {
		return (a >> 4) & 15;
	}

	private int lownibble(int a) {
		return a & 15;
	}

}
