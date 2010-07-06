/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.
Version 0.980   July 2010
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.lib;

public class PhredPhrapAgent {
	
	ChromFileNameParsing nameParsingRule;
	String nameParsingRulesName="";

	String phredParamPath; 
	String phredPath;
	String primerListPath;
	String sampleCodeListPath;

	String phrapOptions = "-qual_show 20 -vector_bound 0 ";
	String phredOptions = "";
	String fileExtension = ".ab1";
	boolean requiresExtension=true;
	boolean translateSampleCodes = true;

	int qualThresholdForTrim = 20;
	int qualThresholdForLowerCase = 49;
	boolean truncateMixedEnds = true;
	int mixedEndWindow = 10;
	int mixedEndThreshold = 5;
	boolean renameContigsInAceFiles = true;
	boolean addFragName = false;

	boolean processPolymorphisms = true;
	boolean verbose = true;
	boolean singleTaxaBlock = false;
	boolean addPhrapFailures = true;

	double polyThreshold = 0.3;
	boolean backupOriginals = true;

	private boolean primerAndSampleCodeInfoFromDatabase;
	private boolean uploadResultsToDatabase = false;
	private String databaseURL = "";

	
	public PhredPhrapAgent() {

	
	}


	
	
	
	public ChromFileNameParsing getNameParsingRule() {
		return nameParsingRule;
	}
	
	public void setNameParsingRule(ChromFileNameParsing nameParsingRule) {
		this.nameParsingRule = nameParsingRule;
	}


	public String getNameParsingRulesName() {
		return nameParsingRulesName;
	}
	
	public void setNameParsingRulesName(String nameParsingRulesName) {
		this.nameParsingRulesName = nameParsingRulesName;
	}


	public String getPhredParamPath() {
		return phredParamPath;
	}


	public void setPhredParamPath(String phredParamPath) {
		this.phredParamPath = phredParamPath;
	}


	public String getPhredPath() {
		return phredPath;
	}


	public void setPhredPath(String phredPath) {
		this.phredPath = phredPath;
	}


	public String getPrimerListPath() {
		return primerListPath;
	}


	public void setPrimerListPath(String primerListPath) {
		this.primerListPath = primerListPath;
	}


	public String getSampleCodeListPath() {
		return sampleCodeListPath;
	}


	public void setSampleCodeListPath(String sampleCodeListPath) {
		this.sampleCodeListPath = sampleCodeListPath;
	}


	public String getPhrapOptions() {
		return phrapOptions;
	}


	public void setPhrapOptions(String phrapOptions) {
		this.phrapOptions = phrapOptions;
	}


	public String getPhredOptions() {
		return phredOptions;
	}


	public void setPhredOptions(String phredOptions) {
		this.phredOptions = phredOptions;
	}


	public String getFileExtension() {
		return fileExtension;
	}


	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}


	public boolean isRequiresExtension() {
		return requiresExtension;
	}


	public void setRequiresExtension(boolean requiresExtension) {
		this.requiresExtension = requiresExtension;
	}


	public boolean isTranslateSampleCodes() {
		return translateSampleCodes;
	}


	public void setTranslateSampleCodes(boolean translateSampleCodes) {
		this.translateSampleCodes = translateSampleCodes;
	}


	public int getQualThresholdForTrim() {
		return qualThresholdForTrim;
	}


	public void setQualThresholdForTrim(int qualThresholdForTrim) {
		this.qualThresholdForTrim = qualThresholdForTrim;
	}


	public int getQualThresholdForLowerCase() {
		return qualThresholdForLowerCase;
	}


	public void setQualThresholdForLowerCase(int qualThresholdForLowerCase) {
		this.qualThresholdForLowerCase = qualThresholdForLowerCase;
	}


	public boolean isTruncateMixedEnds() {
		return truncateMixedEnds;
	}


	public void setTruncateMixedEnds(boolean truncateMixedEnds) {
		this.truncateMixedEnds = truncateMixedEnds;
	}


	public int getMixedEndWindow() {
		return mixedEndWindow;
	}


	public void setMixedEndWindow(int mixedEndWindow) {
		this.mixedEndWindow = mixedEndWindow;
	}


	public int getMixedEndThreshold() {
		return mixedEndThreshold;
	}


	public void setMixedEndThreshold(int mixedEndThreshold) {
		this.mixedEndThreshold = mixedEndThreshold;
	}


	public boolean isRenameContigsInAceFiles() {
		return renameContigsInAceFiles;
	}


	public void setRenameContigsInAceFiles(boolean renameContigsInAceFiles) {
		this.renameContigsInAceFiles = renameContigsInAceFiles;
	}


	public boolean isAddFragName() {
		return addFragName;
	}


	public void setAddFragName(boolean addFragName) {
		this.addFragName = addFragName;
	}


	public boolean isProcessPolymorphisms() {
		return processPolymorphisms;
	}


	public void setProcessPolymorphisms(boolean processPolymorphisms) {
		this.processPolymorphisms = processPolymorphisms;
	}


	public boolean isVerbose() {
		return verbose;
	}


	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}


	public boolean isSingleTaxaBlock() {
		return singleTaxaBlock;
	}


	public void setSingleTaxaBlock(boolean singleTaxaBlock) {
		this.singleTaxaBlock = singleTaxaBlock;
	}


	public boolean isAddPhrapFailures() {
		return addPhrapFailures;
	}


	public void setAddPhrapFailures(boolean addPhrapFailures) {
		this.addPhrapFailures = addPhrapFailures;
	}


	public double getPolyThreshold() {
		return polyThreshold;
	}


	public void setPolyThreshold(double polyThreshold) {
		this.polyThreshold = polyThreshold;
	}


	public boolean isBackupOriginals() {
		return backupOriginals;
	}


	public void setBackupOriginals(boolean backupOriginals) {
		this.backupOriginals = backupOriginals;
	}


	public boolean isPrimerAndSampleCodeInfoFromDatabase() {
		return primerAndSampleCodeInfoFromDatabase;
	}


	public void setPrimerAndSampleCodeInfoFromDatabase(
			boolean primerAndSampleCodeInfoFromDatabase) {
		this.primerAndSampleCodeInfoFromDatabase = primerAndSampleCodeInfoFromDatabase;
	}


	public boolean isUploadResultsToDatabase() {
		return uploadResultsToDatabase;
	}


	public void setUploadResultsToDatabase(boolean uploadResultsToDatabase) {
		this.uploadResultsToDatabase = uploadResultsToDatabase;
	}


	public String getDatabaseURL() {
		return databaseURL;
	}


	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}

}
