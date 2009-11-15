/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import mesquite.categ.lib.*;import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;import mesquite.cont.lib.*;import mesquite.lib.ColorDistribution;import java.awt.*;public class EditedMatrixSequence extends MatrixSequence { 	/*.................................................................................................................*/	public  EditedMatrixSequence (ContigDisplay contigDisplay, MolecularData edited, MolecularData original, ContinuousData quality, Contig contig, int it){		super(contigDisplay, edited, original, quality,contig,  it);		this.sourceData = edited;	}	public boolean isEditedMatrix() {		return true;	}		/*.................................................................................................................*/	protected boolean complemented() {		if (sourceData instanceof DNAData)			return ((DNAData)sourceData).isComplemented(it);		return false;	}	/*.................................................................................................................*/	protected boolean reversed() {		return  sourceData.isReversed(it);	}		public Color getHighlightColor(int iSequence, int iConsensus){			if (edited ==  null || original == null)			return null;		int it = getTaxonNumber();		int ic = matrixBaseFromSequenceBase(iSequence);		if (edited.getState(ic, it) != ChromaseqUtil.getOriginalStateForEditedMatrixBase(edited,ic, it))			return Color.black;		return null;	}	private int getFlag(int ic, int it){		return  ChromaseqUtil.getIntegerCellObject(edited, ChromaseqUtil.trimmableNameRef, ic, it);	}	public Color getStandardColorOfBase(int i){		if (edited ==  null || original == null)			return null;		int ic = matrixBaseFromSequenceBase(i);		int it = getTaxonNumber();		int color = getFlag(ic, it);		if (color == 1) //trimmable		return AceFile.colorTrimmable.getColor();		else if (color == 2) //touched			return AceFile.colorTouched.getColor();		else if (color == 3) //please check			return AceFile.colorPleaseRecheck.getColor();		if (CategoricalState.hasMultipleStates(edited.getState(ic, it)))			return Color.lightGray;		return null;	}	public Color getQualityColorOfBase(int i){		if (edited ==  null || original == null)			return null;		Color color = getStandardColorOfBase(i);		if (color!= null)			return color;		int qual = getQualityOfBase(i); // using index of local sequence 		if (qual==0)			return ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.2);		else			return ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.5);	}		/*..........................*/	public  int universalMapperOtherBaseValue(){		return ChromaseqUniversalMapper.EDITEDMATRIXSEQUENCE;	}}