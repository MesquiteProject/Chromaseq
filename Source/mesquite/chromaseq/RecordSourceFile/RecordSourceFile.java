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

package mesquite.chromaseq.RecordSourceFile; 


import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.QueryDialogs;
import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class RecordSourceFile extends FileAssistantT { 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		recordSourceProject();
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	private void recordSourceProject(){
		MesquiteString s = new MesquiteString();
		s.setValue(getProject().getHomeFileName());
		if (!QueryDialogs.queryString(containerOfModule(), "Name to Stamp", "Indicate source file name to stamp on matrix rows", s))
			return;

		int numMatrices = getProject().getNumberCharMatrices();
		NameReference sourceRef = NameReference.getNameReference("SourceFile");

		for (int im = 0; im<numMatrices; im++){
			CharacterData data = getProject().getCharacterMatrix(im);
			Taxa taxa = data.getTaxa();
			Associable tInfo = data.getTaxaInfo(true);
			boolean anySelected = taxa.anySelected();
			for (int it = 0; it< taxa.getNumTaxa(); it++){
				if (data.hasDataForTaxon(it) && (!anySelected || taxa.getSelected(it)))
					tInfo.setAssociatedObject(sourceRef, it, s.getValue());
			}
		}


	}
	/*.................................................................................................................*/
	public String getName() {
		return "Stamp Source File into Matrix Rows";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Stamps current home file of project into SOURCEFILE note into all rows of all matrices.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -1000;  
	}

}




