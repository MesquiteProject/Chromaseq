/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.chromaseq.ReadWGenBankNumbersTable;

import java.util.Vector;

import mesquite.chromaseq.lib.GenBankNumbersFileReader;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;



/* ======================================================================== */
public class ReadWGenBankNumbersTable extends GenBankNumbersFileReader  {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
 
   	public String exampleFile(){
   		return "<tt>id&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;28s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;COI&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>"
   				+ "voucher123&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;EF201661&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>"
   				+"voucher116&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DQ665754<br>"
			+"xzTY773&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;XN998351&nbsp;&nbsp;&nbsp;&nbsp;YY101012</tt>";
  	}

 
   	/** Called to read the text file, and return an n x 3 String array, with each row as follows
   	 * column 0 -- voucher ID or taxon name. (System will look to match voucherID, but then use taxon name otherwise)
   	 * column 1 -- gene name
   	 * column 2 -- GenBank number(s)*/
   	public String[][] readTable(String pathToFile){
		String[][] table = MesquiteFile.getTabDelimitedTextFile(pathToFile);
		if (table == null)
			return null;
		int idColumn = -1;
		Vector records = new Vector();
		for (int i = 0; i< table[0].length; i++){
			String columnName = table[0][i];
			if (columnName != null && columnName.equalsIgnoreCase("id"))
					idColumn = i;
		}
		if (idColumn<0)
			return null;
		for (int column = 0; column< table[0].length; column++){
			String columnName = table[0][column];
			String geneName = null;
			if (columnName != null){
				if (columnName.equalsIgnoreCase("id"))
					idColumn = column;
				else {
					CharacterData data = getProject().getCharacterMatrixReverseOrder((MesquiteFile)null, null, null, columnName);
					if (data != null) {
						geneName = columnName;
					}
				}
			}
			if (geneName != null){
				//we've found a column that represents a gene matrix in the datafile; now read all the non-blank things in the column
				for (int row = 1; row<table.length; row++){
					if (column < table[row].length && !StringUtil.blank(table[row][column])){ //there's an entry!
						String[] record = new String[3];
						record[ID] = table[row][idColumn];  //the voucher ID
						record[GENE] = geneName;  //the gene name
						record[GENBANK] = table[row][column];  //the GenBank number(s)
						records.addElement(record);
					}
				}
			}
		}
		if (records.size()>0){
			String[][] result = new String[records.size()][];
			for (int i = 0; i<records.size(); i++)
				result[i] = (String[])records.elementAt(i);
			return result;
		}
		
 		return null;
   	}

	public String getName() {
		return "Columns for genes, rows for taxa";
	}
	
	public String getExplanation() {
		return "Reads GenBank numbers from a simple tab-delimited table as a text file, with first row being column names," +
	" and subsequent rows being one for each specimen/taxon.\n—There must be a column with heading \"id\" for specimen/taxon ID. "
				+"These taxon IDs must match either a taxon's name in the data file, or its taxon ID code (as in the Taxon ID Code column of the List of Taxa window)."
				+ "\n—Each different gene locus gets a separate column, whose heading must be the name of the gene."
				+" Entries in gene columns are that gene's GenBank number in row's taxon.\nThe columns must be separated by TABs!!";
	}

}

