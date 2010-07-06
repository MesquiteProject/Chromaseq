/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.Version 0.980   July 2010Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.ProcessChromatogramsAppend; import mesquite.lib.*;import mesquite.lib.duties.*;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class ProcessChromatogramsAppend extends FileInit { 	//for importing sequences	MesquiteProject proj = null;	FileCoordinator coord = null;	MesquiteFile file = null;	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){		getFileCoordinator().addMenuItem(MesquiteTrunk.editMenu, "Append Sequences from Chromatograms...", makeCommand("processChromatograms", this));		return true;	}	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */	public boolean requestPrimaryChoice(){		return true;  	}	/*.................................................................................................................*/	public boolean isPrerelease(){		return true;	}	/*.................................................................................................................*/	public boolean isSubstantive(){		return false;	}	/*.................................................................................................................*/	public Object doCommand(String commandName, String arguments, CommandChecker checker) {		if (checker.compare(this.getClass(), "Prepare chromatogram files for processing, and process them", null, commandName, "processChromatograms")) {			ChromatogramProcessor phphTask = (ChromatogramProcessor)hireEmployee(ChromatogramProcessor.class, "Module to process chromatograms");			if (phphTask != null)				phphTask.processChromatograms(getProject(), true);		}		else			return  super.doCommand(commandName, arguments, checker);		return null;	}	/*.................................................................................................................*/	public String getName() {		return "Append Sequences from Chromatogram Processing";	}	/*.................................................................................................................*/	public boolean showCitation() {		return false;	}	/*.................................................................................................................*/	public String getExplanation() {		return "Prepares a folder of chromatogram files for processing, and processes them.";	}	/*.................................................................................................................*/}