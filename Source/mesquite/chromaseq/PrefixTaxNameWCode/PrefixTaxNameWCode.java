/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.Version 2.72, December 2009.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.PrefixTaxNameWCode;import java.util.*;import java.awt.*;import mesquite.chromaseq.lib.ChromaseqUtil;import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;/* ======================================================================== */public class PrefixTaxNameWCode extends TaxonNameAlterer {		/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){		return true;	}	/*.................................................................................................................*/	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/	public boolean getUserChooseable(){		return true; 	}	/*.................................................................................................................*/   	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class,    	then you must supply a real method for this, not just this stub. */   	public boolean alterName(Taxa taxa, int it){   		boolean nameChanged = false;		String name = taxa.getTaxonName(it);		if (name!=null){			String vc = getVoucherCode(taxa, it);			if (vc != null){				if (!name.contains(vc))					taxa.setTaxonName(it, vc + "." + name, false);			}			nameChanged = true;		}		return nameChanged;   	}	public String getVoucherCode(Taxa taxa, int ic){				if (taxa!=null) {			Object n = ChromaseqUtil.getStringAssociated(taxa, VoucherInfoFromOTUIDDB.voucherCodeRef, ic);			if (n !=null)				return ((String)n);					}		return null;	}	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {    	 	if (checker.compare(this.getClass(), "Appends numbers to taxon names", "[length]", commandName, "appendNumbers")) {	   	 		if (taxa !=null){	   	 			alterTaxonNames(taxa,table);	   	 		}    	 	}    	 	else    	 		return  super.doCommand(commandName, arguments, checker);		return null;   	 }	/*.................................................................................................................*/    	 public String getNameForMenuItem() {		return "Prefix With OTU ID Code";   	 }	/*.................................................................................................................*/    	 public String getName() {		return "Prefix With OTU ID Code";   	 }   	 	/*.................................................................................................................*/  	 public String getExplanation() {		return "Prefixes the taxon name with the OTU ID Code.";   	 }}	