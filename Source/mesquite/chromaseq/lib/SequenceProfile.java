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

import java.awt.*;
import java.util.regex.*;

import org.dom4j.*;

import mesquite.lib.*;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

public class SequenceProfile implements Listable, Explainable {

	public String location = "genome";   
	public String moltype = "DNA";  
	public String description = "";  
	public String productName = "";  
	public String seqIDSuffix = "";  
	public String note = "";  
	//public boolean CDS = true;  
	int gcode= 1;

	public String path;

	public String name = "default";
	public String explanation;

	public SequenceProfile() {
	}

	public SequenceProfile(SequenceProfile spec) {
		if (spec!=null) {
			description = spec.description;
			location = spec.location;
			moltype = spec.moltype;
			productName = spec.productName;
			seqIDSuffix = spec.seqIDSuffix;
			note = spec.note;
			gcode = spec.gcode;
			name = spec.name;
//			CDS = spec.CDS;
		}
	}

	public void setPath(String path){
		this.path = path;
	}
	public void setNote(String note){
		this.note = note;
	}
	public String getNote(){
		return note;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
	public String getProductName(){
		return productName;
	}
	public String getDescription(){
		return description;
	}
/*	public boolean isCDS(){
		return CDS;
	}
	*/
	public String getLocation(){
		return location;
	}
	public int getTranslationTable(){
		return gcode;
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
		Element sequenceProfileElement = DocumentHelper.createElement("sequenceProfile");
		mesquiteElement.add(sequenceProfileElement);
		XMLUtil.addFilledElement(sequenceProfileElement, "version","1");
		Element boundedByTokensElement = DocumentHelper.createElement("boundedByTokens");
		sequenceProfileElement.add(boundedByTokensElement);
		XMLUtil.addFilledElement(boundedByTokensElement, "name",name);
		XMLUtil.addFilledElement(boundedByTokensElement, "description",DocumentHelper.createCDATA(description));
		XMLUtil.addFilledElement(boundedByTokensElement, "location",DocumentHelper.createCDATA(location));
		XMLUtil.addFilledElement(boundedByTokensElement, "moltype",DocumentHelper.createCDATA(moltype));
		XMLUtil.addFilledElement(boundedByTokensElement, "productName",DocumentHelper.createCDATA(productName));
		XMLUtil.addFilledElement(boundedByTokensElement, "seqIDSuffix",DocumentHelper.createCDATA(seqIDSuffix));
		XMLUtil.addFilledElement(boundedByTokensElement, "gcode",DocumentHelper.createCDATA(""+gcode));
		XMLUtil.addFilledElement(boundedByTokensElement, "note",DocumentHelper.createCDATA(""+note));
//		XMLUtil.addFilledElement(boundedByTokensElement, "CDS",DocumentHelper.createCDATA(MesquiteBoolean.toTrueFalseString(CDS)));
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

		Element sequenceProfileElement = root.element("sequenceProfile");
		if (sequenceProfileElement != null) {
			Element versionElement = sequenceProfileElement.element("version");
			if (versionElement == null || !versionElement.getText().equals("1")) {
				return false;
			}
			Element boundedByTokens = sequenceProfileElement.element("boundedByTokens");
			if (boundedByTokens == null) {
				return false;
			}
			name = boundedByTokens.elementText("name");
			description = boundedByTokens.elementText("description");
			location = boundedByTokens.elementText("location");
			moltype = boundedByTokens.elementText("moltype");
			productName = boundedByTokens.elementText("productName");
			seqIDSuffix = boundedByTokens.elementText("seqIDSuffix");
			note = boundedByTokens.elementText("note");
			gcode = MesquiteInteger.fromString(boundedByTokens.elementText("gcode"));			
//			CDS = MesquiteBoolean.fromTrueFalseString(boundedByTokens.elementText("CDS"));			
			//translateSampleCodes = MesquiteBoolean.fromTrueFalseString(boundedByTokens.elementTextTrim("translateSampleCodes"));
		} else {
			return false;
		}
		return true;
	}

	/*.................................................................................................................*/
	public  String getFASTASourceModifiers() {
		StringBuffer sb = new StringBuffer();
	//	sb.append("[productName = " + productName + "] ");
	//	sb.append("[description = " + description + "] ");
		sb.append("[location = " + location + "] ");
		if (location.equalsIgnoreCase("Mitochondrion"))
			sb.append("[mgcode = " + gcode + "] ");
		else
			sb.append("[gcode = " + gcode + "] ");
		sb.append("[moltype = " + moltype + "] ");
		if (StringUtil.notEmpty(note))
			sb.append("[note = " + note + "] ");
		return sb.toString();
	}
	/*.................................................................................................................*/
	public  String getSeqIDSuffix() {
		return seqIDSuffix;
	}

	/*.................................................................................................................*
	public String processTokenAfterRead(String s) {
		if ("\\ ".equals(s))
			return " ";
		else if (StringUtil.blank(s))
			return "";
		else
			return s;
	}
	/*.................................................................................................................*/
	public String[] moltypeStrings() {   // from http://www.insdc.org/controlled-vocabulary-moltype-qualifier
		return new String[] {
				"genomic DNA", 
				"genomic RNA", 
				"mRNA", 
				"tRNA", 
				"rRNA", 
				"other RNA", 
				"other DNA", 
				"transcribed RNA", 
				"viral cRNA", 
				"unassigned DNA", 
				"unassigned RNA" 
		};
	}
	/*.................................................................................................................*/
	public String[] organelleStrings() {   // from http://www.insdc.org/controlled-vocabulary-organelle-qualifier
		return new String[] {
				"chromatophore",
				"hydrogenosome",
				"mitochondrion",
				"nucleomorph",
				"plastid",
				"mitochondrion:kinetoplast",
				"plastid:chloroplast",
				"plastid:apicoplast",
				"plastid:chromoplast",
				"plastid:cyanelle",
				"plastid:leucoplast",
				"plastid:proplastid"
		};
	}
	/*.................................................................................................................*/
	public String[] locationStrings() { // from https://www.ncbi.nlm.nih.gov/projects/Sequin/sequin.hlp.html#Location
		return new String[] {
				"Genomic", 
				"Mitochondrion", 
				"Chloroplast", 
				"Apicoplast", 
				"Chromatophore", 
				"Chromoplast", 
				"Cyanelle", 
				"Endogenous_virus", 
				"Extrachromosomal", 
				"Hydrogenosome", 
				"Kinetoplast", 
				"Leucoplast", 
				"Macronuclear", 
				"Nucleomorph", 
				"Plasmid", 
				"Plastid", 
				"Proplastid", 
				"Proviral"
		};
	}
	/*.................................................................................................................*/
	public String[] molecularStrings() {
		return new String[] {
				"DNA", 
				"RNA"
		};
	}

	/*.................................................................................................................*/
	public String[] gCodeStrings() {
		return new String[] {
				"The Standard Code", 
				"The Vertebrate Mitochondrial Code", 
				"The Yeast Mitochondrial Code", 
				"The Mold, Protozoan, and Coelenterate Mitochondrial Code and the Mycoplasma/Spiroplasma Code", 
				"The Invertebrate Mitochondrial Code", 
				"The Ciliate, Dasycladacean and Hexamita Nuclear Code", 
				"The Echinoderm and Flatworm Mitochondrial Code", 
				"The Euplotid Nuclear Code", 
				"The Bacterial, Archaeal and Plant Plastid Code", 
				"The Alternative Yeast Nuclear Code", 
				"The Ascidian Mitochondrial Code", 
				"The Alternative Flatworm Mitochondrial Code", 
				"Chlorophycean Mitochondrial Code", 
				"Trematode Mitochondrial Code", 
				"Scenedesmus obliquus Mitochondrial Code", 
				"Thraustochytrium Mitochondrial Code", 
				"Pterobranchia Mitochondrial Code", 
				"Candidate Division SR1 and Gracilibacteria Code", 
				"Pachysolen tannophilus Nuclear Code", 
				"Karyorelict Nuclear", 
				"Condylostoma Nuclear", 
				"Mesodinium Nuclear", 
				"Peritrich Nuclear", 
				"Blastocrithidia Nuclear"
		};
	}

	/*.................................................................................................................*/
	public boolean queryOptions(String name) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Sequence Profile",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		if (!StringUtil.blank(name))
			dialog.addLabel("Sequence Profile: "+name);
		else
			dialog.addLabel("Sequence Profile");

		SingleLineTextField descriptionField = dialog.addTextField("Description of sequence*:", description,80, true);
		SingleLineTextField productNameField = dialog.addTextField("Product name:", productName, 80, true);
		int item = StringArray.indexOfIgnoreCase(locationStrings(), location);
		if (item<0) item=0;
		Choice locationChoice = dialog.addPopUpMenu("Location*", locationStrings(), 	item);
		item = StringArray.indexOfIgnoreCase(moltypeStrings(), moltype);
		Choice geneticCodeChoice = dialog.addPopUpMenu("Genetic Code*", gCodeStrings(), gcode-1);
		if (item<0) item=0;
		Choice moltypeChoice = dialog.addPopUpMenu("Molecular type*", moltypeStrings(), 	item);
		SingleLineTextField noteField = dialog.addTextField("Note:", note,80, true);
		SingleLineTextField seqIDSuffixField = dialog.addTextField("SeqID suffix*:", seqIDSuffix,30, true);
		//Checkbox CDSbox = dialog.addCheckBox("CDS", CDS);

		dialog.addHorizontalLine(2);


		//		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);


		String s = "For each gene, gene fragment, or type of sequence data, this allows you to create a profile for that gene; the information in this profile "
				+ "is then included in your submission to GenBank .\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			description = descriptionField.getText();
			location = locationChoice.getSelectedItem();
			moltype = moltypeChoice.getSelectedItem();
			productName = productNameField.getText();
			seqIDSuffix = seqIDSuffixField.getText();
			note = noteField.getText();
			gcode = geneticCodeChoice.getSelectedIndex()+1;
//			CDS =CDSbox.getState();
			//			translateSampleCodes = translateCodesBox.getState();
		}
		//storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public String getDefinitions (MesquiteModule ownerModule){
		return "";

	}


}
