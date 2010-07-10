package mesquite.chromaseq.SequenceNameFromDatabase;


import org.dom4j.Element;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.molec.lib.DNADatabaseURLSource;

public class SequenceNameFromDatabase extends SequenceNameSource  {
	boolean preferencesSet = false;
//	private String databaseURL = "";
	protected DNADatabaseURLSource databaseURLSource = null;

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		databaseURLSource= (DNADatabaseURLSource)hireEmployee(DNADatabaseURLSource.class, "Source of Database Connectivity");
		if (databaseURLSource==null)
			return false;
		return true;
		}

	/*.................................................................................................................*/
	public  void addXMLAttributes(Element element){
		element.addAttribute("databaseURLSource", databaseURLSource.getClassName());
	}

	/*.................................................................................................................*
	public void processSingleXMLPreference (String tag, String content) {
		 if ("databaseURL".equalsIgnoreCase(tag))
				databaseURL = StringUtil.cleanXMLEscapeCharacters(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "databaseURL", databaseURL);
		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void echoParametersToFile(StringBuffer logBuffer) {
		echoStringToFile("Using names from database\n", logBuffer);
	}

	/*.................................................................................................................*/
	
	public boolean isReady() {
		return databaseURLSource!=null;
	}

	/*.................................................................................................................*/

	public String getExtractionCode(String prefix, String ID) {
		return ID;
	}

	public String getSampleCode(String prefix, String ID) {
		return ID;
	}
	/*.................................................................................................................*/
	public void checkDatabaseSource() {
		if (databaseURLSource==null)
			databaseURLSource= (DNADatabaseURLSource)hireEmployee(DNADatabaseURLSource.class, "Source of Database Connectivity");
	}
		/*.................................................................................................................*/

	public String getAlternativeName(String prefix, String ID) {  // short name
		checkDatabaseSource();
		String[] results = new DatabaseSampleCodeSource().getSequenceNamesFromCode(databaseURLSource, prefix, ID);
		if (results==null || results.length<1)
			return null;
		return results[0];
	}
	/*.................................................................................................................*/

	public String getSequenceName(String prefix, String ID) {  // long name
		checkDatabaseSource();
		String[] results = new DatabaseSampleCodeSource().getSequenceNamesFromCode(databaseURLSource, prefix,ID);
		if (results!=null) {
			if(results.length>=2)
				return results[1];
			else if(results.length>=1)
				return results[0];
		}
		return null;
	}
	/*.................................................................................................................*/

	public boolean hasAlternativeNames() {
		return true;
	}
	/*.................................................................................................................*/

	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides sequence names from a database.";
	}

	/*.................................................................................................................*/

	public String getName() {
		return "Sequence Name From Database";
	}

	/*.................................................................................................................*/

}
