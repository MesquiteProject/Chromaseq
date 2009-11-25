/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.chromaseq.ViewChromatograms;

import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.chromaseq.lib.ChromatogramPanel;
import mesquite.chromaseq.lib.ChromatogramCanvas;

import java.awt.*;

public class MultiReadCallsPanel extends ChromatogramPanel {
	MultiReadCallsCanvas multiReadCanvas = null;
	boolean colorByQuality = true;

	public MultiReadCallsPanel(ClosablePanelContainer container, int id, ContigDisplay panel, Chromatogram[] chromatograms) {
//		chromatogram.setWindow(panel);
		this.chromatograms = chromatograms;
		this.contigDisplay = panel;
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

		addKeyListener(contigDisplay);
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
		int halfPeaks = contigDisplay.getApproximateNumberOfPeaksVisible()/2;
		if (blackBackground)
			setBackground(Color.black);
		else
			setBackground(contigDisplay.getBackgroundColor());

		reCalcCenterBase();
		int centerConsensusBase = centerBase-contigDisplay.getContig().getReadExcessAtStart();
		int centerReadBase = getReadBaseFromConsensusBase(whichRead,centerConsensusBase);

		int firstReadBase = centerReadBase - halfPeaks;
		int lastReadBase = centerReadBase+halfPeaks;

		int firstReadLocation = getPhdLocation(read, cwidth, centerReadBase,contigDisplay,true) - cwidth/2;

		/*David: here a correction is introduced to discover what is actually the first and last read bases visible.  Previously
		these bases were calcualted incorrectly if some reads were effectively compressed, and thus drawing didn't go all the way to the edges

		A similar problem of compression was affecting findConsensusBaseNumber, hence some of this code is repeated there.

		 */
		int count = 0;
		while (getPhdLocation(read, cwidth, firstReadBase,contigDisplay,true)  - firstReadLocation >0 && firstReadLocation>0 && count++<200)
			firstReadBase--; //correcting for error in numpeaksvisible for this read
		count = 0;
		while (getPhdLocation(read, cwidth, lastReadBase,contigDisplay,true)  - firstReadLocation <cwidth && count++<200)
			lastReadBase++; //correcting for error in numpeaksvisible for this read
		firstReadBase--;
		lastReadBase++;

		int i;

		//int offsetForInserted = 0;
		int firstSel = MesquiteInteger.unassigned;
		int cmid = 0;

		Color inverseBlackColor = Color.white;

		g.setColor(Color.lightGray);
		g.drawLine(cwidth/2,0,cwidth/2,v+cheight);


		VChromWindow window = (VChromWindow)chromatogramPanel.getMesquiteWindow();

		//	if (isShownComplemented())
		//v-spacer		g.drawString("COMPLEMENTED", 10,v+64);

		//offsetForInserted = 0;


//		=====================  COLOR THE BASES UNDER THE READ ==============================
		for (i=firstReadBase;i < chromatograms[whichRead].getBaseNumber() && i<=lastReadBase;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getContigBaseFromReadBase(whichRead,i);
				//int space = contigDisplay.getSpaceInsertedBeforeDisplayBase(consensusBase);

				//offsetForInserted += space;

				int overallBase = contigDisplay.getUniversalBaseFromContigBase(consensusBase);
				char c = read.getPhdBaseChar(i);
				int qual = read.getPhdBaseQuality(i);

				cmid = getFontMetrics(g.getFont()).charWidth(c) / 2;
				int w = 0;
				if (i+1< chromatograms[whichRead].getBaseNumber())
					//w = chromatogram.getReadBaseLocationAligned(i+1) - chromatogram.getReadBaseLocationAligned(i) + 1;
					w = getPhdLocation(read, cwidth, i+1, contigDisplay,true) - getPhdLocation(read, cwidth, i, contigDisplay,true) + 1;
				else
					w =  (int)contigDisplay.getAveragePeakDistance();
				if (qual>=0 && contigDisplay.getColorMultiReadByQuality()) {
					if (qual==0)
						g.setColor(Color.white);
					//g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.2));
					else
						g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.5));
					/*	char cc= panel.getMatrixStateAtConsensusPosition(consensusBase);
		Color tempC = panel.getBaseColor(cc);
		g.setColor(tempC);*/
					fillRect(g, cwidth, getPhdLocation(read, cwidth, i, contigDisplay,true)- firstReadLocation - cmid - 2, topOfRead, w, readBaseHeight);
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

	//	offsetForInserted = 0;
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

		
		offsetForInserted = 0;
*/
//		=====================  DRAW THE BASES UNDER THE READ ==============================

		for (i=firstReadBase;i < chromatograms[whichRead].getBaseNumber() && i<=lastReadBase;i++) {
			if (i>=0 && i<read.getBasesLength()) {
				int consensusBase = getContigBaseFromReadBase(whichRead,i);
		//		offsetForInserted += contigDisplay.getSpaceInsertedBeforeDisplayBase(consensusBase);
				int overallBase = contigDisplay.getUniversalBaseFromContigBase(consensusBase);
				char c = read.getPhdBaseChar(i);
				int qual = read.getPhdBaseQuality(i);

				cmid = getFontMetrics(g.getFont()).charWidth(c) / 2;
				if (isShownComplemented()){
					c = DNAData.complementChar(c);
				}
				Color textC = contigDisplay.getBaseColor(c,contigDisplay.getBackgroundColor());
				int pixels = contigDisplay.getHorizontalPixels(getPhdLocation(read, cwidth, i, contigDisplay,true) - firstReadLocation);

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
	 		Composite composite = ColorDistribution.getComposite(g);
			ColorDistribution.setTransparentGraphics(g,0.92f);		
			g.setColor(Color.white);
			g.fillRect(0,v-spacer-11, length+12, 12);
			ColorDistribution.setComposite(g,composite);		


			if (chromatograms[whichRead].getRead().getComplemented())
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			g.drawString(s,10,v-spacer-1);
		}

	}

	int localFirstTouched = MesquiteInteger.unassigned;
	/*--------------------------------------*/
	/* to be used by subclasses to tell that panel touched */
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int whichRead = findRead(y);
		if (whichRead<0) return;
		int ic = findUniversalBaseNumber(whichRead, x); 
		localFirstTouched = findConsensusBaseNumber(whichRead,x);
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
					contigDisplay.scrollToUniversalBase(ic);
				contigDisplay.repaintPanels();
			}
		}
	}
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		int whichRead = findRead(y);
		if (whichRead<0) return;
		int ic = findUniversalBaseNumber(whichRead,x); 
		boolean onRequiredSelection = chromTool.getWorksOnlyOnSelection() && !getSelected(ic);
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels() && !onRequiredSelection){
			int cons = findConsensusBaseNumber(whichRead,x);
			((ChromatogramTool)tool).dragged(cons, cons, true, id, chromatogramPanel.contigID, modifiers);
			return;
		}
		if (MesquiteInteger.isCombinable(ic)){
			//deselectAll(); //this isn't correct behaviour!  If shift down should remember previously sleected pieces
			if (MesquiteInteger.isCombinable(contigDisplay.getFirstTouchedOverall())) {
				if (contigDisplay.getFirstTouchedOverall()>ic){
					if (MesquiteInteger.isCombinable(contigDisplay.getSecondTouchedOverall()) && contigDisplay.getSecondTouchedOverall()<contigDisplay.getFirstTouchedOverall() && ic>contigDisplay.getSecondTouchedOverall()){ //retracting
						for (int i = contigDisplay.getSecondTouchedOverall(); i<=ic; i++) 
							deselectOverallBase(whichRead,i);
					}
					else for (int i = ic; i<=contigDisplay.getFirstTouchedOverall(); i++) //adding
						selectOverallBase(i);
				}
				else {
					if (MesquiteInteger.isCombinable(contigDisplay.getSecondTouchedOverall()) && contigDisplay.getSecondTouchedOverall()>contigDisplay.getFirstTouchedOverall() && ic<contigDisplay.getSecondTouchedOverall()){ //retracting
						for (int i = ic+1; i<=contigDisplay.getSecondTouchedOverall(); i++) 
							deselectOverallBase(whichRead,i);
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
		int whichRead = findRead(y);
		if (whichRead<0) return;
		if (!tool.isArrowTool() && chromTool.getWorksOnChromatogramPanels()){
			int cons = findConsensusBaseNumber(whichRead,x);
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
			int ic = findConsensusBaseNumber(whichRead,x);
			if (ic>=0 && ic==localFirstTouched) {
				contigDisplay.scrollToConsensusBase(ic);
				//		panel.deselectAllReads();

			}
		}
		contigDisplay.setSecondTouchedOverall(MesquiteInteger.unassigned);
	}
	/*_________________________________________________*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		int whichRead = findRead(y);
		if (whichRead<0) return;
		int ic = findConsensusBaseNumber(whichRead,x);
		int readBaseNumber = reads[whichRead].getReadBaseFromContigBase(ic);
		int quality = reads[whichRead].getPhdBaseQuality(readBaseNumber);
		double averageQuality = reads[whichRead].getAverageQuality();
		int numBasesHighQuality = reads[whichRead].getNumBasesHighQuality();
		String s = "";
		if (quality>=0)
			s+= "Base quality: " + quality + ",   Peak heights: " + getPeakHeightsOfBase(whichRead,readBaseNumber);
		s+= "\n# Bases with Quality ³ " + reads[whichRead].getNumBasesHighQualityThreshold() + ": " + numBasesHighQuality + ",  Average Quality: " + averageQuality + "  ("+chromatograms[whichRead].getTitle()+")";
		contigDisplay.setExplanation( s);
		if (tool == null)
			return;
		ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (chromTool.getWorksOnlyOnSelection())
			if (!getSelected(findUniversalBaseNumber(whichRead,x)))
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
