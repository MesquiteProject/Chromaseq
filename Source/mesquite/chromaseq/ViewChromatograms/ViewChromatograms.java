/* Mesquite chromaseq source code.  Copyright 2005-2008 D. Maddison and W. Maddison. Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code.  The commenting leaves much to be desired. Please approach this source code with the spirit of helping out. Perhaps with your help we can be more than a few, and make Mesquite better. Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY. Mesquite's web site is http://mesquiteproject.org This source code and its compiled class files are free and modifiable under the terms of  GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.ViewChromatograms; import java.awt.event.KeyEvent;import java.util.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.tol.lib.MesquiteXMLToLUtilities;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class ViewChromatograms extends ChromatogramViewer {	CMTable table;	CharacterData  data;	//ChromatogramWindow window;	double polyThreshold =0.5;	NameReference aceRef = NameReference.getNameReference(AceFile.ACENAMEREF);	MesquiteMenuItemSpec mms;	MesquiteMenuSpec chromM;	Vector windows;	String primerSeqPath;	String primerDatabaseURL="http://btol.tolweb.org";	String[][] primerSequences;  // will be [numPrimers][2]	boolean showPrimers = false;	boolean showSinglePrimerMatch = false;	boolean triedPrimerSequences = false;	boolean lastPrimersFromDatabase = false;	MesquiteBoolean primerInfoFromDatabase =new MesquiteBoolean(false);	String singlePrimer;	MesquiteString xmlPrefs= new MesquiteString();	String xmlPrefsString = null;		//MesquiteBoolean chromatogramsOnTop = new MesquiteBoolean(true);	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		loadPreferences(xmlPrefs);		xmlPrefsString = xmlPrefs.getValue();		windows = new Vector();		return true;	}	public String preparePreferencesForXML () {		StringBuffer buffer = new StringBuffer();		StringUtil.appendXMLTag(buffer, 2, "primerSeqPath", primerSeqPath);   		StringUtil.appendXMLTag(buffer, 2, "showPrimers", showPrimers);   		VChromWindow cw = (VChromWindow)getWindow();		if (cw!=null){			String s = cw.preparePreferencesForXML(); 			if (StringUtil.notEmpty(s))				buffer.append(s);		}		return buffer.toString();	}	public void processSingleXMLPreference (String tag, String content) {		if ("showPrimers".equalsIgnoreCase(tag))			showPrimers = MesquiteBoolean.fromTrueFalseString(content);		else if ("primerSeqPath".equalsIgnoreCase(tag))			primerSeqPath = StringUtil.cleanXMLEscapeCharacters(content);	}	////	public String[][] getPrimerSequences(){		if (primerSequences == null || lastPrimersFromDatabase!=primerInfoFromDatabase.getValue()){			/*MesquiteString dir = new MesquiteString();			MesquiteString file = new MesquiteString();*/			if (primerInfoFromDatabase.getValue()) {				logln("Reading primer sequences from: " + primerDatabaseURL);				PrimerList primers = new PrimerList(true, MesquiteXMLToLUtilities.getTOLPageDatabaseURL(primerDatabaseURL));				primers.readPrimerInfoFromDatabase();				primerSequences= primers.getPrimerSequences();				lastPrimersFromDatabase = true;			} else {				if (primerSeqPath == null && !triedPrimerSequences)					resetPrimersFile();				if (primerSeqPath == null)					return null;				if (!MesquiteFile.fileExists(primerSeqPath)){					return null;				}				triedPrimerSequences = true;				logln("Reading primer sequences from: " + primerSeqPath);				primerSequences = MesquiteFile.getTabDelimitedTextFile(primerSeqPath, false);				lastPrimersFromDatabase = false;			}		//	storePreferences();  this CANNOT be done here, for this needs to be done 		}		return primerSequences;	}		public void resetPrimersFile(){		primerSeqPath = null;		triedPrimerSequences = false;		primerSequences = null;		if (!MesquiteThread.isScripting()){			primerSeqPath = MesquiteFile.openFileDialog("Choose primer sequence file", null, null);			logln("Set primer sequences file to: " + primerSeqPath);		}	}	public void setShowPrimers(boolean show){		showPrimers = show;		storePreferences();	}	public void setPrimerInfoFromDatabase(boolean b){		primerInfoFromDatabase.setValue(b);		storePreferences();	}	public void setShowSinglePrimerMatch(boolean show){		showSinglePrimerMatch = show;		storePreferences();	}	public void setSinglePrimer(String s){		singlePrimer = s;		storePreferences();	}	/*.................................................................................................................*/	public ChromatWindow getWindow(){		if (windows == null || windows.size() == 0)			return null;		return (ChromatWindow)windows.elementAt(0);  //Wayne:	}	public Taxon getTaxon(){		if (getWindow() == null)			return null;		return getWindow().getTaxon();	}	public void centerPanelAtOverallPosition(int ic){		if (getWindow() == null)			return;		getWindow().centerPanelAtOverallPosition(ic);	}	public int getHomePositionInMatrix(){		if (getWindow() == null)			return 0;		return getWindow().getHomePositionInMatrix();	}	public int getCenterBase(){		if (getWindow() == null)			return 0;		return getWindow().getCenterBase();	}	/*.................................................................................................................*/	public boolean isSubstantive(){		return false;	}	/*.................................................................................................................*/	public void tableSelectionChanged(){		synchSelection(true);	}	public void matrixChanged(boolean syncPosition){		if (data == null)			return;		for (int i = 0; i< windows.size(); i++){			ChromatWindow w = (ChromatWindow)windows.elementAt(i);			ContigDisplay p = w.getMainContigPanel();			p.matrixChanged(syncPosition);		}	}	public void showContigs(int it, MesquiteTable table, DNAData data){		double polyThreshold =0.5;		Taxon taxon = data.getTaxa().getTaxon(it);		NameReference aceRef = NameReference.getNameReference(AceFile.ACENAMEREF);		Associable tInfo = data.getTaxaInfo(false);		String path = null;		long whichContig = -1;		if (tInfo != null) {			path = (String)tInfo.getAssociatedObject(aceRef, it);			whichContig = tInfo.getAssociatedLong(AceFile.WHICHCONTIGREF, it);		}		if (StringUtil.blank(path)) {			iQuit();			return;		}		path = MesquiteFile.composePath(getProject().getHomeFile().getDirectoryName(), path);		if (!MesquiteFile.fileExists(path)){			iQuit();			return;		}		AceFile ace = new AceFile(path, path, null, this,  true, polyThreshold);		this.table = (CMTable)table;		this.data = (DNAData)data;		if (whichContig >= 0 && whichContig < ace.getNumContigs()){			VChromWindow w =showContig(taxon, ace.getContig((int)whichContig));  //NEED TO indicate which contig if more than one!!!!			if (w != null) {				windows.addElement(w);				if (showPrimers)					w.startPrimerShow();			}		}		else {			for (int i = 0; i< ace.getNumContigs(); i++) {				VChromWindow w =showContig(taxon, ace.getContig(i));  //NEED TO indicate which contig if more than one!!!!				if (w != null) {					windows.addElement(w);					if (showPrimers)						w.startPrimerShow();				}			}		}	}	public void homeContigsAtMatrixPosition(int ic){		for (int i = 0; i< windows.size(); i++){			ContigDisplay w = (ContigDisplay)windows.elementAt(i);			w.scrollToConsensusBase(w.getConsensusPositionOfMatrixPosition(ic, (DNAData)data));		}	}	/*.................................................................................................................*/	private VChromWindow showContig(Taxon taxon, Contig contig){		/*int count = 0;		for (int i=0; i<contig.getNumReadsToShow(); i++) {			if (!StringUtil.blank(contig.getRead(i).getABIFile()) && !StringUtil.blank(contig.getRead(i).getABIFilePath()))				count++;		}		if (count == 0)			return null;		String[] fileNames = new String[count];		String[] paths = new String[count];		Read[] reads = new Read[count];		count = 0;		for (int i=0; i<contig.getNumReadsToShow(); i++) {			String abiFile = contig.getRead(i).getABIFile();			String abiFilePath = contig.getRead(i).getABIFilePath();			reads[count] = contig.getRead(i);			if (!StringUtil.blank(abiFile) && !StringUtil.blank(abiFilePath)) {				fileNames[count] = abiFile;				paths[count] = abiFilePath;				count++;			}		}*/		MesquiteModule windowServer = hireNamedEmployee(WindowHolder.class, "#WindowBabysitter");		windowServer.makeMenu("Chromatograms");		loadPreferences(xmlPrefs);		xmlPrefsString = xmlPrefs.getValue();		VChromWindow window = VChromWindow.showChromatogram(contig, table, (DNAData)data, taxon, null, windowServer,this, xmlPrefsString);		MesquiteMenuItemSpec mm = windowServer.addMenuItem("Convert Selected to Gaps", makeCommand("selectedToGaps", window));		mm.setShortcut(KeyEvent.VK_MINUS); 		mm = windowServer.addMenuItem("Convert Trimmable to Gaps", makeCommand("trimmableToGaps", window));		mm = windowServer.addMenuItem("Revert Selected to Called", makeCommand("selectedToCalled", window));		mm.setShortcut(KeyEvent.VK_R); 		windowServer.resetContainingMenuBar();		return window;	}	/*.................................................................................................................*/	public void synchSelection(boolean syncPosition){		if (data == null)			return;		for (int i = 0; i< windows.size(); i++){			VChromWindow w = (VChromWindow)windows.elementAt(i);			w.synchChromToTable(syncPosition);		}	}	/*.................................................................................................................*/	public void employeeQuit(MesquiteModule employee){		if (employee instanceof WindowHolder){			windows.removeAllElements();			iQuit();			resetAllWindowsMenus();		}	}	/*.................................................................................................................*/	public String getName() {		return "View Chromatograms";	}	/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Displays chromatograms for sequence in a separate window." ;	}	/*.................................................................................................................*/	public boolean showCitation(){		return true;	}}