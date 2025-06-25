/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.lib;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;

import mesquite.chromaseq.ViewChromatograms.ChromaseqUniversalMapper;
import mesquite.chromaseq.ViewChromatograms.ChromatogramCloseupPanel;
import mesquite.lib.IntegerArray;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.StringUtil;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MousePanel;

public class ChromatogramCanvas extends MousePanel {

	protected ChromatogramCloseupPanel closeupPanel;
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
	//	addKeyListener(contigDisplay);   if this is in then skips problems with jump keys
		chromatograms = parentV.chromatograms;
		numChromatograms = parentV.getNumChromatograms();
//		setBackground(Color.white);
		reads =new Read[numChromatograms];
		for (int i = 0; i<numChromatograms; i++)
			if (chromatograms[i]!=null)
				reads[i] = chromatograms[i].getRead();
		setBackgroundColor();
		int i;
		maxValue = 0;
		if (canShowTraces())
			initTraces(0);
		selected = new boolean[contigDisplay.getTotalNumUniversalBases()];   // uses local read as index

		for (i=0;i<contigDisplay.getTotalNumUniversalBases();i++) {
			selected[i] = false;
		}

	}
	public void setBackgroundColor() {
		if (numChromatograms==1 && reads[0].getComplemented()) 
			setBackground(ColorDistribution.veryVeryLightGray);
	}
	/*..........................*/
	public void setChromatograms(Chromatogram[] chromatograms) {
		this.chromatograms = chromatograms;
		if (chromatograms!=null && chromatograms[0]!=null) {
			numChromatograms = chromatograms.length;
			//		setBackground(Color.white);
			reads =new Read[numChromatograms];
			for (int i = 0; i<numChromatograms; i++)
				reads[i] = chromatograms[i].getRead();
			setBackgroundColor();
			int i;
			maxValue = 0;
			if (canShowTraces())
				initTraces(0);
			selected = new boolean[contigDisplay.getTotalNumUniversalBases()];   // uses local read as index

			for (i=0;i<contigDisplay.getTotalNumUniversalBases();i++) {
				selected[i] = false;
			}
		}else
			numChromatograms=0;

	}
	/*..........................*
	public void setChromatogramPanel(ChromatogramPanel parentV) {
		chromatogramPanel = parentV;
		contigDisplay = chromatogramPanel.contigDisplay;
		chromatograms = parentV.chromatograms;
		numChromatograms = parentV.getNumChromatograms();
		reads =new Read[numChromatograms];
		for (int i = 0; i<numChromatograms; i++)
			reads[i] = chromatograms[i].getRead();
		if (numChromatograms==1 && reads[0].getComplemented()) 
			setBackground(ColorDistribution.veryVeryLightGray);
		int i;
		maxValue = 0;
		if (canShowTraces())
			initTraces(0);
		selected = new boolean[contigDisplay.getTotalNumUniversalBases()];   // uses local read as index

		for (i=0;i<contigDisplay.getTotalNumUniversalBases();i++) {
			selected[i] = false;
		}

	}
	/*..........................*/
	public boolean canShowTraces() {
		return true;
	}
	/*..........................*/
	public void initTraces(int chrom) {
		if (chromatograms[chrom]==null) return;
		
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
	public int findUniversalBaseNumber(int whichRead, int xPixel){ //this needs to return consensus position!
		int cons = findConsensusBaseNumber(whichRead, xPixel);
		int overall = contigDisplay.getUniversalBaseFromContigBase(cons);

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
		ChromaseqUniversalMapper universalMapper = contigDisplay.getUniversalMapper();
		int cwidth = getBounds().width;
		int halfPeaks = contigDisplay.getApproximateNumberOfPeaksVisible()/2;
		int centerConsensusBase;
		int centerReadBase;
		if (contigDisplay.contigExists()) {
			centerConsensusBase = universalMapper.getOtherBaseFromUniversalBase(ChromaseqUniversalMapper.ACEFILECONTIG, centerBase); //centerBase-contigDisplay.getContig().getReadExcessAtStart();
			 centerReadBase = getReadBaseFromConsensusBase(whichRead, centerConsensusBase);
		} else {
			centerConsensusBase = centerBase;
			centerReadBase = centerBase;
		}

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
	public synchronized int findConsensusBaseNumber(int whichRead, int xPixel, int firstReadBase, int lastReadBase, int firstReadLocation){ //this needs to return consensus position!
		if (chromatograms==null || chromatograms.length==0 || chromatograms[whichRead]==null)
			return MesquiteInteger.unassigned;
		if (reads==null || reads.length==0 || reads[whichRead]==null)
			return MesquiteInteger.unassigned;
		Graphics g = getGraphics();
		Font f = null;
		if (g != null)
			f = g.getFont();
		FontMetrics fm = null;
		if (f != null)
			fm = getFontMetrics(f);

//		int offsetForInserted = 0;
		int cwidth = getBounds().width;
		if (isShownReversed()) {
			xPixel = cwidth - xPixel;
			for (int i=firstReadBase; i<=lastReadBase;i++) {
				if (reads==null || reads.length==0 || reads[whichRead]==null)
					return MesquiteInteger.unassigned;
				if (i>=0 && i<reads[whichRead].getBasesLength()) {
					if (chromatograms==null || chromatograms.length==0 || chromatograms[whichRead]==null)
						return MesquiteInteger.unassigned;

					int cmid = 10;
					if (fm != null)
						cmid = fm.charWidth(chromatograms[whichRead].getBase(i)) / 2;
					int w = 0;
					if (i+1< chromatograms[whichRead].getBaseNumber())
						//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
						w = getPhdLocation(reads[whichRead], cwidth, i+1, contigDisplay,true) - getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true) + 1;
					else
						w =  (int)contigDisplay.getAveragePeakDistance();
					int h = getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true)- firstReadLocation - cmid - 2;
					/*if (reversed){
						w = cwidth-w;
						h = cwidth-h;
				}*/
					if (xPixel >= h && xPixel < h + w) {
						int cons = getContigBaseFromReadBase(whichRead,i);
						return cons;
					}
					if (xPixel < h + w){
						int cons = getContigBaseFromReadBase(whichRead,i);
						if (cons>=0 && MesquiteInteger.isCombinable(cons))
							return cons;
						return MesquiteInteger.unassigned;
					}
				}
			}

		}
		else
			for (int i=firstReadBase; i<=lastReadBase;i++) { //i < chromatograms[whichRead].getBaseNumber() &&
				if (reads==null || reads.length==0 || reads[whichRead]==null)
					return MesquiteInteger.unassigned;
				if (i>=0 && i<reads[whichRead].getBasesLength()) {
					if (chromatograms==null || chromatograms.length==0 || chromatograms[whichRead]==null)
						return MesquiteInteger.unassigned;

					int cmid = 10;
					if (fm != null)
						cmid = fm.charWidth(chromatograms[whichRead].getBase(i)) / 2;
					int w = 0;
					if (i+1< chromatograms[whichRead].getBaseNumber())
						//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
						w = getPhdLocation(reads[whichRead], cwidth, i+1, contigDisplay,true) - getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true) + 1;
					else
						w =  (int)contigDisplay.getAveragePeakDistance();
					int h = getPhdLocation(reads[whichRead], cwidth, i, contigDisplay,true)- firstReadLocation - cmid - 2;
					/*if (reversed){
					w = cwidth-w;
					h = cwidth-h;
			}*/
					if (xPixel >= h && xPixel < h + w) {
						int cons = getContigBaseFromReadBase(whichRead,i);
						return cons;
					}
					if (xPixel < h + w){
						int cons = getContigBaseFromReadBase(whichRead,i);
						if (cons>=0 && MesquiteInteger.isCombinable(cons))
							return cons;
						return MesquiteInteger.unassigned;
					}
				}
			}
		if (xPixel < chromatograms[whichRead].getReadBaseLocation(chromatograms[whichRead].getBaseNumber()-1) - firstReadLocation - leftPixel - 5+30)
			return getContigBaseFromReadBase(whichRead,chromatograms[whichRead].getBaseNumber()-1);
		return -contigDisplay.getUniversalBaseFromContigBase(0)-1;

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
		if (read==null)
			return 0;
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
	public int getContigBaseFromReadBase(int whichRead, int i){
		if (contigDisplay.contigExists()) {
			Read read = chromatograms[whichRead].getRead();
			if (read!=null)
				return read.getContigBaseFromReadBase(i);
			else 
				return i;
		}
		return i;
	}
	public void deselectAll(){
		for (int i = 0; i<selected.length; i++)
			selected[i]=false;
	}
	/*--------------------------------------*/
	public void setSelected(int overallBase, boolean sel){
		if (overallBase>=0|| overallBase<selected.length)
			selected[overallBase]= sel;
	}
	/*--------------------------------------*/
	public boolean getSelected(int overallBase){
		if (overallBase<0|| overallBase>=selected.length)
			return false;
		return selected[overallBase];
	}
	/*--------------------------------------*/
	/*This returns for consensus position i, what is the position in the read. */
	public int getReadBaseFromConsensusBase(int whichRead,int i){
		if (!contigDisplay.contigExists())
			return i;
		Read read = chromatograms[whichRead].getRead();
		if (read!=null)
			return read.getReadBaseFromContigBase(i);  
		else
			return i;
	}
	/*--------------------------------------*/
	/*This returns for consensus position i, what is the position in the read. */
	public int getUniversalBaseFromReadBase(int whichRead,int i){
		if (!contigDisplay.contigExists())
			return i;
		Read read = chromatograms[whichRead].getRead();
		if (read!=null){
			int contigBase= read.getContigBaseFromReadBase(i);  
			return contigDisplay.getUniversalBaseFromContigBase(contigBase);
		}
		else
			return i;
	}
	/*--------------------------------------*/
	/*This returns for overall position i, what is the position in the read. */
	public int getReadBaseFromUniversalBase(int whichRead,int i){
		if (chromatograms==null || chromatograms.length==0 || chromatograms[whichRead]==null)
			return i;
		if (contigDisplay==null || !contigDisplay.contigExists())
			return i;
		int contigBase = contigDisplay.getContigBaseFromUniversalBase(i);
		if (!MesquiteInteger.isCombinable(contigBase))
			return MesquiteInteger.unassigned;
		Read read = chromatograms[whichRead].getRead();
		if (read!=null) {
			int readBase =  read.getReadBaseFromContigBase(contigBase);  
			return readBase;
		}
		else
			return i;
	}
	/*--------------------------------------*/
	/** Given the overall base number overallBase, this method selects that base within this chromatogram.
	 * Note that it does NOT translate to the base number within the read, as selection can now happen even if 
	 * there is no read there */
	public boolean setSelectedInChrom(int overallBase, boolean sel, boolean repaint){
//		why is this called an absurd number of times 
		if (selected != null && overallBase >= 0 && overallBase<selected.length) {
			if (selected[overallBase] == sel)
				return false;
			selected[overallBase] = sel;  //selected uses index in consensus
			if (repaint)
				repaint();
			return true;
		}
		return false;
	}
	/*--------------------------------------*/
	/** Given the overall base numbering, as given in overallBase, this method selects that base within this contig*/
	public void selectOverallBase(int overallBase){
		int consensusBase = contigDisplay.getContigBaseFromUniversalBase(overallBase);
//		int readBase = getReadBaseFromConsensusBase(consensusBase);
//		if (j <0 || j>= chromatogram.getTraceLength())
//		return;
		if (overallBase <0 || overallBase>=selected.length)
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
	/*...........................................................................*/
	protected int getY (int[] trace, int peakBottom, int posInChromatogram, double vertScale) {
		if (posInChromatogram<0 || posInChromatogram>=trace.length)
			return 0;
		if (shadowOffset != 0 && (posInChromatogram-shadowOffset>=0&&posInChromatogram-shadowOffset<A.length ))
			return peakBottom-(int)((trace[posInChromatogram] - shadowReduction*trace[posInChromatogram-shadowOffset]) / vertScale);
		else
			return peakBottom-(int)(trace[posInChromatogram] / vertScale);
	}
	/*...........................................................................*/
	protected void drawCurve(Graphics2D g, int width, int x, int y, int x2, int y2, int x3, int y3){
		QuadCurve2D quadCurve = null;
		if (isShownReversed()){
			quadCurve = new QuadCurve2D.Double(width-x,y,width-x2,y2,width-x3,y3);
			g.draw(quadCurve);
		}
		else {
			quadCurve = new QuadCurve2D.Double(x,y,x2,y2,x3,y3);
			g.draw(quadCurve);
		}
	}
	/*...........................................................................*/
	protected void drawCurve(Graphics2D g, int width, int posPrev, int pos, int posNext, int[] trace, int peakBottom, double vertScale, double horizScale, int firstReadLocation){
		int x1 = (int)((posPrev-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int x2 = (int)((pos-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int x3 = (int)((posNext-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int y1 = getY(trace,peakBottom,posPrev,vertScale);
		int y2 = getY(trace,peakBottom,pos,vertScale);
		int y3 = getY(trace,peakBottom,posNext,vertScale);
		drawCurve(g, width, x1,y1,x2,y2,x3,y3);
	}

	public static int SETREAD = 0;
	public static int CLOSEUPREAD = 0;
	/*...............................................................................................................*/
	/* to be used by subclasses to tell that panel touched */
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int ic = findUniversalBaseNumber(SETREAD, x); 
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
		else if (MesquiteEvent.commandOrControlKeyDown(modifiers)){ //ZQ: If this was intended as right click, now use .rightClick(modifiers) instead
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
					contigDisplay.scrollToUniversalBase(ic);
				contigDisplay.repaintPanels();
			}
		}
	}
	/*...............................................................................................................*/
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int ic = findUniversalBaseNumber(SETREAD,x); 
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
	/*...............................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels()){
			int cons = findConsensusBaseNumber(SETREAD,x);
			((ChromatogramTool)tool).dropped(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(contigDisplay.getFirstTouchedOverall())){
			if (!MesquiteInteger.isCombinable(contigDisplay.getSecondTouchedOverall()))
				contigDisplay.focusMatrixOnUniversalBases(contigDisplay.getFirstTouchedOverall(), MesquiteInteger.unassigned);
//				contigDisplay.focusMatrixOn(contigDisplay.getContigBaseFromUniversalBase(contigDisplay.getFirstTouchedOverall()), MesquiteInteger.unassigned);
			else
				contigDisplay.focusMatrixOnUniversalBases(contigDisplay.getFirstTouchedOverall(), contigDisplay.getSecondTouchedOverall());
//			contigDisplay.focusMatrixOn(contigDisplay.getContigBaseFromUniversalBase(contigDisplay.getFirstTouchedOverall()), contigDisplay.getContigBaseFromUniversalBase(contigDisplay.getSecondTouchedOverall()));
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
		int ic;
		int readBaseNumber;
		if (contigDisplay.contigExists()) {
			ic= findConsensusBaseNumber(SETREAD,x);
			readBaseNumber = reads[SETREAD].getReadBaseFromContigBase(ic);
		}
		else {
			ic = findUniversalBaseNumber(SETREAD, x); 
			readBaseNumber = ic;
		}
		if (closeupPanel!=null)
			closeupPanel.setReadBaseNumber(readBaseNumber);

		int quality = reads[SETREAD].getPhdBaseQuality(readBaseNumber);
		double averageQuality = reads[SETREAD].getAverageQuality();
		int numBasesHighQuality = reads[SETREAD].getNumBasesHighQuality();
		String s = "";
		if (quality>=0)
			s+= "Base quality: " + quality + ",   Peak heights: " + getPeakHeightsOfBase(SETREAD,readBaseNumber);
		s+= "\n# Bases with Quality ≥ " + reads[SETREAD].getNumBasesHighQualityThreshold() + ": " + numBasesHighQuality + ",  Average Quality: " + averageQuality + "  ("+chromatograms[SETREAD].getTitle()+")";
		contigDisplay.setExplanation( s);
		if (tool == null)
			return;
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (chromTool.getWorksOnlyOnSelection())
			if (!getSelected(findUniversalBaseNumber(SETREAD,x)))
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
	public ChromatogramCloseupPanel getCloseupPanel() {
		return closeupPanel;
	}
	public void setCloseupPanel(ChromatogramCloseupPanel closeupPanel) {
		this.closeupPanel = closeupPanel;
	}
}