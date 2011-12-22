package mesquite.chromaseq.AccumulateChromatograms;

import java.io.File;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

public class AccumulateChromatograms extends UtilitiesAssistant {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		addMenuItem(null, "Accumulate Chromatograms...", makeCommand("accumulate", this));
		return true;
	}

	/*.................................................................................................................*/
	public boolean accumulateChromatograms( File directory, String directoryPath){
		String[] files = directory.list();
		int count = 0;
		for (int i=0; i<files.length; i++) { // going through the folders and finding the ace files
			if (files[i]!=null ) {
				String filePath = directoryPath + MesquiteFile.fileSeparator + files[i];
				String infoFilePath = directoryPath + MesquiteFile.fileSeparator + ChromaseqUtil.infoFileName;
				File cFile = new File(filePath);
				if (cFile.exists()) {
					if (cFile.isDirectory()) {
						accumulateChromatograms(cFile, filePath);
					}
					else {
						if ((files[i].endsWith(".ab1")||(files[i].endsWith(".ab1")))  && !files[i].startsWith(".") && files[i].contains("_") && !files[i].contains(".b.") && !files[i].contains(".g.") && files[i].contains("DNA")) {
							count++;
							if (count>100) return false;
							Debugg.println(files[i]);
							
						}
					}
				}
			}
		}
		return false;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Segregates into a new folder all chromatograms whose sequence names and gene fragment names contain a particular string.", null, commandName, "accumulate")) {

			String directoryPath = MesquiteFile.chooseDirectory("Choose directory containing ABI files:", null); 
			if (StringUtil.blank(directoryPath))
				return null;
			File directory = new File(directoryPath);
			accumulateChromatograms(directory, directoryPath);

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Accumulate Chromatograms";
	}

}
