package ee.ajapaik.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=FilteredInputStreamReader">Kaido Kalda</a>
 */
public abstract class FilteredInputStreamReader extends InputStreamReader {

	protected MonitorableBufferedInputStream bis;

	public FilteredInputStreamReader(MonitorableBufferedInputStream bis, String cs) throws UnsupportedEncodingException {
		super(bis, cs);
		this.bis = bis;
	}

	@Override
	public int read(char[] ac) throws IOException {
		int read = super.read(ac);
		iterate(ac);
		return read;
	}

	@Override
	public int read(char[] ac, int i, int j) throws IOException {
		int read = super.read(ac, i, j);
		iterate(ac);
		return read;
	}
	
	@Override
	public int read(CharBuffer charbuffer) throws IOException {
		throw new RuntimeException("Not implemented!");
	}
	
	@Override
	public int read() throws IOException {
		return filter((char) super.read());
	}
	
	private void iterate(char[] ac) {
		for (int i = 0; i < ac.length; i++) {
			ac[i] = filter(ac[i]);
		}
	}
	
	public abstract char filter(char c);
}