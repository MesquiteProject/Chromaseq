package mesquite.chromaseq.ViewChromatograms;

import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;

import java.awt.*;

public class MultiReadCallsPanel extends ChromatogramPanel {
	MultiReadCallsCanvas multiReadCanvas = null;
	boolean colorByQuality = true;

	public MultiReadCallsPanel(ClosablePanelContainer container, int id, ContigDisplay panel, Chromatogram[] chromatograms) {
//		chromatogram.setWindow(panel);
		this.chromatograms = chromatograms;
		this.panel = panel;
		this.numChromatograms = chromatograms.length;
		multiReadCanvas = new MultiReadCallsCanvas(this, id);
		multiReadCanvas.setBounds(0, 0, getWidth(), getHeight());
		this.add("Center",multiReadCanvas);
		multiReadCanvas.setVisible(true);

	}

	public void repaintPanel(){
		if (multiReadCanvas != null ) {
			multiReadCanvas.repaint();
		}
	}
	public  void setBounds(int x, int y, int width, int height) {
		super.setBounds(x,y,width,height);
		this.width = width;
		this.height = height;
		multiReadCanvas.setBounds(0, 0, getWidth(), getHeight());
		//chromArea.repaint();
	}
	public  void setSize( int width, int height) {
		super.setSize(width,height);
		this.width = width;
		this.height = height;
		multiReadCanvas.setBounds(0, 0, getWidth(), getHeight());
		multiReadCanvas.repaint();
	}
	public void centerPanelAtOverallPosition(int i){
		centerBase = i;
		multiReadCanvas.repaint();
	}

	public boolean getColorByQuality() {
		return colorByQuality;
	}

	public void setColorByQuality(boolean colorByQuality) {
		this.colorByQuality = colorByQuality;
	}
	public int getAllReadHeight() {
		return numChromatograms * (MultiReadCallsCanvas.readBaseHeight + MultiReadCallsCanvas.spacer);
	}

	public int getRequestedHeight(int width){
		if (open) {
			return getAllReadHeight()+30;
		}
		else
			return ClosablePanel.MINHEIGHT;
	}

}


//=======================================================================================

class MultiReadCallsCanvas extends ChromatogramCanvas {
//	MultiReadCallsPanel chromatogramPanel;
	static int readBaseHeight = 18;
	static int spacer = 2;


	boolean blackBackground=false;
	boolean simplerGraphics=true;
	int count = 0;

	static final double NONSOURCEDIMFRACTION = 0.2;
	static final double NONSOURCELOWERQUALITYDIMFRACTION = 0.15;


	/*..........................*/
	public MultiReadCallsCanvas(ChromatogramPanel parentV, int id) {
		super(parentV,  id);
//		chromatogramPanel = parentV;

		addKeyListener(panel);
	}

	public boolean canShowTraces() {
		return false;
	}

	public  void setBounds(int x, int y, int width, int height) {
		super.setBounds(x,y,width,height);
	}
	/*...........................................................................*/
	public void paint(Graphics g) {	
		for (int i = 0; i<numChromatograms; i++) 
			paintRead(g,i);

			int cheight = getBounds().height;
			int cwidth = getBounds().width;
			g.setColor(ColorDistribution.veryLightGray);
			g.fillRect(0,cheight-3,cwidth,3);

	}	
	/*...........................................................................*/
	public int findRead(int y) {
		for (int i = 0; i<numChromatograms; i++) {
			if (y>=getTopOfRead(i) && y<=getTopOfRead(i)+readBaseHeight)
				return i;
		}

		return -1;
	}

	/*...........................................................................*/
	public int getTopOfRead(int whichRead) {
		int allReadHeight = numChromatograms*(readBaseHeight+spacer);
		int topOfReads = (getBounds().height - allReadHeight)/2-4;
		int bottomOfRead = topOfReads + (int)(whichRead+1)*(readBaseHeight+spacer)-spacer;
		return  bottomOfRead-readBaseHeight;

	}

	/*...........................................................................*/
	public void paintRead(Graphics g, int whichRead) {	
		int topOfRead = getTopOfRead(whichRead);
		int bottomOfRead = topOfRead+readBaseHeight;
		int v = bottomOfRead;
		int cheight = bottomOfRead;

		int cwidth = getBounds().width;

		
		Read read = chromatograms[whichRead].getRead();
		int halfPeaks = panel.getApproximateNumberOfPeaksVisible()/2;
		if (blackBackground)
			setBackground(Color.black);
		else
			setBackground(panel.getBackgroundColor());

		reCalcCenterBase();
		int centerConsensusBase = centerBase-panel.getContig().getReadExcessAtStart();
		int centerReadBase = getReadBaseFromConsensusBase(whichRead,centerConsensusBase);

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

		int offsetForInserted = 0;
		int firstSel = MesquiteInteger.unassigned;
		int cmid = 0;

		Color inverseBlackColor = Color.white;

		g.setColor(Color.lightGray);
		g.drawLine(cwidth/2,0,cwidth/2,v+cheight);


		VChromWindow window = (VChromWindow)chromatogramPanel.getMesquiteWindow();

		//	if (isShownComplemented())
		//v-spacer		g.drawString("COMPLEMENTED", 10,v+64);

		offsetForInserted = 0;


//		=====================  COLOR THE BASES UNDER THE READ ==============================
		for (i=firstReadBase;i < chromatograms[whichRead].getBaseNumber() && i<=lastReadBase;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getConsensusBaseFromReadBase(whichRead,i);
				int space = panel.getSpaceInsertedBeforeConsensusBase(consensusBase);

				offsetForInserted += space;

				int overallBase = panel.getOverallBaseFromConsensusBase(consensusBase);
				char c = read.getPhdBaseChar(i);
				int qual = read.getPhdBaseQuality(i);

				cmid = getFontMetrics(g.getFont()).charWidth(c) / 2;
				int w = 0;
				if (i+1< chromatograms[whichRead].getBaseNumber())
					//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
					w = getPhdLocation(read, cwidth, i+1, panel,true) - getPhdLocation(read, cwidth, i, panel,true) + 1;
				else
					w =  (int)panel.getAveragePeakDistance();
				if (qual>=0 && panel.getColorMultiReadByQuality()) {
					if (qual==0)
						g.setColor(Color.white);
					//g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.2));
					else
						g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.5));
					/*	char cc= panel.getMatrixStateAtConsensusPosition(consensusBase);
		Color tempC = panel.getBaseColor(cc);
		g.setColor(tempC);*/
					fillRect(g, cwidth, getPhdLocation(read, cwidth, i, panel,true)- firstReadLocation - cmid - 2 + offsetForInserted, topOfRead, w, readBaseHeight);
				}

/*

				if (selected[overallBase]){
					w = 0;
					if (i+1< chromatograms[whichRead].getBaseNumber())
						//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
						w = getPhdLocation(read, cwidth, i+1, panel,true) - getPhdLocation(read, cwidth, i, panel,true) + 1;
					else
						w =  (int)panel.getAveragePeakDistance();
					g.setColor(Color.gray);
					fillRect(g, cwidth, getPhdLocation(read, cwidth, i, panel,true)- firstReadLocation - cmid - 2 + offsetForInserted, topOfRead, w, readBaseHeight);
				}
*/

			}
		}

		offsetForInserted = 0;
/*		=====================  SHOW THE SELECTION ==============================
		firstSel = MesquiteInteger.unassigned;
		ColorDistribution.setTransparentGraphics(g);		
		g.setColor(Color.gray);
		cmid = 0;
		for (i=firstReadBase-1; i<=lastReadBase+1;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getConsensusBaseFromReadBase(whichRead,i);
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
*/
		
		offsetForInserted = 0;

//		=====================  DRAW THE BASES UNDER THE READ ==============================

		for (i=firstReadBase;i < chromatograms[whichRead].getBaseNumber() && i<=lastReadBase;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getConsensusBaseFromReadBase(whichRead,i);
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

				//if (selected[overallBase]){
				//	StringUtil.highlightString(g, String.valueOf(c), pixels - cmid, bottomOfRead, textC, Color.white);
				//}
				//else {
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
					drawString(g, cwidth, ""+c,pixels - cmid, bottomOfRead-3);
					String aaa = chromatograms[whichRead].getTitle();

//					Drawing primers					
					GraphicsUtil.setFontStyle(Font.PLAIN,g);
				//}



			}
		}
//		=====================  DRAWING THE READ NAME ==============================
		String s = chromatograms[whichRead].getTitle();
		if (!StringUtil.blank(s)) {
			GraphicsUtil.setFontSize(10,g);
			//GraphicsUtil.setFontStyle(Font.BOLD, g);
			int length = getFontMetrics(g.getFont()).stringWidth(s);
			ColorDistribution.setTransparentGraphics(g,0.92f);		
			g.setColor(Color.white);
			g.fillRect(0,v-spacer-11, length+12, 12);
			ColorDistribution.setOpaqueGraphics(g);		


			if (chromatograms[whichRead].getRead().getComplemented())
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			g.drawString(s,10,v-spacer-1);
		}

	}


	/*--------------------------------------*/
	/* to be used by subclasses to tell that panel touched */
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int whichRead = findRead(y);
		if (whichRead<0) return;
		int ic = findOverallBaseNumber(whichRead, x); 
		boolean onRequiredSelection = chromTool.getWorksOnlyOnSelection() && !getSelected(ic);
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels() &&!onRequiredSelection){
			int cons = findConsensusBaseNumber(whichRead,x);
			((ChromatogramTool)tool).touched(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
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
		int whichRead = findRead(y);
		if (whichRead<0) return;
		int ic = findOverallBaseNumber(whichRead,x); 
		boolean onRequiredSelection = chromTool.getWorksOnlyOnSelection() && !getSelected(ic);
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels() && !onRequiredSelection){
			int cons = findConsensusBaseNumber(whichRead,x);
			((ChromatogramTool)tool).dragged(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(ic)){
			//deselectAll(); //this isn't correct behaviour!  If shift down should remember previously sleected pieces
			if (MesquiteInteger.isCombinable(panel.getFirstTouchedOverall())) {
				if (panel.getFirstTouchedOverall()>ic){
					if (MesquiteInteger.isCombinable(panel.getSecondTouchedOverall()) && panel.getSecondTouchedOverall()<panel.getFirstTouchedOverall() && ic>panel.getSecondTouchedOverall()){ //retracting
						for (int i = panel.getSecondTouchedOverall()+1; i<=ic; i++) 
							deselectOverallBase(whichRead,i);
					}
					else for (int i = ic; i<=panel.getFirstTouchedOverall(); i++) //adding
						selectOverallBase(i);
				}
				else {
					if (MesquiteInteger.isCombinable(panel.getSecondTouchedOverall()) && panel.getSecondTouchedOverall()>panel.getFirstTouchedOverall() && ic<panel.getSecondTouchedOverall()){ //retracting
						for (int i = ic+1; i<=panel.getSecondTouchedOverall(); i++) 
							deselectOverallBase(whichRead,i);
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
		int whichRead = findRead(y);
		if (whichRead<0) return;
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels()){
			int cons = findConsensusBaseNumber(whichRead,x);
			((ChromatogramTool)tool).dropped(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(panel.getFirstTouchedOverall())){
			if (!MesquiteInteger.isCombinable(panel.getSecondTouchedOverall()))
				panel.focusMatrixOn(panel.getConsensusBaseFromOverallBase(panel.getFirstTouchedOverall()), MesquiteInteger.unassigned);
			else
				panel.focusMatrixOn(panel.getConsensusBaseFromOverallBase(panel.getFirstTouchedOverall()), panel.getConsensusBaseFromOverallBase(panel.getSecondTouchedOverall()));
		}
		if (chromatogramPanel.getScrollToTouched()) {
			int ic = findConsensusBaseNumber(whichRead,x);
			if (ic>=0) {
				panel.scrollToConsensusBase(ic);
				//		panel.deselectAllReads();

			}
		}
		panel.setSecondTouchedOverall(MesquiteInteger.unassigned);
	}
	/*_________________________________________________*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int whichRead = findRead(y);
		if (whichRead<0) return;
		int ic = findConsensusBaseNumber(whichRead,x);
		int readBaseNumber = reads[whichRead].getReadBaseFromConsensusBase(ic);
		int quality = reads[whichRead].getPhdBaseQuality(readBaseNumber);
		double averageQuality = reads[whichRead].getAverageQuality();
		int numBasesHighQuality = reads[whichRead].getNumBasesHighQuality();
		String s = "";
		if (quality>=0)
			s+= "Base quality: " + quality + ",   Peak heights: " + getPeakHeightsOfBase(whichRead,readBaseNumber);
		s+= "\n# Bases with Quality ³ " + reads[whichRead].getNumBasesHighQualityThreshold() + ": " + numBasesHighQuality + ",  Average Quality: " + averageQuality + "  ("+chromatograms[whichRead].getTitle()+")";
		panel.setExplanation( s);
		if (tool == null)
			return;
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (chromTool.getWorksOnlyOnSelection())
			if (!getSelected(findOverallBaseNumber(whichRead,x)))
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
