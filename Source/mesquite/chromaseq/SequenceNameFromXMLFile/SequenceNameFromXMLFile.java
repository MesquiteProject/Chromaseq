package mesquite.chromaseq.SequenceNameFromXMLFile;

import java.awt.Button;
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import mesquite.chromaseq.ViewChromatograms.VChromWindow;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;

public class SequenceNameFromXMLFile extends SequenceNameSource implements ActionListener, TextListener {
	ChromaseqSampleToNamesXMLProcessor xmlProcessor;
	String sampleCodeListPath = null; 
	boolean preferencesSet = false;
	SingleLineTextField sampleCodeFilePathField = null;
	String chosenNameCategoryTag;



	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!optionsSpecified()){
			if (MesquiteThread.isScripting() && StringUtil.blank(sampleCodeListPath))
				return false;
			if (!queryOptions())
				return false;
		} 
		return StringUtil.notEmpty(chosenNameCategoryTag);

	}

	/*.................................................................................................................*/
	public void initialize (String sampleCodeListPath) {
		xmlProcessor = new ChromaseqSampleToNamesXMLProcessor(this, sampleCodeListPath);
		if (StringUtil.notEmpty(chosenNameCategoryTag)	)
			xmlProcessor.setChosenTag(chosenNameCategoryTag);
	}
	/*.................................................................................................................*/
	public void initialize() {
		if (StringUtil.notEmpty(sampleCodeListPath) && (xmlProcessor == null || !xmlProcessor.isValid())) {
			initialize(sampleCodeListPath);
		}			
	}
	/*.................................................................................................................*/
	public boolean queryForOptionsAsNeeded() {
		if (StringUtil.blank(sampleCodeListPath)){
			return queryOptions();
		}			
		else if (xmlProcessor == null || !xmlProcessor.isValid()) {
			initialize(sampleCodeListPath);
		} else {
			chosenNameCategoryTag = xmlProcessor.getChosenTag();
		}
		
		return true;
	}

	/*.................................................................................................................*/
	public boolean optionsSpecified(){
		return StringUtil.notEmpty(sampleCodeListPath);
	}

	/*.................................................................................................................*/
	public  void addXMLAttributes(Element element){
		element.addAttribute("sampleCodeListPathXML", sampleCodeListPath);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("sampleCodeListPathXML".equalsIgnoreCase(tag))
			sampleCodeListPath = StringUtil.cleanXMLEscapeCharacters(content);
		if ("chosenNameCategoryTag".equalsIgnoreCase(tag)){
			chosenNameCategoryTag = StringUtil.cleanXMLEscapeCharacters(content);
		}
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "sampleCodeListPathXML", sampleCodeListPath);  
		StringUtil.appendXMLTag(buffer, 2, "chosenNameCategoryTag", chosenNameCategoryTag);  
		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	Choice categoryChoice;
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Location of XML File with Sequence Names",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		sampleCodeFilePathField = dialog.addTextField("Sequence names XML file:", sampleCodeListPath,26);
		sampleCodeFilePathField.addTextListener(this);
		final Button dnaCodesBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		dnaCodesBrowseButton.setActionCommand("DNANumbersBrowse");

		int tagNumber = 0;
		String[] categories = null;
		if (xmlProcessor!=null){
			tagNumber=xmlProcessor.getChosenTagNumber();
			categories=xmlProcessor.getNameCategoryDescriptions();
		} else{
			categories=new String[1];
			categories[0]="Sample Code                  ";
		}
		
		categoryChoice = dialog.addPopUpMenu("Names to use:", categories, tagNumber);

		String s = "This file should contain, in XML format, the names to be used for the sequences, and the sample codes to which each corresponds. See the Chromaseq manual for an example.\n\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			sampleCodeListPath = sampleCodeFilePathField.getText();
			int chosen = categoryChoice.getSelectedIndex();
			if (xmlProcessor!=null){
				xmlProcessor.setChosenTag(chosen);
				chosenNameCategoryTag = xmlProcessor.getChosenTag();
			}
			initialize();
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

	public  String[] getSeqNamesFromXml(MesquiteString sampleCode) {
		queryForOptionsAsNeeded();
		String fileAndFolderNameElement = "fileAndFolderName";
		return xmlProcessor.getSeqNamesFromXml(sampleCode, fileAndFolderNameElement, chosenNameCategoryTag);
	}
	/*.................................................................................................................*

	public  String[] getSeqNamesFromTabDelimitedFile(MesquiteString sampleCode) {
		if (sampleCodeListParser==null)
			return null;
		String sampleCodeString  = sampleCode.getValue();
		sampleCodeListParser.setPosition(0);
		Parser subParser = new Parser();
		String line = sampleCodeListParser.getRawNextDarkLine();
		while (StringUtil.notEmpty(line)) {
			if (line.indexOf("\t")>=0){
				subParser.setString(line);
				subParser.setWhitespaceString("\t");
				subParser.setPunctuationString(";");
				String code = subParser.getFirstToken();
				if (sampleCodeString.equalsIgnoreCase(code)) {
					String seq = subParser.getNextToken();
					if (seq.equals(";") || StringUtil.blank(seq))
						seq="";
					String fullseq = subParser.getNextToken();
					if (StringUtil.blank(fullseq) || fullseq.equals(";"))
						fullseq=seq;
					return new String[]{seq, fullseq};
				}
			}
			else {
				subParser.setString(line);
				String code = subParser.getFirstToken();
				if (sampleCodeString.equalsIgnoreCase(code)) {
					String seq = subParser.getNextToken();
					if (seq.equals(";") || StringUtil.blank(seq))
						seq="";
					String fullseq = subParser.getNextToken();
					if (StringUtil.blank(fullseq) || fullseq.equals(";"))
						fullseq=seq;
					return new String[]{seq, fullseq};
				}
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
		results = getSeqNamesFromXml(new MesquiteString(ID));
		if (results==null || results.length<1)
			return null;
		return results[0];
	}
	/*.................................................................................................................*/

	public String getSequenceName(String prefix, String ID) {  // long name
		String[] results = null;
		results = getSeqNamesFromXml(new MesquiteString(ID));
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
			return "Names and codes XML file unspecified.";
		String s = xmlProcessor.getParameters();
		if (StringUtil.notEmpty(s))
			s="\n"+s;
		return "Names and codes XML file: " + sampleCodeListPath + s;
	}
	/*.................................................................................................................*/
	public void echoParametersToFile(StringBuffer logBuffer) {
		loglnEchoToStringBuffer("Using names and codes XML file: " +sampleCodeListPath+"\n", logBuffer);
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
		return "Sequence Names from XML File";
	}
	/*.................................................................................................................*/

	public boolean requestPrimaryChoice() {
		return true;
	}
	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides sequence names from an XML file.";
	}

	/*.................................................................................................................*/

	public boolean isPrerelease() {
		return true;
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
				initialize(sampleCodeListPath);
			}	
			categoryChoice.removeAll();
			if (xmlProcessor.isValid()) {			
				String[] choices = xmlProcessor.getNameCategoryDescriptions();
				Debugg.println(sampleCodeListPath);
				Debugg.println(choices[3]);
				for (int i=0; i<choices.length; i++) 
					if (!StringUtil.blank(choices[i])) {
						categoryChoice.add(choices[i]);
					}
			}
			categoryChoice.repaint();
			//				categoryChoice = dialog.addPopUpMenu("Names to use:", xmlProcessor.getNameCategoryDescriptions(), tagNumber);

		}
	}

}
