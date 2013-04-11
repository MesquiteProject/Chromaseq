/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.ChromaseqFileCleanup;

import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.FileInit;
import mesquite.lib.characters.*;
import mesquite.meristic.lib.*;
import java.util.*;

public class ChromaseqFileCleanup extends FileInit  implements MesquiteListener{
	Vector reverseRegistryVector ;
	boolean resave = false;

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
	public boolean isPrerelease(){
		return false;  
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
			int code = Notification.getCode(notification);
			int[] parameters = Notification.getParameters(notification);
			if (code==MesquiteListener.NAMES_CHANGED || code==MesquiteListener.SELECTION_CHANGED) {
				return;
			}
			else if (!Notification.appearsCosmetic(notification) && ChromaseqUtil.isChromaseqEditedMatrix((CharacterData)obj)){
				if (!((code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_MOVED) && notification.subcodesContains(MesquiteListener.TAXA_CHANGED))) {
					DNAData editedData= (DNAData)((CharacterData)obj);
					if (editedData.singleCellSubstitution(notification)) 
									return;
				}
				MeristicData reverseRegistryData = findReverseRegistry((DNAData)obj);
				if (reverseRegistryData!=null) {
					ChromaseqUtil.fillReverseRegistryData(reverseRegistryData);
				}
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
		
		NameReference trimmable = NameReference.getNameReference("trimmable");
		
		ListableVector matrices = f.getProject().getCharacterMatrices();
		boolean registryMessageGiven = false;
		count++;
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);

			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {

				//these lines are here to convert old "trimmable" references to the more general "chromaseqStatus" references; read only; written as "chromaseqStatus"
				Object2DArray array = data.getWhichCellObjects(trimmable);
				if (array != null)
					array.setNameReference(ChromaseqUtil.chromaseqCellFlagsNameRef);
				array = null;
				Associable assoc = data.getTaxaInfo(false);
				if (assoc != null){
					LongArray Larray = assoc.getWhichAssociatedLong(trimmable);
					if (Larray != null)
						Larray.setNameReference(ChromaseqUtil.chromaseqCellFlagsNameRef);
				}
				//--------------
				
				MeristicData registryData = ChromaseqUtil.getRegistryData(data);
				 if (registryData==null) {
					if (!registryMessageGiven)
						logln("No chromaseq registry is stored in file; it will now be inferred.");
					ChromaseqUtil.createRegistryData(data, this, false);		
					registryMessageGiven = true;
				 }
				 else if (ChromaseqUtil.buildRequiresForcedRegistration((DNAData)data)) {   //(DNAData)data
					if (!registryMessageGiven)
						logln("Chromaseq registry stored in file is of a defunct version and needs to be rebuilt.");
					ChromaseqUtil.attachStringToMatrix(registryData, new MesquiteString(ChromaseqUtil.MATRIXTODELETE, "extra registration matrix"));
					ChromaseqUtil.createRegistryData(data, this, false);		
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
		//ChromaseqUtil.setChromaseqBuildOfFile(this,  ChromaseqUtil.ChromaseqBuild);

	}
	/*.................................................................................................................*/
	public boolean hasDisconnectedAceFiles(MesquiteFile f) {
		if (f==null)
			return false;
		if (f.getProject()==null)
			return false;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				if (AceDirectoryProcessor.hasDisconnectedAceFiles(data, this))
					return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public void checkNoContigAceFiles(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		count++;
		boolean toChange = false;
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				if (AceDirectoryProcessor.checkNoContigAceFiles(data, this))
					toChange = true;
			}
		}
		resave = toChange;
	}
	/*.................................................................................................................*/
	public boolean processNoContigAceFiles(MesquiteFile f) {
		if (f==null)
			return false;
		if (f.getProject()==null)
			return false;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		count++;
		boolean changed=false;
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
				if (addedBaseData!=null){
					addedBaseData.deleteMe(false);  // don't need this anymore
					MesquiteTrunk.mesquiteTrunk.logln("Deleting unneeded added-bases matrix: " + addedBaseData.getName());
				}
				if (AceDirectoryProcessor.processNoContigAceFiles(data, this))
					changed=true;		
			}
		}
		return changed;
	}
	/*.................................................................................................................*/
	public void removeAllChromaseqLinks(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		count++;
		boolean toChange = false;
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				ChromaseqUtil.purgeChromaseqData(data);
				data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
				data.setDirty(true);
			}
		}
		resave = toChange;
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
	
	boolean removeChromaseqLinks = false;
	/*.................................................................................................................*/
	public void aboutToReadMesquiteBlock(MesquiteFile f) {
		boolean disconnected = hasDisconnectedAceFiles(f);
		boolean checkNoContig = true;
		if (disconnected){
			String helpString = "If you choose the remove the data, it will be more difficult to reconnect the sequence to your chromatograms in the future. " +
			"If you choose to keep the data, you can later reconnect the sequence to your ACE files and chromatograms by using the Show Chromatograms tool "+
			"in the matrix editor.";
			if (AlertDialog.query(containerOfModule(), "Chromaseq data disconnected", "The file specifies that Chromaseq data (chromatograms, ACE files) are linked to sequences, but some of these data can not be found. "+
					"Do you want to remove all Chromaseq data?", "Remove Chromaseq Data", "Keep Data", -1, helpString)) {
				removeChromaseqLinks = true;
			} 
			checkNoContig=false;
		}
		if (checkNoContig)
			checkNoContigAceFiles(f);
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
	/*.................................................................................................................*
	public void deattachChromaseqBuild(MesquiteFile f) {
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
		processNoContigAceFiles(f);
		createRegistryDataIfNeeded(f);
		deleteExtraRegistryMatrices(f);
		if (removeChromaseqLinks)
			removeAllChromaseqLinks(f);
		removeChromaseqLinks = false;
		
		//deattachChromaseqBuild(f);
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {
				MolecularData editedData = ChromaseqUtil.getEditedData(data);
				removeOldNameReferences(editedData);
			}
		}
		if (ChromaseqUtil.isChromaseqDevelopment()){
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
		
		if (resave) {
			FileCoordinator coord = f.getProject().getCoordinatorModule();
			coord.saveFile(f);
		}


	}
}
