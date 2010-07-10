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


package mesquite.chromaseq.SampleAndPrimerFileNameParser;

import java.awt.*;
import java.util.regex.*;

import org.dom4j.*;

import mesquite.lib.*;

public class ChromFileNameParsing implements Listable, Explainable {
	
	public String dnaCodeStartToken = "DNA";   
	public String dnaCodeEndToken = "_";  
	public String dnaCodeSuffixToken = ".";  
	String dnaCodeRemovalToken= "-";
	
	public String primerStartToken="_";
	public String primerEndToken = "_";
	
	public String path;
	
	boolean sampleCodeFirst = true;
	
	public String name = "default";
	public String explanation;
	
	public ChromFileNameParsing() {
	}

	public ChromFileNameParsing(ChromFileNameParsing rule) {
		dnaCodeStartToken = rule.dnaCodeStartToken;
		dnaCodeEndToken = rule.dnaCodeEndToken;
		dnaCodeSuffixToken = rule.dnaCodeSuffixToken;
		dnaCodeRemovalToken = rule.dnaCodeRemovalToken;
		primerStartToken = rule.primerStartToken;
		primerEndToken = rule.primerEndToken;
		name = rule.name;
		sampleCodeFirst = rule.sampleCodeFirst;
	}

	public void setPath(String path){
		this.path = path;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
	public String getStartToken(){
		return dnaCodeStartToken;
	}
	public String getExplanation(){
		return explanation;
	}
	String getProcessedTokenForWrite(String s) {
		if (" ".equals(s))
		return "\\ ";
	else if (StringUtil.blank(s))
		return " ";
	else
		return s;
	}
	public String getXML(){
		Element mesquiteElement = DocumentHelper.createElement("mesquite");
		Document doc = DocumentHelper.createDocument(mesquiteElement);
		Element nameRules = DocumentHelper.createElement("chromFileNameParsingRules");
		mesquiteElement.add(nameRules);
		XMLUtil.addFilledElement(nameRules, "version","1");
		Element boundedByTokensElement = DocumentHelper.createElement("boundedByTokens");
		nameRules.add(boundedByTokensElement);
		XMLUtil.addFilledElement(boundedByTokensElement, "name",name);
		XMLUtil.addFilledElement(boundedByTokensElement, "sampleCodeFirst",MesquiteBoolean.toTrueFalseString(sampleCodeFirst));
		XMLUtil.addFilledElement(boundedByTokensElement, "dnaCodeStartToken",DocumentHelper.createCDATA(dnaCodeStartToken));
		XMLUtil.addFilledElement(boundedByTokensElement, "dnaCodeEndToken",DocumentHelper.createCDATA(dnaCodeEndToken));
		XMLUtil.addFilledElement(boundedByTokensElement, "dnaCodeSuffixToken",DocumentHelper.createCDATA(dnaCodeSuffixToken));
		XMLUtil.addFilledElement(boundedByTokensElement, "dnaCodeRemovalToken",DocumentHelper.createCDATA(dnaCodeRemovalToken));
		XMLUtil.addFilledElement(boundedByTokensElement, "primerStartToken",DocumentHelper.createCDATA(primerStartToken));		
		XMLUtil.addFilledElement(boundedByTokensElement, "primerEndToken",DocumentHelper.createCDATA(primerEndToken));		
		return XMLUtil.getDocumentAsXMLString(doc);
	}
	public void save(String path, String name){
		this.name = name;
		this.path = path;
		MesquiteFile.putFileContents(path, getXML(), true); 	
	}

	public void save(){
		if (path!=null)
			MesquiteFile.putFileContents(path, getXML(), true); 	
	}

	/*.................................................................................................................*/
	public boolean readXML(String contents) {
		Element root = XMLUtil.getRootXMLElementFromString("mesquite", contents);
		if (root==null)
			return false;
		
		Element chromFileNameParsingRules = root.element("chromFileNameParsingRules");
		if (chromFileNameParsingRules != null) {
			Element versionElement = chromFileNameParsingRules.element("version");
			if (versionElement == null || !versionElement.getText().equals("1")) {
				return false;
			}
			Element boundedByTokens = chromFileNameParsingRules.element("boundedByTokens");
			if (boundedByTokens == null) {
				return false;
			}
			name = boundedByTokens.elementText("name");
			dnaCodeStartToken = boundedByTokens.elementText("dnaCodeStartToken");
			dnaCodeEndToken = boundedByTokens.elementText("dnaCodeEndToken");
			dnaCodeSuffixToken = boundedByTokens.elementText("dnaCodeSuffixToken");
			dnaCodeRemovalToken = boundedByTokens.elementText("dnaCodeRemovalToken");			
			primerStartToken = boundedByTokens.elementText("primerStartToken");			
			primerEndToken = boundedByTokens.elementText("primerEndToken");
			sampleCodeFirst = MesquiteBoolean.fromTrueFalseString(boundedByTokens.elementText("sampleCodeFirst"));
			//primerListPath = boundedByTokens.elementTextTrim("primerListPath");
			//dnaNumberListPath = boundedByTokens.elementTextTrim("dnaNumberListPath");
			//translateSampleCodes = MesquiteBoolean.fromTrueFalseString(boundedByTokens.elementTextTrim("translateSampleCodes"));
		} else {
			return false;
		}
		return true;
	/*Parser parser = new Parser();
		Parser subParser = new Parser();
		parser.setString(contents);
		boolean acceptableVersion = false;
		if (!parser.isXMLDocument(false))   // check if XML
			return false;
		if (!parser.resetToMesquiteTagContents())   // check if has mesquite tag
			return false;
		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		if ("chromFileNameParsingRules".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
			parser.setString(tagContent);
			tagContent = parser.getNextXMLTaggedContent(nextTag);
			String subTagContent;
			while (!StringUtil.blank(tagContent)) {
				if ("version".equalsIgnoreCase(nextTag.getValue())) {
					if ("1".equalsIgnoreCase(tagContent))
						acceptableVersion = true;
					else
						return false;
				}
				else if ("boundedByTokens".equalsIgnoreCase(nextTag.getValue()) && acceptableVersion) {
					subParser.setString(tagContent);
					subTagContent = subParser.getNextXMLTaggedContent(nextTag);
					while (!StringUtil.blank(nextTag.getValue())) {
						if ("name".equalsIgnoreCase(nextTag.getValue()))
							name = StringUtil.cleanXMLEscapeCharacters(subTagContent);
						else if ("dnaCodeStartToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeStartToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("dnaCodeEndToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeEndToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("dnaCodeSuffixToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeSuffixToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("dnaCodeRemovalToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeRemovalToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("primerStartToken".equalsIgnoreCase(nextTag.getValue()))
							primerStartToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("primerEndToken".equalsIgnoreCase(nextTag.getValue()))
							primerEndToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("sampleCodeFirst".equalsIgnoreCase(nextTag.getValue()))
							sampleCodeFirst = MesquiteBoolean.fromTrueFalseString(subTagContent);
					/*		else if ("primerListPath".equalsIgnoreCase(nextTag.getValue()))
							primerListPath = StringUtil.cleanXMLEscapeCharacters(subTagContent);
						else if ("dnaNumberListPath".equalsIgnoreCase(nextTag.getValue()))
							dnaNumberListPath = StringUtil.cleanXMLEscapeCharacters(subTagContent);
						else if ("translateSampleCodes".equalsIgnoreCase(nextTag.getValue()))
							translateSampleCodes = MesquiteBoolean.fromTrueFalseString(subTagContent);
						/
						subTagContent = subParser.getNextXMLTaggedContent(nextTag);
					}
				}
				tagContent = parser.getNextXMLTaggedContent(nextTag);
			}
		} else
			return false;
		return true;*/
	}
	
	/*.................................................................................................................*/
	public String processTokenAfterRead(String s) {
		if ("\\ ".equals(s))
			return " ";
		else if (StringUtil.blank(s))
			return "";
		else
			return s;
	}

	/*.................................................................................................................*/
	public boolean queryOptions(String name) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "File Naming Rule",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		if (!StringUtil.blank(name))
			dialog.addLabel("Rules for Chromatogram File Names: "+name);
		else
			dialog.addLabel("Rules for Chromatogram File Names");
		Checkbox sampleCodeFirstBox = dialog.addCheckBox("sample code precedes primer name", sampleCodeFirst);
		SingleLineTextField dnaCodeStartField = dialog.addTextField("String before sample code:", dnaCodeStartToken, 8, true);
		SingleLineTextField dnaCodeEndField = dialog.addTextField("String after sample code:", dnaCodeEndToken, 8, true);
		SingleLineTextField dnaCodeSuffixField = dialog.addTextField("String before sample code suffix:", dnaCodeSuffixToken, 4, true);
		SingleLineTextField dnaCodeRemovalField = dialog.addTextField("Characters to be removed from sample code:", dnaCodeRemovalToken, 4, true);
		dialog.addHorizontalLine(2);
		SingleLineTextField primerStartField = dialog.addTextField("String before primer name:", primerStartToken, 8, true);
		SingleLineTextField primerEndField = dialog.addTextField("String after primer name:", primerEndToken, 8, true);
		
		
//		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);
		
		
		String s = "Mesquite searches within the name of each chromatogram file for both a code indicating the sample (e.g., a voucher number) and the primer name. ";
		s+= "To allow this, you must indicate the string of characters that appears immediately before the sample code, and immediately after, as well as the strings before and after the primer name. ";
		s+= "Those strings cannot also appear within the sample code and primer name.\n";
		dialog.appendToHelpString(s);
		
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			dnaCodeStartToken = dnaCodeStartField.getText();
			dnaCodeEndToken = dnaCodeEndField.getText();
			dnaCodeSuffixToken = dnaCodeSuffixField.getText();
			dnaCodeRemovalToken = dnaCodeRemovalField.getText();
			primerStartToken = primerStartField.getText();
			primerEndToken = primerEndField.getText();
			sampleCodeFirst = sampleCodeFirstBox.getState();
//			translateSampleCodes = translateCodesBox.getState();
		}
		//storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	
	/*.................................................................................................................*/
	public String getStringPiece(MesquiteModule ownerModule, String s, String startToken, String endToken, MesquiteString remainder, StringBuffer logBuffer, String message, 
			MesquiteString startTokenResult){
		String piece="";
		if (remainder!=null)
			remainder.setValue(s);
		
		if (!StringUtil.blank(startToken)  || " ".equals(startToken)) {  
			if (s.indexOf(startToken)>-1) {
				piece = s.substring(s.indexOf(startToken)+startToken.length(), s.length());  // getting substring that starts with DNA number
				if (startTokenResult != null) {
					startTokenResult.setValue(startToken);
				}
			}
			else {
				// check to see if it's a regex and if it matches the string
				try {
					Pattern startTokenPattern = Pattern.compile(startToken);
					Matcher matcher = startTokenPattern.matcher(s);
					if (matcher.find()) {
						// get the part of the match after the dna
						int matchIndex = matcher.end();
						piece = s.substring(matchIndex, s.length());
						String group = matcher.group();
						if (startTokenResult != null) {
							startTokenResult.setValue(group);
						}
					}
				} catch (Exception e) {
					// don't do anything, fall through to error 
				}
				// didn't find it via regex so return
				if (StringUtil.blank(piece)) {
					if (ownerModule!=null)
						ownerModule.echoStringToFile(" ** Can't extract " + message + " from file name: " + s, logBuffer);					
					return null;
				}
			}
		} 
		else
			piece = s;
		remainder.setValue(piece);
		int regexMatchIndex = -1;
		try {
			Pattern endTokenPattern = Pattern.compile(endToken);
			Matcher endTokenMatcher = endTokenPattern.matcher(piece);
			if (endTokenMatcher.find()) {
				regexMatchIndex = endTokenMatcher.start();
			}
		} catch (Exception e) {}
		if (!StringUtil.blank(endToken) || " ".equals(endToken)) {
			if (remainder!=null)
				if (piece.indexOf(endToken)>=0) {
					remainder.setValue(piece.substring(piece.indexOf(endToken), piece.length())); // now wiping out rest, but NOT endToken   +endToken.length()
				} else if (regexMatchIndex > 0) {
					remainder.setValue(piece.substring(piece.indexOf(regexMatchIndex), piece.length()));
				}
			if (piece.indexOf(endToken)>=0) {
				return piece.substring(0,piece.indexOf(endToken));
			} else if (regexMatchIndex > 0) {
				return piece.substring(0, regexMatchIndex);
			} else {
				return piece;
			}
		} 
		
		return null;
	}
	/*.................................................................................................................
	 * 
	 * DANNY -- added startTokenResult to store what actually is matched in the case of regexes
	 * */
	public boolean parseFileName(MesquiteModule ownerModule, String fileName, MesquiteString sampleCode, MesquiteString sampleCodeSuffix, MesquiteString primerName, StringBuffer logBuffer, 
			MesquiteString startTokenResult){
		String primerNamePiece="";
		String sampleCodePiece = "";
		MesquiteString remainder = new MesquiteString();
		
		//	Finding and processing the sample code 
		if (sampleCodeFirst) {
			sampleCodePiece = getStringPiece(ownerModule, fileName, dnaCodeStartToken, dnaCodeEndToken, remainder, logBuffer, "sample code", startTokenResult);
		if (sampleCodePiece==null)
				return false;
			primerNamePiece = getStringPiece(ownerModule, remainder.getValue(), primerStartToken, primerEndToken, remainder, logBuffer, "primer name", null);
		}		
		else	if (sampleCodeFirst) {
			// TODO: DRM FIX THIS
			primerNamePiece = getStringPiece(ownerModule, fileName, primerStartToken, primerEndToken, remainder, logBuffer, "primer name", startTokenResult);
			if (primerNamePiece==null)
				return false;
			sampleCodePiece = getStringPiece(ownerModule, remainder.getValue(), dnaCodeStartToken, dnaCodeEndToken, remainder, logBuffer, "sample code", startTokenResult);
		}		
		if (sampleCodePiece==null || primerNamePiece==null)
			return false;
		sampleCodePiece = StringUtil.removeCharacters(sampleCodePiece,dnaCodeRemovalToken); //TODO: give user control of this

		String suffix = "";
		if (!StringUtil.blank(dnaCodeSuffixToken)) {
			if (sampleCodePiece.indexOf(dnaCodeSuffixToken)>-1) {
				suffix = " " + sampleCodePiece.substring(sampleCodePiece.indexOf(dnaCodeSuffixToken)+1,sampleCodePiece.length());
				sampleCodePiece = sampleCodePiece.substring(0,sampleCodePiece.indexOf(dnaCodeSuffixToken));
			}
		}
		sampleCode.setValue(sampleCodePiece);
		sampleCodeSuffix.setValue(suffix);
		primerName.setValue(primerNamePiece);
		
		return true;
	}

	
}
