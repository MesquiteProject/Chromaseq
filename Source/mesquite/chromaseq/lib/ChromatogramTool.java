/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.lib;import java.awt.*;import mesquite.lib.*;/* еееееееееееееееееееееееееее commands еееееееееееееееееееееееееееееее *//* includes commands,  buttons, miniscrolls/* ======================================================================== */	/** This subclass of MesquiteTool is used in charts.	*/public class ChromatogramTool extends MesquiteTool {	MesquiteCommand touchedCommand;	MesquiteCommand droppedCommand;	MesquiteCommand draggedCommand;	boolean worksOnEditableSequencePanel=true;	boolean worksOnOtherSequencePanels=true;	boolean worksOnChromatogramPanels=true;	boolean worksOnlyOnSelection=false;	boolean isInBetween = false;	public ChromatogramTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand droppedCommand,MesquiteCommand draggedCommand) {		super(initiator, name, imageDirectoryPath,  imageFileName, hotX, hotY, fullDescription, explanation);		this.touchedCommand = touchedCommand;		this.droppedCommand = droppedCommand;		this.draggedCommand = draggedCommand;		setOnlyWorksWhereSpecified(true);	}		public boolean getWorksOnEditableSequencePanel(){		return worksOnEditableSequencePanel;	}	public boolean getWorksOnOtherSequencePanels(){		return worksOnOtherSequencePanels;	}	public boolean getWorksOnChromatogramPanels(){		return worksOnChromatogramPanels;	}	public void setWorksOnEditableSequencePanel(boolean worksOnEditableSequencePanel){		this.worksOnEditableSequencePanel = worksOnEditableSequencePanel;	}	public void setWorksOnOtherSequencePanels(boolean worksOnOtherSequencePanels){		this.worksOnOtherSequencePanels = worksOnOtherSequencePanels;	}	public void setWorksOnChromatogramPanels(boolean worksOnChromatogramPanels){		this.worksOnChromatogramPanels = worksOnChromatogramPanels;	}	public void setWorksOnAllPanels (boolean b){		worksOnChromatogramPanels = b;		worksOnOtherSequencePanels = b;		worksOnEditableSequencePanel = b;	}	public void setTouchedCommand(MesquiteCommand touchedCommand){		this.touchedCommand = touchedCommand;	}	public void setDroppedCommand(MesquiteCommand droppedCommand){		this.droppedCommand = droppedCommand;	}	public void setDraggedCommand(MesquiteCommand draggedCommand){		this.draggedCommand = draggedCommand;	}	public void setIsInBetween(boolean isInBetween){		this.isInBetween = isInBetween;	}	public boolean getIsInBetween(){		return isInBetween;	}	public void dispose(){ 		if (touchedCommand!=null) 			touchedCommand.dispose();		if (droppedCommand!=null) 			droppedCommand.dispose();		if (draggedCommand!=null) 			draggedCommand.dispose();		touchedCommand=null;		droppedCommand=null;		draggedCommand=null;		super.dispose();	}		public void touched (int consensusSite, int localSite, boolean chromatogramPanel, int whichPanel, int contigID, int modifiers) {		if (touchedCommand!=null)			touchedCommand.doItMainThread(Integer.toString(consensusSite) + " " + localSite + " " + chromatogramPanel + " " +  whichPanel + " " + contigID + "  " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", touchedCommand, this), false, false);  	}	public void dropped (int consensusSite, int localSite, boolean chromatogramPanel, int whichPanel,int contigID,  int modifiers) {		if (droppedCommand!=null)			droppedCommand.doItMainThread(Integer.toString(consensusSite) + " " + localSite + " " + chromatogramPanel + " " +  whichPanel + " " + contigID + "  " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", droppedCommand, this), false, false);  	}	public void dragged (int consensusSite, int localSite, boolean chromatogramPanel, int whichPanel, int contigID, int modifiers) {		if (draggedCommand!=null)			draggedCommand.doItMainThread(Integer.toString(consensusSite) + " " + localSite + " " + chromatogramPanel + " " +  whichPanel + " " + contigID + "  " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", draggedCommand, this), false, false);  	}	public boolean getWorksOnlyOnSelection() {		return worksOnlyOnSelection;	}	public void setWorksOnlyOnSelection(boolean worksOnlyOnSelection) {		this.worksOnlyOnSelection = worksOnlyOnSelection;	}	}