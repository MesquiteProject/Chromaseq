package mesquite.chromaseq.lib;

import java.io.File;
import java.util.Hashtable;

import mesquite.lib.MesquiteMessage;
import mesquite.lib.StringUtil;

import org.jdom.Document;
import org.jdom.Element;
import org.tolweb.treegrow.main.RequestParameters;
import org.tolweb.treegrow.main.XMLConstants;

public class SequenceUploader {
	private String abiUploadPageName = "btolxml/SequenceUploadService";
	private String batchCreationPageName = "btolxml/AbiBatchCreationService"; 
	
	public Long createAB1BatchOnServer(String name, String description) {
		Hashtable stringArgs = new Hashtable();
		stringArgs.put(RequestParameters.NAME, name);
		stringArgs.put(RequestParameters.DESCRIPTION, description);
		Document responseDoc = XMLUtilities.getDocumentFromTapestryPageNameMultipart(batchCreationPageName, stringArgs, new Hashtable());
		if (responseDoc == null) {
			MesquiteMessage.warnUser("Cannot create abi upload batch on the server.  Upload will not proceed");
			return null;
		} else {
			Element rootElement = responseDoc.getRootElement();
			String batchIdString = rootElement.getAttributeValue(XMLConstants.ID);
			return new Long(batchIdString);
		}
	}
	
	public void uploadAB1ToServer(String sampleCode, String filename, File abiFile, Long batchId) {
		if (!abiFile.exists()) {
			MesquiteMessage.warnUser("File: " + abiFile + " doesn't exist.");
		}
		Hashtable stringArgs = new Hashtable();
		stringArgs.put(RequestParameters.BATCH_ID, batchId);
		stringArgs.put(RequestParameters.CODE, sampleCode);
		// optional filename arg if we want the file to be named something
		// different on the server
		String filenameArg = filename;
		if (filename == null) {
			filenameArg = abiFile.getName();
		}
		stringArgs.put(RequestParameters.FILENAME, filenameArg);
		Hashtable fileArgs = new Hashtable();
		fileArgs.put(RequestParameters.FILE, abiFile);
		Document doc = XMLUtilities.getDocumentFromTapestryPageNameMultipart(abiUploadPageName, 
				stringArgs, fileArgs);
		if (doc == null || doc.getRootElement().getName().equals(XMLConstants.ERROR)) {
			if (doc == null) {
				MesquiteMessage.warnUser("Problems uploading abi file: " + filenameArg + " to server.");
			} else {
				if (doc.getRootElement().getName().equals(XMLConstants.ERROR)) {
					String errorNum = doc.getRootElement().getAttributeValue(XMLConstants.ERRORNUM); 
					if (!StringUtil.blank(errorNum)) {
						if (errorNum.equals("404")) {
							MesquiteMessage.warnUser("Abi file: " + filenameArg + " not uploaded to server because PCR Reaction #" + sampleCode + " not found.");
						}
					}
				}
			}
		} else {
			MesquiteMessage.warnProgrammer("Successfully uploaded abi file : " + filenameArg + " to server.");
		}
	}
	
	/*public static void main(String[] args) {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");	
		SequenceUploader uploader = new SequenceUploader();
		uploader.uploadAB1ToServer("BP1560", "BP1560.ab1", new File("/home/dmandel/btolchromatograms/BTOL001/Chromatograms.original/A01_A01BP1560_CD439F_538892.ab1"));
	}*/
}
