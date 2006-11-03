/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.MarkSitesInViewer; import java.awt.event.KeyEvent;import java.util.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.cont.lib.*;import mesquite.lib.duties.*;import mesquite.lib.table.*;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class MarkSitesInViewer extends ChromInit {	ChromatWindow window;	static final int TOUCHED=0;	static final int DRAGGED=1;	static final int UP = 2;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {  		return true;	}		public void setWindow(MesquiteWindow w){//		ChromatogramTool moveToMatrixTool = new ChromatogramTool(this, "moveToMatrix", getPath(),"moveToMatrix.gif", 4,2,"Move to Matrix", "Move To Matrix tool", MesquiteModule.makeCommand("moveToMatrix",  this) , null);		ChromatogramTool 	trimmableTool = new ChromatogramTool(this, "markAsTrimmable", getPath(), "thumbsDown.gif", 5,13,"Mark as trimmable", "This tool marks selected cells as trimmable.", MesquiteModule.makeCommand("setTrimmable",  this) , null, MesquiteModule.makeCommand("setTrimmableDrag",  this));		ChromatogramTool 	restoreTool = new ChromatogramTool(this, "restore", getPath(), "thumbsUp.gif", 5,1,"Mark as not trimmable", "This tool marks selected cells as not trimmable.", MesquiteModule.makeCommand("restore",  this) , null,MesquiteModule.makeCommand("restoreDrag",  this));//		ChromatogramTool 	touchedTool = new ChromatogramTool(this, "touch", getPath(), "touched.gif", 1,8,"Mark as changed", "This tool marks selected cells as changed by hand.", MesquiteModule.makeCommand("touch",  this) , null, MesquiteModule.makeCommand("touchDrag",  this) );		ChromatogramTool 	pleaseCheckTool = new ChromatogramTool(this, "check", getPath(), "check.gif", 1,1,"Mark as to be checked", "This tool marks selected cells as to be checked.", MesquiteModule.makeCommand("check",  this) ,  null, MesquiteModule.makeCommand("checkDrag",  this));		trimmableTool.setWorksOnAllPanels(false);		restoreTool.setWorksOnAllPanels(false);		pleaseCheckTool.setWorksOnAllPanels(false);		trimmableTool.setWorksOnEditableSequencePanel(true);		restoreTool.setWorksOnEditableSequencePanel(true);		pleaseCheckTool.setWorksOnEditableSequencePanel(true);		w.addTool(trimmableTool);		w.addTool(restoreTool);	//	w.addTool(touchedTool);		w.addTool(pleaseCheckTool);		if (w instanceof ChromatWindow)			window = (ChromatWindow)w;	}   /*	 public void setContext(Taxon taxon, Contig contig, Read[] reads, SequencePanel[] sequences, DNAData matrixData, DNAData originalData, ContinuousData qualityData, MesquiteTable table){   	 	this.table = (CMTable)table;   	 	this.taxon = taxon;   	 	this.edited = matrixData;   	 	this.originalData = originalData;   	 	this.qualityData = qualityData;   	 	this.contig = contig;		this.reads = reads;		this.sequences = sequences;   	 }*/   	 /*.................................................................................................................*/   	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {   		 if (checker.compare(this.getClass(),  "Marks selected cells as trimmable", "[column touched][row touched]", commandName, "setTrimmable")) {   			 mark(arguments, 1, TOUCHED);   			 //Debugg.println("TOUCH Trimmable arguments: " + arguments);   		 }   		 else if (checker.compare(this.getClass(),  "Marks selected cells as trimmable", "[column touched][row touched]", commandName, "setTrimmableDrag")) {   			 mark(arguments, 1, DRAGGED);   			 //Debugg.println("DRAG Trimmable drag arguments: " + arguments);   		 }   		 else if (checker.compare(this.getClass(),  "Marks selected cells as not trimmable", "[column touched][row touched]", commandName, "restore")) {   			 mark(arguments, 0, TOUCHED);   		 }  		 else if (checker.compare(this.getClass(),  "Marks selected cells as not trimmable", "[column touched][row touched]", commandName, "restoreDrag")) {   			 mark(arguments, 0, DRAGGED);   		 }   		 else if (checker.compare(this.getClass(),  "Marks selected cells as touched", "[column touched][row touched]", commandName, "touch")) {  	 		mark(arguments, 2, TOUCHED);   	 	}  		 else if (checker.compare(this.getClass(),  "Marks selected cells as touched", "[column touched][row touched]", commandName, "touchDrag")) {   	 		mark(arguments, 2, DRAGGED);    	 	}   	 	else if (checker.compare(this.getClass(),  "Marks selected cells as to be checked", "[column touched][row touched]", commandName, "check")) {  	 		mark(arguments, 3, TOUCHED);   	 	}  	 	else if (checker.compare(this.getClass(),  "Marks selected cells as to be checked", "[column touched][row touched]", commandName, "checkDrag")) {  	 		mark(arguments, 3, DRAGGED);   	 	}		else    	 		return super.doCommand(commandName, arguments, commandRec, checker);		return null;   	 }   	 int lastTouched = MesquiteInteger.unassigned;   	 boolean draggable =true;   	 void mark(String arguments, int i, int how){  	 		if (table!=null && edited !=null ){		 		MesquiteInteger pos = new MesquiteInteger(0);		 		int site= MesquiteInteger.fromFirstToken(arguments, pos);		 		int localSite= MesquiteInteger.fromString(arguments, pos);		 		String s = ParseUtil.getToken(arguments, pos);		 	 	int whichPanel= MesquiteInteger.fromString(arguments, pos);		 	 	int cx= MesquiteInteger.fromString(arguments, pos);		 	 	if (!MesquiteInteger.isCombinable(cx))		 	 		cx = 1;		 	 	ChromViewContext context = getContext(cx);		 	 	if (context == null){		 	 		MesquiteMessage.warnProgrammer("No context found for chrominit");		 	 		return;		 	 	}				if ("false".equalsIgnoreCase(s) && whichPanel == 2){ //edited sequence		 			SequencePanel panel = context.sequences[whichPanel];		 			int ic = panel.getCanvas().matrixBaseFromSequenceBase(localSite);		 			int it = context.taxon.getNumber();		 	 		if (how==DRAGGED && draggable) {		 	 			int icStart=-2;		 	 			int icEnd=-2;		 	 			if (ic>lastTouched) {		 	 				icStart = lastTouched+1;		 	 				icEnd = ic;		 	 			}		 	 			else if (ic<lastTouched) {		 	 				icStart = ic;		 	 				icEnd = lastTouched-1;		 	 			}		 	 			for (int j=icStart; j<=icEnd && j>=0; j++)		 	 				if (table.isCellSelected(j, it) || table.isColumnSelected(j))		 	 					markSelectedCells(table, edited, it, i);		 	 				else {		 	 					setFlag(j, it, i);		 	 				}		 	 			lastTouched = ic;		 	 		}		 	 		else if (table.isCellSelected(ic, it) || table.isColumnSelected(ic)) {		 	 			markSelectedCells(table, edited, it, i);		 	 			draggable=false;		 	 		}		 	 		else {		 	 			setFlag(ic, it, i);		 	 			draggable = true;		 	 		}		 			if (how==TOUCHED) {		 				lastTouched = ic;		 						 			}		 			else if (how==UP)		 				lastTouched = MesquiteInteger.unassigned;		 		}	   		}   	 }	/*.................................................................................................................*/	public void markSelectedCells(MesquiteTable table, CharacterData data, int it, int flag) {			MesquiteInteger ms = null;			if (MesquiteLong.isCombinable(flag) &&  flag>=0)				ms = new MesquiteInteger(flag);			for (int ic=0; ic<table.getNumColumns(); ic++)				if (table.isCellSelectedAnyWay(ic, it))				 	data.setCellObject(trimmableNameRef, ic, it, ms);			table.repaintAll();			window.repaintAll();								}	/*.................................................................................................................*/	NameReference trimmableNameRef = NameReference.getNameReference("trimmable");   	private void setFlag(int ic, int it, int c){   		if (edited == null)   			return;  		if (!MesquiteLong.isCombinable(c) || c<0){			edited.setCellObject(trimmableNameRef, ic, it, null);		}		else {			MesquiteInteger ms = new MesquiteInteger(c);			edited.setCellObject(trimmableNameRef, ic, it, ms);		}		table.redrawCell(ic,it);		window.repaintAll();   	} 	/*.................................................................................................................*/    	 public String getName() {		return "Mark Sites in Viewer";   	 }	/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Supplies tools to mark sites as trimmable, etc." ;   	 }   	 }