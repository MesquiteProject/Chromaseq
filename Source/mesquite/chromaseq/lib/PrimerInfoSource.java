package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteModule;

public abstract class PrimerInfoSource extends MesquiteModule {

	public Class getDutyClass() {
		return PrimerInfoSource.class;
	}

 	public String getDutyName() {
 		return "Primer Information Source";
   	 }

 // returns whether primer is in forward direction
 	public abstract boolean isForward(String ID);

 	//returns name of primer as a string
 	public abstract boolean getPrimerName(String ID);

 	//returns sequence of primer as a string
 	public abstract boolean getPrimerSequenceString(String ID);

 	// returns length of primer sequence
 	public abstract int getPrimerSequenceLength(String ID);

 	// returns gene fragment name
 	public abstract String getGeneFragmentName(String ID);

 	//returns whether source has all relevant data supplied (e.g. address of database, etc.)
 	public abstract boolean isReady();

}
