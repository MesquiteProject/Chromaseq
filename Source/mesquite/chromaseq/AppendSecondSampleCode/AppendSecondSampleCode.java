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

package mesquite.chromaseq.AppendSecondSampleCode;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteTextCanvas;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lib.ui.SingleLineTextField;
import mesquite.lib.ui.TextCanvasWithButtons;

public class AppendSecondSampleCode extends UtilitiesAssistant implements ActionListener {
	static ChromatogramFileNameParser nameParserManager;  //ZQ static issue
	String importedDirectoryPath, importedDirectoryName;
	ProgressIndicator progIndicator = null;
	static String previousDirectory = null;
	String translationFilePath = null;
	boolean preferencesSet = false;
	SampleToSampleTranslationFile translationFile = null;


	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e3 = registerEmployeeNeed(ChromatogramFileNameParser.class, "File renaming requires a means to determine the sample code.", "This is activated automatically.");
	}

	public boolean startJob(String arguments, Object condition,  boolean hiredByName) {
		loadPreferences();
		addMenuItem(null, "Rename Files PCR to Sample Code...", makeCommand("rename", this));
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
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Rename Files PCR to Sample Code", null, commandName, "rename")) {
			if (!hireRequired())
				return null;

			if (queryOptions())
				rename(null);
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("translationFilePath".equalsIgnoreCase(tag))
			translationFilePath = StringUtil.cleanXMLEscapeCharacters(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "translationFilePath", translationFilePath);  
		preferencesSet = true;
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	JLabel nameParserLabel = null;

	MesquiteTextCanvas nameParserTextCanvas = null;
	Button nameParserButton = null;
	SingleLineTextField translationFilePathField =  null;


	/*.................................................................................................................*/
	private String getModuleText(MesquiteModule mod) {
		return mod.getName() + "\n" + mod.getParameters();
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Renaming Chromatogram Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		TextCanvasWithButtons textCanvasWithButtons;

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


		translationFilePathField = dialog.addTextField("Translation File Path:", translationFilePath, 40);
		Button phredBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		phredBrowseButton.setActionCommand("transFileBrowse");


		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			translationFilePath = translationFilePathField.getText();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("nameParserButton")) {
			if (nameParserManager!=null) {
				if (nameParserManager.queryOptions() && nameParserTextCanvas!=null)
					nameParserTextCanvas.setText(getModuleText(nameParserManager));
			}
		}
		else if (e.getActionCommand().equalsIgnoreCase("transFileBrowse")) {
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			translationFilePath = MesquiteFile.openFileDialog("Choose translation file, directoryName, fileName", directoryName, fileName);
			if (!StringUtil.blank(translationFilePath)) {
				translationFilePathField.setText(translationFilePath);
			}
		}
	}


	/*.................................................................................................................*/
	public boolean rename(String directoryPath){
		MesquiteBoolean pleaseStorePrefs = new MesquiteBoolean(false);

		if (pleaseStorePrefs.getValue())
			storePreferences();

		if (StringUtil.blank(directoryPath)) {
			directoryPath = MesquiteFile.chooseDirectory("Choose directory containing ABI files:", previousDirectory); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
		}

		if (StringUtil.blank(directoryPath) || StringUtil.blank(translationFilePath))
			return false;

		String translationContents = MesquiteFile.getFileContentsAsString(translationFilePath);
		if (StringUtil.blank(translationContents))
			return false;

		if (translationFile==null)
			translationFile = new SampleToSampleTranslationFile(translationContents);

		ProgressIndicator progIndicator;
		StringBuffer logBuffer = new StringBuffer();

		File directory = new File(directoryPath);
		importedDirectoryPath = directoryPath + MesquiteFile.fileSeparator;
		importedDirectoryName = directory.getName();
		previousDirectory = directory.getParent();
		storePreferences();
		if (directory.exists() && directory.isDirectory()) {
			String[] files = directory.list();
			progIndicator = new ProgressIndicator(getProject(),"Renaming files", files.length);
			progIndicator.setStopButtonName("Stop");
			progIndicator.start();
			boolean abort = false;
			String cPath;


			int loc = 0;


			String processedDirPath = directoryPath + MesquiteFile.fileSeparator +"renamed" + MesquiteFile.fileSeparator;
			String unprocessedDirPath = directoryPath + MesquiteFile.fileSeparator +"noTranslationFound" + MesquiteFile.fileSeparator;

			int numPrepared = 0;
			boolean alreadyWarned = false;

			File newDir = new File(processedDirPath);
			File newUnprocessedDir = new File(unprocessedDirPath);

			try { 
				newDir.mkdir();
				newUnprocessedDir.mkdir();
			}
			catch (SecurityException e) {
				logln("Couldn't make directory.");
				if (progIndicator!=null) progIndicator.goAway();
				return false;
			}

			String lowerCaseSampleNameToMatch=null;
			String newSampleCode = "";
			int renamed=0;

			for (int i=0; i<files.length; i++) {
				if (progIndicator!=null){
					progIndicator.setCurrentValue(i);
					progIndicator.setText("Number of files renamed: " + numPrepared);
					if (progIndicator.isAborted())
						abort = true;
				}
				if (abort)
					break;
				if (files[i]==null )
					;
				else {
					cPath = directoryPath + MesquiteFile.fileSeparator + files[i];
					File cFile = new File(cPath);
					if (cFile.exists() && !cFile.isDirectory() && (!files[i].startsWith("."))) {

						String chromFileName = cFile.getName();
						if (StringUtil.blank(chromFileName)) {
							logln("Bad file name; it is blank.");
							// remove "running"
							if (progIndicator!=null) progIndicator.goAway();
							return false;
						}

						MesquiteString sampleCodeSuffix = new MesquiteString();
						MesquiteString originalSampleCode = new MesquiteString();
						MesquiteString primerName = new MesquiteString();
						MesquiteString startTokenResult = new MesquiteString();
						MesquiteInteger sampleCodeEndIndex = new MesquiteInteger();
						//here's where the names parser processes the name

						if (nameParserManager!=null) {
							if (!nameParserManager.parseFileName(chromFileName, originalSampleCode, sampleCodeSuffix, primerName, logBuffer, startTokenResult, sampleCodeEndIndex))
								continue;
						}
						else {
							loglnEchoToStringBuffer("Naming parsing rule is absent.", logBuffer);
							return false;
						}
						if (startTokenResult.getValue() == null)
							startTokenResult.setValue("");

						//progIndicator.spin();

						numPrepared++;
						try {
							newSampleCode = translationFile.getTranslatedSampleCode(originalSampleCode.getValue());
							String newFileName = "";
							File newFile = null;

							if (StringUtil.notEmpty(newSampleCode)){
								newFileName = chromFileName.substring(0,sampleCodeEndIndex.getValue())+"."+newSampleCode+chromFileName.substring(sampleCodeEndIndex.getValue(), chromFileName.length());

								String newFilePath = processedDirPath + MesquiteFile.fileSeparator + newFileName;					
								newFile = new File(newFilePath); //
								int count=1;
								while (newFile.exists()) {
									newFileName = ""+count + "." + chromFileName;
									newFilePath = processedDirPath + MesquiteFile.fileSeparator + newFileName;
									newFile = new File(newFilePath);
									count++;
								}
							} else {
								String oldFilePath = unprocessedDirPath + MesquiteFile.fileSeparator + chromFileName;					
								newFile = new File(oldFilePath); //
							}
							try { 
								MesquiteFile.copy(cFile,newFile);
								renamed++;
							}
							catch (IOException e) {
								if (!alreadyWarned)
									MesquiteMessage.discreetNotifyUser("Can't copy file");
								alreadyWarned = true;
							}

						}
						catch (SecurityException e) {
							logln( "Can't rename: " + cFile.getName());
						}




					}
				}
			}

			loglnEchoToStringBuffer("Number of files examined: " + (files.length-1), logBuffer);
			loglnEchoToStringBuffer("Number of files renamed: " + renamed, logBuffer);



			if (!abort) {

				progIndicator.spin();
			}


			if (progIndicator!=null)
				progIndicator.goAway();
		}
		return true;
	}


	public String getName() {
		return "Rename File Names PCR to Sample Code";
	}


}
