package mesquite.chromaseq.PhredPhrapTaxonRedo;

import mesquite.chromaseq.lib.AceFile;
import mesquite.chromaseq.lib.ChromatogramViewer;
import mesquite.chromaseq.lib.PhPhRunner;
import mesquite.lib.*;
import mesquite.categ.lib.*;
import mesquite.charMatrices.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.CMTable;
import mesquite.lib.table.MesquiteTable;


/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
	Version 2.6, January 2009.
	Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
	The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
	Perhaps with your help we can be more than a few, and make Mesquite better.

	Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
	Mesquite's web site is http://mesquiteproject.org

	This source code and its compiled class files are free and modifiable under the terms of 
	GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

/* ======================================================================== */
public class PhredPhrapTaxonRedo extends CategDataEditorInit {
	NameReference aceRef = NameReference.getNameReference(AceFile.ACENAMEREF);
	MesquiteMenuItemSpec ms = null;
	MesquiteTable table;
	DNAData data;


	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		//ms = addMenuItem(null, "Phred-Phrap Contig of Taxa", makeCommand("phredPhrapContig", this));
		return true;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = (CMTable)table;
		if (this.data != data){
			if (this.data != null)
				this.data.removeListener(this);
			data.addListener(this);
		}

		if (data instanceof DNAData)
			this.data = (DNAData)data;
		boolean b = false;
		if (data!=null) {
			Associable tInfo= data.getTaxaInfo(false);
			if (tInfo!=null)
				b = tInfo.anyAssociatedObject(aceRef);

		}
		if (ms!=null)
			ms.setEnabled(b);
	}

	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
	public  boolean reContig(){
		if (data==null)
			return false;
		if (!MesquiteThread.isScripting()){
			boolean OK = AlertDialog.query(containerOfModule(), "Redo Contig?", "Are you sure you want to redo the contig for the selected taxa?  You will not be able to undo this.  " 
					+ "Other associated information like footnotes, attachments, and so forth WILL BE LOST.");
			if (!OK)
				return false;
		}
		Taxa taxa = data.getTaxa();
		int numSelected = taxa.numberSelected();

		PhPhRunner phphTask = (PhPhRunner)hireEmployee(PhPhRunner.class, "Module to run Phred & Phrap");
		if (phphTask == null)
			return false;

		boolean[] selected = new boolean[taxa.getNumTaxa()];
		String newDirectoryPath = null;
		int firstSelected = MesquiteInteger.unassigned;
		
		StringArray chromatoPathArray = new StringArray(1);
		StringArray directoryArray = new StringArray(1);
		

		for (int it = 0; it<taxa.getNumTaxa(); it++) {
			selected[it] = taxa.getSelected(it);
			Associable tInfo = data.getTaxaInfo(false);
			String aceFilePath = null;

			if (tInfo != null) {
				aceFilePath = (String)tInfo.getAssociatedObject(aceRef, it);
				aceFilePath = MesquiteFile.composePath(getProject().getHomeFile().getDirectoryName(), aceFilePath);
			}
			String directoryPath = StringUtil.getAllButLastItem(aceFilePath, MesquiteFile.fileSeparator, "/"); 
			if (!MesquiteInteger.isCombinable(firstSelected)) {
				firstSelected = it;
				newDirectoryPath = MesquiteFile.getUniqueModifiedPath(directoryPath + "REDO"); 
				if (!MesquiteFile.createDirectory(newDirectoryPath))
					return false;
			}



			if (!StringUtil.blank(aceFilePath) && !directoryArray.exists(directoryPath)) {
				directoryArray.addAndFillNextUnassigned(directoryPath);
				aceFilePath = MesquiteFile.composePath(getProject().getHomeFile().getDirectoryName(), aceFilePath);
				if (!MesquiteFile.fileExists(aceFilePath)){
					discreetAlert( "The stored path to the chromatogram files appears invalid.  Perhaps the processed chromatograms directory has been moved relative to the NEXUS file, or perhaps your operating system has modified the file names. (path:  " + aceFilePath + ")");
					return false;
				}
				else
					AceFile.getChromatogramFileNames(aceFilePath,chromatoPathArray);
			}
		}

		if (newDirectoryPath==null)
			return false;
		
		for (int i = 0; i<chromatoPathArray.getSize(); i++){
			String newPath = chromatoPathArray.getValue(i);
			String fileName = MesquiteFile.getFileNameFromFilePath(newPath);
			newPath =  MesquiteFile.getUniqueModifiedPath(newDirectoryPath + MesquiteFile.fileSeparator+fileName); 

			MesquiteFile.copyFileFromPaths(chromatoPathArray.getValue(i),newPath, false);
		}

		if (phphTask.doPhredPhrap(getProject(), true, newDirectoryPath)) {
		/*	for (int it =  taxa.getNumTaxa() -1; it> firstSelected; it--){
				if (selected[it]) {
					taxa.deleteTaxa(it, 1, false);
				}
			}
			*/
		}



		taxa.notifyListeners(this, new Notification(PARTS_DELETED));
		data.notifyListeners(this, new Notification(PARTS_DELETED));

		//linked?



		return true;
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Prepare ABI files for processing by Phred and Phrap and run them", null, commandName, "phredPhrapContig")) {

			reContig();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Phred-Phrap Contig of Taxa";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Recontigs taxa using Phred/Phrap/Chromaseq.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

}




