/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.VoucherInfoCoordinator; import mesquite.lib.*;import mesquite.chromaseq.lib.*;public class  VoucherInfoCoordinator extends VoucherInfoCoord {	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed		EmployeeNeed e = registerEmployeeNeed(VoucherInfoSource.class, "Voucher information is obtained from a source.",				"This is activated automatically.");	}	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		hireAllEmployees(VoucherInfoSource.class);		return true;	}			public VoucherInfo getVoucherInfo(String sourceID, String id){		if (sourceID == null || id == null)			return null;		EmployeeVector v = getEmployeeVector();		for (int i = 0; i<v.size(); i++){			if (v.elementAt(i) instanceof VoucherInfoSource){				VoucherInfoSource vis = (VoucherInfoSource)v.elementAt(i);				if (vis.handlesDatabase(sourceID)){					return vis.getVoucherInfo(sourceID, id);				}			}		}		return null;	}	/*.................................................................................................................*/	public String getName() {		return "Coordinator of voucher information";	}	/*.................................................................................................................*/	public boolean isPrerelease(){		return false;  	}	/*.................................................................................................................*/	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/	public int getVersionOfFirstRelease(){		return -1;  	}	/*.................................................................................................................*/	/** returns whether this module is requesting to appear as a primary choice */	public boolean requestPrimaryChoice(){		return true;  	}		/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Finds suppliers of voucher information." ;	}}