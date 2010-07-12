/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.
Version 0.980   July 2010
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.ExtractChromatograms; 

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.dom4j.Document;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.chromaseq.lib.*;

/* ======================================================================== */
public class ExtractChromatograms extends UtilitiesAssistant{ 
	//for importing sequences
	MesquiteProject proj = null;
	FileCoordinator coord = null;
	MesquiteFile file = null;
	ChromatogramFileNameParser nameParserManager;

	String fileExtension = ".ab1";
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
		
		primerInfoTask = (PrimerInfoSource)hireEmployee(PrimerInfoSource.class,  "Supplier of information about primers and gene fragments");
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

			echoStringToFile(" Searching for chromatograms that match the specified criteria. ", logBuffer);
			echoStringToFile(" Processing directory: ", logBuffer);
			echoStringToFile("  "+directoryPath+"\n", logBuffer);
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
							echoStringToFile("Bad file name; it is blank.", logBuffer);
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
							if (!nameParserManager.parseFileName(chromFileName, sampleCode, sampleCodeSuffix, primerName, logBuffer, startTokenResult))
								continue;
						}
						else {
							echoStringToFile("Naming parsing rule is absent.", logBuffer);
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
								echoStringToFile(chromFileName + " ["+fullSeqName + "   " + fragName+"]", logBuffer);
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

			echoStringToFile("Number of files found and segregated: " + numPrepared, logBuffer);



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
		else if ("fileExtension".equalsIgnoreCase(tag))
			fileExtension = StringUtil.cleanXMLEscapeCharacters(content);
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
		StringUtil.appendXMLTag(buffer, 2, "fileExtension", fileExtension);  
		preferencesSet = true;
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Segregate Chromatograms Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()


		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);
		SingleLineTextField fileExtensionField = dialog.addTextField("file extension for chromatogram copies:", fileExtension, 8, true);
		dialog.addHorizontalLine(2);

		SingleLineTextField sampleNameToMatchField = dialog.addTextField("String to match in sample name", sampleNameToMatch,26);
		SingleLineTextField geneNameToMatchField = dialog.addTextField("String to match in gene fragment name", geneNameToMatch,26);


		String s = "This will move all chromatograms whose sample names and gene fragment names contain the specified strings into their own folder.\n";
		s+="Mesquite searches within the name of each chromatogram file for both a code indicating the sample (e.g., a voucher number) and the primer name. ";
		s+= "To allow this, you must first define an rule that defines how the chromatogram file names are structured.\n\n";
		s+= "If you so choose, Mesquite will search for the sample code within a sample names file you select, on each line of which is:\n";
		s+= "   <code><tab><short sample name><tab><long sample name>;\n";
		s+= "where the code, short sample name, and long sample name are all single tokens (you can force a multi-word name to be a single token by surrounding the name with single quotes). ";
		s+= "The short sample name is for the file names, and must be <27 characters; the long sample name is the name you wish to have within the FASTA file.\n\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			fileExtension = fileExtensionField.getText();
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
		if (checker.compare(this.getClass(), "Segregates into a new folder all chromatograms whose sample names and gene fragment names contain a particular string.", null, commandName, "extract")) {

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
	public String getName() {
		return "Segregate Chromatograms";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Segregates into a new folder all chromatograms whose sample names and gene fragment names contain a particular string.";
	}
	/*.................................................................................................................*/

}





