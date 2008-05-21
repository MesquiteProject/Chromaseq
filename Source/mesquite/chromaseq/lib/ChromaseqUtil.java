package mesquite.chromaseq.lib;

import java.util.Vector;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
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


   	
   	public static ContinuousData getQualityData(CharacterData data) {
		String uid = "";
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			uid = ((MesquiteString)obj).getValue();
		}
		ListableVector matrices = data.getProject().getCharacterMatrices();
		for (int i= 0; i< matrices.size(); i++){
			CharacterData d = (CharacterData)matrices.elementAt(i);
			if (d instanceof ContinuousData) {
				obj = d.getAttachment(PHPHIMPORTMATRIXTYPEREF);
				if (obj instanceof MesquiteString)
					if (((MesquiteString)obj).getValue().equalsIgnoreCase(QUALITYREF)) {
						obj = data.getAttachment(PHPHIMPORTIDREF);
						if (obj instanceof MesquiteString) {
							if (uid.equalsIgnoreCase(((MesquiteString)obj).getValue()))
									return (ContinuousData)d;
						}
					}
			}
		}
   		return null;
   	}
   	
   	public static DNAData getOriginalData(CharacterData data) {
		String uid = "";
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			uid = ((MesquiteString)obj).getValue();
		}
		ListableVector matrices = data.getProject().getCharacterMatrices();
		for (int i= 0; i< matrices.size(); i++){
			CharacterData d = (CharacterData)matrices.elementAt(i);
			if (d instanceof DNAData) {
				obj = d.getAttachment(PHPHIMPORTMATRIXTYPEREF);
				if (obj instanceof MesquiteString)
					if (((MesquiteString)obj).getValue().equalsIgnoreCase(ORIGINALREF)) {
						obj = data.getAttachment(PHPHIMPORTIDREF);
						if (obj instanceof MesquiteString) {
							if (uid.equalsIgnoreCase(((MesquiteString)obj).getValue()))
									return (DNAData)d;
						}
					}
			}
		}
   		return null;
   	}
   
   	public static DNAData getEditedData(CharacterData data) {
		String uid = "";
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			uid = ((MesquiteString)obj).getValue();
		}
		ListableVector matrices = data.getProject().getCharacterMatrices();
		for (int i= 0; i< matrices.size(); i++){
			CharacterData d = (CharacterData)matrices.elementAt(i);
			if (d instanceof DNAData) {
				obj = d.getAttachment(PHPHIMPORTMATRIXTYPEREF);
				if (obj instanceof MesquiteString)
					if (((MesquiteString)obj).getValue().equalsIgnoreCase(EDITEDREF)) {
						obj = data.getAttachment(PHPHIMPORTIDREF);
						if (obj instanceof MesquiteString) {
							if (uid.equalsIgnoreCase(((MesquiteString)obj).getValue()))
									return (DNAData)d;
						}
					}
			}
		}
   		return null;
   	}

   	
   	public static MeristicData getRegistryData(CharacterData data) {
		String uid = "";
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			uid = ((MesquiteString)obj).getValue();
		}
		ListableVector matrices = data.getProject().getCharacterMatrices();
		for (int i= 0; i< matrices.size(); i++){
			CharacterData d = (CharacterData)matrices.elementAt(i);
			if (d instanceof MeristicData) {
				obj = d.getAttachment(PHPHIMPORTMATRIXTYPEREF);
				if (obj instanceof MesquiteString)
					if (((MesquiteString)obj).getValue().equalsIgnoreCase(REGISTRYREF)) {
						obj = data.getAttachment(PHPHIMPORTIDREF);
						if (obj instanceof MesquiteString) {
							if (uid.equalsIgnoreCase(((MesquiteString)obj).getValue()))
									return (MeristicData)d;
						}
					}
			}
		}
   		return null;
   	}
   	public static MeristicData getReverseRegistryData(CharacterData data) {
		String uid = "";
		Object obj = data.getAttachment(PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			uid = ((MesquiteString)obj).getValue();
		}
		ListableVector matrices = data.getProject().getCharacterMatrices();
		for (int i= 0; i< matrices.size(); i++){
			CharacterData d = (CharacterData)matrices.elementAt(i);
			if (d instanceof MeristicData) {
				obj = d.getAttachment(PHPHIMPORTMATRIXTYPEREF);
				if (obj instanceof MesquiteString)
					if (((MesquiteString)obj).getValue().equalsIgnoreCase(REVERSEREGISTRYREF)) {
						obj = data.getAttachment(PHPHIMPORTIDREF);
						if (obj instanceof MesquiteString) {
							if (uid.equalsIgnoreCase(((MesquiteString)obj).getValue()))
									return (MeristicData)d;
						}
					}
			}
		}
   		return null;
   	}

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


   	public static MeristicData createReverseRegistryData(MeristicData registryData, DNAData originalData) {
   		MeristicData rr = getReverseRegistryData(originalData);
   		if (rr!=null)
   			return rr;
		MesquiteString uid = null;
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
		originalData.addToLinkageGroup(reverseRegistryData); //link matrices!
		reverseRegistryData.setName("Reverse Registration Data (for internal bookkeeping)");  //DAVID: if change name here have to change elsewhere
		reverseRegistryData.attachIfUniqueName(uid);
		reverseRegistryData.attachIfUniqueName(gN);
		reverseRegistryData.attachIfUniqueName(new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.REVERSEREGISTRYREF));
		reverseRegistryData.setWritable(false);
		
		fillReverseRegistryData(reverseRegistryData);
		reverseRegistryData.setEditorInhibition(true);

		return reverseRegistryData;
   	}
   	
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
		MeristicData registrationData;	
		registrationData =  (MeristicData)manageCharacters.newCharacterData(data.getTaxa(), data.lastApplicable()+1, MeristicData.DATATYPENAME);  //
		registrationData.saveChangeHistory = false;
		registrationData.addToFile(file, data.getProject(), manageCharacters);  
		data.addToLinkageGroup(registrationData); //link matrices!
		registrationData.setName("Registration of " + dataGeneName + " from Phred/Phrap");  //DAVID: if change name here have to change elsewhere
		registrationData.attachIfUniqueName(uid);
		registrationData.attachIfUniqueName(gN);
		registrationData.attachIfUniqueName(new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.REGISTRYREF));
		
		for (int it=0; it<registrationData.getNumTaxa(); it++) 
			for (int ic=0; ic<registrationData.getNumChars(); ic++){
				registrationData.setState(ic, it, 0, ic);
			}
		registrationData.setEditorInhibition(true);

		prepareOriginalAndQualityData(data);

	//	createReverseRegistryData(registrationData, originalData);
		
		return registrationData;
   	}
}
