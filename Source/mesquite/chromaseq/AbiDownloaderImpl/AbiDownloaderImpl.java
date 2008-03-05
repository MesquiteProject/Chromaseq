package mesquite.chromaseq.AbiDownloaderImpl;

import java.awt.Label;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Random;

import org.apache.commons.httpclient.methods.GetMethod;
import org.dom4j.*;

import mesquite.tol.lib.*;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;
//import mesquite.BTOL.lib.*;
import mesquite.molec.lib.DNADatabaseURLSource;

public class AbiDownloaderImpl extends AbiDownloader {
	private PhPhRunner phredPhrap;
	private String gene;
	private String taxon;
	private String batchName;
	private String extraction;
	private String dbUrl;

	public Class getDutyClass() {
		return AbiDownloaderImpl.class;
	}
	public String getName() {
		return "Abi Downloader";
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (phredPhrap == null) {
			phredPhrap = (PhPhRunner)hireEmployee(PhPhRunner.class, "Module to run Phred & Phrap");
		}
		if (phredPhrap != null) {
			return true;
		} else {
			return false;
		}
	}
	public boolean downloadAbiFilesFromDb() {
		return downloadAbiFilesFromDb(null);
	}
	public boolean downloadAbiFilesFromDb(MesquiteProject project) {
		loadPreferences();
		// general plan:
		// (1) query for gene and specimen info
		if (!queryOptions()) {
			return false;
		}
		// (2) check for results (give number)
		Hashtable args = new Hashtable();
		DNADatabaseURLSource databaseURLSource = phredPhrap.getDatabaseURLSource();
		if (databaseURLSource!=null) {
			conditionallyAddQueryArg(args, getGene(), databaseURLSource.getKeyString(DNADatabaseURLSource.GENE));
			conditionallyAddQueryArg(args, getTaxon(), databaseURLSource.getKeyString(DNADatabaseURLSource.TAXON));		
			conditionallyAddQueryArg(args, getBatchName(), databaseURLSource.getKeyString(DNADatabaseURLSource.NAME));
			conditionallyAddQueryArg(args, getExtraction(), databaseURLSource.getKeyString(DNADatabaseURLSource.EXTRACTION));		
			if (databaseURLSource.needsKeyValuePairAuthorization())
				args.put(databaseURLSource.getKeyString(DNADatabaseURLSource.AUTHORIZATION_KEY), databaseURLSource.getKey());
		}

		/*args.put(RequestParameters.EXTRACTION, extractionName);
		args.put(RequestParameters.GENE, geneName);*/
		
		Document results = MesquiteXMLUtilities.getDocumentFromTapestryPageName(databaseURLSource.getBaseURL(), databaseURLSource.getPage(DNADatabaseURLSource.CHROMATOGRAM_SEARCH_SERVICE), args);

		if (results == null) {
			MesquiteXMLUtilities.outputRequestXMLError();
			return false;
		}
		if (results.getRootElement().getName().equals(XMLConstants.ERROR)) {
			// error with the search on the server
			MesquiteMessage.warnUser(results.getRootElement().getText());
			return false;
		} 
		// search succeeded -- see how many results
		Element rootElement = results.getRootElement();
		String numResultsString = rootElement.attributeValue(XMLConstants.COUNT);
		int numResults = 0;
		if (!StringUtil.blank(numResultsString)) {
			boolean hasResults = false;
			if (StringUtil.getIsNumeric(numResultsString)) {
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
			return phredPhrap.doPhredPhrap(project, false, directoryPath);
		} else {
			MesquiteMessage.warnUser("Problems downloading and unzipping chromatograms, phred/phrap will not proceed.");
		}
		return false;
	}
	private void conditionallyAddQueryArg(Hashtable args, String value, String queryKey) {
		if (!StringUtil.blank(value)) {
			args.put(queryKey, value);
		}
	}
	private boolean downloadAndUnzipChromatograms(Hashtable args, String directoryPath) {
		String url = MesquiteXMLToLUtilities.getTOLBaseDatabaseURL(getDbUrl());
		args.put("service", "chromatogramdownload");
		MesquiteMessage.warnUser("Contacting server to download chromatograms");
		Object[] results = BaseHttpRequestMaker.makeHttpRequestAsStream(url, args);
		if (results==null) {
			MesquiteMessage.println("No results from HTTP request");
			return false;
		}
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
			MesquiteMessage.println("Can't create the file, we definitely can't download it!");
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
		loadPreferences();
		MesquiteInteger buttonPressed = new MesquiteInteger(ChromFileNameDialog.CANCEL);		
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), 
				"Download ABI Options", buttonPressed);
		int fieldLength = 26;
		dialog.addLabel("Enter one or more options to find the chromatograms you'd like to use.", Label.CENTER);
		SingleLineTextField urlField = dialog.addTextField("Url", getDbUrl(), fieldLength);
		SingleLineTextField geneField = dialog.addTextField("Gene", getGene(), fieldLength);
		SingleLineTextField taxonField = dialog.addTextField("Taxon", getTaxon(), fieldLength);
		SingleLineTextField batchNameField = dialog.addTextField("ABI Batch", getBatchName(), fieldLength);
		SingleLineTextField extractionField = dialog.addTextField("Extraction", getExtraction(), fieldLength);		
		dialog.completeAndShowDialog(true);
		boolean success = buttonPressed.getValue()== ChromFileNameDialog.OK; 
		if (success) {
			setDbUrl(urlField.getText());
			setGene(geneField.getText());
			setTaxon(taxonField.getText());
			setBatchName(batchNameField.getText());
			setExtraction(extractionField.getText());
			storePreferences();
		}
		return success;
	}
	public String[] getPreferencePropertyNames() {
		return new String[] {"batchName", "dbUrl", "extraction", "gene", "taxon"};
	}
	public String getBatchName() {
		return batchName;
	}
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}
	public String getDbUrl() {
		return dbUrl;
	}
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
	public String getExtraction() {
		return extraction;
	}
	public void setExtraction(String extraction) {
		this.extraction = extraction;
	}
	public String getGene() {
		return gene;
	}
	public void setGene(String gene) {
		this.gene = gene;
	}
	public String getTaxon() {
		return taxon;
	}
	public void setTaxon(String taxon) {
		this.taxon = taxon;
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
