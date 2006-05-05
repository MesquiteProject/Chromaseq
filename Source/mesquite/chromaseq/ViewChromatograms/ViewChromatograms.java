/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison. Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code.  The commenting leaves much to be desired. Please approach this source code with the spirit of helping out. Perhaps with your help we can be more than a few, and make Mesquite better.  Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY. Mesquite's web site is http://mesquiteproject.org  This source code and its compiled class files are free and modifiable under the terms of  GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.ViewChromatograms; import java.awt.event.KeyEvent;import java.util.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class ViewChromatograms extends ChromatogramViewer {	CMTable table;	CharacterData  data;	//ChromatogramWindow window;	double polyThreshold =0.5;	NameReference aceRef = NameReference.getNameReference(AceFile.ACENAMEREF);		MesquiteMenuItemSpec mms;	MesquiteMenuSpec chromM;	Vector windows;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {		windows = new Vector();		return true;	}	/*.................................................................................................................*/	public ChromatogramWindow getWindow(){		return (ChromatogramWindow)windows.elementAt(0);  //Wayne:	}	public Taxon getTaxon(){		if (getWindow() == null)			return null;		return getWindow().getTaxon();	}	public void centerPanelAtOverallPosition(int ic){		if (getWindow() == null)			return;		getWindow().centerPanelsAtOverallPosition(ic);	}	public int getHomePositionInMatrix(){		if (getWindow() == null)			return 0;		return getWindow().getHomePositionInMatrix();	}	public int getCenterBase(){		if (getWindow() == null)			return 0;		return getWindow().getCenterBase();	}	/*.................................................................................................................*/	public boolean isSubstantive(){		return false;	}	/*.................................................................................................................*/	public void tableSelectionChanged(){		synchSelection(true);	}	public void matrixChanged(boolean syncPosition){		if (data == null)			return;		for (int i = 0; i< windows.size(); i++){			ChromatogramWindow w = (ChromatogramWindow)windows.elementAt(i);			w.matrixChanged(syncPosition);		}	}	public void showContigs(Taxon taxon, long whichContig, AceFile ace, MesquiteTable table, DNAData data, CommandRecord commandRec){		this.table = (CMTable)table;		this.data = (DNAData)data;		if (whichContig >= 0 && whichContig < ace.getNumContigs()){			ChromatogramWindow w =showContig(taxon, ace.getContig((int)whichContig), commandRec);  //NEED TO indicate which contig if more than one!!!!			if (w != null) {				windows.addElement(w);			}		}		else {			for (int i = 0; i< ace.getNumContigs(); i++) {				ChromatogramWindow w =showContig(taxon, ace.getContig(i), commandRec);  //NEED TO indicate which contig if more than one!!!!				if (w != null) {					windows.addElement(w);				}			}		}	}	public void homeContigsAtMatrixPosition(int ic){		for (int i = 0; i< windows.size(); i++){			ChromatogramWindow w = (ChromatogramWindow)windows.elementAt(i);			w.scrollToConsensusBase(w.getConsensusPositionOfMatrixPosition(ic, (DNAData)data));		}	}	/*.................................................................................................................*/	public ChromatogramWindow showContig(Taxon taxon, Contig contig,  CommandRecord commandRec){		int count = 0;		for (int i=0; i<contig.getNumReadsToShow(); i++) {			if (!StringUtil.blank(contig.getRead(i).getABIFile()) && !StringUtil.blank(contig.getRead(i).getABIFilePath()))				count++;		}		if (count == 0)			return null;		String[] fileNames = new String[count];		String[] paths = new String[count];		Read[] reads = new Read[count];		count = 0;		for (int i=0; i<contig.getNumReadsToShow(); i++) {			String abiFile = contig.getRead(i).getABIFile();			String abiFilePath = contig.getRead(i).getABIFilePath();			reads[count] = contig.getRead(i);			if (!StringUtil.blank(abiFile) && !StringUtil.blank(abiFilePath)) {				fileNames[count] = abiFile;				paths[count] = abiFilePath;				count++;			}		}		MesquiteModule windowServer = hireNamedEmployee (commandRec, WindowHolder.class, "#WindowBabysitter");		windowServer.makeMenu("Chromatograms");		VChromWindow window = VChromWindow.showChromatogram(paths, fileNames, contig, reads, table, (DNAData)data, taxon, windowServer,this,commandRec);		MesquiteMenuItemSpec mm = windowServer.addMenuItem("Convert Selected to Gaps", makeCommand("selectedToGaps", window));		mm.setShortcut(KeyEvent.VK_MINUS); 		mm = windowServer.addMenuItem("Revert Selected to Called", makeCommand("selectedToCalled", window));		mm.setShortcut(KeyEvent.VK_R); 		windowServer.resetContainingMenuBar();		return window;	}	/*.................................................................................................................*/	public void synchSelection(boolean syncPosition){		if (data == null)			return;		for (int i = 0; i< windows.size(); i++){			ChromatogramWindow w = (ChromatogramWindow)windows.elementAt(i);			w.synchChromToTable(syncPosition);		}			}	/*.................................................................................................................*/	public void employeeQuit(MesquiteModule employee){		if (employee instanceof WindowHolder){			windows.removeAllElements();						iQuit();			resetAllWindowsMenus();		}	}	/*.................................................................................................................*/	public String getName() {		return "View Chromatograms";	}	/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Displays chromatograms for sequence in a separate window." ;	}	/*.................................................................................................................*/	public boolean showCitation(){		return true;	}	}