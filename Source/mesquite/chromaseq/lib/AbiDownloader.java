package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteXMLPreferencesModule;

public abstract class AbiDownloader extends MesquiteXMLPreferencesModule {
	public Class getDutyClass() {
		return AbiDownloader.class;
	}
	public String getDutyName() {
		return "Abi Downloader";
	}
	public abstract boolean downloadAbiFilesFromDb();
	public abstract boolean downloadAbiFilesFromDb( MesquiteProject project);
}
