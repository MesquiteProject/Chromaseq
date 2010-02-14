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

import java.awt.Color;
import java.awt.Graphics;

import mesquite.categ.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.cont.lib.ContinuousData;
import mesquite.meristic.lib.*;

public class ChromaseqUniversalMapper implements MesquiteListener {
	public ContigMapper getContigMapper() {
		return contigMapper;
	}
	public static int totalCreated = 0;
	public static int totalFinalized = 0;

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
	
	double registrationConflict =0.0;

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

		ChromaseqUniversalMapper.totalCreated++;

		init();
	}

	/*.................................................................................................................*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData) {
			CharacterData data = (CharacterData)obj;
			
//Debugg.println("---- ChromaseqUniversalMapper.changed()");
			if (ChromaseqUtil.validChromaseqMatrix(data)) {
				if (!(data.singleCellSubstitution(notification) && ChromaseqUtil.isChromaseqEditedMatrix(data))){
					reset(true);
					ChromaseqUtil.fillReverseRegistryData(reverseRegistryData);
					contigDisplay.repaintPanels();
				}
			}
		} 
	}
	public void finalize() throws Throwable {
		ChromaseqUniversalMapper.totalFinalized++;
		super.finalize();
	}
	/*.................................................................................................................*/
	public void dispose() {
		if (editedData!=null)
			editedData.removeListener(this);
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
//	Debugg.println("\n======= Resetting Universal Base Registry ======= " + (resetCount++));

//	Debugg.println(toString());
		it = contigDisplay.getTaxon().getNumber();
		contigDisplay.setUniversalMapper(this);
		reversedInEditData = contigDisplay.isReversedInEditedData();
		complementedInEditData = contigDisplay.isComplementedInEditedData();

		if (contigMapper==null){
			contigMapper = ContigMapper.getContigMapper(editedData,contig, it,numTrimmedFromStart);
			contigMapper.zeroValues();
		}
		numTrimmedFromStart = contigMapper.getNumTrimmedFromStart();

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
			ChromaseqUtil.fillReverseRegistryData(reverseRegistryData);
			contigMapper.inferFromExistingRegistry(editedData,it, numTrimmedFromStart, null);
		}
		else
			contigMapper.recalc(editedData,it);


		int numResurrectedAtStart = contigMapper.getNumResurrectedAtStart();
		int lastContigBaseBeforeTrimmedEnd = contig.getNumBases() - contigMapper.getNumTrimmedFromEnd();
		int contigBase = numTrimmedFromStart-1;

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
		int paddingBeforeOriginalTrim = contig.getNumPaddedBeforeContigBase(numTrimmedFromStart) ;
		SequenceCanvas sequenceCanvas = originalUntrimmedPanel.getCanvas();
		MesquiteSequence sequence = originalUntrimmedPanel.getSequence();
		int contigLength = sequence.getLength();

		if (sequenceCanvas!=null && sequence!=null){
			// ............  first, tabulate how many bases are added BEFORE any base in the contig, not counting bases added in trimmed region
			int[] addedBases = new int[sequence.getLength()];
			for (int sequenceBase=0; sequenceBase<addedBases.length; sequenceBase++){
				addedBases[sequenceBase] = 0;
			}
			int totalAdded =0;
			for (int sequenceBase=0; sequenceBase<addedBases.length; sequenceBase++){
				if (sequenceBase>numTrimmedFromStart) {  // only count those right of the trimmed ones, as ones before the trimmed ones don't count toward the universal base
					totalAdded+= contigMapper.getAddedBases(sequenceBase);
					addedBases[sequenceBase] = totalAdded;
				}
			}
			
			//............  now go through the contig.  note that for this, the sequenceBase IS the contigBase
			for (int sequenceBase=0; sequenceBase<contigLength; sequenceBase++){
				int universalBase = sequenceBase + contig.getReadExcessAtStart();
				if (sequenceBase<addedBases.length)
					universalBase += addedBases[sequenceBase];  // or should this be sequenceBase+1
				//............  don't have to adjust for padding, as the pads are IN the original untrimmed/contig.  just have to adjust for added bases
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

			 // as we know the trimmed bases are ONLY a subset of the contig, then we can go through the contig and find them all
			for (contigBase=numTrimmedFromStart; contigBase<contigLength; contigBase++){  
				if (!contig.getIsPadding(contigBase)) {
					int sequenceBase = contigBase-numTrimmedFromStart;
					int numPads = contig.getNumPaddedBeforeContigBase(contigBase)-paddingBeforeOriginalTrim;  // account for padding
					if (numPads<prevNumPads)   // in case numPads is -ve
						numPads=prevNumPads;
					sequenceBase -= numPads;
					if (sequenceBase>=sequenceLength) continue;

					int universalBase = startingUniversalBase + sequenceBase + contigMapper.getNumAddedBefore(contigBase)-startingAddedBeforeOriginalTrim;   
					universalBase+=numPads;
					prevNumPads = numPads;
					if (universalBase<otherBaseFromUniversalBase[ORIGINALTRIMMEDSEQUENCE].length)
						otherBaseFromUniversalBase[ORIGINALTRIMMEDSEQUENCE][universalBase] = sequenceBase;
					universalBaseFromOtherBase[ORIGINALTRIMMEDSEQUENCE][sequenceBase] = universalBase;
				}
			}
		}


		// =========== Calculate mappings for EditedInMatrix sequence panel ===========

		int sequenceBase = -1;
		int numChars = editedData.getNumChars();
		int lastPositionInOriginal = -1;
		int matrixBasesSinceLastOriginal = 0;
		prevNumPads = 0;

		int startingUniversalBase = universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][numTrimmedFromStart-numResurrectedAtStart]-startingAddedBeforeOriginalTrim;

		if (!reversedInEditData) { 

			for (int matrixBase = 0; matrixBase< numChars; matrixBase++){  // going through the sourceData object.  This is either the edited matrix or the original matrix

				if (!editedData.isInapplicable(matrixBase,it)){     // the only ones we record are the non-inapplicable ones
					sequenceBase++;
					int positionInOriginal = registryData.getState(matrixBase, it);
					if (registryData.isCombinable(matrixBase, it) && positionInOriginal>=0){ 
						matrixBasesSinceLastOriginal = 0;
						lastPositionInOriginal=positionInOriginal;
					} else
						matrixBasesSinceLastOriginal ++;

					int lastContigBaseWithOriginalMatch = contigMapper.getContigBaseFromOriginalMatrixBase(lastPositionInOriginal, it);   // last contig base encountered that has an original base; won't incude pads
					int numPadsSinceLastOriginal=0;
					int additionalContigBases = 0;
					if (matrixBasesSinceLastOriginal>0)  // now let's find is this really corresponds to another contigBase
						for (int i = lastContigBaseWithOriginalMatch+1; i<contig.getNumBases(); i++){
							int count = 0;
							if (contig.getIsPadding(i)){
								count++;
								numPadsSinceLastOriginal++;
							}
							count+= contigMapper.getAddedBases(i);
							if (count+additionalContigBases > matrixBasesSinceLastOriginal){  // we've gone past were we are
								break;
							}
							additionalContigBases += count;			
						}
					contigBase = lastContigBaseWithOriginalMatch + additionalContigBases;
						
					int universalBase = startingUniversalBase+sequenceBase;
					if (contigBase>=numTrimmedFromStart && contigBase<lastContigBaseBeforeTrimmedEnd)
						universalBase += (contigMapper.getNumDeletedBefore(contigBase)-startingDeletedBeforeOriginalTrim);
					else if (contigBase>=lastContigBaseBeforeTrimmedEnd)
						universalBase += (contigMapper.getNumDeletedBefore(lastContigBaseBeforeTrimmedEnd)-startingDeletedBeforeOriginalTrim);
					int numPads = contig.getNumPaddedBeforeContigBase(contigBase)-paddingBeforeOriginalTrim;
					if (numPads<prevNumPads)
						numPads=prevNumPads;
					universalBase+=numPads;
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
			for (int matrixBase = numChars-1; matrixBase>=0 ; matrixBase--){  // going through the sourceData object.  This is either the edited matrix or the original matrix

				if (!editedData.isInapplicable(matrixBase,it)){     // the only ones we record are the non-inapplicable ones
					sequenceBase++;
					int positionInOriginal = registryData.getState(matrixBase, it);
					if (registryData.isCombinable(matrixBase, it) && positionInOriginal>=0){ 
						matrixBasesSinceLastOriginal = 0;
						lastPositionInOriginal=positionInOriginal;
					} else
						matrixBasesSinceLastOriginal ++;

					int lastContigBaseWithOriginalMatch = contigMapper.getContigBaseFromOriginalMatrixBase(lastPositionInOriginal, it);   // last contig base encountered that has an original base; won't incude pads
					int numPadsSinceLastOriginal=0;
					int additionalContigBases = 0;
					if (matrixBasesSinceLastOriginal>0)  // now let's find is this really corresponds to another contigBase
						for (int i = lastContigBaseWithOriginalMatch+1; i<contig.getNumBases(); i++){
							int count = 0;
							if (contig.getIsPadding(i)){
								count++;
								numPadsSinceLastOriginal++;
							}
							count+= contigMapper.getAddedBases(i);
							if (count+additionalContigBases > matrixBasesSinceLastOriginal){  // we've gone past were we are
								break;
							}
							additionalContigBases += count;			
						}
					contigBase = lastContigBaseWithOriginalMatch + additionalContigBases;
						
					int universalBase = startingUniversalBase+sequenceBase;
					if (contigBase>=numTrimmedFromStart && contigBase<lastContigBaseBeforeTrimmedEnd)
						universalBase += (contigMapper.getNumDeletedBefore(contigBase)-startingDeletedBeforeOriginalTrim);
					else if (contigBase>=lastContigBaseBeforeTrimmedEnd)
						universalBase += (contigMapper.getNumDeletedBefore(lastContigBaseBeforeTrimmedEnd)-startingDeletedBeforeOriginalTrim);
					int numPads = contig.getNumPaddedBeforeContigBase(contigBase)-paddingBeforeOriginalTrim;
					if (numPads<prevNumPads)
						numPads=prevNumPads;
					universalBase+=numPads;
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

		// =========== Now fill in the edges of each mapping ===========
		for(int mapping=0; mapping<numMappings; mapping++) 
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

		double newRegistrationConflict = calcMatchToEditedScore(it);
		
		if (newRegistrationConflict>20.0)
			if (registrationConflict<20.0){
				String name = contigDisplay.getTaxon().getName();
				MesquiteMessage.discreetNotifyUser("\nThe reads and sequences for " + name + " appear to be improperly registered. You may wish to choose Force Reregistration from the Chromatogram menu to resolve this. \n\n[New registration conflict score: " + newRegistrationConflict + ", Previous registration conflict score: " + registrationConflict+"]");
			}
		registrationConflict = newRegistrationConflict;
		
		if (MesquiteTrunk.debugMode){
			System.out.println("ChromaseqUniversalMapper");
			System.out.println("     Registration conflict score: "+newRegistrationConflict);
		}
	
		hasBeenSet = true;

	
//		Debugg.println("\n======= End Resetting Universal Base Registry ======= " + resetCount);
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
	
	/*...........................................................................*/
	public double calcMatchToEditedScoreOfRead(int whichRead, int it) {	
		Read read = contig.getRead(whichRead);
		double conflicts=0.0;
		double bases=0.0;
		for (int universalBase=0; universalBase < getNumUniversalBases(); universalBase++) {
			int contigBase = getOtherBaseFromUniversalBase(ACEFILECONTIG, universalBase);
			int readBase = read.getReadBaseFromContigBase(contigBase);
			if (readBase>=0 && readBase<read.getBasesLength()) {
				int qual = read.getPhdBaseQuality(readBase);

				if (qual>20) {
					char c = read.getPhdBaseChar(readBase);
					long readState = DNAState.fromCharStatic(c);
					int matrixBase = getEditedMatrixBaseFromUniversalBase(universalBase);
					
					long editedState = editedData.getState(matrixBase,it);
					if (complementedInEditData) 
						editedState = DNAState.complement(editedState);
					 if (!DNAState.equalsIgnoreCase(readState, editedState))
						conflicts++;
					 bases++;
				}
			} 
		}

		int editedBases = 0;
		double editedBasesQuality = 0.0;
		ContinuousData qualityData = ChromaseqUtil.getQualityData(editedData);
		for (int ic=0; ic < qualityData.getNumChars(); ic++) {
			double quality = qualityData.getState(ic, it, 0);
			if (quality>20 && MesquiteDouble.isCombinable(quality)) {
				editedBasesQuality+= quality;
				editedBases++;
			} 
		}
		if (editedBases>0)
			editedBasesQuality = editedBasesQuality/editedBases;

		if (bases==0  || editedBasesQuality<20.0)
			return 0.0;
		else
			return (conflicts/bases)*editedBasesQuality;
	}
	/*...........................................................................*/
	public double calcMatchToEditedScore(int it) {	
		if (contig==null || contig.getNumReadsInContig()<=1)
			return 0.0;
		double total = 0.0;
		for (int whichRead = 0; whichRead<contig.getNumReadsInContig(); whichRead++) {
			total += calcMatchToEditedScoreOfRead(whichRead,it);
		}
		double score = total/contig.getNumReadsInContig();
		return score;
	}
	/*.................................................................................................................*/
	private String firstApplicable(CharacterData data, int it, int num) {
		StringBuffer sb = new StringBuffer();
		boolean foundOne = false;
		int count = 0;
		for (int ic=0; ic<data.getNumChars(); ic++) {
			if (!data.isInapplicable(ic, it)){
				if (!foundOne)
					sb.append("["+ic+"] ");
				foundOne = true;
			}
			if (foundOne && count<num) {
				if (data instanceof MeristicData){
					sb.append(" ");
					((MeristicData)data).statesIntoStringBuffer(ic, it, sb, true);
				}
				else if (data instanceof DNAData)
					((DNAData)data).statesIntoStringBuffer(ic, it, sb, true);
				count++;
			}
		}
		return sb.toString();
	}
	/*.................................................................................................................*/

	public String toString() {
		StringBuffer b = new StringBuffer();
	
		b.append("\n\n\n=========  ChromaseqUniversalMapper =============\n");
		b.append("\n"+ editedData.getTaxa().getName(it));
		b.append(contigMapper.toString());
		b.append("\n.........");
		b.append("\n totalUniversalBases: " + totalUniversalBases);
		b.append("\n numTrimmedFromStart: " + numTrimmedFromStart);
		b.append("\n.........");
		
		b.append("\n....  registryData ");
		b.append("\n" + firstApplicable(registryData,it,20));
		b.append("\n....  editedData ");
		b.append("\n" + firstApplicable(editedData,it,20));
		b.append("\n....  originalData ");
		b.append("\n" + firstApplicable(originalData,it,20));
		b.append("\n....  reverseRegistryData");
		b.append("\n" + firstApplicable(reverseRegistryData,it,20));
	/*	for (int universalBase=0; universalBase<newTotalUniversalBases; universalBase++){
			b.append(" "+otherBaseFromUniversalBase[ORIGINALUNTRIMMEDSEQUENCE][universalBase]);
		}
		b.append("\n\n");
		for (int i=0; i<originalUntrimmedPanel.getSequence().getLength(); i++){
			b.append(" "+universalBaseFromOtherBase[ORIGINALUNTRIMMEDSEQUENCE][i]);
		}
		*/
		b.append("\n============================================\n");
		return b.toString();
	}

	public boolean isReverseComplement() {
		return reverseComplement;
	}

	public void setReverseComplement(boolean reverseComplement) {
		this.reverseComplement = reverseComplement;
	}



}
