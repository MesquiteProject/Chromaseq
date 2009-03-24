package mesquite.chromaseq.ViewChromatograms;

import mesquite.categ.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.*;
import mesquite.molec.*;
import mesquite.chromaseq.lib.*;
import mesquite.meristic.lib.*;

public class ChromaseqUniversalMapper implements MesquiteListener {
	public static final int ACECONTIG = 0;   // this is the "Phred.Phrap.Mesquite" sequence line in the chromatogram viewer
	public static final int MATRIXSEQUENCE = 1;   // this is the edited in matrix sequence line in the chromatogram viewer
	public static final int EDITEDMATRIX = 2;    // this is the row in the editedMatrix in the actually editedData object, including gaps etc.
	static final int numMappings = 3;
	
	SequencePanel aceContigPanel;
	SequencePanel matrixSequencePanel;
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
	
//	int[] aceContigBaseFromUniversalBase;
//	int[] UniversalBaseFromAceContigBase;
	
	
	
	public ChromaseqUniversalMapper (ContigDisplay contigDisplay, MolecularData data){
		this.contigDisplay = contigDisplay;
		this.aceContigPanel = contigDisplay.getAceContigPanel();
		this.matrixSequencePanel = contigDisplay.getMatrixSeqPanel();
	
		this.contig = contigDisplay.getContig();
		it = contigDisplay.getTaxon().getNumber();
		editedData = contigDisplay.getEditedData();
		if (editedData!=null)
			editedData.addListener(this);
		registryData = ChromaseqUtil.getRegistryData(editedData);
		reverseRegistryData = ChromaseqUtil.getRegistryData(editedData);
		init();
	}
	
	/*.................................................................................................................*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData) {
			reset();
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
	
	public void createOtherBaseFromUniversalBase() {
		otherBaseFromUniversalBase = new int[numMappings][contigDisplay.getTotalNumOverallBases()];
	}
	public void createUniversalBaseFromOtherBase() {
		if (universalBaseFromOtherBase==null)
			universalBaseFromOtherBase = new int[numMappings][];
		if (universalBaseFromOtherBase[ACECONTIG]==null || universalBaseFromOtherBase[ACECONTIG].length!=aceContigPanel.getLength())
			universalBaseFromOtherBase[ACECONTIG] = new int[aceContigPanel.getLength()];
		if (universalBaseFromOtherBase[MATRIXSEQUENCE]==null || universalBaseFromOtherBase[MATRIXSEQUENCE].length!=matrixSequencePanel.getLength())
			universalBaseFromOtherBase[MATRIXSEQUENCE] = new int[matrixSequencePanel.getLength()];
		if (universalBaseFromOtherBase[EDITEDMATRIX]==null || universalBaseFromOtherBase[EDITEDMATRIX].length!=editedData.getNumChars())
			universalBaseFromOtherBase[EDITEDMATRIX] = new int[editedData.getNumChars()];
	}

	
	int resetCount = 0;
	/*.................................................................................................................*/
	public void reset() {
		Debugg.println("======= Resetting Universal Base Registry ======= " + (resetCount++));
		
		
		// =========== Calculate total number of universal bases ===========
		
		totalNumAddedBases=0;
		totalNumDeletedBases=0;
		for (int ic = 0; ic< editedData.getNumChars(); ic++){  // going through the sourceData object.  This is either the edited matrix or the original matrix
			int positionInOriginal = registryData.getState(ic, it);
			if (registryData!=null){
				if (registryData.isUnassigned(ic, it)) {  //this must be an added base
					totalNumAddedBases++;
				} else if (positionInOriginal>=0 && reverseRegistryData.isUnassigned(positionInOriginal,it)) {  // this must be a deleted base
					totalNumAddedBases++;
				}
			}
		}
		totalUniversalBases = contigDisplay.getTotalNumOverallBases()+totalNumAddedBases-totalNumDeletedBases;

		if (otherBaseFromUniversalBase==null || otherBaseFromUniversalBase[ACECONTIG].length!=totalUniversalBases)
			createOtherBaseFromUniversalBase();
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<otherBaseFromUniversalBase[mapping].length; i++) 
				otherBaseFromUniversalBase[mapping][i]=-1;
		
		createUniversalBaseFromOtherBase();
		for(int mapping=0; mapping<numMappings; mapping++) 
			for (int i=0; i<universalBaseFromOtherBase[mapping].length; i++) 
				universalBaseFromOtherBase[mapping][i]=-1;
		
		
		// =========== Calculate mappings for the ace contig panel (i.e., the "Phred.Phrap.Mesquite" one) ===========
		 
		SequenceCanvas sequenceCanvas = aceContigPanel.getCanvas();
		MesquiteSequence sequence = aceContigPanel.getSequence();
		if (sequenceCanvas!=null && sequence!=null)
			for (int i=0; i<sequence.getLength(); i++){
				int consensus = sequenceCanvas.getConsensusFromLocalIndex(i);
				int overallBase = contigDisplay.getOverallBaseFromConsensusBase(consensus);
				otherBaseFromUniversalBase[ACECONTIG][overallBase] = i;
				universalBaseFromOtherBase[ACECONTIG][i] = overallBase;
			}
		
		
		// =========== Calculate mappings for edited sequence panel ===========
		
		int numBasesFound = -1;
		int numOriginalBasesFound=-1;
		int firstBase = editedData.getNumChars();
		int lastIC=-1;
		int numAddedBases = 0;
		int numDeletedBases = 0;
		int startingUniversalBase = contigDisplay.getOverallBaseFromConsensusBase(contigDisplay.getNumBasesOriginallyTrimmedFromStartOfPhPhContig()-contigDisplay.getNumBasesAddedToStart());
		Debugg.println("startingUniversalBase3: " + startingUniversalBase);
		for (int ic = 0; ic< editedData.getNumChars(); ic++){  // going through the sourceData object.  This is either the edited matrix or the original matrix

			int positionInOriginal = registryData.getState(ic, it);
			if (registryData!=null){
				if (registryData.isUnassigned(ic, it)) {  //this must be an added base
					positionInOriginal=-1;
					numAddedBases++;
				} else if (positionInOriginal>=0 && reverseRegistryData.isUnassigned(positionInOriginal,it)) {  // this must be a deleted base
					numDeletedBases++;
				}
			}
			if (!editedData.isInapplicable(ic, it)){   // there is a state in the source matrix

				numBasesFound++;
				if (positionInOriginal<0) {  // but it wasn't in the original
				} else {
					numOriginalBasesFound++;
					if (ic < firstBase)
						firstBase = ic;
					
					int sequenceBase = numBasesFound;
					int matrixBase = ic;
					int universalBase = startingUniversalBase+numBasesFound; //numOriginalBasesFound+numAddedBases-numDeletedBases;


					if (sequenceBase>=0 && sequenceBase<universalBaseFromOtherBase[MATRIXSEQUENCE].length)
						universalBaseFromOtherBase[MATRIXSEQUENCE][sequenceBase] = universalBase;
					if (sequenceBase>=0 && sequenceBase<universalBaseFromOtherBase[EDITEDMATRIX].length)
						universalBaseFromOtherBase[EDITEDMATRIX][matrixBase] = universalBase;

					if (matrixBase>=0 && matrixBase<otherBaseFromUniversalBase[MATRIXSEQUENCE].length)
						otherBaseFromUniversalBase[MATRIXSEQUENCE][universalBase] = sequenceBase;
					if (matrixBase>=0 && matrixBase<otherBaseFromUniversalBase[MATRIXSEQUENCE].length)
						otherBaseFromUniversalBase[EDITEDMATRIX][universalBase] = matrixBase;

					
					if (ic>lastIC)
						lastIC = ic;

				}
			} 

		}
		
/*
	if (numBasesFound<0) { //all gaps in original
			for (int i=0; i<universalBaseFromOtherBase[MATRIXSEQUENCE].length; i++) {
				universalBaseFromOtherBase[MATRIXSEQUENCE][i] = i;
				otherBaseFromUniversalBase[MATRIXSEQUENCE][i] = i;
			}
		}
		else { //trailing bit go above numbers present
			for (int ic = 0; ic< firstBase; ic++){ //going from first original base to the right
				otherBaseFromUniversalBase[MATRIXSEQUENCE][ic] = ic-firstBase;

			}
			for (int i=numBasesFound+1; i<universalBaseFromOtherBase[MATRIXSEQUENCE].length; i++)
				universalBaseFromOtherBase[MATRIXSEQUENCE][i] = ++lastIC;
		}

		//filling in trailing bit in case matrix was added to
		int highestDefined = otherBaseFromUniversalBase[MATRIXSEQUENCE].length-1;
		for (highestDefined = otherBaseFromUniversalBase[MATRIXSEQUENCE].length-1; highestDefined>=0; highestDefined--){
			if (otherBaseFromUniversalBase[MATRIXSEQUENCE][highestDefined]>=0)
				break;
		}
		int max = -1;
		for (int ic = 0; ic<otherBaseFromUniversalBase[MATRIXSEQUENCE].length; ic++)
			if (MesquiteInteger.isCombinable(otherBaseFromUniversalBase[MATRIXSEQUENCE][ic]) && max < otherBaseFromUniversalBase[MATRIXSEQUENCE][ic])
				max = otherBaseFromUniversalBase[MATRIXSEQUENCE][ic];
		for (int ic = highestDefined+1; ic<otherBaseFromUniversalBase[MATRIXSEQUENCE].length; ic++)
			otherBaseFromUniversalBase[MATRIXSEQUENCE][ic] = ++max;
*/
		
		hasBeenSet = true;
	}
	/*.................................................................................................................*/

	public int getUniversalBaseFromOtherBase(int otherBaseSystem, int otherBase) {
		if (otherBase<0 || otherBase>=universalBaseFromOtherBase[otherBaseSystem].length)
			return 0;
		return universalBaseFromOtherBase[otherBaseSystem][otherBase];
	}
	/*.................................................................................................................*/

	public int getEditedMatrixBaseFromOtherBase(int otherBaseSystem, int otherBase) {
		if (otherBase<0 || otherBase>=universalBaseFromOtherBase[otherBaseSystem].length)
			return 0;
		int universalBase = universalBaseFromOtherBase[otherBaseSystem][otherBase];
		if (universalBase<0 || universalBase>=otherBaseFromUniversalBase[EDITEDMATRIX].length)
			return 0;
		return otherBaseFromUniversalBase[EDITEDMATRIX][universalBase];
	}
	/*.................................................................................................................*/

	public int getOtherBaseFromEditedMatrixBase(int otherBaseSystem, int matrixBase) {
		if (matrixBase<0 || matrixBase>=universalBaseFromOtherBase[EDITEDMATRIX].length)
			return 0;
		int universalBase = universalBaseFromOtherBase[EDITEDMATRIX][matrixBase];
		if (universalBase<0 || universalBase>=otherBaseFromUniversalBase[otherBaseSystem].length)
			return 0;
		return otherBaseFromUniversalBase[otherBaseSystem][universalBase];
	}
	/*.................................................................................................................*/

	
	public String toString() {
		StringBuffer b = new StringBuffer();
		int newTotalUniversalBases = contigDisplay.getTotalNumOverallBases();
		
		
		for (int displayBase=0; displayBase<newTotalUniversalBases; displayBase++){
			b.append(" "+otherBaseFromUniversalBase[ACECONTIG][displayBase]);
		}
		b.append("\n\n");
		for (int i=0; i<aceContigPanel.getSequence().getLength(); i++){
			b.append(" "+universalBaseFromOtherBase[ACECONTIG][i]);
		}
		return b.toString();
	}


}
