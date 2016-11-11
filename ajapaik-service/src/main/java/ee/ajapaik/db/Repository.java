package ee.ajapaik.db;

import com.sleepycat.je.*;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.serialize.Serializer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=Repository">Kaido Kalda</a>
 */
public class Repository implements InitializingBean {

	public interface ResultCallback {
		boolean postResult(DatabaseEntry key, DatabaseEntry value);
	}
	
	private static final Logger logger = Logger.getLogger(Repository.class);
	private static final String RECORD = "record";
	private static final String SET = "set";
	private static final String IMAGE = "image";
	
	private Map<String, Environment> environmentMap;
	
	private long cacheSize;
	private String directory;
	private Serializer serializer;
	
	public Repository() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {			
			@Override
			public void run() {
				synchronized (environmentMap) {
					for (Environment environment : environmentMap.values()) {
						try {
							logger.info("Shutting down repository.");
							environment.close();
						} catch (DatabaseException e) {
							logger.error("Error closing environment", e);
						}
					}
				}
			}
		}));
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setCacheSize(long cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.environmentMap = new HashMap<String, Environment>();
		String[] codes = new File(directory).list();
		if(codes != null) {
			for (String code : codes) {
				getEnvironment(code);
			}
		}
	}

	private Database openDatabase(Environment environment, String table) {
		try {
			DatabaseConfig databaseConfig = new DatabaseConfig();
		    databaseConfig.setAllowCreate(true);
		    databaseConfig.setTransactional(false);
		    
			return environment.openDatabase(null, table, databaseConfig);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	private Environment getEnvironment(String code) {
		Environment environment;
		synchronized (environmentMap) {
			if(!environmentMap.containsKey(code)) {
				File dataDirectory = getDatabaseDirectory(code);
				if(!dataDirectory.exists()) {
					logger.debug("Directory '" + dataDirectory.getAbsolutePath() + "' not exists. Creating...");
					dataDirectory.mkdirs();
				}
				logger.info("Using data dir: " + dataDirectory.getAbsolutePath());
				try{
					EnvironmentConfig environmentConfig = new EnvironmentConfig();
					environmentConfig.setAllowCreate(true);
					environmentConfig.setCacheSize(cacheSize);
					
					environmentMap.put(code, new Environment(dataDirectory, environmentConfig));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			environment = environmentMap.get(code);
		}
		synchronized (environment) {
			return environment;
		}
	}

	private File getDatabaseDirectory(String code) {
		return new File(directory + "/" + code);
	}
	
	private void query(String id, Environment environment, String table, ResultCallback callback) {
		Database db;
		synchronized (environment) {
			db = openDatabase(environment, table);
		}
		
		DatabaseEntry record = new DatabaseEntry();
		Cursor cursor = null;
		try {
			DatabaseEntry recordId;
			if(id != null) {
				recordId = serializer.serializeKey(id);
				
				if (db.get(null, recordId, record, LockMode.DEFAULT) == OperationStatus.SUCCESS)
					callback.postResult(recordId, record);
			} else {
				recordId = new DatabaseEntry();
				cursor = db.openCursor(null, null);
				
				while (cursor.getNext(recordId, record, LockMode.DEFAULT) == OperationStatus.SUCCESS)
					if( !callback.postResult(recordId, record) )
						break;
			}
		} catch (DatabaseException e) {
			logger.error("Error opening cursor", e);
		} catch (Exception e) {
			logger.error("Error deserializing data", e);
		} finally {
			try {
				if(cursor != null)
					cursor.close();
				
				db.close();
			} catch (DatabaseException e) {
				logger.error("Error closing database", e);
			}
		}
	}
	
	public void iterateAllRecordsForIndexing(final RecordHandler handler) {
		synchronized (environmentMap) {
			for(final String code : environmentMap.keySet()) {
				Environment environment = getEnvironment(code);
				synchronized (environment) {
					query(null, environment, RECORD, new ResultCallback() {
						
						@Override
						public boolean postResult(DatabaseEntry key, DatabaseEntry value) {
							try {
								handler.handleRecord(serializer.deserializeRecord(value), code);
								return true;
							} catch (Exception e) {
								logger.error("Error while saving record", e);
								throw new RuntimeException(e);
							}
						}
					});
				}
			}
		}
	}
	
	public Map<String, String> queryAllSets() {
		final Map<String, String> result = new HashMap<String, String>();
		synchronized (environmentMap) {
			for(final String code : environmentMap.keySet()) {
				Environment environment = getEnvironment(code);
				synchronized (environment) {
					query(null, environment, SET, new ResultCallback() {
						@Override
						public boolean postResult(DatabaseEntry key, DatabaseEntry value) {
							try {
								result.put(new String(key.getData()), new String(value.getData()));
								return true;
							} catch (Exception e) {
								logger.error("Error while saving record", e);
								throw new RuntimeException(e);
							}
						}
					});
				}
			}
		}
		return result;
	}

	public byte[] queryImage(String hash) {
		final Object[] holder = new Object[] { null };
		
		synchronized (environmentMap) {
			for(final String code : environmentMap.keySet()) {
				Environment environment = getEnvironment(code);
				synchronized (environment) {
					query(hash, environment, IMAGE, new ResultCallback() {
						@Override
						public boolean postResult(DatabaseEntry key, DatabaseEntry value) {
							try {
								holder[0] = value.getData(); 
								return true;
							} catch (Exception e) {
								logger.error("Error while saving record", e);
								throw new RuntimeException(e);
							}
						}
					});
				}
			}
		}
		return holder[0] != null ? (byte[]) holder[0] : null;
	}
	
	public void saveImage(String key, byte[] data, String code) {
		long start = System.nanoTime();

		Environment environment = getEnvironment(code);
		synchronized (environment) {
			Database db = openDatabase(environment, IMAGE);

			try {
				db.put(null, serializer.serializeKey(key), new DatabaseEntry(data));
			} catch (Exception e) {
				logger.error("Error while saving record", e);
				throw new RuntimeException(e);
			} finally {
				try {
					db.close();
				} catch (DatabaseException e) {
					logger.error("Error closing database", e);
				}
			}
		}
		if(logger.isDebugEnabled())
			logger.debug("Record saved in " + (System.nanoTime() - start) / 1000000D + " ms");
	}
	
	public void saveSingleRecord(String key, Record data, String code) {
		long start = System.nanoTime();

		Environment environment = getEnvironment(code);
		synchronized (environment) {
			Database db = openDatabase(environment, RECORD);

			try {
				db.put(null, serializer.serializeKey(key), serializer.serializeRecord(data));
			} catch (Exception e) {
				logger.error("Error while saving record", e);
				throw new RuntimeException(e);
			} finally {
				try {
					db.close();
				} catch (DatabaseException e) {
					logger.error("Error closing database", e);
				}
			}
		}
		if(logger.isDebugEnabled())
			logger.debug("Record(" + data.getId() + ") saved in " + (System.nanoTime() - start) / 1000000D + " ms");
	}
	
	public void saveSets(Map<String, String> sets, String code) {
		long start = System.nanoTime();

		Environment environment = getEnvironment(code);
		synchronized (environment) {
			Database db = openDatabase(environment, SET);

			try {
				for (String set : sets.keySet()) {
					db.put(null, 
							new DatabaseEntry(set.getBytes()), 
							new DatabaseEntry(sets.get(set).getBytes()));
				}
			} catch (Exception e) {
				logger.error("Error while saving sets", e);
				throw new RuntimeException(e);
			} finally {
				try {
					db.close();
				} catch (DatabaseException e) {
					logger.error("Error closing database", e);
				}
			}
		}
		if(logger.isDebugEnabled())
			logger.debug("Sets saved in " + (System.nanoTime() - start) / 1000000D + " ms");
	}

	public void clearDatabase(String code) {
		synchronized (environmentMap) {
			Environment environment = getEnvironment(code);
			try {
				environment.close();
				environmentMap.remove(code);
			} catch (DatabaseException e) {
				logger.error("Error closing database", e);
			}
			
			File[] dataFiles = getDatabaseDirectory(code).listFiles();
			for (File file : dataFiles) {
				file.delete();
			}
		}
	}

	public void deleteRecord(String key, String code) {
		long start = System.nanoTime();

		Environment environment = getEnvironment(code);
		synchronized (environment) {
			Database db = openDatabase(environment, RECORD);
			try {
				OperationStatus os = db.delete(null, serializer.serializeKey(key));
				
				logger.debug("Deleted: code: " + code + ", key: " + key + ", status: " + os.toString());
			} catch (Exception e) {
				logger.error("Error while deleting record", e);
				throw new RuntimeException(e);
			} finally {
				try {
					db.close();
				} catch (DatabaseException e) {
					logger.error("Error closing database", e);
				}
			}
		}
		if(logger.isDebugEnabled())
			logger.debug("Record deleted in " + (System.nanoTime() - start) / 1000000D + " ms");
		
	}
}
