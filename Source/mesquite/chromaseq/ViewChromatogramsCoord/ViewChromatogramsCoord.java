/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.ViewChromatogramsCoord; import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.chromaseq.lib.*;/* ======================================================================== *//* NAMING CONVENTIONS * Read: one chromatogram read. * Contig: the contig as supplied by, e.g., Phred/Phrap in an AceFile.  * Main Consensus Sequence: the consensus sequence supplied in the contig *  *  * Numbering schemes employed in the chromatogram viewer:   * OverallBase:  this is the base number from 0 to ChromatogramWindow.getTotalNumPeaks()-1.  * 		That is, it is the entire span of the chromatogram viewer. * ConsensusBase: this is the base number within the main consensus (i.e., as supplied by the contig). * 		This is NOT the base number within the matrix sequence, or the original import sequence, it is * 		specifically within the originals contig's consensus.   * ReadBase:  the base number within a read.  Note that there are TWO potential base read numberings, * 		(1) one as found within the .phd file created by Phred, and as supplied by the class Read,  * 		(2) the other as found within the chromatogram itself and supplied by the class Chromatogram. * MatrixPosition: the base number within the data matrix *  * Trace heights, including peak locations: There are two distinct (but obviously correlated) sources * 		of information about trace heights: * 		(1) one as found within the .phd file created by Phred.  This is in the form of peak locations.  * 		(2) the other as found within the chromatogram itself and supplied by the class Chromatogram.* */public class ViewChromatogramsCoord extends DataWindowAssistantI {	CMTable table;	CharacterData  data;	protected TableTool viewChromatTool;	MesquiteMenuItemSpec mms;	MesquiteMenuSpec chromM;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		if (containerOfModule() instanceof MesquiteWindow) {			viewChromatTool = new TableTool(this, "viewChromat", getPath(), "showChromat.gif", 7,10,"Shows the chromatograms for the sequence touched", "This tool shows the chromatograms for the sequence.", MesquiteModule.makeCommand("viewChromatFromTool",  this) , null, null);			viewChromatTool.setWorksOnColumnNames(false);			viewChromatTool.setWorksOnRowNames(true);			viewChromatTool.setWorksOnMatrixPanel(true);			viewChromatTool.setWorksOnCornerPanel(false);			((MesquiteWindow)containerOfModule()).addTool(viewChromatTool);		}		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");		return true;	}	/*.................................................................................................................*/  	 public Snapshot getSnapshot(MesquiteFile file) {   	 	Snapshot temp = new Snapshot();		for (int i = 0; i<getNumberOfEmployees(); i++) {			Object e=getEmployeeVector().elementAt(i);			if (e instanceof ChromatogramViewer) {				ChromatogramViewer cv = (ChromatogramViewer)e;				Taxon t = cv.getTaxon();				int ic = cv.getCenterBase();				if (t != null) {					temp.addLine("viewChromat " + ic + " " + t.getNumber(), cv);				}			}		}  	 	return temp;  	 }	public void endJob(){		if (data != null)			data.removeListener(this);		super.endJob();	}	/*.................................................................................................................*/   	 public boolean isSubstantive(){   	 	return false;   	 }	/*.................................................................................................................*/	public void setTableAndData(MesquiteTable table, CharacterData data){		this.table = (CMTable)table;		if (this.data != data){			if (this.data != null)				this.data.removeListener(this);			data.addListener(this);		}				this.data = data;		boolean b = false;		if (data!=null) {			Associable tInfo= data.getTaxaInfo(false);			if (tInfo!=null)				b = tInfo.anyAssociatedObject(ChromaseqUtil.aceRef);					}		viewChromatTool.setEnabled(b);	}	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/	public void changed(Object caller, Object obj, Notification notification){		int code = Notification.getCode(notification);		if (obj == data) { // && (code != MesquiteListener.SELECTION_CHANGED)){ //selection change handled elsewhere			matrixChanged(false);		}	}		/*.................................................................................................................*/	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */	public CompatibilityTest getCompatibilityTest(){		return new RequiresAnyDNAData();	}		//if snapshot, then interpret ic and overall base and make window	public Object showWindow(int ic, int it, boolean snapshot){			Taxon taxon = data.getTaxa().getTaxon(it);			ChromatogramViewer cv = null;			if (!snapshot)				cv = findViewer(taxon);			if (cv!=null && ic<cv.getWindow().getTotalNumPeaks()) {				cv.getWindow().show();				cv.getWindow().scrollToMatrixBase(ic);				return cv;			}			//if no viewer module active, hire one			Associable tInfo = data.getTaxaInfo(false);			String path = null;						if (tInfo != null) 					path = ChromaseqUtil.getStringAssociated(tInfo, ChromaseqUtil.aceRef, it);			if (!StringUtil.blank(path)) {				path = MesquiteFile.composePath(getProject().getHomeFile().getDirectoryName(), path);				if (!MesquiteFile.fileExists(path)){					discreetAlert( "The stored path to the chromatogram files appears invalid.  Perhaps the processed chromatograms directory has been moved relative to the NEXUS file, or perhaps your operating system has modified the file names. (path:  " + path + ")");					return null;				}				cv = (ChromatogramViewer)hireEmployee(ChromatogramViewer.class, null);				if (cv!=null) {   					   					cv.showContigs(it, table, (DNAData)data);    					if (cv.getWindow() == null)   						return null;   					if (snapshot)   						cv.getWindow().scrollToOverallBase(ic);   					else    						cv.getWindow().scrollToMatrixBase(ic);   					if (cv.getWindow().getContigDisplay().getContigOverviewPanel()!=null)   						cv.getWindow().getContigDisplay().getContigOverviewPanel().centerPanelAtUniversalBase(ic,true);				}			} 			return cv;	}  	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {    	 	if (checker.compare(this.getClass(), "show chromatogram for touched taxon", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "viewChromatFromTool")) { 	 		if (table!=null && data !=null){    				//boolean optionDown = arguments.indexOf("option")>=0; 					   	 		MesquiteInteger io = new MesquiteInteger(0);	   			int column= MesquiteInteger.fromString(arguments, io);	   			 	   			int it= MesquiteInteger.fromString(arguments, io);	   			if (it>=0 && column >=-1) {	   				if (column<0)	   					column=0;	   				return showWindow(column, it, false);	   			}	   		}   	 	}    	 	else if (checker.compare(this.getClass(), "show chromatogram (used in snapshots)", "[overall base touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "viewChromat")) { 	 		if (table!=null && data !=null){    				//boolean optionDown = arguments.indexOf("option")>=0; 					   	 		MesquiteInteger io = new MesquiteInteger(0);	   			int overallBase= MesquiteInteger.fromString(arguments, io);	   			 	   			int it= MesquiteInteger.fromString(arguments, io);	   			if (it>=0 && overallBase >=-1 && MesquiteInteger.isCombinable(overallBase)) {	   				if (overallBase<0)	   					overallBase=0;	   				return showWindow(overallBase, it, true);						   			}	   		}   	 	}   	 	else    	 		return  super.doCommand(commandName, arguments, checker);		return null;   	 }	ChromatogramViewer findViewer(Taxon taxon){    	 		//next, find if already has DataWindowMaker module for it			for (int i = 0; i<getNumberOfEmployees(); i++) {				Object e=getEmployeeVector().elementAt(i);				if (e instanceof ChromatogramViewer) {					ChromatogramViewer dwm = (ChromatogramViewer)e;					Taxon d = dwm.getTaxon();					if (d == taxon) {						if (dwm.getModuleWindow() !=null) {							dwm.getModuleWindow().setVisible(true);						}						return dwm;					}				}			}			return null;	}	/*.................................................................................................................*/	public void tableSelectionChanged(){   		synchSelection(true);   	 }	/*.................................................................................................................*   	 public ChromatogramWindow showContig(Taxon taxon, Contig contig){		MesquiteModule windowServer = hireNamedEmployee ( WindowHolder.class, "#WindowBabysitter");		int count = 0;		for (int i=0; i<contig.getNumReadsToShow(); i++) {			if (!StringUtil.blank(contig.getRead(i).getABIFile()) && !StringUtil.blank(contig.getRead(i).getABIFilePath()))				count++;		}		if (count == 0)			return null;		String[] fileNames = new String[count];		String[] paths = new String[count];		Read[] reads = new Read[count];		count = 0;		for (int i=0; i<contig.getNumReadsToShow(); i++) {			String abiFile = contig.getRead(i).getABIFile();			String abiFilePath = contig.getRead(i).getABIFilePath();			reads[count] = contig.getRead(i);			if (!StringUtil.blank(abiFile) && !StringUtil.blank(abiFilePath)) {				fileNames[count] = abiFile;				paths[count] = abiFilePath;				count++;			}		}		ChromatogramWindow w = ChromatogramWindow.showChromatogram(paths, fileNames, contig, reads, colorByQuality.getValue(), table, data, taxon, windowServer,this);		if (w != null) {			windows.addElement(w);		}		return w;   	 }	/*.................................................................................................................*/	void matrixChanged(boolean syncPosition){		if (data == null)			return;		for (int i = 0; i<getNumberOfEmployees(); i++) {			Object e=getEmployeeVector().elementAt(i);			if (e instanceof ChromatogramViewer) {				ChromatogramViewer dwm = (ChromatogramViewer)e;				dwm.matrixChanged(syncPosition);			}		}	}	/*.................................................................................................................*/	void synchSelection(boolean syncPosition){		if (data == null)			return;		for (int i = 0; i<getNumberOfEmployees(); i++) {			Object e=getEmployeeVector().elementAt(i);			if (e instanceof ChromatogramViewer) {				ChromatogramViewer dwm = (ChromatogramViewer)e;				dwm.synchSelection(syncPosition);			}		}	}	public String getCellExplanation(int ic, int it){		if (it<0 || data == null)			return null;   		if (ic < 0) {   			Taxa taxa =data.getTaxa();   			if (taxa==null)   				return null;   			return  ChromaseqUtil.getStringAssociated(taxa, ChromaseqUtil.origTaxonNameRef, it);   		}   		return null;	}	/*.................................................................................................................*/    	 public String getName() {		return "View Chromatogram Coordinator";   	 }	/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Coordinates the display of chromatograms." ;   	 }	/*.................................................................................................................*/  	 public boolean showCitation(){  	 	return true;  	 }  	 }