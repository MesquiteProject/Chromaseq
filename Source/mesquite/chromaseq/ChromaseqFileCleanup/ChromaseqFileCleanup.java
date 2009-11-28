package mesquite.chromaseq.ChromaseqFileCleanup;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.MolecularData;
import mesquite.chromaseq.lib.ChromaseqUtil;
import mesquite.lib.*;
import mesquite.lib.duties.FileInit;
import mesquite.lib.characters.*;
import mesquite.meristic.lib.*;
import java.util.*;

public class ChromaseqFileCleanup extends FileInit  implements MesquiteListener{
	Vector reverseRegistryVector ;

	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
		reverseRegistryVector = new Vector();
		return true;
	}
	public String getName() {
		return "Chromaseq Cleanup";
	}
	public String getExplanation() {
		return "Used only to clean up legacy files.";
	}

	/*.................................................................................................................*/
	public void storeReverseRegistry(MeristicData reverseRegistryData) {
		reverseRegistryVector.add(reverseRegistryData);
		DNAData data = ChromaseqUtil.getEditedData(reverseRegistryData);
		if (data!=null)
			data.addListener(this);
	}
	/*.................................................................................................................*/
	public MeristicData findReverseRegistry(CharacterData otherData) {
		MeristicData possibleReverseRegistryData = ChromaseqUtil.getReverseRegistryData(otherData);
		if (possibleReverseRegistryData==null)
			return null;
		for (int i=0; i<reverseRegistryVector.size(); i++) {
			MeristicData reverseRegistryData = (MeristicData)reverseRegistryVector.elementAt(i);
			if (reverseRegistryData.equals(possibleReverseRegistryData))
				return reverseRegistryData;
		}
		return null;
	}
	/*.................................................................................................................*/
	public int findReverseRegistryIndex(MeristicData registryData) {
		MeristicData possibleReverseRegistryData = ChromaseqUtil.getReverseRegistryData(registryData);
		if (possibleReverseRegistryData==null)
			return -1;
		for (int i=0; i<reverseRegistryVector.size(); i++) {
			MeristicData reverseRegistryData = (MeristicData)reverseRegistryVector.elementAt(i);
			if (reverseRegistryData.equals(possibleReverseRegistryData))
				return i;
		}
		return -1;
	}


	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof DNAData) {
			if (Notification.appearsCosmetic(notification))
				return;
			MeristicData reverseRegistryData = findReverseRegistry((DNAData)obj);
			if (reverseRegistryData!=null) {
				ChromaseqUtil.fillReverseRegistryData(reverseRegistryData);
			}
		}
	}
	/** For MesquiteListener interface*/
	public void disposing(Object obj){
		if (!(obj instanceof MeristicData))
			return;
		int i = findReverseRegistryIndex((MeristicData)obj);
		if (i>=0) {
			MeristicData reverseRegistryData = (MeristicData)reverseRegistryVector.elementAt(i);
			if (reverseRegistryData!=null) {
				reverseRegistryData.dispose();
				reverseRegistryVector.remove(i);
			}
		}
	}
	int count = 1;
	/*.................................................................................................................*/
	public void createRegistryDataIfNeeded(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		
		ListableVector matrices = f.getProject().getCharacterMatrices();
		boolean registryMessageGiven = false;
		count++;
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				/*				CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
				boolean newAddedBase = false;
				if (addedBaseData==null) {
					ChromaseqUtil.createAddedBaseData(data);		
					newAddedBase = true;
				}
				 */				
				MeristicData registryData = ChromaseqUtil.getRegistryData(data);
				 if (registryData==null) {
					if (!registryMessageGiven)
						logln("No chromaseq registration data is stored in file; it will now be inferred.");
					ChromaseqUtil.createRegistryData(data, this);		
					registryMessageGiven = true;
				 }
				 else if (ChromaseqUtil.buildRequiresForcedRegistration(this)) {   //(DNAData)data
					if (!registryMessageGiven)
						logln("Chromaseq registration data stored in file is of a defunct version and needs to be rebuilt.");
					ChromaseqUtil.attachStringToMatrix(registryData, new MesquiteString(ChromaseqUtil.MATRIXTODELETE, "extra registration matrix"));
					ChromaseqUtil.createRegistryData(data, this);		
					registryMessageGiven = true;
				 }
				 MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);		
				 if (reverseRegistryData==null) {
					 DNAData originalData = ChromaseqUtil.getOriginalData(data);
					 registryData = ChromaseqUtil.getRegistryData(data);
					 if (originalData!=null && registryData!=null) {
						 reverseRegistryData = ChromaseqUtil.createReverseRegistryData(originalData);		
						 storeReverseRegistry(reverseRegistryData);
					 }

				 } else if (reverseRegistryVector.indexOf(reverseRegistryData)<0)
					 storeReverseRegistry(reverseRegistryData);

				 //				if (newAddedBase)
				 //					ChromaseqUtil.fillAddedBaseData(data);
				 ChromaseqUtil.prepareOriginalAndQualityData(data);

			}
		}
		ChromaseqUtil.setChromaseqBuildOfFile(this,  ChromaseqUtil.ChromaseqBuild);

	}
	/*.................................................................................................................*/
	public void deleteExtraRegistryMatrices(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		int numMatrices = matrices.size();
		for (int i=numMatrices-1; i>=0; i--)  {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			MesquiteString ms = ChromaseqUtil.getStringAttached(data, ChromaseqUtil.MATRIXTODELETE);
			if (ms!=null)
				data.deleteMe(false);
		}

	}
	/*.................................................................................................................*/
	public void createReverseRegistryDataIfNeeded(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				MeristicData registryData = ChromaseqUtil.getRegistryData(data);
				MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
				if (registryData!=null && reverseRegistryData==null) {
					DNAData originalData = ChromaseqUtil.getOriginalData(data);
					reverseRegistryData = ChromaseqUtil.createReverseRegistryData(originalData);		
					ChromaseqUtil.prepareOriginalAndQualityData(data);
					storeReverseRegistry(reverseRegistryData);

				}
			}
		}
	}
	/*.................................................................................................................*/
	public void aboutToReadMesquiteBlock(MesquiteFile f) {
		createRegistryDataIfNeeded(f);
	}
	/*.................................................................................................................*/
	public void removeOldNameReferences(MolecularData data) {
		if (data==null)
			return;
		Associable tInfo = data.getTaxaInfo(false);
		if (tInfo != null)
			tInfo.removeAssociatedLongs(ChromaseqUtil.startTrimRef);
	}
	/*.................................................................................................................*/
	public void deattachChromaseqBuild(MesquiteFile f) {
		Debugg.println("\n\n\n*********  detach!!!!!!!!!!!!\n\n\n");
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (data instanceof DNAData) 
				data.detachObjectOfName(ChromaseqUtil.READBUILDREF);
		}
	}
	/*.................................................................................................................*/
	public void fileReadIn(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		createRegistryDataIfNeeded(f);
		deleteExtraRegistryMatrices(f);
		deattachChromaseqBuild(f);
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				MolecularData editedData = ChromaseqUtil.getEditedData(data);
				removeOldNameReferences(editedData);
			}
		}
		if (!ChromaseqUtil.isChromaseqDevelopment())
			return;
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				CharacterData registryData = ChromaseqUtil.getRegistryData(data);
				if (registryData != null) registryData.setUserVisible(true);
				CharacterData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
				if (reverseRegistryData != null) reverseRegistryData.setUserVisible(true);
				CharacterData originalData = ChromaseqUtil.getOriginalData(data);
				if (originalData != null) originalData.setUserVisible(true);
				//				CharacterData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
				//				if (addedBaseData != null) addedBaseData.setUserVisible(true);

			}
		}

	}
}
