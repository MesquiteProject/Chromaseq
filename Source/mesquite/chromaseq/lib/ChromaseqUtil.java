/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.chromaseq.lib;

import java.util.Vector;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.align.lib.PairwiseAligner;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;
import mesquite.meristic.lib.*;

public class ChromaseqUtil{

	public static final int TRIMMABLE=1;
	public static final int BASECALLED=2;
	public static final String PHPHIMPORTIDREF = "phphImportID";
	public static final String GENENAMEREF ="geneName";
	public static final String PHPHMQVERSIONREF ="phphmqVersion";
	public static final String PHPHMQVERSION ="2";
	public static final String PHPHIMPORTMATRIXTYPEREF ="phphImportMatrixType";
	public static final String QUALITYREF ="quality";
	public static final String ORIGINALREF ="original";
	public static final String EDITEDREF ="edited";
	public static final String REGISTRYREF = "registration";
	public static final String REVERSEREGISTRYREF = "reverse registration";
	public static final String ADDEDBASEREF = "added base";






	static NameReference trimmableNameRef = NameReference.getNameReference("trimmable");


	public static boolean isTrimmable(int ic, int it, CharacterData data){
		if (data == null)
			return false;
		if (ic>=0 && it>=0){ 
			Object obj = data.getCellObject(trimmableNameRef, ic, it);
			if (obj != null && obj instanceof MesquiteInteger)
				return ((MesquiteInteger)obj).getValue()==ChromaseqUtil.TRIMMABLE;
		}
		return false;
	}
	public static boolean baseCalled(int ic, int it, CharacterData data){
		if (data == null)
			return false;
		if (ic>=0 && it>=0){ 
			Object obj = data.getCellObject(trimmableNameRef, ic, it);
			if (obj != null && obj instanceof MesquiteInteger)
				return ((MesquiteInteger)obj).getValue()==ChromaseqUtil.BASECALLED;
		}
		return false;
	}

	public static String getUID(CharacterData data) {
		String uid = "";
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			uid = ((MesquiteString)obj).getValue();
		}
		return uid;
	}

	public static String getGeneName(CharacterData data) {
		String gn = "";
		Object obj = data.getAttachment(GENENAMEREF);
		if (obj!=null && obj instanceof MesquiteString) {
			gn = ((MesquiteString)obj).getValue();
		}
		return gn;
	}


	public static boolean isChromaseqEditedMatrix(CharacterData data) {
		if (!(data instanceof DNAData))
			return false;
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj==null) {
			return false;
		}
		obj = data.getAttachment(PHPHIMPORTMATRIXTYPEREF);
		if (obj instanceof MesquiteString)
			if (((MesquiteString)obj).getValue().equalsIgnoreCase(EDITEDREF)) {
				return true;
			}
		return false;
	}


	public static String getPHPHDataType(CharacterData data) {
		Object obj = data.getAttachment(PHPHIMPORTMATRIXTYPEREF);
		if (obj instanceof MesquiteString)
			return ((MesquiteString)obj).getValue();
		return null;
	}

	public static boolean isPHPHDataType(CharacterData data, String candidate) {
		String s = getPHPHDataType(data);
		if (StringUtil.notEmpty(s))
			return s.equalsIgnoreCase(candidate);
		return false;
	}

	/*.................................................................................................................*/


	public static CharacterData getAssociatedData(CharacterData data, String dataType) {
		String uid = getUID(data);
		//	String gn = getGeneName(data);
		Object obj;
		ListableVector matrices = data.getProject().getCharacterMatrices();
		for (int i= 0; i< matrices.size(); i++){
			CharacterData d = (CharacterData)matrices.elementAt(i);
			obj = d.getAttachment(PHPHIMPORTMATRIXTYPEREF);
			if (obj instanceof MesquiteString)
				if (((MesquiteString)obj).getValue().equalsIgnoreCase(dataType)) {
					obj = d.getAttachment(PHPHIMPORTIDREF);
					String s = ((MesquiteString)obj).getValue();
					if (obj instanceof MesquiteString && uid.equalsIgnoreCase(((MesquiteString)obj).getValue())) {
						return d;
					}
				}

		}
		return null;
	}
	/*.................................................................................................................*/

	public static ContinuousData getQualityData(CharacterData data) {
		CharacterData d = getAssociatedData(data,QUALITYREF);
		if (d instanceof ContinuousData)
			return (ContinuousData)d;
		return null;
	}

	public static DNAData getOriginalData(CharacterData data) {
		CharacterData d = getAssociatedData(data,ORIGINALREF);
		if (d instanceof DNAData)
			return (DNAData)d;
		return null;
	}

	public static DNAData getEditedData(CharacterData data) {
		CharacterData d = getAssociatedData(data,EDITEDREF);
		if (d instanceof DNAData)
			return (DNAData)d;
		return null;
	}


	public static MeristicData getRegistryData(CharacterData data) {
		CharacterData d = getAssociatedData(data,REGISTRYREF);
		if (d instanceof MeristicData)
			return (MeristicData)d;
		return null;
	}

	public static MeristicData getReverseRegistryData(CharacterData data) {
		CharacterData d = getAssociatedData(data,REVERSEREGISTRYREF);
		if (d instanceof MeristicData)
			return (MeristicData)d;
		return null;
	}

	public static CategoricalData getAddedBaseData(CharacterData data) {
		CharacterData d = getAssociatedData(data,ADDEDBASEREF);
		if (d instanceof CategoricalData)
			return (CategoricalData)d;
		return null;
	}


	/*.................................................................................................................*/
	public static void resetNumAddedToStart(ContigDisplay contigDisplay, CharacterData data, int it) {
		int numAdded = getNumAddedToStart(data,it);
		contigDisplay.setNumBasesAddedToStart(numAdded);

	}
	/*.................................................................................................................*/
	public static int getNumAddedToStart(CharacterData data, int it) {
		int count=0;
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedBaseData!=null && registryData!=null && editedData!=null) {
			for (int ic=0;ic<addedBaseData.getNumChars() && ic<registryData.getNumChars(); ic++) {

				if (!MesquiteInteger.isCombinable(registryData.getState(ic, it))) { // we still haven't found one that is in the original
					long addedBaseState = addedBaseData.getState(ic, it);
					int addedBaseValue = CategoricalState.getOnlyElement(addedBaseState);
					if (addedBaseValue==1)
						count++;
				} else
					return count;
			}
		}
		return 0;
	}

	/*.................................................................................................................*/
	public static void fillAddedBaseData(CategoricalData addedBaseData, MeristicData registryData, DNAData editedData, int ic, int it) {
		if (addedBaseData!=null && registryData!=null && editedData!=null) {
			int registryState = registryData.getState(ic, it);
			long editedState = editedData.getState(ic, it);
			if (!MesquiteInteger.isCombinable(registryState)) { // then the original data doesn't have a state here
				if (!CategoricalState.isInapplicable(editedState))  // the edited data has something in it
					addedBaseData.setState(ic, it, CategoricalState.makeSet(1));  // mark it as having an added base
				else
					addedBaseData.setToUnassigned(ic, it);
			}
			else if (false) {  // the original data DOES have a state here
					if (CategoricalState.isInapplicable(editedState))  // the edited data DOESN'T have something in it
						addedBaseData.setState(ic, it, CategoricalState.makeSet(2));  // mark it as having a base removed
				}
			 
		}

	}

	/*.................................................................................................................*/
	public static void fillAddedBaseData(CharacterData data, int ic, int it) {
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		fillAddedBaseData(addedBaseData,registryData, editedData, ic,it);
	}
	/*.................................................................................................................*/
	public static void fillAddedBaseData(CharacterData data, int it) {
//Debugg.println("fillAddedBaseData it: " + it);
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedBaseData!=null && registryData!=null && editedData!=null)
			for (int ic=0;ic<addedBaseData.getNumChars() && ic<registryData.getNumChars(); ic++) {
				fillAddedBaseData(addedBaseData,registryData, editedData, ic,it);
			}
	}
	/*.................................................................................................................*/
	public static void fillAddedBaseData(ContigDisplay contigDisplay, CharacterData data, int it) {
		fillAddedBaseData(data, it);
		resetNumAddedToStart(contigDisplay, data,it);
	}


	/*.................................................................................................................*/
	public static void fillAddedBaseData(CharacterData data) {
		if (data==null)
			return;
		for (int it=0;it<data.getNumTaxa(); it++) 
			fillAddedBaseData(data,it);
	}


	/*.................................................................................................................*/

	public static void fillReverseRegistryData(MeristicData reverseRegistryData) {
		MeristicData registryData = getRegistryData(reverseRegistryData);
		if (registryData==null)
			return;
		for (int it=0; it<reverseRegistryData.getNumTaxa(); it++) 
			for (int ic=0; ic<reverseRegistryData.getNumChars(); ic++){
				reverseRegistryData.setToInapplicable(ic, it);
			}
		for (int it=0; it<registryData.getNumTaxa() && it<reverseRegistryData.getNumTaxa(); it++) 
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				int mapping = registryData.getState(ic, it);
				if (mapping>=0 && mapping<=reverseRegistryData.getNumChars())
					reverseRegistryData.setState(mapping, it, 0, ic);
			}
	}

	/*.................................................................................................................*/
	public static void setAddedBaseDataValues(CategoricalData addedBaseData, CharacterData data, String name, MesquiteString uid, MesquiteString gN) {
		addedBaseData.saveChangeHistory = false;
		data.addToLinkageGroup(addedBaseData); //link matrices!
		addedBaseData.setName("Bases added for " + name + " from Phred/Phrap");
		addedBaseData.setResourcePanelIsOpen(false);
		addedBaseData.attachIfUniqueName(uid);
		addedBaseData.attachIfUniqueName(gN);
		addedBaseData.attachIfUniqueName(new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.ADDEDBASEREF));
		addedBaseData.setLocked(true);
		addedBaseData.setColorCellsByDefault(true);
		addedBaseData.setUseDiagonalCharacterNames(false);
	}
	/*.................................................................................................................*/

	public static CategoricalData createAddedBaseData(CharacterData data) {
		CategoricalData addedBaseData = getAddedBaseData(data);
		if (addedBaseData!=null)
			return addedBaseData;
		MesquiteString uid = null;
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			String dataUID= ((MesquiteString)obj).getValue();
			uid = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataUID);
		}
		MesquiteString gN = null;
		String dataGeneName = "";
		obj = data.getAttachment(GENENAMEREF);
		if (obj!=null && obj instanceof MesquiteString) {
			dataGeneName= ((MesquiteString)obj).getValue();
			gN = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataGeneName);
		}
		FileCoordinator coord = data.getProject().getCoordinatorModule();
		MesquiteFile file = data.getProject().getHomeFile();
		CharactersManager manageCharacters = (CharactersManager)coord.findElementManager(mesquite.lib.characters.CharacterData.class);
		addedBaseData =  (CategoricalData)manageCharacters.newCharacterData(data.getTaxa(), data.getNumChars(), CategoricalData.DATATYPENAME);  //
		//registryData =  (MeristicData)manageCharacters.newCharacterData(data.getTaxa(), data.lastApplicable()+1, MeristicData.DATATYPENAME);  //
		addedBaseData.addToFile(file, data.getProject(), manageCharacters);  

		setAddedBaseDataValues(addedBaseData, data, dataGeneName, uid, gN);

		//	createReverseRegistryData(registryData, originalData);

		return addedBaseData;
	}

	/*.................................................................................................................*/

	public synchronized static void fillRegistryData(MeristicData registryData) {
		if (registryData==null)
			return;
		for (int it=0; it<registryData.getNumTaxa(); it++) 
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				registryData.setToInapplicable(ic, it);
			}
		DNAData originalData = getOriginalData(registryData);
		DNAData editedData = getEditedData(registryData);
		if(originalData==null || editedData==null)
			return;
		PairwiseAligner aligner = PairwiseAligner.getDefaultAligner(editedData);
		for (int it=0; it<registryData.getNumTaxa(); it++)  {
			int lengthDifference = originalData.numApplicable(it)-editedData.numApplicable(it);
			if (lengthDifference==0) {  // they both have the same number of applicable; probably ok
				int count=0;
				int icOriginal = 0;
				for (int icEdited=0; icEdited<editedData.getNumChars(); icEdited++){
					if (!editedData.isInapplicable(icEdited,it)) {
						icOriginal = originalData.nextApplicable(it, icOriginal, true);
						if (icOriginal>=0) {
							registryData.setState(icEdited, it, 0, icOriginal);
							icOriginal++;
							count++;
						}
						else break;
					}
				}
			} else {
				if (aligner!=null) {
					MesquiteNumber alignScore = new MesquiteNumber();
					long[][] alignment = aligner.getAlignment(originalData,  it, editedData, it, alignScore);
					if (alignment!=null) {
						int originalCount = 0;
						int editedCount = 0;

						/* now flag all unmatched bases
						for (int ic=0; ic<alignment.length; ic++) {
							boolean originalIsApplicable = alignment[ic][0]!=CategoricalState.inapplicable;
							boolean editedIsApplicable = alignment[ic][1]!=CategoricalState.inapplicable;

							if (editedIsApplicable &&!originalIsApplicable) 
								alignment[ic][1]=CategoricalState.impossible;   //flag it
							if (!editedIsApplicable &&originalIsApplicable) 
								alignment[ic][0]=CategoricalState.impossible;
						}


						for (int ic=0; ic<alignment.length; ic++) {
								if (it==1&& ic<100) Debugg.println(""+ ic + "   " + alignment[ic][0] + "  " + alignment[ic][1]);
						}
						 */
						//		int icEdited = editedData.firstApplicable(it, 0); 
						//		int icOriginal = originalData.firstApplicable(it, 0); 
						int icEdited = 0;
						int icOriginal = 0;

						int diffEdited=0;
						int diffOriginal=0;

						if (it==0) 
							Debugg.println("startloop");
						for (int ic=0; ic<alignment.length && icEdited>=0 && icOriginal>=0; ic++) {
							boolean originalIsApplicable = alignment[ic][0]!=CategoricalState.inapplicable;
							boolean editedIsApplicable = alignment[ic][1]!=CategoricalState.inapplicable;
							if (editedIsApplicable){
								editedCount++;
								diffOriginal++;
							}
							if (originalIsApplicable) {
								originalCount++;
								diffEdited++;
							}

							/*	if (it==0 && icEdited<60) {
								if (editedIsApplicable)
									Debugg.print("editedIsApplicable (editedCount: " + editedCount + ", diffEdited: " + diffEdited + ") ||" );
								if (originalIsApplicable)
									Debugg.print("|| originalIsApplicable (originalCount: " + originalCount + ", diffOriginal: " + diffOriginal + ") ||" );
								Debugg.println("|| icEdited: " + icEdited + ", icOriginal: " + icOriginal + ") " );
							}

							 */
							if (editedIsApplicable && originalIsApplicable) {
								for (int i = 0;i<diffEdited && icEdited>=0; i++) {
									icEdited = editedData.nextApplicable(it, icEdited, false); 
									if (icEdited>=0)
										icEdited++;
								}
								for (int i = 0;i<diffOriginal && icOriginal>=0; i++) {
									icOriginal = originalData.nextApplicable(it, icOriginal, false);
									if (icOriginal>=0)
										icOriginal++;
								}
								icEdited--;
								icOriginal--;
								if (icEdited>=0 && icEdited<registryData.getNumChars() && icOriginal>=0 && icOriginal<originalData.getNumChars()) {
									registryData.setState(icEdited, it, 0, icOriginal);
								}
								icEdited++;
								icOriginal++;

								diffOriginal=0;
								diffEdited=0;
							}
						}

					}


				} else {

					//Debugg.println("sequence " + it + " (" + originalData.getTaxa().getTaxonName(it) + ") with length difference " + lengthDifference);
					for (int ic=0; ic<registryData.getNumChars(); ic++){
						registryData.setState(ic, it, 0, ic);
					}
				}
			}
		}
		fillAddedBaseData(editedData);
	}

	/*.................................................................................................................*/

	public static void setReverseRegistryDataValues(MeristicData reverseRegistryData, DNAData originalData, String name, MesquiteString uid, MesquiteString gN) {
		originalData.addToLinkageGroup(reverseRegistryData); //link matrices!
		reverseRegistryData.setName("Reverse Registration Data of " + name + " (for internal bookkeeping)");  //DAVID: if change name here have to change elsewhere
		reverseRegistryData.attachIfUniqueName(uid);
		reverseRegistryData.attachIfUniqueName(gN);
		reverseRegistryData.attachIfUniqueName(new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.REVERSEREGISTRYREF));
		reverseRegistryData.setWritable(false);
		reverseRegistryData.setResourcePanelIsOpen(false);

	}
	/*.................................................................................................................*/

	public static MeristicData createReverseRegistryData(MeristicData registryData, DNAData originalData) {
		MeristicData rr = getReverseRegistryData(originalData);
		if (rr!=null)
			return rr;
		MesquiteString uid = null;
		int originalNumChars = originalData.getNumChars();
		Object obj = originalData.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			String dataUID= ((MesquiteString)obj).getValue();
			uid = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataUID);
		}
		MesquiteString gN = null;
		String dataGeneName = "";
		obj = originalData.getAttachment(GENENAMEREF);
		if (obj!=null && obj instanceof MesquiteString) {
			dataGeneName= ((MesquiteString)obj).getValue();
			gN = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataGeneName);
		}
		FileCoordinator coord = originalData.getProject().getCoordinatorModule();
		MesquiteFile file = originalData.getProject().getHomeFile();
		CharactersManager manageCharacters = (CharactersManager)coord.findElementManager(mesquite.lib.characters.CharacterData.class);
		MeristicData reverseRegistryData =  (MeristicData)manageCharacters.newCharacterData(originalData.getTaxa(), originalData.getNumChars(), MeristicData.DATATYPENAME);  //
		reverseRegistryData.addToFile(file, originalData.getProject(), manageCharacters);  

		setReverseRegistryDataValues(reverseRegistryData, originalData, dataGeneName, uid, gN);

		fillReverseRegistryData(reverseRegistryData);
		reverseRegistryData.setEditorInhibition(true);

		return reverseRegistryData;
	}
	/*.................................................................................................................*/

	public static void prepareOriginalAndQualityData (CharacterData data) {
		ContinuousData qualityData = getQualityData(data);
		if (qualityData!=null) {
			qualityData.resignFromLinkageGroup();
			qualityData.setLocked(true);
		}
		DNAData originalData = getOriginalData(data);
		if (originalData!=null) {
			originalData.resignFromLinkageGroup();
			originalData.setLocked(true);
		}
	}

	/*.................................................................................................................*/

	public static void setRegistryDataValues(MeristicData registryData, CharacterData data, String name, MesquiteString uid, MesquiteString gN) {
		registryData.saveChangeHistory = false;
		data.addToLinkageGroup(registryData); //link matrices!
		registryData.setName("Registration of " + name + " from Phred/Phrap");  //DAVID: if change name here have to change elsewhere
		registryData.attachIfUniqueName(uid);
		registryData.attachIfUniqueName(gN);
		registryData.attachIfUniqueName(new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.REGISTRYREF));
		registryData.setResourcePanelIsOpen(false);
		registryData.setEditorInhibition(true);
	}

	/*.................................................................................................................*/

	public static MeristicData createRegistryData(CharacterData data) {
		MesquiteString uid = null;
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			String dataUID= ((MesquiteString)obj).getValue();
			uid = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataUID);
		}
		MesquiteString gN = null;
		String dataGeneName = "";
		obj = data.getAttachment(GENENAMEREF);
		if (obj!=null && obj instanceof MesquiteString) {
			dataGeneName= ((MesquiteString)obj).getValue();
			gN = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataGeneName);
		}
		FileCoordinator coord = data.getProject().getCoordinatorModule();
		MesquiteFile file = data.getProject().getHomeFile();
		CharactersManager manageCharacters = (CharactersManager)coord.findElementManager(mesquite.lib.characters.CharacterData.class);
		MeristicData registryData;	
		registryData =  (MeristicData)manageCharacters.newCharacterData(data.getTaxa(), data.getNumChars(), MeristicData.DATATYPENAME);  //
		//registryData =  (MeristicData)manageCharacters.newCharacterData(data.getTaxa(), data.lastApplicable()+1, MeristicData.DATATYPENAME);  //
		registryData.addToFile(file, data.getProject(), manageCharacters);  

		setRegistryDataValues(registryData,  data, dataGeneName,  uid,  gN);

		fillRegistryData(registryData);

		prepareOriginalAndQualityData(data);

		//	createReverseRegistryData(registryData, originalData);

		return registryData;
	}
}
