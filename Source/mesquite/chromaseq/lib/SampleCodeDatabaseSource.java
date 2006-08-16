package mesquite.chromaseq.lib;

import java.util.Hashtable;
import java.util.Map;

import mesquite.lib.MesquiteMessage;

import org.jdom.Document;
import org.tolweb.treegrow.main.HttpRequestMaker;
import org.tolweb.treegrow.main.RequestParameters;
import org.tolweb.treegrow.main.XMLConstants;

public class SampleCodeDatabaseSource {
	private String databaseURL;
	
	public SampleCodeDatabaseSource(String databaseUrl) {
		this.databaseURL = databaseUrl;
	}
	
	/**
	 * Makes a remote call to the database to return the sequenceName
	 * and fullSequenceName based on the sample code
	 * @param code The code of the sequence
	 * @param sourceDatabase 0 for DRM, 1 for BTOL -- more in the future
	 * @return a string array with the first element being the sequenceName
	 * and the second being the fullSequenceName
	 */
	public String[] getSequenceNamesFromCode(String code, int sourceDatabase) {
		String urlPrefix = getDatabaseURL();
		Map args = new Hashtable();
		args.put(RequestParameters.SOURCE_DB, new Integer(sourceDatabase));
		args.put(RequestParameters.CODE, code);
		Document doc = HttpRequestMaker.getTap4ExternalUrlDocument(urlPrefix, "btolxml/XMLService", args);
		String sequenceName = "";
		if (doc == null || doc.getRootElement() == null || 
				doc.getRootElement().getName().equals(XMLConstants.ERROR)) {
			MesquiteMessage.warnUser("No dna extraction found matching code: " + 
					code + " in database: " + sourceDatabase);
			return new String[] {"", ""};
		} else {
			sequenceName = doc.getRootElement().getChildText(XMLConstants.SEQUENCE);
			return new String[] {sequenceName, sequenceName};			
		}
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}
}
