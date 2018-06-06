/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.Version 2.72, December 2009.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.TaxonNameRootWCode;import java.util.*;import java.awt.*;import mesquite.chromaseq.lib.ChromaseqUtil;import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;/* ======================================================================== */public class TaxonNameRootWCode extends TaxonNameAlterer {		String startOfName = "";		/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){		return true;	}	/*.................................................................................................................*/	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/	public boolean getUserChooseable(){		return true; 	}		/*.................................................................................................................*/   	public boolean getOptions(Taxa taxa, int firstSelected){   		if (MesquiteThread.isScripting())   			return true;		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Taxon name from root + OTU ID Code",  buttonPressed);		queryDialog.addLabel("Taxon name from root + OTU ID Code", Label.CENTER);		SingleLineTextField prefixField = queryDialog.addTextField("Root of name", startOfName, 12);		queryDialog.completeAndShowDialog(true);					boolean ok = (queryDialog.query()==0);				if (ok) {			startOfName = prefixField.getText();		}				queryDialog.dispose();		return ok;   	}	/*.................................................................................................................*/   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class,    	then you must supply a real method for this, not just this stub. */	public boolean alterName(Taxa taxa, int it){		boolean nameChanged = false;		String vc = getVoucherCode(taxa, it);		if (vc != null){			taxa.setTaxonName(it, startOfName + vc, false);			nameChanged = true;		}		return nameChanged;	}	public String getVoucherCode(Taxa taxa, int ic){				if (taxa!=null) {			Object n = ChromaseqUtil.getStringAssociated(taxa, VoucherInfoFromOTUIDDB.voucherCodeRef, ic);			if (n !=null)				return ((String)n);					}		return null;	}	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {    	 	if (checker.compare(this.getClass(), "Alters taxon names", "[length]", commandName, "appendNumbers")) {	   	 		if (taxa !=null){	   	 			alterTaxonNames(taxa,table);	   	 		}    	 	}    	 	else    	 		return  super.doCommand(commandName, arguments, checker);		return null;   	 }	/*.................................................................................................................*/    	 public String getNameForMenuItem() {		return "Root Text + OTU ID Code";   	 }	/*.................................................................................................................*/    	 public String getName() {		return "Root Text + OTU ID Code";   	 }   	 	/*.................................................................................................................*/  	 public String getExplanation() {		return "Changes the taxon name to be a root text + OTU ID Code";   	 }}	