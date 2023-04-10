
package mesquite.chromaseq.AbiUploaderImpl;

import java.awt.Label;
import java.awt.TextArea;
import java.io.File;

import mesquite.chromaseq.ChromaseqAuthorDefaults.ChromaseqAuthorDefaults;
import mesquite.chromaseq.SampleAndPrimerFileNameParser.ChromFileNameDialog;
import mesquite.chromaseq.SampleAndPrimerFileNameParser.ChromFileNameParsing;
import mesquite.chromaseq.lib.AbiUploader;
import mesquite.chromaseq.lib.ChromatogramFileNameParser;
import mesquite.chromaseq.lib.SequenceUploader;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.EmployerEmployee;
import mesquite.lib.ExtensibleDialog;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.SingleLineTextField;
import mesquite.lib.StringUtil;
import mesquite.molec.lib.DNADatabaseURLSource;

/**
 * Class that uploads a directory of abi files up to a server
 * @author dmandel
 *
 */
public class AbiUploaderImpl extends AbiUploader {
	private ChromatogramFileNameParser nameParserManager;
	private ChromaseqAuthorDefaults authorDefaults;
	private String url;
	private SingleLineTextField uploadBatchNameField;
	private SingleLineTextField urlField;
	private TextArea uploadBatchDescriptionArea;
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (nameParserManager == null) {
			nameParserManager= (ChromatogramFileNameParser)MesquiteTrunk.mesquiteTrunk.hireEmployee(ChromatogramFileNameParser.class, "Manager for determining how to determine sample code and primer name.");
		}
		if (authorDefaults == null) {
			authorDefaults = (ChromaseqAuthorDefaults)MesquiteTrunk.mesquiteTrunk.findEmployeeWithName("#ChromaseqAuthorDefaults");
		}
		if (nameParserManager != null && authorDefaults != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/*.................................................................................................................*/
	public boolean loadModule(){
		return false;
	}

	
	public Class getDutyClass() {
		return AbiUploaderImpl.class;
	}
	public String getName() {
		return "Abi Uploader";
	}

	/**
	 * loops through a user-specified directory on the filesystem
	 * and uploads abi files to a database url
	 */
	public boolean uploadAbiFilesToDb(DNADatabaseURLSource databaseURLSource) {
		if (databaseURLSource==null)
			return false;
		if (nameParserManager!=null) {
			//nameParsingRule = nameParserManager.chooseNameParsingRules(nameParsingRule);
			if (!queryNames()) {
				// they cancelled, so don't upload
				return false;
			} else {
				setUrl(urlField.getText());
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
					SequenceUploader uploader = new SequenceUploader(databaseURLSource);
					
					boolean result = authorDefaults.verifyAuthorIsntDefault();
					if (!result) {
						return false;
					}
					storePreferences();
					// create the upload batch on the server					
					Long batchId = uploader.createAB1BatchOnServer(getUrl(), uploadBatchNameField.getText(), uploadBatchDescriptionArea.getText(), MesquiteModule.author.getCode());
					if (batchId == null) {
						MesquiteMessage.warnUser("Unable to create chromatogram batch on the server.  Chromatogram upload will not continue.");
						return false;
					}
					for (int i = 0; i < files.length; i++) {
						File nextAbi = files[i];
						MesquiteString sampleCodeSuffix = new MesquiteString();
						MesquiteString sampleCode = new MesquiteString();
						MesquiteString primerName = new MesquiteString();
						MesquiteString dnaCodeResult = new MesquiteString();
						//here's where the names parser processes the name
						if (nameParserManager!=null && nextAbi != null && !nextAbi.isDirectory()
								&& nextAbi.exists()) {
							if (!nameParserManager.parseFileName(nextAbi.getName(), sampleCode, sampleCodeSuffix, primerName, logBuffer, dnaCodeResult, null)) {
								MesquiteMessage.warnUser("Can't upload file: " + nextAbi + " to database because it doesn't match the naming rule.");
							} else {
								MesquiteMessage.warnUser("Going to upload file: " + nextAbi + " to server.");
								String totalCode = dnaCodeResult + sampleCode.toString();
								// totalCode -- eg. DNA1200 or BP1502
								uploader.uploadAB1ToServer(getUrl(),totalCode, null, nextAbi, batchId);
							}
						}
					}
				}
				return true;				
			} else {
				MesquiteMessage.warnUser("The directory path: " + directoryPath + " is not valid.");
				return false;
			}
		}
	}
	
	private boolean queryNames() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Upload ABI Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		urlField = dialog.addTextField("URL", getUrl(), 26);
		uploadBatchNameField = dialog.addTextField("ABI Batch Name", "", 26);
		dialog.addLabel("ABI Batch Description", Label.LEFT);
		uploadBatchDescriptionArea = dialog.addTextArea("", 4);
		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		return success;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String[] getPreferencePropertyNames() {
		return new String[] {"url"};
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){return new AUI();}

	
}



class AUI extends CompatibilityTest {
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		
		if (MesquiteTrunk.mesquiteTrunk.numModulesAvailable(DNADatabaseURLSource.class)<=0)
			return false;
		
		return true;
	}
}

