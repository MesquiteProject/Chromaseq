package mesquite.chromaseq.BTOLDNADatabaseLocationSource;

import mesquite.chromaseq.lib.DNADatabaseURLSource;
import mesquite.tol.lib.MesquiteXMLToLUtilities;

public class BTOLDNADatabaseLocationSource extends DNADatabaseURLSource {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public String getBaseURL() {
		return MesquiteXMLToLUtilities.getTOLPageDatabaseURL("http://btol.tolweb.org");
	}
	
	public String getKey() {
		return "archostemataarec00L";
	}

	public String getABIUploadPage() {
		return "btolxml/SequenceUploadService";
	}

	public String getChromatogramBatchCreationPage() {
		return "btolxml/ChromatogramBatchCreationService";
	}

	public String getChromatogramPage() {
		return "btolxml/ChromatogramSearchService";
	}

	public String getContributorsPage() {
		return "btolxml/ContributorList";
	}

	public String getFASTAUploadPage() {
		return "btolxml/FastaUpload";
	}

	public String getPrimersPage() {
		return "btolxml/PrimerService";
	}

	public String getSequencePage() {
		return "btolxml/XMLService";
	}

	public String getName() {
		return "BTOL DNA Database Location";
	}


}
