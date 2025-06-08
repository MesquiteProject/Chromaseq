/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.GenBankNumbersFromTabbedFile;/*~~  */import mesquite.lists.lib.*;

import java.io.IOException;import java.net.URI;import java.net.URISyntaxException;import java.util.*;import java.awt.*;import java.awt.event.*;
import mesquite.lib.ui.*;
import mesquite.lib.taxa.*;
import mesquite.lib.misc.*;

import mesquite.lib.*;import mesquite.lib.characters.CharacterData;
import mesquite.categ.lib.*;
import mesquite.lib.duties.*;import mesquite.lib.table.*;
import mesquite.chromaseq.lib.*;
/* ======================================================================== */public class GenBankNumbersFromTabbedFile extends TaxaListAssistantI  {	Taxa taxa;	MesquiteTable table;
	GenBankTabbedFileProcessor codeFileWithGenBankNumbers;

	public boolean loadModule(){
		return false;
	}	public String getName() {
		return "Get GenBank Accession Numbers from File";
	}
	public String getNameForMenuItem() {
		return "Get GenBank Accession Numbers from File...";
	}
	public String getExplanation() {		return "Annotates with GenBank accession numbers in tab-delimited text file";	}	/*.................................................................................................................*/	public int getVersionOfFirstRelease(){		return -NEXTRELEASE;  	}	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		addMenuItem("Get GenBank Accession Numbers from File...", new MesquiteCommand("getGenBankNumbers", this));		return true;	}
	/*.................................................................................................................*/
	public String getGeneName(CharacterData data) {
		String geneName = data.getName();
		if ("CAD1".equalsIgnoreCase(data.getName())) {
			geneName = "CAD";
		} else if ("CAD2".equalsIgnoreCase(data.getName())) {
			geneName = "CAD";
		} else if ("CAD3".equalsIgnoreCase(data.getName())) {
			geneName = "CAD";
		} else if ("CAD4".equalsIgnoreCase(data.getName())) {
			geneName = "CAD";
		} else if ("COIBC".equalsIgnoreCase(data.getName())) {
			geneName = "COI";
		} else if ("COIPJ".equalsIgnoreCase(data.getName())) {
			geneName = "COI";
		} else if ("COIALL".equalsIgnoreCase(data.getName())) {
			geneName = "COI";
		}
		return geneName;
	}
	/*.................................................................................................................*/
	public String getFragmentName(CharacterData data) {
		if ("CAD1".equalsIgnoreCase(data.getName())) {
			return data.getName();
		} else if ("CAD2".equalsIgnoreCase(data.getName())) {
			return data.getName();
		} else if ("CAD3".equalsIgnoreCase(data.getName())) {
			return data.getName();
		} else if ("CAD4".equalsIgnoreCase(data.getName())) {
			return data.getName();
		} else if ("COIBC".equalsIgnoreCase(data.getName())) {
			return data.getName();
		} else if ("COIPJ".equalsIgnoreCase(data.getName())) {
			return data.getName();
		} else if ("COIALL".equalsIgnoreCase(data.getName())) {
			return data.getName();
		}
		return "";
	}
	/*.................................................................................................................*/
	public String getAlternativeFragmentName(CharacterData data) {
		if ("CAD1".equalsIgnoreCase(data.getName())) {
			return "CAD234";
		} else if ("CAD2".equalsIgnoreCase(data.getName())) {
			return "CAD234";
		} else if ("CAD3".equalsIgnoreCase(data.getName())) {
			return "CAD234";
		} else if ("CAD4".equalsIgnoreCase(data.getName())) {
			return "CAD234";
		} else if ("COIBC".equalsIgnoreCase(data.getName())) {
			return "COIAll";
		} else if ("COIPJ".equalsIgnoreCase(data.getName())) {
			return "COIAll";
		} else if ("COIALL".equalsIgnoreCase(data.getName())) {
			return "COIAll";
		}
		return "";
	}

	/*.................................................................................................................*/	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.	This should be overridden by any module that wants to respond to a command.*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 		if (checker.compare(MesquiteModule.class, null, null, commandName, "getGenBankNumbers")) {
			if (taxa == null)
				return null;
			codeFileWithGenBankNumbers = new GenBankTabbedFileProcessor();
			if (!codeFileWithGenBankNumbers.chooseCodeFile())
				return null;
			int numMatrices = getProject().getNumberCharMatrices(taxa);
			if (numMatrices<1)
				return null;
			Vector datas = new Vector();
			for (int i = 0; i<numMatrices; i++){
				CharacterData data = getProject().getCharacterMatrix(taxa, i);
				if (data.isUserVisible())
					datas.addElement(data);
			}			if (getEmployer() instanceof ListModule){
				ListModule listModule = (ListModule)getEmployer();
				/*	Vector v = listModule.getAssistants();
				for (int k = 0; k< v.size(); k++){
					ListAssistant a = (ListAssistant)v.elementAt(k);
					if (a instanceof mesquite.molec.TaxaListHasData.TaxaListHasData){
						mesquite.molec.TaxaListHasData.TaxaListHasData tLHD = (mesquite.molec.TaxaListHasData.TaxaListHasData)a;
						CharacterData data = tLHD.getCharacterData();
						if (datas.indexOf(data)>=0)
							datas.removeElement(data);
					}
				}
				 */
				Puppeteer puppeteer = new Puppeteer(this);
				CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
				CommandRecord cRecord = new CommandRecord(true);
				MesquiteThread.setCurrentCommandRecord(cRecord);
				//at this point the vector should include only the ones not being shown.
				boolean anySelected = table.anyCellSelectedAnyWay();
				int added = 0;
				int count = 0;
				for (int i = 0; i<datas.size(); i++) {
					if (datas.elementAt(i) instanceof MolecularData) {
						MolecularData sequenceData =  (MolecularData)datas.elementAt(i);
						for (int it=0; it<taxa.getNumTaxa(); it++) {
							if (!anySelected || table.isRowSelected(it)) {
								boolean wroteToLog = false;

								String voucherCode = (String)taxa.getAssociatedObject(VoucherInfoFromOTUIDDB.voucherCodeRef, it);

								String line = codeFileWithGenBankNumbers.codeIsInCodeListFile(voucherCode, getGeneName(sequenceData), getFragmentName(sequenceData), getAlternativeFragmentName(sequenceData));
								count++;
								if (StringUtil.notEmpty(line)) {
									String genBankNumber = codeFileWithGenBankNumbers.getGenBankNumberFromCodeFileLine(line);
									String oldGenBankNumber = sequenceData.getGenBankNumber(it);
									if (StringUtil.notEmpty(genBankNumber)) {
										if (!genBankNumber.equalsIgnoreCase(oldGenBankNumber)) {
											wroteToLog = true;
											logln("  GenBank accession number added for matrix " + sequenceData.getName() + ",  taxon " + taxa.getTaxonName(it) + ":  "+ genBankNumber);
											added++;
											count=0;
										}
										sequenceData.setGenBankNumber(it, genBankNumber);
									}
								}
								if (!wroteToLog && count>0 && count % 100 == 0)
									log(".");
							}
						}
					}

				}
				logln(""+added + " GenBank accession numbers added");

				MesquiteThread.setCurrentCommandRecord(prevR);
			}		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	/*.................................................................................................................*/	public boolean isSubstantive(){		return false;	}	/*.................................................................................................................*/	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){		this.table = table;		this.taxa = taxa;	}}