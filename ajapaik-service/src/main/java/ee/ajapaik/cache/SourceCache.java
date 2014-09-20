package ee.ajapaik.cache;

import java.util.List;

import ee.ajapaik.model.Source;

public class SourceCache extends Cache<Source> {

	@Override
	protected List<Source> loadData() throws Exception {
		return ajapaikClient.listSources();
	}
}
