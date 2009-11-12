/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import java.awt.Color;import mesquite.categ.lib.*;import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;import mesquite.cont.lib.*;import mesquite.lib.*;import mesquite.lib.characters.CharacterData;import mesquite.lists.lib.ListModule;import mesquite.meristic.lib.MeristicData;public abstract class MatrixSequence implements MesquiteSequence, MesquiteListener { 	MolecularData edited;	MolecularData original;	ContinuousData quality;	ChromaseqBaseMapper chromMapper;	MolecularData sourceData;	MeristicData registryData;	MeristicData reverseRegistryData;	ContigDisplay contigDisplay;	ChromaseqUniversalMapper universalMapper;	Contig contig;	//	int it;	Taxon taxon;	int[] positionInSourceMatrix;  // at position i, stores for sequence position i where that position exists in the source matrix	int[] positionInSequence;   // at position i, stores for matrix position i where that position exists in the sequence	int[] positionInSourceMatrixRev;	int[] positionInSequenceRev;	/*.................................................................................................................*/	public  MatrixSequence (ContigDisplay contigDisplay, MolecularData edited, MolecularData original, ContinuousData quality, Contig contig,  int it){		this.edited = edited;		registryData = ChromaseqUtil.getRegistryData(edited);		reverseRegistryData = ChromaseqUtil.getReverseRegistryData(edited);		this.original = original;		this.quality = quality;		this.contig = contig;		this.contigDisplay = contigDisplay;		if (contigDisplay!=null)			universalMapper = contigDisplay.getUniversalMapper();		//		this.it = it;		taxon = edited.getTaxa().getTaxon(it);		chromMapper = new ChromaseqBaseMapper(edited);		//	calculateOriginalPositions();		edited.addListener(this);	}	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/	public void changed(Object caller, Object obj, Notification notification){		int code = Notification.getCode(notification);		int[] parameters = Notification.getParameters(notification);		if (obj instanceof CharacterData) {			calculateMappingToSourceData();		} 	}	public boolean isEditedMatrix() {		return false;	}	public Color getStandardColorOfBase(int i){		return null;	}	public boolean sourceReadIsLowerQuality(int i, int smallConflictThreshold, MesquiteBoolean higherReadConflicts, int largeConflictThreshold, MesquiteBoolean muchHigherReadConflicts) {		return contig.sourceReadIsLowerQuality(i,smallConflictThreshold, higherReadConflicts, largeConflictThreshold, muchHigherReadConflicts);	}	/** passes which object was disposed*/	public void disposing(Object obj){	}	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/	public boolean okToDispose(Object obj, int queryUser){		return true;	}	public void dispose(){		if (original != null)			original.removeListener(this);	}	public int getTaxonNumber(){		return taxon.getNumber();	}	public MolecularData getData(){		return sourceData;	}	public MolecularData getOriginalData(){		return original;	}	/*.................................................................................................................*/	private boolean reversed() {		return false;	}	public String getSequence(){		int 	it = getTaxonNumber();		StringBuffer seq = new StringBuffer(sourceData.getNumChars());		boolean firstHit = false;		MolecularData originalData = ChromaseqUtil.getOriginalData(sourceData);			for (int ic = 0; ic< sourceData.getNumChars(); ic++){			//int oIC = sourceData.findInvertedPositionOfOriginalSite(ic, it);			int positionInOriginal = registryData.getState(ic, it);			int oIC=ic;			if (!sourceData.isInapplicable(oIC, it) || originalData.isValidAssignedState(positionInOriginal,it)){				sourceData.statesIntoStringBuffer(oIC, it, seq, true, true, true);			}		}		calculateMappingToSourceData();		return seq.toString();	}	public long getSite(int icTarget, MesquiteInteger currentMatrixIC, MesquiteInteger currentSequenceIC, MesquiteBoolean firstHit){		int	it = getTaxonNumber();		for (int ic = currentMatrixIC.getValue(); ic< sourceData.getNumChars(); ic++){			//int oIC = sourceData.findInvertedPositionOfOriginalSite(ic, it);			int oIC=ic;			if (!sourceData.isInapplicable(oIC, it)){				if (currentSequenceIC.getValue() == icTarget){					currentMatrixIC.setValue(ic);					return sourceData.getState(ic, it);				}				currentSequenceIC.increment();			}		}		return CategoricalState.unassigned;	}	public boolean isNucleotides(){		return sourceData instanceof DNAData;	}	public String getName(){		if (sourceData==null)			return null;		int it = getTaxonNumber();		return sourceData.getName() + " (taxon " + sourceData.getTaxa().getTaxonName(it) + ")";	}	public int getLength(){		int it = getTaxonNumber();		int count = 0;		for (int ic = 0; ic< sourceData.getNumChars(); ic++){			if (!sourceData.isInapplicable(ic, it))  //doesn't need to find inverted because counting all				count++;		}		return count;	}	public int getQualityOfBase(int ic){  // using index of local sequence 		int it = getTaxonNumber();		if (sourceData == null)			return 0;		if (quality == null) 			return 100;		return (int)(chromMapper.getQualityScore(matrixBaseFromSequenceBase(ic), it) + 0.01);	}	/*.................................................................................*/	public int matrixBaseFromSequenceBase(int iSequence){		if (positionInSourceMatrix.length<=0)			return 0;		if (iSequence<0)			return positionInSourceMatrix[0]+iSequence;		if (iSequence>=positionInSourceMatrix.length)			return positionInSourceMatrix[positionInSourceMatrix.length-1]+iSequence-positionInSourceMatrix.length+1;		return positionInSourceMatrix[iSequence];	}	/*.................................................................................*/	public int sequenceBaseFromMatrixBase(int iMatrix){   // iMatrix is base in the edited matrix.		if (!isEditedMatrix()) {			if (chromMapper!=null) {				int it = getTaxonNumber();				int originalBase = chromMapper.getOriginalPositionFromEditedMatrix(iMatrix, it);  				if (originalBase<0) {					return -1;				}				else if (originalBase>=original.getNumChars())					return original.getNumChars()-1;				else if (originalBase>=positionInSequence.length)					return positionInSequence.length-1; //return positionInSequence[positionInSequence.length-1];				else					return originalBase; //return positionInSequence[originalBase];			}		}		if (iMatrix<0)			return positionInSequence[0];		if (iMatrix>=positionInSequence.length)			return positionInSequence[positionInSequence.length-1];		return positionInSequence[iMatrix];	}	/*.................................................................................*/	public void dumpFirstPositions(){		String s = "";		for (int i=0; i<30; i++)			s += " " + positionInSourceMatrix[i];		MesquiteMessage.println("originalPositions " + s);		s = "";		for (int i=0; i<30; i++)			s += " " + positionInSequence[i];		MesquiteMessage.println("sequencePositions " + s + "  this " + this);	}	/*.................................................................................*/	public abstract boolean inapplicableInSourceMatrixIndex(int ic, int it);	/*.................................................................................................................*/	/*.................................................................................................................*/	public void calculateMappingToSourceData(){		int it = getTaxonNumber();		/*originalPositions[i] is the position on the original matrix of site i in this sequence; 		note it assumes sequence includes only sites Source to non-gaps in original, 		except for leading or trailing blocks.  The original may have had 		 */		if (positionInSourceMatrix == null || positionInSourceMatrix.length != sourceData.getNumChars())  // it doesn't need to be this big, but it can't be bigger than that			positionInSourceMatrix = new int[sourceData.getNumChars()];		for (int i=0; i<positionInSourceMatrix.length; i++)			positionInSourceMatrix[i] = -1;		//	sequencePositions[i] is the position in this sequence of site i in the original matrix, starting at firstBase of the original matrix (in case shifted right by gaps) 		if (positionInSequence == null || positionInSequence.length != sourceData.getNumChars())			positionInSequence = new int[sourceData.getNumChars()];		for (int i=0; i<positionInSequence.length; i++)			positionInSequence[i] = MesquiteInteger.unassigned; //-1;		if (universalMapper==null)			universalMapper = contigDisplay.getUniversalMapper();		if (universalMapper!=null) {			//Debugg.println(" ||||||| calculateMappingToSourceData ||||||");			if (!universalMapper.getHasBeenSet()|| true) {				universalMapper.reset();			}			//Debugg.println(" ||||||| calculateMappingToSourceData ||||||");			if (isEditedMatrix()) {				for (int matrixBase = 0; matrixBase<positionInSequence.length; matrixBase++) {					positionInSequence[matrixBase] = universalMapper.getOtherBaseFromEditedMatrixBase(ChromaseqUniversalMapper.EDITEDMATRIXSEQUENCE, matrixBase);				}				for (int sequenceBase = 0; sequenceBase<positionInSourceMatrix.length; sequenceBase++) {					positionInSourceMatrix[sequenceBase] = universalMapper.getEditedMatrixBaseFromOtherBase(ChromaseqUniversalMapper.EDITEDMATRIXSEQUENCE, sequenceBase);				}			}			else {				for (int matrixBase = 0; matrixBase<positionInSequence.length; matrixBase++) {					positionInSequence[matrixBase] = matrixBase;				}				for (int sequenceBase = 0; sequenceBase<positionInSourceMatrix.length; sequenceBase++) {					positionInSourceMatrix[sequenceBase] = sequenceBase;				}				//	if (this instanceof EditedMatrixSequence) 				//		Debugg.print(" " +positionInSourceMatrix[sequenceBase]);			}		}		/*		//		Debugg.println(getName());		//		Debugg.println("   calculateMappingToSourceData");				int numBasesFound = -1;		int numOriginalBasesFound = -1;		int firstBase = 0;		int lastIC = -1;		if (reversed()) {			firstBase = 0;			lastIC = sourceData.getNumChars()-1;			for (int ic = sourceData.getNumChars()-1; ic>=0; ic--){				int oIC = ic;				if (!sourceData.isInapplicable(oIC, it)){					numBasesFound++;					if (oIC >firstBase)						firstBase = oIC;					positionInSourceMatrix[numBasesFound] = oIC;					if (oIC>=0 && oIC<positionInSequence.length)						positionInSequence[oIC] = numBasesFound;  					if (oIC<lastIC)						lastIC = oIC;				}			}		}		else {			firstBase = sourceData.getNumChars();			lastIC=-1;			int numAddedBases = 0;			int numDeletedBases = 0;			for (int ic = 0; ic< sourceData.getNumChars(); ic++){  // going through the sourceData object.  This is either the edited matrix or the original matrix				int positionInOriginal = registryData.getState(ic, it);				if (registryData!=null){					if (registryData.isUnassigned(ic, it)) {  //this must be an added base						positionInOriginal=-1;						numAddedBases++;					} else if (positionInOriginal>=0 && reverseRegistryData.isUnassigned(positionInOriginal,it)) {  // this must be a deleted base						numDeletedBases++;					}				}				if (!sourceData.isInapplicable(ic, it)){   // there is a state in the source matrix					numBasesFound++;					if (positionInOriginal<0) {  // but it wasn't in the original					} else {						numOriginalBasesFound++;						if (ic < firstBase)							firstBase = ic;						int sequenceBase = numBasesFound-numAddedBases+numDeletedBases;						if (sequenceBase>=0 && sequenceBase<positionInSourceMatrix.length)							positionInSourceMatrix[sequenceBase] = ic;						int matrixBase = ic;						if (matrixBase>=0 && matrixBase<positionInSequence.length)							positionInSequence[matrixBase] = numBasesFound-numAddedBases+numDeletedBases;						if (ic>lastIC)							lastIC = ic;					}				} 			}		}		if (numBasesFound<0) { //all gaps in original			for (int i=0; i<positionInSourceMatrix.length; i++) {				positionInSourceMatrix[i] = i;				positionInSequence[i] = i;			}		}		else { //trailing bit go above numbers present			for (int ic = 0; ic< firstBase; ic++){ //going from first original base to the right				positionInSequence[ic] = ic-firstBase;			}			for (int i=numBasesFound+1; i<positionInSourceMatrix.length; i++)				positionInSourceMatrix[i] = ++lastIC;		}		//filling in trailing bit in case matrix was added to		int highestDefined = positionInSequence.length-1;		for (highestDefined = positionInSequence.length-1; highestDefined>=0; highestDefined--){			if (positionInSequence[highestDefined]>=0)				break;		}		int max = -1;		for (int ic = 0; ic<positionInSequence.length; ic++)			if (MesquiteInteger.isCombinable(positionInSequence[ic]) && max < positionInSequence[ic])				max = positionInSequence[ic];		for (int ic = highestDefined+1; ic<positionInSequence.length; ic++)			positionInSequence[ic] = ++max;*/		 	}}