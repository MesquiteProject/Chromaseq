/* Mesquite chromaseq source code.  Copyright 2005-2008 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.lib;import java.awt.event.*;import mesquite.lib.*;import mesquite.lib.characters.*;import java.awt.*;import mesquite.categ.lib.*;//Todo: This and contained panels should be within ViewChromatograms as they are designed to be used by only that one module/* ======================================================================== */public interface ContigDisplay extends KeyListener {			/*.................................................................................................................*/	public boolean getColorByQuality();	public boolean getColorReadCallsByQuality();	public boolean getColorEditedInMatrixByQuality();	public boolean getColorMultiReadByQuality();	public boolean getColorOverviewByQuality();	public Color getBaseColor(char c, Color backgroundColor);	public Color getBaseColor(long state, Color backgroundColor);	public Color getBackgroundColor();		public double getHorizScale();	/*.................................................................................................................*/	public double getAveragePeakDistance();	/*.................................................................................................................*/	public int getApproximateNumberOfPeaksVisible();	/*.................................................................................................................*/	/** Returns the number of pixels the window is scrolled */	public int getCenterPixelFromCenterBase(int centerBase);	/*.................................................................................................................*/	/** Returns the base number within the consensus sequence at the base baseNumber (which is numbered from the beginning, including excess bases at the start).   */	public int getConsensusBaseFromOverallBase(int baseNumber);	public int getOverallBaseFromConsensusBase(int baseNumber);	public int  getCodonPositionOfOverallBase(int overallBase);	public Contig getContig();	public int getHorizontalPixels(int pixels);	public void centerPanelsAtConsensusPosition(int ic); 	public void centerPanelsAtOverallPosition(int ic);	public int getHomePositionInMatrix();	public void synchChromToTable(boolean synchPosition);	public void synchTableToChrom(boolean synchPosition);	public void matrixChanged(boolean synchPosition);	public int getCenterBase();	public  void scrollToOverallBase(int overallBase);	 public  void scrollToMatrixBase(int ic);	public int getTotalNumPeaks();  	public String getContigDisplayExplanation (int consensusBase);	public boolean isShownReversed();	public boolean isShownComplemented();		 		//for coordinating selection; uses overall	public void setFirstTouchedOverall(int ic);	//returns overall base	public int getFirstTouchedOverall();	//returns 0 if left of consensus	public int getFirstTouchedConsensus();	public void setSecondTouchedOverall(int ic);	public int getSecondTouchedOverall();	public int getSecondTouchedConsensus();	public void deselectAllChrom(int overallBase);	/* This returns the horizontal position, in pixels, of consensus site ic.*/	public int getFullPixelValueOfDisplayBase(int ic);		/*   consensus	 * 	 * 	 *///	public int getNumPaddedBeforeConsensusBase(int ic);  	/*This should return for character ic in matrix/table, what is the position in the consensus.  This is needed in case the sequence has been aligned,	and thus is not left justified and contiguous.  This has access to both the data and table */	public int getDisplayPositionOfMatrixPosition(int ic, DNAData data);	public int getDisplayPositionOfMatrixPosition(int ic); 	/*This should return for character ic in matrix/table, what is the position in the consensus.  This is needed in case the sequence has been aligned,	and thus is not left justified and contiguous.  This has access to both the data and table *	public int getPaddedConsensusPositionOfMatrixPosition(int ic, DNAData data);  	/*This should return for consensus site ic what character in matrix it corresponds to.  This is needed in case the sequence has been aligned,	and thus is not left justified and contiguous.*/	public int getMatrixPositionOfDisplayPosition(int ic, DNAData data); 	public int getMatrixPositionOfDisplayPosition(int consensusBase);	public int getNumPaddedBeforeDisplayBase(int ic);	public int getNumInsertedBeforeDisplayBase(int ic);	public int getSpaceInsertedBeforeDisplayBase(int ic);	public int getTotalSpaceInsertedBeforeDisplayBase(int ic);	public int getSpaceInsertedAfterDisplayBase(int ic);	public char getMatrixStateAtDisplayPosition(int ic);	/*This should return for consensus site ic what character in matrix it corresponds to.  This is needed in case the sequence has been aligned,	and thus is not left justified and contiguous.*	public int getMatrixPositionOfPaddedConsensusPosition(int ic, DNAData data);		/**/	public void focusMatrixOn(int ic, int it);	 	 /*This uses indices as in consensus*/	public boolean getSelectedDisplayPositionInChrom(int ic);		public boolean getSelectedOverallBase(int overallBase);   	/*This uses indices as in consensus*/	public boolean setSelectedDisplayPositionInChrom(int ic, boolean sel, boolean repnt);  	//this is consensus position	public void selectDisplayPositionInTable(int i);	//this is consensus position	public void deselectDisplayPositionInTable(int i);	public void deselectAllInTable();	public void deselectAllInPanels();	public void deselectAllReads();	public void scrollToConsensusBase(int i);  	public void repaintPanels();  	public void setExplanation(String s);	public Taxon getTaxon();	public void keyReleased(KeyEvent e);	public void keyTyped(KeyEvent e);	public void keyPressed(KeyEvent e); 	public SequencePanel getSequencePanel(int whichPanel);	public String[][] getPrimerSequences();	public boolean showPrimers();	public boolean getShowSinglePrimerMatch();	public String getSinglePrimer();	public int getID();}