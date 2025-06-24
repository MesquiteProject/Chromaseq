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

package mesquite.chromaseq.NumberOfReads;

import mesquite.categ.lib.DNAData;
import mesquite.chromaseq.lib.AceFile;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;

public class NumberOfReads extends NumberForTaxonAndMatrix {

	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}


	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
	   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		currentTaxa = taxa;
	}

	public void calculateNumber(Taxon taxon, MCharactersDistribution matrix, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
		result.setToUnassigned();
		clearResultAndLastResult(result);
		Taxa taxa = taxon.getTaxa();
		int it = taxa.whichTaxonNumber(taxon);
			observedStates = matrix;
			currentTaxa = taxa;
		
		if (observedStates==null)
			return;
		DNAData data = (DNAData)observedStates.getParentData();

		result.setValue(0);
		if (data!=null) {
			AceFile ace = AceFile.getAceFile(this, data, it);
			if (ace!=null)
				result.setValue(ace.getNumReads());
		}




		if (resultString!=null)
			resultString.setValue("Number of reads "+ observedStates.getName()  + ": " + result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Number of Reads";  
	}

	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "# Reads";  
	}
/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public String getParameters() {
		if (observedStates != null && getProject().getNumberCharMatricesVisible()>1){
			CharacterData d = observedStates.getParentData();
			if (d != null && d.getName()!= null) {
				String n =  d.getName();
				if (n.length()>12)
					n = n.substring(0, 12); 
				return "Number of reads in sequence (" + n + ")";
			}
		}
		return "Number of Reads in sequence";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the number of reads associated with a sequence in a taxon." ;
	}

}
