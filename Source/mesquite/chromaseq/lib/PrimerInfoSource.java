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

package mesquite.chromaseq.lib;

import org.dom4j.Element;

import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteSubmenuSpec;

public abstract class PrimerInfoSource extends MesquiteModule {

	public Class getDutyClass() {
		return PrimerInfoSource.class;
	}

 	public String getDutyName() {
 		return "Primer Information Source";
   	 }

 // returns whether primer is in forward direction
 	public abstract boolean isForward(String ID);

 	//returns name of primer as a string
 	public abstract String getPrimerName(String ID);

 	//returns sequence of primer as a string
 	public abstract String getPrimerSequenceString(String ID);

 	// returns length of primer sequence
 	public abstract int getPrimerSequenceLength(String ID);
 	

 	// returns array of all primer sequences, 
 	// [numPrimers][2], with [i][0] containing the primer name of the i'th primer, and [i][0] containing the primer sequence
 	public abstract String[][] getPrimerSequences();

 	// returns array of all primer sequences that correspond to the given gene fragment name (ignoring case), 
 	// [numPrimers][2], with [i][0] containing the primer name of the i'th primer, and [i][0] containing the primer sequence
 	public abstract String[][] getPrimerSequences(String geneFragmentName);


 	// returns gene fragment name
 	public abstract String getGeneFragmentName(String ID);

 	//returns whether source has all relevant data supplied (e.g. address of database, etc.)
 	public abstract boolean isReady();

 	//adds whatever parameter information it wishes to logBuffer so that that text will appear in the log
	public abstract void echoParametersToFile(StringBuffer logBuffer);
	
	/*.................................................................................................................*/
	public  void addXMLAttributes(Element element){
	}
	
	/*.................................................................................................................*/
	public  void addMenuItemsForPrimerSubmenu(MesquiteSubmenuSpec primerSubmenu){
	}
	/*.................................................................................................................*/
	public  void removeMenuItemsFromPrimerSubmenu(MesquiteSubmenuSpec primerSubmenu){
	}


}
