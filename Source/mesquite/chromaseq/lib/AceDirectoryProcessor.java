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


package mesquite.chromaseq.lib;

import java.io.File;

import mesquite.categ.lib.DNAData;
import mesquite.lib.Associable;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.AlertDialog;

public class AceDirectoryProcessor {
	
	String[][] fileNameTranslation;
	
	StringBuffer contigPropertiesFileBuffer = null;

	boolean processPolymorphisms = true;
	boolean addPhrapFailures = true;
	double polyThreshold = 0.3;
	int qualThresholdForTrim = 20;
	int qualThresholdForLowerCase = 49;
	boolean truncateMixedEnds = true;
	int mixedEndWindow = 10;
	int mixedEndThreshold = 5;
	boolean renameContigsInAceFiles = true;
	boolean addFragName = false;
	boolean singleTaxaBlock = false;
	
	MesquiteInteger maxChar = new MesquiteInteger(0);
	
	public AceDirectoryProcessor () {
		
	}
	
	/*.................................................................................................................*/
	public void fillNameTranslation(DNAData editedData, int it, int numReads) {
		Associable as = editedData.getTaxaInfo(false);
		if (as==null)
			return;
		String[] fileNames = ChromaseqUtil.getStringsAssociated(as,  ChromaseqUtil.origReadFileNamesRef, it);
		String[] primerNames = ChromaseqUtil.getStringsAssociated(as,  ChromaseqUtil.primerForEachReadNamesRef, it);
		String[] sampleCodes = ChromaseqUtil.getStringsAssociated(as,  ChromaseqUtil.sampleCodeNamesRef, it);
		

		for (int i=0; i<numReads; i++){
			
			if (fileNames==null) {
				if (i==0)
					MesquiteMessage.warnProgrammer("fileNames NULL in AceDirectoryProcess.fileNameTranslation");
			}
			else if (i*2+1<fileNames.length) {
				fileNameTranslation[0][i] = fileNames[i*2];
				fileNameTranslation[1][i] = fileNames[i*2+1];
			} else
				MesquiteMessage.warnProgrammer("fileNames.length too small in AceDirectoryProcess.fileNameTranslation: fileNames.length = " + fileNames.length + ", numReads: " + numReads);
			
			if (primerNames==null){
				if (i==0)
					MesquiteMessage.warnProgrammer("primerNames NULL in AceDirectoryProcess.fileNameTranslation");
			}
			else if (i*2+1<primerNames.length)
				fileNameTranslation[2][i] = primerNames[i*2+1];
			else
				MesquiteMessage.warnProgrammer("primerNames.length too small in AceDirectoryProcess.fileNameTranslation: primerNames.length = " + primerNames.length + ", numReads: " + numReads);
			
			if (sampleCodes==null){
				if (i==0)
					MesquiteMessage.warnProgrammer("sampleCodes NULL in AceDirectoryProcess.fileNameTranslation");
			}
			else if (i*3+2<sampleCodes.length) {
				fileNameTranslation[3][i] = sampleCodes[i*3+1];
				fileNameTranslation[4][i] = sampleCodes[i*3+2];
			} else
				MesquiteMessage.warnProgrammer("sampleCodes.length too small in AceDirectoryProcess.fileNameTranslation: sampleCodes.length = " + sampleCodes.length + ", numReads: " + numReads);
		}

	}
	/*.................................................................................................................*/
	public void processAceFileWithContig(CharacterData data, MesquiteModule ownerModule, String processedAceFilePath, String fragmentDirPath, AceFile ace, SequenceUploader uploader, String geneName, MesquiteString fullName, String baseName, MesquiteString voucherCode, int it) {
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		DNAData originalData = ChromaseqUtil.getOriginalData(data);
		Taxa taxa = data.getTaxa();
		ace.setNameTranslation(fileNameTranslation);
		ownerModule.log(ace.contigListForLog()+StringUtil.lineEnding());
		if (processPolymorphisms)
			ace.processPolys();  //creates an additional CO that has polys in it
		if (renameContigsInAceFiles)
			ace.renameContigs(fullName.toString(), addFragName, geneName);
		ace.setLowQualityToLowerCase(qualThresholdForLowerCase); 
		ace.writeToPropertiesFile(contigPropertiesFileBuffer, fullName.toString());
		if (truncateMixedEnds)
			ace.trimMixedEnds(mixedEndThreshold, mixedEndWindow, qualThresholdForTrim, addPhrapFailures);

/*		if (uploadResultsToDatabase && StringUtil.notEmpty(databaseURL)) {
			uploader.uploadAceFileToServer(MesquiteXMLToLUtilities.getTOLPageDatabaseURL(databaseURL), ace, processPolymorphisms, qualThresholdForTrim);
		}
*/
		System.out.println("\n\nfasta file name: " + baseName + " ace file: " + ace);
		MesquiteFile.putFileContents(fragmentDirPath  + MesquiteFile.fileSeparator + ChromaseqUtil.processedFastaFolder + MesquiteFile.fileSeparator + baseName+".fas", ace.toFASTAString(processPolymorphisms, qualThresholdForTrim), true);
		MesquiteFile.putFileContents(processedAceFilePath, ace.toString(processPolymorphisms), true);
		ace.importSequence(taxa, editedData, it, originalData, ChromaseqUtil.getQualityData(data), ChromaseqUtil.getRegistryData(data), singleTaxaBlock, processPolymorphisms, maxChar," contig ", false, voucherCode);
		
	}

	/*.................................................................................................................*/
	public void processAceFileWithoutContig(DNAData data, String processedAceFilePath, AceFile ace, String geneName, MesquiteString fullName, int it, MesquiteString voucherCode) {
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		DNAData originalData = ChromaseqUtil.getOriginalData(data);
		Taxa taxa = data.getTaxa();
		ace.processFailedContig(polyThreshold);
		ace.setNameTranslation(fileNameTranslation);
		
		ace.renameContigs(fullName.toString(), addFragName, geneName);
		ace.setLowQualityToLowerCase(qualThresholdForLowerCase); 
		ace.writeToPropertiesFile(contigPropertiesFileBuffer, fullName.toString());
		if (truncateMixedEnds){
			ace.trimMixedEnds(mixedEndThreshold, mixedEndWindow, qualThresholdForTrim, addPhrapFailures);
		}
		MesquiteFile.putFileContents(processedAceFilePath, ace.toString(processPolymorphisms), true);
		ace.importSequence(taxa, editedData, it, originalData, ChromaseqUtil.getQualityData(data), ChromaseqUtil.getRegistryData(data), singleTaxaBlock, processPolymorphisms, maxChar,"", true, voucherCode);
	}
	/*.................................................................................................................*/
	public void reprocessAceFileDirectory(MesquiteFile file, MesquiteModule ownerModule, DNAData data, int it) {
		if (data==null || file==null)
			return;
		String aceFileDirectoryPath = ChromaseqUtil.getAceFileDirectory(file.getDirectoryName(),ownerModule,data,it);
		File aceFileDirectory = new File(aceFileDirectoryPath);
		boolean addFragName = false;  // control of this?
		int currentRead=-1;
		String dataFilePath = MesquiteFile.composePath(data.getProject().getHomeDirectoryName(), "");
		boolean addingPhrapFailures = false;
		AceFile ace = null;
		MesquiteProject project = data.getProject();
		if (project==null)
			return;
		String processedAceFilePath = "";
		MesquiteString fullName = null;
		MesquiteString voucherCode = null;
		String geneName = ChromaseqUtil.getGeneName(data);
		if (aceFileDirectory.isDirectory()) {
			int numPhdFiles = getNumPhdFilesInDirectory(aceFileDirectory, aceFileDirectoryPath);
			fileNameTranslation = new String[5][numPhdFiles];
			fillNameTranslation(data,  it, numPhdFiles);

			String[] files = aceFileDirectory.list();
			for (int i=0; i<files.length; i++) { // going through the folders and finding the ace files
				if (files[i]!=null ) {
					String filePath = aceFileDirectoryPath + MesquiteFile.fileSeparator + files[i];
					String infoFilePath = aceFileDirectoryPath + MesquiteFile.fileSeparator + ChromaseqUtil.infoFileName;
					File cFile = new File(filePath);
					if (cFile.exists()) {
						if (!cFile.isDirectory()) {
							if (files[i].endsWith(ChromaseqUtil.processedACESuffix+".ace")) {
								// don't do anything
							}
							else if (files[i].endsWith(".ace")  && !files[i].startsWith(".") && !addingPhrapFailures) {
								ownerModule.logln("Processing ACE file: " + files[i]);

								String baseName = files[i].substring(0,files[i].length()-4);  //this is the name of the sequence
								processedAceFilePath = aceFileDirectoryPath + MesquiteFile.fileSeparator + baseName+ChromaseqUtil.processedACESuffix+".ace";

								ace = new AceFile(filePath,processedAceFilePath, dataFilePath, dataFilePath, ownerModule, processPolymorphisms, polyThreshold, false);
								if (ace==null)
									return;
								
								ace.setBaseName(baseName);
								fullName = new MesquiteString(baseName);
								voucherCode = new MesquiteString();
								ChromaseqInfoFile.processInfoFile(infoFilePath, fullName, voucherCode);
								String fragmentDirPath = StringUtil.getAllButLastItem(StringUtil.getAllButLastItem(aceFileDirectoryPath,MesquiteFile.fileSeparator ),MesquiteFile.fileSeparator );								
								ace.setLongSequenceName(fullName.toString());
								if (ace.getNumContigs()>=1) {
									processAceFileWithContig(data,  ownerModule, processedAceFilePath,  fragmentDirPath,  ace,  null,  geneName,  fullName,  baseName, voucherCode, it);
								}
								else {
									ownerModule.logln("   ACE file contains no contigs!"); 
									if (project !=null) {
										addingPhrapFailures = true;
										i=0;
										ace.createEmptyContigs(MesquiteFile.numFilesEndingWith(aceFileDirectoryPath,files,".phd.1"));  //create an empty contig
										ace.renameContigs(fullName.toString(), addFragName, geneName);
									}
								}

								if (!addingPhrapFailures)
									ace.dispose();
							}
							else if (files[i].endsWith(".phd.1") && addingPhrapFailures) {
								ownerModule.logln("   Importing single-read Phred file "+files[i]); 
								currentRead++;
								ace.addPhdFileAsSingleReadInContig(currentRead, aceFileDirectoryPath, files[i], processPolymorphisms, polyThreshold);
							}
						}
					}
				}
			}
		}
		
		
		if (addingPhrapFailures && ace!=null) {  // have to process AceFile that we have manually made
			MesquiteFile.putFileContents(processedAceFilePath, ace.toString(processPolymorphisms), true);
			if (project != null){
				processAceFileWithoutContig(data, processedAceFilePath,  ace,  geneName,  fullName,it, voucherCode);
			}
			ace.dispose();
		}
	}

	/*.................................................................................................................*/
	public int getNumPhdFilesInDirectory(File aceFileDirectory, String aceFileDirectoryPath) {
		int count = 0;
		if (aceFileDirectory.isDirectory()) {
			String[] files = aceFileDirectory.list();
			for (int i=0; i<files.length; i++) { // going through the folders and finding the ace files
				if (files[i]!=null ) {
					String filePath = aceFileDirectoryPath + MesquiteFile.fileSeparator + files[i];
					File cFile = new File(filePath);
					if (cFile.exists()) {
						if (!cFile.isDirectory()) {
							 if (files[i].endsWith(".phd.1")) {
								 count++;
							}
						}
					}
				}
			}
		}
		return count;
	}
	/*.................................................................................................................*/
	public  static boolean hasDisconnectedAceFiles(CharacterData data, MesquiteModule ownerModule) {
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if( editedData==null)
			return true;
		for (int it=0; it<editedData.getNumTaxa(); it++)  
			if (AceFile.hasAceFilePath(editedData, it) && !AceFile.hasAceFile(ownerModule, editedData,it)) {
//				boolean db = AceFile.hasAceFile(ownerModule, editedData,it);
				return true;
			}
		return false;
	}
	/*.................................................................................................................*/

		public  static boolean checkNoContigAceFiles(CharacterData data, MesquiteModule ownerModule) {
			DNAData editedData = ChromaseqUtil.getEditedData(data);
			if( editedData==null)
				return false;
			int count=0;
			boolean resave = false;
			boolean warn = false;
			for (int it=0; it<editedData.getNumTaxa(); it++)  
				if (AceFile.hasAceFilePath(editedData, it)){
					if (!AceFile.hasAceFile(ownerModule,editedData,it)) {
					}
					AceFile ace = AceFile.getAceFile(ownerModule, editedData, it);
					if (ace!=null) {
						if (ace.getNumContigs()<=0 || ace.getContig(0).getNumBases()==0){
							if (!warn && !AlertDialog.query(ownerModule.containerOfModule(), "Reprocess and save file?", "Some of the contigs need to be reprocessed, which will" +
									" alter the modified .ace files produced by Phrap and Chromaseq.  To be compatible with these altered .ace files, the Mesquite file " +
									"would then need to be re-saved. If instead you choose not to reprocess contigs, they will not be fully editable in Chromaseq.", "Reprocess and Save", "Do not reprocess", -1)) {
								return false;
							} else
								resave = true;
							warn=true;
							ChromaseqUtil.setReprocessContig(editedData,it);
						}
					}
					count++;
				}
			return resave;
	}
	/*.................................................................................................................*/

	public  static boolean processNoContigAceFiles(CharacterData data, MesquiteModule ownerModule) {
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if( editedData==null)
			return false;
		boolean changed=false;
		MesquiteFile file = data.getProject().getHomeFile();
		int count=0;
		int originalChars = editedData.getNumChars();
		for (int it=0; it<editedData.getNumTaxa(); it++)  
			if (ChromaseqUtil.reprocessContig(editedData,it)) {
				AceDirectoryProcessor aceDirProcessor = new AceDirectoryProcessor();
				aceDirProcessor.reprocessAceFileDirectory(file, ownerModule, editedData, it);
				count++;
			}
		if (count>0){
			editedData.getTaxa().notifyListeners(ownerModule, new Notification(ownerModule.PARTS_ADDED));
			MesquiteMessage.discreetNotifyUser("Some of the contigs have been reprocessed; this will be lost permanently unless you resave the file.");
			changed=true;
		}
		if (originalChars < editedData.getNumChars())
			editedData.notifyListeners(ownerModule, new Notification(ownerModule.PARTS_ADDED));
		else if (originalChars > editedData.getNumChars())
			editedData.notifyListeners(ownerModule, new Notification(ownerModule.PARTS_DELETED));

		ChromaseqUtil.removeAssociatedStrings(editedData, ChromaseqUtil.reprocessContigRef);
		return changed;
	}

	public boolean isProcessPolymorphisms() {
		return processPolymorphisms;
	}

	public void setProcessPolymorphisms(boolean processPolymorphisms) {
		this.processPolymorphisms = processPolymorphisms;
	}

	public boolean isAddPhrapFailures() {
		return addPhrapFailures;
	}

	public void setAddPhrapFailures(boolean addPhrapFailures) {
		this.addPhrapFailures = addPhrapFailures;
	}

	public double getPolyThreshold() {
		return polyThreshold;
	}

	public void setPolyThreshold(double polyThreshold) {
		this.polyThreshold = polyThreshold;
	}

	public int getQualThresholdForTrim() {
		return qualThresholdForTrim;
	}

	public void setQualThresholdForTrim(int qualThresholdForTrim) {
		this.qualThresholdForTrim = qualThresholdForTrim;
	}

	public int getQualThresholdForLowerCase() {
		return qualThresholdForLowerCase;
	}

	public void setQualThresholdForLowerCase(int qualThresholdForLowerCase) {
		this.qualThresholdForLowerCase = qualThresholdForLowerCase;
	}

	public boolean isTruncateMixedEnds() {
		return truncateMixedEnds;
	}

	public void setTruncateMixedEnds(boolean truncateMixedEnds) {
		this.truncateMixedEnds = truncateMixedEnds;
	}

	public int getMixedEndWindow() {
		return mixedEndWindow;
	}

	public void setMixedEndWindow(int mixedEndWindow) {
		this.mixedEndWindow = mixedEndWindow;
	}

	public int getMixedEndThreshold() {
		return mixedEndThreshold;
	}

	public void setMixedEndThreshold(int mixedEndThreshold) {
		this.mixedEndThreshold = mixedEndThreshold;
	}

	public boolean isRenameContigsInAceFiles() {
		return renameContigsInAceFiles;
	}

	public void setRenameContigsInAceFiles(boolean renameContigsInAceFiles) {
		this.renameContigsInAceFiles = renameContigsInAceFiles;
	}

	public boolean isAddFragName() {
		return addFragName;
	}

	public void setAddFragName(boolean addFragName) {
		this.addFragName = addFragName;
	}

	public boolean isSingleTaxaBlock() {
		return singleTaxaBlock;
	}

	public void setSingleTaxaBlock(boolean singleTaxaBlock) {
		this.singleTaxaBlock = singleTaxaBlock;
	}

}
