package mesquite.chromaseq.DropChromatogramsOnMatrix;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowAssistantI;
import mesquite.lib.table.MesquiteTable;
import java.io.*;

public class DropChromatogramsOnMatrix extends DataWindowAssistantI implements MesquiteDropListener{
	MesquiteTable table;
	CharacterData data;

	public void setTableAndData(MesquiteTable table, CharacterData data) {
		this.table = table;
		this.data = data;
		table.addDropListener(this);
	}

	public String getName() {
		return "Drop Chromatograms On Matrix";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*...............................................................................................................*/

	public boolean processDroppedObject(Object obj) {
		if (obj instanceof File) {
			File file = (File)obj;
			String name = file.getName();
			name = name.toLowerCase();
			if (name.endsWith(".ab1")) {
Debugg.println("Chromatogram " + name + " dropped on matrix.");
				return true;
			}
		}
		return false;
	}


}
