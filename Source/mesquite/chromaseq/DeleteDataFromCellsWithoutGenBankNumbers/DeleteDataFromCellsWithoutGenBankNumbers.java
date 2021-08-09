/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


import java.io.IOException;

import mesquite.lib.*;
import mesquite.categ.lib.*;
import mesquite.lib.duties.*;


		return "Delete Data from Entries without GenBank Numbers";
	}
	public String getNameForMenuItem() {
		return "Delete Data from Entries without GenBank Numbers...";
	}
	public String getExplanation() {
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

		addMenuItem("Delete Data from Entries without GenBank Numbers", new MesquiteCommand("deleteDataWithoutGenBankNumbers", this));
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
			if (taxa == null)
				return null;
			int numMatrices = getProject().getNumberCharMatrices(taxa);
			if (numMatrices<1)
				return null;
			if (!AlertDialog.query(containerOfModule(), "Delete data?", "Are you sure you want to delete all sequence data for entries that have no attached GenBank numbers?  (This cannot be undone.)")) 
				return null;

			Vector datas = new Vector();
			for (int i = 0; i<numMatrices; i++){
				CharacterData data = getProject().getCharacterMatrix(taxa, i);
				if (data.isUserVisible())
					datas.addElement(data);
			}
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
					boolean gbnFound = false;
					if (datas.elementAt(i) instanceof MolecularData) {
						MolecularData sequenceData =  (MolecularData)datas.elementAt(i);
						for (int it=0; it<taxa.getNumTaxa(); it++) {
							if (!anySelected || table.isRowSelected(it)) {
							
								if (hasGenBankNumber(sequenceData,it)) {
									gbnFound=true;
								} else
								{
									for (int ic=0; ic<sequenceData.getNumChars(); ic++)
										sequenceData.deassign(ic, it);
								}
									
							}
						}
						if (gbnFound) {
							sequenceData.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
							outputInvalid();
							parametersChanged();
						}
					}

				}

				MesquiteThread.setCurrentCommandRecord(prevR);
			}