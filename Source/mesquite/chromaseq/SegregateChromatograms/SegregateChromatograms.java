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

package mesquite.chromaseq.SegregateChromatograms; 

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JLabel;


import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.chromaseq.lib.*;

/* ======================================================================== */
public class SegregateChromatograms extends UtilitiesAssistant implements ActionListener{ 
	//for importing sequences
	MesquiteProject proj = null;
	FileCoordinator coord = null;
	MesquiteFile file = null;
	ChromatogramFileNameParser nameParserManager;

	boolean requiresExtension=true;
	boolean translateSampleCodes = true;
	
	static String previousDirectory = null;
	ProgressIndicator progIndicator = null;
	int sequenceCount = 0;
	String importedDirectoryPath, importedDirectoryName;
	StringBuffer logBuffer;
	final String processedFolder = "processed";
	final String sequencesFolder = "sequences";
	final String processedFastaFolder = "processedFasta";
	String processedFastaDirectory = null;
	String sampleNameToMatch = null;
	String geneNameToMatch = null;

	boolean preferencesSet = false;
	boolean verbose=false;
	SequenceNameSource sequenceNameTask = null;
	PrimerInfoSource primerInfoTask = null;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e1 = registerEmployeeNeed(SequenceNameSource.class, "Chromatogram processing requires a source of sequence names; choose the one that appropriately determines the sequence names from the sample codes.", "This is activated automatically.");
		EmployeeNeed e2 = registerEmployeeNeed(PrimerInfoSource.class, "Chromatogram processing requires a source of information about primers, including their names, direction, and sequence, as well as the gene fragments to which they correspond.", "This is activated automatically.");
		EmployeeNeed e3 = registerEmployeeNeed(ChromatogramFileNameParser.class, "Chromatogram processing requires a means to determine the sample code and primer name.", "This is activated automatically.");
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		loadPreferences();
		

		addMenuItem(null, "Segregate Chromatograms...", makeCommand("extract", this));
		return true;
	}
	/*.................................................................................................................*/
	public boolean hireRequired(){

		if (nameParserManager == null)
			nameParserManager= (ChromatogramFileNameParser)MesquiteTrunk.mesquiteTrunk.hireEmployee(ChromatogramFileNameParser.class, "Supplier of sample code and primer name from the chromatogram file name.");
		if (nameParserManager == null) {
			return false;
		} else if (!nameParserManager.queryOptions())
				return false;

		sequenceNameTask = (SequenceNameSource)hireEmployee(SequenceNameSource.class,  "Supplier of sequence names from sample codes");
		if (sequenceNameTask==null) 
			return false;
		sequenceNameTask.initialize();
		
		primerInfoTask = (PrimerInfoSource)hireCompatibleEmployee(PrimerInfoSource.class,  new MesquiteString("alwaysAsk"), "Supplier of information about primers and gene fragments");
		if (primerInfoTask==null) 
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
	public boolean extractChromatograms(String directoryPath){
		if ( logBuffer==null)
			logBuffer = new StringBuffer();
		
		if (StringUtil.blank(sampleNameToMatch)&& StringUtil.blank(geneNameToMatch))
			return false;

		if (StringUtil.blank(sampleNameToMatch))
			loglnEchoToStringBuffer("Segregating chromatograms with the following text in the sample name: " + sampleNameToMatch, logBuffer);

		if (StringUtil.blank(geneNameToMatch))
			loglnEchoToStringBuffer("Segregating chromatograms with the following text in the gene name: " + geneNameToMatch, logBuffer);


		MesquiteBoolean pleaseStorePrefs = new MesquiteBoolean(false);

		if (pleaseStorePrefs.getValue())
			storePreferences();

		// if not passed-in, then ask
		if (StringUtil.blank(directoryPath)) {
			directoryPath = MesquiteFile.chooseDirectory("Choose directory containing ABI files:", previousDirectory); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		}

		if (StringUtil.blank(directoryPath))
			return false;

		File directory = new File(directoryPath);
		importedDirectoryPath = directoryPath + MesquiteFile.fileSeparator;
		importedDirectoryName = directory.getName();
		previousDirectory = directory.getParent();
		storePreferences();
		if (directory.exists() && directory.isDirectory()) {
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

			
			String processedDirPath = directoryPath + MesquiteFile.fileSeparator;
			if (StringUtil.notEmpty(sampleNameToMatch) && StringUtil.notEmpty(geneNameToMatch))  // if both not empty have to match both
				processedDirPath += sampleNameToMatch+" "+geneNameToMatch;
			else if (StringUtil.notEmpty(sampleNameToMatch))
				processedDirPath += sampleNameToMatch;
			else if (StringUtil.notEmpty(geneNameToMatch))
				processedDirPath += geneNameToMatch;
			else
				return false;

			loglnEchoToStringBuffer(" Searching for chromatograms that match the specified criteria. ", logBuffer);
			loglnEchoToStringBuffer(" Processing directory: ", logBuffer);
			loglnEchoToStringBuffer("  "+directoryPath+"\n", logBuffer);
			if (sequenceNameTask!=null)
				sequenceNameTask.echoParametersToFile(logBuffer);
			if (primerInfoTask!=null)
				primerInfoTask.echoParametersToFile(logBuffer);

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


			String lowerCaseSampleNameToMatch=null;
			if (StringUtil.notEmpty(sampleNameToMatch))
				lowerCaseSampleNameToMatch= sampleNameToMatch.toLowerCase();
			String lowerCaseFragmentNameToMatch=null;
			if (StringUtil.notEmpty(geneNameToMatch))
				lowerCaseFragmentNameToMatch= geneNameToMatch.toLowerCase();

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

						if (primerInfoTask != null){
							fragName = primerInfoTask.getGeneFragmentName(primerName.getValue());
						}
						if (!StringUtil.blank(sampleCode.getValue())) {
							/* Translate code number to sample name if requested  */
							 if (sequenceNameTask!=null && sequenceNameTask.isReady()) {
								seqName = sequenceNameTask.getAlternativeName(startTokenResult.getValue(), sampleCode.getValue());
								fullSeqName = sequenceNameTask.getSequenceName(startTokenResult.getValue(), sampleCode.getValue());
								}
							else {
								seqName = sampleCode.getValue();
								fullSeqName = sampleCode.getValue();
							}
						}
						else {
							seqName = chromFileName.substring(1, 10); // change!
							fullSeqName = seqName;
						}

						seqName = StringUtil.cleanseStringOfFancyChars(seqName + sampleCodeSuffix.getValue());  // tack on suffix
						fullSeqName = StringUtil.cleanseStringOfFancyChars(fullSeqName + sampleCodeSuffix.getValue());


						//progIndicator.spin();
						
						boolean matchesSampleName = (StringUtil.notEmpty(lowerCaseSampleNameToMatch) && (seqName.toLowerCase().indexOf(lowerCaseSampleNameToMatch)>=0 || fullSeqName.toLowerCase().indexOf(lowerCaseSampleNameToMatch)>=0));
						boolean matchesFragmentName = (StringUtil.notEmpty(lowerCaseFragmentNameToMatch) && (fragName.toLowerCase().indexOf(lowerCaseFragmentNameToMatch)>=0));

						boolean match = false;
						if (StringUtil.notEmpty(lowerCaseSampleNameToMatch) && StringUtil.notEmpty(lowerCaseFragmentNameToMatch))  // if both not empty have to match both
							match = matchesSampleName && matchesFragmentName;
						else
							match = matchesSampleName || matchesFragmentName;  //otherwise just have to match one of them.
						if (match) {
							if (verbose)
								loglnEchoToStringBuffer(chromFileName + " ["+fullSeqName + "   " + fragName+"]", logBuffer);
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
								cFile.renameTo(newFile); 
							}
							catch (SecurityException e) {
								logln( "Can't rename: " + seqName);
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
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
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
		else if ("translateSampleCodes".equalsIgnoreCase(tag))
			translateSampleCodes = MesquiteBoolean.fromTrueFalseString(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "requiresExtension", requiresExtension);  
		StringUtil.appendXMLTag(buffer, 2, "translateSampleCodes", translateSampleCodes);  
		StringUtil.appendXMLTag(buffer, 2, "previousDirectory", previousDirectory);  
		preferencesSet = true;
		return buffer.toString();
	}
	
	
	JLabel nameParserLabel = null;
	JLabel sequenceNameTaskLabel = null;
	JLabel primerInfoTaskLabel = null;
	
	MesquiteTextCanvas nameParserTextCanvas = null;
	MesquiteTextCanvas sequenceNameTaskTextCanvas = null;
	MesquiteTextCanvas primerInfoTaskTextCanvas = null;
	Button sequenceNameTaskButton = null;
	Button nameParserButton = null;
	Button primerInfoTaskButton = null;

	
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
				
		//section for SequenceNameSource
				dialog.addHorizontalLine(1);
				dialog.addLabel("Source of Sequence Names");
				dialog.forceNewPanel();
				s = getModuleText(sequenceNameTask);

				if (MesquiteTrunk.mesquiteTrunk.numModulesAvailable(SequenceNameSource.class)>1){
					textCanvasWithButtons = dialog.addATextCanvasWithButtons(s,"Sequence Name Source...", "sequenceNameTaskReplace", "Options...", "sequenceNameTaskButton",this);
					sequenceNameTaskButton = textCanvasWithButtons.getButton2();
				}
				else {
					textCanvasWithButtons = dialog.addATextCanvasWithButtons(s, "Options...", "sequenceNameTaskButton",this);
					sequenceNameTaskButton = textCanvasWithButtons.getButton();
				}
				sequenceNameTaskButton.setEnabled (sequenceNameTask.hasOptions());
				sequenceNameTaskTextCanvas = textCanvasWithButtons.getTextCanvas();

		//section for PrimerInfoSource
				dialog.addHorizontalLine(1);
				dialog.addLabel("Source of Primer Information");
				dialog.forceNewPanel();
				s = getModuleText(primerInfoTask);
				if (MesquiteTrunk.mesquiteTrunk.numModulesAvailable(PrimerInfoSource.class)>1){
					textCanvasWithButtons = dialog.addATextCanvasWithButtons(s,"Primer Info Source...", "primerInfoTaskReplace", "Options...", "primerInfoTaskButton",this);
					primerInfoTaskButton = textCanvasWithButtons.getButton2();
				}
				else {
					textCanvasWithButtons = dialog.addATextCanvasWithButtons(s, "Options...", "primerInfoTaskButton",this);
					primerInfoTaskButton = textCanvasWithButtons.getButton();
				}
				nameParserButton.setEnabled (primerInfoTask.hasOptions());
				primerInfoTaskTextCanvas = textCanvasWithButtons.getTextCanvas();

				dialog.setDefaultButton("Segregate");

		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);
		dialog.addHorizontalLine(2);

		SingleLineTextField sampleNameToMatchField = dialog.addTextField("Text to match in long sequence name", sampleNameToMatch,26);
		SingleLineTextField geneNameToMatchField = dialog.addTextField("Text to match in gene fragment name", geneNameToMatch,26);


		s = "This will move all chromatograms whose long sequence names and gene fragment names contain the specified text into their own folder.\n";
		s+="Mesquite extracts from within the name of each chromatogram file for both a code indicating the sample (e.g., a voucher number) and the primer name. ";
		s+= "To allow this, you must first define an rule that defines how the chromatogram file names are structured.\n\n";
		s+= "If you so choose, Mesquite will search for the sample code within a sample names file you select, on each line of which is:\n";
		s+= "   <code><tab><short sequence name><tab><long sequence name>;\n";
		s+= "where the code, short sequence name, and long sequence name are all single tokens (you can force a multi-word name to be a single token by surrounding the name with single quotes). ";
		s+= "For Segregating Chromosomes, only the long sequence name is used.   If you wish, it can contain many taxa names, e.g. \"Insecta Coleoptera Carabidae Trechinae Bembidiini Bembidion (Pseudoperyphus) louisella\".\n\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			requiresExtension = requiresExtensionBox.getState();
			sampleNameToMatch = sampleNameToMatchField.getText();
			geneNameToMatch = geneNameToMatchField.getText();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Segregates into a new folder all chromatograms whose sequence names and gene fragment names contain a particular string.", null, commandName, "extract")) {

			if (!hireRequired())
				return null;
			
			if (queryOptions()) {
				extractChromatograms(null);
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
		else if (e.getActionCommand().equalsIgnoreCase("sequenceNameTaskButton")) {
			if (sequenceNameTask!=null) {
				if (sequenceNameTask.queryOptions() && sequenceNameTaskTextCanvas!=null)
					sequenceNameTaskTextCanvas.setText(getModuleText(sequenceNameTask));
			}
		}
		else if (e.getActionCommand().equalsIgnoreCase("primerInfoTaskButton")) {
			if (primerInfoTask!=null) {
				if (primerInfoTask.queryOptions() && primerInfoTaskTextCanvas!=null)
					primerInfoTaskTextCanvas.setText(getModuleText(primerInfoTask));
			}
		}
		else if (e.getActionCommand().equalsIgnoreCase("sequenceNameTaskReplace")) {
			MesquiteCommand command = new MesquiteCommand("setSequenceNameSource", this);
			command.doItMainThread(null, null, false, false);
		}
		else if (e.getActionCommand().equalsIgnoreCase("primerInfoTaskReplace")) {
			MesquiteCommand command = new MesquiteCommand("setPrimerInfoSource", this);
			command.doItMainThread(null, null, false, false);
		}
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Segregate Chromatograms";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Segregates into a new folder all chromatograms whose sequence names and gene fragment names contain a particular string.";
	}
	/*.................................................................................................................*/

}





