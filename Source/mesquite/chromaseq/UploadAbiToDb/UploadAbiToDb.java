package mesquite.chromaseq.UploadAbiToDb;

import mesquite.chromaseq.lib.AbiUploader;
import mesquite.chromaseq.lib.PhPhRunner;
import mesquite.lib.*;
import mesquite.lib.duties.UtilitiesAssistant;

public class UploadAbiToDb extends UtilitiesAssistant {
	private static final String COMMAND_NAME = "uploadAbiToDb";

	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
		addMenuItem(null, "Upload ABI files to database...", makeCommand(COMMAND_NAME, this));
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
	public Object doCommand(String commandName, String arguments,CommandChecker checker) {
		if (checker.compare(this.getClass(), "Uploads a folder of abi files to a database.", null, commandName, COMMAND_NAME)) {
			//String databaseURL = MesquiteString.queryShortString(null, "Database URL, message, current);
			AbiUploader uploader = (AbiUploader) hireEmployee(AbiUploader.class, "Abi Uploader");
			if (uploader != null) {
				uploader.uploadAbiFilesToDb();
			} else {
				MesquiteMessage.warnProgrammer("Can't find ABI uploader module.");
			}
		} else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Upload ABI files to database";
	}

	/* ................................................................................................................. */
	public boolean showCitation() {
		return false;
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return "Uploads a folder of abi files to a database.";
	}
}
