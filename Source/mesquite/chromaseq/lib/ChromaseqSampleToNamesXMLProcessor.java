package mesquite.chromaseq.lib;

import java.awt.Choice;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.StringUtil;
import mesquite.lib.XMLUtil;
import mesquite.lib.ui.ExtensibleDialog;

public class ChromaseqSampleToNamesXMLProcessor {
	Document namesDoc = null;
	String sampleCodeList = "";
	String sampleCodeListPath = null;
	String[] nameCategoryDescriptions;
	String[] nameCategoryTags;
	String chosenNameCategoryTag;
	MesquiteModule ownerModule;

	static final String FILEANDFOLDERNAMETAG = "fileAndFolderName";

	public ChromaseqSampleToNamesXMLProcessor(){
	}
	public ChromaseqSampleToNamesXMLProcessor(MesquiteModule ownerModule, String sampleCodeListPath){
		this.sampleCodeListPath = sampleCodeListPath;
		if (StringUtil.notEmpty(sampleCodeListPath)){
			readXMLDocument();
			if (isValid())
				processNameCategories();
		}
		this.ownerModule = ownerModule;
	}
	/*.................................................................................................................*/
	public boolean readXMLDocument() {
		if (!StringUtil.blank(sampleCodeListPath)) {
			sampleCodeList = MesquiteFile.getFileContentsAsString(sampleCodeListPath);

			if (!StringUtil.blank(sampleCodeList)) {
				namesDoc = XMLUtil.getDocumentFromString("mesquite", sampleCodeList);
				return namesDoc!=null;
			}
		}	
		return false;
		
	}
	/*.................................................................................................................*/
	public boolean isValid() {
		return  namesDoc!=null;
	}


	/*.................................................................................................................*/
	public boolean optionsSpecified(){
		return (namesDoc!=null && nameCategoryTags!=null);
	}


	/*.................................................................................................................*/
	public String getCategoryTag(int index) {
		if (nameCategoryTags!=null && index>=0 && index<nameCategoryTags.length) 
			return nameCategoryTags[index];
		return null;
	}
	/*.................................................................................................................*/
	public String getChosenTag() {
		return chosenNameCategoryTag;
	}
	/*.................................................................................................................*/
	public void setChosenTag(int tagNumber) {
		if (nameCategoryTags!=null && tagNumber>=0 && tagNumber<nameCategoryTags.length)
			chosenNameCategoryTag = nameCategoryTags[tagNumber];
	}
	/*.................................................................................................................*/
	public void setChosenTag(String tag) {
			chosenNameCategoryTag = tag;
	}

	/*.................................................................................................................*/
	public int getChosenTagNumber() {
		return getTagNumber(chosenNameCategoryTag);
	}
	/*.................................................................................................................*/
	public String[] getNameCategoryDescriptions() {
		return nameCategoryDescriptions;
	}

	/*.................................................................................................................*/
	// not used by all objects that use this object
	public boolean queryOptions(MesquiteModule ownerModule) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(), "Name category to use for sequence names",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		int tagNumber = getTagNumber(chosenNameCategoryTag);
		Choice categoryChoice = dialog.addPopUpMenu("", nameCategoryDescriptions, tagNumber);

		dialog.completeAndShowDialog(true);
		boolean success=(buttonPressed.getValue()== dialog.defaultOK);
		if (success)  {
			int chosen = categoryChoice.getSelectedIndex();
			chosenNameCategoryTag = getCategoryTag(chosen);
		}
		dialog.dispose();
		return success;
	}


	/*.................................................................................................................*/
	public int getTagNumber(String tag) {
		if (nameCategoryTags!=null){
			for (int i=0; i<nameCategoryTags.length; i++)
				if (nameCategoryTags[i].equalsIgnoreCase(tag))
				return i;
		}
			return 0;
	}

	/*.................................................................................................................*/

	public void processNameCategories() {
		String elementName = "nameCategory";
		String tagAttribute = "tag";
		String descriptionAttribute = "description";
		Element namesElement = namesDoc.getRootElement().element("names");
		List categoryElements = namesElement.elements(elementName);
		int count=0;
		for (Iterator iter = categoryElements.iterator(); iter.hasNext();) {
			Element nextSampleCodeElement = (Element) iter.next();
			String description = nextSampleCodeElement.attributeValue(descriptionAttribute);
			if (!StringUtil.blank(description)) {
				count++;
			}
		}
		nameCategoryDescriptions = new String[count];
		nameCategoryTags = new String[count];
		count=0;
		for (Iterator iter = categoryElements.iterator(); iter.hasNext();) {
			Element nextSampleCodeElement = (Element) iter.next();
			String description = nextSampleCodeElement.attributeValue(descriptionAttribute);
			String tag = nextSampleCodeElement.attributeValue(tagAttribute);
			if (!StringUtil.blank(tag)&& count<nameCategoryTags.length) {
				nameCategoryTags[count] = tag;
			}
			if (!StringUtil.blank(description) && count<nameCategoryDescriptions.length) {
				nameCategoryDescriptions[count] = description;
				count++;
			}
		}
	}

	/*.................................................................................................................*/

	public  String[] getSeqNamesFromXml(MesquiteString sampleCode, String fileNameTag, String nameTag) {
		String extractionElementName = "extraction";
		String sampleCodeAttribute = "number";
/*		String fileAndFolderNameElement = "fileAndFolderName";
		String standardNameNameElement = "standardName";
		String officialNameNoNumberNoLocalityElement = "officialNameNoNumberNoLocality";
		String officialNameNoLocalityElement = "officialNameNoLocality";
		String officialNameWithLocElement = "officialNameWithLoc";
*/
		String sampleCodeString  = sampleCode.getValue();
		Element namesElement = namesDoc.getRootElement().element("names");
		List extractionElements = namesElement.elements(extractionElementName);
		for (Iterator iter = extractionElements.iterator(); iter.hasNext();) {
			Element nextSampleCodeElement = (Element) iter.next();
			String number = nextSampleCodeElement.attributeValue(sampleCodeAttribute);
			if (!StringUtil.blank(number)) {
				if (sampleCodeString.equals(number)) {
					// have a match
					String fileName = nextSampleCodeElement.elementText(fileNameTag);
					String translationName = fileName;
					if (nextSampleCodeElement.element(nameTag) != null) {
						translationName = nextSampleCodeElement.elementText(nameTag);
					}
					return new String[]{fileName, translationName};
				}
			}
		}
		// got here and no match found -- log an error
		MesquiteMessage.warnUser("No sample code named '" + sampleCode + "' found in sample code xml file.");
		return new String[]{"", ""};
	}

	public String getParameters() {
		if (StringUtil.notEmpty(chosenNameCategoryTag))
			return "Names Category: " + nameCategoryDescriptions[getTagNumber(chosenNameCategoryTag)];
		return "";
	}

}
