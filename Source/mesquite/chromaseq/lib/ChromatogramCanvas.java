package mesquite.chromaseq.lib;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.chromaseq.lib.*;
import mesquite.chromaseq.ViewChromatograms.VChromWindow;
import mesquite.lib.ColorDistribution;
import mesquite.lib.Debugg;
import mesquite.lib.GraphicsUtil;
import mesquite.lib.IntegerArray;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteTool;
import mesquite.lib.MousePanel;
import mesquite.lib.StringUtil;

public class ChromatogramCanvas extends MousePanel {

	protected ChromatogramPanel chromatogramPanel;
	protected ContigDisplay contigDisplay;
	protected Chromatogram[] chromatograms; 
	protected Read[] reads;
	protected int numChromatograms=1;
	protected int[] A,C,G,T;
	protected int maxValue;
	protected int centerPixel;
	protected int leftPixel;
	protected int centerBase;
	protected int verticalPosition = 0;
	protected double shadowReduction = 0.5;
	protected double magnification = 1.0;
	protected int shadowOffset = 0;

	protected boolean[] selected;  // uses local read as index
	protected int id;

	static final double NONSOURCEDIMFRACTION = 0.2;
	static final double NONSOURCELOWERQUALITYDIMFRACTION = 0.15;


	/*..........................*/
	public ChromatogramCanvas(ChromatogramPanel parentV, int id) {
		super();
		this.id = id;
		chromatogramPanel = parentV;
		contigDisplay = chromatogramPanel.contigDisplay;
		addKeyListener(contigDisplay);
		chromatograms = parentV.chromatograms;
		numChromatograms = parentV.getNumChromatograms();
//		setBackground(Color.white);
		reads =new Read[numChromatograms];
		for (int i = 0; i<numChromatograms; i++)
			reads[i] = chromatograms[i].getRead();
		if (numChromatograms==1 && reads[0].getComplemented()) 
			setBackground(ColorDistribution.veryVeryLightGray);
		int i;
		maxValue = 0;
		if (canShowTraces())
			initTraces(0);
		selected = new boolean[contigDisplay.getTotalNumOverallBases()];   // uses local read as index

		for (i=0;i<contigDisplay.getTotalNumOverallBases();i++) {
			selected[i] = false;
		}

	}
	public boolean canShowTraces() {
		return true;
	}
	/*..........................*/
	public void initTraces(int chrom) {
		A = chromatograms[chrom].getATrace();
		C = chromatograms[chrom].getCTrace();
		G = chromatograms[chrom].getGTrace();
		T = chromatograms[chrom].getTTrace();
		for (int i=0;i<chromatograms[chrom].getTraceLength();i++) {
			if (A[i] > maxValue) maxValue = A[i];
			if (C[i] > maxValue) maxValue = C[i];
			if (G[i] > maxValue) maxValue = G[i];
			if (T[i] > maxValue) maxValue = T[i];
		}
	}

	public boolean isShownReversed(){
		return contigDisplay.isShownReversed();
	}
	public boolean isShownComplemented(){
		return contigDisplay.isShownComplemented();
	}
	/*...........................................................................*/
	/** Given a pixel offset from left, returns the consensus position at that point *
	public int OLDfindConsensusPosition(int xPixel){ //this needs to return consensus position!
		reCalcCenterBase();
		for (int i=0;i < chromatogram.getBaseNumber();i++) {
			if (xPixel < chromatogram.getReadBaseLocationAligned(i) - leftPixel - 5)
				return getConsensusBaseFromReadBase(i-1);
		}
		if (xPixel < chromatogram.getReadBaseLocationAligned(chromatogram.getBaseNumber()-1) - leftPixel - 5+30)
			return getConsensusBaseFromReadBase(chromatogram.getBaseNumber()-1);
		return -1;
	}
	/*...........................................................................*/
	protected void reCalcCenterBase(){
		centerBase = chromatogramPanel.centerBase;   //number of centered base
		centerPixel = contigDisplay.getCenterPixelFromCenterBase(centerBase);   //number of pixels over this scrolled base is
		leftPixel=centerPixel-getBounds().width/2;
		verticalPosition = chromatogramPanel.verticalPosition;
	}

	public void setShadowReduction(double mag){
		this.shadowReduction = mag;
		repaint();
	}
	public void setMagnification(double mag){
		this.magnification = mag;
		repaint();
	}
	public void setShadowOffset(int o){
		this.shadowOffset = o;
		repaint();
	}

	/*...........................................................................*/
	public int leftEdgeOfConsensusBase(Graphics g, int i) {	
		return 0;
	}
	/*...........................................................................*/
	/** Given a pixel offset from left, returns the consensus position at that point. This is not the overall consensus position, zero-based,
	 * but instead the position from the start of the main contig. Thus,  positions to the left of the main contig are -ve */
	public int findOverallBaseNumber(int whichRead, int xPixel){ //this needs to return consensus position!
		int cons = findConsensusBaseNumber(whichRead, xPixel);
		int overall = contigDisplay.getOverallBaseFromConsensusBase(cons);

		return overall;
	}
	/*...........................................................................*/
	/** Given a pixel offset from left, returns the consensus position at that point. This is not the overall consensus position, zero-based,
	 * but instead the position from the start of the main contig. Thus,  positions to the left of the main contig are -ve *
	public int OLDfindConsensusBaseNumber(int xPixel){ //this needs to return consensus position!
		//David: 
		/* this isn't finding the consensus base number properly in the first read of lapsiines.nex.  Click to awar from the midline on the
	 * read, and it will select too far away from the midline
	 *
		Graphics g = getGraphics();
		Font f = null;
		if (g != null)
			f = g.getFont();

		reCalcCenterBase();
		//int firstReadLocation = (chromatogram.getReadBaseLocation(getReadBaseFromConsensusBase(centerBase-panel.getContig().getReadExcessAtStart()))-getBounds().width/2);
		int firstReadLocation = getFirstReadLocation();

		int offsetForInserted = 0;
		for (int i=0;i < chromatogram.getBaseNumber();i++) {
			int cmid = 10;
			int consensusBase = getConsensusBaseFromReadBase(i);
			offsetForInserted += panel.getSpaceInsertedBeforeConsensusBase(consensusBase);
			if (f != null)
				cmid = getFontMetrics(f).charWidth(chromatogram.getBase(i)) / 2;
			if (xPixel < chromatogram.getReadBaseLocation(i) - firstReadLocation - cmid + offsetForInserted) {
				int cons = getConsensusBaseFromReadBase(i-1);
//Debugg.println("findConsensusPosition i  " + i  + chromatogram.getBase(i) + "  " + cons);
				return cons;
			}
		}
		if (xPixel < chromatogram.getReadBaseLocation(chromatogram.getBaseNumber()-1) - firstReadLocation - leftPixel - 5+30)
			return getConsensusBaseFromReadBase(chromatogram.getBaseNumber()-1);
		return -panel.getOverallBaseFromConsensusBase(0)-1;
	}
	/*...........................................................................*/
	/** Given a pixel offset from left, returns the consensus position at that point. This is not the overall consensus position, zero-based,
	 * but instead the position from the start of the main contig. Thus,  positions to the left of the main contig are -ve */
	public int findConsensusBaseNumber(int whichRead, int xPixel){ //this needs to return consensus position!
		reCalcCenterBase();
		int cwidth = getBounds().width;
		int halfPeaks = contigDisplay.getApproximateNumberOfPeaksVisible()/2;
		int centerConsensusBase = centerBase-contigDisplay.getContig().getReadExcessAtStart();
		int centerReadBase = getReadBaseFromConsensusBase(whichRead, centerConsensusBase);

		int firstReadBase = centerReadBase - halfPeaks;
		int lastReadBase = centerReadBase+halfPeaks;

		int firstReadLocation = getPhdLocation(reads[whichRead], cwidth, centerReadBase,contigDisplay,true) - cwidth/2;
		int count = 0;
		while (getPhdLocation(reads[whichRead], cwidth, firstReadBase,contigDisplay,true)  - firstReadLocation >0 && firstReadLocation>0 && count++<200)
			firstReadBase--; //correcting for error in numpeaksvisible for this read
		count = 0;
		while (getPhdLocation(reads[whichRead], cwidth, lastReadBase,contigDisplay,true)  - firstReadLocation <cwidth && count++<200)
			lastReadBase++; //correcting for error in numpeaksvisible for this read
		firstReadBase--;
		lastReadBase++;

		return findConsensusBaseNumber(whichRead, xPixel, firstReadBase, lastReadBase, firstReadLocation);
	}

	/*...........................................................................*/
	/** Given a pixel offset from left, returns the consensus position at that point. This is not the overall consensus position, zero-based,
	 * but instead the position from the start of the main contig. Thus,  positions to the left of the main contig are -ve */
	public int findConsensusBaseNumber(int whichRead, int xPixel, int firstReadBase, int lastReadBase, int firstReadLocation){ //this needs to return consensus position!
		Graphics g = getGraphics();
		Font f = null;
		if (g != null)
			f = g.getFont();
		FontMetrics fm = null;
		if (f != null)
			fm = getFontMetrics(f);

		int offsetForInserted = 0;
		int cwidth = getBounds().width;
		if (isShownReversed()) {
			xPixel = cwidth - xPixel;
			for (int i=firstReadBase;i < chromatograms[whichRead].getBaseNumber() && i<=lastReadBase;i++) {
				if (i>=0 && i<reads[whichRead].getBasesLength()) {
					int consensusBase = getConsensusBaseFromReadBase(whichRead,i);
					int space = contigDisplay.getSpaceInsertedBeforeDisplayBase(consensusBase);
					offsetForInserted += space;

					int overallBase = contigDisplay.getOverallBaseFromConsensusBase(consensusBase);

					int cmid = 10;
					if (fm != null)
						cmid = fm.charWidth(chromatograms[whichRead].getBase(i)) / 2;
					int w = 0;
					if (i+1< chromatograms[whichRead].getBaseNumber())
						//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
						w = getPhdLocation(reads[whichRead], cwidth, i+1, contigDisplay,true) - getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true) + 1;
					else
						w =  (int)contigDisplay.getAveragePeakDistance();
					int h = getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true)- firstReadLocation - cmid - 2 + offsetForInserted;
					/*if (reversed){
						w = cwidth-w;
						h = cwidth-h;
				}*/
					if (xPixel >= h && xPixel < h + w) {
						int cons = getConsensusBaseFromReadBase(whichRead,i);
						return cons;
					}
					if (xPixel < h + w){
						int cons = getConsensusBaseFromReadBase(whichRead,i);
						if (cons>=0 && MesquiteInteger.isCombinable(cons))
							return cons;
						return MesquiteInteger.unassigned;
					}
				}
			}

		}
		else
			for (int i=firstReadBase;i < chromatograms[whichRead].getBaseNumber() && i<=lastReadBase;i++) {
				if (i>=0 && i<reads[whichRead].getBasesLength()) {
					int consensusBase = getConsensusBaseFromReadBase(whichRead,i);
					int space = contigDisplay.getSpaceInsertedBeforeDisplayBase(consensusBase);
					offsetForInserted += space;

					int overallBase = contigDisplay.getOverallBaseFromConsensusBase(consensusBase);

					int cmid = 10;
					if (fm != null)
						cmid = fm.charWidth(chromatograms[whichRead].getBase(i)) / 2;
					int w = 0;
					if (i+1< chromatograms[whichRead].getBaseNumber())
						//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
						w = getPhdLocation(reads[whichRead], cwidth, i+1, contigDisplay,true) - getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true) + 1;
					else
						w =  (int)contigDisplay.getAveragePeakDistance();
					int h = getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true)- firstReadLocation - cmid - 2 + offsetForInserted;
					/*if (reversed){
					w = cwidth-w;
					h = cwidth-h;
			}*/
					if (xPixel >= h && xPixel < h + w) {
						int cons = getConsensusBaseFromReadBase(whichRead,i);
						return cons;
					}
					if (xPixel < h + w){
						int cons = getConsensusBaseFromReadBase(whichRead,i);
						//	Debugg.println("xPixel " + xPixel + " cons " + cons);
						return MesquiteInteger.unassigned;
					}
				}
			}
		if (xPixel < chromatograms[whichRead].getReadBaseLocation(chromatograms[whichRead].getBaseNumber()-1) - firstReadLocation - leftPixel - 5+30)
			return getConsensusBaseFromReadBase(whichRead,chromatograms[whichRead].getBaseNumber()-1);
		return -contigDisplay.getOverallBaseFromConsensusBase(0)-1;

	}
	protected void fillRect(Graphics g, int width, int x, int y, int w, int h){
		if (isShownReversed()){
			g.fillRect(width-(x+w), y, w, h);
		}
		else {
			g.fillRect(x, y, w, h);
		}
	}

	protected void drawRect(Graphics g, int width, int x, int y, int w, int h){
		if (isShownReversed()){
			g.drawRect(width-(x+w), y, w, h);
		}
		else {
			g.drawRect(x, y, w, h);
		}
	}
	protected void drawLine(Graphics g, int width, int x, int y, int x2, int y2){
		if (isShownReversed()){
			g.drawLine(width-x, y, width-x2, y2);
			if (chromatogramPanel.getThickTrace())
				g.drawLine(width-x, y-1, width-x2, y2-1);
		}
		else {
			g.drawLine(x, y, x2, y2);
			if (chromatogramPanel.getThickTrace())
				g.drawLine(x+1, y, x2+1, y2);
		}
	}
	protected void drawString(Graphics g, int width, String s, int x, int y){
		if (isShownReversed()){
			int sw = StringUtil.getStringDrawLength(g, s);
			g.drawString(s, width-(x+sw), y);
		}
		else {
			g.drawString(s, x, y);
		}
	}

	protected int getPhdLocation(Read read, int width, int center, ContigDisplay panel, boolean calcAverageIfZero){
		return read.getPhdLocation(center,panel,calcAverageIfZero);
	}

	/*...........................................................................*/
	public int getFirstReadLocation(int whichRead) {	
		int cwidth = getBounds().width;
		reCalcCenterBase();
		int centerConsensusBase = centerBase-contigDisplay.getContig().getReadExcessAtStart();
		int centerReadBase = getReadBaseFromConsensusBase(whichRead, centerConsensusBase);
//		int firstReadLocation = read.getPhdLocation(centerReadBase,panel,true) - cwidth/2;
		int firstReadLocation = chromatograms[whichRead].getReadBaseLocation(centerReadBase) - cwidth/2;
		return firstReadLocation;

		//int firstReadLocation = (chromatogram.getReadBaseLocation(getReadBaseFromConsensusBase(centerBase-panel.getContig().getReadExcessAtStart()))-getBounds().width/2);

	}
	/*--------------------------------------*/
	/*This returns for read position i, what is the position in the consensus. */
	public int getConsensusBaseFromReadBase(int whichRead, int i){
		Read read = chromatograms[whichRead].getRead();
		if (read!=null)
			return read.getContigBaseFromReadBase(i);
		else 
			return i;

	}
	public void deselectAll(){
		for (int i = 0; i<selected.length; i++)
			selected[i]=false;
	}
	/*--------------------------------------*/
	public void setSelected(int overallBase, boolean sel){
		if (overallBase>=0||overallBase<selected.length)
			selected[overallBase]= sel;
	}
	/*--------------------------------------*/
	public boolean getSelected(int overallBase){
		if (overallBase<0||overallBase>=selected.length)
			return false;
		return selected[overallBase];
	}
	/*--------------------------------------*/
	/*This returns for consensus position i, what is the position in the read. */
	public int getReadBaseFromConsensusBase(int whichRead,int i){
		Read read = chromatograms[whichRead].getRead();
		if (read!=null)
			return read.getReadBaseFromContigBase(i);  
		else
			return i;
	}
	/*--------------------------------------*/
	/*This returns for overall position i, what is the position in the read. */
	public int getReadBaseFromUniversalBase(int whichRead,int i){
		int contigBase = contigDisplay.getContigBaseFromUniversalBase(i);
		Read read = chromatograms[whichRead].getRead();
		if (read!=null)
			return read.getReadBaseFromContigBase(contigBase);  
		else
			return i;
	}
	/*--------------------------------------*/
	/** Given the overall base number overallBase, this method selects that base within this chromatogram.
	 * Note that it does NOT translate to the base number within the read, as selection can now happen even if 
	 * there is no read there */
	public boolean setSelectedInChrom(int overallBase, boolean sel, boolean repaint){
//		why is this called an absurd number of times  Debugg.println("1");
		if (selected != null && overallBase >= 0 && overallBase<selected.length) {
			if (selected[overallBase] == sel)
				return false;
			selected[overallBase] = sel;  //selected uses index in consensus
			if (repaint)
				repaint();
			return true;
		}
		return false;
//		Debugg.println("2");
	}
	/*--------------------------------------*/
	/** Given the overall base numbering, as given in overallBase, this method selects that base within this contig*/
	public void selectOverallBase(int overallBase){
		int consensusBase = contigDisplay.getContigBaseFromUniversalBase(overallBase);
//		int readBase = getReadBaseFromConsensusBase(consensusBase);
//		if (j <0 || j>= chromatogram.getTraceLength())
//		return;
//		Debugg.println("|||||||||||| selectOverallBase " + overallBase + " consensusBase " + consensusBase);
		if (overallBase <0 || overallBase> selected.length)
			return;
		selected[overallBase] = true;
		chromatogramPanel.exportSelectUniversalBase(overallBase);
	}

	//this is consensus position
	public void deselectOverallBase(int whichRead, int overallBase){
		int consensusBase = contigDisplay.getContigBaseFromUniversalBase(overallBase);
		int readBase = getReadBaseFromConsensusBase(whichRead, consensusBase);
		selected[overallBase] = false;
		chromatogramPanel.exportDeselectUniversalBase(overallBase);
	}
	/*.................................................................................................................*/
	public String getPeakHeightsOfBase(int whichRead, int readBaseNumber) {   // i is the position, zero-based, in the padded consensus sequence; returns location
		if (readBaseNumber<0)
			return "";
		else {
			int[] traceArray = new int[5];
			int location = getPhdLocation(reads[whichRead], getBounds().width, readBaseNumber,contigDisplay,true);
			String[] baseArray = new String[5];
			traceArray[0] = chromatograms[whichRead].getATrace(location);
			baseArray[0]="A";
			traceArray[1] = chromatograms[whichRead].getCTrace(location);
			baseArray[1]="C";
			traceArray[2] = chromatograms[whichRead].getGTrace(location);
			baseArray[2]="G";
			traceArray[3] = chromatograms[whichRead].getTTrace(location);
			baseArray[3]="T";
			for (int i = 0; i<4; i++){
				int max = IntegerArray.indexOfMaximum(traceArray);
				traceArray[4] = traceArray[i];  //store this here temporarily
				baseArray[4] = baseArray[i];  //store this here temporarily
				traceArray[i] =  traceArray[max];
				baseArray[i] = baseArray[max];
				traceArray[max] = traceArray[4];
				baseArray[max] = baseArray[4];
				traceArray[i] = - traceArray[i];
				traceArray[4]=0;
			}
			for (int i = 0; i<4; i++){
				if (traceArray[i]<0)
					traceArray[i] = -traceArray[i];
			}

			StringBuffer sb = new StringBuffer();

			for (int i = 0; i<4; i++){
				if (traceArray[i]>0) {
					sb.append(baseArray[i] + ": " + traceArray[i]);
					if (i<3 && traceArray[i+1]>0)
						sb.append(", ");
				}
			}

			return sb.toString();
		}
	}
	/*...............................................................................................................*/
	public void setCurrentCursor(int modifiers, int x, int y, ChromatogramTool tool) {
		if (tool == null)
			setCursor(getDisabledCursor());
		else 	if (tool.getWorksOnChromatogramPanels()) 
			setCursor(tool.getCursor());
		else
			setCursor(getDisabledCursor());
	}

	public static int SETREAD = 0;
	/*--------------------------------------*/
	/* to be used by subclasses to tell that panel touched */
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int ic = findOverallBaseNumber(SETREAD, x); 
		boolean onRequiredSelection = chromTool.getWorksOnlyOnSelection() && !getSelected(ic);
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels() &&!onRequiredSelection){
			int cons = findConsensusBaseNumber(SETREAD,x);
			((ChromatogramTool)tool).touched(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
			/*		MesquiteWindow w = chromatogramPanel.getMesquiteWindow();
		if (w!=null)
			w.showQuickMessage(chromatogramPanel,null,x,y,"testing");
			 */
			return;
		}

		if (MesquiteEvent.shiftKeyDown(modifiers)){
			if (MesquiteInteger.isCombinable(ic)){
				contigDisplay.setSecondTouchedOverall(ic);
				chromatogramPanel.exportDeselectAll();
				if (MesquiteInteger.isCombinable(contigDisplay.getFirstTouchedOverall())) {
					if (contigDisplay.getFirstTouchedOverall()>ic){
						for (int i = ic; i<=contigDisplay.getFirstTouchedOverall(); i++)
							selectOverallBase(i);
					}
					else {
						for (int i = contigDisplay.getFirstTouchedOverall(); i<=ic; i++)
							selectOverallBase(i);
					}
				}
				else {
					selectOverallBase(ic);
				}
				//chromatogramPanel.synchTableToChrom(false);
				contigDisplay.repaintPanels();
			}
		}
		else if (MesquiteEvent.commandOrControlKeyDown(modifiers)){
			if (MesquiteInteger.isCombinable(ic)){
				selectOverallBase(ic);
				contigDisplay.repaintPanels();
			}
		}
		else {
			if (MesquiteInteger.isCombinable(ic)){
				contigDisplay.setFirstTouchedOverall(ic);
				chromatogramPanel.exportDeselectAll();
				selectOverallBase(ic);
				if (clickCount>1)
					contigDisplay.scrollToOverallBase(ic);
				contigDisplay.repaintPanels();
			}
		}
	}
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int ic = findOverallBaseNumber(SETREAD,x); 
		boolean onRequiredSelection = chromTool.getWorksOnlyOnSelection() && !getSelected(ic);
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels() && !onRequiredSelection){
			int cons = findConsensusBaseNumber(SETREAD,x);
			((ChromatogramTool)tool).dragged(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(ic)){
			//deselectAll(); //this isn't correct behaviour!  If shift down should remember previously sleected pieces
			if (MesquiteInteger.isCombinable(contigDisplay.getFirstTouchedOverall())) {
				if (contigDisplay.getFirstTouchedOverall()>ic){
					if (MesquiteInteger.isCombinable(contigDisplay.getSecondTouchedOverall()) && contigDisplay.getSecondTouchedOverall()<contigDisplay.getFirstTouchedOverall() && ic>contigDisplay.getSecondTouchedOverall()){ //retracting
						for (int i = contigDisplay.getSecondTouchedOverall()+1; i<=ic; i++) 
							deselectOverallBase(SETREAD,i);
					}
					else for (int i = ic; i<=contigDisplay.getFirstTouchedOverall(); i++) //adding
						selectOverallBase(i);
				}
				else {
					if (MesquiteInteger.isCombinable(contigDisplay.getSecondTouchedOverall()) && contigDisplay.getSecondTouchedOverall()>contigDisplay.getFirstTouchedOverall() && ic<contigDisplay.getSecondTouchedOverall()){ //retracting
						for (int i = ic+1; i<=contigDisplay.getSecondTouchedOverall(); i++) 
							deselectOverallBase(SETREAD,i);
					}
					else for (int i = contigDisplay.getFirstTouchedOverall(); i<=ic; i++)
						selectOverallBase(i);
				}
			}
			else {
				selectOverallBase(ic);
			}
			//chromatogramPanel.synchTableToChrom(false);
			contigDisplay.repaintPanels();
			contigDisplay.setSecondTouchedOverall(ic);
		}
	}
	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels()){
			int cons = findConsensusBaseNumber(SETREAD,x);
			((ChromatogramTool)tool).dropped(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(contigDisplay.getFirstTouchedOverall())){
			if (!MesquiteInteger.isCombinable(contigDisplay.getSecondTouchedOverall()))
				contigDisplay.focusMatrixOn(contigDisplay.getContigBaseFromUniversalBase(contigDisplay.getFirstTouchedOverall()), MesquiteInteger.unassigned);
			else
				contigDisplay.focusMatrixOn(contigDisplay.getContigBaseFromUniversalBase(contigDisplay.getFirstTouchedOverall()), contigDisplay.getContigBaseFromUniversalBase(contigDisplay.getSecondTouchedOverall()));
		}
		if (chromatogramPanel.getScrollToTouched()) {
			int ic = findConsensusBaseNumber(SETREAD,x);
			if (ic>=0) {
				contigDisplay.scrollToConsensusBase(ic);
		//		panel.deselectAllReads();

			}
		}
		contigDisplay.setSecondTouchedOverall(MesquiteInteger.unassigned);
	}
	/*_________________________________________________*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int ic = findConsensusBaseNumber(SETREAD,x);
		int readBaseNumber = reads[SETREAD].getReadBaseFromContigBase(ic);
		int quality = reads[SETREAD].getPhdBaseQuality(readBaseNumber);
		double averageQuality = reads[SETREAD].getAverageQuality();
		int numBasesHighQuality = reads[SETREAD].getNumBasesHighQuality();
		String s = "";
		if (quality>=0)
			s+= "Base quality: " + quality + ",   Peak heights: " + getPeakHeightsOfBase(SETREAD,readBaseNumber);
		s+= "\n# Bases with Quality ³ " + reads[SETREAD].getNumBasesHighQualityThreshold() + ": " + numBasesHighQuality + ",  Average Quality: " + averageQuality + "  ("+chromatograms[SETREAD].getTitle()+")";
		contigDisplay.setExplanation( s);
		if (tool == null)
			return;
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (chromTool.getWorksOnlyOnSelection())
			if (!getSelected(findOverallBaseNumber(SETREAD,x)))
				setCursor(Cursor.getDefaultCursor());
			else
				setCurrentCursor(modifiers, x, y, chromTool);
	}

	/*...............................................................................................................*/
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
		setCursor(Cursor.getDefaultCursor());
	}
	/*...............................................................................................................*/
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		if (tool == null)
			return;
		setCurrentCursor(modifiers, x, y, (ChromatogramTool)tool);
	}
}