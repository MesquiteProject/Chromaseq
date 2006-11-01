package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteModule;

public abstract class AbiUploader extends MesquiteModule {
	public Class getDutyClass() {
		return AbiUploader.class;
	}
	public String getDutyName() {
		return "Abi Uploader";
	}
	public abstract boolean uploadAbiFilesToDb();
}
