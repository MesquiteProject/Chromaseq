package mesquite.chromaseq.lib;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.tolweb.base.http.BaseHttpRequestMaker;
import org.tolweb.base.xml.BaseXMLReader;
import org.tolweb.treegrow.main.RequestParameters;
import org.tolweb.treegrow.main.XMLConstants;

import mesquite.Mesquite;
import mesquite.lib.*;

/* ======================================================================== */
public class PrimerList { 
	String [] primerNames;
	String token;
	String [] fragmentNames;
	boolean [] forward;
	String fragName;
	int numPrimers;
	private String databaseURL;
	
	public PrimerList(String primerList) {
		this(primerList, false);
	}
	
	public PrimerList(String primerListPathOrDbUrl, boolean useDb) {
		if (useDb) {
			setDatabaseURL(primerListPathOrDbUrl);
		} else {
			readTabbedPrimerFile(primerListPathOrDbUrl);
		}
	}
	
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
		List primerChildren = doc.getRootElement().getChildren(primerTagName);
		int numPrimers = primerChildren.size();
		initializeArrays(numPrimers);
		MesquiteString primerName = new MesquiteString();
		MesquiteBoolean isForward = new MesquiteBoolean(false);
		MesquiteString geneName = new MesquiteString();
		int i = 0;
		for (Iterator iter = primerChildren.iterator(); iter.hasNext();) {
			// init for this time around the loop
			primerName.setValue("");
			isForward.setValue(false);
			geneName.setValue("");
			Element nextPrimerElement = (Element) iter.next();
			parseSinglePrimerElement(nextPrimerElement, geneName, primerName, isForward);
			primerNames[i] = primerName.getValue();
			fragmentNames[i] = geneName.getValue();
			forward[i++] = isForward.getValue();
		}
	}

	private void initializeArrays(int numPrimers) {
		primerNames = new String[numPrimers];
		fragmentNames = new String[numPrimers];
		forward = new boolean[numPrimers];
	}
	public void readTabbedPrimerFile(String primerList) {
		Document doc = XMLUtilities.getDocumentFromString(primerList);
		if (doc != null) {
			// xml format so parse accordingly
			parsePrimerXML(doc);
			return;
		}
		String oneFragment;
		int numPrimers = 0;
		String tempList = primerList;

		while (!StringUtil.blank(tempList) && tempList.length() > 10 && tempList.indexOf(";")>=0) {
			oneFragment = tempList.substring(0,tempList.indexOf(";"));
			numPrimers += getNumPrimers(oneFragment);
			tempList = tempList.substring(tempList.indexOf(";")+1, tempList.length());
			tempList.trim();
		}
		initializeArrays(numPrimers);
		
		int count = -1;
		while (!StringUtil.blank(primerList) && primerList.length() > 10 && count < numPrimers) {
			oneFragment = primerList.substring(0,primerList.indexOf(";"));
			
			Parser parser = new Parser(oneFragment);
			fragName = parser.getNextToken(); 
			if (parser.getNextToken().equalsIgnoreCase("Forward")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Reverse")) {
					count ++;
					primerNames[count] = token.trim();
					fragmentNames[count] = fragName;
					forward[count] = true;
					token = parser.getNextToken();
				}
				if (token.equalsIgnoreCase("Reverse")) {
					token = parser.getNextToken();
					while (!StringUtil.blank(token)) {
						count ++;
						primerNames[count] = token.trim();
						fragmentNames[count] = fragName;
						forward[count] = false;
						token = parser.getNextToken();
					}
				}
			} else if (parser.getNextToken().equalsIgnoreCase("Reverse")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Forward")) {
					count ++;
					primerNames[count] = token.trim();
					fragmentNames[count] = fragName;
					forward[count] = false;
					token = parser.getNextToken();
				}
				if (token.equalsIgnoreCase("Forward")) {
					//count --;
					token = parser.getNextToken();
					while (!StringUtil.blank(token)) {
						count ++;
						primerNames[count] = token.trim();
						fragmentNames[count] = fragName;
						forward[count] = true;
						token = parser.getNextToken();
					}
				}
			}
			primerList = primerList.substring(primerList.indexOf(";")+1, primerList.length());
			primerList.trim();
			//count ++;
		}
	}

	/*.................................................................................................................*/
	public String getFragmentName(String primerName, MesquiteString stLouisString) {
		if (!StringUtil.blank(primerName)) {
			if (getUseDatabaseForPrimers()) {
				String urlPrefix = getDatabaseURL();
				Map args = new Hashtable();
				args.put(RequestParameters.PRIMER_NAME, primerName);
				Document doc = null;
//				try {
				doc = XMLUtilities.getDocumentFromTapestryPageName("btolxml/PrimerService", args);
//				}
/*
 * 				catch (ExceptionInInitializerError e) {
		Debugg.println("\n********************\nExceptionInInitializerError in HttpRequestMaker.getTap4ExternalUrlDocument\n********************\n");
				}
				catch (NoClassDefFoundError e) {
		Debugg.println("\n********************\nNoClassDefFoundError in HttpRequestMaker.getTap4ExternalUrlDocument\n********************\n");
				}
*/
				// problems contacting the db!
				if (doc == null) {
					// TODO: There should be some kind of dialog error message here, how do we make it
					//		 popup at this point?
					MesquiteMessage.warnUser("Primer name not found in database: " + primerName+"\n" + doc);
//					Debugg.println("Primer name not found in database: " + primerName);
					return "";
				} else {
					Element root = doc.getRootElement();
					MesquiteBoolean isForward = new MesquiteBoolean(false);
					MesquiteString geneName = new MesquiteString();
					parseSinglePrimerElement(root, geneName, null, isForward);
					assignStLouisString(stLouisString, isForward.getValue());
					return geneName.getValue();
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
	private void parseSinglePrimerElement(Element primerElement, MesquiteString geneName, MesquiteString primerName, MesquiteBoolean isForward) {
		if (primerElement == null) {
			geneName.setValue("");
		} else {
			if (isForward != null) {
				isForward.setValue(BaseXMLReader.getBooleanValue(primerElement, XMLConstants.forward));
			}
			if (geneName != null) {
				geneName.setValue(primerElement.getAttributeValue(XMLConstants.genename));
			}
			if (primerName != null) {
				primerName.setValue(primerElement.getAttributeValue(XMLConstants.name));
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
		fragName = parser.getNextToken(); 
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
	public boolean getUseDatabaseForPrimers() {
		return !StringUtil.blank(getDatabaseURL());
	}
	public String getDatabaseURL() {
		return databaseURL;
	}
	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}
}


