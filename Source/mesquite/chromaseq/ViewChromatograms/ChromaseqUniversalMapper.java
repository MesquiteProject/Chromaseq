package mesquite.chromaseq.ViewChromatograms;

import mesquite.categ.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.meristic.lib.*;

public class ChromaseqUniversalMapper implements MesquiteListener {
	public static final int ORIGINALUNTRIMMEDSEQUENCE = 0;   // this is the "Original Untrimmed" sequence line in the chromatogram viewer
	public static final int ORIGINALTRIMMEDSEQUENCE = 3;    // this is the "Original Trimmed" sequence line in the chromatogram viewer.
	public static final int EDITEDMATRIXSEQUENCE = 1;   // this is the edited in matrix sequence line in the chromatogram viewer
	public static final int EDITEDMATRIX = 2;    // this is the row in the editedMatrix in the actually editedData object, including gaps etc.
	public static final int ACEFILECONTIG = 4;    // this is the "Phred.Phrap.Mesquite" sequence line in the chromatogram viewer.
	static final int numMappings = 5;

	SequencePanel originalUntrimmedPanel;
	SequencePanel matrixSequencePanel;
	SequencePanel originalTrimmedSequencePanel;
	int numBasesOriginallyTrimmedFromStartOfPhPhContig= 0;
	Contig contig;
	ContigDisplay contigDisplay;
	int totalUniversalBases=0;
	int totalNumAddedBases=0;
	int totalNumDeletedBases=0;
	DNAData editedData;
	MeristicData registryData=null;
	MeristicData reverseRegistryData=null;
	int it=0;
	boolean hasBeenSet = false;

	int[][] universalBaseFromOtherBase;
	int[][] otherBaseFromUniversalBase;

	boolean reverseComplement = false;
	boolean reversedInEditData = false;
	boolean complementedInEditData = false;

	public ChromaseqUniversalMapper (ContigDisplay contigDisplay, MolecularData data){
		this.contigDisplay = contigDisplay;
		this.originalUntrimmedPanel = contigDisplay.getAceContigPanel();
		this.matrixSequencePanel = contigDisplay.getMatrixSeqPanel();
		this.originalTrimmedSequencePanel = contigDisplay.getOrigSeqPanel();

		this.contig = contigDisplay.getContig();
		it = contigDisplay.getTaxon().getNumber();
		editedData = contigDisplay.getEditedData();
		if (editedData!=null)
			editedData.addListener(this);
		registryData = ChromaseqUtil.getRegistryData(editedData);
		reverseRegistryData = ChromaseqUtil.getRegistryData(editedData);

		reversedInEditData = contigDisplay.isReversedInEditedData();
		complementedInEditData = contigDisplay.isComplementedInEditedData();

		numBasesOriginallyTrimmedFromStartOfPhPhContig = contig.getNumBasesOriginallyTrimmedFromStartOfPhPhContig(editedData, it);

		init();
	}

	/*.................................................................................................................*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData) {
			reset();
			contigDisplay.repaintPanels();
		} 
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}

	public boolean getHasBeenSet() {
		return hasBeenSet;
	}
	/*.................................................................................................................*/
	public void init() {
		createOtherBaseFromUniversalBase();
		createUniversalBaseFromOtherBase();
	}

	/*.................................................................................................................*/
	public void createOtherBaseFromUniversalBase() {
		otherBaseFromUniversalBase = new int[numMappings][contigDisplay.getTotalNumInitialOverallBases()];
	}
	/*.................................................................................................................*/
	public void createUniversalBaseFromOtherBase() {
		if (universalBaseFromOtherBase==null)
			universalBaseFromOtherBase = new int[numMappings][];
		if (universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE]==null || universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE].length!=originalUntrimmedPanel.getLength())
			universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE] = new int[originalUntrimmedPanel.getLength()];
		if (universalBaseFromOtherBase[ORIGINALTRIMMEDSEQUENCE]==null || universalBaseFromOtherBase[ORIGINALTRIMMEDSEQUENCE].length!=originalTrimmedSequencePanel.getLength())
			universalBaseFromOtherBase[ORIGINALTRIMMEDSEQUENCE] = new int[originalTrimmedSequencePanel.getLength()];
		if (universalBaseFromOtherBase[EDITEDMATRIXSEQUENCE]==null || universalBaseFromOtherBase[EDITEDMATRIXSEQUENCE].length!=matrixSequencePanel.getLength())
			universalBaseFromOtherBase[EDITEDMATRIXSEQUENCE] = new int[matrixSequencePanel.getLength()];
		if (universalBaseFromOtherBase[EDITEDMATRIX]==null || universalBaseFromOtherBase[EDITEDMATRIX].length!=editedData.getNumChars())
			universalBaseFromOtherBase[EDITEDMATRIX] = new int[editedData.getNumChars()];
		if (universalBaseFromOtherBase[ACEFILECONTIG]==null || universalBaseFromOtherBase[ACEFILECONTIG].length!=contig.getNumBases())
			universalBaseFromOtherBase[ACEFILECONTIG] = new int[contig.getNumBases()];
	}
	/*.................................................................................................................*/
	/* this method recalculates all mappings */
	public synchronized void serialFill() {
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<otherBaseFromUniversalBase[mapping].length; i++) 
				otherBaseFromUniversalBase[mapping][i]=i;
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<universalBaseFromOtherBase[mapping].length; i++) 
				universalBaseFromOtherBase[mapping][i]=i;
	}
	/*.................................................................................................................*/


	int resetCount = 0;
	
	
	/*.................................................................................................................*/
	/* this method recalculates all mappings */
	public synchronized void reset() {
//		Debugg.println("======= Resetting Universal Base Registry ======= " + (resetCount++));
//		Debugg.printStackTrace("\n\nuniversalMapper reset: " + Thread.currentThread()+"\n\n");

		// =========== Calculate total number of universal bases ===========

		//	totalNumAddedBases=ChromaseqUtil.getTotalNumBasesAddedBeyondPhPhBases(editedData, it);
		//	totalNumDeletedBases=ChromaseqUtil.getTotalNumOriginalBasesTurnedToGaps(editedData, it);

		
		ChromaseqUtil.fillAddedBaseData(contigDisplay,  editedData, it);
		
		reversedInEditData = contigDisplay.isReversedInEditedData();
		complementedInEditData = contigDisplay.isComplementedInEditedData();
		numBasesOriginallyTrimmedFromStartOfPhPhContig = contigDisplay.getNumBasesOriginallyTrimmedFromStartOfPhPhContig();

		totalNumAddedBases=0;
		totalNumDeletedBases=0;
		int numDeletedFromStart = 0;
		boolean foundFirstBase = false;
		for (int ic = 0; ic< editedData.getNumChars(); ic++){  // going through the sourceData object.  This is either the edited matrix or the original matrix
			if (registryData!=null){
				int positionInOriginal = registryData.getState(ic, it);
				if (registryData.getState(ic, it)==ChromaseqUtil.ADDEDBASEREGISTRY) {  //this must be an added base
					totalNumAddedBases++;
				} else if (positionInOriginal>=0 && reverseRegistryData.getState(positionInOriginal,it)==ChromaseqUtil.DELETEDBASEREGISTRY) {  // this must be a deleted base
					totalNumDeletedBases++;
					
				}
			}
		}

/*		int numDeletedFromEnd=0;
		for (int ic = editedData.getNumChars()-1; ic>=0; ic--){  // going through the sourceData object.  This is either the edited matrix or the original matrix
			if (registryData!=null){
				int positionInOriginal = registryData.getState(ic, it);
				if (registryData.getState(ic, it)==ChromaseqUtil.ADDEDBASEREGISTRY) {  //this must be an added base
					totalNumAddedBases++;
				} else if (positionInOriginal>=0 && reverseRegistryData.getState(positionInOriginal,it)==ChromaseqUtil.DELETEDBASEREGISTRY) {  // this must be a deleted base
					totalNumDeletedBases++;
				}
			}
		}
*/

		/* contigDisplay.getTotalNumOverallBases() is the number of bases according to the contig 
		 * - it's the number of bases in the contig plus the extra bases in front and at the end (as found in individual reads
		 * that extend beyond the contig.  So, it's the length of overall bases according to the PhredPhrap cloud.
		 */
		totalUniversalBases = contigDisplay.getTotalNumInitialOverallBases() + contig.getNumPadded();

		/* Now let's add to this the bases that 
		 */
		totalUniversalBases += totalNumAddedBases-totalNumDeletedBases;

		if (otherBaseFromUniversalBase==null || otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE].length!=totalUniversalBases)
			createOtherBaseFromUniversalBase();
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<otherBaseFromUniversalBase[mapping].length; i++) 
				otherBaseFromUniversalBase[mapping][i]=-1;


		createUniversalBaseFromOtherBase();
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<universalBaseFromOtherBase[mapping].length; i++) 
				universalBaseFromOtherBase[mapping][i]=-1;


		// =========== Calculate mappings for the original untrimmed panel (i.e., the "Original Untrimmed" one) ===========

		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		SequenceCanvas sequenceCanvas = originalUntrimmedPanel.getCanvas();
		MesquiteSequence sequence = originalUntrimmedPanel.getSequence();
		if (sequenceCanvas!=null && sequence!=null){
			int[] addedBases = new int[sequence.getLength()];//+contig.getReadExcessAtStart()];
			for (int sequenceBase=0; sequenceBase<numBasesOriginallyTrimmedFromStartOfPhPhContig; sequenceBase++){
				addedBases[sequenceBase] = 0;
			}
			int totalAddedBases = 0;
			int sequenceBases = numBasesOriginallyTrimmedFromStartOfPhPhContig-1;
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				int icOriginal = registryData.getState(ic, it);
				if (icOriginal==ChromaseqUtil.ADDEDBASEREGISTRY){// || registryData.isInapplicable(ic, it)) { //
					totalAddedBases++;
				} else if (originalData.isValidAssignedState(icOriginal,it)) {
					sequenceBases++;
					if (sequenceBases>=0 && sequenceBases<addedBases.length)
						addedBases[sequenceBases] = totalAddedBases;
				}
			}
			for (int sequenceBase=0; sequenceBase<sequence.getLength(); sequenceBase++){
				int universalBase = sequenceBase + contig.getReadExcessAtStart()+ addedBases[sequenceBase];
				otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase] = sequenceBase;
				universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][sequenceBase] = universalBase;
				otherBaseFromUniversalBase[ACEFILECONTIG][universalBase] = sequenceBase;
				universalBaseFromOtherBase[ACEFILECONTIG][sequenceBase] = universalBase;
			}
		}

		// =========== Calculate mappings for the original import panel (i.e., the "Original.Trimmed" one - just like Original.Untrimmed but trimmed) ===========
		int prevNumPads = 0;
		sequenceCanvas = originalTrimmedSequencePanel.getCanvas();
		sequence = originalTrimmedSequencePanel.getSequence();
		if (sequenceCanvas!=null && sequence!=null){
			int[] addedBases = new int[sequence.getLength()];//+contig.getReadExcessAtStart()];
			int totalAddedBases = 0;
			int sequenceBases = -1;
			for (int ic=0; ic<registryData.getNumChars(); ic++){
				int icOriginal = registryData.getState(ic, it);
				if (icOriginal==ChromaseqUtil.ADDEDBASEREGISTRY){ // || registryData.isInapplicable(ic, it)) { //
					totalAddedBases++;
				} else if (originalData.isValidAssignedState(icOriginal,it)) {
					sequenceBases++;
					if (sequenceBases>=0 && sequenceBases<addedBases.length)
						addedBases[sequenceBases] = totalAddedBases;
				}
			}

			int sequenceLength = sequence.getLength();

/*
		Debugg.println("   totalUniversalBases: " + totalUniversalBases + "   sequenceLength: " + sequenceLength);
			Debugg.println("   contig.getReadExcessAtStart(): " + contig.getReadExcessAtStart() + "   numBasesOriginallyTrimmedFromStartOfPhPhContig: " + numBasesOriginallyTrimmedFromStartOfPhPhContig);
			Debugg.println("   totalAddedBases: " + totalAddedBases);
			Debugg.println("   contigDisplay.getNumBasesAddedToStart(): " + contigDisplay.getNumBasesAddedToStart());
			Debugg.println("   contigDisplay.getNumBasesAddedToEnd(): " + contigDisplay.getNumBasesAddedToEnd());
*/
			
			for (int sequenceBase=0; sequenceBase<sequenceLength; sequenceBase++){
				int universalBase = sequenceBase + contig.getReadExcessAtStart()+ numBasesOriginallyTrimmedFromStartOfPhPhContig+addedBases[sequenceBase];
				int numPads = contig.getNumPaddedBefore(otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase]);  // account for padding
				if (numPads<prevNumPads)
					numPads=prevNumPads;
				universalBase+=numPads;
				prevNumPads = numPads;
				otherBaseFromUniversalBase[ORIGINALTRIMMEDSEQUENCE][universalBase] = sequenceBase;
				universalBaseFromOtherBase[ORIGINALTRIMMEDSEQUENCE][sequenceBase] = universalBase;
			}
		}

		// =========== Calculate mappings for EditedInMatrix sequence panel ===========

		int numBasesFound = -1;
		int numOriginalBasesFound=-1;
		int numAddedBases = 0;
		int numDeletedBases = 0;
		int numChars = editedData.getNumChars();

	//	Debugg.println("   numChars: " + numChars);

	//	boolean firstTimeThrough = true;
		prevNumPads = 0;

		if (!reversedInEditData) { 
			int startingUniversalBase = contigDisplay.getUniversalBaseFromContigBase(numBasesOriginallyTrimmedFromStartOfPhPhContig-contigDisplay.getNumBasesAddedToStart());
//			Debugg.println("   startingUniversalBase: " + startingUniversalBase);
			for (int matrixBase = 0; matrixBase< numChars; matrixBase++){  // going through the sourceData object.  This is either the edited matrix or the original matrix
				
				 int positionInOriginal = registryData.getState(matrixBase, it);
				if (registryData!=null){
					if (registryData.getState(matrixBase, it)==ChromaseqUtil.ADDEDBASEREGISTRY || registryData.getState(matrixBase, it)==ChromaseqUtil.MOVEDBASEREGISTRY) {  //this must be an added base
						positionInOriginal=-1;
						numAddedBases++;
					} else if (positionInOriginal>=0 && reverseRegistryData.getState(positionInOriginal,it)==ChromaseqUtil.DELETEDBASEREGISTRY) {  // this must be a deleted base
						numDeletedBases++;
					}
				}
				
				if (ChromaseqUtil.isUniversalBase(editedData,matrixBase,it)){
					numBasesFound++;
					numOriginalBasesFound++;

					int sequenceBase = numBasesFound-numDeletedBases;
					int universalBase = startingUniversalBase+numBasesFound;
					int numPads = contig.getNumPaddedBefore(otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase]);
					if (numPads<prevNumPads)
						numPads=prevNumPads;
					universalBase+=numPads;  // account for padding
					prevNumPads = numPads;

					if (sequenceBase>=0 && sequenceBase<universalBaseFromOtherBase[EDITEDMATRIXSEQUENCE].length)
						universalBaseFromOtherBase[EDITEDMATRIXSEQUENCE][sequenceBase] = universalBase;
					if (universalBase>=0 && universalBase<otherBaseFromUniversalBase[EDITEDMATRIXSEQUENCE].length)
						otherBaseFromUniversalBase[EDITEDMATRIXSEQUENCE][universalBase] = sequenceBase;


					if (matrixBase>=0 && matrixBase<universalBaseFromOtherBase[EDITEDMATRIX].length)
						universalBaseFromOtherBase[EDITEDMATRIX][matrixBase] = universalBase;
					if (universalBase>=0 && universalBase<otherBaseFromUniversalBase[EDITEDMATRIX].length)
						otherBaseFromUniversalBase[EDITEDMATRIX][universalBase] = matrixBase;
				} 
			}
		}
		else {
			int startingUniversalBase = contigDisplay.getUniversalBaseFromContigBase(numBasesOriginallyTrimmedFromStartOfPhPhContig-contigDisplay.getNumBasesAddedToEnd());
//			Debugg.println("   startingUniversalBase: " + startingUniversalBase);
			for (int matrixBase = numChars-1; matrixBase>=0 ; matrixBase--){  // going through the sourceData object.  This is either the edited matrix or the original matrix

				/*int positionInOriginal = registryData.getState(matrixBase, it);
				if (registryData!=null){
					if (registryData.getState(matrixBase, it)==ChromaseqUtil.ADDEDBASEREGISTRY) {  //this must be an added base
						positionInOriginal=-1;
						numAddedBases++;
					} else if (positionInOriginal>=0 && reverseRegistryData.getState(positionInOriginal,it)==ChromaseqUtil.DELETEDBASEREGISTRY) {  // this must be a deleted base
						numDeletedBases++;
					}
				}*/
				if (ChromaseqUtil.isUniversalBase(editedData,matrixBase,it)){
					numBasesFound++;
					numOriginalBasesFound++;

					int sequenceBase = numBasesFound;
					int universalBase = startingUniversalBase+numBasesFound;
					int numPads = 0;
					if (universalBase>=0)
						numPads = contig.getNumPaddedBefore(otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase]);
					if (numPads<prevNumPads)
						numPads=prevNumPads;
					universalBase+=numPads;  // account for padding
					prevNumPads = numPads;

					if (sequenceBase>=0 && sequenceBase<universalBaseFromOtherBase[EDITEDMATRIXSEQUENCE].length)
						universalBaseFromOtherBase[EDITEDMATRIXSEQUENCE][sequenceBase] = universalBase;
					if (universalBase>=0 && universalBase<otherBaseFromUniversalBase[EDITEDMATRIXSEQUENCE].length)
						otherBaseFromUniversalBase[EDITEDMATRIXSEQUENCE][universalBase] = sequenceBase;

					int reversedMatrixBase = numChars-matrixBase-1;
					if (matrixBase>=0 && matrixBase<universalBaseFromOtherBase[EDITEDMATRIX].length)
						universalBaseFromOtherBase[EDITEDMATRIX][matrixBase] = universalBase;
					if (universalBase>=0 && universalBase<otherBaseFromUniversalBase[EDITEDMATRIX].length)
						otherBaseFromUniversalBase[EDITEDMATRIX][universalBase] = matrixBase;

				} 

			}
		}
		/*		int negativeBase = -1;
		for (int universalBase = firstUniversalBase-1; universalBase>=0; universalBase--) {
			otherBaseFromUniversalBase[EDITEDMATRIX][universalBase] = negativeBase;
			negativeBase--;
		}
		 */
		// =========== Now fill in the edges of each mapping ===========
		for(int mapping=0; mapping<numMappings; mapping++) 
//			if (!(reversedInEditData && (mapping==EDITEDMATRIX)||(mapping==EDITEDMATRIXSEQUENCE))) {
				if (!(reversedInEditData && (mapping==EDITEDMATRIX))) {
				for (int i=0; i<otherBaseFromUniversalBase[mapping].length; i++) { // below specified section
					if (otherBaseFromUniversalBase[mapping][i]>=0) {
						int countDown = otherBaseFromUniversalBase[mapping][i]-1;
						for (int k=i-1; k>=0; k--) {
							otherBaseFromUniversalBase[mapping][k]=countDown;
							countDown--;
						}
						break;
					}
				}
				for (int i=otherBaseFromUniversalBase[mapping].length-1; i>=0; i--) {  // above specified section
					if (otherBaseFromUniversalBase[mapping][i]>=0) {
						int countUp = otherBaseFromUniversalBase[mapping][i]+1;
						for (int k=i+1; k<otherBaseFromUniversalBase[mapping].length; k++) {
							otherBaseFromUniversalBase[mapping][k]=countUp;
							countUp++;
						}
						break;
					}
				}
			}
		// =========== Now fill in the edges of the two edit mappings just for reversed ===========
		if (reversedInEditData) {
			for (int mapping=0; mapping<numMappings; mapping++) 
				if (mapping==EDITEDMATRIX) {
					for (int i=0; i<otherBaseFromUniversalBase[mapping].length; i++) { // below specified section
						if (otherBaseFromUniversalBase[mapping][i]>=0) {  // we've found our first specified
							int countUp =otherBaseFromUniversalBase[mapping][i]+1;
							for (int k=i-1; k>=0; k--) {
								otherBaseFromUniversalBase[mapping][k]=countUp;
								countUp++;
							}
							break;
						}
					}
					for (int i=otherBaseFromUniversalBase[mapping].length-1; i>=0; i--) {  // above specified section
						if (otherBaseFromUniversalBase[mapping][i]>=0) {  // we've found the last specified
							int countDown = otherBaseFromUniversalBase[mapping][i]-1;
							for (int k=i+1; k<otherBaseFromUniversalBase[mapping].length; k++) {
								otherBaseFromUniversalBase[mapping][k]=countDown;
								countDown--;
							}
							break;
						}
					}
				}
		}

//		Debugg.println("======= End Resetting Universal Base Registry ======= \n");

		hasBeenSet = true;
	}
	/*.................................................................................................................*/

	public int getUniversalBaseFromOtherBase(int otherBaseSystem, int otherBase) {
		if (otherBase<0) {
			int firstUniversalBase = 0;
			firstUniversalBase = universalBaseFromOtherBase[otherBaseSystem][0]+otherBase;
			if (firstUniversalBase<0)
				firstUniversalBase=0;
			return firstUniversalBase;
		}
		if (otherBase>=universalBaseFromOtherBase[otherBaseSystem].length) {
			int endUniversalBase = 0;
			if (universalBaseFromOtherBase[otherBaseSystem].length-1 <0)
				return -1;
			endUniversalBase = universalBaseFromOtherBase[otherBaseSystem][universalBaseFromOtherBase[otherBaseSystem].length-1]+ (otherBase-universalBaseFromOtherBase[otherBaseSystem].length+1);
			return endUniversalBase;
		}
		return universalBaseFromOtherBase[otherBaseSystem][otherBase];
	}
	/*.................................................................................................................*/

	public int getOtherBaseFromUniversalBase(int otherBaseSystem, int universalBase) {
		if (universalBase<0)
			return -1;
		if (universalBase>=otherBaseFromUniversalBase[otherBaseSystem].length) {
			int endOtherBase = 0;
			if (otherBaseFromUniversalBase[otherBaseSystem].length-1 <0)
				return -1;
			endOtherBase = otherBaseFromUniversalBase[otherBaseSystem][otherBaseFromUniversalBase[otherBaseSystem].length-1]+ (universalBase-otherBaseFromUniversalBase[otherBaseSystem].length+1);
			return endOtherBase;
		}
		return otherBaseFromUniversalBase[otherBaseSystem][universalBase];
	}
	/*.................................................................................................................*/

	public int getEditedMatrixBaseFromUniversalBase(int universalBase) {
		if (universalBase<0 || universalBase>=otherBaseFromUniversalBase[EDITEDMATRIX].length)
			return -1;
		int editedBase = otherBaseFromUniversalBase[EDITEDMATRIX][universalBase];
		if (reversedInEditData) {
			//editedBase = editedData.getNumChars()-editedBase-1;
		}
		return editedBase;
	}
	/*.................................................................................................................*/

	public int getEditedMatrixBaseFromOtherBase(int otherBaseSystem, int otherBase) {
		if (otherBase<0 || otherBase>=universalBaseFromOtherBase[otherBaseSystem].length)
			return -1;
		int universalBase = universalBaseFromOtherBase[otherBaseSystem][otherBase];
		if (universalBase<0 || universalBase>=otherBaseFromUniversalBase[EDITEDMATRIX].length)
			return -1;
		int editedBase = otherBaseFromUniversalBase[EDITEDMATRIX][universalBase];
		if (reversedInEditData) {
			//	editedBase = editedData.getNumChars()-editedBase-1;
		}
		return editedBase;
	}
	/*.................................................................................................................*/

	public int getOtherBaseFromEditedMatrixBase(int otherBaseSystem, int matrixBase) {
		if (matrixBase<0 || matrixBase>=universalBaseFromOtherBase[EDITEDMATRIX].length)
			return -1;
		if (reversedInEditData) {
			//matrixBase = editedData.getNumChars()-matrixBase-1;
		}
		int universalBase = universalBaseFromOtherBase[EDITEDMATRIX][matrixBase];
		if (universalBase<0 || universalBase>=otherBaseFromUniversalBase[otherBaseSystem].length)
			return -1;
		return otherBaseFromUniversalBase[otherBaseSystem][universalBase];
	}
	/*.................................................................................................................*/

	public int getNumUniversalBases() {
		return totalUniversalBases;
	}


	/*.................................................................................................................*/


	public String toString() {
		StringBuffer b = new StringBuffer();
		int newTotalUniversalBases = getNumUniversalBases();

		for (int universalBase=0; universalBase<newTotalUniversalBases; universalBase++){
			b.append(" "+otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase]);
		}
		b.append("\n\n");
		for (int i=0; i<originalUntrimmedPanel.getSequence().getLength(); i++){
			b.append(" "+universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][i]);
		}
		return b.toString();
	}

	public boolean isReverseComplement() {
		return reverseComplement;
	}

	public void setReverseComplement(boolean reverseComplement) {
		this.reverseComplement = reverseComplement;
	}


}
