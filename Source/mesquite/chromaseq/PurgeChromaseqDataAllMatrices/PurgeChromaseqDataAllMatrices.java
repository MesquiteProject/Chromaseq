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
package mesquite.chromaseq.PurgeChromaseqDataAllMatrices;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.chromaseq.lib.ChromaseqUtil;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.AlertDialog;

/* ======================================================================== */
public class PurgeChromaseqDataAllMatrices extends FileAssistantM {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		purge();
		return true;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Purges the chromaseq data", null, commandName, "purge")) {
			purge();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	private int numEditedMatrices(ListableVector datas){
		int count = 0;
		//First purge all chromaseq data for edited matrices
		for (int i=datas.size()-1; i>=0; i--) { //ask how many edited datas there are and keep looping until they are gone
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
			if (!data.isDoomed() && ChromaseqUtil.isChromaseqEditedMatrix(data)){
				count++;
			}
		}
		return count;
	}

	private void purge(){
		if (AlertDialog.query(containerOfModule(), "Purge Chromaseq Data", "Are you sure you want to remove all data associated with Chromaseq from this file? This cannot be undone.", "Purge", "Cancel", 2)){
			ListableVector datas = getProject().getCharacterMatrices();
			//First purge all chromaseq data for edited matrices
			while (numEditedMatrices(datas)>0){
				for (int i=datas.size()-1; i>=0; i--) { //ask how many edited datas there are and keep looping until they are gone
					mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
					if (!data.isDoomed() && ChromaseqUtil.isChromaseqEditedMatrix(data)){
						ChromaseqUtil.purgeChromaseqData(data);
						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
						data.setDirty(true);
						break;
					}
				}
			}
			//now purge any other chromaseq matrices that might be orphans because their edited matrices had been deleted
			for (int i=datas.size()-1; i>=0; i--) { 
				mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datas.elementAt(i);
				if (!data.isDoomed() && ChromaseqUtil.isChromaseqMatrix(data)){
					data.deleteMe(false);
				}
			}
		}

		//NOW set matrix editors to show by state

		iQuit();

	}
	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return false;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Purge Chromaseq Data from File...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Purge Chromaseq Data from File";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -1200;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Purges Chromaseq data from file." ;  
	}

}


