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

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;

import mesquite.align.lib.*;
import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
import mesquite.categ.lib.*;

public class ChromatogramCloseupPanel extends ChromatogramPanel{
	CloseupChromatogramCanvas chromCanvas;
	int id;
	boolean showA = true;
	boolean showC = true;
	boolean showG = true;
	boolean showT = true;
	int readBaseNumber;

	public ChromatogramCloseupPanel(ClosablePanelContainer container, Chromatogram myChrom, ContigDisplay panel, int id, int contigID) {
		this.id = id;  // this is the read number
		this.contigID = contigID;  //this is the taxon number or other id for contig
		this.contigDisplay = panel;
		open=true;
		chromatograms = new Chromatogram[1];
		setChromatogram(myChrom);
		chromCanvas = new CloseupChromatogramCanvas(this, id);
		chromCanvas.setBounds(0, 0, getWidth(), getHeight());
		this.add("Center",chromCanvas);
		setBackground(Color.white);
		setThickTrace(true);

	}
	public void centerPanelAtOverallPosition(int i){
		centerBase = i;
		chromCanvas.repaint();
	}
	/*.................................................................................................................*/
	public void setChromatogram(Chromatogram myChrom) {
		if (myChrom==null){
			chromatograms[0] = null;
			if (chromCanvas!=null){
				chromCanvas.setChromatograms(chromatograms);
			}
		}
		else {
			chromatograms[0] = myChrom;
			chromatograms[0].setWindow(contigDisplay);
			if (chromCanvas!=null){
				chromCanvas.setChromatograms(chromatograms);
			}
		}
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

}

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
	protected void drawLine(Graphics2D g, int width, int x, int y, int x2, int y2, int x3, int y3){
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
	protected void drawLine(Graphics2D g, int width, int x, int y, int x2, int y2, int x3, int y3, int x4, int y4){
		CubicCurve2D curve = null;
		if (isShownReversed()){
			curve = new CubicCurve2D.Double(width-x,y,width-x2,y2,width-x3,y3,width-x4,y4);
			g.draw(curve);
		}
		else {
			curve = new CubicCurve2D.Double(x,y,x2,y2,x3,y3, x4, y4);
			g.draw(curve);
		}
	}
	protected void drawLine(Graphics2D g, int width, int posPrev, int pos, int posNext, int[] trace, int peakBottom, double vertScale, double horizScale, int firstReadLocation){
		int x1 = (int)((posPrev-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int x2 = (int)((pos-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int x3 = (int)((posNext-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int y1 = getY(trace,peakBottom,posPrev,vertScale);
		int y2 = getY(trace,peakBottom,pos,vertScale);
		int y3 = getY(trace,peakBottom,posNext,vertScale);
		drawLine(g, width, x1,y1,x2,y2,x3,y3);
		//Debugg.println("x1 " + x1 + ", x2 " + x2 + ", x3 " + x3);
	//	Debugg.println("y1 " + y1 + ", y2 " + y2 + ", y3 " + y3);

		//offset+=10;
	}
	protected void drawLine(Graphics2D g, int width, int posPrev2, int posPrev, int pos, int posNext, int[] trace, int peakBottom, double vertScale, double horizScale, int firstReadLocation){
		int pixels1 = (int)((posPrev2-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int pixels2 = (int)((posPrev-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int pixels3 = (int)((pos-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		int pixels4 = (int)((posNext-firstReadLocation)*horizScale);//+ offsetForInserted);  //returns scaled horizontal pixels
		drawLine(g, width, pixels1,getY(trace,peakBottom,posPrev2,vertScale),pixels2,getY(trace,peakBottom,posPrev,vertScale),pixels3,getY(trace,peakBottom,pos,vertScale),pixels4,getY(trace,peakBottom,posNext,vertScale));
	}

	static final int SINGLEREAD = 0;
	
	protected int getY (int[] trace, int peakBottom, int posInChromatogram, double vertScale) {
		if (shadowOffset != 0 && (posInChromatogram-shadowOffset>=0&&posInChromatogram-shadowOffset<A.length ))
			return peakBottom-(int)((trace[posInChromatogram] - shadowReduction*trace[posInChromatogram-shadowOffset]) / vertScale);
		else
			return peakBottom-(int)(trace[posInChromatogram] / vertScale);
	}
	/*...........................................................................*/
	public synchronized void paint(Graphics g) {	
		if (MesquiteWindow.checkDoomed(this)) 
			return;
		if (!(g instanceof Graphics2D) || chromatograms==null || chromatograms.length==0 || chromatograms[SETREAD]==null)
			return;
		int v = -verticalPosition;
		Graphics2D g2 = (Graphics2D)g;
	    g2.setStroke(new BasicStroke(2));
	    

		if (chromatograms==null || chromatograms.length==0 || chromatograms[SETREAD]==null)
			return;
		Read read = chromatograms[SETREAD].getRead();
		int cheight = getBounds().height;
		int shadowHeight = 5;
		int labelHeight = 18;
	//	ChromaseqUniversalMapper universalMapper = contigDisplay.getUniversalMapper();

		int labelBottom = v+cheight-shadowHeight;
		int labelTop = labelBottom-labelHeight+1;
		int cwidth = getBounds().width;
		double vertScale = 4.0;
		double horizScale = 2.0;
		int widthPerPeak = 40;
		int approximatePeaks = cwidth/widthPerPeak;
		int halfPeaks = approximatePeaks/2;
		if (halfPeaks<1) halfPeaks=1;

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


		int firstReadBase = centerReadBase - halfPeaks;
		int lastReadBase = centerReadBase+ halfPeaks;
		
		int maxHeight = 0;
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
		maxHeight = (int)(maxHeight/vertScale);
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
		/*David: here a correction is introduced to discover what is actually the first and last read bases visible.  Previously
		these bases were calcualted incorrectly if some reads were effectively compressed, and thus drawing didn't go all the way to the edges

		A similar problem of compression was affecting findConsensusBaseNumber, hence some of this code is repeated there.

		 */
		


		Color inverseBlackColor = Color.white;

		//		=====================  DRAWING THE PEAKS ==============================
		int peakBottom = labelBottom - labelHeight; //+lines;
		int prevCons = 0;
		int lostSpaceByInsert = 0;
		int lastReadPos = MesquiteInteger.unassigned;
		int start= 0;
		int end = cwidth-2;
		int previousPosInChromatogram = firstReadLocation;
		int previous2PosInChromatogram = firstReadLocation;
		int nextPosInChromatogram = firstReadLocation;
		
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


				if (MesquiteInteger.isCombinable(posInChromatogram) && (posInChromatogram>=0&&posInChromatogram+2<A.length )) {  //is it within bounds of read?
					int minPeakHeightToDraw = 3;
					if (A[posInChromatogram]>minPeakHeightToDraw || C[posInChromatogram]>minPeakHeightToDraw || G[posInChromatogram]>minPeakHeightToDraw || T[posInChromatogram]>minPeakHeightToDraw){

						nextPosInChromatogram = posInChromatogram+1;
						if (posInChromatogram>lastReadLocation)
							posInChromatogram=lastReadLocation;
						prevCons=ic;

						Color baseColor;

						if (closeupPanel.getShowA()) {
							if (isShownComplemented())
								baseColor = contigDisplay.getBaseColor(DNAState.T, contigDisplay.getBackgroundColor());
							else 
								baseColor = contigDisplay.getBaseColor(DNAState.A,contigDisplay.getBackgroundColor());
							if (baseColor.equals(Color.black) && darkBackground)
								baseColor = inverseBlackColor;
							g.setColor(baseColor);
							drawLine(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,A,peakBottom, vertScale, horizScale, firstReadLocation);
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
							drawLine(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,C,peakBottom, vertScale,  horizScale, firstReadLocation);
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
							drawLine(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,G,peakBottom, vertScale, horizScale, firstReadLocation);
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
							drawLine(g2, cwidth, previousPosInChromatogram, posInChromatogram,nextPosInChromatogram,T,peakBottom, vertScale,  horizScale, firstReadLocation);
						//	Debugg.println("T: " + previousPosInChromatogram + " " + posInChromatogram + " " + nextPosInChromatogram);
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
	/*...............................................................................................................*/

}