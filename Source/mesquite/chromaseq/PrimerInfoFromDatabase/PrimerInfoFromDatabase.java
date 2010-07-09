package mesquite.chromaseq.PrimerInfoFromDatabase;

import mesquite.chromaseq.lib.PrimerInfoSource;
import mesquite.lib.StringUtil;
import mesquite.molec.lib.DNADatabaseURLSource;

public class PrimerInfoFromDatabase extends PrimerInfoSource {
	boolean preferencesSet = false;
	PrimerInformationDatabase primers = null;
	protected DNADatabaseURLSource databaseURLSource = null;
	private String databaseURL = "";

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (databaseURLSource==null)
			databaseURLSource= (DNADatabaseURLSource)hireEmployee(DNADatabaseURLSource.class, "Source of Database Connectivity");
		if (databaseURLSource==null)
			return false;
		
		 primers = new PrimerInformationDatabase(databaseURLSource);

		
		return true;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		 if ("databaseURL".equalsIgnoreCase(tag))
				databaseURL = StringUtil.cleanXMLEscapeCharacters(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "databaseURL", databaseURL);
		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void echoParametersToFile(StringBuffer logBuffer) {
		echoStringToFile("Using primers database\n", logBuffer);
	}

	/*.................................................................................................................*/

	public String getGeneFragmentName(String ID) {
		if (primers!=null) {
			return primers.getFragmentName(ID);
		}
		return null;
	}

	public String getPrimerName(String ID) {
		return ID;
	}

	public int getPrimerSequenceLength(String ID) {
		if (primers!=null) {
			String s= primers.getSequence(ID);
			if (s!=null)
				return s.length();
		}
		return 0;
	}

	public String getPrimerSequenceString(String ID) {
		if (primers!=null) {
			return primers.getSequence(ID);
		}
		return null;
	}

	public boolean isForward(String ID) {
		if (primers!=null) {
			return primers.isForward(ID);
		}
		return false;
	}

	public boolean isReady() {
		return databaseURLSource!=null;
	}

	/*.................................................................................................................*/

	public String getName() {
		return "Primer Information from Database";
	}
	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides primer information from a database.";
	}


}




