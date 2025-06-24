/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteModule;



/* ======================================================================== */
public abstract class GenBankNumbersFileReader extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return GenBankNumbersFileReader.class;
   	 }
 	public String getDutyName() {
 		return "GenBank Numbers File Reader";
   	 }

 	public static int ID = 0;
 	public static 	int GENE = 1;
 	public static 	int GENBANK = 2;
   	/** Called to read the text file, and return an n x 3 String array, with
   	 * column 0 -- voucher ID or taxon name. (System will look to match voucherID, but then use taxon name otherwise)
   	 * column 1 -- gene name
   	 * column 2 -- GenBank number(s)*/
   	public abstract String[][] readTable(String pathToFile);

   	/* Return an example file for display as monospace, so use spaces to line up columns etc. */
   	public abstract String exampleFile();
}

