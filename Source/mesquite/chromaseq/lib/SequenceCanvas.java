/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import java.awt.*;import java.awt.event.*;import mesquite.lib.*;import mesquite.lib.table.MesquiteTable;import mesquite.categ.lib.*;import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;public abstract class SequenceCanvas extends MousePanel implements KeyListener, FocusListener { 	protected MesquiteSequence sequence;	//	int[] A,C,G,T;	protected int maxValue;	protected int centerBase, centerPixel, centerWindowPixel;	protected int centerUniversalBase, centerUniversalPixel;	protected int firstUniversalBase, lastUniversalBase;	protected ChromaseqUniversalMapper universalMapper;	protected int it;	protected boolean colorByQuality = true;	//	int startOffset=0;	protected ContigDisplay contigDisplay;	protected boolean[] selected;  // uses local read as index	protected SequencePanel sequencePanel;	protected int id = -1;  	int contigID;	/*..........................*/	public SequenceCanvas(SequencePanel sequencePanel, MesquiteSequence sequence, ContigDisplay contigDisplay, int contigID) {		super();		this.sequence = sequence;		this.contigID = contigID;		setBackground(sequencePanel.getBackground());		this.sequencePanel = sequencePanel;		this.contigDisplay = contigDisplay;		String sequenceString = sequence.getSequence();		selected = new boolean[sequenceString.length()];   		for (int i=0;i<selected.length;i++) {			selected[i] = false;		}		it = contigDisplay.getTaxon().getNumber();		//		startOffset = window.getContig().getReadExcessAtStart();		reCalcCenterBase();		addKeyListener(this);   // we need to add these listeners for all SequenceCanvases, not just the editable ones, so that they can pick up arrow keys and the like 		addFocusListener(this); 		requestFocusInWindow();		if (contigDisplay.isReversedInEditedData())			setBackground(ColorDistribution.veryPaleGoldenRod);	}	public boolean isShownReversed(){		return contigDisplay.isShownReversed();	}	public boolean isReversedInEditedData(){		if (contigDisplay.isReversedInEditedData())			setBackground(ColorDistribution.veryPaleGoldenRod);		return contigDisplay.getEditedData().isReversed(it);	}	public boolean isComplementedInEditedData(){		return contigDisplay.getEditedData().isComplemented(it);	}	public boolean isShownComplemented(){		return contigDisplay.isShownComplemented();	}//	public abstract int getConsensusFromLocalIndex(int i);	public  int getLocalIndexFromUniversalBase(int i){		if (universalMapper == null)			return -1;		return universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(), i);	}	public abstract int matrixBaseFromSequenceBase(int i);	public MesquiteSequence getSequence() {		return sequence;	}	/*	public int getMatrixFromLocalIndex(int i){		return i;	}	/* Returns the number of padded bases that are missing from this sequence (i.e. are in the consensus but not in this sequence) in front of the given consensus position */	//	public abstract  int paddedMissingBeforeConsensus(int ic);	public boolean getEditable(){ 		return true; //override if not editable	}	boolean hasFocus = false; 	public void focusLost(FocusEvent e){		hasFocus = false;		repaint();	}	public void focusGained(FocusEvent e){		hasFocus = true;		repaint();	}	public void keyReleased(KeyEvent e){  	}	public void keyTyped(KeyEvent e){		if (!getEditable()){			contigDisplay.keyTyped(e);			return;		}		int mod = MesquiteEvent.getModifiers(e);		if (MesquiteEvent.optionKeyDown(mod) || MesquiteEvent.commandOrControlKeyDown(mod)){			contigDisplay.keyTyped(e);			return;		}		int sel = oneSelected();		char k = e.getKeyChar();		if (hasFocus && sel>=0) {			enterState(sel, k);		}		else 			contigDisplay.keyTyped(e);	}	public void keyPressed(KeyEvent e){		contigDisplay.keyPressed(e);  //pass this off to the window so that it can deal with arrow keys, etc.	}	public void enterState(int ic, char k){	}	/*.................................................................................................................*	public int nextLowQuality(boolean right, int threshold) {		reCalcCenterBase();		for (int i=centerBase+1;i < sequence.getLength();i++) {			if (i>=0) {				int qual = sequence.getQualityOfBase(i); // using index of local sequence 				if (qual<threshold)					return getConsensusFromLocalIndex(i);			}		}		return -1;	}	/*.................................................................................................................*	public void goToNextLowQuality(boolean right) {		int nextLow = nextLowQuality(right, 30);		if (nextLow>=0 && nextLow<sequence.getLength())			window.scrollToConsensusBase(nextLow);	}	/*.................................................................................................................*/	public void setColorByQuality(boolean colorByQuality) {		this.colorByQuality = colorByQuality;	}	/*.................................................................................................................*/	public int oneSelected(){		int sel = -1;		if (hasFocus){			int count = 0;			for (int i=0; i<selected.length; i++) {				if (selected[i]) {					count++;					sel = i;				}			}			if (count!=1)				sel = -1;		}		return sel;	}	public abstract String getName();	/*...........................................................................*/	protected int getBaseCenterPixel(char c, Graphics g){		return getFontMetrics(g.getFont()).stringWidth(""+c) / 2;	}	/*...........................................................................*/	protected void reCalcCenterBase(){		centerBase = sequencePanel.centerBase;   //number of centered base		centerPixel = contigDisplay.getCenterPixelFromCenterBase(centerBase);   //number of pixels over this scrolled base is		centerWindowPixel = getBounds().width/2;		int halfPeaks = contigDisplay.getApproximateNumberOfPeaksVisible()/2;		firstUniversalBase = contigDisplay.getHorizontalScrollBarValue() - halfPeaks;		lastUniversalBase = contigDisplay.getHorizontalScrollBarValue() + halfPeaks;		centerUniversalBase =firstUniversalBase+ (lastUniversalBase-firstUniversalBase)/2;   //number of centered base	}	long countFLP = 0;	/*..........................*/	public ChromaseqUniversalMapper getUniversalMapper() {		checkForMapper();		return universalMapper;	}	/*..........................*/	protected boolean checkForMapper() {		if (universalMapper!=null)			return true;		if (contigDisplay!=null) {			universalMapper =contigDisplay.getUniversalMapper();			if (universalMapper !=null)				return true;		}		return false;	}	/*..........................*/	private int findSequenceBase(int x, MesquiteInteger universalPos, boolean inBetween) {			long startTime = System.currentTimeMillis();		if (!checkForMapper())			return -1;		int cwidth = getBounds().width;		if (isShownReversed())			x = cwidth-x;		reCalcCenterBase();		int length = sequence.getLength();		if (universalPos != null)			universalPos.setValue(MesquiteInteger.unassigned);		countFLP++;		for (int i=firstUniversalBase;i < lastUniversalBase;i++) {			if (i>=0) {				int pixels = getPixelPositionOfUniversalBase(i);				int halfSpace = (int)(contigDisplay.getAveragePeakDistance()/2.0);				int fullSpace = (int)(contigDisplay.getAveragePeakDistance());				int pixelsNextCons = getPixelPositionOfUniversalBase(i+1);				if (!inBetween){					if (x < pixels+halfSpace){						if (universalPos != null)							universalPos.setValue(i);						if (System.currentTimeMillis()-startTime > 2000)  //here to debugg slow touches							Debugg.printStackTrace("findLocalPosition0 s " + x + " inBetween " + inBetween + " length " + length);						return universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(),i);					}					else if (x< pixelsNextCons-halfSpace){ //within region of inserted bases; find which one and return						int insert = 0;						while (x >= pixels+halfSpace){							insert++;							pixels += fullSpace;						}						if (universalPos != null)							universalPos.setValue(MesquiteInteger.unassigned);						if (System.currentTimeMillis()-startTime > 2000) //here to debugg slow touches							Debugg.printStackTrace("findLocalPosition1 s " + x + " inBetween " + inBetween + " length " + length);						return universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(),insert+i);					}				}				else {					if (x < pixels){						if (universalPos != null)							universalPos.setValue(i);						if (System.currentTimeMillis()-startTime > 2000) //here to debugg slow touches							Debugg.printStackTrace("findLocalPosition2 s " + x + " inBetween " + inBetween + " length " + length);						return universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(),i);					}					else if (x< pixelsNextCons){ //within region of inserted bases						int insert = 0;						while (x >= pixels){							insert++;							pixels += fullSpace;						}						if (universalPos != null)							universalPos.setValue(MesquiteInteger.unassigned);						if (System.currentTimeMillis()-startTime > 2000) //here to debugg slow touches							Debugg.printStackTrace("findLocalPosition3 s " + x + " inBetween " + inBetween + " length " + length);						return universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(),insert+i);					}				}			}		}		if (System.currentTimeMillis()-startTime > 2000) //here to debugg slow touches			Debugg.printStackTrace("findLocalPosition4 s " + x + " inBetween " + inBetween + " length " + length);		return MesquiteInteger.unassigned;	}	/*..........................*/	int topOfBase = 15;	int baseHeight = 16;	int notesHeight = 12;	private void fillRect(Graphics g, int width, int x, int y, int w, int h){		if (isShownReversed()){			g.fillRect(width-(x+w), y, w, h);		}		else {			g.fillRect(x, y, w, h);		}	}	private void drawRect(Graphics g, int width, int x, int y, int w, int h){		if (isShownReversed()){			g.drawRect(width-(x+w), y, w, h);		}		else {			g.drawRect(x, y, w, h);		}	}	private void drawLine(Graphics g, int width, int x, int y, int x2, int y2){		if (isShownReversed()){			g.drawLine(width-x, y, width-x2, y2);		}		else {			g.drawLine(x, y, x2, y2);		}	}	private void drawString(Graphics g, int width, String s, int x, int y){		if (isShownReversed()){			int sw = StringUtil.getStringDrawLength(g, s);			g.drawString(s, width-(x+sw), y);		}		else {			g.drawString(s, x, y);		}	}	/*..........................*/	public void paint(Graphics g) {			paint(g, false, null);	}	int count = 0;  //used for debugging	/*..........................*/	boolean continuingCondition(int i, String sequenceString, int firstSequenceBase, int  lastSequenceBase) {		return i < sequenceString.length() && (i< lastSequenceBase || lastSequenceBase<firstSequenceBase);  	}	/*..........................*/	int firstBase(int firstSequenceBase) {		return firstSequenceBase;	}	int counter = 0;	/*..........................*/	protected int getPixelPositionOfUniversalBase(int universalBase) {		return 	centerWindowPixel - (int)((centerUniversalBase-universalBase)* contigDisplay.getAveragePeakDistance());	}	/*..........................*/	public abstract int universalMapperOtherBaseValue();	/*..........................*/	protected int reverseSequenceBase(int sequenceBase, int sequenceLength){		return sequenceLength-sequenceBase-1;	}	/*..........................*/	public void paint(Graphics g, boolean colorByArray, SequenceMatchCalc suppliedColors) {		if (!checkForMapper()){			return;		}		if (MesquiteWindow.checkDoomed(this))			return;		int otherBaseCode = universalMapperOtherBaseValue();		int sel = oneSelected();		int pixels = 0;		int halfSpace = (int)(contigDisplay.getAveragePeakDistance()/2.0);		Font curFont = g.getFont();		Font boldFont12 = new Font (curFont.getName(), Font.BOLD, 12);		Font plainFont10 = new Font (curFont.getName(), Font.PLAIN, 10);		FontMetrics fMB12 = getFontMetrics(boldFont12);		FontMetrics fMP10 = getFontMetrics(plainFont10);		setBackground(contigDisplay.getBackgroundColor());		MesquiteBoolean higherReadConflicts = new MesquiteBoolean(false);		MesquiteBoolean muchHigherReadConflicts = new MesquiteBoolean(false);		int cheight = getBounds().height;		int cwidth = getBounds().width;		reCalcCenterBase();		String sequenceString = sequence.getSequence();		if (selected.length != sequenceString.length()){			boolean[] newSel = new boolean[sequenceString.length()];			for (int i=0; i<newSel.length; i++)				newSel[i] = false;			for (int i=0; i<newSel.length && i<selected.length; i++)				newSel[i] = selected[i];			selected = newSel;		}		int sequenceLength = sequenceString.length();				//======  Drawing the top line ==========================		g.setColor(Color.lightGray);		g.drawLine(0,0,cwidth,0); //^^^				//======  Drawing the center gray line ==========================		g.setColor(Color.lightGray);		g.drawLine(cwidth/2,0,cwidth/2,cheight);//^^^		/* NOTE: in all of the following loops, there used to be a continuation condition added of "&& (i< lastSequenceBase || lastSequenceBase<0)" 		 * However, this caused the last few bases to be not drawn in several circumstances, so this condition was removed		 * DRM 3 July 2008		 * */		//======  Now to color the bases ==========================		int numBasesColored = 0;		if ((colorByQuality && getColorBaseBackground()) || (colorByArray && suppliedColors != null) || getHasSpecialStandardBaseColors()) {			for (int i=firstUniversalBase; i<lastUniversalBase ;i++) {  				if (i>=0){					int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(otherBaseCode, i);					if (sequenceBase<0 || sequenceBase>=sequenceLength)						continue;					pixels = getPixelPositionOfUniversalBase(i);					Color c = null;					if (colorByArray){						if (suppliedColors != null)							c = suppliedColors.getBaseMatchColor(sequenceBase);					}					else if (colorByQuality)						c = sequence.getQualityColorOfBase(sequenceBase);					else if (getHasSpecialStandardBaseColors())						c = sequence.getStandardColorOfBase(sequenceBase);					if (c != null){						g.setColor(c);						if (pixels-halfSpace>=0)							fillRect(g, cwidth, pixels - halfSpace, topOfBase, (int)contigDisplay.getAveragePeakDistance()+1, baseHeight);//^^^						numBasesColored++;					}				}			}		}		//====== Now to draw a  bar beneath the highlighted bases  ==========================		if (sequencePanel.getShowReadReadConflict())			for (int i=firstUniversalBase; i<lastUniversalBase ;i++) {  				if (i>=0){					int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(otherBaseCode, i);					if (sequenceBase<0 || sequenceBase>=sequenceLength)						continue;					int contigBase =universalMapper.getOtherBaseFromUniversalBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, i);					pixels = getPixelPositionOfUniversalBase(i);					long s = DNAState.fromCharStatic(sequenceString.charAt(sequenceBase));					Color c = sequence.getHighlightColor(sequenceBase, contigBase);					int nmid=0;					if (sequenceBase>=0 && sequenceBase<sequenceString.length())						nmid = getBaseCenterPixel(sequenceString.charAt(sequenceBase),g);					if (c != null){						g.setColor(c);						fillRect(g, cwidth, pixels - nmid - 2, topOfBase+ baseHeight-1, (int)contigDisplay.getAveragePeakDistance()+1, 6);//^^^					}				}			}		//====== Now draw box around those bases that have source with relatively low quality  ==========================		if (sequencePanel.getShowLowerQualSourceConflictsWithHigherQualRead()) {			for (int i=firstUniversalBase; i<lastUniversalBase ;i++) {  				if (i>=0){					int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(otherBaseCode, i);					if (sequenceBase<0 || sequenceBase>=sequenceLength)						continue;					int contigBase = universalMapper.getOtherBaseFromUniversalBase(ChromaseqUniversalMapper.ORIGINALUNTRIMMEDSEQUENCE, i);					if (sequence.sourceReadIsLowerQuality(contigBase, 10, higherReadConflicts, 20, muchHigherReadConflicts)) {						pixels = getPixelPositionOfUniversalBase(i);						if (muchHigherReadConflicts.getValue()) {							g.setColor(Color.blue);							drawRect(g, cwidth, pixels-halfSpace, topOfBase, halfSpace*2, baseHeight);//^^^							drawRect(g, cwidth, pixels-halfSpace-1, topOfBase-1, halfSpace*2+2, baseHeight+2);//^^^						}						else {							g.setColor(Color.black);							drawRect(g, cwidth, pixels-halfSpace, topOfBase, halfSpace*2, baseHeight);//^^^						}					}				}			}		}		g.setFont(boldFont12);		//======  Now to show the selection  ==========================		int firstSel = MesquiteInteger.unassigned;		Composite composite = ColorDistribution.getComposite(g);		ColorDistribution.setTransparentGraphics(g);				g.setColor(Color.gray);		int lastSel = MesquiteInteger.unassigned;				for (int i=firstUniversalBase; i<lastUniversalBase ;i++) {  			if (i>=0){				int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(otherBaseCode, i);				if (sequenceBase<0 || sequenceBase>=sequenceLength)					continue;				pixels = getPixelPositionOfUniversalBase(i);								if (sequenceBase< selected.length && selected[sequenceBase]){					if (firstSel == MesquiteInteger.unassigned)						firstSel = pixels-halfSpace;					lastSel = pixels + halfSpace;				}				else if (firstSel != MesquiteInteger.unassigned){					fillRect(g, cwidth, firstSel, 0, lastSel - firstSel, cheight);//^^^					firstSel = MesquiteInteger.unassigned;				}			}		}		if (firstSel != MesquiteInteger.unassigned){  //uncompleted selection; select to end 			fillRect(g, cwidth, firstSel, 0, lastSel - firstSel, cheight);//^^^			firstSel = MesquiteInteger.unassigned;		}		ColorDistribution.setComposite(g,composite);					//======  Now to draw the text for bases  ==========================		for (int i=firstUniversalBase; i<lastUniversalBase ;i++) {  			if (i>=0){				int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(otherBaseCode, i);				if (sequenceBase<0 || sequenceBase>=sequenceLength)					continue;				pixels = getPixelPositionOfUniversalBase(i);				char c = ' ';				if (sequenceBase>=0 && sequenceBase<sequenceLength) {					c = sequenceString.charAt(sequenceBase);					if (isShownComplemented()){						c = DNAData.complementChar(c);					}				}							Color textC = contigDisplay.getBaseColor(c, contigDisplay.getBackgroundColor());				g.setColor(textC);				g.setFont(boldFont12);				int nmid = fMB12.stringWidth(""+c) / 2;				if (sel== sequenceBase && getEditable()){ //showing text edit box					g.setColor(Color.yellow);					int top = 2 + topOfBase;					int h = baseHeight;					drawRect(g, cwidth, pixels-nmid-1, top -3, nmid*2 +3, h+2);//^^^					g.setColor(Color.blue);					drawRect(g, cwidth, pixels-nmid-2, top -4, nmid*2 +5, h+4);//^^^					drawRect(g, cwidth, pixels-nmid-3, top -5, nmid*2 +7, h+6);//^^^					g.setColor(Color.yellow);					drawRect(g, cwidth, pixels-nmid-4, top -6, nmid*2 +9, h+8);//^^^					drawRect(g, cwidth, pixels-nmid-5, top -7, nmid*2 +11, h+10);//^^^				}				if (pixels-nmid>=0)					drawString(g, cwidth, ""+c,pixels-nmid, topOfBase + 12);//^^^				g.setColor(Color.black);				if ((sequenceBase+1) % 10 == 0 && pixels - nmid > 100) {					g.setColor(Color.lightGray);					g.setFont(plainFont10);					nmid = fMP10.stringWidth(String.valueOf(sequenceBase+1)) / 2;					drawString(g, cwidth, String.valueOf(sequenceBase+1),pixels - nmid,notesHeight);//^^^				}			}		}		g.setFont(plainFont10);		GraphicsUtil.setFontSize(10,g);		g.setColor(Color.gray);		g.drawString(getName(),4,notesHeight);		MesquiteWindow.uncheckDoomed(this);	}	/*--------------------------------------*/	public boolean getColorBaseBackground(){ 		return true; 	}	/*--------------------------------------*/	public boolean getHasSpecialStandardBaseColors(){ 		return false; 	}	/*--------@@@------------------------------*/	/*--------------------------------------*/	public boolean setSelectedUniversalBase(int i, boolean sel, boolean repaint){		if (!checkForMapper())			return false;		firstUniversalTouched = MesquiteInteger.unassigned;		secondUniversalTouched = MesquiteInteger.unassigned;		int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(), i);		if (selected != null && sequenceBase >= 0 && sequenceBase<selected.length) {			if (selected[sequenceBase] == sel)				return false;			selected[sequenceBase] = sel;  //selected uses index in consensus			if (repaint)				repaint();			return true;		}		return false;	}	public boolean getSelectedConsensus(int i){		if (!checkForMapper())			return false;		int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(), i);		if (selected != null && sequenceBase >= 0 && sequenceBase<selected.length)			return selected[sequenceBase] ;		return false;	}	public void deselectAll(){		firstUniversalTouched = MesquiteInteger.unassigned;		secondUniversalTouched = MesquiteInteger.unassigned;		for (int i=0; i<selected.length; i++)			selected[i] = false; 	}	/*--------------------------------------	//this is consensus position	private void selectConsensusRange(int i, int k){		int iLoc = getLocalIndexFromConsensus(i);		int kLoc = getLocalIndexFromConsensus(k);		for (int j= iLoc; j<=kLoc; j++)			if (j >=0 && j< selected.length)				selected[j] = true;		for (int j = i; j<=k; j++)			sequencePanel.exportSelectConsensusPosition(j);	}	 */	protected void selectUniversalRange(int universalBaseStart, int universalBaseEnd){		if (!checkForMapper())			return;		for (int universalBase= universalBaseStart; universalBase<=universalBaseEnd; universalBase++)			if (universalBase >=0) {				int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(), universalBase);				if (sequenceBase<0)					continue;				if (sequenceBase<selected.length)					selected[sequenceBase] = true;			}		for (int universalBase = universalBaseStart; universalBase<=universalBaseEnd; universalBase++){				sequencePanel.exportSelectUniversalBase(universalBase);		}	}	private void deselectUniversalRange(int universalBaseStart, int universalBaseEnd){		if (!checkForMapper())			return;		for (int universalBase= universalBaseStart; universalBase<=universalBaseEnd; universalBase++)			if (universalBase >=0) {				int sequenceBase = universalMapper.getOtherBaseFromUniversalBase(universalMapperOtherBaseValue(), universalBase);				if (sequenceBase<0)					continue;				if (sequenceBase< selected.length)					selected[sequenceBase] = false;			}		for (int universalBase = universalBaseStart; universalBase<=universalBaseEnd; universalBase++){			if (!isInsertedBase(universalBase))				sequencePanel.exportDeselectUniversalBase(universalBase);		}	}	private void deselectAllWithExport(){		for (int i=0;i<selected.length;i++)			selected[i] = false;		sequencePanel.exportDeselectAll();	}	/*	 * public void selectAndFocusConsensusPosition(int ic){		contigDisplay.setFirstTouchedOverall(contigDisplay.getOverallBaseFromConsensusBase(ic));		deselectAllWithExport();		selectConsensusRange(ic, ic);		contigDisplay.repaintPanels();	}	 */	public void selectAndFocusLocalPosition(int ic){		ChromaseqUniversalMapper universalMapper = contigDisplay.getUniversalMapper();		int universalBase = universalMapper.getUniversalBaseFromOtherBase(universalMapperOtherBaseValue(), ic);		contigDisplay.setFirstTouchedOverall(universalBase);		firstUniversalTouched = universalBase;		deselectAllWithExport();		selectUniversalRange(universalBase,universalBase);		contigDisplay.repaintPanels();	}	boolean isInsertedBase(int iloc){		//return getConsensusFromLocalIndex(iloc) == getConsensusFromLocalIndex(iloc-1);		return true;	}	//	int localFirstTouched = MesquiteInteger.unassigned;	int secondUniversalTouched = MesquiteInteger.unassigned;	int firstUniversalTouched = MesquiteInteger.unassigned;	boolean doubleClicked = false;	int universalDragged=MesquiteInteger.unassigned;	/* to be used by subclasses to tell that panel touched */	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {		MesquiteInteger universalPos = new MesquiteInteger();		ChromatogramTool chromTool = (ChromatogramTool)tool;		int iloc = findSequenceBase(x, universalPos, chromTool.getIsInBetween());		int universalBase = universalPos.getValue();		universalDragged=MesquiteInteger.unassigned;		doubleClicked = clickCount>1;		if (!tool.isArrowTool() && ((chromTool.getWorksOnEditableSequencePanel() && getEditable()) || (chromTool.getWorksOnOtherSequencePanels() && !getEditable()))){			((ChromatogramTool)tool).touched(universalBase, iloc, false, id, contigID, modifiers);			return;		}		if (MesquiteEvent.shiftKeyDown(modifiers)){			if (MesquiteInteger.isCombinable(iloc)){				if (!MesquiteInteger.isCombinable(universalBase))					universalBase = universalMapper.getUniversalBaseFromOtherBase(universalMapperOtherBaseValue(), iloc);				contigDisplay.setSecondTouchedOverall(universalBase);				deselectAllWithExport();				secondUniversalTouched = universalBase;				if (!MesquiteInteger.isCombinable(firstUniversalTouched))					firstUniversalTouched = contigDisplay.getFirstTouchedOverall();				if (MesquiteInteger.isCombinable(firstUniversalTouched)) {					if (firstUniversalTouched>secondUniversalTouched){						selectUniversalRange(secondUniversalTouched, firstUniversalTouched);					}					else {						selectUniversalRange(firstUniversalTouched, secondUniversalTouched);					}				}				else {					selectUniversalRange(secondUniversalTouched, secondUniversalTouched);				}				contigDisplay.repaintPanels();			}		}		else if (MesquiteEvent.commandOrControlKeyDown(modifiers)){			if (MesquiteInteger.isCombinable(universalBase)){				selectUniversalRange(universalBase, universalBase);				contigDisplay.repaintPanels();			}		}		else {			if (MesquiteInteger.isCombinable(universalBase)){				contigDisplay.setFirstTouchedOverall(universalBase);				deselectAllWithExport();				MesquiteTable table = contigDisplay.getTable();				if (table!=null)					table.deselectAll();				selectUniversalRange(universalBase,universalBase);				firstUniversalTouched = universalBase;				//	if ((clickCount>1 || sequencePanel.getScrollToTouched()) && MesquiteInteger.isCombinable(ic)){				//window.scrollToConsensusBase(ic);				//window.deselectAllReads();				//	}				contigDisplay.repaintPanels();				requestFocus();			}		}	}	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {		ChromatogramTool chromTool = (ChromatogramTool)tool;		if (!tool.isArrowTool() && ((chromTool.getWorksOnEditableSequencePanel() && getEditable()) || (chromTool.getWorksOnOtherSequencePanels() && !getEditable()))){			MesquiteInteger universalPos = new MesquiteInteger();			int iloc = findSequenceBase(x, universalPos, ((ChromatogramTool)tool).getIsInBetween());			int ic = universalPos.getValue();			((ChromatogramTool)tool).dragged(ic, iloc, false, id, contigID, modifiers);			return;		}		MesquiteInteger universalPos = new MesquiteInteger();		//	int iloc = findSequenceBase(x, universalPos, ((ChromatogramTool)tool).getIsInBetween());		universalDragged = universalPos.getValue();		int ic = universalPos.getValue();		if (MesquiteInteger.isCombinable(universalDragged)){			if (!MesquiteInteger.isCombinable(firstUniversalTouched))				firstUniversalTouched =contigDisplay.getFirstTouchedConsensus();			if (!MesquiteInteger.isCombinable(secondUniversalTouched))				secondUniversalTouched = contigDisplay.getSecondTouchedConsensus();			//deselectAll(); //this isn't correct behaviour!  If shift down should remember previously sleected pieces			if (MesquiteInteger.isCombinable(firstUniversalTouched)) {				if (firstUniversalTouched>ic){					if (MesquiteInteger.isCombinable(secondUniversalTouched) && secondUniversalTouched<firstUniversalTouched && ic>secondUniversalTouched){ //retracting						deselectUniversalRange(secondUniversalTouched, ic);					}					else 						selectUniversalRange(ic, firstUniversalTouched);				}				else {					if (MesquiteInteger.isCombinable(secondUniversalTouched) && secondUniversalTouched>firstUniversalTouched && ic<secondUniversalTouched){ //retracting						deselectUniversalRange(ic+1, secondUniversalTouched);					}					else 						selectUniversalRange(firstUniversalTouched, ic);				}			}			else {				selectUniversalRange(ic, ic);			}			contigDisplay.repaintPanels();			contigDisplay.setSecondTouchedOverall(ic);			secondUniversalTouched = ic;		}	}	/* to be used by subclasses to tell that panel touched */	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {		ChromatogramTool chromTool = (ChromatogramTool)tool;		if (!tool.isArrowTool() && ((chromTool.getWorksOnEditableSequencePanel() && getEditable()) || (chromTool.getWorksOnOtherSequencePanels() && !getEditable()))){			MesquiteInteger consensusPos = new MesquiteInteger();			int iloc = findSequenceBase(x, consensusPos, ((ChromatogramTool)tool).getIsInBetween());			int ic = consensusPos.getValue();			((ChromatogramTool)tool).dropped(ic, iloc, false, id, contigID, modifiers);			return;		}		if (MesquiteInteger.isCombinable(contigDisplay.getFirstTouchedOverall())){			if (!MesquiteInteger.isCombinable(contigDisplay.getSecondTouchedOverall()))				contigDisplay.focusMatrixOnUniversalBases(contigDisplay.getFirstTouchedOverall(), MesquiteInteger.unassigned);			else				contigDisplay.focusMatrixOnUniversalBases(contigDisplay.getFirstTouchedOverall(), contigDisplay.getSecondTouchedConsensus());			if ((doubleClicked || sequencePanel.getScrollToTouched()) && (firstUniversalTouched == universalDragged)){				contigDisplay.scrollToConsensusBase(contigDisplay.getFirstTouchedConsensus());				//window.deselectAllReads();			}		}		contigDisplay.setSecondTouchedOverall(MesquiteInteger.unassigned);	//	firstUniversalTouched = MesquiteInteger.unassigned;	}	/*...............................................................................................................*/	public void setCurrentCursor(int modifiers, int x, int y, ChromatogramTool tool) {		if (tool == null)			setCursor(getDisabledCursor());		else if ((tool.getWorksOnEditableSequencePanel() && getEditable()) || (tool.getWorksOnOtherSequencePanels() && !getEditable()))			setCursor(tool.getCursor());		else			setCursor(getDisabledCursor());	}	/*_________________________________________________*/	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {		if (tool == null)			return;		MesquiteInteger consensusPos = new MesquiteInteger();		int iloc = findSequenceBase(x, consensusPos, ((ChromatogramTool)tool).getIsInBetween());		int ic = consensusPos.getValue();		String s = getSequenceExplanation(ic);		contigDisplay.setExplanation( s);	}	/*...............................................................................................................*/	public abstract String getSequenceExplanation (int consensusBase);	/*...............................................................................................................*/	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {		setCursor(Cursor.getDefaultCursor());	}	/*...............................................................................................................*/	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {		if (tool == null)			return;		setCurrentCursor(modifiers, x, y, (ChromatogramTool)tool);	}}