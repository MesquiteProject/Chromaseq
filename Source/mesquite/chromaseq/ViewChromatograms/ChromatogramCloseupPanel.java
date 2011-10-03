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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;

import mesquite.align.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.categ.lib.*;

public class ChromatogramCloseupPanel extends ChromatogramPanel{
	CloseupChromatogramCanvas chromCanvas;
	Color backgroundColor = Color.white;
	int id;
	boolean showA = true;
	boolean showC = true;
	boolean showG = true;
	boolean showT = true;
	int readBaseNumber;
	Chromatogram currentChromatogram = null;
	boolean showCenterBase = false;
	boolean showSourceBase = true;
	boolean showTouchedBase = false;
	int touchedBase = 0;


	public ChromatogramCloseupPanel(ClosablePanelContainer container, Chromatogram myChrom, ContigDisplay panel, int id, int contigID) {
		this.id = id;  // this is the read number
		this.contigID = contigID;  //this is the taxon number or other id for contig
		this.contigDisplay = panel;
		open=true;
		chromatograms = new Chromatogram[1];
		//	setChromatogram(myChrom);
		setChromatogram(null);
		setBackground(getBackgroundColor());
		backgroundColor = panel.getBackgroundColor();
		chromCanvas = new CloseupChromatogramCanvas(this, id);
		chromCanvas.setBounds(0, 0, getWidth(), getHeight());
		this.add("Center",chromCanvas);
		setThickTrace(true);
	}
	public void centerPanelAtOverallPosition(int i){
		centerBase = i;
		readBaseNumber = chromCanvas.getReadBaseFromUniversalBase(CloseupChromatogramCanvas.SINGLEREAD,centerBase);
		chromCanvas.setCenterReadBase(readBaseNumber);
		chromCanvas.repaint();
	}
	/*.................................................................................................................*/
	public void setChromatogram(Chromatogram myChrom) {
		if (myChrom==null){
			backgroundColor = contigDisplay.getBackgroundColor();
			chromatograms[0] = null;
			if (chromCanvas!=null){
				chromCanvas.setChromatograms(chromatograms);
			}
		}
		else {
			if (showCenterBase)
				backgroundColor = ChromaseqUtil.veryVeryLightBlue;
			else
				backgroundColor = contigDisplay.getBackgroundColor();
			chromatograms[0] = myChrom;
			chromatograms[0].setWindow(contigDisplay);
			if (chromCanvas!=null){
				chromCanvas.setChromatograms(chromatograms);
			}
		}
		setBackground(getBackgroundColor());
		currentChromatogram = chromatograms[0];
	}
	/*.................................................................................................................*/
	public boolean isCurrentChromatogram(Chromatogram chromatogram) {
		return (chromatogram.equals(chromatograms[0]));
	}

	/*_________________________________________________*/
	public void setCloseupPanel(boolean showCenterBase, Chromatogram newChromatogram, boolean centerPanel) {
		
		if (!getShowCenterBase() && getChromatogram()!=null)
			setChromatogram(null);

		setShowCenterBase(showCenterBase);

		Chromatogram chromatogram = getChromatogram();

		if (!newChromatogram.equals(chromatogram)) {
			BasicChromatogramPanel oldChromatogramPanel = contigDisplay.getChromatogramPanel(chromatogram);

			//repaint previous one
			setChromatogram(newChromatogram);
			if (oldChromatogramPanel!=null)
				oldChromatogramPanel.repaint();
			if (centerPanel)
				centerPanelAtOverallPosition(centerBase);

			BasicChromatogramPanel newChromatogramPanel = contigDisplay.getChromatogramPanel(newChromatogram);
			newChromatogramPanel.repaint();
		}

	}
	/*.................................................................................................................*/
	public boolean getShowCenterBase() {
		return showCenterBase;
	}
	public void setShowCenterBase(boolean showCenterBase) {
		this.showCenterBase = showCenterBase;
	}
	/*.................................................................................................................*/
	public boolean getShowTouchedBase() {
		return showTouchedBase;
	}
	public void setShowTouchedBase(boolean showTouchedBase) {
		this.showTouchedBase = showTouchedBase;
	}
	/*.................................................................................................................*/
	public boolean getShowSourceBase() {
		return showSourceBase;
	}
	public void setShowSourceBase(boolean showSourceBase) {
		this.showSourceBase = showSourceBase;
	}

	/*.................................................................................................................*/
	public int getTouchedBase() {
		return touchedBase;
	}
	public void setTouchedBase(int touchedBase) {
		this.touchedBase = touchedBase;
	}

	/*.................................................................................................................*/
	public void setShowA(boolean showA) {
		this.showA = showA;
	}
	/*.................................................................................................................*/
	public boolean getShowA() {
		return showA;
	}
	/*.................................................................................................................*/
	public void setShowC(boolean showC) {
		this.showC = showC;
	}
	/*.................................................................................................................*/
	public boolean getShowC() {
		return showC;
	}
	/*.................................................................................................................*/
	public void setShowG(boolean showG) {
		this.showG = showG;
	}
	/*.................................................................................................................*/
	public boolean getShowG() {
		return showG;
	}
	/*.................................................................................................................*/
	public void setShowT(boolean showT) {
		this.showT = showT;
	}
	/*.................................................................................................................*/
	public boolean getShowT() {
		return showT;
	}
	public void repaintPanel(){
		if (chromCanvas != null ) {
			chromCanvas.repaint();
		}
	}
	public  void setBounds(int x, int y, int width, int height) {
		super.setBounds(x,y,width,height);
		this.width = width;
		this.height = height;
		chromCanvas.setBounds(0, 0, getWidth(), getHeight());
		//chromArea.repaint();
	}
	public  void setSize( int width, int height) {
		super.setSize(width,height);
		this.width = width;
		this.height = height;
		chromCanvas.setBounds(0, 0, getWidth(), getHeight());
		chromCanvas.repaint();
	}
	public int getReadBaseNumber() {
		return readBaseNumber;
	}
	public void setReadBaseNumber(int readBaseNumber) {
		this.readBaseNumber = readBaseNumber;
		chromCanvas.setCenterReadBase(readBaseNumber);
	}
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}

/*-----------------------------*/
class CloseupChromatogramCanvas extends ChromatogramCanvas {

	boolean darkBackground=false;
	Color darkBackgroundColor = Color.gray;
	boolean simplerGraphics=true;
	int centerReadBase = MesquiteInteger.unassigned;

	static final double NONSOURCEDIMFRACTION = 0.2;
	static final double NONSOURCELOWERQUALITYDIMFRACTION = 0.15;


	/*..........................*/
	public CloseupChromatogramCanvas(ChromatogramCloseupPanel parentV, int id) {
		super(parentV,  id);
		closeupPanel = parentV;
		setBackgroundColor();

	}
	public void setCenterReadBase(int base) {
		centerReadBase = base;
		repaint();
	}
	/*
	protected void drawLine(Graphics g, int width, int x, int y, int x2, int y2){
		if (isShownReversed()){
			g.drawLine(width-x, y, width-x2, y2);
			if (closeupPanel.getThickTrace())
				g.drawLine(width-x, y-1, width-x2, y2-1);
		}
		else {
			g.drawLine(x, y, x2, y2);
			if (closeupPanel.getThickTrace())
				g.drawLine(x+1, y, x2+1, y2);
		}
	}
	protected void drawLine(Graphics g, int width, int posPrev, int pos, int posNext, int[] trace, int peakBottom, double vertScale, double horizScale, int firstReadLocation){
		int pixels = (int)((pos-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		drawLine(g, width, pixels,getX(trace,peakBottom,pos,vertScale),pixels+2,getX(trace,peakBottom,pos+2,vertScale));
	}
	 */
	public void setBackgroundColor() {
		if (closeupPanel!=null)
			setBackground(closeupPanel.getBackgroundColor());
		else
			setBackground(Color.white);
	}

	protected void drawBaseInfo(Graphics2D g, Color inverseBlackColor, long state, long complementState, String baseString, String complementString, int[] trace, int pos, int textLeft, int baseVert) {
		Color baseColor;
		if (isShownComplemented())
			baseColor = contigDisplay.getBaseColor(complementState, contigDisplay.getBackgroundColor());
		else 
			baseColor = contigDisplay.getBaseColor(state,contigDisplay.getBackgroundColor());
		if (baseColor.equals(Color.black) && darkBackground)
			baseColor = inverseBlackColor;
		g.setColor(baseColor);
		if (isShownComplemented()) 
			g.drawString(complementString+": " + trace[pos], textLeft,baseVert);
		else
			g.drawString(baseString+": " + trace[pos], textLeft,baseVert);
	}

	protected void drawBaseInfo(Graphics2D g, Color inverseBlackColor, long state, int pos, int textLeft, int baseVert) {
		if (state==DNAState.A) {
			drawBaseInfo(g,  inverseBlackColor, DNAState.A, DNAState.T, "A", "T", A, pos,  textLeft,  baseVert);
		}
		if (state==DNAState.C) {
			drawBaseInfo(g,  inverseBlackColor, DNAState.C, DNAState.G, "C", "G", C, pos,  textLeft,  baseVert);
		}
		if (state==DNAState.G) {
			drawBaseInfo(g,  inverseBlackColor, DNAState.G, DNAState.C, "G", "C", G, pos,  textLeft,  baseVert);
		}
		if (state==DNAState.T) {
			drawBaseInfo(g,  inverseBlackColor, DNAState.T, DNAState.A, "T", "A", T, pos,  textLeft,  baseVert);
		}
	}

	protected void drawAmbiguityInfo(Graphics2D g, long state, int textLeft, int ambiguityVert) {
		g.setColor(Color.gray);
		if (isShownComplemented()) 
			g.drawString(DNAState.toNEXUSString(DNAState.complement(state)), textLeft,ambiguityVert);
		else
			g.drawString(DNAState.toNEXUSString(state), textLeft,ambiguityVert);


	}

	/*...........................................................................*/

	public static final int SINGLEREAD = 0;
	static final int maximumPeakHeight = 600;

	/*...........................................................................*/
	public synchronized void paint(Graphics g) {	
		if (MesquiteWindow.checkDoomed(this)) 
			return;
		if (!(g instanceof Graphics2D) || chromatograms==null || chromatograms.length==0 || chromatograms[SETREAD]==null){
			setBackground(contigDisplay.getBackgroundColor());
			return;
		}

		int v = 0;//-verticalPosition;
		Graphics2D g2 = (Graphics2D)g;

		if (chromatograms==null || chromatograms.length==0 || chromatograms[SETREAD]==null)
			return;
		Read read = chromatograms[SETREAD].getRead();

		if (read.getComplemented())
			g.setColor(Color.red);
		else 
			g.setColor(Color.black);


		String readName = chromatograms[SETREAD].getTitle();

		if (StringUtil.notEmpty(readName))
			g.drawString(readName, 10,20);


		int cheight = getBounds().height;
		//		int shadowHeight = 5;
		//	int labelHeight = 18;
		//	ChromaseqUniversalMapper universalMapper = contigDisplay.getUniversalMapper();

		int cwidth = getBounds().width;
		double vertScale = 8.0;
		double horizScale = 2.0;
		int widthPerPeak = 40;
		int approximatePeaks = cwidth/widthPerPeak;
		int halfPeaks = approximatePeaks/2;
		if (halfPeaks<1) halfPeaks=1;
		//		closeupPanel.drawCloseupBox(halfPeaks);

		centerBase=contigDisplay.getCenterBase();
				
		if  (!MesquiteInteger.isCombinable(centerReadBase)) {
			if (contigDisplay.contigExists()) {		
				centerReadBase = getReadBaseFromUniversalBase(SINGLEREAD,centerBase);
				int universalBaseOfChromatogramStart = getUniversalBaseFromReadBase(SINGLEREAD,0);
				int universalBaseOfChromatogramEnd = getUniversalBaseFromReadBase(SINGLEREAD,read.getBasesLength()-1);


				/*		
			Debugg.println("\n\n" + read.getName() );
			Debugg.println("    +++++ centerBase: " + centerBase);
			Debugg.println("    +++++ universalBaseOfChromatogramStart: " + universalBaseOfChromatogramStart);
			Debugg.println("    +++++ universalBaseOfChromatogramEnd: " + universalBaseOfChromatogramEnd);
				 */

				if (MesquiteInteger.isCombinable(centerBase) && centerBase>=0 && (!MesquiteInteger.isCombinable(centerReadBase) || centerReadBase<0)) {  // find the nearest one that works
					if (centerBase>=universalBaseOfChromatogramStart && centerBase<universalBaseOfChromatogramEnd) {
						for (int i=centerBase-1; i>=0; i--) {
							centerReadBase = getReadBaseFromUniversalBase(SINGLEREAD,i);
							if (centerReadBase>=0) {
								break;
							}
						}
						if (centerReadBase<0)
							for (int i=centerBase+1; i<contigDisplay.getTotalNumUniversalBases(); i++) {
								centerReadBase = getReadBaseFromUniversalBase(SINGLEREAD,i);
								if (centerReadBase>=0) {
									break;
								}
							}
					}
				}


			} else{
				centerReadBase = centerBase;
			} 
		}

		int peakBottom = cheight - 70; //+lines;
		int peakTop = 0;
		if (peakBottom>maximumPeakHeight)
			peakBottom= maximumPeakHeight;

		int firstReadBase = centerReadBase - halfPeaks;
		int lastReadBase = centerReadBase+ halfPeaks;

		//contigDisplay.getCenterBase();
		//	contigDisplay.getApproximateNumberOfPeaksVisible();


		int maxHeight = 0;
		if (A==null || C==null || G==null || T == null)
			return;
		for (int i=0; i<A.length;i++) {
			if (A[i]>maxHeight)
				maxHeight = A[i];
		}
		for (int i=0; i<C.length;i++) {
			if (C[i]>maxHeight)
				maxHeight = C[i];
		}
		for (int i=0; i<G.length;i++) {
			if (G[i]>maxHeight)
				maxHeight = G[i];
		}
		for (int i=0; i<T.length;i++) {
			if (T[i]>maxHeight)
				maxHeight = T[i];
		}
		//	Debugg.println("maxHeight: " + maxHeight);
		//		Debugg.println("   getHeight: " + getHeight());

		vertScale=(int)(maxHeight/(peakBottom-peakTop));
		//	if (vertScale>0)
		//		maxHeight = (int)(maxHeight/vertScale);
		//	maxHeight = (int)(maxHeight/vertScale);


		int firstReadLocation = getPhdLocation(read, cwidth, firstReadBase,contigDisplay,true);
		int lastReadLocation = getPhdLocation(read, cwidth, lastReadBase,contigDisplay,true);
		if (lastReadLocation!=firstReadLocation)
			horizScale = (1.0*cwidth)/(lastReadLocation-firstReadLocation);
		//		int firstReadLocation = getPhdLocation(read, cwidth, centerReadBase,contigDisplay,true) - cwidth/2;

		/*		Debugg.println("    +++++ centerReadBase: " + centerReadBase);
		Debugg.println("    +++++ horizScale: " + horizScale);
		Debugg.println("    +++++ A.length: " + A.length);
		Debugg.println("    +++++ firstReadLocation: " + firstReadLocation + " lastReadLocation: " + lastReadLocation);

		 */	

		g.setColor(Color.lightGray);
		g2.setStroke(new BasicStroke(1));
		g2.drawLine(cwidth/2,0,cwidth/2, peakBottom);
		g2.drawLine(0,peakBottom,cwidth, peakBottom);
		g2.setStroke(new BasicStroke(2));



		Color inverseBlackColor = Color.white;

		//		=====================  DRAWING THE PEAKS ==============================
		int lostSpaceByInsert = 0;
		int lastReadPos = MesquiteInteger.unassigned;
		int previousPosInChromatogram = firstReadLocation;
		int previous2PosInChromatogram = firstReadLocation;
		int nextPosInChromatogram = firstReadLocation;
		Color baseColor;

		//	int half = firstReadLocation + (lastReadLocation-firstReadLocation)/2;

		for (int posInChromatogram=firstReadLocation;posInChromatogram <= lastReadLocation;posInChromatogram++) {   //this goes through the pixels that are to be displayed, and sees if any from this read are in here

			int ic = findConsensusBaseNumber(SINGLEREAD,posInChromatogram, firstReadBase, lastReadBase, firstReadLocation);
			//			if (!MesquiteInteger.isCombinable(ic)) Debugg.println("! ic " + ic);
			if (MesquiteInteger.isCombinable(ic)&&MesquiteInteger.isCombinable(firstReadLocation)){
				if (MesquiteInteger.isCombinable(lastReadPos))
					lostSpaceByInsert += posInChromatogram - lastReadPos -2;
				lastReadPos = posInChromatogram;
				//posInChromatogram -= lostSpaceByInsert;
				//				if (!(readPos>=0&&readPos+2<A.length )) Debugg.println("! ic " + ic + "  " + readPos + "  " + A.length + " firstReadLocation " + firstReadLocation);


				if (MesquiteInteger.isCombinable(previousPosInChromatogram) && previousPosInChromatogram>=0 && MesquiteInteger.isCombinable(posInChromatogram) && (posInChromatogram>=0&&posInChromatogram+2<A.length )) {  //is it within bounds of read?
					int minPeakHeightToDraw = 3;
					if (A[posInChromatogram]>minPeakHeightToDraw || C[posInChromatogram]>minPeakHeightToDraw || G[posInChromatogram]>minPeakHeightToDraw || T[posInChromatogram]>minPeakHeightToDraw){

						nextPosInChromatogram = posInChromatogram+1;
						if (posInChromatogram>lastReadLocation)
							posInChromatogram=lastReadLocation;


						if (closeupPanel.getShowA()) {
							if (isShownComplemented())
								baseColor = contigDisplay.getBaseColor(DNAState.T, contigDisplay.getBackgroundColor());
							else 
								baseColor = contigDisplay.getBaseColor(DNAState.A,contigDisplay.getBackgroundColor());
							if (baseColor.equals(Color.black) && darkBackground)
								baseColor = inverseBlackColor;
							g.setColor(baseColor);
							drawCurve(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,A,peakBottom, vertScale, horizScale, firstReadLocation);
							//	Debugg.println("A: " + previousPosInChromatogram + " " + posInChromatogram + " " + nextPosInChromatogram);
						}
						if (closeupPanel.getShowC()) {
							if (isShownComplemented())
								baseColor = contigDisplay.getBaseColor(DNAState.G,contigDisplay.getBackgroundColor());
							else 
								baseColor = contigDisplay.getBaseColor(DNAState.C,contigDisplay.getBackgroundColor());
							if (baseColor.equals(Color.black) && darkBackground)
								baseColor = inverseBlackColor;
							g.setColor(baseColor);
							drawCurve(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,C,peakBottom, vertScale,  horizScale, firstReadLocation);
							//	Debugg.println("C: " + previousPosInChromatogram + " " + posInChromatogram + " " + nextPosInChromatogram);
						}
						if (closeupPanel.getShowG()) {
							if (isShownComplemented())
								baseColor = contigDisplay.getBaseColor(DNAState.C,contigDisplay.getBackgroundColor());
							else 
								baseColor = contigDisplay.getBaseColor(DNAState.G,contigDisplay.getBackgroundColor());
							if (baseColor.equals(Color.black) && darkBackground)
								baseColor = inverseBlackColor;
							g.setColor(baseColor);
							drawCurve(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,G,peakBottom, vertScale, horizScale, firstReadLocation);
							//	Debugg.println("G: " + previousPosInChromatogram + " " + posInChromatogram + " " + nextPosInChromatogram);
						}
						if (closeupPanel.getShowT()) {
							if (isShownComplemented())
								baseColor = contigDisplay.getBaseColor(DNAState.A,contigDisplay.getBackgroundColor());
							else 
								baseColor = contigDisplay.getBaseColor(DNAState.T,contigDisplay.getBackgroundColor());
							if (baseColor.equals(Color.black) && darkBackground)
								baseColor = inverseBlackColor;
							g.setColor(baseColor);
							drawCurve(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,T,peakBottom, vertScale,  horizScale, firstReadLocation);
							//Debugg.println("T: " + previousPosInChromatogram + " " + posInChromatogram + " " + nextPosInChromatogram);
						}
					}
				}
				previous2PosInChromatogram = previousPosInChromatogram;
				previousPosInChromatogram = posInChromatogram;
			}
		}
		if (shadowOffset != 0){
			g.setColor(Color.gray);
			String s = "Shadow offset " + shadowOffset + "; reduction: " + MesquiteDouble.toString(shadowReduction);
			g.drawString(s,cwidth/2+8,v+30);
			if (shadowOffset <0)
				g.fillRect(cwidth/2 + shadowOffset, v+40, -shadowOffset, 4);
			else
				g.fillRect(cwidth/2, v+40, shadowOffset, 4);
		}



		int inc=15;
		int baseVert = peakBottom+inc+5;
		int ambiguityVert = peakBottom+inc+12;
		int textLeft = cwidth/2-20;
		GraphicsUtil.setFontStyle(Font.BOLD,g);
		GraphicsUtil.setFontSize(14,g);
		int posInChromatogram = getPhdLocation(read, getBounds().width, centerReadBase,contigDisplay,true);
		int ic = findConsensusBaseNumber(SINGLEREAD,posInChromatogram, firstReadBase, lastReadBase, firstReadLocation);

		if (MesquiteInteger.isCombinable(ic)&&MesquiteInteger.isCombinable(firstReadLocation)){
			if (MesquiteInteger.isCombinable(posInChromatogram) && (posInChromatogram>=0&&posInChromatogram+2<A.length )) {  //is it within bounds of read?
				int minPeakHeightToDraw = 3;
				if (A[posInChromatogram]>minPeakHeightToDraw || C[posInChromatogram]>minPeakHeightToDraw || G[posInChromatogram]>minPeakHeightToDraw || T[posInChromatogram]>minPeakHeightToDraw){
					int[] traceHeights = new int[] {A[posInChromatogram],C[posInChromatogram],G[posInChromatogram],T[posInChromatogram]};
					int[] originalTraceHeights = new int[] {A[posInChromatogram],C[posInChromatogram],G[posInChromatogram],T[posInChromatogram]};
					IntegerArray.sort(traceHeights);
					long[] order = new long[4];
					int[] orderInt = new int[4];

					for (int i=0; i<4;i++){
						if (traceHeights[i]==A[posInChromatogram]){
							order[i]=DNAState.A;
							orderInt[i]=0;
							traceHeights[i]=-1;
						}else	if (traceHeights[i]==C[posInChromatogram]){
							order[i]=DNAState.C;
							orderInt[i]=1;
							traceHeights[i]=-1;
						}else	if (traceHeights[i]==G[posInChromatogram]){
							order[i]=DNAState.G;
							orderInt[i]=2;
							traceHeights[i]=-1;
						}else	if (traceHeights[i]==T[posInChromatogram]){
							order[i]=DNAState.T;
							orderInt[i]=3;
							traceHeights[i]=-1;
						}
					}

					for (int i=3; i>=0;i--){
						if (originalTraceHeights[orderInt[i]]>0) {
							drawBaseInfo(g2,  inverseBlackColor, order[i], posInChromatogram,  textLeft,  baseVert);
							baseVert+=inc;
						}
					}

					long firstPair = DNAState.union(order[3], order[2]);
					long firstTriplet = DNAState.union(order[3], order[2]);
					firstTriplet = DNAState.union(firstTriplet,order[1]);
					int ambShift=55;
					if (originalTraceHeights[orderInt[2]]>originalTraceHeights[orderInt[3]]/10){
						g2.setColor(Color.gray);
						g2.setStroke(new BasicStroke(1));
						g.drawLine(textLeft+ambShift, ambiguityVert-inc-inc/2, textLeft+ambShift, ambiguityVert+inc/2);
						drawAmbiguityInfo(g2,  firstPair,  textLeft+ambShift+4,  ambiguityVert);
						ambiguityVert+=inc/2;
						if (originalTraceHeights[orderInt[1]]>originalTraceHeights[orderInt[3]]/10){
							g.drawLine(textLeft+ambShift+20, ambiguityVert-inc*2, textLeft+ambShift+20, ambiguityVert+inc);
							drawAmbiguityInfo(g2,  firstTriplet,  textLeft+ambShift+25,  ambiguityVert);
						}
					}


				}
			}
		}








		MesquiteWindow.uncheckDoomed(this);
	}


	/*----------------------------------------------------------------------------------*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
	}
	public void mouseDrag (int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseExited(int modifiers, int x, int y, MesquiteTool tool) {
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
	}

}