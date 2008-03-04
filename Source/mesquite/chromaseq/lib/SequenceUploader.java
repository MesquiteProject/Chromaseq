package mesquite.chromaseq.lib;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.StringUtil;
import mesquite.lib.MesquiteXMLUtilities;
import mesquite.molec.lib.DNADatabaseURLSource;
//import mesquite.BTOL.lib.*;


import org.dom4j.*;

import mesquite.tol.lib.*;


public class SequenceUploader {
	DNADatabaseURLSource databaseURLSource;
	
	public SequenceUploader(DNADatabaseURLSource databaseURLSource) {
		this.databaseURLSource = databaseURLSource;
	}
	
	public Long createAB1BatchOnServer(String databaseURL, String name, String description, String contributorId) {
		Hashtable stringArgs = new Hashtable();
		stringArgs.put(ToLRequestParameters.NAME, name);
		stringArgs.put(ToLRequestParameters.DESCRIPTION, description);
		stringArgs.put(ToLRequestParameters.CONTRIBUTOR_ID, contributorId);

		Document responseDoc = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURLSource.getBaseURL(), databaseURLSource.getPage(DNADatabaseURLSource.CHROMATOGRAM_BATCH_CREATION_SERVICE), stringArgs, true);
		//Document responseDoc = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURL,batchCreationPageName, stringArgs, true);
		if (responseDoc == null) {
			MesquiteMessage.warnUser("Cannot create abi upload batch on the server.  Upload will not proceed");
			return null;
		} else {
			Element rootElement = responseDoc.getRootElement();
			String batchIdString = rootElement.attributeValue(XMLConstants.ID);
			return new Long(batchIdString);
		}
	}
	
	public void uploadAB1ToServer(String databaseURL, String sampleCode, String filename, File abiFile, Long batchId) {
		if (databaseURLSource==null)
			return;
		if (!abiFile.exists()) {
			MesquiteMessage.warnUser("File: " + abiFile + " doesn't exist.");
		}
		Hashtable stringArgs = new Hashtable();
		stringArgs.put(ToLRequestParameters.BATCH_ID, batchId);
		stringArgs.put(databaseURLSource.getKeyString(DNADatabaseURLSource.SAMPLE_CODE), sampleCode);
		// optional filename arg if we want the file to be named something
		// different on the server
		String filenameArg = filename;
		if (filename == null) {
			filenameArg = abiFile.getName();
		}
		stringArgs.put(ToLRequestParameters.FILENAME, filenameArg);
		stringArgs.put("service", "external");  //BTOL ONLY!!!!

		Hashtable fileArgs = new Hashtable();
		fileArgs.put(ToLRequestParameters.FILE, abiFile);
		
		Document doc = MesquiteXMLUtilities.getDocumentFromTapestryPageNameMultipart(databaseURLSource.getBaseURL(), databaseURLSource.getPage(DNADatabaseURLSource.SEQUENCE_UPLOAD_SERVICE), stringArgs, fileArgs);

		//Document doc = MesquiteXMLUtilities.getDocumentFromTapestryPageNameMultipart(databaseURL, abiUploadPageName, stringArgs, fileArgs);
		if (MesquiteXMLUtilities.getIsError(doc)) {
			if (doc == null) {
				MesquiteMessage.warnUser("Problems uploading abi file: " + filenameArg + " to server.");
			} else {
				if (doc.getRootElement().getName().equals(XMLConstants.ERROR)) {
					String errorNum = doc.getRootElement().attributeValue(XMLConstants.ERRORNUM); 
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
	
	public void uploadAceFileToServer(String databaseURL, AceFile ace, boolean processPolymorphisms, int qualThresholdForTrim) {
		for (int i = 0; i < ace.getNumContigs(); i++) {
			Contig nextContig = ace.getContig(i);
			String name = ace.getContigNameForFASTAFile(i);
			String fastaString = nextContig.toFASTAString(processPolymorphisms, qualThresholdForTrim, name);
			String commaSeparatedFilenames = "";
			int numReads = nextContig.getNumReadsInContig();
			for (int j = 0; j < numReads; j++) {
				Read read = nextContig.getRead(j);
				commaSeparatedFilenames += read.getOriginalName();
				if (j < numReads - 1) {
					commaSeparatedFilenames += ",";
				}
			}
			Hashtable args = new Hashtable();
			args.put(databaseURLSource.getKeyString(DNADatabaseURLSource.FASTA), fastaString);
			args.put(ToLRequestParameters.FILENAME, commaSeparatedFilenames);
			args.put(ToLRequestParameters.CONTRIBUTOR_ID, MesquiteModule.author.getCode());

			Document doc = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURLSource.getBaseURL(), databaseURLSource.getPage(DNADatabaseURLSource.FASTA_UPLOAD_SERVICE), args, true);
			//Document doc = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURL, fasUploadPageName, args, true);
			if (MesquiteXMLUtilities.getIsError(doc)) {
				MesquiteMessage.warnProgrammer("Unable to upload fasta file for chromatograms : " + commaSeparatedFilenames);
			} else {
				MesquiteMessage.warnProgrammer("Successfully uploaded fasta file for chromatograms : " + commaSeparatedFilenames);
			}
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
