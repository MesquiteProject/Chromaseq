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

package mesquite.chromaseq.InterpretASNGenBank;


import java.util.*;
import java.awt.*;

import org.apache.commons.lang.WordUtils;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.chromaseq.lib.*;

//IUPACna " "

public class InterpretASNGenBank extends FileInterpreterI {
	int releaseYear=0;
	int releaseMonth=0;
	int releaseDay=0;
	
	boolean releaseImmediately = false;
	protected boolean includeGaps = false;
	protected boolean simplifyTaxonName = false;
	protected String uniqueSuffix = "";

	GenBankContact contact;
	GenBankAuthors authors;
	GenBankAffiliation affiliation;
	MesquiteString xmlPrefs= new MesquiteString();
	String xmlPrefsString = null;
	



	Class[] acceptedClasses;
	VoucherInfoCoord voucherInfoTask;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(VoucherInfoCoord.class, "Voucher information is needed for ASN export for GenBank submissions.",
		"This is activated automatically when you choose this exporter.");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences(xmlPrefs);
		xmlPrefsString = xmlPrefs.getValue();
		Calendar rightNow = Calendar.getInstance();
		releaseYear = rightNow.get(Calendar.YEAR)+1;
		releaseMonth = rightNow.get(Calendar.MONTH);
		releaseDay = rightNow.get(Calendar.DAY_OF_MONTH);
		contact = new GenBankContact(this, xmlPrefsString);
		authors = new GenBankAuthors(this, xmlPrefsString);
		affiliation = new GenBankAffiliation(this, xmlPrefsString);


		voucherInfoTask = (VoucherInfoCoord)hireEmployee(VoucherInfoCoord.class, null);
		if (voucherInfoTask == null)
			return false;
		acceptedClasses = new Class[] {ProteinState.class, DNAState.class};
		storePreferences();
		return  true;
	}

	protected String addendum = "";
	protected String codeLabel = "";

	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "releaseImmediately", releaseImmediately);   
		//StringUtil.appendXMLTag(buffer, 2, "showPrimers", showPrimers);   
		if (contact!=null){
			String s = contact.preparePreferencesForXML();
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		if (authors!=null){
			String s = authors.preparePreferencesForXML();
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		if (affiliation!=null){
			String s = affiliation.preparePreferencesForXML();
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("releaseImmediately".equalsIgnoreCase(tag))
			releaseImmediately = MesquiteBoolean.fromTrueFalseString(content);
	}

	/*.................................................................................................................*/

	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export ASN for GenBank Options", buttonPressed);
		exportDialog.appendToHelpString("Choose the options for exporting the matrix as a ASN file prepared for submission NCBI's Sequin.");
		exportDialog.appendToHelpString("<br><br><b>SeqID Suffix</b>: this will be added to each taxon name to form the unique SeqID.");
		exportDialog.appendToHelpString("<br><b>Description of gene fragment</b>: this will be added to each sequence's DEFINITION.");
		exportDialog.appendToHelpString("<br><b>Text before VoucherCode in DEFINITION</b>: this will inserted between the organism name and the VoucherCode in the DEFINITION.");



		SingleLineTextField uniqueSuffixField = exportDialog.addTextField("SeqID Suffix", "", 20);
		TextArea fsText =null;
		exportDialog.addLabel("Description of gene fragment:",Label.LEFT);
		fsText =exportDialog.addTextAreaSmallFont(addendum,4);
		SingleLineTextField codeLabelField = exportDialog.addTextField("Text before VoucherCode in DEFINITION", "DNAVoucher", 20);

		Checkbox includeGapsCheckBox = exportDialog.addCheckBox("include gaps", includeGaps);
		exportDialog.addHorizontalLine(1);
		int releaseB = 1;
		if (releaseImmediately)
			releaseB=0;
		RadioButtons releaseButtons = exportDialog.addRadioButtons (new String[] {"release immediately", "release date:"}, releaseB);
		IntegerField releaseYearField = exportDialog.addIntegerField("Release Year", releaseYear, 6);
		IntegerField releaseMonthField = exportDialog.addIntegerField("Release Month", releaseMonth, 4);
		IntegerField releaseDayField = exportDialog.addIntegerField("Release Day", releaseDay, 4);


		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);

		addendum = fsText.getText();
		codeLabel = codeLabelField.getText();
		uniqueSuffix = uniqueSuffixField.getText();

		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);

		if (ok) {
			includeGaps = includeGapsCheckBox.getState();
			if (releaseButtons.getValue()==0){
				releaseImmediately = true;
			} else {
				releaseYear = releaseYearField.getValue();
				releaseMonth = releaseMonthField.getValue();
				releaseDay = releaseDayField.getValue();
				releaseImmediately = false;
			}
		}

		exportDialog.dispose();
		if (ok)
			ok = contact.queryValues();
		if (ok)
			ok = authors.queryValues();
		if (ok)
			ok = affiliation.queryValues();
		if (ok)
			storePreferences();
		return ok;

	}	

	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;  //
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return project.getNumberCharMatrices(acceptedClasses) > 0;  //
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		for (int i = 0; i<acceptedClasses.length; i++)
			if (dataClass==acceptedClasses[i])
				return true;
		return false; 
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return false;
	}
	/** Returns whether the module can read (import) files considering the passed argument string (e.g., fuse) */
	public boolean canImport(String arguments){
		return false;
	}



	/* ============================  exporting ============================*/

	/*.................................................................................................................*/

	public boolean getExportOptionsSimple(boolean dataSelected, boolean taxaSelected){   // an example of a simple query, that only proved line delimiter choice; not used here
		return (ExporterDialog.query(this,containerOfModule(), "Export FASTA Options")==0);
	}	

	/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return getProject().chooseData(containerOfModule(), file, null, DNAState.class, "Select data to export");
	}

	protected String getGenBankFieldOfTaxon(Taxa taxa, int it, String fieldName){
		if (taxa!=null && voucherInfoTask != null) {
			String s = " ";
			String voucherID = ChromaseqUtil.getStringAssociated(taxa, ChromaseqUtil.voucherCodeRef, it);
			VoucherInfo vi= voucherInfoTask.getVoucherInfo(ChromaseqUtil.getStringAssociated(taxa, ChromaseqUtil.voucherDBRef, it), voucherID);
			if (vi != null) {
				return vi.getGenBankFieldValue(fieldName);
			}
		}
		return null; 
	}
	protected String getVoucherID(Taxa taxa, int it){
		if (taxa!=null && voucherInfoTask != null) {
			String voucherID = ChromaseqUtil.getStringAssociated(taxa, ChromaseqUtil.voucherCodeRef, it);
			return voucherID;
		}
		return null; 
	}
	
	
	public  void addGenBankTaxonField(ASNNode baseNode, Taxa taxa, int it, String fieldName) {
		String s = getGenBankFieldOfTaxon(taxa,it,fieldName);
		if (StringUtil.notEmpty(s)) {
			baseNode.addChild(fieldName,s, true);
		}
	}

	protected String getTaxonName(Taxa taxa, int it){
		if (simplifyTaxonName)
			return StringUtil.cleanseStringOfFancyChars(taxa.getTaxonName(it)+uniqueSuffix,false,true);
		else 
			return ParseUtil.tokenize(taxa.getTaxonName(it));
	}
	protected void saveExtraFiles(CharacterData data, String filePath){
	}

	public void appendTabbedLine(StringBuffer sb, int numTabs, String s){
		for (int i=0;i<numTabs; i++)
			sb.append("  ");
		sb.append(s);
		sb.append(getLineEnding());
	}

	public String localIDString(Taxa taxa, int it) {
		return taxa.getTaxonName(it);  //TODO:
	}
	public String getFullDescription(Taxa taxa, int it) {
		return taxa.getTaxonName(it);  //TODO:
	}
	/*.................................................................................................................*/

	public  void addDate(ASNNode baseNode, int year, int month, int day) {
		if (year>0 && month>0 && day>0) {
			ASNNode stdNode = baseNode.addChild("std");
			stdNode.addChild("year",year);
			stdNode.addChild("month",month);
			stdNode.addChild("day",day);
		}
	}
	/*.................................................................................................................*/

	public  void addCurrentDate(ASNNode baseNode) {
		ASNNode stdNode = baseNode.addChild("std");
		Calendar rightNow = Calendar.getInstance();
		stdNode.addChild("year",rightNow.get(Calendar.YEAR));
		stdNode.addChild("month",rightNow.get(Calendar.MONTH));
		stdNode.addChild("day",rightNow.get(Calendar.DAY_OF_MONTH));
	}
	/*.................................................................................................................*/

	public  void addName(ASNNode baseNode,GenBankAuthors authors, int i) {
		ASNNode nameNode = baseNode.addChild("name");
		nameNode.addChild("last", authors.getLastName(i), true);
		nameNode.addChild("first", authors.getFirstName(i), true);
		nameNode.addChild("initials", authors.getInitials(i), true);
		nameNode.addChild("suffix", authors.getSuffix(i), true);
	}

	/*.................................................................................................................*/

	public  void addName(ASNNode baseNode,GenBankContact contact) {
		ASNNode nameNode = baseNode.addChild("name");
		nameNode.addChild("last", contact.getLastName(), true);
		nameNode.addChild("first", contact.getFirstName(), true);
		nameNode.addChild("initials", contact.getInitials(), true);
		nameNode.addChild("suffix", contact.getSuffix(), true);
	}
	/*.................................................................................................................*/

	public  void addAffiliation(ASNNode baseNode,GenBankAffiliation affiliation, GenBankContact contact) {
		ASNNode stdNode = baseNode.addChild("std");
		stdNode.addChild("affil", affiliation.getAffil(), true);
		stdNode.addChild("div", affiliation.getDiv(), true);
		stdNode.addChild("city",affiliation.getCity(), true);
		stdNode.addChild("sub",affiliation.getSub(), true);
		stdNode.addChild("country",affiliation.getCountry(), true);
		if (contact!=null){
			stdNode.addChild("email",contact.getEmail(), true);
			stdNode.addChild("fax",contact.getFax(), true);
			stdNode.addChild("phone",contact.getPhone(), true);
		}
		stdNode.addChild("postal-code",affiliation.getPostalCode(), true);
	}
	/*.................................................................................................................*/

	public String getLocalSequenceID(Taxa taxa, int it) {
		String s = taxa.getTaxonName(it);
		s=StringUtil.blanksToUnderline(StringUtil.cleanseStringOfFancyChars(s)+uniqueSuffix);
		return s;
	}
	/*.................................................................................................................*/

	public  void addContact(ASNNode baseNode, GenBankContact contact) {
		ASNNode contactNode = baseNode.addChild("contact");
		ASNNode nameNode = contactNode.addChild("name");
		addName(nameNode,contact);
		ASNNode affilNode = contactNode.addChild("affil");
		addAffiliation(affilNode,affiliation, contact);
	}
	/*.................................................................................................................*/

	public  void addAuthors(ASNNode baseNode, GenBankAuthors authors, GenBankAffiliation affiliation) {
		ASNNode authorsNode = baseNode.addChild("authors");
		ASNNode namesNode = authorsNode.addChild("names");
		ASNNode stdNode = namesNode.addChild("std");
		for (int i = 0; i<GenBankAuthors.NUMAUTHORS; i++) {
			ASNNode emptyNode = stdNode.addChild("");
			ASNNode nameNode = emptyNode.addChild("name");
			addName(nameNode,authors, i);
		}
		ASNNode affilNode = authorsNode.addChild("affil");
		affilNode.setHasBraces(false);

		addAffiliation(affilNode,affiliation, null);
	}
	/*.................................................................................................................*/

	public String getFullSequenceTitle(Taxa taxa, int it) {
		String s = getGenBankFieldOfTaxon(taxa,  it,  "organism");
		if (StringUtil.notEmpty(codeLabel))
			s+= " " + codeLabel;
		s+= " " + getVoucherID(taxa,  it);
		if (StringUtil.notEmpty(addendum))
			s+= " " + addendum;
		return s;
	}
	/*.................................................................................................................*/

	public void addSubType(ASNNode baseNode, String subTypeName, String nameName, String nameText, boolean quoted) {
		ASNNode subTypeNode = baseNode.addChild("");
		ASNNode unnamedNode = subTypeNode.addChild("");
		unnamedNode.addChild("subtype", subTypeName, quoted);
		unnamedNode.addChild(nameName, nameText, true);
	}
	/*.................................................................................................................*/

	public  void addSubmissionInfo(ASNNode baseNode) {
		ASNNode contactNode = baseNode.addChild("contact");
		addContact(contactNode, contact);
		ASNNode citationNode = baseNode.addChild("cit");
		addAuthors(citationNode, authors, affiliation);
		addCurrentDate(citationNode);
		ASNNode hupNode = baseNode.addChild("hup",true);

		if (releaseYear>0 && releaseMonth>0 && releaseDay>0 && !releaseImmediately) {
			ASNNode reldateNode = baseNode.addChild("reldate");
			addDate(reldateNode, releaseYear, releaseMonth, releaseDay);
		}

		ASNNode subTypeNode = baseNode.addChild("subtype", "new", false);
		ASNNode toolNode = baseNode.addChild("tool");
		toolNode.setStringContent("Mesquite " + getMesquiteVersion() + getBuildVersion(), true);
	}
	
	/*.................................................................................................................*/
	String sequenceClass = "nuc-prot";
	String genome = "genomic";
	String  biomol = "genomic";
	String mol = "dna";
	String repr = "raw";
	String seqDataFormat = "iupacna";
	int geneticCode = 1;
	/*.................................................................................................................*/

	public  boolean addSequence(ASNNode baseNode,CharacterData data, int it) {
		ASNNode setNode = baseNode.addChild("set");
		ASNNode classNode = setNode.addChild("class", sequenceClass, false);

		ASNNode descrNode = setNode.addChild("descr");
		ASNNode sourceNode = descrNode.addChild("source");
		sourceNode.addChild("genome", genome, false);
		ASNNode orgNode = sourceNode.addChild("org");
		ASNNode taxNameNode = orgNode.addChild("taxname", getGenBankFieldOfTaxon(data.getTaxa(),  it,  "organism"), true);
		ASNNode orgNameNode = orgNode.addChild("orgname");
		ASNNode modNode = orgNameNode.addChild("mod");
		addSubType(modNode, "specimen-voucher", "subname", getGenBankFieldOfTaxon(data.getTaxa(),  it,  "specimen-voucher"), false);		
		addSubType(modNode, "authority", "subname", getGenBankFieldOfTaxon(data.getTaxa(),  it,  "authority"), false);
		ASNNode gCodeNode = orgNameNode.addChild("gcode", geneticCode);  // genetic code
		ASNNode subTypeNode = sourceNode.addChild("subtype");
		addSubType(subTypeNode, "country", "name",  getGenBankFieldOfTaxon(data.getTaxa(),  it,  "country"), false);
		addSubType(subTypeNode, "lat-lon", "name", getGenBankFieldOfTaxon(data.getTaxa(),  it,  "lat-lon"), false);
		addSubType(subTypeNode, "identified-by", "name",getGenBankFieldOfTaxon(data.getTaxa(),  it,  "identified-by"), false);

		ASNNode seqSetNode = setNode.addChild("seq-set");
		ASNNode seqNode = seqSetNode.addChild("seq");
		ASNNode idNode = seqNode.addChild("id");
		ASNNode localNode = idNode.addChild("local");
		String localSequenceID=getLocalSequenceID(data.getTaxa(), it);
		localNode.addChild("str", localSequenceID, true);

		ASNNode descNode = seqNode.addChild("descr");
		descNode.addChild("title", getFullSequenceTitle(data.getTaxa(),it), true);
		ASNNode molinfoNode = descNode.addChild("molinfo");
		molinfoNode.addChild("biomol", biomol, false);  //

		ASNNode instNode = seqNode.addChild("inst");
		instNode.addChild("repr", repr, false); //
		instNode.addChild("mol", mol, false); //
		instNode.addChild("length", data.getNumberApplicableInTaxon(it, false));  // check
		ASNNode seqDataNode = instNode.addChild("seq-data");
		ASNNode iupacNode = seqDataNode.addChild(seqDataFormat); //

		StringBuffer sb = new StringBuffer();
		int counter = 1;
		for (int ic = 0; ic<data.getNumChars(); ic++) {
			if (!writeOnlySelectedData || (data.getSelected(ic))){
				int currentSize = sb.length();
				boolean wroteMoreThanOneSymbol = false;
				if (includeGaps || (!data.isInapplicable(ic,it))) {
					data.statesIntoStringBuffer(ic, it, sb, false);
					wroteMoreThanOneSymbol = sb.length()-currentSize>1;
					counter ++;
					//if ((counter % 80 == 1) && (counter > 1)) {    // modulo
					//	sb.append(getLineEnding());
					//}
				}
				if (wroteMoreThanOneSymbol) {
					alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");
					return false;
				}
			}
		}
		ASNNode annotNode = seqNode.addChild("annot");
		
		if (data instanceof DNAData && ((DNAData)data).someCoding()) {  // show translation
		}

		iupacNode.setStringContent(sb.toString(), true);
		return true;

	}
	/*.................................................................................................................*/

	public  MesquiteStringBuffer getDataAsFileText(CharacterData data) {
		Taxa taxa = data.getTaxa();

		int numTaxa = taxa.getNumTaxa();
		int numChars = data.getNumChars();
		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numTaxa*(20L + numChars));
		outputBuffer.append("Seq-submit ::= {" + getLineEnding());

		ASNNode rootNode = new ASNNode("root");
		rootNode.setLineEnding(getLineEnding());
		ASNNode subNode = rootNode.addChild("sub");
		addSubmissionInfo(subNode);

		ASNNode dataNode = rootNode.addChild("data");
		ASNNode entrysNode = dataNode.addChild("entrys");
		ASNNode setNode = entrysNode.addChild("set");
		ASNNode classNode = setNode.addChild("class", "phy-set", false);
		ASNNode descrNode = setNode.addChild("descr");
		ASNNode seqSetNode = entrysNode.addChild("seq-set");
		for (int it = 0; it<numTaxa; it++)
			if (!addSequence(seqSetNode, data, it))
				return null;

		outputBuffer.append(rootNode.toString());
		outputBuffer.append(" }" + getLineEnding());
		
		String wrapped = WordUtils.wrap(outputBuffer.toString(), 100, null, true);
		outputBuffer.replace(0,outputBuffer.length(),wrapped);

		return outputBuffer;
	}

	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export
		Arguments args = new Arguments(new Parser(arguments), true);
		boolean usePrevious = args.parameterExists("usePrevious");

		CharacterData data = findDataToExport(file, arguments);
		if (data ==null) {
			showLogWindow(true);
			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");
			return false;
		}
		Taxa taxa = data.getTaxa();
		if (!MesquiteThread.isScripting() && !usePrevious)
			if (!getExportOptions(data.anySelected(), taxa.anySelected()))
				return false;

		MesquiteStringBuffer outputBuffer = getDataAsFileText(data);

		if (outputBuffer!=null) {
			saveExportedFileWithExtension(outputBuffer, arguments, "sqn");
			return true;
		}
		saveExtraFiles(data, filePath);
		return false;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "ASN File for GenBank submission";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Exports ASN files that consist of molecular sequence data for GenBank submission." ;
	}
	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile mnf, String arguments) {
	}




}

class ASNNode {
	Vector nodes;
	int depth = -1;
	boolean hasBraces = true;
	String name;
	String stringContent;
	boolean booleanContent=false;
	boolean isBoolean = false;
	String lineEnding = "";
	boolean quoted = false;
	int intContent = MesquiteInteger.unassigned;
	ASNNode(String name) {
		this.name = name;
	}
	public ASNNode addChild(String name) {
		ASNNode child = new ASNNode(name);
		if (nodes==null)
			nodes = new Vector();
		nodes.addElement(child);
		child.setDepth(depth+1);
		child.setLineEnding(lineEnding);
		return child;
	}
	public ASNNode addChild(String name, String stringContent, boolean quoted) {
		if (StringUtil.blank(stringContent))
			return null;
		ASNNode child = addChild(name);
		child.setStringContent(stringContent, quoted);
		return child;
	}
	public ASNNode addChild(String name, boolean booleanContent) {
		ASNNode child = addChild(name);
		child.setBooleanContent(booleanContent);
		child.isBoolean = true;
		return child;
	}
	public ASNNode addChild(String name, int intContent) {
		ASNNode child = addChild(name);
		child.setIntContent(intContent);
		return child;
	}

	public int getNumChildren() {
		if (nodes==null) return 0;
		return nodes.size();
	}
	public String getTabs(){
		String s = getLineEnding();
		for (int i = 0; i<=depth; i++)
			s+="\t";
		return s;
	}
	public String toString() {
		String s = "";
		if(depth>=0) {

			s+=getTabs();
			if (StringUtil.notEmpty(name))
				s+=name + " ";

			if (StringUtil.notEmpty(stringContent)){
				if (quoted)
					s+= "\"";
				s+=stringContent;
				if (quoted)
					s+= "\"";
				s+=" ";
			}
			if (MesquiteInteger.isCombinable(intContent))
				s+=intContent + " ";
			if (isBoolean) {
				if (booleanContent)
					s+= "TRUE ";
				else
					s+="FALSE ";
			}
			if (nodes!=null&& nodes.size()>1) 
				s+= "{ ";
		}
		for (int i = 0; i<getNumChildren(); i++){
			s+=nodes.get(i).toString();
			if (i<getNumChildren()-1)
				s+= " ,";
		}
		if (depth>=0&&nodes!=null && nodes.size()>1) 
			s+= "} ";
		return s;
	}
	public String getStringContent() {
		return stringContent;
	}
	public void setStringContent(String stringContent, boolean quoted) {
		this.stringContent = stringContent;
		this.quoted = quoted;
	}
	public int getIntContent() {
		return intContent;
	}
	public void setIntContent(int intContent) {
		this.intContent = intContent;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getLineEnding() {
		return lineEnding;
	}
	public void setLineEnding(String lineEnding) {
		this.lineEnding = lineEnding;
	}
	public boolean isBooleanContent() {
		return booleanContent;
	}
	public void setBooleanContent(boolean booleanContent) {
		this.booleanContent = booleanContent;
	}
	public boolean hasBraces() {
		return hasBraces;
	}
	public void setHasBraces(boolean hasBraces) {
		this.hasBraces = hasBraces;
	}
}

//====================================================================

class GenBankContact implements XMLPreferencesProcessor{
	String lastName;
	String firstName;
	String initials;
	String suffix;
	String email;
	String fax;
	String phone;

	MesquiteModule ownerModule;
	GenBankContact(MesquiteModule ownerModule, String xmlPrefsString){
		this.ownerModule = ownerModule;
		XMLUtil.readXMLPreferences(ownerModule,this, xmlPrefsString);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "lastName", lastName);  
		StringUtil.appendXMLTag(buffer, 2, "firstName", firstName);  
		StringUtil.appendXMLTag(buffer, 2, "initials", initials);  
		StringUtil.appendXMLTag(buffer, 2, "suffix", suffix);  
		StringUtil.appendXMLTag(buffer, 2, "email", email);  
		StringUtil.appendXMLTag(buffer, 2, "fax", fax);  
		StringUtil.appendXMLTag(buffer, 2, "phone", phone);  

		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("lastName".equalsIgnoreCase(tag))
			lastName = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("firstName".equalsIgnoreCase(tag))
			firstName = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("initials".equalsIgnoreCase(tag))
			initials = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("suffix".equalsIgnoreCase(tag))
			suffix = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("email".equalsIgnoreCase(tag))
			email = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("fax".equalsIgnoreCase(tag))
			fax = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("phone".equalsIgnoreCase(tag))
			phone = StringUtil.cleanXMLEscapeCharacters(content);
	}

	/*.................................................................................................................*/

	public boolean queryValues(){
		if (MesquiteThread.isScripting())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(), "Contact Information",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Contact Information");

		SingleLineTextField[] contactNameFields = dialog.addNameFieldsRow(4, new String[] {"First Name", "Initials", "Last Name", "Suffix"}, new int[] {20, 5, 20, 5});
		contactNameFields[0].setText(firstName);
		contactNameFields[1].setText(initials);
		contactNameFields[2].setText(lastName);
		contactNameFields[3].setText(suffix);
		SingleLineTextField emailField = dialog.addTextField("e-mail:", email, 40);
		SingleLineTextField faxField = dialog.addTextField("Fax:", fax, 30);
		SingleLineTextField phoneField = dialog.addTextField("Phone:", phone, 30);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			firstName = contactNameFields[0].getText();
			initials = contactNameFields[1].getText();
			lastName = contactNameFields[2].getText();
			suffix = contactNameFields[3].getText();
			email = emailField.getText();
			fax = faxField.getText();
			phone = phoneField.getText();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) ;
	}

	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getInitials() {  // for some strange reason they automatically append the initial of the first name into the initials field.
		String s = "";
		if (firstName!=null)
			firstName = firstName.trim();
		if (StringUtil.notEmpty(firstName)){
			s+= firstName.charAt(0) + ".";
		}
		s+= initials;
		return s;
	}
	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

}

//====================================================================

class GenBankAffiliation implements XMLPreferencesProcessor{
	String lastName;
	String firstName;
	String initials;
	String suffix;
	String affil;
	String div;
	String city;
	String sub;
	String country;
	String postalCode;
	MesquiteModule ownerModule;
	GenBankAffiliation(MesquiteModule ownerModule, String xmlPrefsString){
		this.ownerModule = ownerModule;
		XMLUtil.readXMLPreferences(ownerModule,this, xmlPrefsString);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "affil", affil);  
		StringUtil.appendXMLTag(buffer, 2, "div", div);  
		StringUtil.appendXMLTag(buffer, 2, "city", city);  
		StringUtil.appendXMLTag(buffer, 2, "sub", sub);  
		StringUtil.appendXMLTag(buffer, 2, "country", country);  
		StringUtil.appendXMLTag(buffer, 2, "postalCode", postalCode);  

		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("affil".equalsIgnoreCase(tag))
			affil = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("div".equalsIgnoreCase(tag))
			div = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("city".equalsIgnoreCase(tag))
			city = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("sub".equalsIgnoreCase(tag))
			sub = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("country".equalsIgnoreCase(tag))
			country = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("postalCode".equalsIgnoreCase(tag))
			postalCode = StringUtil.cleanXMLEscapeCharacters(content);
	}

	/*.................................................................................................................*/

	public boolean queryValues(){
		if (MesquiteThread.isScripting())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(), "Affiliation",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Affiliation");

		SingleLineTextField affilField = dialog.addTextField("Institution:", affil, 40);
		SingleLineTextField divField = dialog.addTextField("Department:", div, 30);
		SingleLineTextField cityField = dialog.addTextField("City:", city, 30);
		SingleLineTextField subField = dialog.addTextField("State/Province:", sub, 30);
		SingleLineTextField countryField = dialog.addTextField("Country:", country, 30);
		SingleLineTextField postalCodeField = dialog.addTextField("Zip/Postal Code:", postalCode, 20);


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			affil = affilField.getText();
			div = divField.getText();
			city = cityField.getText();
			sub = subField.getText();
			country = countryField.getText();
			postalCode = postalCodeField.getText();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) ;
	}

	public String getAffil() {
		return affil;
	}
	public void setAffil(String affil) {
		this.affil = affil;
	}
	public String getDiv() {
		return div;
	}
	public void setDiv(String div) {
		this.div = div;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getSub() {
		return sub;
	}
	public void setSub(String sub) {
		this.sub = sub;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

}


//====================================================================

class GenBankAuthors implements XMLPreferencesProcessor{
	static final int NUMFIELDS = 4;
	public static final int NUMAUTHORS =3;

	String[] lastName;
	String[] firstName;
	String[] initials;
	String[] suffix;
	String consortium;
	MesquiteModule ownerModule;
	GenBankAuthors(MesquiteModule ownerModule, String xmlPrefsString){
		this.ownerModule = ownerModule;
		lastName = new String[NUMAUTHORS];
		firstName = new String[NUMAUTHORS];
		initials = new String[NUMAUTHORS];
		suffix = new String[NUMAUTHORS];
		XMLUtil.readXMLPreferences(ownerModule,this, xmlPrefsString);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "lastName1", lastName[0]);  
		StringUtil.appendXMLTag(buffer, 2, "firstName1", firstName[0]);  
		StringUtil.appendXMLTag(buffer, 2, "initials1", initials[0]);  
		StringUtil.appendXMLTag(buffer, 2, "suffix1", suffix[0]);  
		StringUtil.appendXMLTag(buffer, 2, "lastName2", lastName[1]);  
		StringUtil.appendXMLTag(buffer, 2, "firstName2", firstName[1]);  
		StringUtil.appendXMLTag(buffer, 2, "initials2", initials[1]);  
		StringUtil.appendXMLTag(buffer, 2, "suffix2", suffix[1]);  
		StringUtil.appendXMLTag(buffer, 2, "lastName3", lastName[2]);  
		StringUtil.appendXMLTag(buffer, 2, "firstName3", firstName[2]);  
		StringUtil.appendXMLTag(buffer, 2, "initials3", initials[2]);  
		StringUtil.appendXMLTag(buffer, 2, "suffix3", suffix[2]);  

		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("lastName1".equalsIgnoreCase(tag))
			lastName[0] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("firstName1".equalsIgnoreCase(tag))
			firstName[0] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("initials1".equalsIgnoreCase(tag))
			initials[0] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("suffix1".equalsIgnoreCase(tag))
			suffix[0] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("lastName2".equalsIgnoreCase(tag))
			lastName[1] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("firstName2".equalsIgnoreCase(tag))
			firstName[1] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("initials2".equalsIgnoreCase(tag))
			initials[1] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("suffix2".equalsIgnoreCase(tag))
			suffix[1] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("lastName3".equalsIgnoreCase(tag))
			lastName[2] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("firstName3".equalsIgnoreCase(tag))
			firstName[2] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("initials3".equalsIgnoreCase(tag))
			initials[2] = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("suffix3".equalsIgnoreCase(tag))
			suffix[2] = StringUtil.cleanXMLEscapeCharacters(content);
	}


	/*.................................................................................................................*/

	public boolean queryValues(){
		if (MesquiteThread.isScripting())
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(), "Authors",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Authors");

		SingleLineTextField[][] contactNameFields = dialog.addNameFieldsMatrix(4, 3, new String[] {"First Name", "Initials", "Last Name", "Suffix"}, new int[] {20, 5, 25, 5});

		for (int i = 0; i<NUMAUTHORS; i++) {
			contactNameFields[0][i].setText(firstName[i]);
			contactNameFields[1][i].setText(initials[i]);
			contactNameFields[2][i].setText(lastName[i]);
			contactNameFields[3][i].setText(suffix[i]);
		}
		dialog.addHorizontalLine(1);
		SingleLineTextField consortiumField = dialog.addTextField("Consortium:", consortium, 20);


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			for (int i = 0; i<NUMAUTHORS; i++) {
				firstName[i] =contactNameFields[0][i].getText();
				initials[i] = contactNameFields[1][i].getText();
				lastName[i] = contactNameFields[2][i].getText();
				suffix[i] = contactNameFields[3][i].getText();			
			}
			consortium = consortiumField.getText();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) ;
	}

	public String getLastName(int i) {
		return lastName[i];
	}
	public String getFirstName(int i) {
		return firstName[i];
	}
	public String getInitials(int i) {
		String s = "";
		if (firstName[i]!=null)
			firstName[i] = firstName[i].trim();
		if (StringUtil.notEmpty(firstName[i])){
			s+= firstName[i].charAt(0) + ".";
		}
		s+= initials[i];
		return s;
	}


	public String getSuffix(int i) {
		return suffix[i];
	}

}

