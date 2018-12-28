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

package mesquite.chromaseq.SequenceProfileForGenBank;

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

public class SequenceProfileForGenBank extends SequenceProfileManager {
	public ListableVector sequenceProfileVector;
	public Choice choice;
	public String prefDirectoryName = "SequenceProfilesForGenBank";
	ManageGeneProfileDLOG chooseSequenceProfileDialog;

	private SequenceProfile sequenceProfile=null;
	private String sequenceProfileName="";	

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		sequenceProfileVector = new ListableVector();
		loadSequenceSpecifications();
		if (getNumRules()<=0) {
			SequenceProfile defaultRule = new SequenceProfile();
			sequenceProfileVector.addElement(defaultRule, false);
		}
		int ruleNumber = sequenceProfileVector.indexOfByNameIgnoreCase(sequenceProfileName);
		if (ruleNumber>=0)
			sequenceProfile = (SequenceProfile)(sequenceProfileVector.elementAt(ruleNumber));
		return true;
	}
	/*.................................................................................................................*/
	
	public boolean optionsSpecified(){
		boolean db = StringUtil.notEmpty(sequenceProfileName);
		db = sequenceProfile!=null;
		int ruleNumber = sequenceProfileVector.indexOfByNameIgnoreCase(sequenceProfileName);
		
		return (StringUtil.notEmpty(sequenceProfileName) && sequenceProfile!=null) ;
	}
	public boolean hasOptions(){
		return true;
	}

	public  String getSequenceModifiers(int sequenceType) {
		return "";
	}
	/*.................................................................................................................*/
	public String[] getListOfProfiles(){
		if (sequenceProfileVector==null || sequenceProfileVector.size()<1)
			return null;
		return sequenceProfileVector.getStringArrayList();
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);
		SequenceProfileDialog dialog = new SequenceProfileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Choose the sequence profile", buttonPressed, this, sequenceProfileName);		


		String s = "In preparing sequences for GenBank submission, Mesquite saves data about the sequence (e.g., gene name, genetic code, etc.). ";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			sequenceProfile = dialog.getNameParsingRule();
			sequenceProfileName = sequenceProfile.getName();
		}
		storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return success;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "sequenceSpecificationName", sequenceProfileName);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public String getParameters () {
		if (StringUtil.blank(sequenceProfileName))
			return "Sequence profile: not chosen.";
		return "Sequence profile: " + sequenceProfileName;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("sequenceSpecificationName".equalsIgnoreCase(tag))
			sequenceProfileName = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public SequenceProfile loadSequenceSpecificationFile(String cPath, String fileName, boolean requiresEnding,  boolean userDef) {
		File cFile = new File(cPath);
		if (cFile.exists() && !cFile.isDirectory() && (!requiresEnding || fileName.endsWith("xml"))) {
			String contents = MesquiteFile.getFileContentsAsString(cPath);
			if (!StringUtil.blank(contents)) {
				SequenceProfile localSequenceSpecification = new SequenceProfile();
				localSequenceSpecification.path = cPath;
				if  (localSequenceSpecification.readXML(contents)){
					sequenceProfileVector.addElement(localSequenceSpecification, false);
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
		return sequenceProfileVector.getNumberOfParts();
	}

	/*.................................................................................................................*/
	private void loadSequenceSpecifications(String path, File storageDir, boolean userDef){
		if (storageDir.exists() && storageDir.isDirectory()) {
			String[] fileNames = storageDir.list();
			StringArray.sort(fileNames);
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

	public SequenceProfile chooseSequenceSpecifiation(SequenceProfile spec) {

		SequenceProfile sequenceSpecification = spec;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		chooseSequenceProfileDialog = new ManageGeneProfileDLOG(this, sequenceProfileName, buttonPressed);
		boolean ok = (buttonPressed.getValue()==0);

		if (ok && choice !=null) {
			sequenceProfileName = choice.getSelectedItem();
			int sL = sequenceProfileVector.indexOfByName(sequenceProfileName);
			if (sL >=0 && sL < sequenceProfileVector.size()) {
				sequenceSpecification = (SequenceProfile)sequenceProfileVector.elementAt(sL);
			}
			storePreferences();
		}
		chooseSequenceProfileDialog.dispose();
		chooseSequenceProfileDialog = null;
		return sequenceSpecification;
	}
	/*.................................................................................................................*/
	public boolean manageSequenceProfiles() {

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		chooseSequenceProfileDialog = new ManageGeneProfileDLOG(this, sequenceProfileName, buttonPressed);

		if (choice !=null) {
			sequenceProfileName = choice.getSelectedItem();
			int sL = sequenceProfileVector.indexOfByName(sequenceProfileName);
			if (sL >=0 && sL < sequenceProfileVector.size()) {
				sequenceProfile = (SequenceProfile)sequenceProfileVector.elementAt(sL);
			}
			storePreferences();
		}
		chooseSequenceProfileDialog.dispose();
		chooseSequenceProfileDialog = null;
		return true;
	}


	/*.................................................................................................................*/
	public int numSpecifications(){
		return sequenceProfileVector.size();
	}
	/*.................................................................................................................*/
	public MesquiteString getSpecification(String name){
		int i = sequenceProfileVector.indexOfByName(name);
		if (i<0)
			return null;
		Listable listable = sequenceProfileVector.elementAt(i);
		if (listable!=null)
			return new MesquiteString(listable.getName());	
		else 
			return null;
	}
	/*.................................................................................................................*/
	public int findSpecificationIndex(String name){
		return sequenceProfileVector.indexOfByName(name);
	}
	/*.................................................................................................................*/
	public SequenceProfile getSequenceProfile(int index){
		return (SequenceProfile)(sequenceProfileVector.elementAt(index));

	}
	/*.................................................................................................................*/
	public MesquiteString getSpecification(int i){
		if (i<0 || i>= sequenceProfileVector.size())
			return null;
		Listable listable = sequenceProfileVector.elementAt(i);
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
	public void addSpecification(SequenceProfile sequenceSpecification, String name){
		sequenceSpecification.save(newSpecificationPath(name), name);
		sequenceProfileVector.addElement(sequenceSpecification, false);	
		choice.add(name);
		sequenceProfileName = name;
		//	return s;
	}
	/*.................................................................................................................*/
	public SequenceProfile duplicateNameRule(SequenceProfile sequenceSpecification, String name){
		SequenceProfile specification = new SequenceProfile(sequenceSpecification);
		specification.setName(name);
		specification.setPath(newSpecificationPath(name));
		specification.save();
		sequenceProfileVector.addElement(specification, false);	
		choice.add(name);
		sequenceProfileName = name;
		return specification;
		//	return s;
	}
	/*.................................................................................................................*/
	void renameSpecification(int i, String name){
		SequenceProfile specification = (SequenceProfile)sequenceProfileVector.elementAt(i);
		specification.setName(name);
		specification.save();
		choice.remove(i);
		choice.insert(name,i);
		sequenceProfileName=name;
	}
	/*.................................................................................................................*/
	void deleteSpecification(int i){
		SequenceProfile specification = (SequenceProfile)sequenceProfileVector.elementAt(i);
		if (specification!=null) {
			String oldTemplateName = specification.getName();
			File f = new File(specification.path);
			f.delete();		
			//MesquiteString s = getNameRule(i);
			//if (s !=null)
			sequenceProfileVector.removeElement(specification, false);  //deletes it from the vector
			choice.remove(i);
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Sequence Profile for GenBank";
	}

}	



/*=======================================================================*/
class ManageGeneProfileDLOG extends ExtensibleListDialog {
	SequenceProfileForGenBank ownerModule;
	boolean editLastItem = false;

	public ManageGeneProfileDLOG (SequenceProfileForGenBank ownerModule, String nameParsingRulesName, MesquiteInteger buttonPressed){
		super(ownerModule.containerOfModule(), "Sequence Profile Manager", "Sequence Profile", buttonPressed, ownerModule.sequenceProfileVector);
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
		return "Sequence Profile";
	}
	/*.................................................................................................................*/
	/** this is the name of the class of objects */
	public  String pluralObjectName(){
		return "Sequence Profiles";
	}

	/*.................................................................................................................*/
	public Listable createNewElement(String name, MesquiteBoolean success){
		hide();
		SequenceProfile sequenceSpecification = new SequenceProfile();
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
		SequenceProfile sequenceSpecification = ownerModule.duplicateNameRule((SequenceProfile)currentElement, name);
		return sequenceSpecification;
	}
	/*.................................................................................................................*/
	public boolean getEditable(int item){
		return true;
	}
	/*.................................................................................................................*/
	public void editElement(int item){
		//hide();
		SequenceProfile sequenceSpecification = ((SequenceProfile)ownerModule.sequenceProfileVector.elementAt(item));
		if (sequenceSpecification.queryOptions(sequenceSpecification.getName()))
			sequenceSpecification.save();
		setVisible(true);
	}




}

