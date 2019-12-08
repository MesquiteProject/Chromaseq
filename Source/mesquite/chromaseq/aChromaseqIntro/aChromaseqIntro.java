/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.aChromaseqIntro;import mesquite.lib.duties.*;import mesquite.lib.*;/* ======================================================================== */public class aChromaseqIntro extends PackageIntro {	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		return true;	}	public Class getDutyClass(){		return aChromaseqIntro.class;	}	/*.................................................................................................................*/	public String getManualPath(){		return getPackagePath() +"docs/index.html";  	}	/** returns the URL of the notices file for this module so that it can phone home and check for messages */	/*.................................................................................................................*/	public String  getHomePhoneNumber(){ 		if (MesquiteTrunk.debugMode)			return "http://chromaseq.mesquiteproject.org/noticesAndUpdates/noticesDev.xml";		else if (isPrerelease()) 			return "http://chromaseq.mesquiteproject.org/noticesAndUpdates/noticesPrerelease.xml";		else			return "http://chromaseq.mesquiteproject.org/noticesAndUpdates/notices.xml";		/*	version 1.3 and before: 		 if (MesquiteTrunk.debugMode)			return "http://mesquiteproject.org/packages/chromaseq/noticesDev.xml";		else if (isPrerelease()) 			return "http://mesquiteproject.org/packages/chromaseq/noticesPrerelease.xml";		else			return "http://mesquiteproject.org/packages/chromaseq/notices.xml";			*/	}	/*.................................................................................................................*/	public String getExplanation() {		return "Chromaseq is a package of Mesquite modules providing tools for processing and displaying chromatograms and sequence data.";	}	/*.................................................................................................................*/	public String getName() {		return "Chromaseq Package";	}	/*.................................................................................................................*/	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/	public String getPackageName(){		return "Chromaseq Package";	}	/*.................................................................................................................*/	public boolean isPrerelease(){		return true;  	}	/*.................................................................................................................*/	/** Returns citation for a package of modules*/	public String getPackageCitation(){		if (isPrerelease())			return "Maddison, D.R., & W.P. Maddison.  2018.  Chromaseq.  A package for processing chromatograms and sequence data in Mesquite. Prerelease version " + getPackageVersion() + " (build " + getBuildVersion() + ").";		return "Maddison, D.R., & W.P. Maddison.  2018.  Chromaseq.  A package for processing chromatograms and sequence data in Mesquite. Version " + getPackageVersion() + ".";	}	/*.................................................................................................................*/	/** Returns version for a package of modules*/	public String getPackageVersion(){		return "1.5+";	}	/*.................................................................................................................*/	/** Returns version for a package of modules as an integer*/	public int getPackageVersionInt(){		return 1500;	}	/*.................................................................................................................*/	public String getPackageDateReleased(){		return "7 December 2019 ";	}	/*.................................................................................................................*/	/** Returns build number for a package of modules as an integer*/	public int getPackageBuildNumber(){		return 64;	}/* release history:  	0.981, build 12    25 July 2010 - first beta release 	0.982, build 15    14 August 2010 - second beta	0.983, build 18    21 September 2010 	0.984, build 19    3 October 2010 	0.986, build 21    1 June 2011 - third beta	0.990, build 25    4 October 2011 - first public release, fourth beta	1.0, build 28		23 December 2011	1.01               	10 December 2013	1.1                 	19 August 2014	1.11 (build 35)	29 August 2014	1.12 (build 36)	2 September 2014	1.2 (build 40)  27 June 2016	1.3 (build 48)  12 September 2017	1.31 (build 53)  4 May 2018	1.5 (build 61)  27 December 2018		 */	/*.................................................................................................................*/	/** Returns the  integer version of the MesquiteCore version  that this package requires to function*/	public int getMinimumMesquiteVersionRequiredInt(){		return 360;  	}	/*.................................................................................................................*/	/** Returns the String version of the MesquiteCore version number that this package requires to function*/	public String getMinimumMesquiteVersionRequired(){		return "3.6";  	}	/*.................................................................................................................*/	public String getPackageURL(){		return "http://chromaseq.mesquiteproject.org";  	}	/*.................................................................................................................*/	/** Returns whether there is a splash banner*/	public boolean hasSplash(){		return true; 	}	/*.................................................................................................................*/	public int getVersionOfFirstRelease(){		return 275;  	}}