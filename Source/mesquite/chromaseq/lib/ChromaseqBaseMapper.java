package mesquite.chromaseq.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;
import mesquite.meristic.lib.*;


/* should store numBasesAddedToStart in file
 */
 

/* This class manages the connections between an edited matrix to which chromatogram information is attached, 
 and the associated matrices that contain relevant information.  
 
 For more information, see ChromaseqUtil, which contains the methods to create the registries
 
 These are the systems that connect bases:
 
 	(1) the system of coordination established by Phrap, and that is stored in Contig and Read, that establishes links between the original Contig and the original Reads, with padding, etc.
 		The Contig stores in its bases the original, UNTRIMMED sequences, corresponding to those in the m.ace file.  This contig has already had all of the other
 			alterations on it that Mesquite performs (e.g., convert to lower case, ambiguity codes).  Contig also stores in trimmedBases the bases that remain after trimming.
 		It also stores the padding information for the untrimmed sequences.
 	
 	(2) the registration system, which connects the various matrices within Mesquite (original matrix, quality data, edited matrix)
 			The original matrix (originalData) contains the exact matrix as originally imported.  This is after Mesquite trimming etc. (convert to lower case, ambiguity codes).
 			The quality data contains the quality scores for the original data, and there is a one-to-one mapping between cells in originalData and cells in qualityData
 			The edited matrix contains the current, actively edited matrix.
 			The registration system that connects these cells in these matrices one to another is managed in ChromaseqUtil.  
 	
 	(3) the connection between the numbering in the original matrix/trimmed Contig numbering and what is called the Consensus base, which is the numbering in the Contig
 			that is padded and untrimmed.
 			ConsensusBase = base in originalSequence (i.e., originalData) + numBasesOriginallyTrimmedFromStartOfPhPhContig-numBasesAddedToStart;
 			numBasesAddedToStart is stored in Contig.
 			
 	(3) the connection between the original Consensus numbering and the number of the OverallBase, which is just like the Consensus numbering but it is offset, 
 		as there are some Reads that extend before the start of the original (untrimmed) Contig.  The Read that extends furthest to the left (furthest upstream) extends 
 		contig.getReadExcessAtStart() to the left, and thus, in OverallBase numbering, the first base of the (untrimmed) Contig is numbered getReadExcessAtStart().  The OverallBase numbering 
 		exactly matches the Contig/Consensus numbering, except that it is offset. 
 		
 	(4) The DisplayBase is very similar to the OverallBase, but may have additional bases inserted into it.   There thus needs to be a mapping between the two.  There also needs to be 
		mapping between one of these and the matrix registration system, presumably via either originalData or editedData. 
 	
 	
 
 * */
public class ChromaseqBaseMapper {
	CharacterData editedData;  // this is the active, edited (and editable) matrix
	ContinuousData qualityData;  // this matrix should be structured exactly as the originalData
	DNAData originalData;   // contains the exact matrix as originally imported.
	MeristicData registryData;  // this should be of the same size as the edited matrix.   It contains in cell ic, it the character number in originalData that that cell's original base call is stored 
											// (which, of course, is also the position in qualityData of the quality scores)
	MeristicData reverseRegistryData; // this should be of the same size as the originalData.   It contains in cell ic, it the character number in editedData that that cell's edited base  is stored 
	boolean valid = false;

	public ChromaseqBaseMapper (CharacterData data){
		this.editedData = data;   // this is the edited data; i.e., the real data matrix
		registryData = ChromaseqUtil.getRegistryData(data);
		reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
		qualityData = ChromaseqUtil.getQualityData(data);
		originalData = ChromaseqUtil.getOriginalData(data);
		if (registryData!=null && qualityData!=null && originalData!=null)
			valid=true;
	}

	public boolean isValid() {
		return valid;
	}

	
	public double getQualityScore(int ic, int it){  // ic is the position in the edited matrix
		if (!isValid())
			return MesquiteDouble.unassigned;
		int mapping = registryData.getState(ic, it);
		return qualityData.getState(mapping, it, 0);
	}

	public long getOriginalState(int ic, int it){ // ic is the position in the edited matrix
		if (!isValid())
			return MesquiteLong.unassigned;
		int mapping = registryData.getState(ic, it);
		return originalData.getState(mapping, it);
	}

	public boolean originalIsInapplicable(int ic, int it){ // ic is the position in the edited matrix
		if (!isValid())
			return true;
		int mapping = registryData.getState(ic, it);
		if (mapping==MesquiteInteger.unassigned) return true;
		return originalData.isInapplicable(mapping, it);
	}

	/** This returns the nearest position within the edited matrix that is referred to by a position in the originalData 
	 * TODO: consider reversed sequence!!! */
	public int findNearestEditMatrixPosition(int originalPos, int it){// originalPos is the position in originalData
		if (!isValid())
			return -1;
		int pos = MesquiteInteger.impossible;
		if (reverseRegistryData.isInapplicable(originalPos, it)) {   // the reverse registry says there is no corresponding element in the edited matrix, so look for it
			pos = reverseRegistryData.firstApplicable(it);
			if (pos>originalPos){ // we go up; i.e., the requested one is below the last entry
				int nearestApplicablePosInEdited = reverseRegistryData.getState(pos, it) ;
				return (nearestApplicablePosInEdited - (pos-originalPos)); 
			}
			else {
				pos = reverseRegistryData.lastApplicable(it);
				if (pos<originalPos) {// we go down; i.e., the requested one is above the last entry
					int nearestApplicablePosInEdited = reverseRegistryData.getState(pos, it) ;
					return (nearestApplicablePosInEdited + (originalPos-pos));
				}
				else {  //TODO:  we have applicable on either side of ours.  what do we do here?
					
				}
			} 
			
		} else
			return reverseRegistryData.getState(originalPos,it);
		return -1;
	}

	public int getEditedMatrixPositionFromOriginal(int originalPos, int it){
		if (!isValid())
			return -1;
		if (reverseRegistryData==null) {
			for(int ic=0; ic<registryData.getNumChars(); ic++) {
				int mapping = registryData.getState(ic, it);
				if (mapping==originalPos)
					return ic;
			}
		}
		else  if (originalPos<0) {  // it is outside the matrix; happens in MoveReadToMatrix
			int pos = reverseRegistryData.getState(0, it) ;
			if (pos<0 || MesquiteInteger.isInapplicable(pos) || !MesquiteInteger.isCombinable(pos))  // there is no reverse registry data for this one, so find the nearest one
				pos = findNearestEditMatrixPosition(0, it);
			return pos+originalPos;
		} 
		else  if (originalPos>=reverseRegistryData.getNumChars()) {  // it is outside the matrix; happens in MoveReadToMatrix
			int pos = reverseRegistryData.getState(reverseRegistryData.getNumChars()-1, it) ;
			if (pos<0 || MesquiteInteger.isInapplicable(pos) || !MesquiteInteger.isCombinable(pos))  // there is no reverse registry data for this one, so find the nearest one
				pos = findNearestEditMatrixPosition(reverseRegistryData.getNumChars()-1, it);
			return pos+(originalPos-reverseRegistryData.getNumChars()+1);
		} 
		else {
			int pos = reverseRegistryData.getState(originalPos, it) ;
			if (pos<0 || MesquiteInteger.isInapplicable(pos) || !MesquiteInteger.isCombinable(pos))  // there is no reverse registry data for this one, so find the nearest one
				pos = findNearestEditMatrixPosition(originalPos, it);
			return pos;
		}
		return -1;

	}
	public int getOriginalPositionFromEditedMatrix(int ic, int it){
		if (!isValid())
			return -1;
		return registryData.getState(ic, it);
	}



}
