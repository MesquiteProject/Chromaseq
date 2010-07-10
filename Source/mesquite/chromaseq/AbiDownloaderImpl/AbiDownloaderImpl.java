/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.
Version 0.980   July 2010
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.AbiDownloaderImpl;

import java.awt.Label;
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
	protected DNADatabaseURLSource databaseURLSource = null;
	private ChromatogramProcessor chromatogramProcessorTask;
	private String gene;
	private String taxon;
	private String batchName;
	private String extraction;
	private String dbUrl;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e1 = registerEmployeeNeed(ChromatogramProcessor.class, "Requires a processor of chromatograms.", "This is activated automatically.");
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (chromatogramProcessorTask == null) {
			chromatogramProcessorTask = (ChromatogramProcessor)hireEmployee(ChromatogramProcessor.class, "Module to process chromatograms");
		}
		if (chromatogramProcessorTask != null) {
			return true;
		} else {
			return false;
		}
	}

	public Class getDutyClass() {
		return AbiDownloaderImpl.class;
	}
	public String getName() {
		return "Abi Downloader";
	}
	public boolean downloadAbiFilesFromDb() {
		return downloadAbiFilesFromDb(null);
	}
	/*.................................................................................................................*/
	public void checkDatabaseSource() {
		if (databaseURLSource==null)
			databaseURLSource= (DNADatabaseURLSource)hireEmployee(DNADatabaseURLSource.class, "Source of Database Connectivity");
	}
	/*.................................................................................................................*/
	public boolean downloadAbiFilesFromDb(MesquiteProject project) {
		loadPreferences();
		// general plan:
		// (1) query for gene and specimen info
		if (!queryOptions()) {
			return false;
		}
		// (2) check for results (give number)
		Hashtable args = new Hashtable();
		checkDatabaseSource();

		if (databaseURLSource!=null) {
			conditionallyAddQueryArg(args, getGene(), databaseURLSource.getKeyString(DNADatabaseURLSource.GENE));
			conditionallyAddQueryArg(args, getTaxon(), databaseURLSource.getKeyString(DNADatabaseURLSource.TAXON));		
			conditionallyAddQueryArg(args, getBatchName(), databaseURLSource.getKeyString(DNADatabaseURLSource.NAME));
			conditionallyAddQueryArg(args, getExtraction(), databaseURLSource.getKeyString(DNADatabaseURLSource.EXTRACTION));		
			if (databaseURLSource.needsKeyValuePairAuthorization())
				args.put(databaseURLSource.getKeyString(DNADatabaseURLSource.AUTHORIZATION_KEY), databaseURLSource.getKey());
		} else {
			MesquiteMessage.warnUser("Database Source could not be obtained.");
			return false;
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
		String numResultsString = rootElement.attributeValue(databaseURLSource.getKeyString(DNADatabaseURLSource.COUNT));
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
		boolean downloadOk = downloadAndUnzipChromatograms(databaseURLSource,args, directoryPath);
		if (downloadOk) {
			// (5) run p/p on that directory
			return chromatogramProcessorTask.processChromatograms(project, false, directoryPath);
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
	private boolean downloadAndUnzipChromatograms(DNADatabaseURLSource databaseURLSource, Hashtable args, String directoryPath) {
		if (databaseURLSource== null)
			return false;
		
		String url = databaseURLSource.getChromatogramDownloadURL(args);
		
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
		MesquiteInteger buttonPressed = new MesquiteInteger(ExtensibleDialog.defaultCANCEL);		
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
		boolean success = buttonPressed.getValue()== dialog.defaultOK; 
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
