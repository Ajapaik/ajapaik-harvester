package ee.ajapaik.harvester;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.openarchives.oai._2.DeletedRecordType;
import org.openarchives.oai._2.IdentifyType;
import org.openarchives.oai._2.ListMetadataFormatsType;
import org.openarchives.oai._2.ListRecordsType;
import org.openarchives.oai._2.ListSetsType;
import org.openarchives.oai._2.MetadataFormatType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.ResumptionTokenType;
import org.openarchives.oai._2.SetType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.ajapaik.db.Repository;
import ee.ajapaik.model.InfoSystem;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.Digester;
import ee.ajapaik.util.FilteredInputStreamReader;
import ee.ajapaik.util.IOHandler;
import ee.ajapaik.util.MonitorableBufferedInputStream;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=HarvestTask">Kaido Kalda</a>
 */
public abstract class HarvestTask extends QuartzJobBean implements ListRecordsType.Listener {

	protected static final Logger logger = Logger.getLogger(HarvestTask.class);
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	protected Repository repository;
	protected String taskCode;
	protected InfoSystem infoSystem;
	protected boolean encodeUrlParameters;
	protected boolean allowInvalidCharacters;
	protected String invalidCharacterReplacement;
	protected String supportedMetadataPrefixes;
	protected Map<String, String> sets;
	protected String format;

	public void setInfoSystem(InfoSystem infoSystem) {
		this.infoSystem = infoSystem;
		this.taskCode = Digester.digestToString(infoSystem.getName());
	}

	public Map<String, String> getSets() {
		return sets;
	}

	public void setSets(Map<String, String> sets) {
		this.sets = sets;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setEncodeUrlParameters(boolean encodeUrlParameters) {
		this.encodeUrlParameters = encodeUrlParameters;
	}

	public void setAllowInvalidCharacters(boolean allowInvalidCharacters) {
		this.allowInvalidCharacters = allowInvalidCharacters;
	}

	public void setInvalidCharacterReplacement(String invalidCharacterReplacement) {
		this.invalidCharacterReplacement = invalidCharacterReplacement;
	}

	public void setSupportedMetadataPrefixes(String supportedMetadataPrefixes) {
		this.supportedMetadataPrefixes = supportedMetadataPrefixes;
	}

	public String getSetName(String setSpec) {
		return this.sets.get(setSpec);
	}

	@Override
	protected void executeInternal(JobExecutionContext jobexecutioncontext) throws JobExecutionException {
		// Register UncaughtExceptionHandler
		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("Uncought error: ", e);
			}
		});

		Date startDate = new Date();
		logger.info("Harvester task started @ " + startDate);
		try {
			boolean supportsDeleted = isSupportsDeletedRecords();
			Date lastHarvest = supportsDeleted ? infoSystem.getLastHarvestTime() : null;
			
			this.format = getSupportedMetadataFormat();
			
			if(infoSystem.getUseSets() != null && infoSystem.getUseSets()) {
				this.sets = loadSets();
			}
			
			if (format != null) {
				HashMap<String, String> params = new HashMap<String, String>();
				addParameter(params, "metadataPrefix", format);

				// XXX: why to clear?
				// repository.clearDatabase(taskCode);
				
				// XXX: set list may change, should clear
				// old list before update.
				if(infoSystem.getUseSets() != null && infoSystem.getUseSets()) {
					repository.saveSets(sets, taskCode);
				}
				
				String set = infoSystem.getUseSet();
				if (set != null) {
					if(infoSystem.getUseSets() != null && infoSystem.getUseSets()) {
						addParameter(params, "set", set);
					}
					
					if (lastHarvest != null) {
						addParameter(params, "from", DATE_FORMAT.format(lastHarvest));
					}

					iterateRecords(params);
				} else {
					iterateSets(params, lastHarvest);
				}
			}
			
			// Mark last harvest date
			infoSystem.setLastHarvestTime(startDate);
		} catch (IOException e) {
			logger.error("Error in IO", e);
		} catch (JAXBException e) {
			logger.error("Error parsing stream", e);
		} catch (Exception e) {
			logger.error("Unknown error", e);
		}
		
		long duration = System.currentTimeMillis() - startDate.getTime();
		
		logger.info("Harvester task finished @ " + new Date() + ". Tooks: " + convertMSToDHMS(duration));
	}
	
	private static String convertMSToDHMS(long ms) {
		long seconds = ms / 1000;
	    long s = seconds % 60;
	    long m = (seconds / 60) % 60;
	    long h = (seconds / (60 * 60));
	    
	    return h + "h " + m + "m " + s + "s";
	}

	private boolean isSupportsDeletedRecords() throws ClientProtocolException, IOException, JAXBException {
		DeletedRecordType type = executeOperation(IdentifyType.class, null).getDeletedRecord();
		return !DeletedRecordType.NO.equals(type);
	}

	private Map<String, String> loadSets() throws ClientProtocolException, IOException, JAXBException {
		LinkedHashMap<String, String> sets = new LinkedHashMap<String, String>();
		HashMap<String, String> setParams = new HashMap<String, String>();
		do {
			ListSetsType listSets = executeOperation(ListSetsType.class, setParams);
			
			if(listSets == null) {
				break;
			}
			
			for (SetType set : listSets.getSet()) {
				sets.put(set.getSetSpec(), set.getSetName());
			}

			ResumptionTokenType rt = listSets.getResumptionToken();
			if (rt != null && rt.getValue() != null) {
				setParams.put("resumptionToken", rt.getValue());
			} else {
				setParams.remove("resumptionToken");
			}

		} while (setParams.get("resumptionToken") != null);

		return sets;
	}

	private void iterateSets(HashMap<String, String> params, Date lastHarvest) throws ClientProtocolException, IOException, JAXBException {
		
		List<String> setsToIgnore = new ArrayList<String>();
		if(infoSystem.getIgnoreSet() != null) {
			setsToIgnore = Arrays.asList(infoSystem.getIgnoreSet().split(","));
		}
		
		if(sets != null && sets.size() > 0) {
			for (String set : this.sets.keySet()) {
				if(!setsToIgnore.contains(set)) {
					iterateSet(params, lastHarvest, set);
				} else {
					logger.debug("Set ignored: " + set);
				}
			}
		} else {
			iterateSet(params, lastHarvest, null);
		}
				
	}

	private void iterateSet(HashMap<String, String> params, Date lastHarvest, String set) throws ClientProtocolException, IOException, JAXBException {
		logger.debug("Opening set: " + set);

		if(set != null) {
			addParameter(params, "set", set);
		}
		
		addParameter(params, "metadataPrefix", format);
		
		if (lastHarvest != null) {
			addParameter(params, "from", DATE_FORMAT.format(lastHarvest));
		}

		iterateRecords(params);

		logger.debug("Set iterated: " + set);
	}
	
	private void addParameter(HashMap<String, String> params, String parameter, String value) {
		params.put(parameter, value);
	}
	
	private void removeParameter(HashMap<String, String> params, String parameter) {
		params.remove(parameter);
	}

	@Override
	public void handleRecord(ListRecordsType listRecordsType, RecordType recordType) {
		Record rec = mapRecord(recordType);
		if (rec != null) {
			save(rec, recordType.getHeader().getSetSpec());
		}
	}
	
	protected void save(Record rec, List<String> specs) {
		if(rec.isDeleted()) {
			repository.deleteRecord(rec.getId(), taskCode);
			return;
		}
		
		rec.setSetSpec(specs);
		rec.setDateCreated(new Date());
		
		repository.saveSingleRecord(rec.getId(), rec, taskCode);
	}

	protected abstract Record mapRecord(RecordType recordType);

	private void iterateRecords(HashMap<String, String> params)
			throws ClientProtocolException, IOException, JAXBException {
		Listener listener = new Listener() {
			@Override
			public void beforeUnmarshal(Object obj, Object obj1) {
				if (obj instanceof ListRecordsType) {
					((ListRecordsType) obj).setRecordListener(HarvestTask.this);
				}
			};

			@Override
			public void afterUnmarshal(Object obj, Object obj1) {
				if (obj instanceof ListRecordsType) {
					((ListRecordsType) obj).setRecordListener(null);
				}
			}
		};

		while (true) {
			ListRecordsType listRecords = executeOperation(ListRecordsType.class, params, listener);
			if (listRecords != null) {
				ResumptionTokenType rt = listRecords.getResumptionToken();
				if (rt != null && rt.getValue() != null && !"".equals(rt.getValue())) {
					logger.debug("Resuming records from token: " + rt.getValue());

					removeParameter(params, "metadataPrefix");
					removeParameter(params, "set");

					addParameter(params, "resumptionToken", rt.getValue());
				} else {
					removeParameter(params, "resumptionToken");
					break;
				}
			} else {
				removeParameter(params, "resumptionToken");
				break;
			}
		}
	}

	private String getSupportedMetadataFormat() throws ClientProtocolException,
			IOException, JAXBException {
		ListMetadataFormatsType meta = executeOperation(
				ListMetadataFormatsType.class, null);
		String[] supportedFormats = supportedMetadataPrefixes.split(",");
		for (MetadataFormatType type : meta.getMetadataFormat()) {
			for (String format : supportedFormats) {
				if (type.getMetadataPrefix().equalsIgnoreCase(format))
					return format;
			}
		}
		return null;
	}

	private <T> T executeOperation(Class<T> clazz,
			Map<String, String> additionalParams)
			throws ClientProtocolException, IOException, JAXBException {
		return executeOperation(clazz, additionalParams, null);
	}

	@SuppressWarnings("unchecked")
	private <T> T executeOperation(Class<T> clazz, Map<String, String> additionalParams, Listener listener) throws ClientProtocolException, IOException, JAXBException {
		JAXBElement<OAIPMHtype> response = null;
		if (clazz.equals(IdentifyType.class)) {
			response = getResponse(getOperationAddress("Identify", additionalParams), listener);
			return (T) response.getValue().getIdentify();
		} else if (clazz.equals(ListMetadataFormatsType.class)) {
			response = getResponse(getOperationAddress("ListMetadataFormats", additionalParams), listener);
			return (T) response.getValue().getListMetadataFormats();
		} else if (clazz.equals(ListSetsType.class)) {
			response = getResponse(getOperationAddress("ListSets", additionalParams), listener);
			return (T) response.getValue().getListSets();
		} else if (clazz.equals(ListRecordsType.class)) {
			response = getResponse(getOperationAddress("ListRecords", additionalParams), listener);
			return (T) response.getValue().getListRecords();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private JAXBElement<OAIPMHtype> getResponse(String operation, Listener listener) throws ClientProtocolException, IOException, JAXBException {
		URL url = new URL(operation);
		
		InputStream is = IOHandler.openStream(url);

		if (is != null) {
			MonitorableBufferedInputStream bis = null;
			try {
				bis = new MonitorableBufferedInputStream(is, 8192 + 1024);
				JAXBContext jc = JAXBContext.newInstance("org.openarchives.oai._2:"
								+ "org.openarchives.oai._2_0.oai_dc:"
								+ "org.purl.dc.elements._1:"
								+ "org.purl.dc.terms:"
								+ "eu.europeana.schemas.ese");

				Unmarshaller u = jc.createUnmarshaller();

				if (listener != null)
					u.setListener(listener);

				if (allowInvalidCharacters) {
					FilteredInputStreamReader fisr = new FilteredInputStreamReader(bis, "UTF-8") {
						@Override
						public char filter(char b) {
							if (b == 0 || b == 0x9 || b == 0xA || b == 0xD
									|| (b >= 0x20 && b <= 0xD7FF)
									|| (b >= 0xE000 && b <= 0xFFFD)
									|| (b >= 0x10000 && b <= 0x10FFFF)) {
								return b;
							}

							if (logger.isTraceEnabled()) {
								try {
									throw new Exception();
								} catch (Exception e) {
									logger.trace("Stack trace:", e);
								}
							}

							logger.warn("Invalid character detected: " + b);
							try {
								logger.warn("Buffer: " + new String(bis.getBuffer(), "UTF-8").trim());
							} catch (UnsupportedEncodingException e) {
								logger.error("Error in encoding buffer", e);
							}

							return invalidCharacterReplacement.toCharArray()[0];
						}
					};
					
					return (JAXBElement<OAIPMHtype>) u.unmarshal(fisr);
				} else {
					return (JAXBElement<OAIPMHtype>) u.unmarshal(bis);
				}
			} catch (JAXBException e) {
				byte[] buffer = bis.getBuffer();
				if(buffer == null) {
					logger.error("Buffer is null", e);
				} else {
					logger.error("Error in buffer: " + new String(buffer, "UTF-8").trim(), e);
				}
				
				throw e;
			} finally {
				try {
					if (bis != null)
						bis.close();
				} catch (IOException e) {
					logger.warn("Error while closing stream", e);
				}
			}
		}
		return null;
	}
	
	private String getOperationAddress(String verb, Map<String, String> additionalParams) {
		StringBuilder builder = new StringBuilder(infoSystem.getAddress());

		builder.append("?verb=");
		builder.append(verb);

		if (additionalParams != null) {
			for (Entry<String, String> entry : additionalParams.entrySet()) {
				builder.append("&");
				builder.append(entry.getKey());
				builder.append("=");

				if (encodeUrlParameters) {
					try {
						builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						logger.error("Error encoding url parameter: " + entry.getValue(), e);
						builder.append(entry.getValue());
					}
				} else
					builder.append(entry.getValue());
			}
		}
		return builder.toString();
	}
	
}
