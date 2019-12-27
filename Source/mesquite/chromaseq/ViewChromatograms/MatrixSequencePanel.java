/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.ViewChromatograms; import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.table.MesquiteTable;import java.awt.Graphics;import mesquite.categ.lib.*;import mesquite.chromaseq.lib.*;class MatrixSequencePanel extends SequencePanel {	public MatrixSequencePanel(ContigDisplay window, MatrixSequence sequence, int contigID) {		super(window, sequence, contigID);	}	public SequenceCanvas makeCanvas(){		return new MatrixSequenceCanvas(this,sequence, window, contigID);	}}class MatrixSequenceCanvas extends SequenceCanvas { 	/*..........................*/	public MatrixSequenceCanvas(SequencePanel sequencePanel, MesquiteSequence sequence, ContigDisplay window, int contigID) {		super(sequencePanel,  sequence, window, contigID);		//setBackground(Color.green);		id = 2;  	}	/*--------------------------------------*/	public boolean getHasSpecialStandardBaseColors(){ 		return true; 	}	/*--------------------------------------*/	public boolean getEditable(){ 		return true; //override if not editable	}	/*...............................................................................................................*/	public String getSequenceExplanation (int consensusBase){		checkForMapper();		String s = "";		if (universalMapper!=null) {			int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(),consensusBase);			s += "Base "+ (sequenceBase+1) +". ";			int matrixBase = universalMapper.getEditedMatrixBaseFromUniversalBase(consensusBase);			double quality = ChromaseqUtil.getQualityScoreForEditedMatrixBase(((MatrixSequence)sequence).getData(), matrixBase, it);			if (MesquiteDouble.isCombinable(quality)) {				s += " Quality score: "+ quality+".  ";			}		}		s += "This shows the sequence as it appears in the data matrix; you may alter base calls directly here.";		return s;	}	/*--------------------------------------*/	public boolean getColorBaseBackground(){ 		return true; 	}	/*	CharacterState csBefore = data.getCharacterState(null, column,row);	parser.setString(s);	MesquiteString result = new MesquiteString("");	int response = data.setState(column, row, parser, true, result); //receive errors?	if (response == CharacterData.OK){		CharacterState csAfter = data.getCharacterState(null, column,row);		if (csBefore !=null && !csBefore.equals(csAfter) && !notifySuppressed) {			UndoInstructions undoInstructions = window.setUndoInstructions(UndoInstructions.SINGLEDATACELL, column, row, csBefore, csAfter);			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, new int[] {column, row}, new UndoReference(undoInstructions, window.ownerModule)));			doAutosize = true;		}	}	 */	int counterRepaint = 0;	int counterPaint = 0;	long lastRepaint = 0;	public void repaint(){ //Debugg.println		if (MesquiteTrunk.debugMode) {			Debugg.printStackTrace("REPAINT " + counterRepaint++ );			lastRepaint = System.currentTimeMillis();		}		super.repaint();	}	public void paint(Graphics g) {			if (MesquiteTrunk.debugMode) {			Debugg.printStackTrace("PAINT " + counterPaint++ + "  " + (System.currentTimeMillis()-lastRepaint)/1000.0);		}		super.paint(g);	}	MesquiteTimer[] timers = {new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),new MesquiteTimer(),			new MesquiteTimer(),new MesquiteTimer()};	public void enterState(int i, char k){		MolecularData data = ((MatrixSequence)sequence).getData();		if (data == null)			return;		int it =  ((MatrixSequence)sequence).getTaxonNumber();		int ic = contigDisplay.getUniversalMapper().getEditedMatrixBaseFromOtherBase(ChromaseqUniversalMapper.EDITEDMATRIXSEQUENCE, i);		//boolean wasBase = data.isValidAssignedState(ic,it);		if (contigDisplay.isComplementedInEditedData())			k = DNAData.complementChar(k);		if (contigDisplay.isShownComplemented())			k = DNAData.complementChar(k);		long s = DNAState.fromCharStatic(k);		if (!DNAState.isImpossible(s)) {			CategoricalState csBefore = (CategoricalState)data.getCharacterState(null, ic,it);			ChromaseqUtil.setStateOfMatrixBase(contigDisplay,data,ic,it,s, true);			CategoricalState csAfter = (CategoricalState)data.getCharacterState(null, ic,it);			ChromaseqUtil.setIntegerCellObject(data,ChromaseqUtil.chromaseqCellFlagsNameRef, ic, it, new MesquiteInteger(ChromaseqUtil.MANUALLYCHANGED));			//		ChromaseqUtil.fillAddedBaseData(data, ic, it);			//		ChromaseqUtil.resetNumAddedToStart(contigDisplay, data,it);			//data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED, new int[] {ic, it}));			if (csBefore !=null && !csBefore.equals(csAfter)) {				int[] subcodes = new int[] {MesquiteListener.SINGLE_CELL};				if (csBefore.isInapplicable()==csAfter.isInapplicable())					subcodes = new int[] {MesquiteListener.SINGLE_CELL, MesquiteListener.CELL_SUBSTITUTION};				timers[0].start();				MesquiteTable table = contigDisplay.getTable();				if (table!=null)					table.deselectAll();				timers[0].end();				timers[1].start();				UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.SINGLEDATACELL, ic, it, csBefore, csAfter,data,contigDisplay.getTable());				Notification notification = new Notification(MesquiteListener.DATA_CHANGED, new int[] {ic,it}, new UndoReference(undoInstructions, contigDisplay.getModule()));				notification.setSubcodes(new int[]{CategoricalState.compressToShort(csBefore.getValue()), CategoricalState.compressToShort(csAfter.getValue())});				MesquiteWindow window = sequencePanel.window.getWindow();				UndoReference undoReference = Notification.getUndoReference(notification);				window.setUndoer(undoReference);				notification.setSubcodes(subcodes);				timers[1].end();				timers[2].start();				//data.setDumpNotified(true);				data.notifyListeners(this, notification);				timers[2].end();				//data.setDumpNotified(false);			}						repaint();		}	}	public void clearManuallyChanged(int i) {		MolecularData data = ((MatrixSequence)sequence).getData();		int it =  ((MatrixSequence)sequence).getTaxonNumber();		int ic = contigDisplay.getUniversalMapper().getEditedMatrixBaseFromOtherBase(ChromaseqUniversalMapper.EDITEDMATRIXSEQUENCE, i);		ChromaseqUtil.setIntegerCellObject(data,ChromaseqUtil.chromaseqCellFlagsNameRef, ic, it, new MesquiteInteger(ChromaseqUtil.NORMAL));	}	public String getName(){		return "Edited in Matrix";	}	public int matrixBaseFromSequenceBase(int i){		if (!MesquiteInteger.isCombinable(i))			return i;		return ((MatrixSequence)sequence).matrixBaseFromSequenceBase(i);	}	/*.................................................................................................................*/	public  int universalMapperOtherBaseValue(){		return ChromaseqUniversalMapper.EDITEDMATRIXSEQUENCE;	}	/*.................................................................................................................*	public int getLocalIndexFromConsensus(int i) {		if (!MesquiteInteger.isCombinable(i))			return i;		int loc =  -1;		loc = contigDisplay.getMatrixPositionOfUniversalBase(i) - contigDisplay.numPadsInTrimmedRegionAtStart(((MatrixSequence)sequence).getTaxonNumber());//+ window.getNumPaddedBeforeEditedBase(i) ;  // the position in the matrix		loc = ((EditedMatrixSequence)sequence).sequenceBaseFromMatrixBase(loc);//+window.getNumPaddedBeforeEditedBase(i);		return loc;	}	/*.................................................................................................................*	public int getConsensusFromLocalIndex(int i) {		if (!MesquiteInteger.isCombinable(i))			return i;		int loc = -1;	//	i=i-window.getNumPaddedBeforeEditedBase(i)-window.numPadsInTrimmedRegionAtStart(((MatrixSequence)sequence).getTaxonNumber());		int matrixBase = ((EditedMatrixSequence)sequence).matrixBaseFromSequenceBase(i);		loc = contigDisplay.getConsensusPositionOfMatrixPosition(matrixBase)+contigDisplay.numPadsInTrimmedRegionAtStart(((MatrixSequence)sequence).getTaxonNumber());		//loc = loc + window.getNumPaddedBeforeEditedBase(loc);		return loc;	}	/*.................................................................................................................*/	public void selectUniversalRange(int i, int j){		super.selectUniversalRange(i, j);		if (i == j){			contigDisplay.setExplanation("Ambiguity codes: R-AG, Y-CT, M-AC, W-AT, S-CG, Y-CT, K-GT, V-ACG, D-AGT, H-ACT, B-CGT, N-ACGT");		}	}}