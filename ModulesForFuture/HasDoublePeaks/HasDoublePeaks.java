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

package mesquite.chromaseq.HasDoublePeaks;

import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.AceFile;
import mesquite.chromaseq.lib.ChromaseqUtil;
import mesquite.chromaseq.lib.Contig;
import mesquite.chromaseq.lib.MatrixSequence;
import mesquite.cont.lib.ContinuousData;
import mesquite.lib.*;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.*;

public class HasDoublePeaks extends BooleanForTaxon {

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of sequences.",
		"The source of characters is arranged initially");
	}
	MatrixSourceCoord matrixSourceTask;
	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	double fractionThreshold = 0.3;
	double strictFractionThreshold = 0.1;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, DNAState.class, "Source of character matrix (for " + getName() + ")"); 
		if (matrixSourceTask==null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		return true;
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}

	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == matrixSourceTask)  // character source quit and none rehired automatically
			iQuit();
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
		matrixSourceTask.initialize(currentTaxa);
	}

	public void calculateBoolean(Taxa taxa, int it, MesquiteBoolean result, MesquiteString resultString){
		if (result==null)
			return;
		result.setToUnassigned();
		clearResultAndLastResult(result);
		if (taxa != currentTaxa || observedStates == null ) {
			observedStates = matrixSourceTask.getCurrentMatrix(taxa);
			currentTaxa = taxa;
		}
		if (observedStates==null)
			return;
		DNAData data = (DNAData)observedStates.getParentData();
		ContinuousData qualityData = ChromaseqUtil.getQualityData(data);
		if (qualityData==null || data==null)
			return;

		Contig contig = ChromaseqUtil.getContig(data, it, this, false);
		int count = 0;

		if (contig!=null) {
			double quality=0.0;

			int numChars = data.getNumChars();
			int numApplicable = data.getNumberApplicableInTaxon(it, true);

			int[] seq = new int[numApplicable];
			IntegerArray.zeroArray(seq);

			int base=0;
			double qualityThreshold = 60.0;
			boolean doublePeak = false;
			boolean partialDoublePeak = false;
			boolean highQuality = false;

			final int hasSinglePeak = 1;
			final int hasPartialDoublePeak = 2;
			final int hasDoublePeak = 3;

			for (int ic=0; ic<numChars; ic++) {
				if (data.isValidAssignedState(ic, it)){
					if (base<seq.length) {
						quality = ChromaseqUtil.getQualityScoreForEditedMatrixBase(data, ic, it);
						doublePeak = contig.getDoublePeak(ic, fractionThreshold);
						partialDoublePeak = contig.getDoublePeak(ic, strictFractionThreshold);
						highQuality = (MesquiteDouble.isCombinable(quality) && quality>qualityThreshold);
						if (doublePeak) {
							if (highQuality)
								seq[base] = hasDoublePeak;
							else
								seq[base] = - hasDoublePeak;
						} else if (partialDoublePeak) {
							if (highQuality)
								seq[base] = hasPartialDoublePeak;
							else
								seq[base] = - hasPartialDoublePeak;
						} else
							if (highQuality)
								seq[base] = hasSinglePeak;
							else
								seq[base] = - hasSinglePeak;
					}
					base++;
				}
			}

			

			int previousHighQuality = 0;
			final int highQualityWindowThreshold = 3;
			boolean highEnoughQuality = false;
			int minDistanceFromHighQualityStretch = 3;
			int endOfLastHighQualityStretch = -2*minDistanceFromHighQualityStretch;

			count = 0;
			//base=0;
			boolean found = false;
			for (int ic=0; ic<seq.length && !found; ic++) {
				highQuality = (seq[ic]>0);
				doublePeak = (seq[ic] == hasDoublePeak || seq[ic] == -hasDoublePeak);
				if (highQuality && !doublePeak){
					previousHighQuality++;
					if (previousHighQuality>highQualityWindowThreshold)
						endOfLastHighQualityStretch=ic;
				}
				else {
					previousHighQuality=0;
				}
				if (previousHighQuality>highQualityWindowThreshold)
					highEnoughQuality=true;
				if (doublePeak && (ic-endOfLastHighQualityStretch)<minDistanceFromHighQualityStretch && highEnoughQuality){
					endOfLastHighQualityStretch = -2*minDistanceFromHighQualityStretch;
					doublePeak=false;
					found = false;
					for (int icAdd=1; icAdd<=minDistanceFromHighQualityStretch  && !found; icAdd++) {  // scan to see if there is a high enough single peak close enough
						int ic2 = ic+icAdd;
						if (ic2>=seq.length)
							break;
						boolean nextHighQuality = (seq[ic2]>0);
						boolean nextDoublePeak = (seq[ic2] == hasDoublePeak || seq[ic2] == -hasDoublePeak);
						if (nextHighQuality && !nextDoublePeak){
							count++;
							found=true;
						}
					}
				}
			}
		}

		result.setValue(count>0);

		if (resultString!=null)
			resultString.setValue("Has double peaks "+ observedStates.getName()  + ": " + result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Has Double Peaks";  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}

	public String getParameters() {
		return "Presence of double peaks in sequence from: " + matrixSourceTask.getParameters();
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the presence of double peaks associated with a sequence in a taxon, based upon chromatograms.  May miss some double peaks if there are double-peak sites that are very close together." ;
	}

}
