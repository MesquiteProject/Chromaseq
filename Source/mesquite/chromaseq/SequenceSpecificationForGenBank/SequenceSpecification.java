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

import java.awt.*;
import java.util.regex.*;

import org.dom4j.*;

import mesquite.lib.*;

public class SequenceSpecification implements Listable, Explainable {

	public String location = "genome";   
	public String moltype = "DNA";  
	public String description = "";  
	public String productName = "";  
	public String seqIDSuffix = "";  
	int gcode= 1;

	public String path;

	public String name = "default";
	public String explanation;

	public SequenceSpecification() {
	}

	public SequenceSpecification(SequenceSpecification spec) {
		if (spec!=null) {
			description = spec.description;
			location = spec.location;
			moltype = spec.moltype;
			productName = spec.productName;
			seqIDSuffix = spec.seqIDSuffix;
			gcode = spec.gcode;
			name = spec.name;
		}
	}

	public void setPath(String path){
		this.path = path;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
	public String getDescription(){
		return description;
	}
	public String getLocation(){
		return location;
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
		Element sequenceSpecificationElement = DocumentHelper.createElement("sequenceSpecification");
		mesquiteElement.add(sequenceSpecificationElement);
		XMLUtil.addFilledElement(sequenceSpecificationElement, "version","1");
		Element boundedByTokensElement = DocumentHelper.createElement("boundedByTokens");
		sequenceSpecificationElement.add(boundedByTokensElement);
		XMLUtil.addFilledElement(boundedByTokensElement, "name",name);
		XMLUtil.addFilledElement(boundedByTokensElement, "description",DocumentHelper.createCDATA(description));
		XMLUtil.addFilledElement(boundedByTokensElement, "location",DocumentHelper.createCDATA(location));
		XMLUtil.addFilledElement(boundedByTokensElement, "moltype",DocumentHelper.createCDATA(moltype));
		XMLUtil.addFilledElement(boundedByTokensElement, "productName",DocumentHelper.createCDATA(productName));
		XMLUtil.addFilledElement(boundedByTokensElement, "seqIDSuffix",DocumentHelper.createCDATA(seqIDSuffix));
		XMLUtil.addFilledElement(boundedByTokensElement, "gcode",DocumentHelper.createCDATA(""+gcode));
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

		Element sequenceSpecificationElement = root.element("sequenceSpecification");
		if (sequenceSpecificationElement != null) {
			Element versionElement = sequenceSpecificationElement.element("version");
			if (versionElement == null || !versionElement.getText().equals("1")) {
				return false;
			}
			Element boundedByTokens = sequenceSpecificationElement.element("boundedByTokens");
			if (boundedByTokens == null) {
				return false;
			}
			name = boundedByTokens.elementText("name");
			description = boundedByTokens.elementText("description");
			location = boundedByTokens.elementText("location");
			moltype = boundedByTokens.elementText("moltype");
			productName = boundedByTokens.elementText("productName");
			seqIDSuffix = boundedByTokens.elementText("seqIDSuffix");
			gcode = MesquiteInteger.fromString(boundedByTokens.elementText("gcode"));			
			//primerListPath = boundedByTokens.elementTextTrim("primerListPath");
			//dnaNumberListPath = boundedByTokens.elementTextTrim("dnaNumberListPath");
			//translateSampleCodes = MesquiteBoolean.fromTrueFalseString(boundedByTokens.elementTextTrim("translateSampleCodes"));
		} else {
			return false;
		}
		return true;
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
	public String[] moltypeStrings() {
		return new String[] {
				"genomic", 
				"precursor RNA", 
				"mRNA", 
				"rRNA", 
				"tRNA", 
				"snRNA", 
				"scRNA", 
				"other-genetic", 
				"cRNA", 
				"snoRNA", 
				"transcribed RNA"
		};
	}
	/*.................................................................................................................*/
	public String[] locationStrings() {
		return new String[] {
				"genomic", 
				"chloroplast", 
				"kinetoplast", 
				"mitochondrion", 
				"plastid", 
				"macronuclear", 
				"extrachromosomal", 
				"plasmid", 
				"cyanelle", 
				"proviral", 
				"virion", 
				"nucleomorph", 
				"apicoplast", 
				"leucoplast", 
				"proplastid", 
				"endogenous-virus", 
				"hydrogenosome"
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
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Gene Fragment Specification",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		if (!StringUtil.blank(name))
			dialog.addLabel("Gene Fragment Specification: "+name);
		else
			dialog.addLabel("Gene Fragment Specification");

		SingleLineTextField descriptionField = dialog.addTextField("description of sequence:", description,80, true);
		int item = StringArray.indexOfIgnoreCase(locationStrings(), location);
		if (item<0) item=0;
		Choice locationChoice = dialog.addPopUpMenu("Location", locationStrings(), 	item);
		item = StringArray.indexOfIgnoreCase(moltypeStrings(), moltype);
		Choice geneticCodeChoice = dialog.addPopUpMenu("Genetic Code", gCodeStrings(), gcode-1);
		if (item<0) item=0;
		Choice moltypeChoice = dialog.addPopUpMenu("Molecular type", moltypeStrings(), 	item);
		SingleLineTextField productNameField = dialog.addTextField("Product name:", productName, 80, true);
		SingleLineTextField seqIDSuffixField = dialog.addTextField("SeqID suffix:", seqIDSuffix,30, true);

		dialog.addHorizontalLine(2);


		//		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);


		String s = "For each gene, gene fragment, or type of sequence data, this allows you to create a profile for that gene; the information in this specification "
				+ "is then included in your submission to GenBank .\n";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			description = descriptionField.getText();
			location = locationChoice.getSelectedItem();
			moltype = moltypeChoice.getSelectedItem();
			productName = productNameField.getText();
			seqIDSuffix = seqIDSuffixField.getText();
			gcode = geneticCodeChoice.getSelectedIndex()+1;
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
