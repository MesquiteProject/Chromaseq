package mesquite.chromaseq.RemoveTrimmable;

/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.chromaseq.lib.*;

/* ======================================================================== */
public class RemoveTrimmable extends DataAlterer {
	CharacterState fillState=null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public void alterCell(CharacterData data, int ic, int it){
   		if (fillState!= null && ChromaseqUtil.isTrimmable(ic,it,data)){
   			data.setState(ic,it, fillState);
   			if (!MesquiteLong.isCombinable(numCellsAltered))
   				numCellsAltered = 0;
   			numCellsAltered++;
 		}
   	}
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table,UndoReference undoReference){

   		fillState = data.getCharacterState(fillState, 0, 0); //just to have a template
   		fillState.setToInapplicable();
		boolean success = alterContentOfCells(data,table, undoReference);
		return success;
   	}

   	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Remove Trimmable";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Fills trimmable bases in selected sequences with \"-\" in the character data editor." ;
   	 }
   	 
}

