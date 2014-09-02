package ee.ajapaik.axis.service;

import java.io.IOException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import ee.ajapaik.model.Location;
import ee.ajapaik.model.MediaView;
import ee.ra.ais.ProposalServiceStub;
import ee.ra.ais.ProposalServiceStub.DescriptionUnitMeta_type0;
import ee.ra.ais.ProposalServiceStub.DescriptionUnitMetasSequence;
import ee.ra.ais.ProposalServiceStub.DescriptionUnitMetas_type0;
import ee.ra.ais.ProposalServiceStub.DescriptionUnit_type0;
import ee.ra.ais.ProposalServiceStub.Object_type0;
import ee.ra.ais.ProposalServiceStub.Proposal_type0;
import ee.ra.ais.ProposalServiceStub.Reference_type0;
import ee.ra.ais.ProposalServiceStub.ReferencesReferencesSequence;
import ee.ra.ais.ProposalServiceStub.ReferencesReferences_type0;
import ee.ra.ais.ProposalServiceStub.SetRequest;
import ee.ra.ais.ProposalServiceStub.SetResponse;

public class ProposalServiceClient extends AbstractSOAPClient<ProposalServiceStub> {
	
	protected static final Logger logger = Logger.getLogger(ProposalServiceClient.class);
	
	private HttpEntity entity;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		
		((DefaultHttpClient)getHttpClient()).setReuseStrategy(new ConnectionReuseStrategy() {

			@Override
			public boolean keepAlive(HttpResponse response, HttpContext context) {
				return false;
			}
		});
	}

	@Override
	protected ProposalServiceStub getService(ConfigurationContext context, String endpoint) throws AxisFault {
		return new ProposalServiceStub(context, endpoint);
	}
	
	public void proposePermalink(MediaView mediaView, String link) throws Exception {
		Proposal_type0 proposalType = new Proposal_type0();
		proposalType.setNotes("permalink");
		proposalType.setObjectId(mediaView.getObjectId().intValue());
		proposalType.setObjectPuri(new URI(mediaView.getPuri()));
		proposalType.setTaskId(mediaView.getTaskId().byteValue());
		proposalType.setXProposalObjectTypeId("DESCRIPTION_UNIT");
		proposalType.setXProposalTypeId("FIELD_CHANGE");
		
//		DescriptionUnitMetas_type0 metas = new DescriptionUnitMetas_type0();
//		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "PHOTO_FORMAT", "A4"));
		
		DescriptionUnit_type0 descriptionUnitType = new DescriptionUnit_type0();
//		descriptionUnitType.setDescriptionUnitMetas(metas);
		descriptionUnitType.setReferencesReferences(getReference("Photo", "URL", link));
		
		Object_type0 objectType = new Object_type0();
		objectType.setDescriptionUnit(descriptionUnitType);
		
		proposalType.setObject(objectType);
		
		SetRequest request = new SetRequest();
		request.setProposal(proposalType);
		
		parseResponse(service.set(request));
		
		if(entity != null) {
			logger.debug("Closing entity");
			
			entity.getContent().close();
			
			this.entity = null;
		}
	}

	public void proposeLocation(MediaView mediaView, Location location) throws Exception {
		Proposal_type0 proposalType = new Proposal_type0();
		proposalType.setNotes("Usaldusväärsus: " + location.getNotes());
		proposalType.setObjectId(mediaView.getObjectId().intValue());
		proposalType.setObjectPuri(new URI(mediaView.getPuri()));
		proposalType.setTaskId(mediaView.getTaskId().byteValue());
		proposalType.setXProposalObjectTypeId("DESCRIPTION_UNIT");
		proposalType.setXProposalTypeId("FIELD_CHANGE");
		
		DescriptionUnitMetas_type0 metas = new DescriptionUnitMetas_type0();
		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "PHOTO_FORMAT", "A4"));
		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "GEO_LATITUDE", location.getLat()));
		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "GEO_LONGITUDE", location.getLon()));
//		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "GEO_AZIMUTH", proposal.getAzi().toString()));
		
		DescriptionUnit_type0 descriptionUnitType = new DescriptionUnit_type0();
		descriptionUnitType.setDescriptionUnitMetas(metas);
//		descriptionUnitType.setReferencesReferences(getReference("", "", ""));
		
		Object_type0 objectType = new Object_type0();
		objectType.setDescriptionUnit(descriptionUnitType);
		
		proposalType.setObject(objectType);
		
		SetRequest request = new SetRequest();
		request.setProposal(proposalType);
		
		parseResponse(service.set(request));
		
		
		if(entity != null) {
			logger.debug("Closing entity");
			
            try {
                EntityUtils.consume(entity);
                
//                entity.getContent().close();
            } catch (IOException e) {
                logger.error("Error while cleaning response", e);
            }
            
			this.entity = null;
		}
	}

	private void parseResponse(SetResponse response) throws Exception {
		if(response.getErrors() != null) {
			throw new Exception(response.getErrors());
		}
		
		if(response.getWarnings() != null) {
			logger.warn("Proposal returned warning: " + response.getWarnings());
		}
		
		if(response.getResult() != null) {
			logger.info("Proposal returned result: " + response.getResult());
		}
	}

	@Override
	protected void beforeResponse(HttpResponse response) {
		response.removeHeaders("Content-Type");
		
		this.entity = response.getEntity();
	}

	private ReferencesReferences_type0 getReference(String name, String type, String value) {
		Reference_type0 referenceType = new Reference_type0();
		referenceType.setName(name);
		referenceType.setReferenceTypeId(type);
		referenceType.setReferenceValue(value);
		
		ReferencesReferencesSequence seq = new ReferencesReferencesSequence();
		seq.setReference(referenceType);
		
		ReferencesReferences_type0 references = new ReferencesReferences_type0();
		references.addReferencesReferencesSequence(seq);
		
		return references;
	}

	private DescriptionUnitMetasSequence getSeq(String group, String id, String value) {
		DescriptionUnitMeta_type0 descriptionUnitMeta = new DescriptionUnitMeta_type0();
		descriptionUnitMeta.setValue(value);
		descriptionUnitMeta.setXMetadataGroupId(group);
		descriptionUnitMeta.setXMetadataId(id);
		
		DescriptionUnitMetasSequence seq = new DescriptionUnitMetasSequence();
		seq.setDescriptionUnitMeta(descriptionUnitMeta);
		
		return seq;
	}
}
