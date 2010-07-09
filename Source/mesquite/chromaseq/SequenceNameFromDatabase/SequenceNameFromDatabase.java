package mesquite.chromaseq.SequenceNameFromDatabase;


import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.molec.lib.DNADatabaseURLSource;

public class SequenceNameFromDatabase extends SequenceNameSource  {
	boolean preferencesSet = false;
	private String databaseURL = "";
	protected DNADatabaseURLSource databaseURLSource = null;

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		databaseURLSource= (DNADatabaseURLSource)hireEmployee(DNADatabaseURLSource.class, "Source of Database Connectivity");
		if (databaseURLSource==null)
			return false;
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
		echoStringToFile("Using names from database\n", logBuffer);
	}

	/*.................................................................................................................*
	public boolean queryOptions() {
		if (MesquiteThread.isScripting())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Location of Database with Sequence Names",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		final SingleLineTextField databaseURLField = dialog.addTextField("Database URL:", databaseURL, 26, true);		

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== ChromFileNameDialog.OK);
		if (success)  {
			databaseURL = databaseURLField.getText();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	/*.................................................................................................................*/
	
	public boolean isReady() {
		return databaseURLSource!=null;
	}

	/*.................................................................................................................*
	private void prepareFile() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				// check to see if xml
				namesDoc = XMLUtil.getDocumentFromString("samplecodes", sampleCodeList);
				/*
				 * 					if (namesInXml) {
					// check root element
					String rootElementName = namesDoc.getRootElement().getName();
					if (!rootElementName.equals("samplecodes")) {
						// bad root, warn user
						MesquiteMessage.warnUser("Sample codes xml file has a bad format.  Ignoring.");
						namesInXml = false;
					}
				}
				 
				sampleCodeListParser = new Parser(sampleCodeList);
			}
		}			
		
	}
	/*.................................................................................................................*/

	public String getExtractionCode(String prefix, String ID) {
		return ID;
	}

	public String getSampleCode(String prefix, String ID) {
		return ID;
	}
	/*.................................................................................................................*

	public String[] getSeqNamesFromXml(MesquiteString sampleCode) {
		String elementName = "samplecode";
		String nameAttrName = "name";
		String sequenceElementName = "sequence";
		String fullSequenceElementName = "fullsequence";
		String sampleCodeString  = sampleCode.getValue();
		List sampleCodeElements = namesDoc.getRootElement().elements(elementName);
		for (Iterator iter = sampleCodeElements.iterator(); iter.hasNext();) {
			Element nextSampleCodeElement = (Element) iter.next();
			String name = nextSampleCodeElement.attributeValue(nameAttrName);
			if (!StringUtil.blank(name)) {
				if (sampleCodeString.equals(name)) {
					// have a match
					String seq = nextSampleCodeElement.elementText(sequenceElementName);
					String fullseq = seq;
					if (nextSampleCodeElement.element(fullSequenceElementName) != null) {
						fullseq = nextSampleCodeElement.elementText(fullSequenceElementName);
					}
					return new String[]{seq, fullseq};
				}
			}
		}
		// got here and no match found -- log an error
		MesquiteMessage.warnUser("No sample code named '" + sampleCode + "' found in sample code xml file.");
		return new String[]{"", ""};
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
