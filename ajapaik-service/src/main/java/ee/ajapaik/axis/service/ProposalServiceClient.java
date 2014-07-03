package ee.ajapaik.axis.service;

import java.math.BigDecimal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.http.HttpResponse;

import ee.ajapaik.model.Proposal;
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

	@Override
	protected ProposalServiceStub getService(ConfigurationContext context, String endpoint) throws AxisFault {
		return new ProposalServiceStub(context, endpoint);
	}

	public void propose(Proposal proposal) throws Exception {
		SetRequest request = getRequest(proposal);

		SetResponse x = service.set(request);
		
		String e = x.getErrors();
		String r = x.getResult();
		String w = x.getWarnings();
		
		return;
	}
	
	@Override
	protected void beforeResponse(HttpResponse response) {
		response.removeHeaders("Content-Type");
	}

	private SetRequest getRequest(Proposal proposal) throws MalformedURIException {
		Proposal_type0 proposalType = new Proposal_type0();
		proposalType.setNotes(proposal.getNotes());
		proposalType.setObjectId(proposal.getObjectId().intValue());
		proposalType.setObjectPuri(new URI(proposal.getObjectPuri()));
		proposalType.setTaskId(proposal.getTaskId().byteValue());
		proposalType.setXProposalObjectTypeId("DESCRIPTION_UNIT");
		proposalType.setXProposalTypeId("FIELD_CHANGE");
		
		DescriptionUnitMetas_type0 metas = new DescriptionUnitMetas_type0();
		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "PHOTO_FORMAT", "A4"));
		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "GEO_LATITUDE", proposal.getLat().toString()));
		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "GEO_LONGITUDE", proposal.getLon().toString()));
		metas.addDescriptionUnitMetasSequence(getSeq("IMAGE", "GEO_AZIMUTH", proposal.getAzi().toString()));
		
		DescriptionUnit_type0 descriptionUnitType = new DescriptionUnit_type0();
		descriptionUnitType.setDescriptionUnitMetas(metas);
		descriptionUnitType.setReferencesReferences(getReference(proposal.getUrl()));
		
		Object_type0 objectType = new Object_type0();
		objectType.setDescriptionUnit(descriptionUnitType);
		
		proposalType.setObject(objectType);
		
		SetRequest request = new SetRequest();
		request.setProposal(proposalType);
		
		return request;
	}

	private ReferencesReferences_type0 getReference(String value) {
		Reference_type0 referenceType = new Reference_type0();
		referenceType.setName("");
		referenceType.setReferenceTypeId("URL");
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
	
	public static void main(String[] args) throws Exception {
		ProposalServiceClient c = new ProposalServiceClient();
		c.setEndpoint("http://rahvusarhiiv.tietotest.ee/service/proposal/");
		c.setAcceptGzip(false);
		c.setGzipRequest(false);
		c.setChunked(true);
		c.setUseBasicAuth(false);
		c.setUserAgent("test agent");
		c.setMaxConcurrentConnections(1);
		c.setRetryCount(3);
		c.afterPropertiesSet();
		
		
		Proposal proposal = new Proposal();
		proposal.setAzi(new BigDecimal("10.1"));
		proposal.setLat(new BigDecimal("59.437897"));
		proposal.setLon(new BigDecimal("24.749004"));
		proposal.setNotes("Usaldusv22rsus: 60%");
		proposal.setObjectId(577744L);
		proposal.setObjectPuri("http://opendata.rahvusarhiiv.tietotest.ee/du/4a5168f868a1");
		proposal.setTaskId(9L);
		proposal.setUrl("http://ajapaik.ee");
		
		c.propose(proposal);
	}
}
