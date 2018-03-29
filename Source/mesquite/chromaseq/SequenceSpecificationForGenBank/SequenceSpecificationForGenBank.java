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

package mesquite.chromaseq.SequenceSpecificationForGenBank;

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

public class SequenceSpecificationForGenBank extends SequenceSpecificationManager {
	public ListableVector sequenceSpecificationVector;
	public Choice choice;
	public String prefDirectoryName = "SequenceSpecifications";
	ChooseGeneSpecificationDLOG chooseSequenceSpecificationDialog;

	private SequenceSpecification sequenceSpecification=null;
	private Choice sequenceSpecificationChoice;	
	private String sequenceSpecificationName="";	

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		sequenceSpecificationVector = new ListableVector();
		loadSequenceSpecifications();
		if (getNumRules()<=0) {
			SequenceSpecification defaultRule = new SequenceSpecification();
			sequenceSpecificationVector.addElement(defaultRule, false);
		}
		int ruleNumber = sequenceSpecificationVector.indexOfByNameIgnoreCase(sequenceSpecificationName);
		if (ruleNumber>=0)
			sequenceSpecification = (SequenceSpecification)(sequenceSpecificationVector.elementAt(ruleNumber));
		return true;
	}
	/*.................................................................................................................*/
	
	public boolean optionsSpecified(){
		boolean db = StringUtil.notEmpty(sequenceSpecificationName);
		db = sequenceSpecification!=null;
		int ruleNumber = sequenceSpecificationVector.indexOfByNameIgnoreCase(sequenceSpecificationName);
		
		return (StringUtil.notEmpty(sequenceSpecificationName) && sequenceSpecification!=null) ;
	}
	public boolean hasOptions(){
		return true;
	}

	public  String getSequenceModifiers(int sequenceType) {
		return "";
	}
	/*.................................................................................................................*/
	public String[] getListOfSpecifications(){
		if (sequenceSpecificationVector==null || sequenceSpecificationVector.size()<1)
			return null;
		return sequenceSpecificationVector.getStringArrayList();
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		SequenceSpecificationDialog dialog = new SequenceSpecificationDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Choose the sequence specification", buttonPressed, this, sequenceSpecificationName);		


		String s = "In preparing sequences for GenBank submission, Mesquite saves data about the sequence (e.g., gene name, genetic code, etc.). ";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			sequenceSpecification = dialog.getNameParsingRule();
			sequenceSpecificationName = sequenceSpecification.getName();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "sequenceSpecificationName", sequenceSpecificationName);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public String getParameters () {
		if (StringUtil.blank(sequenceSpecificationName))
			return "Sequence specification: not chosen.";
		return "Sequence specification: " + sequenceSpecificationName;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("sequenceSpecificationName".equalsIgnoreCase(tag))
			sequenceSpecificationName = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public SequenceSpecification loadSequenceSpecificationFile(String cPath, String fileName, boolean requiresEnding,  boolean userDef) {
		File cFile = new File(cPath);
		if (cFile.exists() && !cFile.isDirectory() && (!requiresEnding || fileName.endsWith("xml"))) {
			String contents = MesquiteFile.getFileContentsAsString(cPath);
			if (!StringUtil.blank(contents)) {
				SequenceSpecification localSequenceSpecification = new SequenceSpecification();
				localSequenceSpecification.path = cPath;
				if  (localSequenceSpecification.readXML(contents)){
					sequenceSpecificationVector.addElement(localSequenceSpecification, false);
					return localSequenceSpecification;
				}
				return null;
			}
		}
		return null;
	}


	public void setChoice (Choice choice) {
		this.choice = choice;
	}
	public Choice getChoice() {
		return choice;
	}
	public int getNumRules() {
		return sequenceSpecificationVector.getNumberOfParts();
	}

	/*.................................................................................................................*/
	private void loadSequenceSpecifications(String path, File storageDir, boolean userDef){
		if (storageDir.exists() && storageDir.isDirectory()) {
			String[] fileNames = storageDir.list();
			for (int i=0; i<fileNames.length; i++) {
				if (fileNames[i]==null )
					;
				else {
					String cPath = path + MesquiteFile.fileSeparator + fileNames[i];
					loadSequenceSpecificationFile(cPath, fileNames[i], true, userDef);
				}
			}
		}
	}
	private void loadSequenceSpecifications(){
		String path = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator + prefDirectoryName;
		File storageDir = new File(path);
		loadSequenceSpecifications(path, storageDir, true);
	}

	public SequenceSpecification chooseSequenceSpecifiation(SequenceSpecification spec) {

		SequenceSpecification sequenceSpecification = spec;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		chooseSequenceSpecificationDialog = new ChooseGeneSpecificationDLOG(this, sequenceSpecificationName, buttonPressed);
		boolean ok = (buttonPressed.getValue()==0);

		if (ok && choice !=null) {
			sequenceSpecificationName = choice.getSelectedItem();
			int sL = sequenceSpecificationVector.indexOfByName(sequenceSpecificationName);
			if (sL >=0 && sL < sequenceSpecificationVector.size()) {
				sequenceSpecification = (SequenceSpecification)sequenceSpecificationVector.elementAt(sL);
			}
			storePreferences();
		}
		chooseSequenceSpecificationDialog.dispose();
		chooseSequenceSpecificationDialog = null;
		return sequenceSpecification;
	}
	/*.................................................................................................................*/
	public boolean manageSequenceSpecifications() {

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		chooseSequenceSpecificationDialog = new ChooseGeneSpecificationDLOG(this, sequenceSpecificationName, buttonPressed);

		if (choice !=null) {
			sequenceSpecificationName = choice.getSelectedItem();
			int sL = sequenceSpecificationVector.indexOfByName(sequenceSpecificationName);
			if (sL >=0 && sL < sequenceSpecificationVector.size()) {
				sequenceSpecification = (SequenceSpecification)sequenceSpecificationVector.elementAt(sL);
			}
			storePreferences();
		}
		chooseSequenceSpecificationDialog.dispose();
		chooseSequenceSpecificationDialog = null;
		return true;
	}


	/*.................................................................................................................*/
	public int numSpecifications(){
		return sequenceSpecificationVector.size();
	}
	/*.................................................................................................................*/
	public MesquiteString getSpecification(String name){
		int i = sequenceSpecificationVector.indexOfByName(name);
		if (i<0)
			return null;
		Listable listable = sequenceSpecificationVector.elementAt(i);
		if (listable!=null)
			return new MesquiteString(listable.getName());	
		else 
			return null;
	}
	/*.................................................................................................................*/
	public int findSpecification(String name){
		return sequenceSpecificationVector.indexOfByName(name);
	}
	/*.................................................................................................................*/
	public MesquiteString getSpecification(int i){
		if (i<0 || i>= sequenceSpecificationVector.size())
			return null;
		Listable listable = sequenceSpecificationVector.elementAt(i);
		if (listable!=null)
			return new MesquiteString(listable.getName());	
		else 
			return null;
	}
	/*.................................................................................................................*/
	private String newSpecificationPath(String name){
		String base = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator + prefDirectoryName;
		if (!MesquiteFile.fileExists(base)) {
			File f = new File(base);
			f.mkdir();
		}
		String candidate = base + MesquiteFile.fileSeparator + StringUtil.punctuationToUnderline(name)+ ".xml";
		if (!MesquiteFile.fileExists(candidate))
			return candidate;
		candidate = base + MesquiteFile.fileSeparator  + "specification1.xml";
		int count = 2;
		while (MesquiteFile.fileExists(candidate)){
			candidate = base + MesquiteFile.fileSeparator  + "specification" + (count++) + ".xml";
		}
		return candidate;
	}
	/*.................................................................................................................*/
	public void addSpecification(SequenceSpecification sequenceSpecification, String name){
		sequenceSpecification.save(newSpecificationPath(name), name);
		sequenceSpecificationVector.addElement(sequenceSpecification, false);	
		choice.add(name);
		sequenceSpecificationName = name;
		//	return s;
	}
	/*.................................................................................................................*/
	public SequenceSpecification duplicateNameRule(SequenceSpecification sequenceSpecification, String name){
		SequenceSpecification specification = new SequenceSpecification(sequenceSpecification);
		specification.setName(name);
		specification.setPath(newSpecificationPath(name));
		specification.save();
		sequenceSpecificationVector.addElement(specification, false);	
		choice.add(name);
		sequenceSpecificationName = name;
		return specification;
		//	return s;
	}
	/*.................................................................................................................*/
	void renameSpecification(int i, String name){
		SequenceSpecification specification = (SequenceSpecification)sequenceSpecificationVector.elementAt(i);
		specification.setName(name);
		specification.save();
		choice.remove(i);
		choice.insert(name,i);
		sequenceSpecificationName=name;
	}
	/*.................................................................................................................*/
	void deleteSpecification(int i){
		SequenceSpecification specification = (SequenceSpecification)sequenceSpecificationVector.elementAt(i);
		if (specification!=null) {
			String oldTemplateName = specification.getName();
			File f = new File(specification.path);
			f.delete();		
			//MesquiteString s = getNameRule(i);
			//if (s !=null)
			sequenceSpecificationVector.removeElement(specification, false);  //deletes it from the vector
			choice.remove(i);
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Sequence Specification for GenBank";
	}

}	



/*=======================================================================*/
class ChooseGeneSpecificationDLOG extends ExtensibleListDialog {
	SequenceSpecificationForGenBank ownerModule;
	boolean editLastItem = false;

	public ChooseGeneSpecificationDLOG (SequenceSpecificationForGenBank ownerModule, String nameParsingRulesName, MesquiteInteger buttonPressed){
		super(ownerModule.containerOfModule(), "Sequence Specification Manager", "Sequence Specification", buttonPressed, ownerModule.sequenceSpecificationVector);
		this.ownerModule = ownerModule;
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
		return "Sequence Specification";
	}
	/*.................................................................................................................*/
	/** this is the name of the class of objects */
	public  String pluralObjectName(){
		return "Sequence Specifications";
	}

	/*.................................................................................................................*/
	public Listable createNewElement(String name, MesquiteBoolean success){
		hide();
		SequenceSpecification sequenceSpecification = new SequenceSpecification();
		if (sequenceSpecification.queryOptions(name)) {
			addNewElement(sequenceSpecification,name);  //add name to list
			ownerModule.addSpecification(sequenceSpecification, name);
			if (success!=null) success.setValue(true);
			setVisible(true);
			return sequenceSpecification;

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
		ownerModule.deleteSpecification(item);
		setVisible(true);
	}
	/*.................................................................................................................*/
	public void renameElement(int item, Listable element, String newName){
		ownerModule.renameSpecification(item,newName);
	}
	/*.................................................................................................................*/
	public Listable duplicateElement(String name){
		SequenceSpecification sequenceSpecification = ownerModule.duplicateNameRule((SequenceSpecification)currentElement, name);
		return sequenceSpecification;
	}
	/*.................................................................................................................*/
	public boolean getEditable(int item){
		return true;
	}
	/*.................................................................................................................*/
	public void editElement(int item){
		//hide();
		SequenceSpecification sequenceSpecification = ((SequenceSpecification)ownerModule.sequenceSpecificationVector.elementAt(item));
		if (sequenceSpecification.queryOptions(sequenceSpecification.getName()))
			sequenceSpecification.save();
		setVisible(true);
	}




}

