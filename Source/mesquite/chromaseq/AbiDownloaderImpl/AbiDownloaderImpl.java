package mesquite.chromaseq.AbiDownloaderImpl;

import java.awt.Label;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.tolweb.base.http.BaseHttpRequestMaker;
import org.tolweb.treegrow.main.RequestParameters;
import org.tolweb.treegrow.main.StringUtils;
import org.tolweb.treegrow.main.XMLConstants;

import mesquite.chromaseq.PhredPhrap.PhredPhrap;
import mesquite.chromaseq.lib.AbiDownloader;
import mesquite.chromaseq.lib.ChromFileNameDialog;
import mesquite.chromaseq.lib.PhPhRunner;
import mesquite.chromaseq.lib.XMLUtilities;
import mesquite.lib.CommandRecord;
import mesquite.lib.ExtensibleDialog;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.SingleLineTextField;
import mesquite.lib.StringUtil;
import mesquite.lib.ZipUtil;

public class AbiDownloaderImpl extends AbiDownloader {
	private PhPhRunner phredPhrap;
	private SingleLineTextField geneField;
	private SingleLineTextField taxonField;
	private SingleLineTextField batchNameField;
	private SingleLineTextField extractionField;

	public Class getDutyClass() {
		return AbiDownloaderImpl.class;
	}
	public String getName() {
		return "Abi Downloader";
	}
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		if (phredPhrap == null) {
			phredPhrap = (PhPhRunner)hireEmployee(commandRec, PhPhRunner.class, "Module to run Phred & Phrap");
		}
		if (phredPhrap != null) {
			return true;
		} else {
			return false;
		}
	}
	public boolean downloadAbiFilesFromDb(CommandRecord record) {
		return downloadAbiFilesFromDb(record, null);
	}
	public boolean downloadAbiFilesFromDb(CommandRecord record, MesquiteProject project) {
		// general plan:
		// (1) query for gene and specimen info
		if (!queryOptions()) {
			return false;
		}
		// (2) check for results (give number)
		Hashtable args = new Hashtable();
		conditionallyAddQueryArg(args, geneField, RequestParameters.GENE);
		conditionallyAddQueryArg(args, taxonField, RequestParameters.TAXON);		
		conditionallyAddQueryArg(args, batchNameField, RequestParameters.NAME);
		conditionallyAddQueryArg(args, extractionField, RequestParameters.EXTRACTION);		
		
		/*args.put(RequestParameters.EXTRACTION, extractionName);
		args.put(RequestParameters.GENE, geneName);*/
		Document results = XMLUtilities.getDocumentFromTapestryPageName("btolxml/ChromatogramSearchService", args);
		if (results == null) {
			XMLUtilities.outputRequestXMLError();
			return false;
		}
		if (results.getRootElement().getName().equals(XMLConstants.ERROR)) {
			// error with the search on the server
			MesquiteMessage.warnUser(results.getRootElement().getText());
			return false;
		} 
		// search succeeded -- see how many results
		Element rootElement = results.getRootElement();
		String numResultsString = rootElement.getAttributeValue(XMLConstants.COUNT);
		int numResults = 0;
		if (!StringUtil.blank(numResultsString)) {
			boolean hasResults = false;
			if (StringUtils.getIsNumeric(numResultsString)) {
				numResults = Integer.parseInt(numResultsString);
				if (numResults > 0) {
					hasResults = true;
				}
			}
			if (!hasResults) {
				MesquiteMessage.warnUser("There were no chromatograms found matching your search criteria.");
			}
		} else {
			// error occurred, warn the user
			MesquiteMessage.warnUser("There was an error searching for chromatograms on the server, the error was: " + rootElement.getText());
		}
		MesquiteMessage.warnUser("Your search found " + numResults + " chromatograms.");
		// (3) ask for directory to download results
		String directoryPath = MesquiteFile.chooseDirectory("Choose directory to download ABI files:");
		//String directoryPath = "/home/dmandel/pptest/";
		if (StringUtil.blank(directoryPath)) {
			return false;
		}
		// (4) make http request, download, unzip
		boolean downloadOk = downloadAndUnzipChromatograms(args, directoryPath);
		if (downloadOk) {
			// (5) run p/p on that directory
			return phredPhrap.doPhredPhrap(project, false, null, directoryPath);
		} else {
			MesquiteMessage.warnUser("Problems downloading and unzipping chromatograms, phred/phrap will not proceed.");
		}
		return false;
	}
	private void conditionallyAddQueryArg(Hashtable args, SingleLineTextField field, String queryKey) {
		if (!StringUtil.blank(field.getText())) {
			args.put(queryKey, field.getText());
		}
	}
	private boolean downloadAndUnzipChromatograms(Hashtable args, String directoryPath) {
		String url = XMLUtilities.baseDatabaseURL;
		args.put("service", "chromatogramdownload");
		MesquiteMessage.warnUser("Contacting server to download chromatograms");
		Object[] results = BaseHttpRequestMaker.makeHttpRequestAsStream(url, args);
		InputStream zipStream = (InputStream) results[0];
		GetMethod getMethod = (GetMethod) results[1];
		Random randomGen = new Random(System.currentTimeMillis());
		int filenameInt = randomGen.nextInt();
		filenameInt = Math.abs(filenameInt);
		String fullFilePath = directoryPath + MesquiteFile.fileSeparator + filenameInt + ".zip";
		File file = null;
		FileOutputStream outStream = null; 
		try {
			file = new File(fullFilePath);
			boolean fileCreated = file.createNewFile();
			outStream = new FileOutputStream(file);
		} catch (Exception e) {
			// can't create the file, we definitely can't download it!
			return false;
		}
		int totalBytesRead = 0;
		int bytesRead = -1; 
		final int BUF_SIZE = 4096; 
		byte[] buf = new byte[BUF_SIZE];		
		try {
			while ((bytesRead = zipStream.read(buf, 0, BUF_SIZE)) > -1) { 
				outStream.write(buf, 0, bytesRead);
				totalBytesRead += bytesRead;
				//MesquiteMessage.warnUser("read " + totalBytesRead + " bytes of chromatogram zip file");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outStream != null) {
				try {
					outStream.flush();
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (zipStream != null) {
				try {
					zipStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// all done reading from the connection, so release it
			getMethod.releaseConnection();
		}
		MesquiteMessage.warnUser("Zip file of chromatograms downloaded.  Unzipping will proceed.");
		
		// at this point we should have the zip downloaded and on the local filesystem
		// now we want to unzip it
		ZipUtil.unzipFileToDirectory(fullFilePath, directoryPath, true);

		return true;
	}
	
	private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(ChromFileNameDialog.CANCEL);		
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), 
				"Download ABI Options", buttonPressed);
		int fieldLength = 26;
		dialog.addLabel("Enter one or more options to find the chromatograms you'd like to use.", Label.CENTER);
		geneField = dialog.addTextField("Gene", "", fieldLength);
		taxonField = dialog.addTextField("Taxon", "", fieldLength);
		batchNameField = dialog.addTextField("ABI Batch", "", fieldLength);
		extractionField = dialog.addTextField("Extraction", "", fieldLength);		
		dialog.completeAndShowDialog(true);
		return (buttonPressed.getValue()== ChromFileNameDialog.OK);
	}
	
	/* TEST CODE!!
	 * public static void main(String[] args) {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");		
		AbiDownloaderImpl downloader = new AbiDownloaderImpl();
		downloader.downloadAbiFilesFromDb();
	}*/
}
