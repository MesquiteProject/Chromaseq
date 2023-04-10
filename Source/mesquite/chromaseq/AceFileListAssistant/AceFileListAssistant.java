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

package mesquite.chromaseq.AceFileListAssistant;

import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.ChromaseqUtil;
import mesquite.lists.lib.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.table.*;


/* ======================================================================== */
public class AceFileListAssistant extends TaxonListAssistant {
	Taxa taxa;
	MesquiteTable table=null;
	Taxa currentTaxa = null;
	DNAData data = null;
	MatrixSourceCoord matrixSourceTask;
	MesquiteMenuItemSpec msBrowse;
	MesquiteMenuItemSpec msSet;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, DNAState.class, "Source of DNA matrix (for " + getName() + ")"); 
		if (matrixSourceTask==null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		return true;
	}

	/*.................................................................................................................*/
	public void setData() {
			matrixSourceTask.initialize(currentTaxa);
			MCharactersDistribution observedStates = matrixSourceTask.getCurrentMatrix(currentTaxa);
			if (observedStates==null) {
				data=null;
				return;
			}
			CharacterData d = observedStates.getParentData();
			if (d instanceof DNAData)
				data = (DNAData)d;
			else
				data=null;
		
	}
	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		if (this.taxa != null)
			this.taxa.removeListener(this);
		this.taxa = taxa;
		if (this.taxa != null)
			this.taxa.addListener(this);
		if (taxa != currentTaxa || data == null ) {
			currentTaxa = taxa;
			setData();
		}
		this.table = table;
		deleteMenuItem(msBrowse);
		msBrowse = addMenuItem("Browse for m.ace file...", makeCommand("browseFile", this));
		deleteMenuItem(msSet);
		msSet = addMenuItem("Set path of m.ace file...", makeCommand("setFile", this));
	}
	/*.................................................................................................................*/
	public void dispose() {
		super.dispose();
		if (taxa!=null)
			taxa.removeListener(this);
	}
	/*.................................................................................................................*/
	private void setFilePath(String arguments){
		if (table !=null && taxa!=null) {
			boolean changed=false;
			String path = arguments; //parser.getFirstToken(arguments);
			if (StringUtil.blank(path))
				return;
			path = StringUtil.stripTrailingWhitespace(path);   //DRM March 2023   Strip trailing whitespace as will be incorrectly saved in file path
			if (employer!=null && employer instanceof ListModule) {
				int c = ((ListModule)employer).getMyColumn(this);
				for (int i=0; i<taxa.getNumTaxa(); i++) {
					if (table.isCellSelectedAnyWay(c, i)) {
						setString(i,path);
						if (!changed)
							outputInvalid();
						changed = true;
					}
				}
			}

			outputInvalid();
			parametersChanged();

		}
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == matrixSourceTask)  // character source quit and none rehired automatically
			iQuit();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		setData();
		super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getMatrixSource", matrixSourceTask);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the matrix source", null, commandName, "getMatrixSource")) {
			return matrixSourceTask;
		}
		else	if (checker.compare(this.getClass(), "Sets the m.ace file to the one you choose by browsing", null, commandName, "browseFile")) {
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			String path = MesquiteFile.openFileDialog("Choose m.ace file", directoryName, fileName);
			if (StringUtil.blank(path))
				return null;
			path = MesquiteFile.decomposePath(getProject().getHomeFile().getDirectoryName(), path);  // convert to relative path
			setFilePath(path);	
		}
		else if (checker.compare(this.getClass(), "Sets the path to the m.ace file to the one you enter", null, commandName, "setFile")) {
			if (table!=null){
				String path = MesquiteString.queryString(containerOfModule(), "m.ace file", "Path of m.ace file:", "");
				if (StringUtil.blank(path))
					return null;
				path = MesquiteFile.decomposePath(getProject().getHomeFile().getDirectoryName(), path);  // convert to relative path
				setFilePath(path);	
			}
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getTitle() {
		return "Modified Ace Files";
	}
	public String getStringForTaxon(int ic){
		if (data==null || taxa==null)
			return "-";
		Associable as = data.getTaxaInfo(false);
		String s = ChromaseqUtil.getStringAssociated(as,ChromaseqUtil.aceRef, ic);

		if (s!=null) {
			return s;
		}
		return "-";
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int row){
		return false;
	}
	/*...............................................................................................................*/
	/** for those permitting editing, indicates user has edited to incoming string.*/
	public void setString(int row, String s){
		if (taxa!=null && data!=null) {
			Associable as = data.getTaxaInfo(false);
			ChromaseqUtil.setStringAssociated(as,ChromaseqUtil.aceRef, row, s);
			taxa.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		}

	}
	public boolean useString(int ic){
		return true;
	}

	public String getWidestString(){
		return "88888888888888888  ";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Modified Ace File";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -1000;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Lists the modified ACE files (which end in \"m.ace\") that provide links to chromatograms." ;
	}
}
