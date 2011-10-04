/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.
Version 0.980   July 2010
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

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
				return true;
			}
		}
		return false;
	}


}
