package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteXMLPreferencesModule;
import mesquite.molec.lib.DNADatabaseURLSource;

public abstract class AbiUploader extends MesquiteXMLPreferencesModule {
	public Class getDutyClass() {
		return AbiUploader.class;
	}
	public String getDutyName() {
		return "Abi Uploader";
	}
	public abstract boolean uploadAbiFilesToDb(DNADatabaseURLSource databaseURLSource);
}
