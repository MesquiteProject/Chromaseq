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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.*;

import mesquite.lib.*;
import mesquite.molec.lib.*;
import mesquite.tol.lib.XMLConstants;
//import mesquite.BTOL.lib.*;

/* ======================================================================== */
public class PrimerList { 
	String [] primerNames;
	String token;
	String [] fragmentNames;
	String [] sequences;
	boolean [] forward;
	String primerName;
	String fragmentName;
	int numPrimers;
	private boolean useDb;
	private String databaseURL;
	private DNADatabaseURLSource databaseURLSource =null;

	public PrimerList(String primerList) {
		this(primerList, false, null);
	}
	public PrimerList(boolean useDb, DNADatabaseURLSource databaseURLSource) {
		this.databaseURLSource = databaseURLSource;
		if (databaseURLSource!=null)
			databaseURL = databaseURLSource.getBaseURL();
		this.databaseURL = databaseURL;
		if (useDb) {
			this.useDb = useDb;
		} else {
			readTabbedPrimerFile(null);
		}
	}
	
	public PrimerList(boolean useDb, String databaseURL) {
		this.databaseURL = databaseURL;
		if (useDb) {
			this.useDb = useDb;
		} else {
			readTabbedPrimerFile(null);
		}
	}


	public PrimerList(String primerListPathOrDbUrl, boolean useDb, String databaseURL) {
		this.databaseURL = databaseURL;
		if (useDb) {
			this.useDb = useDb;
		} else {
			readTabbedPrimerFile(primerListPathOrDbUrl);
		}
	}

	
	/*
	public void readXMLPrimerFile(String primerList) {  // this does not work; just started to build this.
		String oneFragment;
		int numPrimers = 0;
		String tempList = primerList;

		Parser parser = new Parser();
		Parser subParser = new Parser();
		parser.setString(primerList);
		if (!parser.isXMLDocument(false))   // check if XML
			return;
		if (!parser.resetToMesquiteTagContents())   // check if has mesquite tag, and reset to this if so
			return;
		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);

		while (!StringUtil.blank(tagContent)) {
			if ("primers".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
				subParser.setString(tagContent);
				String subTagContent = subParser.getNextXMLTaggedContent(nextTag);
				while (!StringUtil.blank(subTagContent)) {
					if ("version".equalsIgnoreCase(nextTag.getValue())) {
						int version = MesquiteInteger.fromString(subTagContent);
						boolean acceptableVersion = (1==version);
						if (acceptableVersion) {
							parser.setString(tagContent);
							tagContent = parser.getNextXMLTaggedContent(nextTag);
							int count = -1;
							while (!StringUtil.blank(tagContent)) {
								if ("fragment".equalsIgnoreCase(nextTag.getValue()) || "gene".equalsIgnoreCase(nextTag.getValue())) {
									count++;
									subParser.setString(tagContent);
									subTagContent = subParser.getNextXMLTaggedContent(nextTag);
									while (!StringUtil.blank(subTagContent)) {
										if ("primer".equalsIgnoreCase(nextTag.getValue())) {
											primerNames[count] = StringUtil.cleanXMLEscapeCharacters(subTagContent);
										}
										else if ("forward".equalsIgnoreCase(nextTag.getValue())) {
											primerNames[count] = StringUtil.cleanXMLEscapeCharacters(subTagContent);
										}
										else if ("reverse".equalsIgnoreCase(nextTag.getValue())) {
										}
										subTagContent = subParser.getNextXMLTaggedContent(nextTag);
									}
								}
								tagContent = subParser.getNextXMLTaggedContent(nextTag);
							}

						}
						else
							return;
					}
					subTagContent = subParser.getNextXMLTaggedContent(nextTag);
				}
			}
			tagContent = parser.getNextXMLTaggedContent(nextTag);
		}



	}

	 */
	/**
	 * format:
	 * <primers>
	 * 	<primer name="SSU4F" genename="18S" forward="1"/>
	 * 	<primer name="SS1554R" genename="18S" forward="0"/>
	 *  ...
	 * </primers>
	 * @param doc
	 */
	private void parsePrimerXML(Document doc) {
		String primerTagName = "primer";
		String rootTagName = "primers";
		Element root = doc.getRootElement();
		if (!root.getName().equals(rootTagName)) {
			MesquiteMessage.warnUser("Malformed primer xml file, cannot parse.");
		}
		List primerChildren = doc.getRootElement().elements(primerTagName);
		//int numPrimers = primerChildren.size();
		initializeArrays(primerChildren.size());
		MesquiteString primerName = new MesquiteString();
		MesquiteBoolean isForward = new MesquiteBoolean(false);
		MesquiteString geneName = new MesquiteString();
		MesquiteString sequence = new MesquiteString();
		numPrimers=0;
		int i = 0;
		for (Iterator iter = primerChildren.iterator(); iter.hasNext();) {
			// init for this time around the loop
			primerName.setValue("");
			isForward.setValue(false);
			geneName.setValue("");
			Element nextPrimerElement = (Element) iter.next();
			parseSinglePrimerElement(nextPrimerElement, geneName, primerName, sequence, isForward);
			primerNames[i] = primerName.getValue();
			fragmentNames[i] = geneName.getValue();
			sequences[i] = sequence.getValue();
			forward[i++] = isForward.getValue();
			numPrimers++;
		}
	}

	private void initializeArrays(int numPrimers) {
		primerNames = new String[numPrimers];
		fragmentNames = new String[numPrimers];
		sequences = new String[numPrimers];
		forward = new boolean[numPrimers];
	}
	public void readPrimerInfoFromDatabase() {
		if (databaseURLSource==null)
			return;
		Map args = new Hashtable();
		if (databaseURLSource.needsKeyValuePairAuthorization())
			args.put(databaseURLSource.getKeyString(DNADatabaseURLSource.AUTHORIZATION_KEY), databaseURLSource.getKey());

		Document doc = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURLSource.getBaseURL(),databaseURLSource.getPage(DNADatabaseURLSource.PRIMER_SERVICE), args);
		if (doc != null) {
			// xml format so parse accordingly
			parsePrimerXML(doc);
			return;
		}
	}

	public String[][] getPrimerArray () {
		String [][] primers = new String[numPrimers][2];
		for (int i=0; i<numPrimers; i++)
			primers[i][0] = primerNames[i];
		for (int i=0; i<numPrimers; i++)
			primers[i][1] = sequences[i];
		return primers;
	}
	
	public void readTabbedPrimerFile(String primerList) {
		Document doc = MesquiteXMLUtilities.getDocumentFromString(primerList);
		if (doc != null) {
			// xml format so parse accordingly
			parsePrimerXML(doc);
			return;
		}
		String onePrimer="";
		numPrimers = 0;
		String tempList = primerList;

		while (!StringUtil.blank(tempList) && tempList.length() > 10 && tempList.indexOf(";")>=0) {
			onePrimer = tempList.substring(0,tempList.indexOf(";"));
			numPrimers ++;
			tempList = tempList.substring(tempList.indexOf(";")+1, tempList.length());
			tempList.trim();
		}
		if (numPrimers==0){
			MesquiteMessage.discreetNotifyUser("No primers found in primer file; file may be in wrong format");
			return;
		}
			
		initializeArrays(numPrimers);

		int count = -1;
		while (!StringUtil.blank(primerList) && primerList.length() > 10 && count < numPrimers) {
			if (primerList.indexOf(";")>=0)
				onePrimer = primerList.substring(0,primerList.indexOf(";"));

			Parser parser = new Parser(onePrimer);
			parser.setWhitespaceString("\t");
			parser.setPunctuationString(";");
			primerName = parser.getNextToken(); 
			if (primerName!=null)
				primerName=primerName.trim();
			fragmentName = parser.getNextToken(); 
			if (fragmentName!=null)
				fragmentName=fragmentName.trim();
			boolean isForward = true;
			token = parser.getNextToken();
			if (token !=null)
					isForward = token.equalsIgnoreCase("F") || token.equalsIgnoreCase("Forward");
			String sequence =  parser.getNextToken(); 
			count ++;
			primerNames[count] = primerName;
			fragmentNames[count] = fragmentName;
			forward[count] = isForward;
			if (StringUtil.notEmpty(sequence))
				sequences[count] = sequence;
			else
				sequences[count] = "";

			
			primerList = primerList.substring(primerList.indexOf(";")+1, primerList.length());
			primerList.trim();
			//count ++;
		}
	}

	/*.................................................................................................................*/
	public String getFragmentName(String primerName, MesquiteString stLouisString) {
		if (!StringUtil.blank(primerName)) {
			if (getUseDb()) {
				Map args = new Hashtable();
				if (databaseURLSource!=null)
					args.put(databaseURLSource.getKeyString(DNADatabaseURLSource.PRIMER_NAME), primerName);
				if (databaseURLSource!=null && databaseURLSource.needsKeyValuePairAuthorization())
					args.put(databaseURLSource.getKeyString(DNADatabaseURLSource.AUTHORIZATION_KEY), databaseURLSource.getKey());
				Document doc = null;
//				try {
				doc = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURLSource.getBaseURL(),databaseURLSource.getPage(DNADatabaseURLSource.PRIMER_SERVICE), args);
//				}
				// problems contacting the db!
				if (doc == null) {
					// TODO: There should be some kind of dialog error message here, how do we make it
					//		 popup at this point?
					MesquiteMessage.warnUser("Primer name not found in database: " + primerName+"\n");
					return "";
				} else {
					String s = doc.toString();
					Element root = doc.getRootElement();
					if (root!=null) {
						Element primer = root.element("primer");
						if (primer!=null) {
							MesquiteBoolean isForward = new MesquiteBoolean(false);
							MesquiteString geneName = new MesquiteString();
							parseSinglePrimerElement(primer, geneName, null, null,isForward);
							assignStLouisString(stLouisString, isForward.getValue());
							return geneName.getValue();
						}
					}
				}
			} else {
				for (int i=0; i<primerNames.length; i++) {
					if (primerName.trim().equalsIgnoreCase(primerNames[i])) {
						assignStLouisString(stLouisString, forward[i]);
						String returnVal = fragmentNames[i];
						return returnVal;
					}
				}
			}
		}
		return "";
	}
	/**
	 * gets the gene name and whether the primer is forward
	 * format:
	 * <primer name="SSU4F" genename="18S" forward="1"/>
	 *  
	 * @param primerElement the element to parse
	 * @param isForward whether the primer is forward
	 * @return the gene name
	 */
	public static boolean getBooleanValue(Element nodeElement, String attributeName) {
		boolean returnValue = false;
		String attrValue = nodeElement.attributeValue(attributeName);
		if (!StringUtil.blank(attrValue)) {
			if (attrValue.equalsIgnoreCase(XMLConstants.ONE) || attrValue.equalsIgnoreCase(XMLConstants.TRUE) || attrValue.equalsIgnoreCase(XMLConstants.y)) {
				returnValue = true;
			}
		}
		return returnValue;
	}    
	private void parseSinglePrimerElement(Element primerElement, MesquiteString geneName, MesquiteString primerName, MesquiteString sequence, MesquiteBoolean isForward) {
		if (primerElement == null) {
			geneName.setValue("");
		} else {
			if (isForward != null) {
				//isForward.setValue(getBooleanValue(primerElement, "forward"));

				String direction = primerElement.attributeValue(XMLConstants.direction);
				if (!StringUtil.blank(direction)) {
					if (direction.equalsIgnoreCase("F"))
						isForward.setValue(true);
					else if (direction.equalsIgnoreCase("R"))
						isForward.setValue(false);
				}
			}
			if (geneName != null) {
				geneName.setValue(primerElement.attributeValue(XMLConstants.genename));
			}
			if (primerName != null) {
				primerName.setValue(primerElement.attributeValue(XMLConstants.name));
			}
			if (sequence != null) {
				sequence.setValue(primerElement.attributeValue(XMLConstants.sequence));
			}
		}
	}
	private void assignStLouisString(MesquiteString stLouisString, boolean isForward) {
		if (isForward)
			stLouisString.setValue("b.");
		else
			stLouisString.setValue("g.");		
	}
	/*.................................................................................................................*/
	int getNumPrimers(String oneFragment) {
		int count = 0;
		Parser parser = new Parser(oneFragment);
		primerName = parser.getNextToken(); 
		String token;
		if (parser.getNextToken().equalsIgnoreCase("Forward")) {
			token = parser.getNextToken();
			while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Reverse")) {
				count ++;
				token = parser.getNextToken();
			}
			if (token.equalsIgnoreCase("Reverse")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token)) {
					count ++;
					token = parser.getNextToken();
				}
			}
		}
		else if (parser.getNextToken().equalsIgnoreCase("Reverse")) {
			token = parser.getNextToken();
			while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Forward")){
				count ++;
				token = parser.getNextToken();
			}
			if (token.equalsIgnoreCase("Forward")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token)) {
					count ++;
					token = parser.getNextToken();
				}
			}
		}
		return count;
	}
	/*.................................................................................................................*/
	public String[][] getPrimerSequences() {
		if (primerNames==null || sequences==null)
			return null;
		String[][] sequenceList = new String[numPrimers][2];
		for (int i=0; i<numPrimers&& i<primerNames.length && i<sequences.length; i++) {
			sequenceList[i][0] = primerNames[i];
			sequenceList[i][1] = sequences[i];
		}
		return sequenceList;
	}
	public boolean getUseDb() {
		return useDb;
	}
	public void setUseDb(boolean useDb) {
		this.useDb = useDb;
	}
	public String getDatabaseURL() {
		return databaseURL;
	}
	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}
}


