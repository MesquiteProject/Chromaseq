/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib;import java.awt.event.*;import mesquite.lib.*;import mesquite.lib.characters.*;import java.awt.*;import mesquite.categ.lib.*;/* ======================================================================== */public abstract class ChromatWindow extends MesquiteWindow {	public static boolean DEFAULTSHOWPRIMERS = false;	public ChromatWindow(MesquiteModule owner, boolean info){		super(owner, info);	}	public abstract Taxon getTaxon();	public abstract void centerPanelAtOverallPosition(int ic);	public abstract int getHomePositionInMatrix();	public abstract int getCenterBase();	public abstract int getTotalNumPeaks();	public abstract void scrollToMatrixBase(int ic);	public abstract void scrollToOverallBase(int ic);	public abstract void scrollToConsensusBase(int ic);	public abstract int getConsensusPositionOfMatrixPosition(int ic);	public abstract SequencePanel getSequencePanel(int whichPanel);	public abstract ContigDisplay getMainContigPanel();	}