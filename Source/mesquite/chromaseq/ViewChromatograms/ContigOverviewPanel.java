/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.
Version 0.980   July 2010
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */


package mesquite.chromaseq.ViewChromatograms;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ContigOverviewPanel extends ChromatogramPanel implements AdjustmentListener{
	ContigOverviewCanvas contigOverviewCanvas = null;
	boolean colorByQuality = true;
	int scrollDepth = 16;
	Scrollbar scrollBar;
	MesquitePanel scrollPanel;
	int firstBase=MesquiteInteger.unassigned;
	int numBases ;
	int numBasesVisible ;


	public ContigOverviewPanel(ClosablePanelContainer container, int id, ContigDisplay panel, Chromatogram[] chromatograms) {
//		chromatogram.setWindow(panel);
		this.chromatograms = chromatograms;
		this.contigDisplay = panel;
		open = true;
		this.numChromatograms = chromatograms.length;
		contigOverviewCanvas = new ContigOverviewCanvas(this, id);
		contigOverviewCanvas.setBounds(0, 0, getWidth(), getHeight()-scrollDepth);
		this.add("Center",contigOverviewCanvas);
		contigOverviewCanvas.setVisible(true);

		scrollPanel = new MesquitePanel();
		scrollPanel.setBackground(Color.blue);
		scrollBar = new Scrollbar(Scrollbar.HORIZONTAL);
		scrollBar.addAdjustmentListener(this);
		numBases = panel.getTotalNumUniversalBases();
		numBasesVisible = panel.getApproximateNumberOfPeaksVisible();
		firstBase=numBases/2 - numBasesVisible/2;

		scrollBar.setUnitIncrement(10);
		scrollPanel.add("South",scrollBar);
		scrollPanel.setVisible(true);
		scrollPanel.setBackground(Color.white);
		add(scrollPanel);
		setScrollPosition();

	}
	
	public ContigDisplay getContigDisplay() {
		return contigDisplay;
	}

	public void repaintPanel(){
		if (contigOverviewCanvas != null ) {
			contigOverviewCanvas.repaint();
		}
	}
	public  void setBounds(int x, int y, int width, int height) {
		super.setBounds(x,y,width,height);
		this.width = width;
		this.height = height;
		resize();
		//chromArea.repaint();
	}
	public  void setScrollPosition( ){
		numBases = contigDisplay.getTotalNumUniversalBases();
		int left = contigOverviewCanvas.getLeftBoundaryOfOverview(contigOverviewCanvas.getGraphics());
		numBasesVisible = getBounds().width-left;

		scrollPanel.setBounds(left,getBounds().height-scrollDepth-1, getBounds().width-left,scrollDepth);
		scrollBar.setBounds(0,0, getBounds().width-left,scrollDepth);
		scrollBar.setValues(firstBase,numBasesVisible, 0, numBases); 
		scrollBar.setBlockIncrement(numBasesVisible/2);

	}
	public  void resize( ) {
		contigOverviewCanvas.setBounds(0, 0, getWidth(), getHeight()-scrollDepth);
		setScrollPosition();

		contigOverviewCanvas.repaint();
	}

	public  void setSize( int width, int height) {
		super.setSize(width,height);
		this.width = width;
		this.height = height;
		resize();
	}
	public void centerPanelAtOverallPosition(int i){
		centerBase = i;
		int boundary = contigDisplay.getApproximateNumberOfPeaksVisible()/2+10;
		if (i-boundary<firstBase) {   //contigDisplay.getApproximateNumberOfPeaksVisible()
			if (i-boundary<0)
				positionOverview(i);
			else
				positionOverview(i-boundary);
		}
		else if (i+boundary>firstBase+numBasesVisible) {
			if (i+boundary>numBases)
				positionOverview(i-numBasesVisible);
			else 
				positionOverview(i-numBasesVisible+boundary);
		}
		
		contigOverviewCanvas.repaint();
	
	}
	public void centerPanelAtUniversalBase(int i, boolean trueCenter){
		centerBase = i;
		if (trueCenter) {
			int newFirstBase = i=numBasesVisible/2;
			int boundary = contigDisplay.getApproximateNumberOfPeaksVisible()/2+10;
			if (newFirstBase-boundary<firstBase) {   //contigDisplay.getApproximateNumberOfPeaksVisible()
				newFirstBase=newFirstBase-boundary;
			}
			else if (newFirstBase+boundary>firstBase+numBasesVisible) {
				if (newFirstBase+boundary>numBases)
					newFirstBase = newFirstBase-numBasesVisible;
				else 
					newFirstBase = newFirstBase-numBasesVisible+boundary;

			}
			positionOverview(newFirstBase);
		} else {
			int boundary = contigDisplay.getApproximateNumberOfPeaksVisible()/2+10;
			if (i-boundary<firstBase) {   //contigDisplay.getApproximateNumberOfPeaksVisible()
				if (i-boundary<0)
					positionOverview(i);
				else
					positionOverview(i-boundary);
			}
			else if (i+boundary>firstBase+numBasesVisible) {
				if (i+boundary>numBases)
					positionOverview(i-numBasesVisible);
				else 
					positionOverview(i-numBasesVisible+boundary);

			}
		}
		
		contigOverviewCanvas.repaint();

	}

	public boolean getColorByQuality() {
		return colorByQuality;
	}

	public void setColorByQuality(boolean colorByQuality) {
		this.colorByQuality = colorByQuality;
	}
	public int getAllReadHeight() {
		return numChromatograms * (ContigOverviewCanvas.readBaseHeight + ContigOverviewCanvas.spacer);
	}
	public int getRequestedHeight(int width){
		if (open) {
			return getAllReadHeight()+28 + scrollDepth;
		}
		else
			return ClosablePanel.MINHEIGHT;
	}
	public void positionOverview(int pos){
		firstBase=pos;

		contigOverviewCanvas.setPosition(firstBase);
		contigOverviewCanvas.repaint();
	}

	public void adjustmentValueChanged(AdjustmentEvent evt) {
		if(evt.getAdjustable() == scrollBar) {
			switch(evt.getAdjustmentType()) {
			case AdjustmentEvent.UNIT_DECREMENT:
			case AdjustmentEvent.UNIT_INCREMENT:
			case AdjustmentEvent.BLOCK_INCREMENT:
			case AdjustmentEvent.BLOCK_DECREMENT:
			case AdjustmentEvent.TRACK:
				positionOverview(evt.getValue());
				break;
			}
		}
	}


}

//=======================================================================================

class ContigOverviewCanvas extends ChromatogramCanvas {
//	MultiReadCallsPanel chromatogramPanel;
	static int readBaseHeight = 7;
	static int spacer = 2;

	VChromWindow window = null;
	int firstBase = 0;

	boolean blackBackground=false;
	boolean simplerGraphics=true;
	int count = 0;

	static final double NONSOURCEDIMFRACTION = 0.2;
	static final double NONSOURCELOWERQUALITYDIMFRACTION = 0.15;
	static Color CONFLICTCOLOR = Color.red;


	/*..........................*/
	public ContigOverviewCanvas(ChromatogramPanel parentV, int id) {
		super(parentV,  id);
//		chromatogramPanel = parentV;
		addKeyListener(contigDisplay);
	}

	public boolean canShowTraces() {
		return false;
	}
	public void setPosition(int pos) {
		firstBase = pos;
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
		topOfReads=15;
		int bottomOfRead = topOfReads + (int)(whichRead+1)*(readBaseHeight+spacer)-spacer;
		return  bottomOfRead-readBaseHeight;

	}
	/*...........................................................................*/
	public int getMaximumReadNameWidth (Graphics g) {
		int max = 0;
		if (g==null)
			return 0;
		for (int i = 0; i<numChromatograms; i++) {
			String s = chromatograms[i].getTitle();
			int length = 0;
			if (!StringUtil.blank(s)) {
				GraphicsUtil.setFontSize(9,g);
				//	GraphicsUtil.setFontStyle(Font.BOLD, g);
				length = getFontMetrics(g.getFont()).stringWidth(s);
			}
			if (max<length)
				max=length;
		}
		return max;
	}
	/*...........................................................................*/
	public int getLeftBoundaryOfOverview (Graphics g) {
		return getMaximumReadNameWidth(g)+12;
	}


	int singleBaseWidth = 1;

	/*...........................................................................*/
	public Rectangle getFieldOfView(Graphics g) {	
		window = (VChromWindow)chromatogramPanel.getMesquiteWindow();

		int halfPeaks = contigDisplay.getApproximateNumberOfPeaksVisible()/2;
		int centerUniversalBase = window.getCenterBase();//-firstBase;//-panel.getContig().getReadExcessAtStart();
		int firstUniversalBase = centerUniversalBase-halfPeaks;
		int lastUniversalBase = centerUniversalBase+halfPeaks;
		//	firstUniversalBase = panel.getOverallBaseFromUniversalBase(firstUniversalBase);
		int left = getLeftBoundaryOfOverview(g);
		int top = getTopOfRead(0)-5;
		int bottom = getTopOfRead(numChromatograms-1)+readBaseHeight + 5;
		int width = contigDisplay.getApproximateNumberOfPeaksVisible();
/*		if (firstUniversalBase+contigDisplay.getApproximateNumberOfPeaksVisible()> contigDisplay.getTotalNumUniversalBases()){
			width =  contigDisplay.getTotalNumUniversalBases()-firstUniversalBase;
			if (width<0) width=0;
		}
	*/	
		int leftBoundary = getLeftBoundaryOfOverview(g)-firstBase*singleBaseWidth;
		int cwidth = getBounds().width-leftBoundary;
		int pixelWidth = width*singleBaseWidth;
		if (contigDisplay.isShownReversed()){
			int leftPixel = left+(firstUniversalBase-firstBase)*singleBaseWidth;
			return new Rectangle(cwidth-(leftPixel+pixelWidth), top, pixelWidth,bottom-top);
		}
		else {
			int leftPixel = left+(firstUniversalBase-firstBase)*singleBaseWidth;
			return new Rectangle(leftPixel, top, pixelWidth,bottom-top);
		}
	}	
	protected void fillRect(Graphics g, int width, int x, int y, int w, int h){
		if (isShownReversed()){
			g.fillRect(width-(x+w), y, w, h);
		}
		else {
			g.fillRect(x, y, w, h);
		}
	}


	/*...........................................................................*/
	public int getUniversalBaseFromLocation(int location, Graphics g) {
		int left = getLeftBoundaryOfOverview(g);
		if (contigDisplay.isShownReversed())
			return contigDisplay.getTotalNumUniversalBases()-((location-left)/singleBaseWidth)-firstBase;
		else
			return (location-left)/singleBaseWidth+firstBase;
	}
	/*...........................................................................*/
	public void paint(Graphics g) {	
		window = (VChromWindow)chromatogramPanel.getMesquiteWindow();

		for (int i = 0; i<numChromatograms; i++) 
			paintRead(g,i);

		int cheight = getBounds().height;
		int cwidth = getBounds().width;
		g.setColor(ColorDistribution.veryLightGray);
		g.fillRect(0,cheight-3,cwidth,3);

		g.setColor(Color.gray);
		Rectangle box = getFieldOfView(g);
		int left = getLeftBoundaryOfOverview(g);
		if (box.x<left) {
			box.width = box.width - (left-box.x);
			box.x = left;
		} 
		if (box.width>0)
			g.drawRect(box.x, box.y, box.width, box.height);

	}	
	
	/*...........................................................................*/
	public void paintRead(Graphics g, int whichRead) {	
		int topOfRead = getTopOfRead(whichRead);
		int bottomOfRead = topOfRead+readBaseHeight;

		int left = getLeftBoundaryOfOverview(g)-firstBase*singleBaseWidth;
		int cwidth = getBounds().width-left;
		
		Read read = chromatograms[whichRead].getRead();
		int numBases = contigDisplay.getTotalNumUniversalBases();
		ChromaseqUniversalMapper universalMapper = contigDisplay.getUniversalMapper();

		if (blackBackground)
			setBackground(Color.black);
		else
			setBackground(contigDisplay.getBackgroundColor());

		Color baseColor;
		

//		=====================  COLOR THE BASES OF THE READ ==============================
		int firstLocation = -1;
		int lastLocation = -1;
		for (int universalBase=firstBase;universalBase < numBases;universalBase++) {   //as it now stands, this is the overallBase
			int readBase = getReadBaseFromUniversalBase(whichRead, universalBase);
			if (readBase>=0 && readBase<read.getBasesLength()) {
			//	if (contigDisplay.isReversedInEditedData())
			//		readBase =read.getBasesLength()-readBase-1; 
				int qual = read.getPhdBaseQuality(readBase);
				char c = read.getPhdBaseChar(readBase);
				char matrixC= contigDisplay.getMatrixStateAtUniversalBase(universalBase); //

				if (qual>=0 && contigDisplay.getColorOverviewByQuality()) {
					if (qual==0)
						g.setColor(Color.black);
					else if (matrixC == c || matrixC=='X' || matrixC=='x' || matrixC=='-')  // they match
						g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.5));
					else  // they don't match
						g.setColor(CONFLICTCOLOR);
				}
				else  {
					baseColor = contigDisplay.getBaseColor(c,window.getBackgroundColor());
					
					g.setColor(baseColor);
				}
				if (firstLocation<0)
					firstLocation = left+(universalBase)*singleBaseWidth;
				fillRect(g, cwidth, left+(universalBase)*singleBaseWidth, topOfRead, singleBaseWidth, readBaseHeight);
				lastLocation = left+(universalBase)*singleBaseWidth+singleBaseWidth;
			} 
		}



//		=====================  DRAWING THE READ NAME ==============================
		String s = chromatograms[whichRead].getTitle();
		if (!StringUtil.blank(s)) {
			GraphicsUtil.setFontSize(9,g);
			//	GraphicsUtil.setFontStyle(Font.BOLD, g);

			if (chromatograms[whichRead].getRead().getComplemented())
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			g.drawString(s,8,bottomOfRead-spacer+2);
		}
		
		final int lineLength = 8;
		g.setColor(Color.lightGray);
		if (chromatograms[whichRead].getRead().getComplemented()){
			if (firstLocation>lineLength+getLeftBoundaryOfOverview(g)) {
				drawLine(g, cwidth, firstLocation-2, topOfRead + readBaseHeight/2, firstLocation-2-lineLength, topOfRead + readBaseHeight/2);
				drawLine(g, cwidth, firstLocation-2-lineLength, topOfRead + readBaseHeight/2, firstLocation-2-lineLength+readBaseHeight/2, topOfRead);
				drawLine(g, cwidth, firstLocation-2-lineLength, topOfRead + readBaseHeight/2, firstLocation-2-lineLength+readBaseHeight/2, topOfRead+readBaseHeight);
			}
			drawLine(g, cwidth, lastLocation+2, topOfRead + readBaseHeight/2, lastLocation+lineLength, topOfRead + readBaseHeight/2);
			drawLine(g, cwidth, lastLocation+lineLength, topOfRead + readBaseHeight/2, lastLocation+lineLength+readBaseHeight/2, topOfRead);
			drawLine(g, cwidth, lastLocation+lineLength, topOfRead + readBaseHeight/2, lastLocation+lineLength+readBaseHeight/2, topOfRead+readBaseHeight);
		} else {
			if (firstLocation>lineLength+getLeftBoundaryOfOverview(g)) {
				drawLine(g, cwidth, firstLocation-2, topOfRead + readBaseHeight/2, firstLocation-lineLength, topOfRead + readBaseHeight/2);
				drawLine(g, cwidth, firstLocation-lineLength, topOfRead + readBaseHeight/2, firstLocation-lineLength-readBaseHeight/2, topOfRead);
				drawLine(g, cwidth, firstLocation-lineLength, topOfRead + readBaseHeight/2, firstLocation-lineLength-readBaseHeight/2, topOfRead+readBaseHeight);
			}
			drawLine(g, cwidth, lastLocation+2, topOfRead + readBaseHeight/2, lastLocation+2+lineLength, topOfRead + readBaseHeight/2);
			drawLine(g, cwidth, lastLocation+2+lineLength, topOfRead + readBaseHeight/2, lastLocation+2+lineLength-readBaseHeight/2, topOfRead);
			drawLine(g, cwidth, lastLocation+2+lineLength, topOfRead + readBaseHeight/2, lastLocation+2+lineLength-readBaseHeight/2, topOfRead+readBaseHeight);

		}


	}

	
	
	boolean mouseDownInBox = false;
	int offsetInBox = 0;
	/*_________________________________________________*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		mouseDownInBox = false;
		int offsetInBox = 0;
		Rectangle box = getFieldOfView(getGraphics());
		if (box.contains(x,y) && window!=null) {
			mouseDownInBox = true;
			int centerOfBox = (box.x+box.width/2);
			offsetInBox = x-centerOfBox;
			int universalBase = getUniversalBaseFromLocation(x-offsetInBox,getGraphics()); 
		} else {
			int numBases = contigDisplay.getUniversalMapper().getNumUniversalBases();
			int universalBase = getUniversalBaseFromLocation(x-offsetInBox,getGraphics()); 
			if (MesquiteInteger.isCombinable(universalBase) && universalBase>=0 && universalBase<numBases){
				contigDisplay.scrollToUniversalBase(universalBase);
				contigDisplay.repaintPanels();
			}
		}
	}
	/*_________________________________________________*/
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
		//ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (mouseDownInBox) {
			setCursor(window.getHandCursor());
			int numBases = contigDisplay.getUniversalMapper().getNumUniversalBases();
			int universalBase = getUniversalBaseFromLocation(x-offsetInBox,getGraphics()); 
			if (MesquiteInteger.isCombinable(universalBase) && universalBase>=0 && universalBase<numBases){
				contigDisplay.scrollToUniversalBase(universalBase);
				contigDisplay.repaintPanels();
			}
		}
	}
	/*_________________________________________________*/
	/* to be used by subclasses to tell that panel touched */
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		int whichRead = findRead(y);
		if (whichRead<0) return;
		setCursor(Cursor.getDefaultCursor());

	}
	/*_________________________________________________*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		Rectangle box = getFieldOfView(getGraphics());
		if (box.contains(x,y) && window!=null)
			setCursor(window.getHandCursor());
		else
			setCursor(Cursor.getDefaultCursor());
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