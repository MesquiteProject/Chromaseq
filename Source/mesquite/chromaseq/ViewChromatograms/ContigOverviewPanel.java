package mesquite.chromaseq.ViewChromatograms;

import mesquite.categ.lib.*;
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
	int firstBase=0;
	int numBases ;
	int numBasesVisible ;


	public ContigOverviewPanel(ClosablePanelContainer container, int id, ContigDisplay panel, Chromatogram[] chromatograms) {
//		chromatogram.setWindow(panel);
		this.chromatograms = chromatograms;
		this.panel = panel;
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
		numBases = panel.getTotalNumPeaks();
		numBasesVisible = panel.getApproximateNumberOfPeaksVisible();

		scrollBar.setUnitIncrement(10);
		scrollPanel.add("South",scrollBar);
		scrollPanel.setVisible(true);
		scrollPanel.setBackground(Color.white);
		add(scrollPanel);
		setScrollPosition();

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
		numBases = panel.getTotalNumPeaks();
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
		if (i<firstBase) {
			if (i-2<0)
				positionOverview(i);
			else
				positionOverview(i-2);
		}
		else if (i>firstBase+numBasesVisible) {
			if (i+2>numBases)
				positionOverview(i-numBasesVisible);
			else 
				positionOverview(i-numBasesVisible+2);
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


	/*..........................*/
	public ContigOverviewCanvas(ChromatogramPanel parentV, int id) {
		super(parentV,  id);
//		chromatogramPanel = parentV;
		addKeyListener(panel);
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

		int halfPeaks = panel.getApproximateNumberOfPeaksVisible()/2;
		int centerConsensusBase = window.getCenterBase()-firstBase;//-panel.getContig().getReadExcessAtStart();
		int firstConsensusBase = centerConsensusBase-halfPeaks;
		//	firstConsensusBase = panel.getOverallBaseFromConsensusBase(firstConsensusBase);
		int lastConsensusBase = centerConsensusBase+halfPeaks;
		int left = getLeftBoundaryOfOverview(g);
		int top = getTopOfRead(0)-5;
		int bottom = getTopOfRead(numChromatograms-1)+readBaseHeight + 5;
		int width = panel.getApproximateNumberOfPeaksVisible();
		if (firstConsensusBase+panel.getApproximateNumberOfPeaksVisible()> panel.getTotalNumPeaks()){
			width =  panel.getTotalNumPeaks()-firstConsensusBase;
			if (width<0) width=0;
		}
		return new Rectangle(left+firstConsensusBase*singleBaseWidth, top, width*singleBaseWidth,bottom-top);
	}	

	/*...........................................................................*/
	public int getOverallBaseLocation(int overallBase, int left, Graphics g) {
		return left+(overallBase)*singleBaseWidth;
	}
	/*...........................................................................*/
	public int getBaseFromLocation(int location, int left, Graphics g) {
		return (location-left)/singleBaseWidth;
	}
	/*...........................................................................*/
	public int getOverallBaseFromLocation(int location, Graphics g) {
		int left = getLeftBoundaryOfOverview(g);
		return (location-left)/singleBaseWidth;
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
		int numBases = panel.getTotalNumPeaks();

		if (blackBackground)
			setBackground(Color.black);
		else
			setBackground(panel.getBackgroundColor());

		Color baseColor;

//		=====================  COLOR THE BASES OF THE READ ==============================
		int firstLocation = -1;
		int lastLocation = -1;
		for (int i=firstBase;i < numBases;i++) {
			int readBase = getReadBaseFromOverallBase(whichRead, i);
			if (readBase>=0 && readBase<read.getBasesLength()) {

				char c = read.getPhdBaseChar(readBase);
				char matrixC= panel.getMatrixStateAtConsensusPosition(read.getConsensusBaseFromReadBase(readBase));
				int qual = read.getPhdBaseQuality(readBase);
				
if (i>500 && i<520 && false) {
		int consensus = read.getConsensusBaseFromReadBase(readBase);
			Debugg.println("" + consensus +  "       " + c + "   " + matrixC + "   " + qual);
}

				if (qual>=0 && panel.getColorOverviewByQuality()) {
					if (qual==0)
						g.setColor(Color.black);
					else if (matrixC == c || matrixC=='X' || matrixC=='x' || matrixC=='-')
						g.setColor(ColorDistribution.brighter(AceFile.getColorOfQuality(qual),0.5));
					else 
						g.setColor(Color.red);
				}
				else  {
					baseColor = panel.getBaseColor(c,window.getBackgroundColor());
					
					g.setColor(baseColor);
				}
				if (firstLocation<0)
					firstLocation = left+(i)*singleBaseWidth;
				fillRect(g, cwidth, left+(i)*singleBaseWidth, topOfRead, singleBaseWidth, readBaseHeight);
				lastLocation = left+(i)*singleBaseWidth+singleBaseWidth;
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
				g.drawLine(firstLocation-2, topOfRead + readBaseHeight/2, firstLocation-2-lineLength, topOfRead + readBaseHeight/2);
				g.drawLine(firstLocation-2-lineLength, topOfRead + readBaseHeight/2, firstLocation-2-lineLength+readBaseHeight/2, topOfRead);
				g.drawLine(firstLocation-2-lineLength, topOfRead + readBaseHeight/2, firstLocation-2-lineLength+readBaseHeight/2, topOfRead+readBaseHeight);
			}
			g.drawLine(lastLocation+2, topOfRead + readBaseHeight/2, lastLocation+lineLength, topOfRead + readBaseHeight/2);
			g.drawLine(lastLocation+lineLength, topOfRead + readBaseHeight/2, lastLocation+lineLength+readBaseHeight/2, topOfRead);
			g.drawLine(lastLocation+lineLength, topOfRead + readBaseHeight/2, lastLocation+lineLength+readBaseHeight/2, topOfRead+readBaseHeight);
		} else {
			if (firstLocation>lineLength+getLeftBoundaryOfOverview(g)) {
				g.drawLine(firstLocation-2, topOfRead + readBaseHeight/2, firstLocation-lineLength, topOfRead + readBaseHeight/2);
				g.drawLine(firstLocation-lineLength, topOfRead + readBaseHeight/2, firstLocation-lineLength-readBaseHeight/2, topOfRead);
				g.drawLine(firstLocation-lineLength, topOfRead + readBaseHeight/2, firstLocation-lineLength-readBaseHeight/2, topOfRead+readBaseHeight);
			}
			g.drawLine(lastLocation+2, topOfRead + readBaseHeight/2, lastLocation+2+lineLength, topOfRead + readBaseHeight/2);
			g.drawLine(lastLocation+2+lineLength, topOfRead + readBaseHeight/2, lastLocation+2+lineLength-readBaseHeight/2, topOfRead);
			g.drawLine(lastLocation+2+lineLength, topOfRead + readBaseHeight/2, lastLocation+2+lineLength-readBaseHeight/2, topOfRead+readBaseHeight);

		}


	}

	
	
	boolean mouseDownInBox = false;
	int offsetInBox = 0;
//	int mouseDownRead = -1;
	/*--------------------------------------*/
	/* to be used by subclasses to tell that panel touched */
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		mouseDownInBox = false;
		int offsetInBox = 0;
		Rectangle box = getFieldOfView(getGraphics());
		if (box.contains(x,y) && window!=null) {
			mouseDownInBox = true;
			int centerOfBox = (box.x+box.width/2);
			offsetInBox = x-centerOfBox;

		} else {
			int ic = getOverallBaseFromLocation(x-offsetInBox,getGraphics()); 
			 ic = panel.getConsensusBaseFromOverallBase(ic)+firstBase;
			if (MesquiteInteger.isCombinable(ic)){
				panel.scrollToConsensusBase(ic);
				panel.repaintPanels();
			}
		}
	}
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
		//ChromatogramTool chromTool = (ChromatogramTool)tool;
		if (mouseDownInBox) {
			setCursor(window.getHandCursor());
			int numBases = panel.getTotalNumPeaks();
			int ic = getOverallBaseFromLocation(x-offsetInBox,getGraphics()); 
			int overallBase = ic+firstBase;
			int consensus = panel.getConsensusBaseFromOverallBase(ic)+firstBase;
			if (MesquiteInteger.isCombinable(consensus) && overallBase>=0 && overallBase<numBases){
				panel.scrollToConsensusBase(consensus);
				panel.repaintPanels();
			}
		}
	}
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