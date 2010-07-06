/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.Version 0.980   July 2010Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.ViewChromatograms; import java.awt.Color;import mesquite.categ.lib.*;import mesquite.chromaseq.lib.*;import mesquite.lib.*;class OriginalUntrimmedSeqPanel extends SequencePanel {			public OriginalUntrimmedSeqPanel(ContigDisplay window, MesquiteSequence sequence, int contigID) {		super(window, sequence, contigID);	}	public SequenceCanvas makeCanvas(){		return new OriginalUntrimmedCanvas(this,sequence, window, contigID);	}	}class OriginalUntrimmedCanvas extends SequenceCanvas { 		/*..........................*/	public OriginalUntrimmedCanvas(SequencePanel sequencePanel, MesquiteSequence sequence, ContigDisplay window, int contigID) {		super(sequencePanel,  sequence, window, contigID);		setBackground(Color.white);		id = 0;  }		public String getName(){		return "Original Untrimmed";	}   	 /*--------------------------------------*/   	public boolean getEditable(){    		return false; //override if not editable   	}	/*..........................*/	public  int universalMapperOtherBaseValue(){		return ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE;	}	/*..........................*/	protected int reverseSequenceBase(int sequenceBase, int sequenceLength){		return sequenceBase;	} 	/*...............................................................................................................*/   	public String getSequenceExplanation (int consensusBase){   		String s = contigDisplay.getContigDisplayExplanation(consensusBase);   		s += "\nThis is the consensus sequence as originally produced by Phred, Phrap, and Mesquite";   		return s;   	} 	public int matrixBaseFromSequenceBase(int i){		if (!MesquiteInteger.isCombinable(i))			return i;		return contigDisplay.getUniversalMapper().getEditedMatrixBaseFromOtherBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, i);	}	/*.................................................................................................................*	public int getLocalIndexFromConsensus(int i) {		return i;	}	/*.................................................................................................................*	public int getConsensusFromLocalIndex(int i) {		return i;	}	/*.................................................................................................................*/}