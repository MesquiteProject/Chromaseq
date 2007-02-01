package mesquite.chromaseq.lib;

import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;

public abstract class AbiDownloader extends MesquiteModule {
	public Class getDutyClass() {
		return AbiDownloader.class;
	}
	public String getDutyName() {
		return "Abi Downloader";
	}
	public abstract boolean downloadAbiFilesFromDb(CommandRecord record);
	public abstract boolean downloadAbiFilesFromDb(CommandRecord commandRec, MesquiteProject project);
}
