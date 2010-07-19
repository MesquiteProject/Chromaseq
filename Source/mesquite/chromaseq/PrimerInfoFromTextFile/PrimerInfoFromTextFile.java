package mesquite.chromaseq.PrimerInfoFromTextFile;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.dom4j.Document;
import org.dom4j.Element;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.CharSourceCoordObed;

public class PrimerInfoFromTextFile extends PrimerInfoSource implements ActionListener {
	String primerListPath = null;
	boolean preferencesSet = false;
	SingleLineTextField primerFileField = null;
	String primerList = "";
	Parser primerListParser = null;
	Document primerDoc = null;
	boolean primersInXml = false;
	PrimerInformationFile primers = null;
	
	MesquiteMenuItemSpec chooseInfoMenu = null;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		boolean alwaysAsk = false;
		if (condition instanceof MesquiteString && condition !=null)
			alwaysAsk = "alwaysAsk".equalsIgnoreCase(((MesquiteString)condition).getValue());
		if (alwaysAsk || StringUtil.blank(primerListPath))
			if (!queryOptions())
				return false;
		if (StringUtil.notEmpty(primerListPath))
			prepareFile();
		return true;
	}

	/*.................................................................................................................*/
	public  void addXMLAttributes(Element element){
		element.addAttribute("primerListPath", primerListPath);
	}
	/*.................................................................................................................*/
	public  void addMenuItemsForPrimerSubmenu(MesquiteSubmenuSpec primerSubmenu){
		if (primerSubmenu!=null)
			chooseInfoMenu = addItemToSubmenu(null, primerSubmenu, "Choose Primer Information File ...", MesquiteModule.makeCommand("choosePrimerFile",  this));
		else
			chooseInfoMenu= addMenuItem( "Choose Primer Information File...", MesquiteModule.makeCommand("choosePrimerFile",  this));
	}

	/*.................................................................................................................*/
	public  void removeMenuItemsFromPrimerSubmenu(MesquiteSubmenuSpec primerSubmenu){
		if (chooseInfoMenu!=null){
			deleteMenuItem(chooseInfoMenu);
			chooseInfoMenu=null;
		}
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



		String s = "This file should contain, either in a tab-delimited format, information about the primers (names, sequences, forward/reverse, etc.).\n\n";
		s += "<BR>Each line should be in the following format:<br><br>\n";
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
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Allows the user to choose the primer information file", "[name of module]", commandName, "choosePrimerFile")) {
			if (!MesquiteThread.isScripting())
				queryOptions();
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
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
	
 	// returns array of all primer sequences
 	public  String[][] getPrimerSequences(){
		if (primers!=null) {
			return primers.getAllSequences();
		}
		return null;
 	}

 	// returns array of all primer sequences that correspond to the given gene fragment name (ignoring case)
 	public  String[][] getPrimerSequences(String geneFragmentName){
		if (primers!=null) {
			return primers.getAllSequences(geneFragmentName);
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
		return "Provides primer information from a tab-delimited text file.";
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




