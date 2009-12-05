/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.MoveReadToMatrix; import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class MoveReadToMatrix extends ChromInit {	//ContigDisplay contigPanel;	ChromatWindow chromWindow;	ChromatogramTool moveToMatrixTool = null;	MesquiteCursor downMoveToMatrixCursor = null;	MesquiteCursor upMoveToMatrixCursor = null;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		return true;	}	public void setWindow(MesquiteWindow w){		moveToMatrixTool = new ChromatogramTool(this, "moveToMatrix", getPath(),"moveToMatrix.gif", 4,2,"Move to Matrix", "Move To Matrix tool", MesquiteModule.makeCommand("moveToMatrix",  this) , null,null);		moveToMatrixTool.setWorksOnAllPanels(false);		moveToMatrixTool.setWorksOnChromatogramPanels(true);		moveToMatrixTool.setWorksOnlyOnSelection(true);		downMoveToMatrixCursor = new MesquiteCursor(this, "moveToMatrix", getPath(),"moveToMatrix.gif", 4,2);		upMoveToMatrixCursor = new MesquiteCursor(this, "moveToMatrix", getPath(),"moveToMatrixUp.gif", 4,2);		w.addTool(moveToMatrixTool);		if (w instanceof ChromatWindow){			//		contigPanel = (ContigDisplay)((ChromatWindow)w).getMainContigPanel();			chromWindow = (ChromatWindow)w;		}	}	/*.................................................................................................................*/	public void adjustIfPositionChanges () {		if (chromWindow!=null && moveToMatrixTool!=null){			if (chromWindow.getChromatogramsOnTop()) 				moveToMatrixTool.setStandardCursor(downMoveToMatrixCursor, "moveToMatrix.gif");			else				moveToMatrixTool.setStandardCursor(upMoveToMatrixCursor, "moveToMatrixUp.gif");			if (chromWindow.getCurrentTool() == moveToMatrixTool)				chromWindow.resetCursor();		}	}	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) {		if (checker.compare(this.getClass(), "Move sequence in selected sites of panel to matrix", "[site][true = chromatogram panel][whichPanel][modifiers]", commandName, "moveToMatrix")) {			MesquiteInteger pos = new MesquiteInteger(0);			int site= MesquiteInteger.fromFirstToken(arguments, pos);			int localSite= MesquiteInteger.fromString(arguments, pos);			String s = ParseUtil.getToken(arguments, pos);			int whichPanel= MesquiteInteger.fromString(arguments, pos);			int cx= MesquiteInteger.fromString(arguments, pos);			if (!MesquiteInteger.isCombinable(cx))				cx = 1;			ChromViewContext context = getContext(cx);			if ("true".equalsIgnoreCase(s)){ //chromatogram				selectedFromReadToMatrix(context, whichPanel);			}			else {				selectedFromSequencePanelToMatrix(context, whichPanel);			}		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	/** Returns whether consensus position ic is within read */	boolean isPositionInRead(int ic, Read read){		if (read ==null)			return false;		int readBase= read.getReadBaseFromContigBase(ic);		return (readBase>=0 && readBase<read.getBasesLength());	}	/** Returns the state in the read at consensus position ic */	long getReadStateAtConsensusPosition(int ic, Read read){		if (read ==null)			return DNAState.unassigned;		int readBase= read.getReadBaseFromContigBase(ic);		char readChar = read.getPhdBaseChar(readBase);  		if (readChar=='*')			return DNAState.unassigned;		//		char readChar = read.getPolyBaseChar(readBase);    can't do it this way as this is does not consider padding		return DNAState.fromCharStatic(readChar);	}	/** Returns whether consensus position ic is within read */	boolean isPositionInSequence(int ic, SequencePanel panel, String sequence){		if (sequence ==null)			return false;		int local = panel.getCanvas().getLocalIndexFromUniversalBase(ic);		if (local>=0 && local < sequence.length()){			return true;		}		return false;	}	long getSequenceStateAtUniversalBase(int ic, SequencePanel panel, String sequence){		if (sequence ==null)			return DNAState.unassigned;		int local = panel.getCanvas().getLocalIndexFromUniversalBase(ic);		if (local>=0 && local < sequence.length()){			char s = sequence.charAt(local);			return DNAState.fromCharStatic(s);		}		return DNAState.unassigned;	}	/*................................................................*/	private void selectedFromReadToMatrix(ChromViewContext context, int whichRead){		if (context == null)			return;		ContigDisplay contigDisplay = context.contigPanel;		if (contigDisplay == null)			return;		ChromaseqUniversalMapper universalMapper = contigDisplay.getUniversalMapper();		if (universalMapper==null)			return;		Taxon taxon = contigDisplay.getTaxon();		//if needed expand matrix		//first, find min and max of selected		int it = taxon.getNumber();		int minSelected = -1;		int maxSelected = -1;		int selectOffset = 0;		boolean[] selected = new boolean[universalMapper.getNumUniversalBases()];		int count = 0;		for (int i=0; i<selected.length; i++) {			selected[i]= contigDisplay.getSelectedUniversalBase(i);			if (selected[i])				count++;		}		boolean foundFirstBase = false;		for (int universalBase = 0; universalBase < universalMapper.getNumUniversalBases(); universalBase++) {			int contigBase = universalMapper.getOtherBaseFromUniversalBase(ChromaseqUniversalMapper.ACEFILECONTIG, universalBase);			if (selected[universalBase]){				if (isPositionInRead(contigBase, context.reads[whichRead])){ //selected, therefore move					if (minSelected == -1)						minSelected = universalBase;					maxSelected = universalBase;				}			} 		}		/* to test, short circuit the above and use this just to expand the matrix on both sides*				maxSelected = contigPanel.getTotalNumPeaks();				minSelected = 0;				/**/		//second, find out if this is beyond matrix to see if need to expand matrix		int minSelectedInMatrix = contigDisplay.getMatrixPositionOfUniversalBase(minSelected, originalData); //this should return accurate -ve number or +ve number if byeond edge!		int maxSelectedInMatrix =  contigDisplay.getMatrixPositionOfUniversalBase(maxSelected, originalData);//DAVID!: what if either of the above is undefined?		if (minSelectedInMatrix>maxSelectedInMatrix) {			int temp = maxSelectedInMatrix;			maxSelectedInMatrix = minSelectedInMatrix;			minSelectedInMatrix = temp;		}		/*		 * Debugg.println("|||||||||||||||||||||\n   minSelected: " + minSelected+", maxSelected: " + maxSelected);		Debugg.println("   minSelectedInMatrix: " + minSelectedInMatrix+", maxSelectedInMatrix: " + maxSelectedInMatrix);		Debugg.println("   numSelected: " + count);		 */		if (!contigDisplay.contigExists()) {			if (minSelectedInMatrix<0) 				minSelectedInMatrix = minSelected;			if (maxSelectedInMatrix<0) 				maxSelectedInMatrix = maxSelected;			if (maxSelectedInMatrix<minSelected) 				maxSelectedInMatrix = minSelected;			universalMapper.serialFill();		}		//Debugg.println("   minSelectedInMatrix: " + minSelectedInMatrix+", maxSelectedInMatrix: " + maxSelectedInMatrix);		//Debugg.println("minSelected: " + minSelected + ", maxSelected: " + maxSelected + ", minSelectedInMatrix: " + minSelectedInMatrix + ", maxSelectedInMatrix: " + maxSelectedInMatrix);		boolean added = false;		int origNumChars = editedData.getNumChars();		if (maxSelectedInMatrix>= origNumChars){ //need to extend at right			editedData.addCharacters(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			editedData.addInLinked(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			added = true;//			for (int ic = minSelectedInMatrix; ic < editedData.getNumChars(); ic++) //				ChromaseqUtil.specifyAsMovedBase(contigPanel, editedData,ic,it);		}		if (minSelectedInMatrix<0){  //need to extend at left			editedData.addCharacters(-1, -minSelectedInMatrix, false);			editedData.addInLinked(-1, -minSelectedInMatrix, false);			added = true;			selectOffset = -minSelectedInMatrix;//			for (int ic = 0; ic < maxSelectedInMatrix-minSelectedInMatrix; ic++) //				ChromaseqUtil.specifyAsMovedBase(contigPanel, editedData,ic,it);		} 		/*else			for (int ic = minSelectedInMatrix; ic <= maxSelectedInMatrix; ic++) 				ChromaseqUtil.specifyAsMovedBase(contigPanel, editedData,ic,it);*/		context = getContext(contigDisplay);		int firstBaseInEdited = editedData.firstApplicable(it);		int addedToStart = 0;		int basesAdded = 0;		boolean[] addedBase = new boolean[universalMapper.getNumUniversalBases()];		for (int i=0; i<addedBase.length; i++) {			addedBase[i]=false;		}		for (int universalBase = 0; universalBase< universalMapper.getNumUniversalBases(); universalBase++){			if (selected[universalBase]){ //selected, therefore move				int contigBase = contigDisplay.getContigBaseFromUniversalBase(universalBase);				if (!contigDisplay.getContig().getIsPadding(contigBase)) {					int matrixPosition = contigDisplay.getMatrixPositionOfUniversalBase(universalBase, originalData);					//if (matrixPosition<0) matrixPosition = contigBase;					long state = getReadStateAtConsensusPosition(contigBase, context.reads[whichRead]);					if (contigDisplay.isComplementedInEditedData())						state = DNAData.complement(state);					int icEdit = matrixPosition+selectOffset;					if (editedData.isInapplicable(icEdit, it)){ //then it is an added base												addedBase[universalBase] = true;						basesAdded ++;					}					//Debugg.println("   universalBase: " + universalBase+", contigBase: " + contigBase+", matrixPosition: " + matrixPosition+", icEdit: " + icEdit);					if (icEdit<firstBaseInEdited)						addedToStart++;					editedData.setState(icEdit, it, state);  				}			}		}		if (basesAdded>0){			contigDisplay.getContigMapper().recalc(it);		}		if (added){			editedData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			registryData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));		}		else {			editedData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));			registryData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));		}		// ======  now have to mark all the added bases as such; must do this after the listeners have been notified ======		if (basesAdded>0 && false) {//	Debugg.println(" basesAdded: " + basesAdded);			int universalBaseOfStartContig = universalMapper.getUniversalBaseFromOtherBase(ChromaseqUniversalMapper.ACEFILECONTIG, 0);			int universalBaseOfEndContig = universalMapper.getUniversalBaseFromOtherBase(ChromaseqUniversalMapper.ACEFILECONTIG, contigDisplay.getContig().getNumBases()-1);			for (int universalBase = 0; universalBase< universalMapper.getNumUniversalBases() && universalBase<addedBase.length; universalBase++){				if (addedBase[universalBase]){ 					int contigBase = contigDisplay.getContigBaseFromUniversalBase(universalBase);//					Debugg.println("*   universalBase: " + universalBase);//					Debugg.println("    contigBase: " + contigBase);					if (contigBase>=0) {  // let's only mark the ones that match contigs						ChromaseqUtil.specifyBaseAsAdded(contigDisplay, editedData, contigDisplay.getMatrixPositionOfUniversalBase(universalBase, originalData)+selectOffset, it, contigBase, -1);					} else if (universalBase <= universalBaseOfStartContig){ 						ChromaseqUtil.specifyBaseAsAdded(contigDisplay, editedData, contigDisplay.getMatrixPositionOfUniversalBase(universalBase, originalData)+selectOffset, it, contigBase, 0);					} else if (universalBase >= universalBaseOfEndContig){ 						ChromaseqUtil.specifyBaseAsAdded(contigDisplay, editedData, contigDisplay.getMatrixPositionOfUniversalBase(universalBase, originalData)+selectOffset, it, contigBase, contigDisplay.getContig().getNumBases()-1);					} else { 						ChromaseqUtil.specifyBaseAsAdded(contigDisplay, editedData, contigDisplay.getMatrixPositionOfUniversalBase(universalBase, originalData)+selectOffset, it, contigBase, -1);					}				}			}			//contigPanel.getContigMapper().recalc(it);		}		universalMapper.reset(false);		contigDisplay.repaintPanels();	}	/*................................................................*/	private void selectedFromSequencePanelToMatrix(ChromViewContext context, int whichPanel){		if (context == null)			return;		ContigDisplay contigPanel = context.contigPanel;		if (contigPanel == null)			return;		ChromaseqUniversalMapper universalMapper = contigPanel.getUniversalMapper();		if (universalMapper==null)			return;		Taxon taxon = contigPanel.getTaxon();		//if needed expand matrix		//first, find min and max of selected		int it = taxon.getNumber();		int minSelected = -1;		int maxSelected = -1;		int selectOffset = 0;		boolean[] selected = new boolean[universalMapper.getNumUniversalBases()];		for (int i=0; i<selected.length; i++)			selected[i]= contigPanel.getSelectedUniversalBase(i);		SequencePanel sequencePanel  = chromWindow.getSequencePanel(whichPanel);		//SequencePanel sequencePanel = panels[whichPanel];		String sequence = sequencePanel.getSequenceString();		boolean foundFirstBase = false;		for (int universalBase = 0; universalBase < universalMapper.getNumUniversalBases(); universalBase++) {			int editedMatrixBase = universalMapper.getOtherBaseFromUniversalBase(ChromaseqUniversalMapper.EDITEDMATRIX, universalBase);			int readBase = universalMapper.getOtherBaseFromUniversalBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, universalBase);			if (editedMatrixBase>=0)				foundFirstBase=true;		}		for (int ic = 0; ic< universalMapper.getNumUniversalBases(); ic++){			int consensusBase = contigPanel.getContigBaseFromUniversalBase(ic);			if (selected[ic] && (isPositionInSequence(ic, sequencePanel, sequence))){ //selected, therefore move				if (minSelected == -1)					minSelected = consensusBase;				maxSelected = consensusBase;			}		}		/* to test, short circuit the above and use this just to expand the matrix on both sides*				maxSelected = contigPanel.getTotalNumPeaks();				minSelected = 0;				/**/		//second, find out if this is beyond matrix to see if need to expand matrix		int minSelectedInMatrix = contigPanel.getMatrixPositionOfUniversalBase(minSelected, originalData); //this should return accurate -ve number or +ve number if byeond edge!		int maxSelectedInMatrix =  contigPanel.getMatrixPositionOfUniversalBase(maxSelected, originalData);		boolean added = false;		int origNumChars = editedData.getNumChars();		if (maxSelectedInMatrix>= origNumChars){ //need to extend at right			editedData.addCharacters(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			editedData.addInLinked(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			added = true;		}		if (minSelectedInMatrix<0){  //need to extend at left			editedData.addCharacters(-1, -minSelectedInMatrix, false);			editedData.addInLinked(-1, -minSelectedInMatrix, false);			added = true;			selectOffset = -minSelectedInMatrix;		}		int firstBaseInEdited = editedData.firstApplicable(it);		int addedToStart = 0;		for (int universalBase = 0; universalBase< contigPanel.getTotalNumUniversalBases(); universalBase++){			if (selected[universalBase]){ //selected, therefore move				int matrixPosition = contigPanel.getMatrixPositionOfUniversalBase(universalBase, originalData);				int icEdit = matrixPosition+selectOffset;				if (icEdit<firstBaseInEdited)					addedToStart++;				editedData.setState(icEdit, it, getSequenceStateAtUniversalBase(universalBase, context.sequences[whichPanel], sequence));  			}		}		//		ChromaseqUtil.fillAddedBaseData(contigPanel, editedData, it);		if (added){			editedData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//originalData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//qualityData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			registryData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));		}		else {			editedData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));			registryData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));		}		contigPanel.repaintPanels();	}	/*................................................................*	private void selectedFromSequencePanelToMatrix(ChromViewContext context, int whichPanel){		if (context == null)			return;		ContigDisplay contigPanel = context.contigPanel;		ChromaseqUniversalMapper universalMapper = contigPanel.getUniversalMapper();		if (contigPanel == null)			return;		Taxon taxon = contigPanel.getTaxon();		//if needed expand matrix		//first, find min and max of selected		int it = taxon.getNumber();		int minSelected = -1;		int maxSelected = -1;		int selectOffset = 0;		boolean[] selected = new boolean[contigPanel.getTotalNumOverallBases()];		for (int i=0; i<selected.length; i++)			selected[i]=contigPanel.getSelectedOverallBase(i);		SequencePanel sequencePanel  = chromWindow.getSequencePanel(whichPanel);		//SequencePanel sequencePanel = panels[whichPanel];		String sequence = sequencePanel.getSequenceString();		for (int ic = 0; ic< contigPanel.getTotalNumOverallBases(); ic++){			int consensusBase = contigPanel.getConsensusBaseFromOverallBase(ic);			if (selected[ic] && (isPositionInSequence(consensusBase, sequencePanel, sequence))){ //selected, therefore move				if (minSelected == -1)					minSelected = consensusBase;				maxSelected = consensusBase;			}		}		//second, find out if this is beyond matrix to see if need to expand matrix		int minSelectedInMatrix = contigPanel.getMatrixPositionOfConsensusBase(minSelected, originalData); //this should return accurate -ve number or +ve number if byeond edge!		int maxSelectedInMatrix =  contigPanel.getMatrixPositionOfConsensusBase(maxSelected, originalData);		boolean added = false;		int origNumChars = editedData.getNumChars();		if (maxSelectedInMatrix>= origNumChars){ //need to extend at right			editedData.addCharacters(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			editedData.addInLinked(origNumChars, maxSelectedInMatrix-origNumChars+1, false);			added = true;		}		if (minSelectedInMatrix<0){  //need to extend at left			editedData.addCharacters(-1, -minSelectedInMatrix, false);			editedData.addInLinked(-1, -minSelectedInMatrix, false);			added = true;			selectOffset = -minSelectedInMatrix;		}		int firstBaseInEdited = editedData.firstApplicable(it);		int addedToStart = 0;		for (int ic = 0; ic< contigPanel.getTotalNumOverallBases(); ic++){			if (selected[ic]){ //selected, therefore move				int consensusBase = contigPanel.getConsensusBaseFromOverallBase(ic);				int matrixPosition = contigPanel.getMatrixPositionOfConsensusBase(consensusBase, originalData);				int icEdit = matrixPosition+selectOffset;				if (icEdit<firstBaseInEdited)					addedToStart++;				editedData.setState(icEdit, it, getSequenceStateAtConsensusPosition(consensusBase, context.sequences[whichPanel], sequence));  			}		}		ChromaseqUtil.fillAddedBaseData(contigPanel, editedData, it);		if (added){			editedData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//originalData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			//qualityData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));			registryData.notifyListeners(this, new Notification(CharacterData.PARTS_ADDED));		}		else {			editedData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));			registryData.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));		}		contigPanel.repaintPanels();	}	/*.................................................................................................................*/	public String getName() {		return "Move Read to Matrix";	}	/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Moves the selected portion of the read that was touched to the sequence in the matrix" ;	}}