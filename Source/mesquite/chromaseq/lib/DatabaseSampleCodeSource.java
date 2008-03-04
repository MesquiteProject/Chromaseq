package mesquite.chromaseq.lib;

import java.util.Hashtable;
import java.util.Map;

import mesquite.lib.MesquiteMessage;
import mesquite.lib.StringUtil;
import mesquite.lib.MesquiteXMLUtilities;
import mesquite.BTOL.lib.*;
import mesquite.molec.lib.*;

import org.dom4j.*;
import mesquite.tol.lib.*;

/**
 * Class that looks up sequences in a database based on a sample code 
 * @author dmandel
 *
 */
public class DatabaseSampleCodeSource {
	
	public DatabaseSampleCodeSource() {
	}
	
	/**
	 * Makes a remote call to the database to return the sequenceName
	 * and fullSequenceName based on the sample code
	 * 
	 * Assumes XMLUtilities.setDatabaseURL() has been called previously
	 * with the correct url
	 * 
	 * @param code The code of the sequence
	 * @return a string array with the first element being the sequenceName
	 * and the second being the fullSequenceName
	 */
	public String[] getSequenceNamesFromCode(DNADatabaseURLSource databaseURLSource, String code) {
		if (databaseURLSource==null)
			return null;
		Map args = new Hashtable();
		args.put(BTOLRequestParameters.CODE, code);
		Document doc = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURLSource.getBaseURL(), databaseURLSource.getPage(DNADatabaseURLSource.SEQUENCE_NAME_SERVICE), args);
		String sequenceName = "";
		if (doc == null) {
			outputCodeError(code);
			return new String[] {"", ""};
		} else {
			sequenceName = doc.getRootElement().elementText(XMLConstants.SEQUENCE);
			if (StringUtil.blank(sequenceName) || sequenceName.equals("null")) {
				outputCodeError(code);
			}
			return new String[] {sequenceName, sequenceName};			
		}
	}

	private void outputCodeError(String code) {
		MesquiteMessage.warnUser("No dna extraction found matching code: " + 
				code);
	}
}
