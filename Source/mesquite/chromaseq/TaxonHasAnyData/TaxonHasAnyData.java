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

package mesquite.chromaseq.TaxonHasAnyData;

import java.awt.Container;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class TaxonHasAnyData extends BooleanForTaxon {

	Taxa currentTaxa = null;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
	   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		currentTaxa = taxa;
	}

	public void calculateBoolean(Taxa taxa, int it, MesquiteBoolean result, MesquiteString resultString){
		if (result==null)
			return;
		result.setToUnassigned();
		clearResultAndLastResult(result);
		boolean any = false;
		for (int im = 0; im < getProject().getNumberCharMatrices(taxa); im++){
			CharacterData data = getProject().getCharacterMatrix(taxa, im);
			if (data.hasDataForTaxon(it)){
				any = true;
				break;
			}
		}
		result.setValue(any);

		if (resultString!=null){
			if (any)
				resultString.setValue("Has Data");
			else
				resultString.setValue("No Data");
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Has Any Data";  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports whether or not the taxon has any data in any matrix." ;
	}

}
