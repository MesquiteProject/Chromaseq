package mesquite.chromaseq.ChromaseqFileCleanup;

import mesquite.categ.lib.DNAData;
import mesquite.chromaseq.lib.ChromaseqUtil;
import mesquite.lib.*;
import mesquite.lib.duties.FileInit;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
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
			reverseRegistryData.dispose();
			reverseRegistryVector.remove(i);
		}
	}

	/*.................................................................................................................*/
	public void createRegistryDataIfNeeded(MesquiteFile f) {
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				MeristicData registryData = ChromaseqUtil.getRegistryData(data);
				if (registryData==null) {
					ChromaseqUtil.createRegistryData(data);			
				}
				DNAData originalData = ChromaseqUtil.getOriginalData(data);
				MeristicData reverseRegistryData = ChromaseqUtil.createReverseRegistryData(registryData,originalData);			
				storeReverseRegistry(reverseRegistryData);
			}
		}
	}
	/*.................................................................................................................*/
	public void aboutToReadMesquiteBlock(MesquiteFile f) {
		createRegistryDataIfNeeded(f);
	}

	/*.................................................................................................................*/
	public void fileReadIn(MesquiteFile f) {
		createRegistryDataIfNeeded(f);
	}
}
