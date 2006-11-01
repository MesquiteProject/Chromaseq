package mesquite.chromaseq.AbiUploaderImpl;

import java.io.File;

import mesquite.chromaseq.lib.AbiUploader;
import mesquite.chromaseq.lib.ChromFileNameDialog;
import mesquite.chromaseq.lib.ChromFileNameParsing;
import mesquite.chromaseq.lib.NameParserManager;
import mesquite.chromaseq.lib.SequenceUploader;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;

public class AbiUploaderImpl extends AbiUploader {
	private NameParserManager nameParserManager;
	private ChromFileNameParsing nameParsingRule;	
	
	public Class getDutyClass() {
		return AbiUploaderImpl.class;
	}
	public String getName() {
		return "Abi Uploader";
	}
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		if (nameParserManager == null) {
			nameParserManager = (NameParserManager)MesquiteTrunk.mesquiteTrunk.findEmployeeWithName("#ChromFileNameParsManager");
		}
		if (nameParserManager != null) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * loops through a user-specified directory on the filesystem
	 * and uploads abi files to a database url
	 */
	public boolean uploadAbiFilesToDb() {
		if (nameParserManager!=null) {
			//nameParsingRule = nameParserManager.chooseNameParsingRules(nameParsingRule);
			if (!queryNames()) {
				// they cancelled, so don't upload
				return false;
			}
		}
		String directoryPath = MesquiteFile.chooseDirectory("Choose directory containing ABI files:", null);
		
		if (StringUtil.blank(directoryPath)) {
			return false;		
		} else {
			File directory = new File(directoryPath);
			if (directory.exists() && directory.isDirectory()) {
				File[] files = directory.listFiles();				
				if (files == null || files.length == 0) {
					MesquiteMessage.warnUser("There are no files in the selected directory.");
				} else {
					StringBuffer logBuffer = new StringBuffer();
					SequenceUploader uploader = new SequenceUploader();					
					for (int i = 0; i < files.length; i++) {
						File nextAbi = files[i];
						MesquiteString sampleCodeSuffix = new MesquiteString();
						MesquiteString sampleCode = new MesquiteString();
						MesquiteString primerName = new MesquiteString();
						MesquiteString dnaCodeResult = new MesquiteString();
						//here's where the names parser processes the name
						if (nameParsingRule!=null && nextAbi != null && !nextAbi.isDirectory()
								&& nextAbi.exists()) {
							if (!nameParsingRule.parseFileName(this, nextAbi.getName(), sampleCode, sampleCodeSuffix, primerName, logBuffer, dnaCodeResult)) {
								MesquiteMessage.warnUser("Can't upload file: " + nextAbi + " to database because it doesn't match the naming rule.");
							} else {
								MesquiteMessage.warnUser("Going to upload file: " + nextAbi + " to server.");
								String totalCode = dnaCodeResult + sampleCode.toString();
								// totalCode -- eg. DNA1200 or BP1502
								uploader.uploadAB1ToServer(totalCode, null, nextAbi);
							}
						}
					}
				}
			} else {
				MesquiteMessage.warnUser("The directory path: " + directoryPath + " is not valid.");
			}
			return true;
		}
	}
	
	private boolean queryNames() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ChromFileNameDialog.CANCEL);
		ChromFileNameDialog dialog = new ChromFileNameDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), 
				"Upload ABI Options", buttonPressed, nameParserManager, "");  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.completeAndShowDialog(true);
		nameParsingRule = dialog.getNameParsingRule();
		boolean success=(buttonPressed.getValue()== ChromFileNameDialog.OK);
		return success;
	}
}
