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

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CharactersManager;
import mesquite.align.lib.PairwiseAligner;
import mesquite.charMatrices.ManageCharacters.ManageCharacters;
import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;
import mesquite.meristic.lib.*;

public class ChromaseqUtil{
	
	public static final int CHROMASEQBLOCKVERSION = 2;
	public static final int ChromaseqBuild = 25;
	public static final int LOWESTBUILDNOTREQUIRINGFORCEDREGISTRATION = 25;

	/*  
	builds:
	23: first build of new (November 2009), apparently file-format-complete ChromaseqUniversalMapper and ContigMapper scheme
	24: first build with single-read code in, 25 November 2009
	25: build of 27 November 2009, after fixing padding issue
	 * */

	public static final int TRIMMABLE=1;
	public static final int BASECALLED=2;
	public static final String PHPHMQVERSION ="2";


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
//	public static final String READBUILDREF ="chromaseqBuild";//MesquiteString: data
	public static final String PHPHMQVERSIONREF ="phphmqVersion";//MesquiteString: data
	public static final String PHPHIMPORTMATRIXTYPEREF ="phphImportMatrixType";//MesquiteString: data

/*	public static final int UNCHANGEDBASE = 0;
	public static final int ADDEDBASE = 1;
	public static final int DELETEDBASE = 2;
	public static final int DELETEDBASEREGISTRY = -2;
	public static final int ADDEDBASEREGISTRY = -3;
	public static final int MOVEDBASEREGISTRY = -4;
*/
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



	//===========================ASSOCIABLE handling==============================
	public static final NameReference voucherCodeRef = NameReference.getNameReference("VoucherCode"); //String: taxa
	public static final NameReference voucherDBRef = NameReference.getNameReference("VoucherDB");//String: taxa
	public static final NameReference origTaxonNameRef= NameReference.getNameReference("origName");//String: taxa

	public static final NameReference aceRef = NameReference.getNameReference("aceFile"); //String: tInfo
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
	public static final NameReference trimmableNameRef = NameReference.getNameReference("trimmable"); //long: tInfo, data(ch); MesquiteInteger: data(cells)

	public static final NameReference contigMapperRef = NameReference.getNameReference("contigMapper");//long: tInfo

	public static final NameReference qualityNameRef = NameReference.getNameReference("phredPhrapQuality"); //double: tinfo

	//public static final NameReference trimmableNameRef = NameReference.getNameReference("trimmable"); //long: data(ch); MesquiteInteger: data(cells)

	public static final NameReference paddingRef = NameReference.getNameReference("paddingBefore"); //MesquiteInteger: data(cells)
	//public static final NameReference trimmableNameRef = NameReference.getNameReference("trimmable"); //MesquiteInteger: data(cells)


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
			int trim =  ChromaseqUtil.getIntegerCellObject(data, trimmableNameRef, ic, it);
			return trim==ChromaseqUtil.TRIMMABLE;
		}
		return false;
	}
	public static boolean baseCalled(int ic, int it, CharacterData data){
		if (data == null)
			return false;
		if (ic>=0 && it>=0){ 
			int trim =  ChromaseqUtil.getIntegerCellObject(data, trimmableNameRef, ic, it);
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

	public static int getChromaseqBuildOfFile(MesquiteModule ownerModule) {
		mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock init = (mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock)ownerModule.findNearestColleagueWithDuty(mesquite.chromaseq.ManageChromaseqBlock.ManageChromaseqBlock.class);
		if (init !=null) {
			int build =  init.getChromaseqBuildOfFile();
//			Debugg.println("build: " + build);
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
	public static CategoricalData getAddedBaseData(CharacterData data) {
		CharacterData d = getAssociatedData(data,ADDEDBASEREF);
		if (d instanceof CategoricalData)
			return (CategoricalData)d;
		return null;
	}

	/*.................................................................................................................*

	public static MeristicData getAddedDeletedBaseData(CharacterData data) {
		CharacterData d = getAssociatedData(data,ADDEDDELETEDBASEREF);
		if (d instanceof MeristicData)
			return (MeristicData)d;
		return null;
	}


	/*.................................................................................................................*
	public static void resetNumAddedToStart(ContigDisplay contigDisplay, CharacterData data, int it) {
		int numAdded = getNumAddedToStart(data,it,true);
		contigDisplay.setNumBasesAddedToStart(numAdded);

	}
	/*.................................................................................................................*
	public static int getNumAddedToStart(CharacterData data, int it, boolean includeMoved) {
		int count=0;
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedBaseData!=null && registryData!=null && editedData!=null) {
			for (int ic=0;ic<addedBaseData.getNumChars() && ic<registryData.getNumChars(); ic++) {

				if (registryData.getState(ic, it)==ADDEDBASEREGISTRY || (includeMoved && registryData.getState(ic, it)==MOVEDBASEREGISTRY))
					count++;
				else if (!MesquiteInteger.isCombinable(registryData.getState(ic, it))) { // we still haven't found one that is in the original
					long addedBaseState = addedBaseData.getState(ic, it);
					int addedBaseValue = CategoricalState.getOnlyElement(addedBaseState);
					if (addedBaseValue==ADDEDBASE)
						count++;
				} else
					return count;
			}
		}
		return 0;
	}
	/*.................................................................................................................*
	public static int getNumAddedDeletedToStart(CharacterData data, int it, boolean includeMoved) {
		int count=0;
		MeristicData addedDeletedBaseData = ChromaseqUtil.getAddedDeletedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedDeletedBaseData!=null && registryData!=null && editedData!=null) {
			for (int ic=0;ic<addedDeletedBaseData.getNumChars() && ic<editedData.getNumChars(); ic++) {
				if (!editedData.isInapplicable(ic, it)) {
					if (addedDeletedBaseData.isCombinable(ic, it))
						count += addedDeletedBaseData.getState(ic, it, 0);
				}
				else break;
			}
		}
		return 0;
	}
	/*.................................................................................................................*
	public static void resetNumAddedToEnd(ContigDisplay contigDisplay, CharacterData data, int it) {
		int numAdded = getNumAddedToEnd(data,it, true);
		contigDisplay.setNumBasesAddedToEnd(numAdded);

	}
	/*.................................................................................................................*
	public static int getNumAddedToEnd(CharacterData data, int it, boolean includeMoved) {
		int count=0;
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedBaseData!=null && registryData!=null && editedData!=null) {
			for (int ic=addedBaseData.getNumChars()-1;ic>=0; ic--) {
				if (registryData.getState(ic, it)==ADDEDBASEREGISTRY || (includeMoved && registryData.getState(ic, it)==MOVEDBASEREGISTRY))
					count++;
				else if (!MesquiteInteger.isCombinable(registryData.getState(ic, it))) { // we still haven't found one that is in the original
					long addedBaseState = addedBaseData.getState(ic, it);
					int addedBaseValue = CategoricalState.getOnlyElement(addedBaseState);
					if (addedBaseValue==ADDEDBASE)
						count++;
				} else
					return count;
			}
		}
		return 0;
	}

	/*.................................................................................................................*
	public static int getNumAddedDeletedToEnd(CharacterData data, int it, boolean includeMoved) {
		int count=0;
		MeristicData addedDeletedBaseData = ChromaseqUtil.getAddedDeletedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedDeletedBaseData!=null && registryData!=null && editedData!=null) {
			for (int ic=addedDeletedBaseData.getNumChars()-1;ic>=0; ic--) {
				if (!editedData.isInapplicable(ic, it)) {
					if (addedDeletedBaseData.isCombinable(ic, it))
						count += addedDeletedBaseData.getState(ic, it, 0);
				}
				else break;
			}
		}
		return 0;
	}

	/*.................................................................................................................*
	public static int getTotalNumBasesAddedBeyondPhPhBases(CharacterData data, int it) {
		int count=0;
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedBaseData!=null && registryData!=null && editedData!=null) {
			for (int ic=0;ic<addedBaseData.getNumChars() && ic<registryData.getNumChars(); ic++) {

				if (!MesquiteInteger.isCombinable(registryData.getState(ic, it))) { // we still haven't found one that is in the original
					long addedBaseState = addedBaseData.getState(ic, it);
					int addedBaseValue = CategoricalState.getOnlyElement(addedBaseState);
					if (addedBaseValue==ADDEDBASE)
						count++;
				} 			}
		}
		return count;
	}
	/*.................................................................................................................*
	public static int getTotalNumOriginalBasesTurnedToGaps(CharacterData data, int it) {
		int count=0;
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		if (addedBaseData!=null && registryData!=null && editedData!=null) {
			for (int ic=0;ic<addedBaseData.getNumChars() && ic<registryData.getNumChars(); ic++) {

				if (!MesquiteInteger.isCombinable(registryData.getState(ic, it))) { // we still haven't found one that is in the original
					long addedBaseState = addedBaseData.getState(ic, it);
					int addedBaseValue = CategoricalState.getOnlyElement(addedBaseState);
					if (addedBaseValue==DELETEDBASE)
						count++;
				} 			}
		}
		return count;
	}

	/*.................................................................................................................*
	public static void fillAddedBaseData(CategoricalData addedBaseData, MeristicData registryData, DNAData editedData, int ic, int it) {
		if (addedBaseData!=null && registryData!=null && editedData!=null) {
			int registryState = registryData.getState(ic, it);
			long editedState = editedData.getState(ic, it);
			if (!MesquiteInteger.isCombinable(registryState)) { // then the original data doesn't have a state here
				if (!CategoricalState.isInapplicable(editedState))  // the edited data has something in it
					addedBaseData.setState(ic, it, CategoricalState.makeSet(ADDEDBASE));  // mark it as having an added base
				else
					addedBaseData.setToUnassigned(ic, it);
				registryData.setToUnassigned(ic, it);  //set it to unassigned as a mark that it is extra

			}
			else {  // the original data DOES have a state here
				if (CategoricalState.isInapplicable(editedState))  // the edited data DOESN'T have something in it
					addedBaseData.setState(ic, it, CategoricalState.makeSet(DELETEDBASE));  // mark it as having a base removed
			}

		}

	}

	/*.................................................................................................................*
	public static void setNewGap(CharacterData data, int ic, int it) {
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
		int icOriginal = registryData.getState(ic, it);
		registryData.setState(ic, it, DELETEDBASEREGISTRY);  // registry now says that there is nothing in original data here
		reverseRegistryData.setState(icOriginal, it, DELETEDBASEREGISTRY);  // registry now says that there is nothing in original data here
	}

	/*.................................................................................................................*
	public static void specifyAsAddedBase(CharacterData data, int ic, int it) {
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		registryData.setState(ic, it, ChromaseqUtil.ADDEDBASEREGISTRY);
		fillAddedBaseData(data,ic,it);
	}
	/*.................................................................................................................*/
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
		
	//	fillAddedBaseData(data,ic,it);
	//	resetNumAddedToStart(contigDisplay, data,it);
	//	resetNumAddedToEnd(contigDisplay, data,it);
	}
	/*.................................................................................................................*
	public static void insertGapIntoEditedMatrix(CharacterData data, int ic, int it) {
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		int icOriginal = registryData.getState(ic, it);
		registryData.setState(ic, it, ChromaseqUtil.ADDEDBASEREGISTRY);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
		if ( icOriginal>=0)
			for (int originalBase=icOriginal; originalBase<reverseRegistryData.getNumChars(); originalBase++) {
			int posInEdited = reverseRegistryData.getState(ic, it)+1;
			reverseRegistryData.setState(ic, it, posInEdited);
		}
		fillAddedBaseData(data,ic,it);
	}
	/*.................................................................................................................*/
	public static void specifyBaseAsAdded(ContigDisplay contigDisplay, CharacterData data, int ic, int it, int contigBase, int addToContigBase) {
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
		ContigMapper contigMapper = contigDisplay.getContigMapper();
		
		if (contigBase>=0){  //it matches a contig base; let's resurrect it
//			if (!contigMapper.getDeletedBase(contigBase))
//				Debugg.println("base not deleted! " + contigBase);
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
				if (icOriginal>=0){  // found one that is in contig.
					contigMapper.addToAddedBases(contigDisplay.getUniversalMapper().getOtherBaseFromEditedMatrixBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, icEdited),1);
					break;
				}
			}
			contigMapper.recalc(it);

		}
		else {  // let's find the next one up that it can be
			for (int icEdited = ic; icEdited<registryData.getNumChars(); icEdited++){
				int  icOriginal = registryData.getState(icEdited, it);
				if (icOriginal>=0){  // found one that is in contig.
					contigMapper.addToAddedBases(contigDisplay.getUniversalMapper().getOtherBaseFromEditedMatrixBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, icEdited),1);
					break;
				}
			}
			contigMapper.recalc(it);
		}
	}
	/*.................................................................................................................*/
	public static boolean isUniversalBase(CharacterData data, int icEdited, int it) {
		/*MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		if (registryData==null) return false;
		int icOriginal = registryData.getState(icEdited, it);
		DNAData originalData = ChromaseqUtil.getOriginalData(data);*/
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		return (!editedData.isInapplicable(icEdited, it));
	}

	/*.................................................................................................................*
	public static void fillAddedBaseData(CharacterData data, int ic, int it) {
		CategoricalData addedBaseData = ChromaseqUtil.getAddedBaseData(data);
		MeristicData registryData = ChromaseqUtil.getRegistryData(data);
		DNAData editedData = ChromaseqUtil.getEditedData(data);
		fillAddedBaseData(addedBaseData,registryData, editedData, ic,it);
	}
	/*.................................................................................................................*
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
	/*.................................................................................................................*
	public static void fillAddedBaseData(ContigDisplay contigDisplay, CharacterData data, int it) {
		fillAddedBaseData(data, it);
//		resetNumAddedToStart(contigDisplay, data,it);
//		resetNumAddedToEnd(contigDisplay, data,it);
	}


	/*.................................................................................................................*
	public static void fillAddedBaseData(CharacterData data) {
		if (data==null)
			return;
		for (int it=0;it<data.getNumTaxa(); it++) 
			fillAddedBaseData(data,it);
	}


	/*.................................................................................................................*
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
	/*.................................................................................................................*
	public static void setAddedDeletedBaseDataValues(MeristicData addedDeletedBaseData, CharacterData data, String name, MesquiteString uid, MesquiteString gN) {
		addedDeletedBaseData.saveChangeHistory = false;
		data.addToLinkageGroup(addedDeletedBaseData); //link matrices!
		addedDeletedBaseData.setName("Number of bases added and deleted for " + name + " from Phred/Phrap");
		addedDeletedBaseData.setResourcePanelIsOpen(false);
		attachStringToMatrix(addedDeletedBaseData,uid);
		attachStringToMatrix(addedDeletedBaseData,gN);
		attachStringToMatrix(addedDeletedBaseData,new MesquiteString(ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF, ChromaseqUtil.ADDEDDELETEDBASEREF));
		addedDeletedBaseData.setLocked(true);
		addedDeletedBaseData.setColorCellsByDefault(true);
		addedDeletedBaseData.setUseDiagonalCharacterNames(false);
	}
	/*.................................................................................................................*

	public static CategoricalData createAddedBaseData(CharacterData data) {
		CategoricalData addedBaseData = getAddedBaseData(data);
		if (addedBaseData!=null)
			return addedBaseData;
		MesquiteString uid = null;
		Object obj = ChromaseqUtil.getStringAttached(data, PHPHIMPORTIDREF);
		if (obj!=null && obj instanceof MesquiteString) {
			String dataUID= ((MesquiteString)obj).getValue();
			uid = new MesquiteString(ChromaseqUtil.PHPHIMPORTIDREF, dataUID);
		}
		MesquiteString gN = null;
		String dataGeneName = "";
		obj = ChromaseqUtil.getStringAttached(data, GENENAMEREF);
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
		addedBaseData.setUserVisible(isChromaseqDevelopment());

		setAddedBaseDataValues(addedBaseData, data, dataGeneName, uid, gN);

		//	createReverseRegistryData(registryData, originalData);

		return addedBaseData;
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
		if (registryData==null)
			return 0.0;
		int mapping = registryData.getState(ic, it);
		return qualityData.getState(mapping, it, 0);
	}
	/*.................................................................................................................*/
	public static long getOriginalStateForEditedMatrixBase(CharacterData data, int ic, int it){  // ic is the position in the edited matrix
		DNAData originalData = getOriginalData(data);
		MeristicData registryData = getRegistryData(data);
		if (registryData==null)
			return 0;
		int mapping = registryData.getState(ic, it);
		return originalData.getStateRaw(mapping, it);
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
	 public synchronized static void inferContigMapper(PairwiseAligner aligner, MesquiteFile file, DNAData editedData, int it) {
		 if (aligner==null || editedData==null) 
			 return;
		 DNAData originalData = getOriginalData(editedData);
		 AceFile ace = AceFile.getAceFile(file, null, editedData,  it);
		 Associable tInfo = editedData.getTaxaInfo(false);
		 long whichContig = 0;
		 if (tInfo != null)
			 whichContig = ChromaseqUtil.getLongAssociated(tInfo,ChromaseqUtil.whichContigRef, it);
		 Contig contig = ace.getContig((int)whichContig); 


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
		 
//		 Debugg.println("contigMapper inference, it: " + it + ", numTrimmedFromStart: " + numTrimmedFromStart);
		 //======= now figure out bases internally added or deleted using alignment between originalTrimmed and editedData

		 long[] editedBases = new long[editedData.getNumChars()];
		 for (int ic = 0; ic<editedData.getNumChars(); ic++){
			 editedBases[ic] = editedData.getState(ic, it);
		 }
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
			MesquiteNumber alignScore = new MesquiteNumber();
			int original = 0;
			int edited = 1;
			long[][] alignment = aligner.getAlignment(originalData,  it, editedData, it, alignScore, true);
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
				for (int ic=0; ic<alignment.length; ic++) 
					if (alignment[ic][edited]!=CategoricalState.inapplicable){ // we've found one in the alignment, now we need to find the same one in the original
						icEdited = editedData.nextApplicable(it, icEdited, true);
						if (icEdited>=0){  // we've found it
							locationInEdited[ic] = icEdited;
							icEdited++;
							if (icEdited>=editedData.getNumChars())
								break;
						} else
							break;
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
						} else if (editedIsApplicable) {  // but original has nothing, must be a new base in the sequence
							if (ic<firstApplicableInOriginal || ic> lastApplicableInOriginal){ //then these must be end bases, that were moved there
					//			registryData.setState(locationInEdited[ic], it, 0, MOVEDBASEREGISTRY);
							}
							else {
					//			registryData.setState(locationInEdited[ic], it, 0, ADDEDBASEREGISTRY);
							}
						} else if (originalIsApplicable) {  // but there is nothing in the editedData!
					//		registryData.setState(locationInEdited[ic], it, 0, DELETEDBASEREGISTRY);
							//if (locationInOriginal[ic]>=0 && reverseRegistryData !=null)
							//reverseRegistryData.setState(locationInOriginal[ic], it,0, DELETEDBASEREGISTRY);
						}
				}

				//======  find out how many were deleted from start
				int numDeleted = 0;	
				int firstEditedBase = -1;
				for (int ic=0; ic<alignment.length; ic++) {
					boolean originalIsApplicable = alignment[ic][original]!=CategoricalState.inapplicable;
					if (locationInEdited[ic]>=0){
						firstEditedBase=ic;
						break;
					}
					if (originalIsApplicable) {
						numDeleted++;
					}
				}
				if (numDeleted>0 && firstEditedBase>=0){
					int start = locationInEdited[firstEditedBase]-1;
					int ic2 = locationInOriginal[firstEditedBase]-1;
					for (int ic=0; ic<numDeleted; ic++){
						ic2 = originalData.prevApplicable(it, ic2, true);
						if (ic2>=0)
							registryData.setState(start-ic, it, 0, ic2);
						ic2--;

					}

				}
				//======  now take care of deleted from end
				numDeleted = 0;	
				int lastEditedBase = -1;
				for (int ic=alignment.length-1; ic>=0; ic--) {
					boolean originalIsApplicable = alignment[ic][original]!=CategoricalState.inapplicable;
					if (locationInEdited[ic]>=0){
						lastEditedBase=ic;
						break;
					}
					if (originalIsApplicable) {
						numDeleted++;
					}
				}
				if (numDeleted>0 && lastEditedBase>=0){
					int start = locationInEdited[lastEditedBase]+1;
					int ic2 = locationInOriginal[firstEditedBase]+1;
					for (int ic=0; ic<numDeleted; ic++) {
						ic2 = originalData.prevApplicable(it, ic2, false);
						if (ic2>=0)
							registryData.setState(start+ic, it, 0, ic2);
						ic2--;
					}

				}

			}
			

		} else {

			//Debugg.println("sequence " + it + " (" + originalData.getTaxa().getTaxonName(it) + ") with length difference " + lengthDifference);
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				registryData.setState(ic, it, 0, ic);
			}
		}
		/*int startChar = -1;
			int endChar=-1;
			for (int ic = 0; ic<editedData.getNumChars(); ic++) {
				if (registryData.isInapplicable(ic, it)||registryData.isUnassigned(ic, it)) {
					if (editedData.isValidAssignedState(ic, it))
						registryData.setState(ic, it, 0, MOVEDBASEREGISTRY);
				} else { // we've found a non-applicable, time to exit loop
					startChar=ic;
					break;
				}
			}
			for (int ic = editedData.getNumChars()-1; ic>=0; ic--) {
				if (registryData.isInapplicable(ic, it)||registryData.isUnassigned(ic, it)) {
					if (editedData.isValidAssignedState(ic, it))
						registryData.setState(ic, it, 0, MOVEDBASEREGISTRY);
				} else { // we've found a non-applicable, time to exit loop
					endChar=ic;
					break;
				}
			}
			for (int ic = startChar; ic<=endChar; ic++) {
				if ((registryData.isInapplicable(ic, it)||registryData.isUnassigned(ic, it))) {
					if (editedData.isValidAssignedState(ic, it))
						registryData.setState(ic, it, 0, ADDEDBASEREGISTRY);
				} 
			}
		 */

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
		for (int it=0; it<registryData.getNumTaxa(); it++)  {
			if (it==0) 
				MesquiteTrunk.mesquiteTrunk.logln("Creating Registry Data [" + editedData.getName() + "]");
			else
				MesquiteTrunk.mesquiteTrunk.log(".");
			CommandRecord.tick("Registering sequences for taxon " + (it+1));
			inferRegistryDataUsingAlignment(aligner,registryData,it);
			if (it==editedData.getNumTaxa()-1)
				MesquiteTrunk.mesquiteTrunk.logln(".");
			else
				MesquiteTrunk.mesquiteTrunk.log(".");
			//CommandRecord.tick("Registering contig from ACE file for taxon " + (it+1));
			inferContigMapper(aligner, file, editedData, it);
		}
	//	fillAddedBaseData(editedData);
	}

	/*.................................................................................................................*/
	/* called if no registry data are available */
	public static MeristicData createRegistryData(CharacterData data, MesquiteModule ownerModule) {
//		if (rD!=null)
//			rD.deleteMe(false);
		DNAData editedData = getEditedData(data);
/*		Debugg.println("\n\n|||||||||||||||||\nbefore detach\n" + editedData.listAttachments());
		editedData.detach(ChromaseqUtil.READBUILDREF);
		Debugg.println("\nafter detach\n" + editedData.listAttachments());
		editedData.attachIfUniqueName(new MesquiteLong(ChromaseqUtil.READBUILDREF, ChromaseqBuild));
		Debugg.println("\nafter attach\n" + editedData.listAttachments()+"\n|||||||||||||||||\n");
*/
		
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


		inferRegistryData(registryData, file);

		prepareOriginalAndQualityData(data);

		//	createReverseRegistryData(registryData, originalData);

		return registryData;
	}
	public static boolean isChromaseqDevelopment(){
		return StringArray.indexOf(MesquiteTrunk.startupArguments, "-chromaseqDev")>=0;
	}
}
