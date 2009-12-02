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

import java.awt.Color;
import java.awt.Graphics;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.MolecularData;
import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;
import mesquite.meristic.lib.MeristicData;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;

public class ContigMapper {
	int numAddedToEnd, numResurrectedAtStart;
	int numResurrectedAtEnd;
	int numDeletedFromStart, numDeletedFromEnd;
	int numTrimmedFromEnd=0;
	int numTrimmedFromStart = 0;
	int[] addedBefore;
	int[] totalAddedDeletedBefore, totalAddedDeletedAfter;
	int[] totalAddedBefore;
	boolean[] deleted;
	int numBases=0;
	Contig contig=null;
	boolean storedInFile = false;
	boolean 	hasBeenSetUp = false;
	MolecularData editedData = null;
	double matchToEditedScore = MesquiteDouble.unassigned;

	public ContigMapper (Contig contig) {
		this.contig = contig;
		init();
	}
	public ContigMapper () {
		init();
	}

	/*.................................................................................................................*/
	public int getContigBaseFromOriginalMatrixBase(int originalMatrixBase, int it) {
		if (editedData==null || contig==null)
			return originalMatrixBase;
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		int trimmedBase = originalData.numValidStateOrUnassigned(0, originalMatrixBase, it)-1;  // how many trimmed bases are up to that point
		int contigBase = contig.getContigBaseFromTrimmedBase(trimmedBase);
		return contigBase;  //-startingAddedBeforeOriginalTrim
	}
	
	/*.................................................................................................................*/
	public int getMatrixBaseFromContigBase(int contigBase, int it) {
		if (editedData==null || contig==null)
			return contigBase;
		int trimmedBase = contig.getTrimmedBaseFromContigBase(contigBase);
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		int count =-1;
		for (int ic=0; ic<=originalData.getNumChars(); ic++)
			if (originalData.isValidStateOrUnassigned(ic,it)){
				count++;
				if (count==trimmedBase) {  // we've found as many states as trimmedBases
					break;
				}
			}
		if (count>=0) {
			MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(editedData);
			return reverseRegistryData.getState(count,it);
		}
		return contigBase;  
	}


	/*.................................................................................................................*/
	public static ContigMapper getContigMapper (MolecularData data, Contig contig, int it) {
		ContigMapper contigMapper = ChromaseqUtil.getContigMapperAssociated(data, it);
		if (contigMapper==null) {
			contigMapper = new ContigMapper(contig);
			ChromaseqUtil.setContigMapperAssociated(data, it, contigMapper);
		}
		return contigMapper;
	}
	/*.................................................................................................................*/
	public static ContigMapper getContigMapper (MolecularData data, Contig contig, int it, int numTrimmedFromStart) {
		ContigMapper contigMapper = ChromaseqUtil.getContigMapperAssociated(data, it);
		if (contigMapper==null) {
			contigMapper = new ContigMapper(contig);
			ChromaseqUtil.setContigMapperAssociated(data, it, contigMapper);
			if (MesquiteInteger.isCombinable(numTrimmedFromStart) && numTrimmedFromStart>=0)
				contigMapper.setNumTrimmedFromStart(numTrimmedFromStart);
		}
		return contigMapper;
	}
	/*.................................................................................................................*
	public static void checkTaxonNumbers(MolecularData editedData) {
		for (int it = 0; it<editedData.getNumTaxa(); it++){
			Taxon taxon = editedData.getTaxa().getTaxon(it);
			Associable tInfo = editedData.getTaxaInfo(false);
			if (tInfo != null && taxon != null) {
				ContigMapper contigMapper = ChromaseqUtil.getContigMapperAssociated(editedData, it);
				if (contigMapper!=null){
					contigMapper.setTaxonNumber(it);
				}
			}
		}
	}
	/*.................................................................................................................*/
	public void setData(MolecularData data) {
		MolecularData editedData = ChromaseqUtil.getEditedData(data);
		this.editedData = editedData;
	}
	/*.................................................................................................................*/
	public void recalcPart (){
		int count = 0;
		for (int ic = 0; ic<addedBefore.length; ic++){
			count += addedBefore[ic] ;
			totalAddedDeletedBefore[ic] = count;
			totalAddedBefore[ic] = count;
		}
		count=0;
		for (int ic = 0; ic<deleted.length; ic++){
			totalAddedDeletedBefore[ic] -= count;
			if (deleted[ic])
				count++;
		}
		count = 0;
		for (int ic = addedBefore.length-1; ic>=0; ic--){
			count += addedBefore[ic] ;
			totalAddedDeletedAfter[ic] = count;
		}
		count=0;
		for (int ic = deleted.length-1; ic>=0; ic--){
			totalAddedDeletedAfter[ic] -= count;
			if (deleted[ic])
				count++;
		}
		totalAddedDeletedAfter[deleted.length-1] -= numDeletedFromEnd;

		count=0;
		for (int ic = numTrimmedFromStart; ic>=0; ic--){
			if (!deleted[ic] && ic<numTrimmedFromStart)
				count++;
		}
		setNumResurrectedAtStart(count);

		count=0;
		for (int ic = numBases-numTrimmedFromEnd; ic<deleted.length; ic++){
			if (!deleted[ic] && ic>numBases-numTrimmedFromEnd-1)
				count++;
		}
		setNumResurrectedAtEnd(count);

	}
	/*.................................................................................................................*/
	public void recalc(MolecularData editedData, int it) {
		setData(editedData);
		recalc(it);
	}
	/*.................................................................................................................*/
	public void calcEndTrim(int it) {
		if (editedData==null)
			return;
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		int contigBase = numTrimmedFromStart-1;
		int lastContigBaseInOriginal = -1;

		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			if (originalData.isValidStateOrUnassigned(ic, it)){ // an original state is here!
				contigBase++;
				lastContigBaseInOriginal=contigBase;
			}
		}
		int num = contig.getNumBases()-lastContigBaseInOriginal-1;
		setNumTrimmedFromEnd(num);
		for (int ic = 0; ic< num; ic++){  
			setDeletedBase(contig.getNumBases()-ic-1, true);
		}
	}
	/*.................................................................................................................*/
	/** This method does the basic calculations to summarize various aspects of the added and deleted bases; it does not alter the core storage about which bases are deleted and how many are added.*/
	public void recalc(int it) {
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(editedData);
		MeristicData registryData = ChromaseqUtil.getRegistryData(editedData);
		//	this.numTrimmedFromStart = numTrimmedFromStart;
		int contigBase = numTrimmedFromStart-1;
		int lastContigBaseInOriginal = -1;
		int lastEditedBaseInOriginal = -1;
		int deletedAtEnd = 0;

		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			int positionInEdited = reverseRegistryData.getState(ic, it,0);
			if (originalData.isValidStateOrUnassigned(ic, it)){ // an original state is here!
				contigBase++;
				if (getDeletedBase(contigBase)){  
					deletedAtEnd++;
				}
				else{  // is in edited, so reset deletedAtEnds
					deletedAtEnd=0;
					lastEditedBaseInOriginal = positionInEdited;  // record last base in edited which corresponds to one in original
				}
				lastContigBaseInOriginal=contigBase;
			}
		}
		int numFromEnd = numBases-lastContigBaseInOriginal-1;


		setNumDeletedFromEnd(deletedAtEnd);
		setNumTrimmedFromEnd(numFromEnd);
		int addedBase=0;
		int addedToEnd=0;
		for (int ic = 0; ic< editedData.getNumChars(); ic++){  
			if (editedData.isValidStateOrUnassigned(ic, it)){ // a state is here in the edited data
				int positionInOriginal = registryData.getState(ic, it);
				if (positionInOriginal<0 || !registryData.isCombinable(ic, it) || !originalData.isValidStateOrUnassigned(positionInOriginal, it)){  // not in original!
					addedBase++;
					if (ic>=lastEditedBaseInOriginal)
						addedToEnd++;
				}
				else { // it is in original; now record added bases
					addedBase=0;
				}
			}
		}

		setNumAddedToEnd(addedToEnd);
		recalcPart();
		
		hasBeenSetUp = true;

	}
	
	/*.................................................................................................................*/
	public void checkContig(MolecularData editedData, int it, MesquiteModule ownerModule) {
		if (getContig()==null && ownerModule!=null) {
			Contig contig = 	ChromaseqUtil.getContig(editedData,it,ownerModule, false);
			if (contig!=null)
				setContig(contig);
		}
	}

	/*.................................................................................................................*/
	public void inferFromExistingRegistry(MolecularData editedData, int it, MesquiteModule ownerModule) {
		inferFromExistingRegistry(editedData,it, numTrimmedFromStart,ownerModule);
	}
	/*.................................................................................................................*/
	public void inferFromExistingRegistry(MolecularData editedData, int it, int numTrimmedFromStart, MesquiteModule ownerModule) {
//		Debugg.println("\n======================  START OF INFER CONTIG MAPPER =============");
//		Debugg.println(toString());
//		Debugg.println("\n======================  ABOUT TO BEGIN CONTIG MAPPER =============");

		checkContig(editedData,it, ownerModule);
		
		this.editedData = editedData;
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(editedData);
		MeristicData registryData = ChromaseqUtil.getRegistryData(editedData);
		this.numTrimmedFromStart = numTrimmedFromStart;
		setNumTrimmedFromStart(numTrimmedFromStart);
		int lastContigBaseInOriginal = -1;
		int lastEditedBaseInOriginal = -1;
		int deletedAtEnd = 0;
		int contigBase = 0;
		if (contig!=null) {
			if (contig.getNumBases()>numBases)
				setNumBases(contig.getNumBases());
		}
		int numContigBases = numBases;
		if (deleted==null || deleted.length!=numBases)
			init(numBases);
		
		// =========== cleanup RegistryData from earlier versions ===========
		for (int ic = 0; ic< registryData.getNumChars(); ic++){  
			int icOriginal = registryData.getState(ic, it,0);
			if (registryData.isCombinable(ic, it) && icOriginal>=0 && editedData.isInapplicable(ic, it)){
				registryData.setToInapplicable(ic, it);
				reverseRegistryData.setToInapplicable(icOriginal, it);
			}
		}

		for (int ic = 0; ic< numContigBases; ic++){  
			setDeletedBase(ic, false);
			setAddedBases(0, 0);
		}

		int firstOriginalInEditor = -1;
		int numOriginalBeforeFirstOriginalInEditor = 0;
		int firstTrimmedContigBaseInEditor = numTrimmedFromStart-1;
		// =========== Find the first  ===========
		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			if (originalData.isValidStateOrUnassigned(ic, it)){ 
				firstTrimmedContigBaseInEditor++;
				int positionInEdited = reverseRegistryData.getState(ic, it,0);
				if (positionInEdited>=0 && reverseRegistryData.isCombinable(ic, it) && !editedData.isInapplicable(positionInEdited, it)){  // in original, marked as in editor, and there
					firstOriginalInEditor = positionInEdited;
					break;
				} else
					numOriginalBeforeFirstOriginalInEditor++;
			}
		}

		int numResurrectedAtStart = 0;
		if (editedData.isReversed(it)) {
			for (int ic = editedData.getNumChars(); ic> firstOriginalInEditor; ic--){  
				if (editedData.isValidStateOrUnassigned(ic, it)){ // a state is here in the edited data
					numResurrectedAtStart++;
				}
			}
		}
		else
			for (int ic = 0; ic< firstOriginalInEditor; ic++){  
				if (editedData.isValidStateOrUnassigned(ic, it)){ // a state is here in the edited data
					numResurrectedAtStart++;
				}
			}
		
		for (int ic = 0; ic< numTrimmedFromStart-numResurrectedAtStart; ic++){  
			setDeletedBase(ic, true);
		}
		
		int numAddedBeforeFirstContigBase = numResurrectedAtStart-numTrimmedFromStart;
		if (numAddedBeforeFirstContigBase>0)
			setAddedBases(0, numAddedBeforeFirstContigBase);
		else
			setAddedBases(0, 3);
		

		// =========== Now process the bases within the original trimmed region ===========
		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			int positionInEdited = reverseRegistryData.getState(ic, it,0);
			if (originalData.isValidStateOrUnassigned(ic, it)){ // an original state is here!
				contigBase =getContigBaseFromOriginalMatrixBase(ic,it);

				if (positionInEdited<0 || editedData.isInapplicable(positionInEdited, it)){  // not in edited, record as deleted
					setDeletedBase(contigBase, true);
					deletedAtEnd++;
				}
				else{  // is in edited, so reset deletedAtEnds
					deletedAtEnd=0;
					setDeletedBase(contigBase, false);
					lastEditedBaseInOriginal = positionInEdited;  // record last base in edited which corresponds to one in original
				}
				lastContigBaseInOriginal=contigBase;
			}
		}
		int numFromEnd = numContigBases-lastContigBaseInOriginal-1;


		for (int ic = 0; ic< numFromEnd; ic++){  
			setDeletedBase(numContigBases-1-ic, true);
		}
		

		int addedBase=0;
		int addedToEnd=0;
		if (editedData.isReversed(it)) {
			for (int ic = firstOriginalInEditor; ic>=0; ic--){  
				if (editedData.isValidStateOrUnassigned(ic, it)){ // a state is here in the edited data
					int positionInOriginal = registryData.getState(ic, it);
					if (positionInOriginal<0 || !registryData.isCombinable(ic, it) || !originalData.isValidStateOrUnassigned(positionInOriginal, it)){  // not in original!
						addedBase++;
						if (ic>=lastEditedBaseInOriginal)
							addedToEnd++;
					}
					else { // it is in original; now record added bases
						setAddedBases(getContigBaseFromOriginalMatrixBase(positionInOriginal, it), addedBase);
						addedBase=0;
					}
				}
			}
		} else {
			for (int ic = firstOriginalInEditor; ic< editedData.getNumChars(); ic++){  
				if (editedData.isValidStateOrUnassigned(ic, it)){ // a state is here in the edited data
					int positionInOriginal = registryData.getState(ic, it);
					if (positionInOriginal<0 || !registryData.isCombinable(ic, it) || !originalData.isValidStateOrUnassigned(positionInOriginal, it)){  // not in original!
						addedBase++;
						if (ic>=lastEditedBaseInOriginal)
							addedToEnd++;
					}
					else { // it is in original; now record added bases
						if (addedBase>0) {
							contigBase = getContigBaseFromOriginalMatrixBase(positionInOriginal, it);
							setAddedBases(contigBase, addedBase);
						}
						addedBase=0;
					}
				}
			}
		}

		recalc(it);
		
		
		hasBeenSetUp = true;
//		Debugg.println(toString());
//		Debugg.println("\n======================  END OF INFER CONTIG MAPPER =============");
	}
	/*.................................................................................................................*/
	public void setUp(MolecularData editedData, int it, int numTrimmedFromStart) {
		this.editedData = editedData;
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(editedData);
		MeristicData registryData = ChromaseqUtil.getRegistryData(editedData);
		setNumTrimmedFromStart(numTrimmedFromStart);
		int lastContigBaseInOriginal = -1;
		int lastEditedBaseInOriginal = -1;
		int deletedAtEnd = 0;
		for (int ic = 0; ic< numTrimmedFromStart; ic++){  
			setDeletedBase(ic, true);
		}
		int contigBase = 0;

		int firstOriginalInEditor = -1;
		int numBeforeFirstOriginalInEditor = 0;
		// =========== Find the first  ===========
		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			if (originalData.isValidStateOrUnassigned(ic, it)){ 
				int positionInEdited = reverseRegistryData.getState(ic, it,0);
				if (reverseRegistryData.isCombinable(ic, it) && !editedData.isInapplicable(positionInEdited, it)){  // in original, marked as in editor, and there
					firstOriginalInEditor = positionInEdited;
				} else
					numBeforeFirstOriginalInEditor++;
			}
		}
		
		contigBase = numTrimmedFromStart-1;
		// =========== Now process the bases within the original trimmed region ===========
		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			int positionInEdited = reverseRegistryData.getState(ic, it,0);
			if (originalData.isValidStateOrUnassigned(ic, it)){ // an original state is here!
				contigBase++;
				if (positionInEdited<0 || editedData.isInapplicable(positionInEdited, it)){  // not in edited, record as deleted
					setDeletedBase(contigBase, true);
					deletedAtEnd++;
				}
				else{  // is in edited, so reset deletedAtEnds
					deletedAtEnd=0;
					setDeletedBase(contigBase, false);
					lastEditedBaseInOriginal = positionInEdited;  // record last base in edited which corresponds to one in original
				}
				lastContigBaseInOriginal=contigBase;
			}
		}
		int numFromEnd = contig.getNumBases()-lastContigBaseInOriginal-1;


		for (int ic = 0; ic< numFromEnd; ic++){  
			setDeletedBase(contig.getNumBases()-ic-1, true);
		}
		int addedBase=0;
		int addedToEnd=0;
		for (int ic = 0; ic< editedData.getNumChars(); ic++){  
			if (editedData.isValidStateOrUnassigned(ic, it)){ // a state is here in the edited data
				int positionInOriginal = registryData.getState(ic, it);
				if (positionInOriginal<0 || !registryData.isCombinable(ic, it) || !originalData.isValidStateOrUnassigned(positionInOriginal, it)){  // not in original!
					addedBase++;
					if (ic>=lastEditedBaseInOriginal)
						addedToEnd++;
				}
				else { // it is in original; now record added bases
					setAddedBases(positionInOriginal, addedBase);
					addedBase=0;
				}
			}
		}

		recalc(it);
		


		
		hasBeenSetUp = true;
	}
	/*.................................................................................................................*/
	public static boolean contigMapperExists (MolecularData data, int it) {
		ContigMapper contigMapper = ChromaseqUtil.getContigMapperAssociated(data, it);
		return (contigMapper!=null);
	}

	/*.................................................................................................................*/
	public  void zeroValues (){
		if (deleted!=null)
			for (int ic = 0; ic<deleted.length; ic++){
				deleted[ic]=false;
			}
		if (addedBefore!=null)
			for (int ic = 0; ic<addedBefore.length; ic++){
				addedBefore[ic]=0;
				totalAddedDeletedBefore[ic]=0;
				totalAddedDeletedAfter[ic]=0;
				totalAddedBefore[ic]=0;
			}
		numAddedToEnd = 0;
		numDeletedFromStart =0;
		numDeletedFromEnd = 0;
		numTrimmedFromEnd = 0;
		numTrimmedFromStart=0;
	}
	/*.................................................................................................................*/
	public void init () {
		if (contig!=null)
			numBases = contig.getNumBases();
		init(numBases);
	}
	/*.................................................................................................................*/
	public void init (int numBases) {
		if (numBases>0) {
			addedBefore = new int[numBases];
			totalAddedBefore = new int[numBases];
			totalAddedDeletedBefore = new int[numBases];
			totalAddedDeletedAfter = new int[numBases];
			deleted = new boolean[numBases];
			this.numBases = numBases;
		}
		zeroValues();
	}
	/*.................................................................................................................*/
	public void init (Contig contig) {
		this.contig=contig;
		numBases = contig.getNumBases();
	}
	/*.................................................................................................................*/
	public  int getTotalNumberAddedBases (){
		return totalAddedBefore[totalAddedBefore.length-1]+getNumAddedToEnd();
	}
	/*.................................................................................................................*
	public  int getTotalNumberAddedDeletedBases (){
		return totalAddedBefore[totalAddedBefore.length-1]+getNumAddedDeletedFromEnd();
	}

	/*.................................................................................................................*/
	public  int getNumAddedToEnd (){
		return numAddedToEnd;
	}
	/*.................................................................................................................*/
	public  int getNumDeletedFromEnd (){
		return numDeletedFromEnd;
	}
	/*.................................................................................................................*/
	public  void setNumAddedToEnd (int num){
		numAddedToEnd = num;
	}
	/*.................................................................................................................*/
	public  void setNumDeletedFromEnd (int num){
		numDeletedFromEnd = num;
	}
	/*.................................................................................................................*/
	public  int getNumAddedDeletedFromEnd (){
		return numAddedToEnd-numDeletedFromEnd;
	}

	/*.................................................................................................................*
	public  int getNumAddedDeletedAfter (int contigBase){
		if (contigBase<0 || totalAddedAfter == null || contigBase>=totalAddedAfter.length)
			return 0;
		return totalAddedAfter[contigBase];
	}
	/*.................................................................................................................*/
	public  int getNumAddedDeletedBefore (int contigBase){
		if (contigBase<0 || totalAddedDeletedBefore == null || contigBase>=totalAddedDeletedBefore.length)
			return 0;
		return totalAddedDeletedBefore[contigBase];
	}
	/*.................................................................................................................*/
	public  int getNumAddedBefore (int consensusBase){
		if (consensusBase<0 || addedBefore.length == 0)
			return 0;
		int count = 0;
		for (int ic = 0; ic<addedBefore.length && ic<=consensusBase; ic++)
			count += addedBefore[ic] ;
		return count;
	}
	/*.................................................................................................................*/
	public  int getNumDeletedBefore (int consensusBase){
		if (consensusBase<0 || deleted.length == 0)
			return 0;
		int count = 0;
		for (int ic = 0; ic<deleted.length && ic<consensusBase; ic++)
			if (deleted[ic])
				count ++;
		return count;
	}
	/*.................................................................................................................*/
	public  boolean getDeletedBase (int contigBase){
		if (contigBase>=0 && contigBase<deleted.length)
			return deleted[contigBase];
		return false;
	}
	/*.................................................................................................................*/
	public  void setDeletedBase (int contigBase, boolean b){
		if (contigBase>=0 && contigBase<deleted.length) {
			deleted[contigBase] = b;
		}
	}
	/*.................................................................................................................*/
	public  void setPadding (int contigBase, boolean b){
		if (contig!=null) 
			contig.setPadding(contigBase,b);		
	}
	/*.................................................................................................................*/
	public  boolean getIsPadding (int contigBase){
		if (contig!=null) 
			return contig.getIsPadding(contigBase);	
		return false;
	}
	/*.................................................................................................................*/
	public  void setDeletedBases (int contigStart, int contigEnd, boolean b){
		if (deleted==null)
			return;
		for (int contigBase=contigStart; contigBase<=contigEnd; contigBase++)
			if (contigBase>=0 && contigBase<deleted.length) {
				deleted[contigBase] = b;
		}
	}
	/*.................................................................................................................*/
	public  void addToAddedBases (int contigBase, int numAdded){
		if (addedBefore==null)
			return;
		if (contigBase>=0 && contigBase<addedBefore.length)
			addedBefore[contigBase] += numAdded;
	}
	/*.................................................................................................................*/
	public  void setAddedBases (int contigBase, int numAdded){
		if (addedBefore==null)
			return;
		if (contigBase>=0 && contigBase<addedBefore.length)
			addedBefore[contigBase] = numAdded;
	}
	/*.................................................................................................................*/
	public  int getAddedBases (int contigBase){
		if (addedBefore==null)
			return 0;
		if (contigBase>=0 && contigBase<addedBefore.length)
			return addedBefore[contigBase];
		return 0;
	}
	/*.................................................................................................................*/
	public Contig getContig() {
		return contig;
	}
	/*.................................................................................................................*/
	public void setContig(Contig contig) {
		this.contig = contig;
		if (contig.getNumBases()> numBases)
			init(contig.getNumBases());
	}
	/*.................................................................................................................*/
	public void setStoredInFile(boolean storedInFile) {
		this.storedInFile = storedInFile;
	}
	/*.................................................................................................................*/
	public boolean getStoredInFile() {
		return storedInFile;
	}
	/*.................................................................................................................*/
	public void markAsDeletedBasesTrimmedAtStart(int trimmed) {
		if (deleted==null)
			return;
		for (int i = 0; i<deleted.length && i<trimmed; i++)
			setDeletedBase(i,true);
	}
	/*.................................................................................................................*/
	public void markAsDeletedBasesTrimmedAtEnd(int trimmed) {
		if (deleted==null)
			return;
		for (int i = 0; i<deleted.length && i<trimmed; i++)
			setDeletedBase(deleted.length-i-1,true);
	}
	/*.................................................................................................................*/
	public String getNEXUSCommand() {
		StringBuffer sb = new StringBuffer();
		sb.append(" NUMBASES = " + deleted.length);
		sb.append(" TRIMSTART = " + numTrimmedFromStart);
		sb.append(" DELETED = ");
		for (int i=0; i<deleted.length; i++)
			if (deleted[i])
				sb.append("1") ;
			else sb.append("0") ;
		sb.append("   ADDEDBEFORE = '");
		for (int i=0; i<addedBefore.length; i++)
			sb.append(" "+addedBefore[i]) ;
		sb.append("'");
		return sb.toString();
	}
	/*.................................................................................................................*/
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("---- ContigMapper details ---- \n");
		sb.append("DELETED ");
		for (int i=0; i<deleted.length; i++)
			if (deleted[i])
				sb.append("1") ;
			else sb.append("0") ;
		sb.append("\nADDEDBEFORE ");
		for (int i=0; i<addedBefore.length; i++)
			sb.append(" "+addedBefore[i]) ;
		sb.append("\nTOTALADDEDDELETEDBEFORE ");
		for (int i=0; i<totalAddedDeletedBefore.length; i++)
			sb.append(" "+totalAddedDeletedBefore[i]) ;
		sb.append("\nnumAddedToEnd: " + numAddedToEnd); 
		sb.append("\nnumDeletedFromEnd: " + numDeletedFromEnd); 
		sb.append("\nnumTrimmedFromEnd: " + numTrimmedFromEnd); 
		sb.append("\nnumTrimmedFromStart: " + numTrimmedFromStart); 
		sb.append("\nnumResurrectedAtStart: " + numResurrectedAtStart); 
		sb.append("\nnumBases: " + numBases); 
		int numDeletedAtStart=0;
		for (int i=0; i<deleted.length; i++)
			if (deleted[i])
				numDeletedAtStart++ ;
			else break;
		sb.append("\nnumDeletedAtStart: " + numDeletedAtStart); 
		sb.append("\n-------------------------- \n");
		return sb.toString();
	}
	public int getNumTrimmedFromEnd() {
		return numTrimmedFromEnd;
	}
	public void setNumTrimmedFromEnd(int num) {
		this.numTrimmedFromEnd = num;
	}
	public boolean hasBeenSetUp() {
		return hasBeenSetUp;
	}
	public void setHasBeenSetUp(boolean hasBeenSetUp) {
		this.hasBeenSetUp = hasBeenSetUp;
	}
	public int getNumResurrectedAtStart() {
		return numResurrectedAtStart;
	}
	public void setNumResurrectedAtStart(int num) {
		this.numResurrectedAtStart = num;
	}
	public int getNumLeftToResurrectAtStart() {
		int num = numTrimmedFromStart-numResurrectedAtStart;
		if (num<0)
			return 0;
		return num;
	}
	public int getNumResurrectedAtEnd() {
		return numResurrectedAtEnd;
	}
	public void setNumResurrectedAtEnd(int numResurrectedAtEnd) {
		this.numResurrectedAtEnd = numResurrectedAtEnd;
	}
	public int getNumLeftToResurrectAtEnd() {
		int num = numTrimmedFromEnd-numResurrectedAtEnd;
		if (num<0)
			return 0;
		return num;
	}

	public int getNumBases() {
		return numBases;
	}
	public void setNumBases(int numBases) {
		if (numBases!=this.numBases || deleted==null) {
			this.numBases = numBases;
			init(numBases);
		}
	}
	public int getNumTrimmedFromStart() {
		return numTrimmedFromStart;
	}
	public int getLastTrimmedContigBase() {
		return numBases - numTrimmedFromEnd-1;
	}
	public void setNumTrimmedFromStart(int numTrimmedFromStart) {
		this.numTrimmedFromStart = numTrimmedFromStart;
		if (contig!=null)
			contig.setNumTrimmedFromStart(numTrimmedFromStart);
	/*	if (editedData==null)
			return;
		Associable tInfo = editedData.getTaxaInfo(true);
		if (tInfo != null) {
		//	tInfo.removeAssociatedLongs(ChromaseqUtil.startTrimRef);
			ChromaseqUtil.setLongAssociated(tInfo,ChromaseqUtil.startTrimRef, it, numTrimmedFromStart);
		}
*/
	}
	
	/*...........................................................................*
	public double calcMatchToEditedScoreOfRead(int whichRead, int it) {	
		Read read = contig.getRead(whichRead);
		double conflicts=0.0;
		double bases=0.0;
		for (int contigBase=0;contigBase < numBases;contigBase++) {
			int readBase = read.getReadBaseFromContigBase(contigBase);
			if (readBase>=0 && readBase<read.getBasesLength()) {
				int qual = read.getPhdBaseQuality(readBase);
				char c = read.getPhdBaseChar(readBase);
				long readState = DNAState.fromCharStatic(c);

				int matrixBase = getMatrixBaseFromContigBase(contigBase, it);
				long editedState = editedData.getState(matrixBase,it);
				if (qual>20) {
					 if (!DNAState.equalsIgnoreCase(readState, editedState))
						conflicts++;
					 bases++;
				}
			} 
		}
		Debugg.println("      conflicts: " + conflicts + ", bases: " + bases);
		if (bases==0)
			return 0.0;
		else
			return conflicts/bases;
	}
	/*...........................................................................*
	public double calcMatchToEditedScore(int it) {	
		if (contig==null || contig.getNumReadsInContig()==0)
			return 0.0;
		double total = 0.0;
		for (int whichRead = 0; whichRead<contig.getNumReadsInContig(); whichRead++) {
			total += calcMatchToEditedScoreOfRead(whichRead,it);
		}
		double score = total/contig.getNumReadsInContig();
		setMatchToEditedScore(score);
		return score;
	}
	/*...........................................................................*/


	public double getMatchToEditedScore() {
		return matchToEditedScore;
	}
	public void setMatchToEditedScore(double matchToEditedScore) {
		this.matchToEditedScore = matchToEditedScore;
	}


}
