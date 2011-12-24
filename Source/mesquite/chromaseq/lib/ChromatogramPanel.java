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

import mesquite.lib.*;

public  class ChromatogramPanel extends MesquitePanel{
	protected boolean open=true;  // REMOVE ONCE CONVERT TO CLOSABLEPANEL; same for isOpen()
	protected Chromatogram[] chromatograms;
	protected int numChromatograms = 1;
	protected ContigDisplay contigDisplay;
	
	
	protected	int width = 0;
	protected int height = 0;
	protected int top = 0;

	
	protected int centerBase = 0;
	protected int centerPixel = 0;
	protected int verticalPosition = 0;
	public int contigID;
	boolean scrollToTouched = false;

	
	public static boolean SHOWREADMATRIXCONFLICTDEFAULT = false;
	public static final boolean THICKTRACEDEFAULT = false;
	protected boolean thickTrace = THICKTRACEDEFAULT;

	
	protected boolean listen = true;
	
	public ChromatogramPanel() {
		super();
	}
	
	public boolean isOpen() {
		return open;
	}
	
	
	//this is consensus position
	public void exportSelectUniversalBase(int i){
		listen = false;
		contigDisplay.selectUniversalBaseInTable(i);

		contigDisplay.setSelectedUniversalBaseInChrom(i, true, true);
		listen = true;
	}
	//this is consensus position
	public void exportDeselectUniversalBase(int i){
		listen = false;
		contigDisplay.deselectUniversalBaseInTable(i);
		contigDisplay.setSelectedUniversalBaseInChrom(i, false, true);
		listen = true;
	}
	public void exportDeselectAll(){
		listen = false;
		contigDisplay.deselectAllInTable();
		contigDisplay.deselectAllInPanels();
		listen = true;
	}

	/*.................................................................................................................*/
	public void setScrollToTouched(boolean scrollToTouched) {
		this.scrollToTouched = scrollToTouched;
	}
	/*.................................................................................................................*/
	public boolean getScrollToTouched() {
		return scrollToTouched;
	}
	

	public ContigDisplay getContigDisplay() {
		return contigDisplay;
	}
	public Chromatogram getChromatogram(int i) {
		return chromatograms[i];
	}
	public Chromatogram getChromatogram() {
		return chromatograms[0];
	}
	public void setChromatogram(Chromatogram chromatogram) {
		this.chromatograms[0] = chromatogram;
	}
	public void setChromatogram(int i, Chromatogram chromatogram) {
		this.chromatograms[i] = chromatogram;
	}

	public int getCenterBase() {
		return centerBase;
	}
	public void setCenterBase(int centerBase) {
		this.centerBase = centerBase;
	}

	public int getCenterPixel() {
		return centerPixel;
	}
	public void setCenterPixel(int centerPixel) {
		this.centerPixel = centerPixel;
	}

	public int getVerticalPosition() {
		return verticalPosition;
	}
	public void setVerticalPosition(int verticalPosition) {
		this.verticalPosition = verticalPosition;
	}
	
	/*.................................................................................................................*/
	public void setThickTrace(boolean thickTrace) {
		this.thickTrace = thickTrace;
	}
	/*.................................................................................................................*/
	public boolean getThickTrace() {
		return thickTrace;
	}
	public int getContigID() {
		return contigID;
	}
	public void setContigID(int contigID) {
		this.contigID = contigID;
	}
	public int getNumChromatograms() {
		return numChromatograms;
	}
	public void setNumChromatograms(int numChromatograms) {
		this.numChromatograms = numChromatograms;
	}


	
}
