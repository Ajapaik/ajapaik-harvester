package ee.ajapaik.cache;

import java.util.List;

import ee.ajapaik.model.Photo;

public class PhotoCache extends Cache<Photo> {
	
	@Override
	protected List<Photo> loadData() throws Exception {
		return ajapaikClient.listPhotos();
	}
}
