package ee.ajapaik.cache;

import java.util.List;

import ee.ajapaik.model.City;

public class CityCache extends Cache<City> {
	
	@Override
	protected List<City> loadData() throws Exception {
		return ajapaikClient.listCities();
	}
}
