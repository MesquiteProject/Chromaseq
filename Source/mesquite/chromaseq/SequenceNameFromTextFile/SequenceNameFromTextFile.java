package mesquite.chromaseq.SequenceNameFromTextFile;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.chromaseq.lib.SequenceNameSource;
import mesquite.lib.*;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

public class SequenceNameFromTextFile extends SequenceNameSource implements ActionListener {
	String sampleCodeListPath = null;
	String sampleCodeList = "";
	Parser sampleCodeListParser = null;
//	Document namesDoc = null;
//	boolean namesInXml = false;
	boolean preferencesSet = false;
	SingleLineTextField dnaCodesField = null;

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!optionsSpecified()){
			if (MesquiteThread.isScripting() && StringUtil.blank(sampleCodeListPath))
				return false;
			if (!queryOptions())
				return false;
		}
		return true;
	}


	/*.................................................................................................................*/
	public boolean optionsSpecified(){
		return StringUtil.notEmpty(sampleCodeListPath);
	}


	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		 if ("sampleCodeListPath".equalsIgnoreCase(tag))
			sampleCodeListPath = StringUtil.cleanXMLEscapeCharacters(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "sampleCodeListPath", sampleCodeListPath);  
		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Location of File with Sequence Names",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		dnaCodesField = dialog.addTextField("Sequence names file:", sampleCodeListPath,26);
		final Button dnaCodesBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		dnaCodesBrowseButton.setActionCommand("DNANumbersBrowse");

		String s = "This file should contain,  in a tab delimited format, the names to be used for the sequences, and the sample codes to which each corresponds.\n\n";
		s+= "<BR>Each line should look like this:<br><br>\n";
		s+= "   &lt;code&gt;&lt;tab&gt;&lt;short sample name&gt;&lt;tab&gt;&lt;long sample name&gt;;<br><br>\n";
		s+= "where the code, short sample name, and long sample name are all single tokens (do NOT surround the name with quotes). ";
		s+= "The short sample name is for the file names, and must be <27 characters; the long sample name is the name you wish to have within the FASTA file.\n\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			sampleCodeListPath = dnaCodesField.getText();
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
	public void initialize() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);
			if (!StringUtil.blank(sampleCodeList)) {
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
	/*.................................................................................................................*/

	public  String[] getSeqNamesFromTabDelimitedFile(MesquiteString sampleCode) {
		if (sampleCodeListParser==null)
			return null;
		String sampleCodeString  = sampleCode.getValue();
		sampleCodeListParser.setPosition(0);
		Parser subParser = new Parser();
		String line = sampleCodeListParser.getRawNextDarkLine();

		while (StringUtil.notEmpty(line)) {
			if (line.indexOf("\t")>=0){  //Debugg.println(Why is this using NEXUS tokenization rules? e.g. [] are stripped
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
		return "Names and codes file: " + sampleCodeListPath;
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
		return "Sequence Names from Text File (version 1.0 style)";
	}
	/*.................................................................................................................*/

	public boolean requestPrimaryChoice() {
		return true;
	}
	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides sequence names from a text file.";
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
				if (dnaCodesField!=null) 
					dnaCodesField.setText(sampleCodeListPath);
			}
		}
	}

}
