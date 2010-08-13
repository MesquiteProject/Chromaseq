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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.Panel;
import java.awt.event.*;
import java.io.*;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;

/* This module supplies the sample code and primer name given the chromatogram file name, for chromatogram file names that contain the sample code and primer name directly */

public class SampleAndPrimerFileNameParser extends ChromatogramFileNameParser {
	public ListableVector nameParsingRules;
	public Choice choice;
	public String prefDirectoryName = "ChromNameParsingRules";
	ChooseNameParsingRuleDLOG chooseNameParsingRuleDialog;

	private ChromFileNameParsing nameParsingRule=null;
	private Choice nameRulesChoice;	
	private String nameParsingRulesName="";	

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		nameParsingRules = new ListableVector();
		loadNameParsingRules();
		if (getNumRules()<=0) {
			ChromFileNameParsing defaultRule = new ChromFileNameParsing();
			nameParsingRules.addElement(defaultRule, false);
		}
		int ruleNumber = nameParsingRules.indexOfByNameIgnoreCase(nameParsingRulesName);
		if (ruleNumber>=0)
			nameParsingRule = (ChromFileNameParsing)(nameParsingRules.elementAt(ruleNumber));
		return true;
	}
	/*.................................................................................................................*/
	
	public boolean optionsSpecified(){
		boolean db = StringUtil.notEmpty(nameParsingRulesName);
		db = nameParsingRule!=null;
		int ruleNumber = nameParsingRules.indexOfByNameIgnoreCase(nameParsingRulesName);
		
		return (StringUtil.notEmpty(nameParsingRulesName) && nameParsingRule!=null) ;
	}
	public boolean hasOptions(){
		return true;
	}


	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		ChromFileNameDialog dialog = new ChromFileNameDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Choose rule describing file name structure", buttonPressed, this, nameParsingRulesName);		


		String s = "Mesquite searches within the name of each chromatogram file for both a code indicating the sample (e.g., a voucher number) and the primer name. ";
		s+= "To allow this, you must first define a rule that defines how the chromatogram file names are structured.\n\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			nameParsingRule = dialog.getNameParsingRule();
			nameParsingRulesName = nameParsingRule.getName();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "nameParsingRulesName", nameParsingRulesName);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public String getParameters () {
		if (StringUtil.blank(nameParsingRulesName))
			return "Chromatogram File Name Rule: not specified.";
		return "Chromatogram File Name Rule: " + nameParsingRulesName;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("nameParsingRulesName".equalsIgnoreCase(tag))
			nameParsingRulesName = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public ChromFileNameParsing loadNameRulesFile(String cPath, String fileName, boolean requiresEnding,  boolean userDef) {
		File cFile = new File(cPath);
		if (cFile.exists() && !cFile.isDirectory() && (!requiresEnding || fileName.endsWith("xml"))) {
			String contents = MesquiteFile.getFileContentsAsString(cPath);
			if (!StringUtil.blank(contents)) {
				ChromFileNameParsing localNameParsingRules = new ChromFileNameParsing();
				localNameParsingRules.path = cPath;
				if  (localNameParsingRules.readXML(contents)){
					nameParsingRules.addElement(localNameParsingRules, false);
					return localNameParsingRules;
				}
				return null;
			}
		}
		return null;
	}

	/*.................................................................................................................*/

	public boolean parseFileName(String fileName, MesquiteString sampleCode, MesquiteString sampleCodeSuffix, MesquiteString primerName, StringBuffer logBuffer, 
			MesquiteString startTokenResult){
		if (nameParsingRule==null)
			return false;
		return nameParsingRule.parseFileName(this, fileName, sampleCode, sampleCodeSuffix, primerName, logBuffer, startTokenResult);
	}

	public void setChoice (Choice choice) {
		this.choice = choice;
	}
	public Choice getChoice() {
		return choice;
	}
	public int getNumRules() {
		return nameParsingRules.getNumberOfParts();
	}

	/*.................................................................................................................*/
	private void loadNameParsingRules(String path, File nameRulesDir, boolean userDef){
		if (nameRulesDir.exists() && nameRulesDir.isDirectory()) {
			String[] nameRulesList = nameRulesDir.list();
			for (int i=0; i<nameRulesList.length; i++) {
				if (nameRulesList[i]==null )
					;
				else {
					String cPath = path + MesquiteFile.fileSeparator + nameRulesList[i];
					loadNameRulesFile(cPath, nameRulesList[i], true, userDef);
				}
			}
		}
	}
	private void loadNameParsingRules(){
		String path = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator + prefDirectoryName;
		File nameRulesDir = new File(path);
		loadNameParsingRules(path, nameRulesDir, true);
	}

	public ChromFileNameParsing chooseNameParsingRules(ChromFileNameParsing rule) {

		ChromFileNameParsing nameParsingRule = rule;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		chooseNameParsingRuleDialog = new ChooseNameParsingRuleDLOG(this, nameParsingRulesName, buttonPressed);
		//chooseNameParsingRuleDialog.completeAndShowDialog();
		boolean ok = (buttonPressed.getValue()==0);

		if (ok && choice !=null) {
			nameParsingRulesName = choice.getSelectedItem();
			int sL = nameParsingRules.indexOfByName(nameParsingRulesName);
			if (sL >=0 && sL < nameParsingRules.size()) {
				nameParsingRule = (ChromFileNameParsing)nameParsingRules.elementAt(sL);
			}
			storePreferences();
		}
		chooseNameParsingRuleDialog.dispose();
		chooseNameParsingRuleDialog = null;
		return nameParsingRule;
	}


	/*.................................................................................................................*/
	public int numNameRules(){
		return nameParsingRules.size();
	}
	/*.................................................................................................................*/
	public MesquiteString getNameRule(String name){
		int i = nameParsingRules.indexOfByName(name);
		if (i<0)
			return null;
		Listable listable = nameParsingRules.elementAt(i);
		if (listable!=null)
			return new MesquiteString(listable.getName());	
		else 
			return null;
	}
	/*.................................................................................................................*/
	public int findNameRule(String name){
		return nameParsingRules.indexOfByName(name);
	}
	/*.................................................................................................................*/
	public MesquiteString getNameRule(int i){
		if (i<0 || i>= nameParsingRules.size())
			return null;
		Listable listable = nameParsingRules.elementAt(i);
		if (listable!=null)
			return new MesquiteString(listable.getName());	
		else 
			return null;
	}
	/*.................................................................................................................*/
	private String newNameRulePath(String name){
		String base = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator + prefDirectoryName;
		if (!MesquiteFile.fileExists(base)) {
			File f = new File(base);
			f.mkdir();
		}
		String candidate = base + MesquiteFile.fileSeparator + StringUtil.punctuationToUnderline(name)+ ".xml";
		if (!MesquiteFile.fileExists(candidate))
			return candidate;
		candidate = base + MesquiteFile.fileSeparator  + "nameRule1.xml";
		int count = 2;
		while (MesquiteFile.fileExists(candidate)){
			candidate = base + MesquiteFile.fileSeparator  + "nameRule" + (count++) + ".xml";
		}
		return candidate;
	}
	/*.................................................................................................................*/
	public void addNameRule(ChromFileNameParsing chromFileNameParsing, String name){
		chromFileNameParsing.save(newNameRulePath(name), name);
		nameParsingRules.addElement(chromFileNameParsing, false);	
		choice.add(name);
		nameParsingRulesName = name;
		//	return s;
	}
	/*.................................................................................................................*/
	public ChromFileNameParsing duplicateNameRule(ChromFileNameParsing chromFileNameParsing, String name){
		ChromFileNameParsing rule = new ChromFileNameParsing(chromFileNameParsing);
		rule.setName(name);
		rule.setPath(newNameRulePath(name));
		rule.save();
		nameParsingRules.addElement(rule, false);	
		choice.add(name);
		nameParsingRulesName = name;
		return rule;
		//	return s;
	}
	/*.................................................................................................................*/
	void renameNameRule(int i, String name){
		ChromFileNameParsing rule = (ChromFileNameParsing)nameParsingRules.elementAt(i);
		rule.setName(name);
		rule.save();
		choice.remove(i);
		choice.insert(name,i);
		nameParsingRulesName=name;
	}
	/*.................................................................................................................*/
	void deleteNameRule(int i){
		ChromFileNameParsing rule = (ChromFileNameParsing)nameParsingRules.elementAt(i);
		if (rule!=null) {
			String oldTemplateName = rule.getName();
			File f = new File(rule.path);
			f.delete();		
			//MesquiteString s = getNameRule(i);
			//if (s !=null)
			nameParsingRules.removeElement(rule, false);  //deletes it from the vector
			choice.remove(i);
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Basic Chromatogram File Name Parser";
	}

}	



/*=======================================================================*/
class ChooseNameParsingRuleDLOG extends ExtensibleListDialog {
	SampleAndPrimerFileNameParser ownerModule;
	boolean editLastItem = false;

	public ChooseNameParsingRuleDLOG (SampleAndPrimerFileNameParser ownerModule, String nameParsingRulesName, MesquiteInteger buttonPressed){
		super(ownerModule.containerOfModule(), "File Name Rules Manager", "File Name Rules", ownerModule.nameParsingRules);
		this.ownerModule = ownerModule;
		/*
			String message = "This dialog box allows you to create and edit snippets of code stored in the current file or project, to be inserted into batch files according to their names.";
			message += "  They are used, for instance, in parametric bootstrapping to store commands (specific to this file) that might be placed in the batch file to instruct another program.";
			message += " Code snippets are stored in the Mesquite file, and thus in order to save them, you need to save the Mesquite file.";
			appendToHelpString(message);
			setHelpURL(ownerModule,"");
		 */		
		completeAndShowDialog("Done", null, true, null);

	}
	/*.................................................................................................................*/
	public void windowOpened(WindowEvent e){
		if (editLastItem)
			editNumberedElement(getLastItem());
		editLastItem = false;
		super.windowOpened(e);
	}
	/*.................................................................................................................*/
	/** this is the name of the class of objects */
	public  String objectName(){
		return "Chromatogram File Name Rule";
	}
	/*.................................................................................................................*/
	/** this is the name of the class of objects */
	public  String pluralObjectName(){
		return "Chromatogram File Name Rules";
	}

	/*.................................................................................................................*/
	public Listable createNewElement(String name, MesquiteBoolean success){
		hide();
		ChromFileNameParsing chromFileNameParsing = new ChromFileNameParsing();
		if (chromFileNameParsing.queryOptions(name)) {
			addNewElement(chromFileNameParsing,name);  //add name to list
			ownerModule.addNameRule(chromFileNameParsing, name);
			if (success!=null) success.setValue(true);
			setVisible(true);
			return chromFileNameParsing;

		}
		else  {
			if (success!=null) success.setValue(false);
			setVisible(true);
			return null;
		}
	}
	/*.................................................................................................................*/
	public void deleteElement(int item, int newSelectedItem){
		hide();
		ownerModule.deleteNameRule(item);
		setVisible(true);
	}
	/*.................................................................................................................*/
	public void renameElement(int item, Listable element, String newName){
		ownerModule.renameNameRule(item,newName);
	}
	/*.................................................................................................................*/
	public Listable duplicateElement(String name){
		ChromFileNameParsing rule = ownerModule.duplicateNameRule((ChromFileNameParsing)currentElement, name);
		return rule;
	}
	/*.................................................................................................................*/
	public boolean getEditable(int item){
		return true;
	}
	/*.................................................................................................................*/
	public void editElement(int item){
		hide();
		ChromFileNameParsing rule = ((ChromFileNameParsing)ownerModule.nameParsingRules.elementAt(item));
		if (rule.queryOptions(rule.getName()))
			rule.save();
		setVisible(true);
	}




}

