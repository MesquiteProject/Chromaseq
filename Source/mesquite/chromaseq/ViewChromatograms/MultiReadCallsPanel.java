package mesquite.chromaseq.ViewChromatograms;

import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;

import java.awt.*;

public class MultiReadCallsPanel extends ChromatogramPanel {
	MultiReadCallsCanvas multiReadCanvas = null;

	public MultiReadCallsPanel(int id) {
		chromatogram.setWindow(panel);
		multiReadCanvas = new MultiReadCallsCanvas(this, id);
		//this.setLayout(new BorderLayout(0,0));
		multiReadCanvas.setBounds(0, 0, getWidth(), getHeight());
		this.add("Center",multiReadCanvas);
		if (chromatogram.getRead().getComplemented()) 
			setBackground(ColorDistribution.veryVeryLightGray);
		else
			setBackground(Color.white);
	}

}


//=======================================================================================

class MultiReadCallsCanvas extends ChromatogramCanvas {
	MultiReadCallsPanel chromatogramPanel;

	boolean blackBackground=false;
	boolean simplerGraphics=true;

	static final double NONSOURCEDIMFRACTION = 0.2;
	static final double NONSOURCELOWERQUALITYDIMFRACTION = 0.15;


	/*..........................*/
	public MultiReadCallsCanvas(MultiReadCallsPanel parentV, int id) {
		super(parentV,  id);
		chromatogramPanel = parentV;
		addKeyListener(panel);
	}

	/*...........................................................................*/
	public void paint(Graphics g) {	
		int v = -verticalPosition;
		Read read = chromatogram.getRead();
		int cheight = getBounds().height;
		int shadowHeight = 5;
		int labelHeight = 18;
		int labelBottom = v+cheight-shadowHeight;
		int labelTop = labelBottom-labelHeight+1;
		int cwidth = getBounds().width;
		double scale = 1.0*maxValue / cheight / magnification;
		int halfPeaks = panel.getApproximateNumberOfPeaksVisible()/2;
		if (blackBackground)
			setBackground(Color.black);
		else
			setBackground(panel.getBackgroundColor());

		reCalcCenterBase();
		int centerConsensusBase = centerBase-panel.getContig().getReadExcessAtStart();
		int centerReadBase = getReadBaseFromConsensusBase(centerConsensusBase);

		int firstReadBase = centerReadBase - halfPeaks;
		int lastReadBase = centerReadBase+halfPeaks;

		int firstReadLocation = getPhdLocation(read, cwidth, centerReadBase,panel,true) - cwidth/2;

		/*David: here a correction is introduced to discover what is actually the first and last read bases visible.  Previously
		these bases were calcualted incorrectly if some reads were effectively compressed, and thus drawing didn't go all the way to the edges

		A similar problem of compression was affecting findConsensusBaseNumber, hence some of this code is repeated there.

		 */
		int count = 0;
		while (getPhdLocation(read, cwidth, firstReadBase,panel,true)  - firstReadLocation >0 && firstReadLocation>0 && count++<200)
			firstReadBase--; //correcting for error in numpeaksvisible for this read
		count = 0;
		while (getPhdLocation(read, cwidth, lastReadBase,panel,true)  - firstReadLocation <cwidth && count++<200)
			lastReadBase++; //correcting for error in numpeaksvisible for this read
		firstReadBase--;
		lastReadBase++;

		int i;
		double fadeMax = 0.5;

		int offsetForInserted = 0;
		int firstSel = MesquiteInteger.unassigned;
		int cmid = 0;




		Color inverseBlackColor = Color.white;




//		=====================  DRAWING THE PEAKS ==============================
		int peakBottom = labelBottom - labelHeight; //+lines;
		int prevCons = 0;
		int lostSpaceByInsert = 0;
		int lastReadPos = MesquiteInteger.unassigned;
		int start= 0;
		int end = cwidth-2;




//		=====================  DRAWING THE READ NAME ==============================
		if (!StringUtil.blank(chromatogram.getTitle())) {
			if (chromatogram.getRead().getComplemented())
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			g.drawString(chromatogram.getTitle(),10,v+30);
		}

		VChromWindow window = (VChromWindow)chromatogramPanel.getMesquiteWindow();

		if (isShownComplemented())
			g.drawString("COMPLEMENTED", 10,v+64);

		offsetForInserted = 0;



//		=====================  COLOR THE BASES UNDER THE READ ==============================
		for (i=firstReadBase;i < chromatogram.getBaseNumber() && i<=lastReadBase;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getConsensusBaseFromReadBase(i);
				int space = panel.getSpaceInsertedBeforeConsensusBase(consensusBase);

				offsetForInserted += space;

				int overallBase = panel.getOverallBaseFromConsensusBase(consensusBase);
				char c = read.getPhdBaseChar(i);
				int qual = read.getPhdBaseQuality(i);

				cmid = getFontMetrics(g.getFont()).charWidth(c) / 2;
				int w = 0;
				if (i+1< chromatogram.getBaseNumber())
					//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
					w = getPhdLocation(read, cwidth, i+1, panel,true) - getPhdLocation(read, cwidth, i, panel,true) + 1;
				else
					w =  (int)panel.getAveragePeakDistance();
				if (qual>=0 && panel.getColorReadCallsByQuality()) {
					if (qual==0)
						g.setColor(Color.white);
					//g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.2));
					else
						g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.5));
					/*	char cc= panel.getMatrixStateAtConsensusPosition(consensusBase);
		Color tempC = panel.getBaseColor(cc);
		g.setColor(tempC);*/
					fillRect(g, cwidth, getPhdLocation(read, cwidth, i, panel,true)- firstReadLocation - cmid - 2 + offsetForInserted, labelTop, w, labelHeight);
				}



				if (selected[overallBase]){
					w = 0;
					if (i+1< chromatogram.getBaseNumber())
						//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
						w = getPhdLocation(read, cwidth, i+1, panel,true) - getPhdLocation(read, cwidth, i, panel,true) + 1;
					else
						w =  (int)panel.getAveragePeakDistance();
					g.setColor(Color.gray);
					fillRect(g, cwidth, getPhdLocation(read, cwidth, i, panel,true)- firstReadLocation - cmid - 2 + offsetForInserted, labelTop, w, labelHeight);
				}


			}
		}

		offsetForInserted = 0;
//		=====================  SHOW THE SELECTION ==============================
		firstSel = MesquiteInteger.unassigned;
		ColorDistribution.setTransparentGraphics(g);		
		g.setColor(Color.gray);
		cmid = 0;
		for (i=firstReadBase-1; i<=lastReadBase+1;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getConsensusBaseFromReadBase(i);
				offsetForInserted += panel.getSpaceInsertedBeforeConsensusBase(consensusBase);
				int overallBase = panel.getOverallBaseFromConsensusBase(consensusBase);

				char c = read.getPhdBaseChar(i);
				cmid = getFontMetrics(g.getFont()).charWidth(c) / 2;
				if (selected[overallBase]){

					if (firstSel == MesquiteInteger.unassigned)
						firstSel = getPhdLocation(read, cwidth, i, panel,true)- firstReadLocation - cmid - 2 + offsetForInserted;
				}
				else if (firstSel != MesquiteInteger.unassigned){
					int endOfSel = getPhdLocation(read, cwidth, i, panel,true)- firstReadLocation - cmid - 2 + offsetForInserted;

					fillRect(g, cwidth, firstSel, 0, endOfSel - firstSel, cheight);
					firstSel = MesquiteInteger.unassigned;
				}
			}
		}
		if (firstSel != MesquiteInteger.unassigned){ //unfinished selection
			int endOfSel = getPhdLocation(read, cwidth, lastReadBase+1, panel,true)- firstReadLocation - 2 + offsetForInserted;
			fillRect(g, cwidth, firstSel, 0, endOfSel - firstSel, cheight);
			firstSel = MesquiteInteger.unassigned;
		}
		ColorDistribution.setOpaqueGraphics(g);		

		offsetForInserted = 0;

//		=====================  DRAW THE BASES UNDER THE READ ==============================

		for (i=firstReadBase;i < chromatogram.getBaseNumber() && i<=lastReadBase;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getConsensusBaseFromReadBase(i);
				offsetForInserted += panel.getSpaceInsertedBeforeConsensusBase(consensusBase);
				int overallBase = panel.getOverallBaseFromConsensusBase(consensusBase);
				char c = read.getPhdBaseChar(i);
				int qual = read.getPhdBaseQuality(i);

				cmid = getFontMetrics(g.getFont()).charWidth(c) / 2;
				if (isShownComplemented()){
					c = DNAData.complementChar(c);
				}
				Color textC = panel.getBaseColor(c,panel.getBackgroundColor());
				int pixels = panel.getHorizontalPixels(getPhdLocation(read, cwidth, i, panel,true) - firstReadLocation) + offsetForInserted;

				if (selected[overallBase]){
					StringUtil.highlightString(g, String.valueOf(c), pixels - cmid, labelBottom-3, textC, Color.white);
				}
				else {
					if (c=='N') 
						g.setColor(Color.lightGray);
					else {
						if (textC.equals(Color.black) && blackBackground)
							g.setColor(inverseBlackColor);
						else
							g.setColor(textC);
						GraphicsUtil.setFontStyle(Font.BOLD,g);
					}
					GraphicsUtil.setFontSize(12,g);
					drawString(g, cwidth, ""+c,pixels - cmid, labelBottom-3);
					String aaa = chromatogram.getTitle();

//					Drawing primers					
					GraphicsUtil.setFontStyle(Font.PLAIN,g);
				}



			}
		}

//		=====================  DRAW THE BOTTOM LINES ==============================
		if (!simplerGraphics) {
			g.setColor(Color.black);
			g.drawLine(0,cheight-5,cwidth,v+cheight-5);
			g.drawLine(0,cheight-4,cwidth,v+cheight-4);
			g.setColor(Color.gray);
			g.drawLine(0,cheight-3,cwidth,v+cheight-3);
			g.setColor(Color.lightGray);
			g.drawLine(0,cheight-2,cwidth,v+cheight-2);
			g.setColor(ColorDistribution.veryLightGray);
			g.drawLine(0,cheight-1,cwidth,v+cheight-1);
		}
//		Debugg.println("end of paint, "+chromatogram.getTitle());
	}


	/*--------------------------------------*/
	/* to be used by subclasses to tell that panel touched */
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int ic = findOverallBaseNumber(x); 
		boolean onRequiredSelection = chromTool.getWorksOnlyOnSelection() && !getSelected(ic);
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels() &&!onRequiredSelection){
			int cons = findConsensusBaseNumber(x);
			((ChromatogramTool)tool).touched(cons, cons, true, id, chromatogramPanel.getContigID(), modifiers);
			/*		MesquiteWindow w = chromatogramPanel.getMesquiteWindow();
		if (w!=null)
			w.showQuickMessage(chromatogramPanel,null,x,y,"testing");
			 */
			return;
		}

		if (MesquiteEvent.shiftKeyDown(modifiers)){
			if (MesquiteInteger.isCombinable(ic)){
				panel.setSecondTouchedOverall(ic);
				chromatogramPanel.exportDeselectAll();
				if (MesquiteInteger.isCombinable(panel.getFirstTouchedOverall())) {
					if (panel.getFirstTouchedOverall()>ic){
						for (int i = ic; i<=panel.getFirstTouchedOverall(); i++)
							selectOverallBase(i);
					}
					else {
						for (int i = panel.getFirstTouchedOverall(); i<=ic; i++)
							selectOverallBase(i);
					}
				}
				else {
					selectOverallBase(ic);
				}
				//chromatogramPanel.synchTableToChrom(false);
				panel.repaintPanels();
			}
		}
		else if (MesquiteEvent.commandOrControlKeyDown(modifiers)){
			if (MesquiteInteger.isCombinable(ic)){
				selectOverallBase(ic);
				panel.repaintPanels();
			}
		}
		else {
			if (MesquiteInteger.isCombinable(ic)){
				panel.setFirstTouchedOverall(ic);
				chromatogramPanel.exportDeselectAll();
				selectOverallBase(ic);
				if (clickCount>1)
					panel.scrollToOverallBase(ic);
				panel.repaintPanels();
			}
		}
	}
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int ic = findOverallBaseNumber(x); 
		boolean onRequiredSelection = chromTool.getWorksOnlyOnSelection() && !getSelected(ic);
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels() && !onRequiredSelection){
			int cons = findConsensusBaseNumber(x);
			((ChromatogramTool)tool).dragged(cons, cons, true, id, chromatogramPanel.getContigID(), modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(ic)){
			//deselectAll(); //this isn't correct behaviour!  If shift down should remember previously sleected pieces
			if (MesquiteInteger.isCombinable(panel.getFirstTouchedOverall())) {
				if (panel.getFirstTouchedOverall()>ic){
					if (MesquiteInteger.isCombinable(panel.getSecondTouchedOverall()) && panel.getSecondTouchedOverall()<panel.getFirstTouchedOverall() && ic>panel.getSecondTouchedOverall()){ //retracting
						for (int i = panel.getSecondTouchedOverall()+1; i<=ic; i++) 
							deselectOverallBase(i);
					}
					else for (int i = ic; i<=panel.getFirstTouchedOverall(); i++) //adding
						selectOverallBase(i);
				}
				else {
					if (MesquiteInteger.isCombinable(panel.getSecondTouchedOverall()) && panel.getSecondTouchedOverall()>panel.getFirstTouchedOverall() && ic<panel.getSecondTouchedOverall()){ //retracting
						for (int i = ic+1; i<=panel.getSecondTouchedOverall(); i++) 
							deselectOverallBase(i);
					}
					else for (int i = panel.getFirstTouchedOverall(); i<=ic; i++)
						selectOverallBase(i);
				}
			}
			else {
				selectOverallBase(ic);
			}
			//chromatogramPanel.synchTableToChrom(false);
			panel.repaintPanels();
			panel.setSecondTouchedOverall(ic);
		}
	}
	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels()){
			int cons = findConsensusBaseNumber(x);
			((ChromatogramTool)tool).dropped(cons, cons, true, id, chromatogramPanel.getContigID(), modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(panel.getFirstTouchedOverall())){
			if (!MesquiteInteger.isCombinable(panel.getSecondTouchedOverall()))
				panel.focusMatrixOn(panel.getConsensusBaseFromOverallBase(panel.getFirstTouchedOverall()), MesquiteInteger.unassigned);
			else
				panel.focusMatrixOn(panel.getConsensusBaseFromOverallBase(panel.getFirstTouchedOverall()), panel.getConsensusBaseFromOverallBase(panel.getSecondTouchedOverall()));
		}
		if (chromatogramPanel.getScrollToTouched()) {
			int ic = findConsensusBaseNumber(x);
			if (ic>=0) {
				panel.scrollToConsensusBase(ic);
		//		panel.deselectAllReads();

			}
		}
		panel.setSecondTouchedOverall(MesquiteInteger.unassigned);
	}
	/*_________________________________________________*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int ic = findConsensusBaseNumber(x);
		int readBaseNumber = read.getReadBaseFromConsensusBase(ic);
		int quality = read.getPhdBaseQuality(readBaseNumber);
		double averageQuality = read.getAverageQuality();
		int numBasesHighQuality = read.getNumBasesHighQuality();
		String s = "";
		if (quality>=0)
			s+= "Base quality: " + quality + ",   Peak heights: " + getPeakHeightsOfBase(readBaseNumber);
		s+= "\n# Bases with Quality ³ " + read.getNumBasesHighQualityThreshold() + ": " + numBasesHighQuality + ",  Average Quality: " + averageQuality + "  ("+chromatogram.getTitle()+")";
		panel.setExplanation( s);
		if (tool == null)
			return;
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (chromTool.getWorksOnlyOnSelection())
			if (!getSelected(findOverallBaseNumber(x)))
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
