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

package mesquite.chromaseq.lib;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

import org.apache.hivemind.util.PropertyUtils;
import org.dom4j.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.align.lib.PairwiseAligner;
import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;
import mesquite.meristic.lib.*;

public class ChromaseqUtil{
	
	public static final int CHROMASEQBLOCKVERSION = 2;
	public static final int ChromaseqBuild = 25;
	public static final int ChromaseqRegistrationBuild = 25;
	public static final int LOWESTBUILDNOTREQUIRINGFORCEDREGISTRATION = 25;
	


	public static Color veryVeryLightBlue = new Color((float)0.92, (float)0.92, (float)0.99);  

	/*  
	registration builds:
	23: first build of new (November 2009), apparently file-format-complete ChromaseqUniversalMapper and ContigMapper scheme
	24: first build with single-read code in, 25 November 2009
	25: build of 27 November 2009, after fixing padding issue
	 * */

	public static final int NORMAL = 0;
	public static final int TRIMMABLE=1;
	public static final int BASECALLED=2;
	public static final int MANUALLYCHANGED = 2;
	public static final int CHECK = 3;
	public static final int CHECK2 = 4;

	
	public static final String PHPHMQVERSION ="2";

	public static final String infoFileName = "info.xml";
	public static final String processedACESuffix = "m";
	public static final String processedFastaFolder = "processedFasta";

	//===========================MATRIX TYPES==============================
	public static final String QUALITYREF ="quality";
	public static final String ORIGINALREF ="original";
	public static final String EDITEDREF ="edited";
	public static final String REGISTRYREF = "registration";
	public static final String REVERSEREGISTRYREF = "reverse registration";
	public static final String ADDEDBASEREF = "added base";
	public static final String MATRIXTODELETE = "matrix to delete";
//	public static final String ADDEDDELETEDBASEREF = "added deleted base";

	//===========================ATTACHABLE handling==============================
	public static final String PHPHIMPORTIDREF = "phphImportID"; //MesquiteString: data
	public static final String GENENAMEREF ="geneName";//MesquiteString: data
	public static final String REGISTRATIONBUILDREF ="chromaseqRegistrationBuild";//MesquiteString: data
	public static final String PHPHMQVERSIONREF ="phphmqVersion";//MesquiteString: data
	public static final String PHPHIMPORTMATRIXTYPEREF ="phphImportMatrixType";//MesquiteString: data

	//===========================ASSOCIABLE handling==============================
	public static final NameReference voucherCodeRef = NameReference.getNameReference("VoucherCode"); //String: taxa
	public static final NameReference voucherDBRef = NameReference.getNameReference("VoucherDB");//String: taxa
	public static final NameReference origTaxonNameRef= NameReference.getNameReference("origName");//String: taxa

	public static final NameReference aceRef = NameReference.getNameReference("aceFile"); //String: tInfo
	public static final NameReference reprocessContigRef = NameReference.getNameReference("reprocessContig"); //String: tInfo
	
	public static final NameReference chromatogramReadsRef = NameReference.getNameReference("chromatogramReads");//String: tInfo

	public static final NameReference origReadFileNamesRef= NameReference.getNameReference("readFileNames");//Strings: tInfo
	public static final NameReference primerForEachReadNamesRef= NameReference.getNameReference("primerForEachRead");//Strings: tInfo
	public static final NameReference sampleCodeNamesRef= NameReference.getNameReference("sampleCodeForEachRead");//Strings: tInfo
	public static final NameReference sampleCodeRef= NameReference.getNameReference("sampleCodeForTaxon");//Strings: tInfo

//	public static final NameReference chromatogramsExistRef = NameReference.getNameReference("chromatogramsExist");//long: tinfo
	public static final NameReference numChromatogramsRef = NameReference.getNameReference("numberOfChromatograms");//long: tinfo
//	public static final NameReference singleReadRef = NameReference.getNameReference("singleRead");//long: tinfo
	public static final NameReference startTrimRef = NameReference.getNameReference("startTrim");//long: tInfo
	public static final NameReference whichContigRef = NameReference.getNameReference("whichContig");	//long, tinfo
	public static final NameReference chromaseqCellFlagsNameRef = NameReference.getNameReference("chromaseqStatus");  //long, tinfo and cells

	public static final NameReference contigMapperRef = NameReference.getNameReference("contigMapper");

	public static final NameReference qualityNameRef = NameReference.getNameReference("phredPhrapQuality"); //double: tinfo

	public static final NameReference paddingRef = NameReference.getNameReference("paddingBefore"); //MesquiteInteger: data(cells)


	/*.................................................................................................................*/

	public static void attachStringToMatrix(Attachable a, MesquiteString s){
		a.attachIfUniqueName(s);
	}
	public static MesquiteString getStringAttached(Attachable a, String s){
		Object obj = a.getAttachment(s);
		if (obj instanceof MesquiteString)
			return (MesquiteString)obj;
		return null;
	}
	public static long getLongAttached(Attachable a, String s){
		Object obj = a.getAttachment(s, MesquiteLong.class);
		if (obj!=null && obj instanceof MesquiteLong)
			return ((MesquiteLong)obj).getValue();
		return MesquiteLong.unassigned;
	}
	//===============================================================================

	public static void setBackground(ContigDisplay contigDisplay, Component component){
		if (contigDisplay!=null && contigDisplay.isReversedInEditedData())
			component.setBackground(ColorDistribution.lightMesquiteBrown);
		else
			component.setBackground(Color.white);

	}

	public static String getStringAssociated(Associable a, NameReference nr, int index){
		return (String)a.getAssociatedObject(nr, index);
	}
	public static void setStringAssociated(Associable a, NameReference nr, int index, String c){
		a.setAssociatedObject(nr, index, c);
	}
	public static String[] getStringsAssociated(Associable a, NameReference nr, int index){
		return (String[])a.getAssociatedObject(nr, index);
	}
	public static void setStringsAssociated(Associable a, NameReference nr, int index, String[] c){
		a.setAssociatedObject(nr, index, c);
	}
	public static long getLongAssociated(Associable a, NameReference nr, int index){
		return a.getAssociatedLong(nr, index);
	}
	public static void setLongAssociated(Associable a, NameReference nr, int index, long c){
		a.setAssociatedLong(nr, index, c);
	}
	public static boolean getBooleanAssociated(Associable a, NameReference nr, int index){
		return a.getAssociatedBit(nr, index);
	}
	public static void setBooleanAssociated(Associable a, NameReference nr, int index, boolean b){
		a.setAssociatedBit(nr, index, b);
	}
	public static double getDoubleAssociated(Associable a, NameReference nr, int index){
		return a.getAssociatedDouble(nr, index);
	}
	public static void setDoubleAssociated(Associable a, NameReference nr, int index, double c){
		a.setAssociatedDouble(nr, index, c);
	}
	public static int getIntegerCellObject(CharacterData data, NameReference nr, int ic, int it){
		Object obj = data.getCellObject(nr, ic, it);
		if (obj != null && obj instanceof MesquiteInteger)
			return ((MesquiteInteger)obj).getValue();
		return MesquiteInteger.unassigned;
	}
	public static void setIntegerCellObject(CharacterData data, NameReference nr, int ic, int it, MesquiteInteger c){
		data.setCellObject(nr, ic, it, c);
	}
	

	public static boolean validChromaseqMatrix(CharacterData data) {
		DNAData originalData = getOriginalData(data);
		if (originalData==null)
			return false;
		return true;
	}

	/*.................................................................................................................*/
	public static ContigMapper getContigMapperAssociated(MolecularData data, int it) {
		if (data==null)
			return null;
		Taxon taxon = data.getTaxa().getTaxon(it);
		Associable tInfo = data.getTaxaInfo(false);
		if (tInfo != null && taxon != null) {
			return (ContigMapper)tInfo.getAssociatedObject(ChromaseqUtil.contigMapperRef, it);
		}
		return null;
	}
	/*.................................................................................................................*/
	public static void setContigMapperAssociated(MolecularData data, int it, ContigMapper contigMapper) {
		if (data==null)
			return;
		Taxon taxon = data.getTaxa().getTaxon(it);
		Associable tInfo = data.getTaxaInfo(true);
		if (tInfo != null && taxon != null) {
			tInfo.setAssociatedObject(ChromaseqUtil.contigMapperRef, it, contigMapper);
		}
//		contigMapper.setTaxonNumber(it);
		contigMapper.setData(data);
	}

	public static boolean isTrimmable(int ic, int it, CharacterData data){
		if (data == null)
			return false;
		if (ic>=0 && it>=0){ 
			int trim =  ChromaseqUtil.getIntegerCellObject(data, chromaseqCellFlagsNameRef, ic, it);
			return trim==ChromaseqUtil.TRIMMABLE;
		}
		return false;
	}
	public static boolean baseCalled(int ic, int it, CharacterData data){
		if (data == null)
			return false;
		if (ic>=0 && it>=0){ 
			int trim =  ChromaseqUtil.getIntegerCellObject(data, chromaseqCellFlagsNameRef, ic, it);
			return trim==ChromaseqUtil.BASECALLED;
		}
		return false;
	}

	public static String getUID(CharacterData data) {
		String uid = "";
		Object obj = ChromaseqUtil.getStringAttached(data, PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			uid = ((MesquiteString)obj).getValue();
		}
		return uid;
	}

	public static int getChromaseqRegistrationBuildOfMatrix(DNAData data) {
		if (data==null)
			return 0;
		Object obj = data.getAttachment(REGISTRATIONBUILDREF, MesquiteLong.class);
		if (obj!=null && obj instanceof MesquiteLong) {
			return (int)((MesquiteLong)obj).getValue();
		}
		return 0;
		
	}
	public static void setChromaseqRegistrationBuildOfMatrix(DNAData data, int build) {
		if (data==null)
			return;
		data.detachAllObjectsOfName(REGISTRATIONBUILDREF);
		data.attach(new MesquiteLong(REGISTRATIONBUILDREF,build));
	}


	public static boolean buildRequiresForcedRegistration(DNAData data) {
		int build = getChromaseqRegistrationBuildOfMatrix(data);
		return build < LOWESTBUILDNOTREQUIRINGFORCEDREGISTRATION;
	}


	/*.................................................................................................................*

	public static int getChromaseqBuildOfFile(MesquiteModule ownerModule) {
		mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock init = (mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock)ownerModule.findNearestColleagueWithDuty(mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock.class);
		if (init !=null) {
			int build =  init.getChromaseqBuildOfFile();
			return build;
		}
		return 0;
	}
	public static void setChromaseqBuildOfFile(MesquiteModule ownerModule, int build) {
		mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock init = (mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock)ownerModule.findNearestColleagueWithDuty(mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock.class);
		if (init !=null) {
			 init.setChromaseqBuildOfFile(build);
		}
	}	
		public static boolean buildRequiresForcedRegistration(MesquiteModule ownerModule) {
		int build = getChromaseqBuildOfFile(ownerModule);
		return build < LOWESTBUILDNOTREQUIRINGFORCEDREGISTRATION;
	}

/*.................................................................................................................*/

	
	public static String getGeneName(CharacterData data) {
		String gn = "";
		Object obj = ChromaseqUtil.getStringAttached(data, GENENAMEREF);
		if (obj!=null && obj instanceof MesquiteString) {
			gn = ((MesquiteString)obj).getValue();
		}
		return gn;
	}


	public static boolean isChromaseqEditedMatrix(CharacterData data) {
		if (!(data instanceof DNAData))
			return false;
		Object obj = ChromaseqUtil.getStringAttached(data, PHPHIMPORTIDREF);
		if (obj==null) {
			return false;
		}
		obj = ChromaseqUtil.getStringAttached(data, PHPHIMPORTMATRIXTYPEREF);
		if (obj instanceof MesquiteString)
			if (((MesquiteString)obj).getValue().equalsIgnoreCase(EDITEDREF)) {
				return true;
			}
		return false;
	}
	
	public static boolean isChromaseqRegistryMatrix(CharacterData data) {
		if (!(data instanceof DNAData))
			return false;
		Object obj = ChromaseqUtil.getStringAttached(data, PHPHIMPORTIDREF);
		if (obj==null) {
			return false;
		}
		obj = ChromaseqUtil.getStringAttached(data, PHPHIMPORTMATRIXTYPEREF);
		if (obj instanceof MesquiteString)
			if (((MesquiteString)obj).getValue().equalsIgnoreCase(REGISTRYREF)) {
				return true;
			}
		return false;
	}


	public static String getPHPHDataType(CharacterData data) {
		Object obj = ChromaseqUtil.getStringAttached(data, PHPHIMPORTMATRIXTYPEREF);
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
	public static String getAceFileDirectory(String directoryName, MesquiteModule ownerModule, DNAData data, int it) {
		if (data==null)
			return null;
		Associable tInfo = data.getTaxaInfo(false);
		String path = null;
		if (tInfo == null)
			return null;
		path = ChromaseqUtil.getStringAssociated(tInfo, ChromaseqUtil.aceRef, it);
		if (StringUtil.blank(path))
			return null;
		path = StringUtil.getAllButLastItem(path,MesquiteFile.fileSeparator );
		if (StringUtil.notEmpty(directoryName))
			path = MesquiteFile.composePath(directoryName, path);
		if (!MesquiteFile.fileOrDirectoryExists(path))
			return null;
		return path;
	}
	/*.................................................................................................................*/
	public static String getAceFileDirectory(MesquiteFile file, MesquiteModule ownerModule, DNAData data, int it) {
		return getAceFileDirectory(file.getDirectoryName(),ownerModule,data,it);
	}
	/*.................................................................................................................*/
	public static boolean reprocessContig(DNAData data, int it) {
		if (data==null)
			return false;
		Associable tInfo = data.getTaxaInfo(false);
		if (tInfo == null)
			return false;
		String s = ChromaseqUtil.getStringAssociated(tInfo, ChromaseqUtil.reprocessContigRef, it);
		return (StringUtil.notEmpty(s));
	}
	/*.................................................................................................................*/
	public static void setReprocessContig(DNAData data, int it) {
		if (data==null)
			return;
		Associable tInfo = data.getTaxaInfo(true);
		if (tInfo == null)
			return;
		ChromaseqUtil.setStringAssociated(tInfo, ChromaseqUtil.reprocessContigRef, it, "reprocess contig");
	}
	/*.................................................................................................................*/
	public static void removeAssociatedObjects(DNAData data, NameReference nr) {
		if (data==null)
			return;
		Associable tInfo = data.getTaxaInfo(true);
		if (tInfo == null)
			return;
		tInfo.removeAssociatedObjects(nr);
	}
	/*.................................................................................................................*/
	public static String getAceFilePath(String directoryName, MesquiteModule ownerModule, DNAData data, int it, boolean returnOriginalAceFile) {
		if (data==null)
			return null;
		Associable tInfo = data.getTaxaInfo(false);
		String path = null;
		if (tInfo == null)
			return null;
		path = ChromaseqUtil.getStringAssociated(tInfo, ChromaseqUtil.aceRef, it);	
		if (StringUtil.blank(path))
			return null;
		if (path.endsWith("m.ace") && returnOriginalAceFile)
			path = path.substring(0,path.length()-5)+".ace";
		if (StringUtil.notEmpty(directoryName))
			path = MesquiteFile.composePath(directoryName, path);
		if (!MesquiteFile.fileExists(path))
			return null;
		return path;
	}

	/*.................................................................................................................*/
	public static String getAceFilePath(MesquiteFile file, MesquiteModule ownerModule, DNAData data, int it,boolean returnOriginalAceFile) {
		return getAceFilePath(file.getDirectoryName(),ownerModule,data,it,returnOriginalAceFile);
	}
	/*.................................................................................................................*/


	public static CharacterData getAssociatedData(CharacterData data, String dataType) {
		if (data==null)
			return null;
		if (data.getProject()==null)
			return null;
		String uid = getUID(data);
		//	String gn = getGeneName(data);
		Object obj;
		ListableVector matrices = data.getProject().getCharacterMatrices();
		for (int i= 0; i< matrices.size(); i++){
			CharacterData d = (CharacterData)matrices.elementAt(i);
			obj = getStringAttached(d,PHPHIMPORTMATRIXTYPEREF);
			MesquiteString aboutToDelete = ChromaseqUtil.getStringAttached(d, MATRIXTODELETE);  // if it is to be deleted it is not the one we want
			if (aboutToDelete==null && obj instanceof MesquiteString)
				if (((MesquiteString)obj).getValue().equalsIgnoreCase(dataType)) {
					obj = getStringAttached(d,PHPHIMPORTIDREF);
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
	
	/*.................................................................................................................*/
	public static boolean isChromaseqDevelopment(){
		return StringArray.indexOf(MesquiteTrunk.startupArguments, "-chromaseqDev")>=0;
	}

	/*.................................................................................................................*/
	public static CategoricalData getAddedBaseData(CharacterData data) {
		CharacterData d = getAssociatedData(data,ADDEDBASEREF);
		if (d instanceof CategoricalData)
			return (CategoricalData)d;
		return null;
	}


	/*.................................................................................................................*
	public static void specifyAsMovedBase(ContigDisplay contigDisplay, CharacterData data, int ic, int it) {
//		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
//		registryData.setState(ic, it, ChromaseqUtil.MOVEDBASEREGISTRY);
//		fillAddedBaseData(data,ic,it);
		//resetNumAddedToStart(contigDisplay, data,it);
		//resetNumAddedToEnd(contigDisplay, data,it);
	}
	/*.................................................................................................................*/
	public static void setStateOfMatrixBase(ContigDisplay contigDisplay, CharacterData data, int ic, int it, long s, boolean recalc) {
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		boolean wasInapplicable = editedData.isInapplicable(ic,it);
		editedData.setState(ic, it, s);
		boolean baseInContig = contigDisplay.baseInContig(ic);
		if (CategoricalState.isInapplicable(s) && !wasInapplicable){
			MeristicData registryData = ChromaseqUtil.getRegistryData(data);
			MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
			int icOriginal = registryData.getState(ic, it);
			registryData.setToInapplicable(ic, it);
			if (icOriginal>=0 && MeristicState.isCombinable(icOriginal)) {
				reverseRegistryData.setToInapplicable(icOriginal, it);
			}
		}
		if (CategoricalState.isInapplicable(s)!=wasInapplicable) {
			if (baseInContig)
				contigDisplay.setBaseInContigDeleted(ic, CategoricalState.isInapplicable(s));
			else { // wasn't in contig, so need to decrease the next bases added
				ContigMapper contigMapper = contigDisplay.getContigMapper();
				ChromaseqUniversalMapper universalMapper = contigDisplay.getUniversalMapper();
				int contigBase = -1;
				for (int i=ic+1; i < editedData.getNumChars(); i++)
					if (contigDisplay.baseInContig(i)){
						contigBase = universalMapper.getOtherBaseFromEditedMatrixBase(ChromaseqUniversalMapper.ACEFILECONTIG, i);
						break;
					}
				if (contigBase<0) { // didn't find it going up, let's look down
					for (int i=ic-1; i >=0; i--)
						if (contigDisplay.baseInContig(i)){
							contigBase = universalMapper.getOtherBaseFromEditedMatrixBase(ChromaseqUniversalMapper.ACEFILECONTIG, i);
							if (contigBase<contigDisplay.getContig().getNumBases()-1) // it's not the end base
								contigBase++;
							else {  // never found it; must be just bases added to the end, so decrease that by one
								contigBase = -1;
								contigMapper.setNumAddedToEnd(contigMapper.getNumAddedToEnd()-1);
							}
								
							break;
						}
				}
				if (contigBase>=0)
					contigMapper.addToAddedBases(contigBase, -1);				
			}
			if (recalc)
				contigDisplay.getContigMapper().recalc(it);
		}
		
	}
	/*.................................................................................................................*/
	public static void specifyBaseAsAdded(ContigDisplay contigDisplay, CharacterData data, int ic, int it, int contigBase, int addToContigBase) {
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
		ContigMapper contigMapper = contigDisplay.getContigMapper();
		
		if (contigBase>=0){  //it matches a contig base; let's resurrect it
			contigMapper.setDeletedBase(contigBase, false);
			if (contigMapper.getIsPadding(contigBase)) {
				//contigMapper.setPadding(contigBase,false);
				//contigMapper.getContig().resetPadding(MesquiteInteger.unassigned, false);
			}
			if (contigBase>=contigDisplay.getNumTrimmedFromStart() && contigBase<= contigDisplay.getContig().getNumBases()-contigMapper.getNumTrimmedFromEnd()) {
				registryData.setState(ic,it,0,contigBase);
				reverseRegistryData.setState(contigBase,it,0,ic);
			}
			contigMapper.recalc(it);
		}
		else if (addToContigBase>=0) {
			contigMapper.addToAddedBases(addToContigBase, 1);

		} else if (contigDisplay.isReversedInEditedData()) {
			for (int icEdited = ic; icEdited>=0; icEdited--){
				int  icOriginal = registryData.getState(icEdited, it);
				if (registryData.isCombinable(icEdited, it) && icOriginal>=0){  // found one that is in contig.
					registryData.setToInapplicable(ic,it);
					contigMapper.addToAddedBases(contigDisplay.getUniversalMapper().getOtherBaseFromEditedMatrixBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, icEdited),1);
					break;
				}
			}
			contigMapper.recalc(it);

		}
		else {  // let's find the next one up that it can be
			for (int icEdited = ic; icEdited<registryData.getNumChars(); icEdited++){
				int  icOriginal = registryData.getState(icEdited, it);
				if (registryData.isCombinable(icEdited, it) && icOriginal>=0){  // found one that is in contig.
					registryData.setToInapplicable(ic,it);
					contigMapper.addToAddedBases(contigDisplay.getUniversalMapper().getOtherBaseFromEditedMatrixBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, icEdited),1);
					break;
				}
			}
			contigMapper.recalc(it);
		}
	}
	/*.................................................................................................................*
	public static boolean isUniversalBase(CharacterData data, int icEdited, int it) {
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		return (!editedData.isInapplicable(icEdited, it));
	}


	/*.................................................................................................................*/
	public static void setAddedBaseDataValues(CategoricalData addedBaseData, CharacterData data, String name, MesquiteString uid, MesquiteString gN) {
		addedBaseData.saveChangeHistory = false;
		data.addToLinkageGroup(addedBaseData); //link matrices!
		addedBaseData.setName("Bases added for " + name + " from Phred/Phrap");
		addedBaseData.setResourcePanelIsOpen(false);
		attachStringToMatrix(addedBaseData,uid);
		attachStringToMatrix(addedBaseData,gN);
		attachStringToMatrix(addedBaseData,new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.ADDEDBASEREF));
		addedBaseData.setLocked(true);
		addedBaseData.setColorCellsByDefault(true);
		addedBaseData.setUseDiagonalCharacterNames(false);
	}
	/*.................................................................................................................*/
	// reverseRegistryData is same size as originalData and contains the positions in the editedData of that original data cell
	public static void fillReverseRegistryData(MeristicData reverseRegistryData) {
		MeristicData registryData = getRegistryData(reverseRegistryData);
		DNAData editedData = getEditedData(reverseRegistryData);
		if (registryData==null)
			return;
		for (int it=0; it<reverseRegistryData.getNumTaxa(); it++) 
			for (int ic=0; ic<reverseRegistryData.getNumChars(); ic++){
				reverseRegistryData.setToInapplicable(ic, it);
			}
		for (int it=0; it<registryData.getNumTaxa() && it<reverseRegistryData.getNumTaxa(); it++) 
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				int mapping = registryData.getState(ic, it);
				if (MesquiteInteger.isCombinable(mapping) && mapping>=0 && mapping<=reverseRegistryData.getNumChars()) {
					if (editedData.isInapplicable(ic, it)) // then even though the registry points into the original data, there is no data in the edited matrix
						reverseRegistryData.setToUnassigned(ic, it);
					else
						reverseRegistryData.setState(mapping, it, 0, ic);
				}
			}
	}
	
	/*.................................................................................................................*/

	public synchronized static void simpleFillRegistryData(CharacterData data) {
		DNAData editedData = getEditedData(data);
		MeristicData registryData = getRegistryData(data);
		if (registryData==null)
			return;
		for (int it=0; it<registryData.getNumTaxa(); it++) {
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				if (editedData.isValidStateOrUnassigned(ic, it))
					registryData.setState(ic, it, 0, ic);
				else
					registryData.setToInapplicable(ic, it);

			}
		}
	}
	/*.................................................................................................................*/

	public synchronized static void reFillRegistries(CharacterData data, int it) {
		DNAData editedData = getEditedData(data);
		MeristicData registryData = getRegistryData(data);
		if (registryData==null)
			return;
		for (int ic=0; ic<registryData.getNumChars(); ic++){
			registryData.setToInapplicable(ic, it);
		}
		PairwiseAligner aligner = PairwiseAligner.getDefaultAligner(editedData);
		inferRegistryDataUsingAlignment(aligner,registryData,it);
		MeristicData reverseRegistryData = getReverseRegistryData(data);
		for (int ic=0; ic<reverseRegistryData.getNumChars(); ic++){
			reverseRegistryData.setToInapplicable(ic, it);
		}
		for (int ic=0; ic<registryData.getNumChars(); ic++){
			int mapping = registryData.getState(ic, it);
			if (MesquiteInteger.isCombinable(mapping) && mapping>=0 && mapping<=reverseRegistryData.getNumChars()) {
				if (editedData.isInapplicable(ic, it)) // then even though the registry points into the original data, there is no data in the edited matrix
					reverseRegistryData.setToUnassigned(ic, it);
				else
					reverseRegistryData.setState(mapping, it, 0, ic);
			}
		}
	}

	/*.................................................................................................................*/

	public static void setReverseRegistryDataValues(MeristicData reverseRegistryData, DNAData originalData, String name, MesquiteString uid, MesquiteString gN) {
		originalData.addToLinkageGroup(reverseRegistryData); //link matrices!
		reverseRegistryData.setName("Reverse Registration Data of " + name + " (for internal bookkeeping)");  //DAVID: if change name here have to change elsewhere
		attachStringToMatrix(reverseRegistryData,uid);
		attachStringToMatrix(reverseRegistryData,gN);
		attachStringToMatrix(reverseRegistryData,new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.REVERSEREGISTRYREF));
		reverseRegistryData.setWritable(false);
		reverseRegistryData.setResourcePanelIsOpen(false);

	}
	/*.................................................................................................................*/

	public static MeristicData createReverseRegistryData(DNAData originalData) {
		MeristicData rr = getReverseRegistryData(originalData);
		if (rr!=null)
			return rr;
		MesquiteString uid = null;
		int originalNumChars = originalData.getNumChars();
		Object obj = getStringAttached(originalData,PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			String dataUID= ((MesquiteString)obj).getValue();
			uid = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataUID);
		}
		MesquiteString gN = null;
		String dataGeneName = "";
		obj = getStringAttached(originalData,GENENAMEREF);
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
		reverseRegistryData.setUserVisible(isChromaseqDevelopment());

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
	public static double getQualityScoreForEditedMatrixBase(CharacterData data, int ic, int it){  // ic is the position in the edited matrix
		ContinuousData qualityData = getQualityData(data);
		MeristicData registryData = getRegistryData(data);
		if (registryData==null || qualityData==null)
			return 0.0;
		int mapping = registryData.getState(ic, it);
		return qualityData.getState(mapping, it, 0);
	}
	/*.................................................................................................................*/
	public static long getOriginalStateForEditedMatrixBase(CharacterData data, int ic, int it){  // ic is the position in the edited matrix
		DNAData originalData = getOriginalData(data);
		MeristicData registryData = getRegistryData(data);
		if (originalData==null || registryData==null)
			return 0;
		int mapping = registryData.getState(ic, it);
		return originalData.getState(mapping, it);
	}
	/*.................................................................................................................*/
	public static int getOriginalBaseNumberOfMatrixPosition(CharacterData data, int ic, int it){  // ic is the position in the edited matrix
		MeristicData registryData = getRegistryData(data);
		if (registryData==null)
			return 0;
		return registryData.getState(ic, it);
	}
	/*.................................................................................................................*/
	public static boolean editedMatrixBaseSameAsOriginal(CharacterData data, int ic, int it){  // ic is the position in the edited matrix
		long originalState = ChromaseqUtil.getOriginalStateForEditedMatrixBase(data, ic, it);
		long currentState = ((DNAData)data).getState(ic,it);
		if (currentState==CategoricalState.inapplicable && originalState==CategoricalState.impossible || currentState==CategoricalState.impossible && originalState==CategoricalState.inapplicable)
			return true;
		if (((DNAData)data).isReversed(it)) 
			return originalState==DNAState.complement(currentState);
		
		return originalState==currentState;
	}
	/*.................................................................................................................*/
	public static boolean originalIsInapplicableForEditedMatrixBase(CharacterData data, int ic, int it){  // ic is the position in the edited matrix
		DNAData originalData = getOriginalData(data);
		MeristicData registryData = getRegistryData(data);
		if (registryData==null)
			return false;
		int mapping = registryData.getState(ic, it);
		return originalData.isInapplicable(mapping, it);
	}


	/*.................................................................................................................*/

	public static void setRegistryDataValues(MeristicData registryData, CharacterData data, String name, MesquiteString uid, MesquiteString gN) {
		registryData.saveChangeHistory = false;
		data.addToLinkageGroup(registryData); //link matrices!
		registryData.setName("Registration of " + name + " from Phred/Phrap");  //DAVID: if change name here have to change elsewhere
		attachStringToMatrix(registryData,uid);
		attachStringToMatrix(registryData,gN);
		attachStringToMatrix(registryData,new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.REGISTRYREF));
		registryData.setResourcePanelIsOpen(false);
		registryData.setEditorInhibition(true);
	}
	
	/*.................................................................................................................*/
	public static long[] getEditedSequence(DNAData editedData, int it) {
		int numChars = editedData.getNumChars();
		long[] editedBases = new long[numChars];
		for (int ic = 0; ic<numChars; ic++){
			long s =  editedData.getState(ic, it);
			if (editedData.isComplemented(it)) 
				s = DNAState.complement(s);
			int base = ic;
			if (editedData.isReversed(it))
				base = numChars-ic-1;
			editedBases[base] = s;
		}
		return editedBases;
	}
	/*.................................................................................................................*/
	public static void purgeChromaseqData(CharacterData data) {
		DNAData editedData = getEditedData(data);
		if (editedData == null)
			return;
		CategoricalData addedBaseData = getAddedBaseData(data);
		if (addedBaseData!=null){
			addedBaseData.deleteMe(false);  
		}
		MeristicData registryData = getRegistryData(data);
		if (registryData!=null){
			registryData.deleteMe(false);  
		}
		MeristicData reverseRegistryData = getReverseRegistryData(data);
		if (reverseRegistryData!=null){
			reverseRegistryData.deleteMe(false);  
		}
		DNAData originalData = getOriginalData(data);
		if (originalData!=null){
			originalData.deleteMe(false);  
		}
		ContinuousData qualityData = getQualityData(data);
		if (qualityData!=null){
			qualityData.deleteMe(false);  
		}
		editedData.detachAllObjectsOfName(ChromaseqUtil.PHPHMQVERSIONREF);
		editedData.detachAllObjectsOfName(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF);
		editedData.detachAllObjectsOfName(ChromaseqUtil.PHPHIMPORTIDREF);
		editedData.detachAllObjectsOfName(ChromaseqUtil.GENENAMEREF);
		editedData.detachAllObjectsOfName(ChromaseqUtil.REGISTRATIONBUILDREF);

		removeAssociatedObjects(editedData, origTaxonNameRef);
		removeAssociatedObjects(editedData, aceRef);
		removeAssociatedObjects(editedData, reprocessContigRef);
		removeAssociatedObjects(editedData, chromatogramReadsRef);
		
		 Associable tInfo = editedData.getTaxaInfo(false);
		 if (tInfo != null){
			 tInfo.removeAssociatedLongs(numChromatogramsRef);
			 tInfo.removeAssociatedLongs(whichContigRef);
			 tInfo.removeAssociatedLongs(startTrimRef);
			 tInfo.removeAssociatedLongs(chromaseqCellFlagsNameRef);
			 tInfo.removeAssociatedDoubles(qualityNameRef);
		 }
		removeAssociatedObjects(editedData, paddingRef);
	//	removeAssociatedObjects(editedData, qualityNameRef);
		removeAssociatedObjects(editedData, contigMapperRef);
		removeAssociatedObjects(editedData, chromaseqCellFlagsNameRef);
	//	removeAssociatedObjects(editedData, whichContigRef);
		removeAssociatedObjects(editedData, startTrimRef);
		removeAssociatedObjects(editedData, sampleCodeRef);
		removeAssociatedObjects(editedData, sampleCodeNamesRef);
		removeAssociatedObjects(editedData, primerForEachReadNamesRef);
		removeAssociatedObjects(editedData, origReadFileNamesRef);
		
}


	/*.................................................................................................................*/
	public static int getWhichContig(DNAData editedData, int it) {
		 Associable tInfo = editedData.getTaxaInfo(false);
		 long whichContig = 0;
		 if (tInfo != null)
			 whichContig = ChromaseqUtil.getLongAssociated(tInfo,ChromaseqUtil.whichContigRef, it);
		 return (int)whichContig;
	}
	/*.................................................................................................................*/
	 public synchronized static void inferContigMapper(PairwiseAligner aligner, MesquiteFile file, DNAData editedData, int it) {
		 if (aligner==null || editedData==null) 
			 return;
		 DNAData originalData = getOriginalData(editedData);
		 AceFile ace = AceFile.getAceFile(file, null, editedData,  it);
		 if (ace ==null){
			 // give warning!!!!!
			 return;
		 }
		 int whichContig = getWhichContig(editedData,it);
		 Contig contig = ace.getContig(whichContig); 
		 
		 if (contig==null) {   // how can this happen???  it does, but how?
			 return;
		 }
		 if (contig.getNumBases()==0)
			 return;

		 
		 //====== first calculate, record, and specify trimmed region from start and end of contigs.  Use alignment to do this  ======= 
		 int numTrimmedFromStart = 0;//contig.getNumBasesOriginallyTrimmedFromStartOfPhPhContig(editedData, it);
		 int numTrimmedFromEnd = 0;
		 long[] contigBases = contig.getSequenceAsLongArray();
		 long[] originalTrimmed = new long[originalData.getNumChars()];
		 for (int ic = 0; ic<originalData.getNumChars(); ic++){
			 originalTrimmed[ic] = originalData.getState(ic, it);
		 }
		 MesquiteNumber alignScore = new MesquiteNumber();
		 aligner.setMaintainOrder(true);
		 long[][] contigOriginalAlignment = aligner.alignSequences(contigBases, originalTrimmed, true, alignScore);
		 int contigSeq = 0;
		 int originalSeq = 1;
		 
	//	 if (!MesquiteInteger.isCombinable(numTrimmedFromStart)){
			 numTrimmedFromStart=0;
			 for (int ic=0; ic<contigOriginalAlignment.length; ic++){
				 if (contigOriginalAlignment[ic][originalSeq]!=CategoricalState.inapplicable) // we've found the first imported, trimmed base, so leave here
					 break;
				 if (contigOriginalAlignment[ic][contigSeq]!=CategoricalState.inapplicable) // we've found the first imported, trimmed base, so leave here
					 numTrimmedFromStart++;
			 }
	//	 }
			 
		 numTrimmedFromEnd=0;
		 for (int ic=contigOriginalAlignment.length-1; ic>=0; ic--){
			 if (contigOriginalAlignment[ic][originalSeq]!=CategoricalState.inapplicable) // we've found the first imported, trimmed base, so leave here
				 break;
			 if (contigOriginalAlignment[ic][contigSeq]!=CategoricalState.inapplicable) // we've found the first imported, trimmed base, so leave here
				 numTrimmedFromEnd++;
		 }

		 int padBeforeTrim = contig.resetPadding(numTrimmedFromStart, false);
//		 numTrimmedFromStart -= padBeforeTrim;


		 ContigMapper contigMapper = ContigMapper.getContigMapper(editedData, null, it);  // this will also attach it!
		 contigMapper.setNumBases(contig.getNumBases());
		 contigMapper.zeroValues();
		 contigMapper.setNumTrimmedFromStart(numTrimmedFromStart);
		 contigMapper.markAsDeletedBasesTrimmedAtStart(numTrimmedFromStart);
		 contigMapper.setNumTrimmedFromEnd(numTrimmedFromEnd);
		 contigMapper.markAsDeletedBasesTrimmedAtEnd(numTrimmedFromEnd);
		 contigMapper.setStoredInFile(true);
		 
		 //======= now figure out bases internally added or deleted using alignment between originalTrimmed and editedData

		 long[] editedBases = getEditedSequence(editedData,it);
		 int editedSeq = 0;
		 long[][] editedOriginalAlignment = aligner.alignSequences(editedBases, originalTrimmed, true, alignScore);

		 int added = 0;
		 int contigBase = numTrimmedFromStart-1;
		 int addedToStart = 0;
//		 for (int ic=editedOriginalAlignment.length-1; ic>=0; ic--){
			 for (int ic=0; ic<editedOriginalAlignment.length; ic++){
			 if (editedOriginalAlignment[ic][originalSeq]!=CategoricalState.inapplicable){   // we've found an original base, i.e., one in contig
				 contigBase++;
				 if (editedOriginalAlignment[ic][editedSeq]==CategoricalState.inapplicable) // there's nothing in the edited - must be deleated
					 contigMapper.setDeletedBase(contigBase, true);
				 else {
					 if (contigBase==numTrimmedFromStart && addedToStart>0 && numTrimmedFromStart>0) {  // special case; need to shift and mark previous ones as not deleted
						 int newStart = contigBase-addedToStart;
						 int numAdded = addedToStart-numTrimmedFromStart;
						 if (numAdded>0 && newStart<=0){
							 contigMapper.setAddedBases(0, numAdded);
						 }
						contigMapper.setDeletedBases(numTrimmedFromStart-addedToStart, numTrimmedFromStart-1, false);
					 }
					 else 
						 contigMapper.setAddedBases(contigBase, added);
				 }
				 added=0;
			 } else if  (editedOriginalAlignment[ic][editedSeq]!=CategoricalState.inapplicable){ // here's one not in contig, but in edited - must be added
				 added++;
				 if (contigBase<numTrimmedFromStart)
					 addedToStart++;
			 }
		 }
		 contigMapper.setNumAddedToEnd(added);
		 

		 //contig.getPolyBaseString(i)
	 }

	/*.................................................................................................................*/

	public synchronized static void inferRegistryDataUsingAlignment(PairwiseAligner aligner, MeristicData registryData, int it) {
		DNAData originalData = getOriginalData(registryData);
		DNAData editedData = getEditedData(registryData);
		if(originalData==null || editedData==null)
			return;

		for (int ic=0; ic<editedData.getNumChars(); ic++){
			registryData.setToInapplicable(ic, it);
		}


		if (aligner!=null) {
			aligner.setMaintainOrder(true);
			MesquiteNumber alignScore = new MesquiteNumber();
			int original = 0;
			int edited = 1;
			 long[] editedBases = getEditedSequence(editedData,it);
			 long[] originalBases = new long[originalData.getNumChars()];
			 for (int ic = 0; ic<originalData.getNumChars(); ic++){
				 originalBases[ic] = originalData.getState(ic, it);
			 }
			long[][] alignment = aligner.alignSequences(originalBases, editedBases, true, alignScore);
			if (alignment!=null) {

				//======  make mapping from the alignment sequences into the originalData and editData matrices ========
				int[] locationInOriginal = new int[alignment.length];
				int icOriginal = 0;
				for (int ic=0; ic<alignment.length; ic++) 
					locationInOriginal[ic] = -1;
				for (int ic=0; ic<alignment.length; ic++) 
					if (alignment[ic][original]!=CategoricalState.inapplicable){ // we've found one in the alignment, now we need to find the same one in the original
						icOriginal = originalData.nextApplicable(it, icOriginal, true);
						if (icOriginal>=0){  // we've found it
							locationInOriginal[ic] = icOriginal;
							icOriginal++;
							if (icOriginal>=originalData.getNumChars())
								break;
						} else
							break;
					}

				int[] locationInEdited = new int[alignment.length];
				for (int ic=0; ic<alignment.length; ic++) 
					locationInEdited[ic] = -1;
				int icEdited = 0;
				if (editedData.isReversed(it))
					icEdited = editedData.getNumChars()-1;
				for (int ic=0; ic<alignment.length; ic++) 
					if (alignment[ic][edited]!=CategoricalState.inapplicable){ // we've found one in the alignment, now we need to find the same one in the original
						if (editedData.isReversed(it)) {
							icEdited = editedData.prevApplicable(it, icEdited, true);
							if (icEdited>=0){  // we've found it
								locationInEdited[ic] = icEdited;
								icEdited--;
								if (icEdited<0)
									break;
							} else
								break;
						} else {
							icEdited = editedData.nextApplicable(it, icEdited, true);
							if (icEdited>=0){  // we've found it
								locationInEdited[ic] = icEdited;
								icEdited++;
								if (icEdited>=editedData.getNumChars())
									break;
							} else
								break;
						}
					}

				//======  now deterimine the boundaries of the sequence in the original sequence in the alignment

				int firstApplicableInOriginal = -1;
				int lastApplicableInOriginal = alignment.length;
				for (int ic=0; ic<alignment.length; ic++) {
					if (alignment[ic][original]!=CategoricalState.inapplicable){
						firstApplicableInOriginal= ic;
						break;
					}
				}
				for (int ic=alignment.length-1; ic>=0; ic--) {
					if (alignment[ic][original]!=CategoricalState.inapplicable) {
						lastApplicableInOriginal= ic;
						break;
					}
				}

				//======  now go through and determine what should be in main part of registry
				for (int ic=0; ic<alignment.length; ic++) {
					boolean originalIsApplicable = alignment[ic][original]!=CategoricalState.inapplicable;
					boolean editedIsApplicable = alignment[ic][edited]!=CategoricalState.inapplicable;

					if (locationInEdited[ic]>=0)
						if (editedIsApplicable && originalIsApplicable){
							registryData.setState(locationInEdited[ic], it, 0, locationInOriginal[ic]);
						}
				}

				//======  find out how many were deleted from one end
				int numDeletedFromStartOfContig = 0;
				int numDeletedFromEndOfContig = 0;
				int firstEditedBase = 0;
				int lastEditedBase = 0;
				int numDeleted = 0;	
				int boundaryBase = -1;
				for (int ic=0; ic<alignment.length; ic++) {
					boolean originalIsApplicable = alignment[ic][original]!=CategoricalState.inapplicable;
					if (locationInEdited[ic]>=0){
						boundaryBase=ic;
						break;
					}
					if (originalIsApplicable) {
						numDeleted++;
					}
				}
				if (editedData.isReversed(it)){
					lastEditedBase=boundaryBase;
					numDeletedFromEndOfContig = numDeleted;
				} else {
					firstEditedBase=boundaryBase;
					numDeletedFromStartOfContig = numDeleted;
				}
				//======  find out how many were deleted from the other end
				numDeleted = 0;	
				boundaryBase = -1;
				for (int ic=alignment.length-1; ic>=0; ic--) {
					boolean originalIsApplicable = alignment[ic][original]!=CategoricalState.inapplicable;
					if (locationInEdited[ic]>=0){
						boundaryBase=ic;
						break;
					}
					if (originalIsApplicable) {
						numDeleted++;
					}
				}
				int reversalFactor = 1;
				if (!editedData.isReversed(it)){
					lastEditedBase=boundaryBase;
					numDeletedFromEndOfContig = numDeleted;
					
				} else {
					firstEditedBase=boundaryBase;
					numDeletedFromStartOfContig = numDeleted;
					reversalFactor=-1;
				}
				
				//======  now process them
				if (numDeletedFromStartOfContig>0 && firstEditedBase>=0){
					int start = locationInEdited[firstEditedBase]-1*reversalFactor;
					int ic2 = locationInOriginal[firstEditedBase]-1;
					for (int ic=0; ic<numDeletedFromStartOfContig; ic++){
						ic2 = originalData.prevApplicable(it, ic2, true);
						if (ic2>=0)
							registryData.setState(start-ic*reversalFactor, it, 0, ic2);
						ic2--;

					}
				}
				if (numDeletedFromEndOfContig>0 && lastEditedBase>=0){
					int start = locationInEdited[lastEditedBase]+1*reversalFactor;
					int ic2 = locationInOriginal[firstEditedBase]+1;
					for (int ic=0; ic<numDeletedFromEndOfContig; ic++) {
						ic2 = originalData.prevApplicable(it, ic2, false);
						if (ic2>=0)
							registryData.setState(start+ic*reversalFactor, it, 0, ic2);
						ic2--;
					}

				}

			}
			

		} else {
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				registryData.setState(ic, it, 0, ic);
			}
		}

	}
	


	/*.................................................................................................................*/

	public synchronized static void inferRegistryData(CharacterData data, int it) {
		DNAData originalData = getOriginalData(data);
		DNAData editedData = getEditedData(data);
		MeristicData registryData = getRegistryData(data);
		if (registryData==null || originalData==null || editedData==null)
			return;
		for (int ic=0; ic<registryData.getNumChars(); ic++){
			registryData.setToInapplicable(ic, it);
		}
		PairwiseAligner aligner = PairwiseAligner.getDefaultAligner(editedData);
		inferRegistryDataUsingAlignment(aligner,registryData,it);
		MesquiteFile file = data.getProject().getHomeFile();
		inferContigMapper(aligner, file, editedData, it);
	}
	/*.................................................................................................................*/

	public synchronized static void inferRegistryData(MeristicData registryData, MesquiteFile file) {
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
		int count=0;
		for (int it=0; it<registryData.getNumTaxa(); it++)  
			if (AceFile.hasAceFilePath(editedData, it)){
				if (count==0) 
					MesquiteTrunk.mesquiteTrunk.logln("Creating Registry Data [" + editedData.getName() + "]");
				else
					MesquiteTrunk.mesquiteTrunk.log(".");
				CommandRecord.tick("Registering sequences for taxon " + editedData.getTaxa().getName(it));
				inferRegistryDataUsingAlignment(aligner,registryData,it);
				MesquiteTrunk.mesquiteTrunk.log(".");
				inferContigMapper(aligner, file, editedData, it);
				count++;
			}
		MesquiteTrunk.mesquiteTrunk.logln("");
		//	fillAddedBaseData(editedData);
	}

	/*.................................................................................................................*/

	public synchronized static void inferRegistryData(MeristicData registryData) {
		if (registryData==null)
			return;
		MesquiteFile file = registryData.getProject().getHomeFile();
		inferRegistryData(registryData,file);
		MeristicData reverseRegistryData = getReverseRegistryData(registryData);	
		if (reverseRegistryData!=null)
			fillReverseRegistryData(reverseRegistryData);


	}

	/*.................................................................................................................*/
	/* called if no registry data are available */
	public static MeristicData createRegistryData(CharacterData data, MesquiteModule ownerModule, boolean defaultRegistry) {
		DNAData editedData = getEditedData(data);
		if (editedData==null)
			return null;
		setChromaseqRegistrationBuildOfMatrix(editedData, ChromaseqRegistrationBuild);

		MesquiteString uid = null;
		Object obj = getStringAttached(data,PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			String dataUID= ((MesquiteString)obj).getValue();
			uid = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataUID);
		}
		MesquiteString gN = null;
		String dataGeneName = "";
		obj = getStringAttached(data,GENENAMEREF);
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
		registryData.setUserVisible(isChromaseqDevelopment());

		setRegistryDataValues(registryData,  data, dataGeneName,  uid,  gN);
		
		if (defaultRegistry){
			simpleFillRegistryData(data);
		}
		else
			inferRegistryData(registryData, file);

		prepareOriginalAndQualityData(data);
		
		//	createReverseRegistryData(registryData, originalData);

		return registryData;
	}
	/*.................................................................................................................*/
	public static Contig getContig(CharacterData data, int it, MesquiteModule ownerModule, boolean warnIfNoAce) {
		DNAData editedData = getEditedData(data);
		if (editedData==null)
			return null;
		Associable tInfo = data.getTaxaInfo(false);
		if (tInfo == null)
			return null;
		AceFile ace = AceFile.getAceFile(ownerModule, editedData, it);
		if (ace == null){
			if (warnIfNoAce)
				ownerModule.alert("Sorry, there seems to be no sequence for that taxon");
			return null;
		}
		long whichContig = ChromaseqUtil.getLongAssociated(tInfo,ChromaseqUtil.whichContigRef, it);
		if (whichContig < 0 || whichContig >= ace.getNumContigs())
			return null;
		return ace.getContig((int)whichContig);
	}



}
