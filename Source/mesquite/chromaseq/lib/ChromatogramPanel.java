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
		contigDisplay.selectDisplayPositionInTable(i);

		contigDisplay.setSelectedUniversalBaseInChrom(i, true, true);
		listen = true;
	}
	//this is consensus position
	public void exportDeselectUniversalBase(int i){
		listen = false;
		contigDisplay.deselectDisplayPositionInTable(i);
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
