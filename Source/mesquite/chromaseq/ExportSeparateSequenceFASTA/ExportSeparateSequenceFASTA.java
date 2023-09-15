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
import mesquite.lib.characters.CharacterData;
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
		return 50;
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
	protected boolean buildFileName = false;
	protected boolean includeTaxonName = true;
	protected String voucherPrefix = "DNA";
	String voucherSuffix = "";
	protected boolean includeGeneNameInFastaHeader = false;

	boolean removeExcluded = false;

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Single-sequence FASTA export", buttonPressed);
		exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		exportDialog.addLabel("Saving each sequence in a separate FASTA file");

		RadioButtons radios = exportDialog.addRadioButtons(new String[] {"use taxon name and gene fragment as file name", "build file name around Voucher ID and gene fragment as follows"}, 0);
		SingleLineTextField voucherPrefixField= exportDialog.addTextField("Prefix before Voucher ID", voucherPrefix, 8);
		Checkbox includeTaxonNameBox= exportDialog.addCheckBox("Include taxon name", includeTaxonName);
		SingleLineTextField voucherSuffixField= exportDialog.addTextField("Suffix (before file extension)", voucherSuffix, 8);
		Checkbox includeGeneNameBox= exportDialog.addCheckBox("Include gene name in FASTA header", includeGeneNameInFastaHeader);

		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		//		convertAmbiguities = convertToMissing.getState();
		if (ok) {
			voucherPrefix = voucherPrefixField.getText();
			buildFileName = radios.getValue()==1;
			voucherSuffix = voucherSuffixField.getText();
			includeTaxonName = includeTaxonNameBox.getState();
			includeGeneNameInFastaHeader = includeGeneNameBox.getState();
		}

		exportDialog.dispose();
		return ok;
	}	
	/*.................................................................................................................*/
	public String getFileName(Taxa taxa, int it, CharacterData data, int index, String voucherID, String identifierString) {
		String fileName = "";
		boolean prefixWithID = buildFileName && StringUtil.notEmpty(voucherID);
		if (prefixWithID)
			fileName=StringUtil.cleanseStringOfFancyChars(voucherPrefix+voucherID,false,true);
		else 
			fileName=StringUtil.cleanseStringOfFancyChars(taxa.getName(it),false,true);

		String s = ChromaseqUtil.getFragmentName(data, index);
		if (StringUtil.notEmpty(s)) 
			fileName += "_"+StringUtil.cleanseStringOfFancyChars(s,false,true);
		else
			fileName += "_"+StringUtil.cleanseStringOfFancyChars(data.getName(),false,true);

		if (buildFileName) {
			if (includeTaxonName && prefixWithID)
				fileName+="_"+StringUtil.cleanseStringOfFancyChars(taxa.getName(it),false,true);
			if (StringUtil.notEmpty(voucherSuffix))
				fileName+="_"+StringUtil.cleanseStringOfFancyChars(voucherSuffix,false,true);
		}

		fileName += ".fas";

		return fileName;
	}
	
	/*.................................................................................................................*/
	public String getTitleLineForTabbedFile() {
		return "";
	}

	/*.................................................................................................................*/
	public String getLineForTabbedFile(Taxa taxa, int it, CharacterData data, int index, String voucherID, String identifierString) {
		return "";
	}

	/*.................................................................................................................*/

	private void putFastaAsFile(Taxa taxa, int it, CharacterData data, int index, String directory, String fasta, String voucherID, String identifierString) {
		String filePath = directory;
		filePath = directory+getFileName(taxa, it, data, index, voucherID, identifierString);
		MesquiteFile.putFileContents(filePath, fasta, true);
	}
	/*.................................................................................................................*/
	public String getSequenceName(Taxa taxa, int it, CharacterData data, String voucherID) {
		String s = taxa.getTaxonName(it);
		if (includeGeneNameInFastaHeader)
			s=data.getName()+": " + s;
		return s;
	}
	/*.................................................................................................................*/
	public String getIdentifierString() {
		return "";
	}
	/*.................................................................................................................*/
	public synchronized boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		//		boolean usePrevious = args.parameterExists("usePrevious");

		if (!MesquiteThread.isScripting())
			if (!getExportOptions(false, true))
				return false;

		String directory = MesquiteFile.chooseDirectory("Choose directory into which files will be saved:");
		if (StringUtil.blank(directory))
			return false;
		if (!directory.endsWith(MesquiteFile.fileSeparator))
			directory+=MesquiteFile.fileSeparator;


		StringBuffer buffer = new StringBuffer(500);
		StringBuffer metadataBuffer = new StringBuffer(0);
		metadataBuffer.append(getTitleLineForTabbedFile());
		int count = 0;

		for (int taxaNumber=0; taxaNumber<getProject().getNumberTaxas(file); taxaNumber++) {
			Taxa taxa = (Taxa)getProject().getTaxa(file,taxaNumber);
			int numMatrices = getProject().getNumberCharMatrices(null, taxa, MolecularState.class, true);
			for (int iM = 0; iM < numMatrices; iM++){
				CharacterData data = getProject().getCharacterMatrixVisible(taxa, iM, MolecularState.class);
				if (data != null) {
					int numTaxa = taxa.getNumTaxa();
					for (int it = 0; it<numTaxa; it++) {
						if (!writeOnlySelectedTaxa || taxa.getSelected(it)){
							String voucherID = ChromaseqUtil.getStringAssociated(taxa, VoucherInfoFromOTUIDDB.voucherCodeRef, it);
							buffer.setLength(0);
							buffer = ((MolecularData)data).getSequenceAsFasta(false,false,it, getSequenceName(taxa,it,data, voucherID));
							String content=null;
							if (buffer!=null) 
								content = buffer.toString();
							else 
								buffer = new StringBuffer(500);
							if (StringUtil.notEmpty(content)){
								String idString = getIdentifierString();
								putFastaAsFile(taxa, it, data, iM, directory, content, voucherID, idString);
								String metadata = getLineForTabbedFile(taxa, it, data, iM, voucherID, idString);
								if (StringUtil.notEmpty(metadata)) 
									metadataBuffer.append(metadata);
								count++;
							}
						}
					}
				}
			}
		}
		
		if (metadataBuffer.length()>0)
			MesquiteFile.putFileContentsQuery("Choose location for metadata file", metadataBuffer.toString(), true);

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
