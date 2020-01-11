/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import java.io.*;public abstract class Chromatogram {	protected Read read;	protected String title;	protected ContigDisplay window;		public abstract boolean readChromatogram(InputStream in) throws IOException;	public abstract void write(OutputStream OUT);	public abstract int getATrace(int x);	public abstract int getCTrace(int x);	public abstract int getGTrace(int x);	public abstract int getTTrace(int x);	public abstract String getTraceValuesString(int x);	public abstract String getTraceValuesStringAllRows(boolean withTitle);	public abstract int[] getATrace();	public abstract int[] getCTrace();	public abstract int[] getGTrace();	public abstract int[] getTTrace();	public abstract int getTraceLength();	public abstract int getBaseNumber();	public abstract char getBase(int x);	public abstract int getBasePosition(int x);	public abstract String getSequence();	public abstract String getComments();	public abstract void reverseComplement(boolean reverseBasePosition);	public void setWindow(ContigDisplay window) {		this.window = window;	}	public ContigDisplay getWindow() {		return window;	}	public void setTitle(String s) {		title = s;	}	public String getTitle() {		return title;	}	public Read getRead(){		return read;	}	public void report(String s) {}	/* ....................................*/	/** This returns the horizontal measurement within a read that corresponds to pixel distance represented by displayPixels along the entire span on the consensus sequence plus others  */	public int getLocationWithinReadFromDisplayPixels(int displayPixels) {		int pos = displayPixels;   //let's start out with where we are in the entire span		if (window!=null && read!=null) {			int consensusBasePositionOfStart = read.getContigBaseFromReadBase(0);  //get the base in the consensus sequence that the start of this read corresponds to			pos -=window.getFullPixelValueOfContigBase(consensusBasePositionOfStart);		}		return pos;	}	/* ....................................*/	public int getAveragePeakDistance() {		int total = 0;		int count = 0;		int quarter = getBaseNumber()/4;		if (quarter<1) quarter = 1;		for (int i=quarter;i < quarter*3;i++) {			total += getBasePosition(i)-getBasePosition(i-1);			count++;		}		return total/count+1;  	}	/* ....................................*/	public int getReadBaseLocation(int x) {   //returns the horizontal location of the base numbered x.  		if (x<0)			return  (int)(x*window.getAveragePeakDistance());   //approximate		else if (x>getBaseNumber()-1)			return (int)((x-getBaseNumber()+1)*window.getAveragePeakDistance()); //approximate		int pos = getBasePosition(x);		return pos;	}	/* ....................................*/	public int getReadBaseLocationAligned(int x) {   //returns the horizontal position in pixels of the base numbered x, in the chromatogram window,													//relative to the standard base standardX.  		if (x<0)			return  (int)(x*window.getAveragePeakDistance());   //approximate		else if (x>getBaseNumber()-1)			return (int)((x-getBaseNumber()+1)*window.getAveragePeakDistance()); //approximate		int pos = getBasePosition(x);		if (window!=null && read!=null) {			int consensusBasePositionOfStart = read.getContigBaseFromReadBase(0);  //get the base in the consensus sequence that the start of this read corresponds to			pos +=window.getFullPixelValueOfContigBase(consensusBasePositionOfStart);		}		return pos;	}}