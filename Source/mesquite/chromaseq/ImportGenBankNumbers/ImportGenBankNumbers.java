package mesquite.chromaseq.ImportGenBankNumbers;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.StringTokenizer;

import javax.swing.JEditorPane;

import mesquite.categ.lib.MolecularData;
import mesquite.chromaseq.lib.GenBankNumbersFileReader;
import mesquite.lib.Associable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.NameReference;
import mesquite.lib.Notification;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.TaxonUtility;
import mesquite.lib.duties.TextDisplayer;
import mesquite.lib.misc.VoucherInfoFromOTUIDDB;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.RadioButtons;
import mesquite.lists.lib.TaxonListUtility;

public class ImportGenBankNumbers extends TaxonListUtility implements ItemListener, ActionListener {
	
	int conflictBehaviour = 0;
	static int conflictIGNORE =0;
	static int conflictADDED=1;
	static int conflictREPLACE =2;
	boolean verboseReport = true;


	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
		hireAllEmployees(GenBankNumbersFileReader.class);
		loadPreferences();
		return true;
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Import GenBank Numbers from File";
	}
	/* ................................................................................................................. */
	public String getNameForMenuItem() {
		return "Import GenBank Numbers from File...";
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return "Reads text files of voucher codes or taxon names by genes and imports matches into '\"GenBankStatus\" note.";
	}

	/* ................................................................................................................. */
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice() {
		return false;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return false;
	}

	/* ................................................................................................................. */
	public boolean isSubstantive() {
		return true;
	}
	/* ................................................................................................................. */
	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	public void itemStateChanged(ItemEvent e) {
		if (e.getItemSelectable() == formats){
			int which = formats.getSelectedIndex();
			explanationOfReader.setText("Explanation of chosen file format:\n" + getChoice(which).getExplanation());

		}
	}
	
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("verboseReport".equalsIgnoreCase(tag)){
			verboseReport = MesquiteBoolean.fromTrueFalseString(content);
		}
		if ("conflictBehaviour".equalsIgnoreCase(tag))
			conflictBehaviour = MesquiteInteger.fromString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "verboseReport",verboseReport);
		StringUtil.appendXMLTag(buffer, 2, "conflictBehaviour",conflictBehaviour);
		return buffer.toString();
	}

	
	
	public void actionPerformed(ActionEvent e) {
		int which = formats.getSelectedIndex();
		String example = ((GenBankNumbersFileReader)getChoice(which)).exampleFile();
		AlertDialog.noticeHTML(dialog, "Example of file format", example, 400, 300, null);

	}

	MesquiteModule getChoice(int i){
		if (choices == null)
			return null;
		if (i>=0 && i<choices.size())
			return (MesquiteModule)choices.elementAt(i);
		return null;
	}

	ExtensibleDialog dialog = null;
	Choice formats;
	TextArea explanationOfReader = null;
	JEditorPane examplePane = null;
	Button exampleButton = null;
	GenBankNumbersFileReader readerChosen = null;

	ListableVector choices = null;
	public boolean queryOptions(MesquiteModule displayer) {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		MesquiteWindow w = containerOfModule();
		if (displayer != null)
			w = displayer.containerOfModule();
		dialog = new ExtensibleDialog(w,  "Importing GenBank Numbers",buttonPressed);  
		ListableVector emps = getEmployeeVector();
		choices = new ListableVector();
		for (int i = 0; i<emps.size(); i++)
			if (emps.elementAt(i) instanceof GenBankNumbersFileReader)
				choices.addElement(emps.elementAt(i), false);
		dialog.addLabel("Format of table in file:");
		formats = dialog.addPopUpMenu ("File formats:", choices, 0);
		formats.addItemListener(this);
		explanationOfReader =dialog.addLargeTextLabel("Explanation of chosen file format:\n\n" + getChoice(0).getExplanation());
		exampleButton = dialog.addButton("Example");
		Container parentOfFormats = formats.getParent();
		Container parentOfButton = exampleButton.getParent();
		readerChosen = null;
		parentOfButton.remove(exampleButton);
		parentOfFormats.add(exampleButton);
		exampleButton.addActionListener(this);

		dialog.addStackedLabels("If you choose to import, and there is already an existing GenBank number", " recorded in the data file, and also an incoming one from the import file:", 0);
		RadioButtons rb = dialog.addRadioButtons(new String[]{"Ignore Incoming", "Add Incoming to Existing", "Replace Existing by Incoming"},conflictBehaviour);
		dialog.addHorizontalLine(1);
		Checkbox verboseReportCheckBox = dialog.addCheckBox("verbose report", verboseReport);
		
		dialog.addHorizontalLine(1);
		dialog.addLargeOrSmallTextLabel("You can choose to Import the GenBank numbers according to the options above, or you can choose to see what is available (but not import or change your current data file) by Surveying the file.");

		dialog.completeAndShowDialog("Survey Only (see Log)", "Import", "Cancel", "Cancel");
		surveyOnly = buttonPressed.getValue()==0;
		if (buttonPressed.getValue()<2)  { //not cancel
			int which = formats.getSelectedIndex();
			readerChosen =(GenBankNumbersFileReader)getChoice(which);
			verboseReport = verboseReportCheckBox.getState();
			conflictBehaviour = rb.getValue();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()<2);
	}


	NameReference genBankColor = NameReference.getNameReference("genbankcolor");
	boolean surveyOnly = true;


	/* ................................................................................................................. */
	/** Called if matrix was not found on first attempt based only on raw value of matrixStringInGenBankTable */
	CharacterData getMatrixFromGenBankTableEntry(Taxa taxa, String matrixStringInGenBankTable) {
		CharacterData matrix =  getProject().getCharacterMatrixReverseOrder((MesquiteFile)null, taxa, null, matrixStringInGenBankTable);  // first look to see if raw string is a matrix name
		if (matrix==null) {  // the complete token didn't match a matrix name, but let's see if the token can be parsed into components separated by ;
			Parser parser = new Parser (matrixStringInGenBankTable);
			parser.setWhitespaceString(";");
			String gene = parser.getFirstToken();
			while (StringUtil.notEmpty(gene)) {
				gene = StringUtil.stripBoundingWhitespace(gene);
				matrix =  getProject().getCharacterMatrixReverseOrder((MesquiteFile)null, taxa, null, gene);
				if (matrix!=null)
					break;
				gene = parser.getNextToken();
			}
		}
		return matrix;
	}

	/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
	public boolean operateOnTaxa(MesquiteTable table, Taxa taxa){
		MesquiteFile file = 	MesquiteFile.open(true, (String)null, "Choose text file with GenBank number information", null);

		if (file == null)
			return false;
		String path = file.getPath();
		String preview = MesquiteFile.getFileContentsAsString(path, 4000); 
		if (StringUtil.blank(preview))
			return false;
		TextDisplayer displayer = (TextDisplayer)hireEmployee(TextDisplayer.class, null);
		if (displayer!=null){
			displayer.showText(preview, "File contents", true); 
		}

		//Now present dialog with choices
		if (!queryOptions(displayer)) {
			fireEmployee(displayer);
			return false;
		}
		fireEmployee(displayer);
		String verb = "will ";
		if (surveyOnly){
			verb = "would ";
			log("\nYou have asked for \"survey-only\" mode, which means that Mesquite will not import any information to your data file," 
			+" but will only report on what Genbank information would be imported according to the requested options. "
					+"In addition, the cells of the \"GenBank Number\" columns in the List of Taxa will be colored to indicate what information is available in the import.\n\nOption Chosen: ");
		}
		
			log("If GenBank information already exists in data file for the taxon and gene, any incoming GenBank information " + verb);
			if (conflictBehaviour == conflictIGNORE)
				logln("be ignored.");
			else if (conflictBehaviour == conflictADDED)
				logln("be added to the information already existing.");
			else if (conflictBehaviour == conflictREPLACE)
				logln("replace the information already existing!");
			if (surveyOnly)
				logln("");

		


		String[][] genbanktable = readerChosen.readTable(path);

		if (genbanktable == null || genbanktable.length == 0)
			return false;

		int count = 0;
		boolean anySelected = taxa.anySelected();
		if (anySelected)
			logln("Only those taxa that are selected " + verb +"be considered.");
		String thoseNotFound = "";
		boolean first = true;
		boolean colored = false;

		for (int im = 0; im<getProject().getNumberCharMatrices(taxa); im++){
			CharacterData data = getProject().getCharacterMatrix(taxa, im);
			Associable associable = data.getTaxaInfo(false);  //the metadata is associated with this, not with the matrix directly
			if (associable != null){
				for (int it = 0; it<taxa.getNumTaxa(); it++) {
					if (associable.getAssociatedObject(genBankColor, it) != null){
						associable.setAssociatedObject(genBankColor,  it, null);  //reset to no color before starting
						colored = true;
					}
				}
			}
		}


		for (int row = 0; row< genbanktable.length; row++){
			int it = findTaxon(taxa, genbanktable[row][GenBankNumbersFileReader.ID]);
			if (it>=0 && (!anySelected || taxa.getSelected(it))){
				CharacterData matrix = getMatrixFromGenBankTableEntry(taxa, genbanktable[row][GenBankNumbersFileReader.GENE]);
				if (matrix != null){
					Associable associable = matrix.getTaxaInfo(false);  //the metadata is associated with this, not with the matrix directly
					if (!StringUtil.blank(genbanktable[row][GenBankNumbersFileReader.GENBANK])){
						String current = (String)associable.getAssociatedString(MolecularData.genBankNumberRef,  it);
						if (surveyOnly || conflictBehaviour != conflictIGNORE || StringUtil.blank(current)){
							String incoming = StringUtil.stripBoundingWhitespace(genbanktable[row][GenBankNumbersFileReader.GENBANK]);
							String incomingCompact = StringUtil.stripWhitespace(incoming);
							String currentCompact = StringUtil.stripWhitespace(current);
							Color color = null;
							String report = null;
							if (!StringUtil.blank(incoming) && incoming.length() > 1){
								if (StringUtil.blank(current)){
									current = incoming;
									if (surveyOnly) {
										report = "to be acquired";
										color = Color.yellow;
									}
									else {
										report = "acquired";
										color = ColorDistribution.veryLightYellow;
									}
								}
								else if (current.length() == 1 && incoming.length()>1){ //current is just one character, a placeholder; ignore it and replace by incoming
									current = incoming;
									if (surveyOnly) {
										report = "to be acquired";
										color = Color.yellow;
									}
									else {
										report = "acquired";
										color = ColorDistribution.veryLightYellow;
									}
								}
								else if (incoming.indexOf(current)>= 0 && incoming.length() > current.length()){  //incoming is superstring of current; replace by current
									current = incoming;
									if (surveyOnly) {
										color = Color.magenta;
										report = "to REPLACE previous \"" + current + "\"";
									}
									else {
										color = ColorDistribution.veryLightYellow;
										report = "REPLACED previous \"" + current + "\"";
									}
								}
								else if (current.equals(incoming)){  //same; do nothing
									if (verboseReport)
										report = "same as previous \"" + current + "\"";
									if (surveyOnly) {
										color = ColorDistribution.veryLightGreen;
									}
								}
								else if (current.indexOf(incoming)>= 0){  //already included
									report = "already included in previous \"" + current + "\"";
									if (surveyOnly)
										color = ColorDistribution.veryLightGreen;
								}
								else if (!StringUtil.blank(incomingCompact) && incomingCompact.indexOf(currentCompact)<0){  
									if (conflictBehaviour == conflictADDED){
										if (surveyOnly)
											report = "to append to previous \"" + current + "\"";
										else
											report = "appended to previous \"" + current + "\"";

										current = current + ";" + incoming;
									}
									else {
										if (surveyOnly) {
											if (conflictBehaviour==conflictREPLACE)
												report = "to REPLACE previous \"" + current + "\"";
										}
										else
											report = "REPLACED to previous \"" + current + "\"";
										current =  incoming;
									}
									if (surveyOnly)
										color = Color.magenta;
									else
										color = ColorDistribution.veryLightYellow;
								}
								if (!surveyOnly)
									associable.setAssociatedString(MolecularData.genBankNumberRef,  it, StringUtil.stripBoundingWhitespace(current));
							}
							if (report != null) {
								if (first)
									logln("GenBank numbers found. Report: ");
								logln("\"" + incoming + "\" " + report + " for gene " + matrix.getName() + " in taxon " + taxa.getTaxonName(it) + " (" + genbanktable[row][GenBankNumbersFileReader.ID] + ")");
								first = false;
							}
							if (color != null) {
								colored = true;
								associable.setAssociatedObject(genBankColor,  it, color);  //color is not saved
							}
							count++;
						}

					}
				}
			}
			else if (!StringUtil.blank(genbanktable[row][GenBankNumbersFileReader.ID]))
				thoseNotFound += " " + genbanktable[row][GenBankNumbersFileReader.ID];

		}
		logln("");
		if (first)
			logln("No GenBank numbers found that match taxon IDs and genes in this file.");
		if (colored) {
			ListableVector datas = getProject().getCharacterMatrices();
			datas.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
		}
		if (!surveyOnly && !first)
			logln("GenBank numbers imported.  Number of matches found: " + count);
		if (!StringUtil.blank(thoseNotFound) && verboseReport)
			logln("Taxon ID's in table but not found in taxa block: " + thoseNotFound);
		logln("");
		return true;
	}
	private int findTaxon(Taxa taxa, String id){
		if (StringUtil.blank(id))
			return -1;
		for (int it =0; it<taxa.getNumTaxa(); it++){
			String code = taxa.getAssociatedString(VoucherInfoFromOTUIDDB.voucherCodeRef, it);
			if (!StringUtil.blank(code)){
				if (code.equalsIgnoreCase(id))
					return it;
				StringTokenizer tokenizer = new StringTokenizer(code, "/");
				while (tokenizer.hasMoreElements()){
					String token = tokenizer.nextToken();
					if (!StringUtil.blank(token) && token.equalsIgnoreCase(id))
						return it;
				}
				if (id.indexOf("/")>=0){
					if (StringUtil.indexOfIgnoreCase(code, id)>=0)
						return it;
				}
			}
			String name = taxa.getTaxonName( it);
			if (!StringUtil.blank(name) && name.equalsIgnoreCase(id))
				return it;

		}
		return -1;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -1700;  
	}


}
