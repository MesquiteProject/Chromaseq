package mesquite.chromaseq.NumberOfReads;

import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.AceFile;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Taxa;
import mesquite.lib.Taxon;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.duties.NumberForTaxon;

public class NumberOfReads extends NumberForTaxon {

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of sequences.",
		"The source of characters is arranged initially");
	}
	MatrixSourceCoord matrixSourceTask;
	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, DNAState.class, "Source of character matrix (for " + getName() + ")"); 
		if (matrixSourceTask==null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		return true;
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

	public void calculateNumber(Taxon taxon, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
		result.setToUnassigned();
		clearResultAndLastResult(result);
		Taxa taxa = taxon.getTaxa();
		int it = taxa.whichTaxonNumber(taxon);
		if (taxa != currentTaxa || observedStates == null ) {
			observedStates = matrixSourceTask.getCurrentMatrix(taxa);
			currentTaxa = taxa;
		}
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
	public boolean isPrerelease() {
		return false;
	}
	public String getParameters() {
		return "Number of Reads in sequence from: " + matrixSourceTask.getParameters();
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the number of reads associated with a sequence in a taxon." ;
	}

}
