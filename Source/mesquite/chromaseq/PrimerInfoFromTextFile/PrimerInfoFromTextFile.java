package mesquite.chromaseq.PrimerInfoFromTextFile;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dom4j.Document;
import org.dom4j.Element;

import mesquite.chromaseq.SampleAndPrimerFileNameParser.ChromFileNameDialog;
import mesquite.chromaseq.lib.PrimerInfoSource;
import mesquite.chromaseq.lib.PrimerList;
import mesquite.lib.ExtensibleDialog;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Parser;
import mesquite.lib.SingleLineTextField;
import mesquite.lib.StringUtil;

public class PrimerInfoFromTextFile extends PrimerInfoSource implements ActionListener {
	String primerListPath = null;
	boolean preferencesSet = false;
	SingleLineTextField primerFileField = null;
	String primerList = "";
	Parser primerListParser = null;
	Document primerDoc = null;
	boolean primersInXml = false;
	PrimerInformationFile primers = null;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return queryOptions();
	}

	/*.................................................................................................................*/
	public  void addXMLAttributes(Element element){
		element.addAttribute("primerListPath", primerListPath);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		 if ("primerListPath".equalsIgnoreCase(tag))
			 primerListPath = StringUtil.cleanXMLEscapeCharacters(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "primerListPath", primerListPath);  
		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		if (MesquiteThread.isScripting())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Location of File with Primer Information",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		primerFileField = dialog.addTextField("Primer information file:", primerListPath,26);
		final Button fileBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		fileBrowseButton.setActionCommand("browse");



		String s = "This file should contain, either in a tab-delimited format or in XML, information about the primers (names, sequences, forward/reverse, etc.).\n\n";
		s += "<BR>If it is a tab-delimited file, each line should be in the following format:<br><br>\n";
		s += "  &lt;primer name&gt;&lt;tab&gt;&lt;gene fragment name&gt;&lt;tab&gt;&lt;F for forward primers or R for reverse&gt;&lt;tab&gt;&lt;primer sequence&gt;&lt;tab&gt;;<br><br>\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			primerListPath = primerFileField.getText();
			prepareFile();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	/*.................................................................................................................*/
	public void echoParametersToFile(StringBuffer logBuffer) {
		echoStringToFile("Using primers file: " + primerListPath+"\n", logBuffer);
	}

	/*.................................................................................................................*/
	private void prepareFile() {
		if (!StringUtil.blank(primerListPath)) {
			primerList = MesquiteFile.getFileContentsAsString(primerListPath);

			if (!StringUtil.blank(primerList)) {
				 primers = new PrimerInformationFile(primerList);
			}
		}			
		
	}

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

	/*.................................................................................................................*/
	public boolean isReady() {
		return primerListPath!=null && primers != null;
	}

	/*.................................................................................................................*/

	public boolean requestPrimaryChoice() {
		return true;
	}

	/*.................................................................................................................*/

	public String getName() {
		return "Primer Information from Text File";
	}
	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides primer information from a text file (either tab-delimited or XML).";
	}

	/*.................................................................................................................*/

	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		 if (e.getActionCommand().equalsIgnoreCase("browse")) {
			MesquiteString dnaNumberListDir = new MesquiteString();
			MesquiteString dnaNumberListFile = new MesquiteString();
			String s = MesquiteFile.openFileDialog("Choose file containing primer information", dnaNumberListDir, dnaNumberListFile);
			if (!StringUtil.blank(s)) {
				primerListPath = s;
				if (primerFileField!=null) 
					primerFileField.setText(primerListPath);
			}
		}
	}

}




