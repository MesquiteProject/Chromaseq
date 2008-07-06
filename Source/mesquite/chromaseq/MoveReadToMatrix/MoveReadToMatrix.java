/* Mesquite chromaseq source code.  Copyright 2005-2008 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.MoveReadToMatrix; import java.awt.event.KeyEvent;import java.util.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.cont.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class MoveReadToMatrix extends ChromInit {	//ContigDisplay contigPanel;	ChromatWindow chromWindow;	ChromatogramTool moveToMatrixTool = null;	MesquiteCursor downMoveToMatrixCursor = null;	MesquiteCursor upMoveToMatrixCursor = null;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		return true;	}	public void setWindow(MesquiteWindow w){		moveToMatrixTool = new ChromatogramTool(this, "moveToMatrix", getPath(),"moveToMatrix.gif", 4,2,"Move to Matrix", "Move To Matrix tool", MesquiteModule.makeCommand("moveToMatrix",  this) , null,null);		moveToMatrixTool.setWorksOnAllPanels(false);		moveToMatrixTool.setWorksOnChromatogramPanels(true);		moveToMatrixTool.setWorksOnlyOnSelection(true);		downMoveToMatrixCursor = new MesquiteCursor(this, "moveToMatrix", getPath(),"moveToMatrix.gif", 4,2);		upMoveToMatrixCursor = new MesquiteCursor(this, "moveToMatrix", getPath(),"moveToMatrixUp.gif", 4,2);		w.addTool(moveToMatrixTool);		if (w instanceof ChromatWindow){			//		contigPanel = (ContigDisplay)((ChromatWindow)w).getMainContigPanel();			chromWindow = (ChromatWindow)w;		}	}	/*.................................................................................................................*/	public void adjustIfPositionChanges () {		if (chromWindow!=null && moveToMatrixTool!=null){			if (chromWindow.getChromatogramsOnTop()) 				moveToMatrixTool.setStandardCursor(downMoveToMatrixCursor, "moveToMatrix.gif");			else				moveToMatrixTool.setStandardCursor(upMoveToMatrixCursor, "moveToMatrixUp.gif");			if (chromWindow.getCurrentTool() == moveToMatrixTool)				chromWindow.resetCursor();		}	}	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) {		if (checker.compare(this.getClass(), "Move sequence in selected sites of panel to matrix", "[site][true = chromatogram panel][whichPanel][modifiers]", commandName, "moveToMatrix")) {			MesquiteInteger pos = new MesquiteInteger(0);			int site= MesquiteInteger.fromFirstToken(arguments, pos);			int localSite= MesquiteInteger.fromString(arguments, pos);			String s = ParseUtil.getToken(arguments, pos);			int whichPanel= MesquiteInteger.fromString(arguments, pos);			int cx= MesquiteInteger.fromString(arguments, pos);			if (!MesquiteInteger.isCombinable(cx))				cx = 1;			ChromViewContext context = getContext(cx);			if ("true".equalsIgnoreCase(s)){ //chromatogram				selectedFromReadToMatrix(context, whichPanel);			}			else {				selectedFromSequencePanelToMatrix(context, whichPanel);			}		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	/** Returns whether consensus position ic is within read */	boolean isPositionInRead(int ic, Read read){		if (read ==null)			return false;		int readBase= read.getReadBaseFromConsensusBase(ic);		return (readBase>=0 && readBase<read.getBasesLength());	}	/** Returns the state in the read at consensus position ic */	long getReadStateAtConsensusPosition(int ic, Read read){		if (read ==null)			return DNAState.unassigned;		int readBase= read.getReadBaseFromConsensusBase(ic);//		char readChar = read.getPhdBaseChar(readBase);  		char readChar = read.getPolyBaseChar(readBase);  		return DNAState.fromCharStatic(readChar);	}	/** Returns whether consensus position ic is within read */	boolean isPositionInSequence(int ic, SequencePanel panel, String sequence){		if (sequence ==null)			return false;		int local = panel.getCanvas().getLocalIndexFromConsensus(ic);		if (local>=0 && local < sequence.length()){			return true;		}		return false;	}	long getSequenceStateAtConsensusPosition(int ic, SequencePanel panel, String sequence){		if (sequence ==null)			return DNAState.unassigned;		int local = panel.getCanvas().getLocalIndexFromConsensus(ic);		if (local>=0 && local < sequence.length()){			char s = sequence.charAt(local);			return DNAState.fromCharStatic(s);		}		return DNAState.unassigned;	}	/*--------------------------------------*/	private void selectedFromReadToMatrix(ChromViewContext context, int whichRead){		if (context == null)			return;		ContigDisplay contigPanel = context.contigPanel;		if (contigPanel == null)			return;		Taxon taxon = contigPanel.getTaxon();		//if needed expand matrix		//first, find min and max of selected		int it = taxon.getNumber();		int minSelected = -1;		int maxSelected = -1;		int selectOffset = 0;		boolean[] selected = new boolean[contigPanel.getTotalNumPeaks()];		int count = 0;		for (int i=0; i<selected.length; i++) {			selected[i]=contigPanel.getSelectedOverallBase(i);			if (selected[i])				count++;		}		for (int ic = 0; ic< contigPanel.getTotalNumPeaks(); ic++){			int consensusBase = contigPanel.getConsensusBaseFromOverallBase(ic);			if (selected[ic])				if (isPositionInRead(consensusBase, context.reads[whichRead])){ //selected, therefore move					if (minSelected == -1)						minSelected = consensusBase;					maxSelected = consensusBase;				}		}		/* to test, short circuit the above and use this just to expand the matrix on both sides*				maxSelected = contigPanel.getTotalNumPeaks();				minSelected = 0;				/**/		//second, find out if this is beyond matrix to see if need to expand matrix		int minSelectedInMatrix = contigPanel.getMatrixPositionOfDisplayPosition(minSelected, originalData); //this should return accurate -ve number or +ve number if byeond edge!		int maxSelectedInMatrix =  contigPanel.getMatrixPositionOfDisplayPosition(maxSelected, originalData);		boolean added = false;		int origNumChars = edited.getNumChars();		if (maxSelectedInMatrix>= origNumChars){ //need to extend at right			edited.addCharacters(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			edited.addInLinked(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			added = true;		}		if (minSelectedInMatrix<0){  //need to extend at left			edited.addCharacters(-1, -minSelectedInMatrix, false);			edited.addInLinked(-1, -minSelectedInMatrix, false);			added = true;			selectOffset = -minSelectedInMatrix;		}		context = getContext(contigPanel);		for (int ic = 0; ic< contigPanel.getTotalNumPeaks(); ic++){			if (selected[ic]){ //selected, therefore move				int consensusBase = contigPanel.getConsensusBaseFromOverallBase(ic);				int matrixPosition = contigPanel.getMatrixPositionOfDisplayPosition(consensusBase, originalData);				long state = getReadStateAtConsensusPosition(consensusBase, context.reads[whichRead]);				edited.setState(matrixPosition+selectOffset, it, state);  			}		}		if (added){			edited.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//originalData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//qualityData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			registryData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));		}		else {			edited.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));			registryData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));		}		contigPanel.repaintPanels();	}	/*--------------------------------------*/	private void selectedFromSequencePanelToMatrix(ChromViewContext context, int whichPanel){		if (context == null)			return;		ContigDisplay contigPanel = context.contigPanel;		if (contigPanel == null)			return;		Taxon taxon = contigPanel.getTaxon();		//if needed expand matrix		//first, find min and max of selected		int it = taxon.getNumber();		int minSelected = -1;		int maxSelected = -1;		int selectOffset = 0;		boolean[] selected = new boolean[contigPanel.getTotalNumPeaks()];		for (int i=0; i<selected.length; i++)			selected[i]=contigPanel.getSelectedOverallBase(i);		SequencePanel sequencePanel  = chromWindow.getSequencePanel(whichPanel);		//SequencePanel sequencePanel = panels[whichPanel];		String sequence = sequencePanel.getSequenceString();		for (int ic = 0; ic< contigPanel.getTotalNumPeaks(); ic++){			int consensusBase = contigPanel.getConsensusBaseFromOverallBase(ic);			if (selected[ic] && (isPositionInSequence(consensusBase, sequencePanel, sequence))){ //selected, therefore move				if (minSelected == -1)					minSelected = consensusBase;				maxSelected = consensusBase;			}		}		/* to test, short circuit the above and use this just to expand the matrix on both sides*				maxSelected = contigPanel.getTotalNumPeaks();				minSelected = 0;				/**/		//second, find out if this is beyond matrix to see if need to expand matrix		int minSelectedInMatrix = contigPanel.getMatrixPositionOfDisplayPosition(minSelected, originalData); //this should return accurate -ve number or +ve number if byeond edge!		int maxSelectedInMatrix =  contigPanel.getMatrixPositionOfDisplayPosition(maxSelected, originalData);		boolean added = false;		int origNumChars = edited.getNumChars();		if (maxSelectedInMatrix>= origNumChars){ //need to extend at right			edited.addCharacters(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			edited.addInLinked(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			added = true;		}		if (minSelectedInMatrix<0){  //need to extend at left			edited.addCharacters(-1, -minSelectedInMatrix, false);			edited.addInLinked(-1, -minSelectedInMatrix, false);			added = true;			selectOffset = -minSelectedInMatrix;		}		for (int ic = 0; ic< contigPanel.getTotalNumPeaks(); ic++){			if (selected[ic]){ //selected, therefore move				int consensusBase = contigPanel.getConsensusBaseFromOverallBase(ic);				int matrixPosition = contigPanel.getMatrixPositionOfDisplayPosition(consensusBase, originalData);				edited.setState(matrixPosition+selectOffset, it, getSequenceStateAtConsensusPosition(consensusBase, context.sequences[whichPanel], sequence));  			}		}		if (added){			edited.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//originalData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//qualityData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			registryData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));		}		else {			edited.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));			registryData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));		}		contigPanel.repaintPanels();	}	/*.................................................................................................................*/	public String getName() {		return "Move Read to Matrix";	}	/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Moves the selected portion of the read that was touched to the sequence in the matrix" ;	}}