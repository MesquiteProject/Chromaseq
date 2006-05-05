/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.OpenCloseGaps; import java.awt.event.KeyEvent;import java.util.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.cont.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class OpenCloseGaps extends ChromInit {	CMTable table;	Taxon taxon;	Contig contig;	Read[] reads;	DNAData edited, originalData;	ContinuousData qualityData;	ChromatogramWindow window;	SequencePanel[] sequences;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {  		return true;	}		public void setWindow(MesquiteWindow w){		ChromatogramTool 	openGapTool = new ChromatogramTool(this, "insertBase", getPath(), "addChars.gif", 7, 15,"Insert Base", "This tool inserts a base in the edited sequence.", MesquiteModule.makeCommand("insertBase",  this) , null, null);		openGapTool.setWorksOnAllPanels(false);		openGapTool.setWorksOnEditableSequencePanel(true);		w.addTool(openGapTool);		openGapTool.setIsInBetween(true);		if (w instanceof ChromatogramWindow)			window = (ChromatogramWindow)w;	}   	 public void setContext(Taxon taxon, Contig contig, Read[] reads, SequencePanel[] sequences, DNAData matrixData, DNAData originalData, ContinuousData qualityData, MesquiteTable table){   	 	this.table = (CMTable)table;   	 	this.taxon = taxon;   	 	this.edited = matrixData;   	 	this.originalData = originalData;   	 	this.qualityData = qualityData;   	 	this.contig = contig;		this.reads = reads;		this.sequences = sequences;   	 }	NameReference trimmableNameRef = NameReference.getNameReference("trimmable");	/*.................................................................................................................*/    	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {    	 	if (checker.compare(this.getClass(),  "Inserts a base", "[site touched][local site][is chromatogram panel, true or false][which panel touched]", commandName, "insertBase")) {   	 		if (table!=null && edited !=null && taxon != null){		 		MesquiteInteger pos = new MesquiteInteger(0);		 		int site= MesquiteInteger.fromFirstToken(arguments, pos);		 		int localSite= MesquiteInteger.fromString(arguments, pos);		 		String s = ParseUtil.getToken(arguments, pos);		 	 	int whichPanel= MesquiteInteger.fromString(arguments, pos);		 		if ("false".equalsIgnoreCase(s) && whichPanel == 2){ //edited sequence		 			SequencePanel panel = window.getSequencePanel(whichPanel);		 			int ic = panel.getCanvas().matrixBaseFromSequenceBase(localSite);		 			int it = taxon.getNumber();		 			MesquiteBoolean dataChanged = new MesquiteBoolean(false);					int added = edited.moveCells(ic, edited.getNumChars()-1, 1, it, true, false, true, false, dataChanged);					edited.setState(ic, it, CategoricalState.unassigned);				 	edited.setCellObject(trimmableNameRef, ic, it, new MesquiteInteger(2));					if (dataChanged.getValue()) {						edited.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED), commandRec);						edited.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));					}						   			sequences[2].getCanvas().selectAndFocusLocalPosition(localSite);	  			window.repaintPanels();	  			table.repaintAll();	 	 				 		}	   		}  	 	} 		else    	 		return super.doCommand(commandName, arguments, commandRec, checker);		return null;   	 }	/*.................................................................................................................*/    	 public String getName() {		return "Insert Base";   	 }	/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Supplies tool to inserts bases in the edited sequence." ;   	 }   	 }