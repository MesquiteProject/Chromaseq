/* Mesquite chromaseq source code.  Copyright 2005-2007 D. Maddison and W. Maddison.
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
	
	
	public void addBasesToMasterRegistry(int starting, int num) {
		int start = starting+1;
		if (start<masterRegistryOfLinkedBases.length){ // we are adding before the end; therefore go through and change values in linked
			for (int i = start; i<masterRegistryOfLinkedBases.length; i++) {
				if (MesquiteInteger.isCombinable(masterRegistryOfLinkedBases[i]) && masterRegistryOfLinkedBases[i]<linkedRegistryOfMasterBases.length)
					linkedRegistryOfMasterBases[masterRegistryOfLinkedBases[i]] = i+num;
			}
		}
		masterRegistryOfLinkedBases = IntegerArray.addParts(masterRegistryOfLinkedBases, starting, num);  // this adds the physical spots in the array - they will start out with unassigned
	}
	
	
	public void copyRegistry(RegistryMapper theRegistry){
		masterRegistryOfLinkedBases = new int[theRegistry.getMasterLength()];
		for (int i = 0; i<theRegistry.getMasterLength(); i++)
			masterRegistryOfLinkedBases[i] = theRegistry.getLinkedBaseAtMasterBaseRegistryPosition(i);
		linkedRegistryOfMasterBases = new int[theRegistry.getLinkedLength()];
		for (int i = 0; i<theRegistry.getLinkedLength(); i++)
			linkedRegistryOfMasterBases[i] = theRegistry.getMasterBaseAtLinkedBaseRegistryPosition(i);
	}
	
	public void trimLinked(int numTrimmedFromStart, int numTrimmedFromEnd){
		if (numTrimmedFromStart<=0 && numTrimmedFromEnd<=0)
			return;
		if (numTrimmedFromEnd>0) {
	//		int i1 = getMasterBaseAtLinkedBaseRegistryPosition(getLinkedLength()-numTrimmedFromEnd);
	//		int i2 = getMasterBaseAtLinkedBaseRegistryPosition(getLinkedLength()-1);
			for (int i = getMasterBaseAtLinkedBaseRegistryPosition(getLinkedLength()-numTrimmedFromEnd); i<=getMasterBaseAtLinkedBaseRegistryPosition(getLinkedLength()-1) && i<masterRegistryOfLinkedBases.length; i++)
				if (i>=0)
					masterRegistryOfLinkedBases[i] = MesquiteInteger.unassigned;
		}
		if (numTrimmedFromStart>0) {
			for (int i = 0; i<=getMasterBaseAtLinkedBaseRegistryPosition(numTrimmedFromStart-1); i++)
				masterRegistryOfLinkedBases[i] = MesquiteInteger.unassigned;
			for (int i = 0; i<getMasterLength(); i++)
				if (MesquiteInteger.isCombinable(masterRegistryOfLinkedBases[i]))
					masterRegistryOfLinkedBases[i] -=numTrimmedFromStart;   // trimming from the start means that in the master list these bases are now shifted away  from the start more.
			for (int i = 0; i<getLinkedLength(); i++)
				if (MesquiteInteger.isCombinable(linkedRegistryOfMasterBases[i]))
					linkedRegistryOfMasterBases[i] +=numTrimmedFromStart;   // trimming from the start means that in the master list these bases are now shifted away  from the start more.
		}
		linkedRegistryOfMasterBases = IntegerArray.deleteParts(linkedRegistryOfMasterBases, getLinkedLength()- (numTrimmedFromStart+numTrimmedFromEnd), numTrimmedFromStart+numTrimmedFromEnd);  
	}
	
	
	public void initRegistry(int lengthMaster, int lengthLinked){
		masterRegistryOfLinkedBases = new int[lengthMaster];
		for (int i = 0; i<lengthMaster; i++)
			masterRegistryOfLinkedBases[i] = MesquiteInteger.unassigned;
		linkedRegistryOfMasterBases = new int[lengthLinked];
		for (int i = 0; i<lengthLinked; i++)
			linkedRegistryOfMasterBases[i] = MesquiteInteger.unassigned;
	}
	
	/** For position "linkedBasePosition" in the linkedRegistry, sets the value to be the master base to which that corresponds */
	public void setMasterBaseAtLinkedBaseRegistryPosition(int linkedBasePosition, int masterBase) {
		if (linkedBasePosition>=0 && linkedBasePosition<linkedRegistryOfMasterBases.length)
			linkedRegistryOfMasterBases[linkedBasePosition] = masterBase;
	}
	/** Returns the position in the masterRegistry of position i in the linkedRegistry */
	public int getMasterBaseAtLinkedBaseRegistryPosition(int linkedBasePosition) {
		if (linkedBasePosition>=0 && linkedBasePosition<linkedRegistryOfMasterBases.length)
			return linkedRegistryOfMasterBases[linkedBasePosition];
		return MesquiteInteger.unassigned;
	}
	
	/** Sets the position in the linkedRegistry of position masterBase in the masterRegistry */
	public void setLinkedBaseAtMasterBaseRegistryPosition(int masterBasePosition, int linkedBase) {
		if (masterBasePosition>=0 && masterBasePosition<masterRegistryOfLinkedBases.length)
			masterRegistryOfLinkedBases[masterBasePosition] = linkedBase;
	}
	/** Returns the position in the linkedRegistry of position i in the masterRegistry */
	public int getLinkedBaseAtMasterBaseRegistryPosition(int masterBasePosition) {
		if (masterBasePosition>=0 && masterBasePosition<masterRegistryOfLinkedBases.length)
			return masterRegistryOfLinkedBases[masterBasePosition];
		return MesquiteInteger.unassigned;
	}
	
	
	/*....................................................................................*/
	public int getLinkedLength() {
		return linkedRegistryOfMasterBases.length;
	}
	/*....................................................................................*/
	public int getMasterLength() {
		return masterRegistryOfLinkedBases.length;
	}
	
	/*....................................................................................*/
	public String getName() {
		return name;
	}
	/*....................................................................................*/
	public void report() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName() + "\n");
		int maxLength = MesquiteInteger.maximum(masterRegistryOfLinkedBases.length, linkedRegistryOfMasterBases.length);
		sb.append("i\tM\tL\n");
		for (int i=0; i< maxLength; i++) {
			sb.append("" + i + "\t");
			if (i<masterRegistryOfLinkedBases.length)
				if (!MesquiteInteger.isCombinable(masterRegistryOfLinkedBases[i])){
					if (MesquiteInteger.isUnassigned(masterRegistryOfLinkedBases[i]))
						sb.append("*");
					else
						sb.append("еее");
				}
				else
					sb.append("" + masterRegistryOfLinkedBases[i]);
			sb.append("\t");
			if (i<linkedRegistryOfMasterBases.length)
				if (!MesquiteInteger.isCombinable(linkedRegistryOfMasterBases[i])) {
					if (MesquiteInteger.isUnassigned(linkedRegistryOfMasterBases[i]))
						sb.append("*");
					else
						sb.append("еее");
				}
				else
					sb.append("" + linkedRegistryOfMasterBases[i]);
			sb.append("\n");
		}
		MesquiteMessage.println(sb.toString());
	}
	
}
