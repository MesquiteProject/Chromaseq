/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.ExportSeparateSequenceFASTA;
/*~~  */

import java.awt.*;
import java.util.Vector;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.AssociationSource;
import mesquite.assoc.lib.TaxaAssociation;
import mesquite.basic.ManageTaxaPartitions.ManageTaxaPartitions;
import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.ChromaseqUtil;
import mesquite.cont.lib.ContinuousData;
import mesquite.io.lib.*;


/* ============  a file interpreter for DNA/RNA  Fasta files ============*/

public class ExportSeparateSequenceFASTA extends FileInterpreterI {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
	//	EmployeeNeed e = registerEmployeeNeed(VoucherInfoCoord.class, "Voucher information is needed for FASTA export for Genbank submissions.",
	//			"This is activated automatically when you choose this exporter.");
	}
	//VoucherInfoCoord voucherInfoTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//voucherInfoTask = (VoucherInfoCoord)hireEmployee(VoucherInfoCoord.class, null);
		return true;
	}
	public void readFile(MesquiteProject mf, MesquiteFile mNF, String arguments) {
		
	}

	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;  //
	}
	public boolean canImport(String arguments){
		return false;
	}
	/** returns whether module is able ever to export.*/
	public boolean canExportEver(){
		return true;
	}
	/** returns whether module has something it can export in the project.  Should be overridden*/
	public boolean canExportProject(MesquiteProject project){
		 return project.getNumberCharMatrices(MolecularState.class) > 0;  //
	}

	/** returns whether module can export a character data matrix of the given type.  Should be overridden*/
	public boolean canExportData(Class dataClass){
		if (dataClass==null) return false;
		return ((MolecularState.class).isAssignableFrom(dataClass)); 
	}

	protected int taxonNameLengthLimit() {
		return 15;
	}

	/* ============================  exporting ============================*/
	/*.................................................................................................................*/
	boolean convertAmbiguities = false;
	boolean useData = true;
	String addendum = "";
	String fileName = "untitled.nex";
	boolean permitMixed = false;
	boolean generateMBBlock = true;
	boolean simplifyNames = false;
	boolean addPrefixPlusVoucherID = false;
	String voucherPrefix = "DNA";

	boolean removeExcluded = false;

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Sequence-sequence FASTA export", buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		exportDialog.addLabel("Saving each sequence in a separate FASTA file");
		
		Checkbox addPrefixPlusVoucherIDBox= exportDialog.addCheckBox("Use prefix plus voucher ID as file name.", addPrefixPlusVoucherID);
		SingleLineTextField voucherPrefixField= exportDialog.addTextField("Prefix in front of voucher ID", voucherPrefix, 8);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		//		convertAmbiguities = convertToMissing.getState();
		if (ok) {
			voucherPrefix = voucherPrefixField.getText();
			addPrefixPlusVoucherID = addPrefixPlusVoucherIDBox.getState();
		}

		exportDialog.dispose();
		return ok;
	}	
	/*.................................................................................................................*/
	
	private void putFastaAsFile(Taxa taxa, int it, CharacterData data, int index, String directory, String fasta) {

		String filePath = directory;

		String voucherID = ChromaseqUtil.getStringAssociated(taxa, VoucherInfo.voucherCodeRef, it);
		if (addPrefixPlusVoucherID && StringUtil.notEmpty(voucherID))
			filePath+=StringUtil.cleanseStringOfFancyChars(voucherPrefix+voucherID,false,true);
		else 
			filePath+=StringUtil.cleanseStringOfFancyChars(taxa.getName(it),false,true);

		String s = ChromaseqUtil.getFragmentName(data, index);
		if (StringUtil.notEmpty(s)) 
			filePath += "."+StringUtil.cleanseStringOfFancyChars(s,false,true);
		else
			filePath += "."+StringUtil.cleanseStringOfFancyChars(data.getName(),false,true);
			

		filePath += ".fas";
		MesquiteFile.putFileContents(filePath, fasta, true);
	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
//		boolean usePrevious = args.parameterExists("usePrevious");

		if (!MesquiteThread.isScripting())
			if (!getExportOptions(false, false))
				return false;

		String directory = MesquiteFile.chooseDirectory("Choose directory into which files will be saved:");
		if (StringUtil.blank(directory))
			return false;
		if (!directory.endsWith(MesquiteFile.fileSeparator))
			directory+=MesquiteFile.fileSeparator;

		
		StringBuffer buffer = new StringBuffer(500);
		int count = 0;

		for (int taxaNumber=0; taxaNumber<getProject().getNumberTaxas(file); taxaNumber++) {
			Taxa taxa = (Taxa)getProject().getTaxa(file,taxaNumber);
			int numMatrices = getProject().getNumberCharMatrices(null, taxa, MolecularState.class, true);
			for (int iM = 0; iM < numMatrices; iM++){
				CharacterData data = getProject().getCharacterMatrixVisible(taxa, iM, MolecularState.class);
				if (data != null) {
					int numTaxa = taxa.getNumTaxa();
					for (int it = 0; it<numTaxa; it++) {
						buffer.setLength(0);
						buffer = ((MolecularData)data).getSequenceAsFasta(false,false,it);
						String content = buffer.toString();
						if (StringUtil.notEmpty(content)){
							putFastaAsFile(taxa, it, data, iM, directory, content);
							count++;
						}
					}
				}
			}
		}
		
		logln(""+ count + " FASTA files saved");

		return true;
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Single-sequence FASTA Files";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports each sequence as a separate FASTA file." ;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}


}
