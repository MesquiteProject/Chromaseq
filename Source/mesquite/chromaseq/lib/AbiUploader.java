package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteXMLPreferencesModule;

public abstract class AbiUploader extends MesquiteXMLPreferencesModule {
	public Class getDutyClass() {
		return AbiUploader.class;
	}
	public String getDutyName() {
		return "Abi Uploader";
	}
	public abstract boolean uploadAbiFilesToDb();
}
