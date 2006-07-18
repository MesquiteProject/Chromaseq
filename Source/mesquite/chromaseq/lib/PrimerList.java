package mesquite.chromaseq.lib;

import java.util.Hashtable;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.tolweb.treegrow.main.HttpRequestMaker;
import org.tolweb.treegrow.main.RequestParameters;
import org.tolweb.treegrow.main.XMLConstants;
import org.tolweb.treegrow.main.XMLReader;

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

	public void readTabbedPrimerFile(String primerList) {
		String oneFragment;
		int numPrimers = 0;
		String tempList = primerList;

		while (!StringUtil.blank(tempList) && tempList.length() > 10 && tempList.indexOf(";")>=0) {
			oneFragment = tempList.substring(0,tempList.indexOf(";"));
			numPrimers += getNumPrimers(oneFragment);
			tempList = tempList.substring(tempList.indexOf(";")+1, tempList.length());
			tempList.trim();
		}
		primerNames = new String[numPrimers];
		fragmentNames = new String[numPrimers];
		forward = new boolean[numPrimers];
		
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
				Document doc = HttpRequestMaker.getTap4ExternalUrlDocument(urlPrefix, "btolxml/PrimerService", args);
				// problems contacting the db!
				if (doc == null || doc.getRootElement() == null || doc.getRootElement().getName().equals(XMLConstants.ERROR)) {
					// TODO: There should be some kind of dialog error message here, how do we make it
					//		 popup at this point?
					return "";
				} else {
					Element root = doc.getRootElement();
					String geneName = root.getAttributeValue(XMLConstants.genename);
					boolean isForward = XMLReader.getBooleanValue(root, XMLConstants.forward);
					assignStLouisString(stLouisString, isForward);
					return geneName;
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


