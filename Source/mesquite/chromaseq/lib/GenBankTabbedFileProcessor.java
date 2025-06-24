package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteString;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;

public class GenBankTabbedFileProcessor {

	String sampleCodeListPath = null;
	String sampleCodeListFile = null;
	String sampleCodeList = "";
	Parser sampleCodeListParser = null;

	
	public boolean chooseCodeFile() {
		MesquiteString dnaNumberListDir = new MesquiteString();
		MesquiteString dnaNumberListFile = new MesquiteString();
		String s = MesquiteFile.openFileDialog("Choose file containing sample codes and GenBank numbers", dnaNumberListDir, dnaNumberListFile);
		if (!StringUtil.blank(s)) {
			sampleCodeListPath = s;
			sampleCodeListFile = dnaNumberListFile.getValue();
			return processCodesFile();
		}
		return false;
	}
	/*.................................................................................................................*/
	public boolean processCodesFile() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				sampleCodeListParser = new Parser(sampleCodeList);
				return true;
			}
		}	
		return false;

	}
	
	
	/*.................................................................................................................*/
	public boolean codesMatch(String OTUIDCode, String codeInFile) {
		if (StringUtil.blank(OTUIDCode))
			return false;
		if (!OTUIDCode.contains("/"))  // doesn't contain multiple entries
			return OTUIDCode.equalsIgnoreCase(codeInFile);
		Parser parser = new Parser(OTUIDCode);
		parser.setPunctuationString("/");
		String code = parser.getFirstToken();
		code = StringUtil.stripBoundingWhitespace(code);
		while (StringUtil.notEmpty(code)) {
			if (code.equalsIgnoreCase(codeInFile))
				return true;
			code = parser.getNextToken();
			code = StringUtil.stripBoundingWhitespace(code);
		}
		return false;

	}
	
	
	/** This scans the file listing the accessions, and sees if a particular entry is in the list.  The file format
	 * should be as follows:  Tab-delimited file, four columns.  
	 * First column is sample code.
	 * Second column is gene name
	 * Third column is fragment name
	 * Fourth column is GenBank accession number. 
	 * */

	public String codeIsInCodeListFile(String codeToMatch, String geneToMatch, String fragmentToMatch, String alternativeFragmentToMatch) {
		if (sampleCodeListParser==null)
			return null;
		if (StringUtil.blank(codeToMatch)||StringUtil.blank(geneToMatch))
			return null;
		sampleCodeListParser.setPosition(0);
		Parser subParser = new Parser();
		String line = sampleCodeListParser.getRawNextDarkLine();
		while (StringUtil.notEmpty(line)) {
			subParser.setString(line);
			MesquiteInteger pos = new MesquiteInteger(0);
			String code = StringUtil.getNextTabbedToken(line, pos);
			String gene = StringUtil.getNextTabbedToken(line, pos);
			String fragment = StringUtil.getNextTabbedToken(line, pos); 
			fragment = StringUtil.stripBoundingWhitespace(fragment);
			if (StringUtil.notEmpty(fragmentToMatch)) {
				if (codesMatch(codeToMatch, code) && geneToMatch.equalsIgnoreCase(gene)&& (fragmentToMatch.equalsIgnoreCase(fragment) || (StringUtil.notEmpty(alternativeFragmentToMatch) && alternativeFragmentToMatch.equalsIgnoreCase(fragment)))) {
					return line;
				}
			}
			else if (codesMatch(codeToMatch, code) && geneToMatch.equalsIgnoreCase(gene)) {
				return line;
			}
			line = sampleCodeListParser.getRawNextDarkLine();
		}
		return null;
	}

	public String getGenBankNumberFromCodeFileLine(String line) {
		Parser subParser = new Parser(line);
		MesquiteInteger pos = new MesquiteInteger(0);
		String code = StringUtil.getNextTabbedToken(line, pos);
		String gene = StringUtil.getNextTabbedToken(line, pos);
		String fragment = StringUtil.getNextTabbedToken(line, pos); 
		String number = StringUtil.getNextTabbedToken(line, pos); 
		number = StringUtil.stripBoundingWhitespace(number);
		return number;
	}



	
}
