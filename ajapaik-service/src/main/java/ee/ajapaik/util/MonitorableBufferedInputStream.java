package ee.ajapaik.util;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=MonitorableBufferedInputStream">Kaido Kalda</a>
 */
public class MonitorableBufferedInputStream extends BufferedInputStream {

	public MonitorableBufferedInputStream(InputStream inputstream, int size) {
		super(inputstream, size);

	}

	public byte[] getBuffer() {
		return buf;
	}
}
