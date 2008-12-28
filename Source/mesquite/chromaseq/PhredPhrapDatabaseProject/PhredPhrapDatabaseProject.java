/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.PhredPhrapDatabaseProject; import java.io.*;import java.util.*;import java.awt.*;import java.awt.event.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.molec.lib.DNADatabaseURLSource;import mesquite.categ.lib.*;import mesquite.cont.lib.*;import mesquite.chromaseq.lib.*;/* ======================================================================== */public class PhredPhrapDatabaseProject extends GeneralFileMaker { 	private static final String EXPLANATION = "Download abi files from a database, phred/phrap them, and create a project.";		/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName){		//MesquiteTrunk.mesquiteTrunk.addMenuItem(MesquiteTrunk.fileMenu, "New Project from Chromatograms...", makeCommand("runPhredPhrap", this));		//MesquiteTrunk.mesquiteTrunk.addMenuItem(MesquiteTrunk.fileMenu, "-", null);		return true;	}	/*.................................	................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */   	public boolean requestPrimaryChoice(){   		return true;     	}	/*.................................................................................................................*/   	 public boolean isPrerelease(){   	 	return true;   	 }	/*.................................................................................................................*/   	 public boolean isSubstantive(){   	 	return false;   	 }   	/** make a new    MesquiteProject.*/ 	public MesquiteProject establishProject(String arguments){		/* UNCOMMENT FOR NETWORK DEBUGGING		 * 		 * System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");*/ 		 	 	MesquiteProject project = null; 		boolean success= false;		AbiDownloader downloader = (AbiDownloader) hireEmployee(				AbiDownloader.class, "Abi Downloader"); 		if (downloader != null){			FileCoordinator fileCoord = getFileCoordinator();			MesquiteFile thisFile = new MesquiteFile();			project = fileCoord.initiateProject(thisFile.getFileName(), thisFile);			success = downloader.downloadAbiFilesFromDb( project); 				 			fireEmployee(downloader); 			if (success){ 				project.autosave = true; 				return project; 			} 			project.developing = false; 		} else { 			MesquiteMessage.warnProgrammer("Can't find ABI downloader module."); 		} 		return null;	} 	/*.................................................................................................................*/ 	public String getName() {		return "Phred/Phrap Import from Chromatograms in Database...";   	 }	/*.................................................................................................................*/  	 public boolean showCitation() {		return false;   	 }   	 	/*.................................................................................................................*/  	 public String getExplanation() {		return EXPLANATION;   	 }	/*.................................................................................................................*/}