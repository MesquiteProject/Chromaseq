package mesquite.chromaseq.lib;

import mesquite.lib.*;

public class ChromatogramPanel extends MesquitePanel {
	protected Chromatogram chromatogram;
	protected ContigDisplay panel;
	
	protected int centerBase = 0;
	protected int centerPixel = 0;
	protected int verticalPosition = 0;
	protected int contigID;
	boolean scrollToTouched = false;

	
	public static final boolean THICKTRACEDEFAULT = true;
	protected boolean thickTrace = THICKTRACEDEFAULT;

	
	protected boolean listen = true;
	//this is consensus position
	public void exportSelectConsensusPosition(int i){
		listen = false;
		panel.selectConsensusPositionInTable(i);

		panel.setSelectedConsensusPositionInChrom(i, true, true);
		listen = true;
	}
	//this is consensus position
	public void exportDeselectConsensusPosition(int i){
		listen = false;
		panel.deselectConsensusPositionInTable(i);
		panel.setSelectedConsensusPositionInChrom(i, false, true);
		listen = true;
	}
	public void exportDeselectAll(){
		listen = false;
		panel.deselectAllInTable();
		panel.deselectAllInPanels();
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
		return panel;
	}
	public Chromatogram getChromatogram() {
		return chromatogram;
	}
	public void setChromatogram(Chromatogram chromatogram) {
		this.chromatogram = chromatogram;
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

	
}
