package mesquite.chromaseq.lib;

import java.util.Map;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteFile;

import org.jdom.Document;
import org.tolweb.base.http.BaseHttpRequestMaker;
import org.tolweb.base.xml.BaseXMLReader;
import org.tolweb.treegrow.main.XMLConstants;

public class XMLUtilities {
	private static String checkConnectionURL = "http://google.com";
	private static String databaseURL = "http://btol.tolweb.org/onlinecontributors/app?service=external&page=";
	private static String testDatabaseURL = "http://zissou.cals.arizona.edu:8080/onlinecontributors/app?service=external&page=";
	
	public static Document getDocumentFromTapestryPageName(String pageName, Map args) {
		return getDocumentFromTapestryPageName(pageName, args, false);
	}
	public static Document getDocumentFromTapestryPageNameMultipart(String pageName, Map stringArgs, 
			Map fileArgs) {
		String url = testDatabaseURL;
		// need this here so tapestry will call the external service
		stringArgs.put("service", "external");
		stringArgs.put("page", pageName);
		Document returnDoc = BaseHttpRequestMaker.getTap4ExternalUrlDocumentMultipart(url, pageName, 
	    		stringArgs, fileArgs);
		return returnDoc;
	}
	public static Document getDocumentFromTapestryPageName(String pageName, Map args, boolean isPost) {
		Document returnDoc = null;
		args.put("service", "external");
		if (checkConnection()) {
			String url = getDatabaseURL();
			if (isPost) {
				url = testDatabaseURL;
			}
			returnDoc = BaseHttpRequestMaker.getTap4ExternalUrlDocument(url, 
					pageName, args, isPost);
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
	
	public static Document getDocumentFromFilePath(String path) {
		String docString = MesquiteFile.getFileContentsAsString(path);
		return BaseXMLReader.getDocumentFromString(docString);
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
