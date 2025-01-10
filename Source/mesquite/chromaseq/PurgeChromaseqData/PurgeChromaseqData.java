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

package mesquite.chromaseq.PurgeChromaseqData; 


import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.AlertDialog;
import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class PurgeChromaseqData extends DataUtility { 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
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
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -1000;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** Called to operate on the data in all cells.  Returns true if data altered*/
	public boolean operateOnData(CharacterData data){ 
		if (data==null)
			return false;
		if (AlertDialog.query(containerOfModule(), "Purge Chromaseq Data", "Are you sure you want to remove all data associated with Chromaseq from this matrix? This cannot be undone.", "Purge", "Cancel", 2)){
			ChromaseqUtil.purgeChromaseqData(data);
/*			MesquiteModule mbc = findEmployeeWithName("#ColorByState", true);
			MesquiteWindow mw = containerOfModule();
			mw.setCellColorer(mbc);
*/
			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			data.setDirty(true);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		return temp;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Purge Matrix of Chromaseq Data";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Purges matrix of data associated with Chromaseq, so that the file can be moved elsewhere without Chromaseq complaining about not finding its associated files.";
	}
}




