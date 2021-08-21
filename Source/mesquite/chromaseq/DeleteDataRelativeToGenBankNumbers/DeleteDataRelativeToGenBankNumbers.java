/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.DeleteDataRelativeToGenBankNumbers;/*~~  */import mesquite.lists.lib.*;

import java.io.IOException;import java.net.URI;import java.net.URISyntaxException;import java.util.*;import java.awt.*;import java.awt.event.*;

import mesquite.lib.*;import mesquite.lib.characters.CharacterData;
import mesquite.categ.lib.*;
import mesquite.lib.duties.*;import mesquite.lib.table.*;
/* ======================================================================== */public class DeleteDataRelativeToGenBankNumbers extends TaxaListAssistantI  {	Taxa taxa;	MesquiteTable table;
	boolean deleteWithGenBankNumbers = true;
	public String getName() {
		return "Delete Sequences with OR without GenBank Numbers";
	}
	public String getNameForMenuItem() {
		return "Delete Sequences with OR without GenBank Numbers...";
	}
	public String getExplanation() {		return "This will delete any sequence for which there is OR is not an attached GenBank accession number.";	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/	public int getVersionOfFirstRelease(){		return -1530;  	}	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		addMenuItem("Delete Sequences with OR without GenBank Numbers...", new MesquiteCommand("deleteDataGB", this));		return true;	}
	/*...............................................................................................................*/
	/** Sets the GenBank number of a particular taxon in this data object. */
	public boolean hasGenBankNumber(MolecularData data, int it){
		if (data==null) return false;
		Taxon taxon = data.getTaxa().getTaxon(it);
		Associable tInfo = data.getTaxaInfo(true);
		if (tInfo != null && taxon != null) {
			String s = (String)tInfo.getAssociatedObject(MolecularData.genBankNumberRef, it);
			if (StringUtil.notEmpty(s))
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		 if ("deleteWithGenBankNumbers".equalsIgnoreCase(tag))
			 deleteWithGenBankNumbers = MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "deleteWithGenBankNumbers", deleteWithGenBankNumbers);  
		return buffer.toString();
	}

	/*.................................................................................................................*/
	
	private boolean queryOptions() {
		loadPreferences();
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);		
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), 
				"Delete data relative to presence of GenBank numbers", buttonPressed);
		int defaultValue =0;
		if (!deleteWithGenBankNumbers) 
			defaultValue=1;
		RadioButtons deleteWithGenBankRadioButtons = dialog.addRadioButtons(new String[] {"delete each sequence for which there IS an associated GenBank accession number", "delete each sequence for which there is NOT an associated GenBank accession number"}, defaultValue);
		dialog.completeAndShowDialog(true);
		boolean success = buttonPressed.getValue()== dialog.defaultOK; 
		if (success) {
			deleteWithGenBankNumbers = deleteWithGenBankRadioButtons.getValue()==0;		
		}
		return success;
	}

	/*.................................................................................................................*/

	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.	This should be overridden by any module that wants to respond to a command.*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 		if (checker.compare(MesquiteModule.class, null, null, commandName, "deleteDataGB")) {
			if (taxa == null)
				return null;
			int numMatrices = getProject().getNumberCharMatrices(taxa);
			if (numMatrices<1)
				return null;
			if (queryOptions() && !AlertDialog.query(containerOfModule(), "Delete data?", "Are you SURE you want to delete the sequence data?  (This cannot be undone.)")) 
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
				for (int i = 0; i<datas.size(); i++) {
					boolean dataToDelete = false;
					if (datas.elementAt(i) instanceof MolecularData) {
						MolecularData sequenceData =  (MolecularData)datas.elementAt(i);
						for (int it=0; it<taxa.getNumTaxa(); it++) {
							if (!anySelected || table.isRowSelected(it)) {
							
								if (deleteWithGenBankNumbers == hasGenBankNumber(sequenceData,it)) {
									dataToDelete=true;
									for (int ic=0; ic<sequenceData.getNumChars(); ic++)
										sequenceData.deassign(ic, it);
								}
									
							}
						}
						if (dataToDelete) {
							sequenceData.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
							outputInvalid();
							parametersChanged();
						}
					}

				}

				MesquiteThread.setCurrentCommandRecord(prevR);
			}		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	/*.................................................................................................................*/	public boolean isSubstantive(){		return true;	}	/*.................................................................................................................*/	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){		this.table = table;		this.taxa = taxa;	}}