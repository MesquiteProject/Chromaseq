/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.lib; import mesquite.categ.lib.*;import mesquite.cont.lib.*;import mesquite.lib.*;import mesquite.lib.characters.CharacterData;import mesquite.lists.lib.ListModule;public abstract class MatrixSequence implements MesquiteSequence, MesquiteListener { 	MolecularData edited;	MolecularData original;	ContinuousData quality;	MolecularData data;	Contig contig;	int it;	int[] originalPositions;	int[] sequencePositions;	/*.................................................................................................................*/	public  MatrixSequence (MolecularData edited, MolecularData original, ContinuousData quality, Contig contig,  int it){		this.edited = edited;		this.original = original;		this.quality = quality;		this.contig = contig;		this.it = it;	//	calculateOriginalPositions();		original.addListener(this);}	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/	public void changed(Object caller, Object obj, Notification notification, CommandRecord commandRec){		int code = Notification.getCode(notification);		int[] parameters = Notification.getParameters(notification);		if (obj instanceof CharacterData) {			calculateOriginalPositions();		}	}		public boolean sourceReadIsLowerQuality(int i, int smallConflictThreshold, MesquiteBoolean higherReadConflicts, int largeConflictThreshold, MesquiteBoolean muchHigherReadConflicts) {		return contig.sourceReadIsLowerQuality(i,smallConflictThreshold, higherReadConflicts, largeConflictThreshold, muchHigherReadConflicts);	}		/** passes which object was disposed*/	public void disposing(Object obj){	}	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/	public boolean okToDispose(Object obj, int queryUser){			return true;	}	public void dispose(){			if (original != null)					original.removeListener(this);	}	public int getTaxonNumber(){		return it;	}	public MolecularData getData(){		return data;	}	public MolecularData getOriginalData(){		return original;	}	public String getSequence(){		StringBuffer seq = new StringBuffer(data.getNumChars());		boolean firstHit = false;		for (int ic = 0; ic< data.getNumChars(); ic++){			int oIC = data.findInvertedPositionOfOriginalSite(ic, it);			if (!(original.isInapplicable(oIC, it) && data.isInapplicable(oIC, it))){				data.statesIntoStringBuffer(oIC, it, seq, true, true, true);				firstHit = true;			}			else if (!firstHit && !data.isInapplicable(oIC, it)){				data.statesIntoStringBuffer(oIC, it, seq, true, true, true);			}		}		calculateOriginalPositions();		return seq.toString();	}	public boolean isNucleotides(){		return data instanceof DNAData;	}		public String getName(){		return data.getName() + " (taxon " + data.getTaxa().getTaxonName(it) + ")";	}	public int getLength(){		int count = 0;		for (int ic = 0; ic< data.getNumChars(); ic++){			if (!data.isInapplicable(ic, it))  //doesn't need to find inverted because counting all				count++;		}		return count;	}	public int getQualityOfBase(int ic){  // using index of local sequence    		if (data == null)   			return 0;   		if (quality == null)    			return 100;   		return (int)(quality.getState(matrixBaseFromSequenceBase(ic), it, 0) + 0.01);	}	public int sequenceBaseFromMatrixBase(int iMatrix){		if (iMatrix<0)			return iMatrix + sequencePositions[0];		if (iMatrix>=sequencePositions.length)			return iMatrix;		return sequencePositions[iMatrix];		}	public int matrixBaseFromSequenceBase(int iSequence){		if (originalPositions == null || originalPositions.length<1)			return iSequence;		if (iSequence<0)			return iSequence + originalPositions[0];		if (iSequence>=originalPositions.length)			return iSequence ;		return originalPositions[iSequence];	}	public void dumpFirstPositions(){			String s = "";			for (int i=0; i<30; i++)				s += " " + originalPositions[i];			Debugg.println("originalPositions " + s);			s = "";			for (int i=0; i<30; i++)				s += " " + sequencePositions[i];			Debugg.println("sequencePositions " + s + "  this " + this);		//	Debugg.println(" sequenceBaseFromMatrixBase  " + sequenceBaseFromMatrixBase(4)  + " matrixBaseFromSequenceBase  " + matrixBaseFromSequenceBase(4));	}	public abstract boolean inapplicableInSourceMatrixIndex(int ic, int it);		public void calculateOriginalPositions(){		/*originalPositions[i] is the position on the original matrix of site i in this sequence; 		note it assumes sequence includes only sites corresponding to non-gaps in original, 		except for leading or trailing blocks.  The original may have had 		*/		if (originalPositions == null || originalPositions.length != data.getNumChars())			originalPositions = new int[data.getNumChars()];		for (int i=0; i<originalPositions.length; i++)				originalPositions[i] = -1;		//	sequencePositions[i] is the position in this sequence of site i in the original matrix, starting at firstBase of the original matrix (in case shifted right by gaps) 		if (sequencePositions == null || sequencePositions.length != original.getNumChars())			sequencePositions = new int[data.getNumChars()];		for (int i=0; i<sequencePositions.length; i++)			sequencePositions[i] = -1;				int count = -1;		int firstBase = data.getNumChars();		int lastIC = -1;		for (int ic = 0; ic< data.getNumChars(); ic++){			int oIC = data.findInvertedPositionOfOriginalSite(ic, it);						if (!inapplicableInSourceMatrixIndex(oIC, it)){				count++;				if (oIC < firstBase)						firstBase = oIC;				originalPositions[count] = oIC;				if (oIC>=0 && oIC<sequencePositions.length)					sequencePositions[oIC] = count;				if (oIC>lastIC)					lastIC = oIC;			}		}		if (count<0) { //all gaps in original			for (int i=0; i<originalPositions.length; i++) {				originalPositions[i] = i;				sequencePositions[i] = i;			}		}		else { //trailing bit go above numbers present			for (int ic = 0; ic< firstBase; ic++){ //going from first original base to the right				sequencePositions[ic] = ic-firstBase;							}			for (int i=count+1; i<originalPositions.length; i++)				originalPositions[i] = ++lastIC;		}				//filling in trailing bit in case matrix was added to		int highestDefined = sequencePositions.length-1;		for (highestDefined = sequencePositions.length-1; highestDefined>=0; highestDefined--){			if (sequencePositions[highestDefined]>=0)				break;		}		int max = -1;		for (int ic = 0; ic<sequencePositions.length; ic++)			if (max < sequencePositions[ic])				max = sequencePositions[ic];		for (int ic = highestDefined+1; ic<sequencePositions.length; ic++)			sequencePositions[ic] = ++max;;}		public void OLDcalculateOriginalPositions(){		/*originalPositions[i] is the position on the original matrix of site i in this sequence; 		note it assumes sequence includes only sites corresponding to non-gaps in original, 		except for leading or trailing blocks.  The original may have had 		*/		if (originalPositions == null || originalPositions.length != data.getNumChars())			originalPositions = new int[data.getNumChars()];		for (int i=0; i<originalPositions.length; i++)				originalPositions[i] = -1;		int count = -1;		int firstBase = -1;		int lastIC = -1;		for (int ic = 0; ic< data.getNumChars(); ic++){			if (!inapplicableInSourceMatrixIndex(ic, it)){				count++;				if (firstBase <0)						firstBase = ic;				originalPositions[count] = ic;				lastIC = ic;			}		}		if (count<0) { //all gaps in original			for (int i=0; i<originalPositions.length; i++)				originalPositions[i] = i;		}		else { //trailing bit go above numbers present			for (int i=count+1; i<originalPositions.length; i++)				originalPositions[i] = ++lastIC;		}		//	sequencePositions[i] is the position in this sequence of site i in the original matrix, starting at firstBase of the original matrix (in case shifted right by gaps) 		if (sequencePositions == null || sequencePositions.length != original.getNumChars())			sequencePositions = new int[data.getNumChars()];		for (int i=0; i<sequencePositions.length; i++)			sequencePositions[i] = -1;		count = -1;		for (int ic = 0; ic< firstBase; ic++){ //going from first original base to the right			sequencePositions[ic] = ic-firstBase;					}		for (int ic = firstBase; ic >= 0 && ic< data.getNumChars(); ic++){ //going from first original base to the right			if (!inapplicableInSourceMatrixIndex(ic, it))				count++;			sequencePositions[ic] = count;					}		if (count<0) { //all gaps in original			for (int i=0; i<sequencePositions.length; i++)				sequencePositions[i] = i;		}	}}