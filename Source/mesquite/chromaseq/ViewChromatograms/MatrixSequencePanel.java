/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.ViewChromatograms; import java.awt.Color;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.chromaseq.lib.*;class MatrixSequencePanel extends SequencePanel {			public MatrixSequencePanel(ContigDisplay window, MatrixSequence sequence, int contigID) {		super(window, sequence, contigID);	}	public SequenceCanvas makeCanvas(){		return new MatrixSequenceCanvas(this,sequence, window, contigID);	}	}class MatrixSequenceCanvas extends SequenceCanvas { 	/*..........................*/	public MatrixSequenceCanvas(SequencePanel sequencePanel, MesquiteSequence sequence, ContigDisplay window, int contigID) {		super(sequencePanel,  sequence, window, contigID);		//setBackground(Color.green);		id = 2;  	}	/*--------------------------------------*/	public boolean getHasSpecialStandardBaseColors(){ 		return true; 	}   	 /*--------------------------------------*/   	public boolean getEditable(){    		return true; //override if not editable   	}	/*...............................................................................................................*/   	public String getSequenceExplanation (int consensusBase){   		String s = "";   		s += "This shows the sequence as it appears in the data matrix; you may alter base calls directly here.";   		return s;   	} 	 /*--------------------------------------*/   	public boolean getColorBaseBackground(){    		return true;    	}	NameReference trimmableNameRef = NameReference.getNameReference("trimmable");	public void enterState(int i, char k){		MolecularData data = ((MatrixSequence)sequence).getData();		if (data == null)			return;		int it =  ((MatrixSequence)sequence).getTaxonNumber();		int ic = ((MatrixSequence)sequence).matrixBaseFromSequenceBase(i);		data.setState(ic, it, k);		data.setCellObject(trimmableNameRef, ic, it, new MesquiteInteger(2));		data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED, new int[] {ic, it}));		ChromaseqUtil.fillAddedBaseData(data, ic, it);		ChromaseqUtil.resetNumAddedToStart(window, data,it);		repaint();	}	public String getName(){		return "Edited in Matrix";	}	public int matrixBaseFromSequenceBase(int i){		if (!MesquiteInteger.isCombinable(i))			return i;		return ((MatrixSequence)sequence).matrixBaseFromSequenceBase(i);	}	/*.................................................................................................................*/	public int getLocalIndexFromConsensus(int i) {		if (!MesquiteInteger.isCombinable(i))			return i;		int loc =  -1;		loc = window.getMatrixPositionOfConsensusBase(i);  // the position in the matrix		loc = ((EditedMatrixSequence)sequence).sequenceBaseFromMatrixBase(loc);		return loc;	}	/*.................................................................................................................*/	public int getConsensusFromLocalIndex(int i) {		if (!MesquiteInteger.isCombinable(i))			return i;		int loc = -1;		int matrixBase = ((EditedMatrixSequence)sequence).matrixBaseFromSequenceBase(i);		loc = window.getConsensusPositionOfMatrixPosition(matrixBase) ;//+ window.getNumPaddedBeforeEditedBase(i) ;		return loc;	}		public void selectLocalRange(int i, int j){		super.selectLocalRange(i, j);		if (i == j){			window.setExplanation("Ambiguity codes: R-AG, Y-CT, M-AC, W-AT, S-CG, Y-CT, K-GT, V-ACG, D-AGT, H-ACT, B-CGT, N-ACGT");		}	}}