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
	ContigMapper contigMapper;
	int totalUniversalBases=0;
	//	int totalNumAddedBases=0;
	//	int totalNumDeletedBases=0;
	DNAData editedData;
	DNAData originalData;
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
		originalData = ChromaseqUtil.getOriginalData(editedData);
		registryData = ChromaseqUtil.getRegistryData(editedData);
		reverseRegistryData = ChromaseqUtil.getReverseRegistryData(editedData);

		reversedInEditData = contigDisplay.isReversedInEditedData();
		complementedInEditData = contigDisplay.isComplementedInEditedData();

		numBasesOriginallyTrimmedFromStartOfPhPhContig = contig.getNumBasesOriginallyTrimmedFromStartOfPhPhContig(editedData, it);
		contigMapper = ContigMapper.getContigMapper(editedData,contig, it,numBasesOriginallyTrimmedFromStartOfPhPhContig);

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
//   Debugg.println("======= Resetting Universal Base Registry ======= " + (resetCount++));
		//		Debugg.printStackTrace("\n\nuniversalMapper reset: " + Thread.currentThread()+"\n\n");

		// =========== Calculate total number of universal bases ===========

		//	totalNumAddedBases=ChromaseqUtil.getTotalNumBasesAddedBeyondPhPhBases(editedData, it);
		//	totalNumDeletedBases=ChromaseqUtil.getTotalNumOriginalBasesTurnedToGaps(editedData, it);


//		ChromaseqUtil.fillAddedBaseData(contigDisplay,  editedData, it);

		reversedInEditData = contigDisplay.isReversedInEditedData();
		complementedInEditData = contigDisplay.isComplementedInEditedData();
		numBasesOriginallyTrimmedFromStartOfPhPhContig = contigDisplay.getNumBasesOriginallyTrimmedFromStartOfPhPhContig();
		if (contigMapper==null){
			contigMapper = ContigMapper.getContigMapper(editedData,contig, it,numBasesOriginallyTrimmedFromStartOfPhPhContig);
			contigMapper.zeroValues();
		}
		if (contigMapper.getContig()==null)
			contigMapper.setContig(contigDisplay.getContig());
		if (!contigMapper.hasBeenSetUp())
			if (contigMapper.getStoredInFile()){
				contigMapper.setNumTrimmedFromStart(numBasesOriginallyTrimmedFromStartOfPhPhContig);
				contigMapper.recalc(editedData,it);
			}
			else
				contigMapper.setUp(editedData,it, numBasesOriginallyTrimmedFromStartOfPhPhContig);
		int numResurrectedAtStart = contigMapper.getNumResurrectedAtStart();
//		int numBasesOriginallyTrimmedFromEndOfPhPhContig = contigMapper.getNumBasesOriginallyTrimmedFromEndOfPhPhContig();
		int originalEndOfTrimmedContig = contig.getNumBases() - contigMapper.getNumBasesOriginallyTrimmedFromEndOfPhPhContig();
		int contigBase = numBasesOriginallyTrimmedFromStartOfPhPhContig-1;

//				Debugg.println(contigMapper.toString());

		int totalNumAddedDeletedBases=contigMapper.getTotalNumberAddedDeletedBases();

		/* contigDisplay.getTotalNumOverallBases() is the number of bases according to the contig 
		 * - it's the number of bases in the contig plus the extra bases in front and at the end (as found in individual reads
		 * that extend beyond the contig.  So, it's the length of overall bases according to the PhredPhrap cloud.
		 */
		totalUniversalBases = contigDisplay.getTotalNumInitialOverallBases() + contig.getNumPadded();

		/* Now let's add to this the bases that 
		 */
		totalUniversalBases += totalNumAddedDeletedBases;

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


		int startingNumAddedDeletedBefore = contigMapper.getNumAddedDeletedBefore(numBasesOriginallyTrimmedFromStartOfPhPhContig) ;
		int startingAddedBeforeOriginalTrim = contigMapper.getNumAddedBefore(numBasesOriginallyTrimmedFromStartOfPhPhContig) ;
		int startingDeletedBeforeOriginalTrim = contigMapper.getNumDeletedBefore(numBasesOriginallyTrimmedFromStartOfPhPhContig) ;


		MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		SequenceCanvas sequenceCanvas = originalUntrimmedPanel.getCanvas();
		MesquiteSequence sequence = originalUntrimmedPanel.getSequence();
		if (sequenceCanvas!=null && sequence!=null){
			int[] addedBases = new int[sequence.getLength()];//+contig.getReadExcessAtStart()];
			for (int sequenceBase=0; sequenceBase<addedBases.length; sequenceBase++){
				addedBases[sequenceBase] = 0;
			}
			int totalAdded =0;
			for (int sequenceBase=0; sequenceBase<addedBases.length; sequenceBase++){
				if (sequenceBase>=numBasesOriginallyTrimmedFromStartOfPhPhContig) {
					totalAdded+= contigMapper.getAddedBases(sequenceBase);
					addedBases[sequenceBase] = totalAdded;
				}
				//	if (sequenceBase>numBasesOriginallyTrimmedFromStartOfPhPhContig && contigMapper.getDeletedBase(sequenceBase))
				//		addedBases[sequenceBase]++;
			}
			/*	int totalAddedBases = 0;
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
			 */
//			Debugg.println("   contig.getReadExcessAtStart(): " + contig.getReadExcessAtStart());
			for (int sequenceBase=0; sequenceBase<sequence.getLength(); sequenceBase++){
				int universalBase = sequenceBase + contig.getReadExcessAtStart();
				if (sequenceBase<0 && false){
					Debugg.println("   sequenceBase: " + sequenceBase);
					Debugg.println("     universalBase: " + universalBase);
					Debugg.println("     addedBases[sequenceBase+1]: " + addedBases[sequenceBase+1]);
				}
				if (sequenceBase<addedBases.length-1)
					universalBase += addedBases[sequenceBase];
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
			int sequenceLength = sequence.getLength();

			/*
		Debugg.println("   totalUniversalBases: " + totalUniversalBases + "   sequenceLength: " + sequenceLength);
			Debugg.println("   contig.getReadExcessAtStart(): " + contig.getReadExcessAtStart() + "   numBasesOriginallyTrimmedFromStartOfPhPhContig: " + numBasesOriginallyTrimmedFromStartOfPhPhContig);
			Debugg.println("   totalAddedBases: " + totalAddedBases);
			Debugg.println("   contigDisplay.getNumBasesAddedToStart(): " + contigDisplay.getNumBasesAddedToStart());
			Debugg.println("   contigDisplay.getNumBasesAddedToEnd(): " + contigDisplay.getNumBasesAddedToEnd());
			 */
			int startingUniversalBase = universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig];

			for (int sequenceBase=0; sequenceBase<sequenceLength; sequenceBase++){
				contigBase = sequenceBase+numBasesOriginallyTrimmedFromStartOfPhPhContig;
				int universalBase = startingUniversalBase + sequenceBase + contigMapper.getNumAddedBefore(contigBase)-startingAddedBeforeOriginalTrim;
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
		int lastPositionInOriginal = -1;

		//	Debugg.println("   numChars: " + numChars);

		//	boolean firstTimeThrough = true;
		prevNumPads = 0;


		int startingUniversalBase = universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig-numResurrectedAtStart]-startingAddedBeforeOriginalTrim;
		
		if (!reversedInEditData) { 

			for (int matrixBase = 0; matrixBase< numChars; matrixBase++){  // going through the sourceData object.  This is either the edited matrix or the original matrix

				if (!editedData.isInapplicable(matrixBase,it)){
					int positionInOriginal = registryData.getState(matrixBase, it);
					if (registryData.isCombinable(matrixBase, it) && positionInOriginal>=0){ 
						numOriginalBasesFound++;
						lastPositionInOriginal=positionInOriginal;
					}

					numBasesFound++;

					int sequenceBase = numBasesFound;
					contigBase = numBasesOriginallyTrimmedFromStartOfPhPhContig+lastPositionInOriginal-1;  //-startingAddedBeforeOriginalTrim
					if (numBasesFound==1 && false){
						Debugg.println("   sequenceBase: " + sequenceBase);
						Debugg.println("   contigBase: " + contigBase);
						Debugg.println("   contigMapper.getNumDeletedBefore(contigBase): " + contigMapper.getNumDeletedBefore(contigBase));
						Debugg.println("   startingUniversalBase: " + startingUniversalBase);
						Debugg.println("   universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig]: " + universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig]);
						Debugg.println("   startingNumAddedDeletedBefore: " + startingNumAddedDeletedBefore);
						Debugg.println("   numResurrectedAtStart: " + numResurrectedAtStart);
						Debugg.println("   startingAddedBeforeOriginalTrimm: " + startingAddedBeforeOriginalTrim);
						Debugg.println("   startingDeletedBeforeOriginalTrimm: " + startingDeletedBeforeOriginalTrim);
						Debugg.println("   originalEndOfTrimmedContig: " + originalEndOfTrimmedContig);
					}
					int universalBase = startingUniversalBase+numBasesFound;
					if (contigBase>numBasesOriginallyTrimmedFromStartOfPhPhContig && contigBase<originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(contigBase+1)-startingDeletedBeforeOriginalTrim);
					else if (contigBase>=originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(originalEndOfTrimmedContig)-startingDeletedBeforeOriginalTrim);
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
		else {  // data are reversed!
			//			Debugg.println("   startingUniversalBase: " + startingUniversalBase);
			for (int matrixBase = numChars-1; matrixBase>=0 ; matrixBase--){  // going through the sourceData object.  This is either the edited matrix or the original matrix

				if (!editedData.isInapplicable(matrixBase,it)){
					int positionInOriginal = registryData.getState(matrixBase, it);
					if (registryData.isCombinable(matrixBase, it) && positionInOriginal>=0){ 
						numOriginalBasesFound++;
						lastPositionInOriginal=positionInOriginal;
					}

					numBasesFound++;

					int sequenceBase = numBasesFound;
					contigBase = numBasesOriginallyTrimmedFromStartOfPhPhContig+lastPositionInOriginal-1;  //-startingAddedBeforeOriginalTrim
					if (numBasesFound==1 && false){
						Debugg.println("   sequenceBase: " + sequenceBase);
						Debugg.println("   contigBase: " + contigBase);
						Debugg.println("   contigMapper.getNumDeletedBefore(contigBase): " + contigMapper.getNumDeletedBefore(contigBase));
						Debugg.println("   startingUniversalBase: " + startingUniversalBase);
						Debugg.println("   universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig]: " + universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig]);
						Debugg.println("   startingNumAddedDeletedBefore: " + startingNumAddedDeletedBefore);
						Debugg.println("   numResurrectedAtStart: " + numResurrectedAtStart);
						Debugg.println("   startingAddedBeforeOriginalTrimm: " + startingAddedBeforeOriginalTrim);
						Debugg.println("   startingDeletedBeforeOriginalTrimm: " + startingDeletedBeforeOriginalTrim);
						Debugg.println("   originalEndOfTrimmedContig: " + originalEndOfTrimmedContig);
					}
					int universalBase = startingUniversalBase+numBasesFound;
					if (contigBase>numBasesOriginallyTrimmedFromStartOfPhPhContig && contigBase<originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(contigBase+1)-startingDeletedBeforeOriginalTrim);
					else if (contigBase>=originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(originalEndOfTrimmedContig)-startingDeletedBeforeOriginalTrim);
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

				/*				if (ChromaseqUtil.isUniversalBase(editedData,matrixBase,it)){
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
				 */
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

	/*	int start = 570;
		int end = 600;
		for (int universalBase = start; universalBase<end;universalBase++) {
			Debugg.println(" universalBase: " + universalBase + ", otherBaseFromUniversalBase[editedMatrixSequence]: " + otherBaseFromUniversalBase[EDITEDMATRIXSEQUENCE][universalBase]);
		}
*/
//		Debugg.println("======= End Resetting Universal Base Registry ======= " + resetCount + "\n");

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
