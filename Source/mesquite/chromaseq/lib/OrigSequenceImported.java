/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import mesquite.categ.lib.*;import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;import mesquite.cont.lib.*;import mesquite.lib.ColorDistribution;import mesquite.meristic.lib.MeristicData;import java.awt.*;public class OrigSequenceImported extends MatrixSequence { 	/*.................................................................................................................*/	public  OrigSequenceImported (ContigDisplay contigDisplay, MolecularData edited, MolecularData original, ContinuousData quality, Contig contig, int it){		super(contigDisplay, edited,  original,  quality, contig,  it);		sourceData = original;	}		public String getSequence(){		int 	it = getTaxonNumber();		StringBuffer seq = new StringBuffer(sourceData.getNumChars());		MolecularData originalData = ChromaseqUtil.getOriginalData(sourceData);				for (int ic = 0; ic< originalData.getNumChars(); ic++){			if (originalData.isValidAssignedState(ic, it)||originalData.isUnassigned(ic, it)){				((DNAData)originalData).statesIntoStringBuffer(ic, it, seq, true, true, true);			}		}		return seq.toString();	}	public Color getHighlightColor(int iSequence, int iConsensus){		return null;	}	public Color getQualityColorOfBase(int i){  // using index of local sequence 		int qual = getQualityOfBase(i); // using index of local sequence 		if (qual==0)			return ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.2);		else			return ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.5);	}	/*..........................*/	public  int universalMapperOtherBaseValue(){		return ChromaseqUniversalMapper.ORIGINALTRIMMEDSEQUENCE;	}}