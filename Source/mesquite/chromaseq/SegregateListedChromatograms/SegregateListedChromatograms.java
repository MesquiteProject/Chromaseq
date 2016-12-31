/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

/* initiated 
 * 8.vi.14 DRM based upon SegregateChromatograms
 */

/** this module is simple; it segregates files contained in a directory that have a sample code that matches those listed in a 
 * chosen file of sample codes.  It uses a ChromatogramFileNameParser to find the sample code within each file's name*/

package mesquite.chromaseq.SegregateListedChromatograms; 

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.chromaseq.lib.*;

/* ======================================================================== */
public class SegregateListedChromatograms extends UtilitiesAssistant implements ActionListener{ 
	//for importing sequences
	MesquiteProject proj = null;
	FileCoordinator coord = null;
	MesquiteFile file = null;
	ChromatogramFileNameParser nameParserManager;

	boolean requiresExtension=true;

	static String previousDirectory = null;
	ProgressIndicator progIndicator = null;
	int sequenceCount = 0;
	String importedDirectoryPath, importedDirectoryName;
	StringBuffer logBuffer;

	String sampleCodeListPath = null;
	String sampleCodeListFile = null;
	String sampleCodeList = "";
	Parser sampleCodeListParser = null;
	
	String segregatedFolderName = "SampleCodeInList";

	boolean copyFile = false;


	boolean preferencesSet = false;
	boolean verbose=true;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e1 = registerEmployeeNeed(ChromatogramFileNameParser.class, "Chromatogram processing requires a means to determine the sample code.", "This is activated automatically.");
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();

		addMenuItem(null, "Segregate Chromatograms of Listed Codes...", makeCommand("extract", this));
		return true;
	}
	/*.................................................................................................................*/
	public boolean hireRequired(){

		if (nameParserManager == null)
			nameParserManager= (ChromatogramFileNameParser)MesquiteTrunk.mesquiteTrunk.hireEmployee(ChromatogramFileNameParser.class, "Supplier of sample code from the chromatogram file name.");
		if (nameParserManager == null) {
			return false;
		} else if (!nameParserManager.queryOptions())
			return false;

		return true;
	}
	/*.................................................................................................................*/
	boolean makeDirectoriesForMatch(String matchStringPath){

		File newDir = new File(matchStringPath);
		try { newDir.mkdir();    //make folder for this match			
		}
		catch (SecurityException e) { 
			logln("Couldn't make directory.");
			return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	public boolean processCodesFile() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				sampleCodeListParser = new Parser(sampleCodeList);
				return true;
			}
		}	
		return false;

	}
	/*.................................................................................................................*/

	public boolean sampleCodeIsInCodesFile(MesquiteString sampleCode) {
		if (sampleCodeListParser==null)
			return false;
		if (sampleCode==null || sampleCode.isBlank())
			return false;
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
				return true;
			}
			line = sampleCodeListParser.getRawNextDarkLine();
		}
		return false;
	}

	/*.................................................................................................................*/
	public boolean extractChromatograms(String directoryPath, File directory){

		logBuffer.setLength(0);
		String[] files = directory.list();
		progIndicator = new ProgressIndicator(getProject(),"Segregating Chromatograms", files.length);
		progIndicator.setStopButtonName("Stop");
		progIndicator.start();
		boolean abort = false;
		String cPath;
		String seqName;
		String fullSeqName;
		String fragName = "";
		sequenceCount = 0;


		int loc = 0;


		String processedDirPath = "";
		if (StringUtil.notEmpty(segregatedFolderName))
			processedDirPath =directoryPath + MesquiteFile.fileSeparator + segregatedFolderName;
		else
			processedDirPath = directoryPath + MesquiteFile.fileSeparator + "SampleCodeInList";

		loglnEchoToStringBuffer(" Searching for chromatograms that match the specified criteria. ", logBuffer);
		loglnEchoToStringBuffer(" Processing directory: ", logBuffer);
		loglnEchoToStringBuffer("  "+directoryPath+"\n", logBuffer);

		File newDir;
		int numPrepared = 0;

		newDir = new File(processedDirPath);

		try { 
			newDir.mkdir();
		}
		catch (SecurityException e) {
			logln("Couldn't make directory.");
			if (progIndicator!=null) progIndicator.goAway();
			return false;
		}


		for (int i=0; i<files.length; i++) {
			if (progIndicator!=null){
				progIndicator.setCurrentValue(i);
				progIndicator.setText("Number of files segregated: " + numPrepared);
				if (progIndicator.isAborted())
					abort = true;
			}
			if (abort)
				break;
			fragName = "";
			if (files[i]==null )
				;
			else {
				cPath = directoryPath + MesquiteFile.fileSeparator + files[i];
				File cFile = new File(cPath);
				if (cFile.exists() && !cFile.isDirectory() && (!files[i].startsWith(".")) && (!requiresExtension || (files[i].endsWith("ab1") ||  files[i].endsWith(".abi")  || files[i].endsWith(".ab")  ||  files[i].endsWith(".CRO") || files[i].endsWith(".scf")))) {

					String chromFileName = cFile.getName();
					if (StringUtil.blank(chromFileName)) {
						loglnEchoToStringBuffer("Bad file name; it is blank.", logBuffer);
						// remove "running"
						if (progIndicator!=null) progIndicator.goAway();
						return false;
					}

					MesquiteString sampleCodeSuffix = new MesquiteString();
					MesquiteString sampleCode = new MesquiteString();
					MesquiteString primerName = new MesquiteString();
					MesquiteString startTokenResult = new MesquiteString();
					//here's where the names parser processes the name

					if (nameParserManager!=null) {
						if (!nameParserManager.parseFileName(chromFileName, sampleCode, sampleCodeSuffix, primerName, logBuffer, startTokenResult, null))
							continue;
					}
					else {
						loglnEchoToStringBuffer("Naming parsing rule is absent.", logBuffer);
						return false;
					}
					if (startTokenResult.getValue() == null)
						startTokenResult.setValue("");

					boolean match = sampleCodeIsInCodesFile(sampleCode);



					if (match) {
						if (verbose)
							loglnEchoToStringBuffer(chromFileName, logBuffer);
						numPrepared++;
						if (!makeDirectoriesForMatch(processedDirPath)){   //make directories for this in case it doesn't already exist
							if (progIndicator!=null) progIndicator.goAway();
							return false;
						}
						try {
							String newFileName = chromFileName;
							String newFilePath = processedDirPath + MesquiteFile.fileSeparator + chromFileName;					
							File newFile = new File(newFilePath); //
							int count=1;
							while (newFile.exists()) {
								newFileName = ""+count + "." + chromFileName;
								newFilePath = processedDirPath + MesquiteFile.fileSeparator + newFileName;
								newFile = new File(newFilePath);
								count++;
							}
							if (copyFile){
								try {
									MesquiteFile.copy(cFile, newFile);
								}
								catch (IOException e) {
									logln( "Can't copy: " + chromFileName);
								}
							}
							else
								cFile.renameTo(newFile); 
						}
						catch (SecurityException e) {
							logln( "Can't rename: " + chromFileName);
						}

					} 


				}
			}
		}

		loglnEchoToStringBuffer("Number of files examined: " + files.length, logBuffer);
		loglnEchoToStringBuffer("Number of files found and segregated: " + numPrepared, logBuffer);



		if (!abort) {

			progIndicator.spin();
		}


		if (progIndicator!=null)
			progIndicator.goAway();

		return true;

	}
	/*.................................................................................................................*/
	public boolean extractChromatograms(String directoryPath){
		if ( logBuffer==null)
			logBuffer = new StringBuffer();

		loglnEchoToStringBuffer("Segregating chromatograms with codes present in text file: " + sampleCodeListFile, logBuffer);


		MesquiteBoolean pleaseStorePrefs = new MesquiteBoolean(false);

		if (pleaseStorePrefs.getValue())
			storePreferences();

		// if not passed-in, then ask
		if (StringUtil.blank(directoryPath)) {
			directoryPath = MesquiteFile.chooseDirectory("Choose directory containing chromatograms:", previousDirectory); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		}

		if (StringUtil.blank(directoryPath))
			return false;


		copyFile = QueryDialogs.queryTwoRadioButtons(containerOfModule(), "Move or Copy files", "", null, "Move files", "Copy files")==1;

		File directory = new File(directoryPath);
		importedDirectoryPath = directoryPath + MesquiteFile.fileSeparator;
		importedDirectoryName = directory.getName();
		previousDirectory = directory.getParent();
		storePreferences();
		if (directory.exists() && directory.isDirectory()) {
			return extractChromatograms(directoryPath, directory);
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("requiresExtension".equalsIgnoreCase(tag))
			requiresExtension = MesquiteBoolean.fromTrueFalseString(content);
		else if ("previousDirectory".equalsIgnoreCase(tag))
			previousDirectory = StringUtil.cleanXMLEscapeCharacters(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "requiresExtension", requiresExtension);  
		StringUtil.appendXMLTag(buffer, 2, "previousDirectory", previousDirectory);  
		preferencesSet = true;
		return buffer.toString();
	}


	JLabel nameParserLabel = null;

	MesquiteTextCanvas nameParserTextCanvas = null;
	Button nameParserButton = null;


	/*.................................................................................................................*/
	private String getModuleText(MesquiteModule mod) {
		return mod.getName() + "\n" + mod.getParameters();
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Segregate Chromatograms Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		TextCanvasWithButtons textCanvasWithButtons;

		//section for name parser

		dialog.addHorizontalLine(1);
		dialog.addLabel("Chromatogram File Name Parser");
		dialog.forceNewPanel();
		String s = getModuleText(nameParserManager);
		if (MesquiteTrunk.mesquiteTrunk.numModulesAvailable(ChromatogramFileNameParser.class)>1){
			textCanvasWithButtons = dialog.addATextCanvasWithButtons(s,"File Name Parser...", "nameParserReplace", "Options...", "nameParserButton",this);
			nameParserButton = textCanvasWithButtons.getButton2();
		}
		else {
			textCanvasWithButtons = dialog.addATextCanvasWithButtons(s, "Options...", "nameParserButton",this);
			nameParserButton = textCanvasWithButtons.getButton();
		}
		nameParserButton.setEnabled (nameParserManager.hasOptions());
		nameParserTextCanvas = textCanvasWithButtons.getTextCanvas();
		SingleLineTextField segregatedFolderNameField = dialog.addTextField("Name of folder into which files will be segregated", segregatedFolderName, 40);


		dialog.setDefaultButton("Segregate");

		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);
		dialog.addHorizontalLine(2);


		s = "This will move all chromatograms whose sample codes are listed in a chosen file into their own folder.\n";
		s+="Mesquite extracts from within the name of each chromatogram file a code indicating the sample (e.g., a voucher number). ";
		s+= "It then looks at the first entry on each line of a tab-delimited text file, and sees if it can find in that sample codes file ";
		s+= "the sample code in the chromatogram's file name.  If so, it will move the file into a folder; if not, it will ignore the chromatogram file.";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			requiresExtension = requiresExtensionBox.getState();
			segregatedFolderName = segregatedFolderNameField.getText();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Segregates into a new folder all chromatograms whose file names contain sample codes that are listed in a text file.", null, commandName, "extract")) {

			if (!hireRequired())
				return null;

			if (queryOptions()) {
				MesquiteString dnaNumberListDir = new MesquiteString();
				MesquiteString dnaNumberListFile = new MesquiteString();
				String s = MesquiteFile.openFileDialog("Choose file containing sample codes", dnaNumberListDir, dnaNumberListFile);
				if (!StringUtil.blank(s)) {
					sampleCodeListPath = s;
					sampleCodeListFile = dnaNumberListFile.getValue();
					processCodesFile();
					extractChromatograms(null);

				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("nameParserButton")) {
			if (nameParserManager!=null) {
				if (nameParserManager.queryOptions() && nameParserTextCanvas!=null)
					nameParserTextCanvas.setText(getModuleText(nameParserManager));
			}
		}
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Segregate Chromatograms in Listed Codes";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Segregates into a new folder all chromatograms whose sample codes are listed in a specified file.";
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -1110;  
	}

}





