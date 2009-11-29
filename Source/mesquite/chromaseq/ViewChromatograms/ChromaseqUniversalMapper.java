/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
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
	int numTrimmedFromStart= 0;
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

		contigMapper = ContigMapper.getContigMapper(editedData,contig, it);

		init();
	}

	/*.................................................................................................................*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData) {
			
Debugg.println("ChromaseqUniversalMapper.changed()");

			reset(true);

			ChromaseqUtil.fillReverseRegistryData(reverseRegistryData);
//			ContigMapper.checkTaxonNumbers(editedData);
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
	public void createOtherBaseFromUniversalBase(int totalUniversalBases) {
		otherBaseFromUniversalBase = new int[numMappings][totalUniversalBases];
//		contigDisplay.setTotalNumInitialOverallBases(totalUniversalBases);
	}
	/*.................................................................................................................*/
	public void createOtherBaseFromUniversalBase() {
		otherBaseFromUniversalBase = new int[numMappings][contigDisplay.getTotalNumUniversalBases()];
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
	public synchronized void reset(boolean forceFullContigMapSetup) {
//	Debugg.println("======= Resetting Universal Base Registry ======= " + (resetCount++));
		//		Debugg.printStackTrace("\n\nuniversalMapper reset: " + Thread.currentThread()+"\n\n");

		// =========== Calculate total number of universal bases ===========

		//	totalNumAddedBases=ChromaseqUtil.getTotalNumBasesAddedBeyondPhPhBases(editedData, it);
		//	totalNumDeletedBases=ChromaseqUtil.getTotalNumOriginalBasesTurnedToGaps(editedData, it);
		it = contigDisplay.getTaxon().getNumber();
		contigDisplay.setUniversalMapper(this);

		//MolecularData originalData = ChromaseqUtil.getOriginalData(editedData);
		reversedInEditData = contigDisplay.isReversedInEditedData();
		complementedInEditData = contigDisplay.isComplementedInEditedData();
		numTrimmedFromStart = contigMapper.getNumTrimmedFromStart();
		//		Debugg.println("   numBasesOriginallyTrimmedFromStartOfPhPhContig: " + numBasesOriginallyTrimmedFromStartOfPhPhContig);


		if (contigMapper==null){
			contigMapper = ContigMapper.getContigMapper(editedData,contig, it,numTrimmedFromStart);
			contigMapper.zeroValues();
		}
		if (contigMapper.getContig()==null)
			contigMapper.setContig(contigDisplay.getContig());
		if (!contigMapper.hasBeenSetUp()) {
			if (contigMapper.getStoredInFile()){
				contigMapper.setNumTrimmedFromStart(numTrimmedFromStart);
				contigMapper.recalc(editedData,it);
			}
			else
				contigMapper.setUp(editedData,it, numTrimmedFromStart);
		} 
		else if (forceFullContigMapSetup) {
			contigMapper.inferFromExistingRegistry(editedData,it, numTrimmedFromStart);
		}
		else
			contigMapper.recalc(editedData,it);

		int numResurrectedAtStart = contigMapper.getNumResurrectedAtStart();
		//		int numBasesOriginallyTrimmedFromEndOfPhPhContig = contigMapper.getNumBasesOriginallyTrimmedFromEndOfPhPhContig();
		int originalEndOfTrimmedContig = contig.getNumBases() - contigMapper.getNumTrimmedFromEnd();
		int contigBase = numTrimmedFromStart-1;

//		Debugg.println(contigMapper.toString());

		int totalNumAddedBases=contigMapper.getTotalNumberAddedBases();

		/* contigDisplay.getTotalNumOverallBases() is the number of bases according to the contig 
		 * - it's the number of bases in the contig plus the extra bases in front and at the end (as found in individual reads
		 * that extend beyond the contig.  So, it's the length of overall bases according to the PhredPhrap cloud.
		 */
		totalUniversalBases = contigDisplay.getTotalNumInitialOverallBases() + contig.getNumPadded();

		/* Now let's add to this the bases that 
		 */
		totalUniversalBases += totalNumAddedBases;

		if (otherBaseFromUniversalBase==null || otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE].length!=totalUniversalBases)
			createOtherBaseFromUniversalBase();
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<otherBaseFromUniversalBase[mapping].length; i++) 
				otherBaseFromUniversalBase[mapping][i]= MesquiteInteger.unassigned;


		createUniversalBaseFromOtherBase();
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<universalBaseFromOtherBase[mapping].length; i++) 
				universalBaseFromOtherBase[mapping][i]= MesquiteInteger.unassigned;
		


		// =========== Calculate mappings for the original untrimmed panel (i.e., the "Original Untrimmed" one) ===========


		int prevNumPads = 0;
		int startingNumAddedDeletedBefore = contigMapper.getNumAddedDeletedBefore(numTrimmedFromStart) ;
		int startingAddedBeforeOriginalTrim = contigMapper.getNumAddedBefore(numTrimmedFromStart) ;
		int startingDeletedBeforeOriginalTrim = contigMapper.getNumDeletedBefore(numTrimmedFromStart) ;
		int paddingBeforeOriginalTrim = contig.getNumPaddedBefore(numTrimmedFromStart) ;

		/*
		Debugg.println("   totalUniversalBases: " + totalUniversalBases);
		Debugg.println("   otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE].length: " + otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE].length);
		Debugg.println("   totalNumAddedBases: " + totalNumAddedBases);
		Debugg.println("   numTrimmedFromStart: " + numTrimmedFromStart);
		Debugg.println("   startingNumAddedDeletedBefore: " + startingNumAddedDeletedBefore);
		Debugg.println("   startingAddedBeforeOriginalTrim: " + startingAddedBeforeOriginalTrim);
		Debugg.println("   startingDeletedBeforeOriginalTrim: " + startingDeletedBeforeOriginalTrim);
		Debugg.println("   paddingBeforeOriginalTrim: " + paddingBeforeOriginalTrim);

	*/
		
		SequenceCanvas sequenceCanvas = originalUntrimmedPanel.getCanvas();
		MesquiteSequence sequence = originalUntrimmedPanel.getSequence();

//	Debugg.println(sequence.getSequence());
	
	
		if (sequenceCanvas!=null && sequence!=null){
			int[] addedBases = new int[sequence.getLength()];//+contig.getReadExcessAtStart()];
			for (int sequenceBase=0; sequenceBase<addedBases.length; sequenceBase++){
				addedBases[sequenceBase] = 0;
			}
			int totalAdded =0;
			for (int sequenceBase=0; sequenceBase<addedBases.length; sequenceBase++){
				if (sequenceBase>=numTrimmedFromStart) {
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
				if (sequenceBase<5 && false){
					Debugg.println("   sequenceBase: " + sequenceBase);
					Debugg.println("     universalBase: " + universalBase);
					Debugg.println("     addedBases[sequenceBase]: " + addedBases[sequenceBase]);
				}
				if (sequenceBase<addedBases.length)
					universalBase += addedBases[sequenceBase];
				/*	int numPads = contig.getNumPaddedBefore(sequenceBase);  // account for padding
				if (numPads<prevNumPads)
					numPads=prevNumPads;
				universalBase+=numPads;
				prevNumPads = numPads;
				 */
				if (universalBase>= totalUniversalBases)
					continue;
				if (universalBase<otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE].length)
					otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase] = sequenceBase;
				universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][sequenceBase] = universalBase;
				if (universalBase<otherBaseFromUniversalBase[ACEFILECONTIG].length)
					otherBaseFromUniversalBase[ACEFILECONTIG][universalBase] = sequenceBase;
				universalBaseFromOtherBase[ACEFILECONTIG][sequenceBase] = universalBase;
			}
		}

		// =========== Calculate mappings for the original import panel (i.e., the "Original.Trimmed" one - just like Original.Untrimmed but trimmed) ===========
		prevNumPads = 0;
		sequenceCanvas = originalTrimmedSequencePanel.getCanvas();
		sequence = originalTrimmedSequencePanel.getSequence();
		if (sequenceCanvas!=null && sequence!=null){
			int sequenceLength = sequence.getLength();
			int startingUniversalBase = universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numTrimmedFromStart];


			for (int sequenceBase=0; sequenceBase<sequenceLength; sequenceBase++){
				contigBase = sequenceBase+numTrimmedFromStart;
				
				// should the next line be contigMapper.getNumAddedBefore(contigBase) or contigMapper.getNumAddedBefore(contigBase+1)
				int universalBase = startingUniversalBase + sequenceBase + contigMapper.getNumAddedBefore(contigBase)-startingAddedBeforeOriginalTrim;   
				int numPads = contig.getNumPaddedBefore(otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase]);  // account for padding
				if (numPads<prevNumPads)
					numPads=prevNumPads;
				universalBase+=numPads-paddingBeforeOriginalTrim;  //don't need to deal with padding as padding is IN the original untrimmed sequence and is thus already in the "num deleted before"
				prevNumPads = numPads;
				if (universalBase<otherBaseFromUniversalBase[ORIGINALTRIMMEDSEQUENCE].length)
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


		int startingUniversalBase = universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numTrimmedFromStart-numResurrectedAtStart]-startingAddedBeforeOriginalTrim;

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
					contigBase = numTrimmedFromStart+lastPositionInOriginal-1;  //-startingAddedBeforeOriginalTrim
					if (numBasesFound==1 && false){
						Debugg.println("   sequenceBase: " + sequenceBase);
						Debugg.println("   contigBase: " + contigBase);
						Debugg.println("   contigMapper.getNumDeletedBefore(contigBase): " + contigMapper.getNumDeletedBefore(contigBase));
						Debugg.println("   startingUniversalBase: " + startingUniversalBase);
						Debugg.println("   universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig]: " + universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numTrimmedFromStart]);
						Debugg.println("   startingNumAddedDeletedBefore: " + startingNumAddedDeletedBefore);
						Debugg.println("   numResurrectedAtStart: " + numResurrectedAtStart);
						Debugg.println("   startingAddedBeforeOriginalTrimm: " + startingAddedBeforeOriginalTrim);
						Debugg.println("   startingDeletedBeforeOriginalTrimm: " + startingDeletedBeforeOriginalTrim);
						Debugg.println("   originalEndOfTrimmedContig: " + originalEndOfTrimmedContig);
					}
					int universalBase = startingUniversalBase+numBasesFound;
					if (contigBase>=numTrimmedFromStart && contigBase<originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(contigBase+1)-startingDeletedBeforeOriginalTrim);
					else if (contigBase>=originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(originalEndOfTrimmedContig)-startingDeletedBeforeOriginalTrim);
					int numPads = contig.getNumPaddedBefore(otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase-paddingBeforeOriginalTrim]);
					if (numPads<prevNumPads)
						numPads=prevNumPads;
				//	if (contigBase<numTrimmedFromStart)
					universalBase+=numPads-paddingBeforeOriginalTrim;  // don't need to deal with padding as padding is IN the original untrimmed sequence and is thus already in the "num deleted before"
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
					contigBase = numTrimmedFromStart+lastPositionInOriginal-1;  //-startingAddedBeforeOriginalTrim
					if (numBasesFound==1 && false){
						Debugg.println("   sequenceBase: " + sequenceBase);
						Debugg.println("   contigBase: " + contigBase);
						Debugg.println("   contigMapper.getNumDeletedBefore(contigBase): " + contigMapper.getNumDeletedBefore(contigBase));
						Debugg.println("   startingUniversalBase: " + startingUniversalBase);
						Debugg.println("   universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numBasesOriginallyTrimmedFromStartOfPhPhContig]: " + universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numTrimmedFromStart]);
						Debugg.println("   startingNumAddedDeletedBefore: " + startingNumAddedDeletedBefore);
						Debugg.println("   numResurrectedAtStart: " + numResurrectedAtStart);
						Debugg.println("   startingAddedBeforeOriginalTrimm: " + startingAddedBeforeOriginalTrim);
						Debugg.println("   startingDeletedBeforeOriginalTrimm: " + startingDeletedBeforeOriginalTrim);
						Debugg.println("   originalEndOfTrimmedContig: " + originalEndOfTrimmedContig);
					}
					int universalBase = startingUniversalBase+numBasesFound;
					if (contigBase>numTrimmedFromStart && contigBase<originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(contigBase+1)-startingDeletedBeforeOriginalTrim);
					else if (contigBase>=originalEndOfTrimmedContig)
						universalBase += (contigMapper.getNumDeletedBefore(originalEndOfTrimmedContig)-startingDeletedBeforeOriginalTrim);
					int numPads = contig.getNumPaddedBefore(otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase]);
					if (numPads<prevNumPads)
						numPads=prevNumPads;
					universalBase+=numPads-paddingBeforeOriginalTrim;  // don't need to deal with padding as padding is IN the original untrimmed sequence and is thus already in the "num deleted before"
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

		if (false) {
			int start = 450;
			int end = 510;
			for (int universalBase = start; universalBase<end;universalBase++) {
				Debugg.println(" universalBase: " + universalBase + ", otherBaseFromUniversalBase[editedMatrixSequence]: " + otherBaseFromUniversalBase[EDITEDMATRIXSEQUENCE][universalBase]);
			}
		}

//		Debugg.println("======= End Resetting Universal Base Registry ======= " + resetCount + "\n");

		hasBeenSet = true;
	}
	/*.................................................................................................................*/

	public int getUniversalBaseFromOtherBase(int otherBaseSystem, int otherBase) {
		if (!MesquiteInteger.isCombinable(otherBase))
			return MesquiteInteger.unassigned;
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
				return MesquiteInteger.unassigned;
			endUniversalBase = universalBaseFromOtherBase[otherBaseSystem][universalBaseFromOtherBase[otherBaseSystem].length-1]+ (otherBase-universalBaseFromOtherBase[otherBaseSystem].length+1);
			return endUniversalBase;
		}
		return universalBaseFromOtherBase[otherBaseSystem][otherBase];
	}
	/*.................................................................................................................*/

	public int getOtherBaseFromUniversalBase(int otherBaseSystem, int universalBase) {
		if (!MesquiteInteger.isCombinable(universalBase) || universalBase<0)
			return MesquiteInteger.unassigned;
		if (universalBase>=otherBaseFromUniversalBase[otherBaseSystem].length) {
			int endOtherBase = 0;
			if (otherBaseFromUniversalBase[otherBaseSystem].length-1 <0)
				return MesquiteInteger.unassigned;
			endOtherBase = otherBaseFromUniversalBase[otherBaseSystem][otherBaseFromUniversalBase[otherBaseSystem].length-1]+ (universalBase-otherBaseFromUniversalBase[otherBaseSystem].length+1);
			return endOtherBase;
		}
		return otherBaseFromUniversalBase[otherBaseSystem][universalBase];
	}
	/*.................................................................................................................*/

	public int getEditedMatrixBaseFromUniversalBase(int universalBase) {
		if (!MesquiteInteger.isCombinable(universalBase) || universalBase<0 || universalBase>=otherBaseFromUniversalBase[EDITEDMATRIX].length)
			return MesquiteInteger.unassigned;
		int editedBase = otherBaseFromUniversalBase[EDITEDMATRIX][universalBase];
		if (reversedInEditData) {
			//editedBase = editedData.getNumChars()-editedBase-1;
		}
		return editedBase;
	}
	/*.................................................................................................................*/

	public int getEditedMatrixBaseFromOtherBase(int otherBaseSystem, int otherBase) {
		if (!MesquiteInteger.isCombinable(otherBase) || otherBase<0 || otherBase>=universalBaseFromOtherBase[otherBaseSystem].length)
			return MesquiteInteger.unassigned;
		int universalBase = universalBaseFromOtherBase[otherBaseSystem][otherBase];
		if (!MesquiteInteger.isCombinable(universalBase) || universalBase<0 || universalBase>=otherBaseFromUniversalBase[EDITEDMATRIX].length)
			return MesquiteInteger.unassigned;
		int editedBase = otherBaseFromUniversalBase[EDITEDMATRIX][universalBase];
		if (reversedInEditData) {
			//	editedBase = editedData.getNumChars()-editedBase-1;
		}
		return editedBase;
	}
	/*.................................................................................................................*/

	public int getOtherBaseFromEditedMatrixBase(int otherBaseSystem, int matrixBase) {
		if (!MesquiteInteger.isCombinable(matrixBase) || matrixBase<0 || matrixBase>=universalBaseFromOtherBase[EDITEDMATRIX].length)
			return MesquiteInteger.unassigned;
		int universalBase = universalBaseFromOtherBase[EDITEDMATRIX][matrixBase];
		if (!MesquiteInteger.isCombinable(universalBase) || universalBase<0 || universalBase>=otherBaseFromUniversalBase[otherBaseSystem].length)
			return MesquiteInteger.unassigned;
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
