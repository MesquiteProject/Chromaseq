/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.chromaseq.lib;

import mesquite.lib.*;

/* 
 * This class contains two arrays, as well as methods for accessing them.
 * These two arrays specify the mapping between the master registry of bases and a linked array of bases.
 */

public class RegistryMapper {
	int[] linkedRegistryOfMasterBases;  //length of linked; for each element, contains which array element within master
	int[] masterRegistryOfLinkedBases;  //length of master; for each element, contains which linked element resides there; MesquiteInteger.unassigned if none.
	//Thus, masterRegistry[linkedRegistry[i]] ==i
	
	String name="";
	
	public RegistryMapper(int lengthMaster, int lengthLinked){
		initRegistry(lengthMaster,lengthLinked);
	}
	public RegistryMapper(int lengthMaster, int lengthLinked, String name){
		this.name = name;
		initRegistry(lengthMaster,lengthLinked);
	}

	public void initRegistry(int lengthMaster, int lengthLinked){
		masterRegistryOfLinkedBases = new int[lengthMaster];
		for (int i = 0; i<lengthMaster; i++)
			masterRegistryOfLinkedBases[i] = MesquiteInteger.unassigned;
		linkedRegistryOfMasterBases = new int[lengthLinked];
		for (int i = 0; i<lengthLinked; i++)
			linkedRegistryOfMasterBases[i] = MesquiteInteger.unassigned;
	}

	/** Sets the position in the masterRegistry of position linkedBase in the linkedRegistry */
	public void setPositionInMasterRegistryOfLinkedBase(int linkedBase, int masterBase) {
		if (linkedBase>=0 && linkedBase<linkedRegistryOfMasterBases.length)
			 linkedRegistryOfMasterBases[linkedBase] = masterBase;
	}
	/** Returns the position in the masterRegistry of position i in the linkedRegistry */
	public int getPositionInMasterRegistryOfLinkedBase(int linkedBase) {
		if (linkedBase>=0 && linkedBase<linkedRegistryOfMasterBases.length)
			return linkedRegistryOfMasterBases[linkedBase];
		return MesquiteInteger.unassigned;
	}

	/** Sets the position in the linkedRegistry of position masterBase in the masterRegistry */
	public void setPositionInLinkedRegistryOfMasterBase(int masterBase, int linkedBase) {
		if (masterBase>=0 && masterBase<masterRegistryOfLinkedBases.length)
			 masterRegistryOfLinkedBases[masterBase] = linkedBase;
	}
/** Returns the position in the linkedRegistry of position i in the masterRegistry */
	public int getPositionInLinkedRegistryOfMasterBase(int masterBase) {
		if (masterBase>=0 && masterBase<masterRegistryOfLinkedBases.length)
			return masterRegistryOfLinkedBases[masterBase];
		return MesquiteInteger.unassigned;
	}

}
