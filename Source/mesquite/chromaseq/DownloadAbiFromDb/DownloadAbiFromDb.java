package mesquite.chromaseq.DownloadAbiFromDb;

import mesquite.chromaseq.lib.AbiDownloader;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.duties.UtilitiesAssistant;
import mesquite.molec.lib.DNADatabaseURLSource;

public class DownloadAbiFromDb extends UtilitiesAssistant {
	private static final String COMMAND_NAME = "downloadAbiFromDb";
	private static final String EXPLANATION = "Download abi files from a database and process them.";

	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem(null, "Process Chromatograms from Database...", makeCommand(
				COMMAND_NAME, this));
		return true;
	}

	/* ................................................................................................................. */
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice() {
		return true;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return true;
	}

	/* ................................................................................................................. */
	public boolean isSubstantive() {
		return false;
	}

	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments,
			 CommandChecker checker) {
		if (checker.compare(this.getClass(),
				EXPLANATION, null,
				commandName, COMMAND_NAME)) {
			AbiDownloader downloader = (AbiDownloader) hireEmployee(
					AbiDownloader.class, "Abi Downloader");
			if (downloader != null) {
				downloader.downloadAbiFilesFromDb();
			} else {
				MesquiteMessage.warnProgrammer("Can't find ABI downloader module.");
			}
			fireEmployee(downloader);
		} else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}

	/* ................................................................................................................. */
	public String getName() {
		return EXPLANATION;
	}

	/* ................................................................................................................. */
	public boolean showCitation() {
		return false;
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return EXPLANATION;
	}
	/*.................................................................................................................*/

	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){return new DAFD();}

}

class DAFD extends CompatibilityTest {
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		
		if (MesquiteTrunk.mesquiteTrunk.numModulesAvailable(AbiDownloader.class)<=0)
			return false;
		
		return true;
	}
}

