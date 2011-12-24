/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
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
public class PrimerInformationFile { 
	String [] primerNames;
	String token;
	String [] fragmentNames;
	String [] sequences;
	boolean [] forward;
	String primerName;
	String fragmentName;
	int numPrimers;

	public PrimerInformationFile(String primerList) {
		readTabbedPrimerFile(primerList);
	}

	
	/*
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
		numPrimers = 1;
		String tempList = primerList;

		while (!StringUtil.blank(tempList) && tempList.length() > 10  && tempList.indexOf(";")>=0) {
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
		while (!StringUtil.blank(primerList) && primerList.length() > 10 && count < numPrimers && primerList.indexOf(";")>=0) {
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
			
			if (count<primerNames.length) {
				primerNames[count] = primerName;
				fragmentNames[count] = fragmentName;
				forward[count] = isForward;
				if (StringUtil.notEmpty(sequence))
					sequences[count] = sequence;
				else
					sequences[count] = "";
			}
			
			primerList = primerList.substring(primerList.indexOf(";")+1, primerList.length());
			primerList.trim();
			//count ++;
		}
	}

	/*.................................................................................................................*/
	public String getFragmentName(String primerName) {
		if (!StringUtil.blank(primerName)) {
			for (int i=0; i<primerNames.length; i++) {
				if (primerName.trim().equalsIgnoreCase(primerNames[i])) {
					return fragmentNames[i];
				}
			}
		}
		return "";
	}
	/*.................................................................................................................*/
	public String getSequence(String primerName) {
		if (!StringUtil.blank(primerName)) {
			for (int i=0; i<primerNames.length; i++) {
				if (primerName.trim().equalsIgnoreCase(primerNames[i])) {
					return sequences[i];
				}
			}
		}
		return "";
	}
	/** returns all primer sequences that correspond to the given gene fragment name, 
	[numPrimers][2], with [i][0] containing the primer name of the i'th primer, and [i][1] containing the primer sequence */
	/*.................................................................................................................*/
	public String[][] getAllSequences(String geneFragmentName) {
		if (sequences==null || sequences.length==0)
			return null;

		int count = 0;
		for (int i=0; i<sequences.length; i++) {
			if (StringUtil.notEmpty(sequences[i]) && (StringUtil.blank(geneFragmentName)|| geneFragmentName.equalsIgnoreCase(fragmentNames[i])))
				count++;
		}
		String [][] seq = new String[count][2];
		count=0;
		for (int i=0; i<sequences.length; i++) {
			if (StringUtil.notEmpty(sequences[i]) && (StringUtil.blank(geneFragmentName)|| geneFragmentName.equalsIgnoreCase(fragmentNames[i]))) {
				seq[count][0] = primerNames[i];
				seq[count][1] = sequences[i];
				count++;
			}
		}
		return seq;
	}
	/*.................................................................................................................*/
	public String[][] getAllSequences() {
		return getAllSequences(null);
	}
	/*.................................................................................................................*/
	public boolean isForward(String primerName) {
		if (!StringUtil.blank(primerName)) {
			for (int i=0; i<primerNames.length; i++) {
				if (primerName.trim().equalsIgnoreCase(primerNames[i])) {
					return forward[i];
				}
			}
		}
		return false;
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
}


