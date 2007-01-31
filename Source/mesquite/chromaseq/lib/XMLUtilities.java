package mesquite.chromaseq.lib;

import java.util.Map;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteMessage;

import org.jdom.Document;
import org.tolweb.base.http.BaseHttpRequestMaker;
import org.tolweb.base.xml.BaseXMLReader;
import org.tolweb.treegrow.main.XMLConstants;

public class XMLUtilities {
	private static String checkConnectionURL = "http://google.com";
	public static String baseDatabaseURL = "http://localhost/onlinecontributors/app"; 	
	private static String databaseURL = baseDatabaseURL + "?page=";
	
	public static Document getDocumentFromTapestryPageName(String pageName, Map args) {
		return getDocumentFromTapestryPageName(pageName, args, false);
	}
	public static Document getDocumentFromTapestryPageNameMultipart(String pageName, Map stringArgs, 
			Map fileArgs) {
		String url = databaseURL;
		// need this here so tapestry will call the external service
		stringArgs.put("service", "external");
		stringArgs.put("page", pageName);
		try {
			Document returnDoc = BaseHttpRequestMaker.getTap4ExternalUrlDocumentMultipart(url, pageName, 
		    		stringArgs, fileArgs);
			return returnDoc;
		} catch (Exception e) {
			e.printStackTrace();
			// error in communication, likely a dead connection on one end or the other
			return null;
		}
	}
	public static Document getDocumentFromTapestryPageName(String pageName, Map args, boolean isPost) {
		Document returnDoc = null;
		args.put("service", "external");
		if (checkConnection()) {
			String url = getDatabaseURL();
			try {
				returnDoc = BaseHttpRequestMaker.getTap4ExternalUrlDocument(url, 
						pageName, args, isPost);
			} catch (Exception e) {
				// communication error, allow doc to remain null
			}
			if (returnDoc == null || returnDoc.getRootElement() == null || 
					returnDoc.getRootElement().getName().equals(XMLConstants.ERROR)) {
				returnDoc = null;
			}
		} else {
			String error = "You don't appear to have an internet connection.  This operation cannot complete without a connection";
			Debugg.println(error);
		}
		return returnDoc;
	}
	
	public static Document getDocumentFromString(String docString) {
		return BaseXMLReader.getDocumentFromString(docString);		
	}
	
	public static Document getDocumentFromFilePath(String path) {
		String docString = MesquiteFile.getFileContentsAsString(path);
		return getDocumentFromString(docString);
	}
	
	public static void outputRequestXMLError() {
		String errorMessage = "A network error has occurred, this usually means one of two things:\n(1) You don't have an internet connection" + 
			"\n(2)The server you are attempting to contact is having problems\nPlease make sure you have an internet connection and try again.";
		MesquiteMessage.warnUser(errorMessage);
	}
	
	private static boolean checkConnection() {
		/*byte[] bytes = BaseHttpRequestMaker.makeHttpRequest(checkConnectionURL);
		return bytes != null;*/
		return true;
	}
	public static String getDatabaseURL() {
		return databaseURL;
	}

	public static void setDatabaseURL(String URL) {
		databaseURL = URL;
	}
}
