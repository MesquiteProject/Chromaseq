/* Mesquite (package mesquite.io).  Copyright 2000-2006 D. Maddison and W. Maddison. Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.ExportFastaDNAForGenBank;/*~~  */import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.categ.lib.*;import mesquite.chromaseq.lib.VoucherInfo;import mesquite.chromaseq.lib.VoucherInfoCoord;import mesquite.io.lib.*;/* ============  a file interpreter for DNA/RNA  Fasta files ============*/public class ExportFastaDNAForGenBank extends InterpretFasta {	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed		EmployeeNeed e = registerEmployeeNeed(VoucherInfoCoord.class, "Voucher information is needed for FASTA export for Genbank submissions.",				"This is activated automatically when you choose this exporter.");	}	NameReference anr = NameReference.getNameReference("VoucherCode");		NameReference vdb = NameReference.getNameReference("VoucherDB");	VoucherInfoCoord voucherInfoTask;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		voucherInfoTask = (VoucherInfoCoord)hireEmployee(commandRec, VoucherInfoCoord.class, null);		return voucherInfoTask != null && super.startJob(arguments, condition, commandRec, hiredByName);	}	/*.................................................................................................................*/	public boolean canImport() {  		 return false;  //	}	public boolean canImport(String arguments){		return false;	}		/*.................................................................................................................*/	public boolean canExportEver() {  		 return true;  //	}/*.................................................................................................................*/	public boolean canExportProject(MesquiteProject project) {  		 return project.getNumberCharMatrices(DNAState.class) > 0;  //	}/*.................................................................................................................*/	public boolean canExportData(Class dataClass) {  		return (dataClass==DNAState.class);	}/*.................................................................................................................*/	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  		 return charTask.newCharacterData(taxa, 0, "DNA Data");  //	}	protected String getSupplementForTaxon(Taxa taxa, int it){		if (taxa!=null && voucherInfoTask != null) {			VoucherInfo vi= voucherInfoTask.getVoucherInfo((String)taxa.getAssociatedObject(vdb, it), (String)taxa.getAssociatedObject(anr, it));			if (vi != null)				return " " + vi.toGenBankString();		}		return null; 	}/*.................................................................................................................*/	public CharacterData findDataToExport(MesquiteFile file, String arguments, CommandRecord commandRec) { 		return getProject().chooseData(containerOfModule(), file, null, DNAState.class, "Select data to export", commandRec);	}/*.................................................................................................................*/	public void setFastaState(CharacterData data, int ic, int it, char c) {    		if ((c=='U')||(c=='u')) {   			((DNAData)data).setDisplayAsRNA(true);   		}   		((DNAData)data).setState(ic,it,c);	}							/*.................................................................................................................*/    	 public String getName() {		return "FASTA (DNA/RNA) for GenBank Deposition";   	 }/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Exports FASTA files for GenBank deposition." ;   	 }   	 }	