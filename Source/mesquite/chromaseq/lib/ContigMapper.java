package mesquite.chromaseq.lib;

import mesquite.categ.lib.MolecularData;
import mesquite.meristic.lib.MeristicData;
import mesquite.lib.*;

public class ContigMapper {
	int numAddedToEnd, numResurrectedAtStart;
	int numDeletedFromStart, numDeletedFromEnd;
	int numBasesOriginallyTrimmedFromEndOfPhPhContig=0;
	int numTrimmedFromStart = 0;
	int[] addedBefore;
	int[] totalAddedBefore, totalAddedAfter;
	boolean[] deleted;
	int numBases=0;
	Contig contig=null;
	boolean storedInFile = false;
	boolean 	hasBeenSetUp = false;
	MolecularData editedData = null;
	int it = -1;

	public ContigMapper (Contig contig) {
		this.contig = contig;
		init();
	}
	public ContigMapper () {
		init();
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
			contigMapper.setNumTrimmedFromStart(numTrimmedFromStart);
		}
		return contigMapper;
	}
	/*.................................................................................................................*/
	public void setTaxonNumber(int it) {
		this.it = it;
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
			totalAddedBefore[ic] = count;
		}
		count=0;
		for (int ic = 0; ic<deleted.length; ic++){
			totalAddedBefore[ic] -= count;
			if (deleted[ic])
				count++;
		}
		count = 0;
		for (int ic = addedBefore.length-1; ic>=0; ic--){
			count += addedBefore[ic] ;
			totalAddedAfter[ic] = count;
		}
		count=0;
		for (int ic = deleted.length-1; ic>=0; ic--){
			totalAddedAfter[ic] -= count;
			if (deleted[ic])
				count++;
		}
		totalAddedAfter[deleted.length-1] -= numDeletedFromEnd;

		count=0;
		for (int ic = deleted.length-1; ic>=0; ic--){
			if (!deleted[ic] && ic<numTrimmedFromStart)
				count++;
		}
		setNumResurrectedAtStart(count);

	}
	/*.................................................................................................................*/
	public void recalc(MolecularData editedData, int it) {
		setData(editedData);
		setTaxonNumber(it);
		recalc();
	}
	/*.................................................................................................................*/
	public void calcEndTrim() {
		if (editedData==null)
			return;
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		int contigBase = numTrimmedFromStart-1;
		int lastContigBaseInOriginal = -1;

		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			if (originalData.isValidAssignedState(ic, it)){ // an original state is here!
				contigBase++;
				lastContigBaseInOriginal=contigBase;
			}
		}
		int numBasesOriginallyTrimmedFromEndOfPhPhContig = contig.getNumBases()-lastContigBaseInOriginal-1;
		setNumBasesOriginallyTrimmedFromEndOfPhPhContig(numBasesOriginallyTrimmedFromEndOfPhPhContig);
		for (int ic = 0; ic< numBasesOriginallyTrimmedFromEndOfPhPhContig; ic++){  
			setDeletedBase(contig.getNumBases()-ic-1, true);
		}
	}
	/*.................................................................................................................*/
	/** This method does the basic calculations to summarize various aspects of the added and deleted bases; it does not alter the core storage about which bases are deleted and how many are added.*/
	public void recalc() {
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
			if (originalData.isValidAssignedState(ic, it)){ // an original state is here!
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
		int numBasesOriginallyTrimmedFromEndOfPhPhContig = contig.getNumBases()-lastContigBaseInOriginal-1;


		setNumDeletedFromEnd(deletedAtEnd);
		setNumBasesOriginallyTrimmedFromEndOfPhPhContig(numBasesOriginallyTrimmedFromEndOfPhPhContig);
		int addedBase=0;
		int addedToEnd=0;
		for (int ic = 0; ic< editedData.getNumChars(); ic++){  
			if (editedData.isValidAssignedState(ic, it)){ // a state is here in the edited data
				int positionInOriginal = registryData.getState(ic, it);
				if (positionInOriginal<0 || !registryData.isCombinable(ic, it) || !originalData.isValidAssignedState(positionInOriginal, it)){  // not in original!
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
	}
	/*.................................................................................................................*/
	public void setUp(MolecularData editedData, int it, int numTrimmedFromStart) {
		this.editedData = editedData;
		this.it = it;
		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		MeristicData reverseRegistryData = ChromaseqUtil.getReverseRegistryData(editedData);
		MeristicData registryData = ChromaseqUtil.getRegistryData(editedData);
		this.numTrimmedFromStart = numTrimmedFromStart;
		int contigBase = numTrimmedFromStart-1;
		int lastContigBaseInOriginal = -1;
		int lastEditedBaseInOriginal = -1;
		int deletedAtEnd = 0;
		for (int ic = 0; ic< numTrimmedFromStart; ic++){  
			setDeletedBase(ic, true);
		}

		// =========== cleanup RegistryData from earlier versions ===========
		for (int ic = 0; ic< registryData.getNumChars(); ic++){  
			if (registryData.getState(ic, it,0)>=0 && editedData.isInapplicable(ic, it))
				registryData.setToInapplicable(ic, it);
		}

		// =========== Do initial setup of contigMapper ===========
		for (int ic = 0; ic< originalData.getNumChars(); ic++){  
			int positionInEdited = reverseRegistryData.getState(ic, it,0);
			if (originalData.isValidAssignedState(ic, it)){ // an original state is here!
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
		int numBasesOriginallyTrimmedFromEndOfPhPhContig = contig.getNumBases()-lastContigBaseInOriginal-1;


		for (int ic = 0; ic< numBasesOriginallyTrimmedFromEndOfPhPhContig; ic++){  
			setDeletedBase(contig.getNumBases()-ic-1, true);
		}
		int addedBase=0;
		int addedToEnd=0;
		for (int ic = 0; ic< editedData.getNumChars(); ic++){  
			if (editedData.isValidAssignedState(ic, it)){ // a state is here in the edited data
				int positionInOriginal = registryData.getState(ic, it);
				if (positionInOriginal<0 || !registryData.isCombinable(ic, it) || !originalData.isValidAssignedState(positionInOriginal, it)){  // not in original!
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

		recalc();
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
				totalAddedBefore[ic]=0;
				totalAddedAfter[ic]=0;
			}
		numAddedToEnd = 0;
		numDeletedFromStart =0;
		numDeletedFromEnd = 0;
		numBasesOriginallyTrimmedFromEndOfPhPhContig = 0;
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
			totalAddedAfter = new int[numBases];
			deleted = new boolean[numBases];
		}
		zeroValues();
	}
	/*.................................................................................................................*/
	public void init (Contig contig) {
		this.contig=contig;
	}
	/*.................................................................................................................*/
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
		if (contigBase<0 || totalAddedBefore == null || contigBase>=totalAddedBefore.length)
			return 0;
		return totalAddedBefore[contigBase];
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
	public  void addToAddedBases (int contigBase, int numAdded){
		if (contigBase>=0 && contigBase<addedBefore.length)
			addedBefore[contigBase] += numAdded;
	}
	/*.................................................................................................................*/
	public  void setAddedBases (int contigBase, int numAdded){
		if (contigBase>=0 && contigBase<addedBefore.length)
			addedBefore[contigBase] = numAdded;
	}
	/*.................................................................................................................*/
	public  int getAddedBases (int contigBase){
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
	public void setTrimmedFromStart(int trimmed) {
		for (int i = 0; i<deleted.length && i<trimmed; i++)
			setDeletedBase(i,true);
	}
	/*.................................................................................................................*/
	public void setTrimmedFromEnd(int trimmed) {
		for (int i = 0; i<deleted.length && i<trimmed; i++)
			setDeletedBase(deleted.length-i-1,true);
	}
	/*.................................................................................................................*/
	public String getNEXUSCommand() {
		StringBuffer sb = new StringBuffer();
		sb.append(" NUMBASES = " + deleted.length);
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
		for (int i=0; i<totalAddedBefore.length; i++)
			sb.append(" "+totalAddedBefore[i]) ;
		sb.append("\nnumAddedToEnd: " + numAddedToEnd); 
		sb.append("\nnumDeletedFromEnd: " + numDeletedFromEnd); 
		sb.append("\nnumBasesOriginallyTrimmedFromEndOfPhPhContig: " + numBasesOriginallyTrimmedFromEndOfPhPhContig); 
		sb.append("\n-------------------------- \n");
		return sb.toString();
	}
	public int getNumBasesOriginallyTrimmedFromEndOfPhPhContig() {
		return numBasesOriginallyTrimmedFromEndOfPhPhContig;
	}
	public void setNumBasesOriginallyTrimmedFromEndOfPhPhContig(
			int numBasesOriginallyTrimmedFromEndOfPhPhContig) {
		this.numBasesOriginallyTrimmedFromEndOfPhPhContig = numBasesOriginallyTrimmedFromEndOfPhPhContig;
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
	public int getNumBases() {
		return numBases;
	}
	public void setNumBases(int numBases) {
		if (numBases!=this.numBases) {
			this.numBases = numBases;
			init(numBases);
		}
	}
	public int getNumTrimmedFromStart() {
		return numTrimmedFromStart;
	}
	public void setNumTrimmedFromStart(int numTrimmedFromStart) {
		this.numTrimmedFromStart = numTrimmedFromStart;
	}


}
