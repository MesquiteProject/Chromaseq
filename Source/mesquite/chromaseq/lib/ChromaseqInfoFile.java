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

import java.util.Vector;

import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.StringUtil;
import mesquite.lib.XMLUtil;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class ChromaseqInfoFile {

	static final int infoFileVersion = 2;
	
	/*
	 * file version changes:  with version 2, added formal SequenceNameSource and PrimerInfoSource module info
	 */

	Document doc = null;
	Element rootElement = null;
	Element chromaseqElement = null;
	Element sampleElement;
	Element processedFolderElement= null;
	String filePath = null;
	String directoryPath = null;

	public ChromaseqInfoFile() {
		rootElement = DocumentHelper.createElement("mesquite");
		chromaseqElement = rootElement.addElement("chromaseq");
		doc = DocumentHelper.createDocument(rootElement);
		chromaseqElement.addAttribute("version", ""+infoFileVersion);
		processedFolderElement = chromaseqElement.addElement("processedFolder");
	}

	public void dispose () {
		rootElement = null;
		doc = null;
		chromaseqElement = null;
		processedFolderElement= null;
		sampleElement=null;
	}

	/*.................................................................................................................*/
	public void setFilePath(String path) {
		this.filePath = path;
	}
	public void setDirectoryPath(String path) {
		this.directoryPath = path;
	}
	public String getDirectoryPath() {
		return directoryPath;
	}
	/*.................................................................................................................*/
	public static ChromaseqInfoFile getInfoFile(Vector infoFiles, String directoryPath) {
		if (infoFiles==null || StringUtil.blank(directoryPath))
				return null;
		for (int i=0; i<infoFiles.size(); i++) {
			ChromaseqInfoFile infoFile = (ChromaseqInfoFile) infoFiles.get(i);
			if (directoryPath.equals(infoFile.getDirectoryPath()))
					return infoFile;
		}
		return null;
	}
	/*.................................................................................................................*/
	public static void writeInfoFiles(Vector infoFiles) {
		if (infoFiles==null)
				return;
		for (int i=0; i<infoFiles.size(); i++) {
			ChromaseqInfoFile infoFile = (ChromaseqInfoFile) infoFiles.get(i);
			if (infoFile!=null)
				infoFile.write();
		}
	}
	/*.................................................................................................................*/
	public static void dispose(Vector infoFiles) {
		if (infoFiles==null)
				return;
		for (int i=0; i<infoFiles.size(); i++) {
			ChromaseqInfoFile infoFile = (ChromaseqInfoFile) infoFiles.get(i);
			infoFile.dispose();
		}
		infoFiles.removeAllElements();
	}
	/*.................................................................................................................*/
	public  boolean write(){
		if (StringUtil.blank(filePath))
			return false;
		String xml = XMLUtil.getDocumentAsXMLString(doc, false);
		if (!StringUtil.blank(xml))
			MesquiteFile.putFileContents(filePath, xml, true);
		return true;
	}
	/*.................................................................................................................*/
	public  Element addSample(String fullName, String voucherCode){
		sampleElement = processedFolderElement.addElement("sample");
		sampleElement.addAttribute("fullName", fullName);
		sampleElement.addAttribute("voucherCode", voucherCode);
		return sampleElement;
	}
	/*.................................................................................................................*/
	public  void addPhredPhrapOptions(String phredOptions, String phrapOptions){
		XMLUtil.addFilledElement(processedFolderElement, "phredOptions", phredOptions);
		XMLUtil.addFilledElement(processedFolderElement, "phrapOptions", phrapOptions);
	}
	/*.................................................................................................................*/
	public  void addPrimerInfoSource(PrimerInfoSource primerInfoSource){
		Element element = processedFolderElement.addElement("primerInfoSource");
		element.addAttribute("module", ""+primerInfoSource.getClassName());
		primerInfoSource.addXMLAttributes(element);
	}
	/*.................................................................................................................*/
	public  void addSequenceNameSource(SequenceNameSource sequenceNameSource){
		Element element = processedFolderElement.addElement("sequenceNameSource");
		element.addAttribute("module", ""+sequenceNameSource.getClassName());
		sequenceNameSource.addXMLAttributes(element);
}
	/*.................................................................................................................*
	public  void addSampleCodeFile(String sampleCodeFilePath){
		XMLUtil.addFilledElement(processedFolderElement, "sampleCodeTranslationFilePath", sampleCodeFilePath);
	}
	/*.................................................................................................................*
	public  void addPrimerFile(String primerFile){
		XMLUtil.addFilledElement(processedFolderElement, "primerInformationFilePath", primerFile);
	}
	/*.................................................................................................................*/
	public  Element getProcessedFolderElement(){
		return processedFolderElement;
	}
	/*.................................................................................................................*/
	public  void addChromaseqProcessingOptions(int qualThresholdForLowerCase, boolean processPolymorphisms, double polyThreshold, boolean truncateMixedEnds, int qualThresholdForTrim, int mixedEndWindow, int mixedEndThreshold){
		Element chromaseqProcessingOptionsElement = processedFolderElement.addElement("chromaseqProcessingOptions");
		chromaseqProcessingOptionsElement.addAttribute("ambiguityThreshold", ""+polyThreshold);
		chromaseqProcessingOptionsElement.addAttribute("convertMultiPeaksToAmbiguity", ""+MesquiteBoolean.toTrueFalseString(processPolymorphisms));
		chromaseqProcessingOptionsElement.addAttribute("truncateMixedEnds", ""+MesquiteBoolean.toTrueFalseString(truncateMixedEnds));
		chromaseqProcessingOptionsElement.addAttribute("qualityThresholdForTrim", ""+qualThresholdForTrim);
		chromaseqProcessingOptionsElement.addAttribute("trimWindowLength", ""+mixedEndWindow);
		chromaseqProcessingOptionsElement.addAttribute("trimWindowThreshold", ""+mixedEndThreshold);
		chromaseqProcessingOptionsElement.addAttribute("lowerCaseQualityThreshold", ""+qualThresholdForLowerCase);
	}

	/*.................................................................................................................*/
	public void addChromatogramInfo(String originalChromFileName, String newChromFileName, String primerName){
		if (sampleElement==null)
			return;
		Element chromatogramElement = sampleElement.addElement("chromatogram");
		chromatogramElement.addAttribute("originalName", originalChromFileName);
		chromatogramElement.addAttribute("newName", newChromFileName);
		chromatogramElement.addAttribute("primerName", primerName);
	}

	/*.................................................................................................................*/
	public static void processInfoFile(String infoFilePath, MesquiteString fullName, MesquiteString voucherCode){
		String s = MesquiteFile.getFileContentsAsString(infoFilePath); 
		Document doc = XMLUtil.getDocumentFromString(s);
		if (doc == null) {  // must be old format
			fullName.setValue(s);
		} else {
			Element rootElement = doc.getRootElement();
			Element chromaseqElement = rootElement.element("chromaseq");
			if (chromaseqElement != null) {
				String versionString = chromaseqElement.attributeValue("version");
				int versionInXml = MesquiteInteger.fromString(versionString);
				if (versionInXml==infoFileVersion) {
					Element processedFolderElement = chromaseqElement.element("processedFolder");
					Element sampleElement = processedFolderElement.element("sample");
					String name = sampleElement.attributeValue("fullName");
					if (fullName!=null) fullName.setValue(name);
					String voucher = sampleElement.attributeValue("voucherCode");
					if (voucherCode!=null) voucherCode.setValue(voucher);
				}

			}
		}

	}

}
