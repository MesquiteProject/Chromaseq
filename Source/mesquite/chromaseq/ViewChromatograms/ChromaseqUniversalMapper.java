package mesquite.chromaseq.ViewChromatograms;

import mesquite.categ.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.*;
import mesquite.molec.*;
import mesquite.chromaseq.lib.*;
import mesquite.meristic.lib.*;

public class ChromaseqUniversalMapper implements MesquiteListener {
	public static final int ACECONTIG = 0;
	public static final int MATRIXSEQUENCE = 1;
	static final int numMappings = 2;
	
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
	}


	/*.................................................................................................................*/
	public void reset() {
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
					int sequenceBase = numBasesFound-numAddedBases+numDeletedBases;
					if (sequenceBase>=0 && sequenceBase<universalBaseFromOtherBase[MATRIXSEQUENCE].length)
						universalBaseFromOtherBase[MATRIXSEQUENCE][sequenceBase] = ic;

					int matrixBase = ic;
					if (matrixBase>=0 && matrixBase<otherBaseFromUniversalBase[MATRIXSEQUENCE].length)
						otherBaseFromUniversalBase[MATRIXSEQUENCE][matrixBase] = numBasesFound-numAddedBases+numDeletedBases;

					if (ic>lastIC)
						lastIC = ic;

				}
			} 

		}

		

	}
	
	public int getUniversalBaseFromOtherBase(int otherBaseSystem, int otherBase) {
		if (otherBase<0 || otherBase>=universalBaseFromOtherBase[otherBaseSystem].length)
			return 0;
		return universalBaseFromOtherBase[otherBaseSystem][otherBase];
	}
	
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
