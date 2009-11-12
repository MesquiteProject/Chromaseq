/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.ViewChromatograms; import java.awt.*;import mesquite.categ.lib.*;import mesquite.chromaseq.lib.*;import mesquite.lib.*;class OrigImportedSeqPanel extends SequencePanel {			public OrigImportedSeqPanel(ContigDisplay window, MatrixSequence sequence, int contigID) {		super(window, sequence, contigID);	}	public SequenceCanvas makeCanvas(){		return new OrigImportedSeqCanvas(this,sequence, window, contigID);	}	}class OrigImportedSeqCanvas extends SequenceCanvas { 	/*..........................*/	public OrigImportedSeqCanvas(SequencePanel sequencePanel, MesquiteSequence sequence, ContigDisplay window, int contigID) {		super(sequencePanel,  sequence, window, contigID);		setBackground(Color.white);		id = 1;  	}	   	 /*--------------------------------------*/   	public boolean getEditable(){    		return false; //override if not editable   	}	/*..........................*/	public  int universalMapperOtherBaseValue(){		return ChromaseqUniversalMapper.ORIGINALIMPORTSEQUENCE;	}	/*...............................................................................................................*/   	public String getSequenceExplanation (int consensusBase){   		String s = "";   		s += "This is the consensus sequence as produced by Phred, Phrap, and Mesquite, and then trimmed and imported into Mesquite.";   		return s;   	} 	 /*--------------------------------------*/   	public boolean getColorBaseBackground(){    		return true;    	}		public String getName(){		return "Original Import";	}	public int matrixBaseFromSequenceBase(int i){		if (!MesquiteInteger.isCombinable(i))			return i;		return ((MatrixSequence)sequence).matrixBaseFromSequenceBase(i);	}	/*.................................................................................................................*/	public int getLocalIndexFromConsensus(int i) {		if (!MesquiteInteger.isCombinable(i))			return i;		return  ((MatrixSequence)sequence).sequenceBaseFromMatrixBase(contigDisplay.getMatrixPositionOfConsensusBase(i, (DNAData)((MatrixSequence)sequence).getOriginalData()));	}	/*.................................................................................................................*/	public int getConsensusFromLocalIndex(int i) {		if (!MesquiteInteger.isCombinable(i))			return i;		return contigDisplay.getConsensusPositionOfMatrixPosition(((MatrixSequence)sequence).matrixBaseFromSequenceBase(i), (DNAData)((MatrixSequence)sequence).getOriginalData());	}}