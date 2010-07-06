/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.Version 0.980   July 2010Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.ExportFastaDNAForGenBank;/*~~  */import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.categ.lib.*;import mesquite.chromaseq.lib.ChromaseqUtil;import mesquite.chromaseq.lib.VoucherInfo;import mesquite.chromaseq.lib.VoucherInfoCoord;import mesquite.io.lib.*;/* ============  a file interpreter for DNA/RNA  Fasta files ============*/public class ExportFastaDNAForGenBank extends InterpretFasta {	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed		EmployeeNeed e = registerEmployeeNeed(VoucherInfoCoord.class, "Voucher information is needed for FASTA export for Genbank submissions.",				"This is activated automatically when you choose this exporter.");	}	VoucherInfoCoord voucherInfoTask;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		voucherInfoTask = (VoucherInfoCoord)hireEmployee(VoucherInfoCoord.class, null);		return voucherInfoTask != null && super.startJob(arguments, condition, hiredByName);	}	/*.................................................................................................................*/	public boolean canImport() {  		 return false;  //	}	public boolean canImport(String arguments){		return false;	}		protected String addendum = "";	protected String codeLabel = "DNAVoucher";	protected boolean addVoucherNumberToDescription = false;	/*.................................................................................................................*/	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export FASTA for GenBank Options", buttonPressed);		exportDialog.appendToHelpString("Choose the options for exporting the matrix as a FASTA file prepared for processing by NCBI's Sequin.");		exportDialog.appendToHelpString("<br><br><b>SeqID Suffix</b>: this will be added to each taxon name to form the unique SeqID.");		exportDialog.appendToHelpString("<br><b>Description of gene fragment</b>: this will be added to each sequence's DEFINITION.");		exportDialog.appendToHelpString("<br><b>Text before Voucher ID Code in DEFINITION</b>: this will inserted between the organism name and the Voucher ID Code in the DEFINITION.");				SingleLineTextField uniqueSuffixField = exportDialog.addTextField("SeqID Suffix", "", 20);		TextArea fsText =null;		exportDialog.addLabel("Description of gene fragment:",Label.LEFT);		fsText =exportDialog.addTextAreaSmallFont(addendum,4);		Checkbox addVoucherNumberBox = exportDialog.addCheckBox("add Voucher ID Code to DEFINITION", addVoucherNumberToDescription);		SingleLineTextField codeLabelField = exportDialog.addTextField("Text before Voucher ID Code in DEFINITION", codeLabel, 20);		Checkbox includeGapsCheckBox = exportDialog.addCheckBox("include gaps", includeGaps);				exportDialog.completeAndShowDialog(dataSelected, taxaSelected);		addendum = fsText.getText();		codeLabel = codeLabelField.getText();		uniqueSuffix = uniqueSuffixField.getText();		addVoucherNumberToDescription = addVoucherNumberBox.getState();		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);		includeGaps = includeGapsCheckBox.getState();				exportDialog.dispose();		return ok;	}		/*.................................................................................................................*	protected void saveExtraFiles(CharacterData data){		if (data instanceof DNAData) {			Arguments args = new Arguments(new Parser(arguments), true);			boolean usePrevious = args.parameterExists("usePrevious");			CharacterData data = findDataToExport(file, arguments);			if (data ==null) {				showLogWindow(true);				logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");				return;			}			Taxa taxa = data.getTaxa();			if (!MesquiteThread.isScripting() && !usePrevious)				if (!getExportOptions(data.anySelected(), taxa.anySelected()))					return;			int numTaxa = taxa.getNumTaxa();			int numChars = data.getNumChars();			StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));			int counter = 1;			for (int it = 0; it<numTaxa; it++){				if (!writeOnlySelectedTaxa || (taxa.getSelected(it))){					// TO DO: also have the option of only writing taxa with data in them					counter = 1;					outputBuffer.append(">");					outputBuffer.append(getTaxonName(taxa,it));					outputBuffer.append(getSupplementForTaxon(taxa, it));					outputBuffer.append(getLineEnding());					for (int ic = 0; ic<numChars; ic++) {						if (!writeOnlySelectedData || (data.getSelected(ic))){							int currentSize = outputBuffer.length();							if (includeGaps || (!data.isInapplicable(ic,it))) {								data.statesIntoStringBuffer(ic, it, outputBuffer, false);								counter ++;							}							if (outputBuffer.length()-currentSize>1) {								alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");								return;							}							if ((counter % 50 == 1) && (counter > 1)) {    // modulo								outputBuffer.append(getLineEnding());							}						}					}					outputBuffer.append(getLineEnding());				}			}			saveExportedFileWithExtension(outputBuffer, arguments, "txt");		}	}	/*.................................................................................................................*/	public boolean canExportEver() {  		 return true;  //	}/*.................................................................................................................*/	public boolean canExportProject(MesquiteProject project) {  		 return project.getNumberCharMatrices(DNAState.class) > 0;  //	}/*.................................................................................................................*/	public boolean canExportData(Class dataClass) {  		return (dataClass==DNAState.class);	}/*.................................................................................................................*/	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  		 return charTask.newCharacterData(taxa, 0, DNAData.DATATYPENAME);  //	}	protected String getSupplementForTaxon(Taxa taxa, int it){		if (taxa!=null && voucherInfoTask != null) {			String s = " ";			String voucherID = ChromaseqUtil.getStringAssociated(taxa, ChromaseqUtil.voucherCodeRef, it);			VoucherInfo vi= voucherInfoTask.getVoucherInfo(ChromaseqUtil.getStringAssociated(taxa, ChromaseqUtil.voucherDBRef, it), voucherID);			if (vi != null) {				s += vi.toGenBankString();				if (addVoucherNumberToDescription)					return s + " " + codeLabel + " " + voucherID + " " + addendum;				else					return s + " " + addendum;			}		}		return null; 	}	protected String getTaxonName(Taxa taxa, int it){		return StringUtil.cleanseStringOfFancyChars(taxa.getTaxonName(it)+uniqueSuffix,false,true);	}/*.................................................................................................................*/	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 		return getProject().chooseData(containerOfModule(), file, null, DNAState.class, "Select data to export");	}/*.................................................................................................................*/	public void setFastaState(CharacterData data, int ic, int it, char c) {    		if ((c=='U')||(c=='u')) {   			((DNAData)data).setDisplayAsRNA(true);   		}   		((DNAData)data).setState(ic,it,c);	}	/*.................................................................................................................*/	public  String getUnassignedSymbol(){		return "N";	}							/*.................................................................................................................*/    	 public String getName() {		return "FASTA (DNA/RNA) for GenBank Deposition";   	 }/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Exports FASTA files for GenBank deposition." ;   	 }   	 }	