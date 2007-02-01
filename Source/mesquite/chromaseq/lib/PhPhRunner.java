/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.Version 1.11, June 2006.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.lib; import mesquite.lib.*;import mesquite.categ.lib.*;import mesquite.lib.table.*;/* ======================================================================== */public abstract class  PhPhRunner extends MesquiteModule {   	 public Class getDutyClass() {   	 	return PhPhRunner.class;   	 } 	public String getDutyName() { 		return "Phred Phrap runner";   	 }	 public abstract boolean doPhredPhrap(MesquiteProject project, boolean appendIfPossible, CommandRecord commandRec);	 public abstract boolean doPhredPhrap(MesquiteProject project, boolean appendIfPossible, CommandRecord commandRec, String outputDirectory);	   	 }