package mesquite.chromaseq.PrimerInfoFromDatabase;

import org.dom4j.Element;

import mesquite.chromaseq.lib.PrimerInfoSource;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTrunk;
import mesquite.molec.lib.DNADatabaseURLSource;

public class PrimerInfoFromDatabase extends PrimerInfoSource {
	boolean preferencesSet = false;
	PrimerInformationDatabase primers = null;
	protected DNADatabaseURLSource databaseURLSource = null;
//	private String databaseURL = "";

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		
		if (databaseURLSource==null)
			databaseURLSource= (DNADatabaseURLSource)hireEmployee(DNADatabaseURLSource.class, "Source of Database Connectivity");
		if (databaseURLSource==null)
			return false;
		
		 primers = new PrimerInformationDatabase(databaseURLSource);

		return true;
	}
	public boolean optionsSpecified(){
		return (databaseURLSource!=null) ;
	}

	public boolean hasOptions(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean loadModule(){
		return false;
	}

	/*.................................................................................................................*
	public  void addMenuItemsForPrimerSubmenu(MesquiteSubmenuSpec primerSubmenu){
		if (primerSubmenu!=null)
			addItemToSubmenu(null, primerSubmenu, "Choose Primer Information Database Source...", MesquiteModule.makeCommand("choosePrimerDatabaseSource",  this));
		else
			addMenuItem( "Choose Primer Information Database Source...", MesquiteModule.makeCommand("choosePrimerDatabaseSource",  this));
	}
	/*.................................................................................................................*/
	public  void addXMLAttributes(Element element){
		element.addAttribute("databaseURLSource", databaseURLSource.getClassName());
	}


	/*.................................................................................................................*/
	public void echoParametersToFile(StringBuffer logBuffer) {
		loglnEchoToStringBuffer("Using primers database\n", logBuffer);
	}

	/*.................................................................................................................*/

	public String getGeneFragmentName(String ID) {
		if (primers!=null) {
			return primers.getFragmentName(ID);
		}
		return null;
	}

	public String getPrimerName(String ID) {
		return ID;
	}

	public int getPrimerSequenceLength(String ID) {
		if (primers!=null) {
			String s= primers.getSequence(ID);
			if (s!=null)
				return s.length();
		}
		return 0;
	}

	public String getPrimerSequenceString(String ID) {
		if (primers!=null) {
			return primers.getSequence(ID);
		}
		return null;
	}
	
 	// returns array of all primer sequences
 	public  String[][] getPrimerSequences(){
		if (primers!=null) {
			return primers.getAllSequences();
		}
		return null;
 	}

 	// returns array of all primer sequences that correspond to the given gene fragment name (ignoring case)
 	public  String[][] getPrimerSequences(String geneFragmentName){
		if (primers!=null) {
			return primers.getAllSequences(geneFragmentName);
		}
		return null;
 	}


	public boolean isForward(String ID) {
		if (primers!=null) {
			return primers.isForward(ID);
		}
		return false;
	}

	public boolean isReady() {
		return databaseURLSource!=null;
	}

	/*.................................................................................................................*/

	public String getName() {
		return "Primer Information from Database";
	}
	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides primer information from a database.";
	}

	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){return new PIFD();}

}


class PIFD extends CompatibilityTest {
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		
		if (MesquiteTrunk.mesquiteTrunk.numModulesAvailable(DNADatabaseURLSource.class)<=0)
			return false;
		
		return true;
	}
}



