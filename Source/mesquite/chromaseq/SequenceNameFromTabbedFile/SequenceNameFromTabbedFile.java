package mesquite.chromaseq.SequenceNameFromTabbedFile;

import java.awt.Button;
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import mesquite.chromaseq.lib.SequenceNameSource;
import mesquite.lib.*;

public class SequenceNameFromTabbedFile extends SequenceNameSource implements ActionListener, TextListener {
	String sampleCodeListPath = null;
	String sampleCodeList = "";
	Parser sampleCodeListParser = null;
	boolean preferencesSet = false;
	SingleLineTextField sampleCodeFilePathField = null;
	int chosenNameCategory = -1;
	String[] nameCategories = null;

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!optionsSpecified()){
			if (MesquiteThread.isScripting() && StringUtil.blank(sampleCodeListPath))
				return false;
			scanTabbedDocument();  // just in case we have the path already stored
			if (!queryOptions())
				return false;
			if (!scanTabbedDocument())
				return false;
		} 
		return true;
	}

	/*.................................................................................................................*/
	public boolean processNameCategories() {
		sampleCodeListParser = new Parser(sampleCodeList);
		Parser subParser = new Parser();
		String line = sampleCodeListParser.getRawNextDarkLine();
		subParser.setString(line);
		subParser.setWhitespaceString("\t");
		subParser.setPunctuationString("");
		String s = subParser.getFirstToken(); // should be "code"
		s = subParser.getNextToken(); // should be "File Name"
		int count = 0;
		while (!subParser.atEnd() && StringUtil.notEmpty(s)){  // let's count
			s = subParser.getNextToken(); // should be  the category name
			if (StringUtil.notEmpty(s))
				count++;
		}
		nameCategories = new String[count];
		subParser.setString(line);
		s = subParser.getFirstToken(); // should be "code"
		s = subParser.getNextToken(); // should be "File Name"
		count = 0;
		while (!subParser.atEnd() && StringUtil.notEmpty(s)){
			s = subParser.getNextToken(); // should be the category name
			if (StringUtil.notEmpty(s)){
				nameCategories[count]=s;
				count++;
			}
		}
		return true;
		
	}
	/*.................................................................................................................*/
	public boolean scanTabbedDocument() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				sampleCodeListParser = new Parser(sampleCodeList);
				return processNameCategories();
			}
		}	
		return false;
		
	}
	/*.................................................................................................................*/
	public boolean queryForOptionsAsNeeded() {
		if (StringUtil.blank(sampleCodeListPath)){
			return queryOptions();
		}			
		if (StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);
		}
		if (!StringUtil.blank(sampleCodeList)) 
			sampleCodeListParser = new Parser(sampleCodeList);
		if (nameCategories==null){
			return processNameCategories();
		}
		return true;
	}

	/*.................................................................................................................*
	public void initialize() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				sampleCodeListParser = new Parser(sampleCodeList);
				processNameCategories();
			}
		}			
	}

	/*.................................................................................................................*/
	public boolean optionsSpecified(){
		return StringUtil.notEmpty(sampleCodeListPath);
	}


	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		 if ("sampleCodeListPath".equalsIgnoreCase(tag)){
			sampleCodeListPath = StringUtil.cleanXMLEscapeCharacters(content);
		 }
		 if ("chosenNameCategory".equalsIgnoreCase(tag)){
			chosenNameCategory = MesquiteInteger.fromString(content);
		 }
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "sampleCodeListPath", sampleCodeListPath);  
		StringUtil.appendXMLTag(buffer, 2, "chosenNameCategory", chosenNameCategory);  
		preferencesSet = true;
		return buffer.toString();
	}
	Choice categoryChoice ;

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Location of File with Sequence Names",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		sampleCodeFilePathField = dialog.addTextField("Sequence names file:", sampleCodeListPath,26);
		sampleCodeFilePathField.addTextListener(this);
		final Button dnaCodesBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		dnaCodesBrowseButton.setActionCommand("DNANumbersBrowse");

		String[] categories=null;
		
		if (nameCategories==null) {
			if (!scanTabbedDocument()) {
				categories = new String[1];
				categories[0]="Sample Code                  ";
			} else
				categories = nameCategories;
		} else
			categories = nameCategories;

		int currentCategory = chosenNameCategory;
		if (currentCategory<0)
			currentCategory=0;
		categoryChoice = dialog.addPopUpMenu("Names to use:", categories, currentCategory);

		String s = "This file must contain in its first line the titles of each of the columns, delimited by tabs.  The first column must be the sample code, the second column the names to be use for file and folder names, ";
		s+= "and the third and later columns should contain alternative naming schemes for the sequences. Each of the following lines must contain the entry for one sample.\n\n";
		s+= "<BR><BR>For example, the file might look like this:<br><br>\n";
		s+= "code  &lt;tab&gt;  File name &lt;tab&gt;  Standard name  &lt;tab&gt;  Name with numbers  &lt;tab&gt;  Name with localities <br>\n";
		s+= "001  &lt;tab&gt;  Bemb_quadrimaculatum_001 &lt;tab&gt;  Bembidion quadrimaculatum  &lt;tab&gt;  Bembidion quadrimaculatum 001  &lt;tab&gt;  Bembidion quadrimaculatum ONT <br>\n";
		s+= "002  &lt;tab&gt;  Bemb_festivum_002 &lt;tab&gt;  Bembidion festivum  &lt;tab&gt;  Bembidion festivum 002  &lt;tab&gt;  Bembidion festivum CA:Fresno <br>\n";
		s+= "003  &lt;tab&gt;  Bemb_occultator_003 &lt;tab&gt;  Bembidion occultator  &lt;tab&gt;  Bembidion occultator 003  &lt;tab&gt;  Bembidion occultator ON:Dwight <br>\n";
		s+= "004  &lt;tab&gt;  Lion_chintimini_004 &lt;tab&gt;  Lionepha chintimini  &lt;tab&gt;  Lionepha chintimini 004  &lt;tab&gt;  Lionepha chintimini OR:Marys Peak <br><br>\n";
		s+= "You will need to choose which of the later columns are to be used as the name within the FASTA file.\n\n";

		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			sampleCodeListPath = sampleCodeFilePathField.getText();
			chosenNameCategory = categoryChoice.getSelectedIndex();
		//	initialize();  // is this needed?
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	public boolean isReady() {
		return sampleCodeListPath!=null;
	}

	/*.................................................................................................................*/

	public String getExtractionCode(String prefix, String ID) {
		return ID;
	}

	public String getSampleCode(String prefix, String ID) {
		return ID;
	}
	/*.................................................................................................................*/

	public  String[] getSeqNamesFromTabDelimitedFile(MesquiteString sampleCode) {
		queryForOptionsAsNeeded();
		if (sampleCodeListParser==null)
			return null;
		String sampleCodeString  = sampleCode.getValue();
		sampleCodeListParser.setPosition(0);
		Parser subParser = new Parser();
		String line = sampleCodeListParser.getRawNextDarkLine();
		while (StringUtil.notEmpty(line)) {
				subParser.setString(line);
				subParser.setWhitespaceString("\t");
				subParser.setPunctuationString("");
				String code = subParser.getFirstToken();
				if (sampleCodeString.equalsIgnoreCase(code)) {
					String fileName = subParser.getNextToken();
					String sequenceName=subParser.getNextToken();
					for (int i=0; i<chosenNameCategory; i++) {
						sequenceName=subParser.getNextToken();
					}
					if (StringUtil.blank(sequenceName))
						sequenceName=fileName;
					return new String[]{fileName, sequenceName};
			}
			line = sampleCodeListParser.getRawNextDarkLine();
		}
		// got here and no match found -- log an error
		MesquiteMessage.warnUser("No sample code named '" + sampleCode + "' found in sample code names file.");
		return new String[]{""+sampleCode, ""+sampleCode};
	}
	/*.................................................................................................................*/

	public String getAlternativeName(String prefix, String ID) {  // short name
		String[] results = null;
		results = getSeqNamesFromTabDelimitedFile(new MesquiteString(ID));
		
		if (results==null || results.length<1)
			return null;
		return results[0];
	}
	/*.................................................................................................................*/

	public String getSequenceName(String prefix, String ID) {  // long name
		String[] results = null;
			results = getSeqNamesFromTabDelimitedFile(new MesquiteString(ID));
		
		if (results!=null) {
			if(results.length>=2)
				return results[1];
			else if(results.length>=1)
				return results[0];
		}
		return null;
	}
	/*.................................................................................................................*/
	
	public String getParameters() {
		if (StringUtil.blank(sampleCodeListPath))
			return "Names and codes file unspecified.";
		String s = "Names and codes file: " + sampleCodeListPath;
		if (nameCategories==null || chosenNameCategory<0 || chosenNameCategory>=nameCategories.length || StringUtil.blank(nameCategories[chosenNameCategory]))
			return s;
		return s+"\nNames Category: " + nameCategories[chosenNameCategory];
	}
	/*.................................................................................................................*/
	public void echoParametersToFile(StringBuffer logBuffer) {
		loglnEchoToStringBuffer("Using names and codes file: " +sampleCodeListPath+"\n", logBuffer);
	}

	/*.................................................................................................................*/

	public boolean hasAlternativeNames() {
		return true;
	}
	/*.................................................................................................................*/

	public boolean hasOptions() {
		return true;
	}
	/*.................................................................................................................*/

	public String getName() {
		return "Sequence Names from Tab-delimited Text File";
	}
	/*.................................................................................................................*/

	public boolean requestPrimaryChoice() {
		return true;
	}
	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides sequence names from a tab-delimited text file.";
	}

	/*.................................................................................................................*/

	public boolean isPrerelease() {
		return false;
	}

	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		 if (e.getActionCommand().equalsIgnoreCase("DNANumbersBrowse")) {
			MesquiteString dnaNumberListDir = new MesquiteString();
			MesquiteString dnaNumberListFile = new MesquiteString();
			String s = MesquiteFile.openFileDialog("Choose file containing sample codes and names", dnaNumberListDir, dnaNumberListFile);
			if (!StringUtil.blank(s)) {
				sampleCodeListPath = s;
				if (sampleCodeFilePathField!=null) 
					sampleCodeFilePathField.setText(sampleCodeListPath);
			}
		}
	}
	public void textValueChanged(TextEvent e) {
		if (e.getSource().equals(sampleCodeFilePathField)) {
			sampleCodeListPath = sampleCodeFilePathField.getText();
			if (StringUtil.notEmpty(sampleCodeListPath)) {
				chosenNameCategory=-1;
			//	scanTabbedDocument();
				scanTabbedDocument();
			//	processNameCategories();
				//initialize(sampleCodeListPath);
			}	
			categoryChoice.removeAll();
			if (nameCategories!=null) {			
				for (int i=0; i<nameCategories.length; i++) 
					if (!StringUtil.blank(nameCategories[i])) {
						categoryChoice.add(nameCategories[i]);
					}
			}
			categoryChoice.repaint();
			//				categoryChoice = dialog.addPopUpMenu("Names to use:", xmlProcessor.getNameCategoryDescriptions(), tagNumber);

		}
	}

}
