/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.chromaseq.ReadDGenBankNumbersTable;

import mesquite.chromaseq.lib.GenBankNumbersFileReader;
import mesquite.lib.MesquiteFile;



/* ======================================================================== */
public class ReadDGenBankNumbersTable extends GenBankNumbersFileReader  {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
 
	public boolean isPrerelease(){
		return false;
	}
   	public String exampleFile(){
   		return "<tt>voucher123&nbsp;&nbsp;&nbsp;28S&nbsp;&nbsp;&nbsp;EF201661<br>voucher116&nbsp;&nbsp;&nbsp;COI&nbsp;&nbsp;&nbsp;DQ665754"
   				+"<br>xzTY773&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;28S&nbsp;&nbsp;&nbsp;XN998351"
   				+"<br>xzTY773&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;COI&nbsp;&nbsp;&nbsp;YY101012</tt>";
  	}

 
   	/** Called to read the text file, and return an n x 3 String array, with each row as follows
   	 * column 0 -- voucher ID or taxon name. (System will look to match voucherID, but then use taxon name otherwise)
   	 * column 1 -- gene name
   	 * column 2 -- GenBank number(s)*/
   	public String[][] readTable(String pathToFile){
		String[][] table = MesquiteFile.getTabDelimitedTextFile(pathToFile);
		return table;
   	}

	public String getName() {
		return "Three columns: Specimen ID, gene, GenBank number";
	}
	
	public String getExplanation() {
		return "Reads GenBank numbers from simple tab-delimited text file, each row of which pertains to the GenBank number in a sample/specimen for a single gene. There must be three columns,"
				+ " separated by tabs: (1) taxon ID code, (2) the name of the gene/matrix, and (3) the GenBank number. The columns must be separated by TABs!! "
				+"The taxon ID code must match the unique code for the sample that appears in the Taxon ID Code column of the List of Taxa window.";
	}

}

