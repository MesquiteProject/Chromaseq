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

package mesquite.chromaseq.SequenceQuality;

import mesquite.chromaseq.lib.ChromaseqUtil;
import mesquite.cont.lib.ContinuousData;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;

	public class SequenceQuality extends NumberForTaxonAndMatrix {
		Taxa currentTaxa = null;
		MCharactersDistribution observedStates =null;
		ContinuousData qualityData;
		CharacterData data;


		/*.................................................................................................................*/
		public boolean startJob(String arguments, Object condition, boolean hiredByName) {
			return true;
		}

		/*.................................................................................................................*/
		public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
			observedStates = null;
			super.employeeParametersChanged(employee, source, notification);
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
		
	   	int count = 0;
	   	double summ = 0;
		/*.................................................................................................................*/
		private double getQualityTaxon(int it){
			if (data == null || qualityData == null)
				return 0;
			double sum = 0;
			int num = 0;
			int numChars = data.getNumChars(false);
			for (int ic = 0; ic<numChars; ic++){
				if (!data.isInapplicable(ic, it) && !data.isUnassigned(ic, it)) {
					double d = ChromaseqUtil.getQualityScoreForEditedMatrixBase(data,ic, it);
					if (MesquiteDouble.isCombinable(d) && d>=0 && d<=100){
						sum += d;
						num++;
					}
				}
			}
			if (num == 0)
				return 0;
			return sum*1.0/num;
			/*
	   		Object obj = data.getCellObject(qualityNameRef, ic, it);//IF USED use  ChromaseqUtil.getIntegerCellObject
	   		if (obj instanceof MesquiteInteger)
	   			return ((MesquiteInteger)obj).getValue();
	   		return 0;
			 */
		}
		/*.................................................................................................................*
   	private double getQualityTaxon2(int it){
	   		if (data == null)
	   			return 0;
	   		if (qualityData == null)
	   			return 0;
	    		double sum = 0;
	   		for (int ic = 0; ic<qualityData.getNumChars(false); ic++){
	   			if (!data.isUnassigned(ic, it) && !qualityData.isInapplicable(ic, it)) {
	   				double d = ChromaseqUtil.getQualityScoreForEditedMatrixBase(data,ic, it);
	   				if (d>101 || MesquiteDouble.isCombinable(d))
	   					;
	   				else if (d>=90.0)
	   					sum += 1;
	   				else if (d>=80.0)
	   					sum += 0.9;
	   				else if (d>=70.0)
	   					sum += 0.7;
	   				else if (d>=60.0)
	   					sum += 0.5;
	   				else if (d>=50.0)
	   					sum += 0.4;
	   				else if (d>=40.0)
	   					sum += 0.2;
	   				else
	   					sum += 1/(100.0 - d); //count good states more!
	   					//sum += 1/((100.0 - d)*(100.0 - d)); //count good states more!
	   			}
	   		}
	   		summ = sum;
	   		return sum*50/qualityData.getNumChars(false);
	   	}
	/*.................................................................................................................*/


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
			data = observedStates.getParentData();
			qualityData = ChromaseqUtil.getQualityData(data);

			double qualityScore = getQualityTaxon(it);

			result.setValue(qualityScore);
		
		
			if (resultString!=null)
				resultString.setValue("Quality of sequence in matrix "+ observedStates.getName()  + ": " + result.toString());
			saveLastResult(result);
			saveLastResultString(resultString);
		}
		/*.................................................................................................................*/
		public String getName() {
			if (observedStates != null && getProject().getNumberCharMatrices()>1){
				CharacterData d = observedStates.getParentData();
				if (d != null && d.getName()!= null) {
					String n =  d.getName();
					if (n.length()>12)
						n = n.substring(0, 12); 
					return "Qual.Score (" + n + ")";
				}
			}
			return "Quality Score";
		}

		/*.................................................................................................................*/
		public String getVeryShortName() {
			return "Seq. Quality";  
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
					return "Sequence quality in matrix (" + n + ")";
				}
			}
		return "Sequence quality in matrix";
		}
		/*.................................................................................................................*/

		/** returns an explanation of what the module does.*/
		public String getExplanation() {
			return "Reports a measure of sequence quality as judged by Phred/Phrap scores, for a molecular sequence in a taxon." ;
		}

	}



