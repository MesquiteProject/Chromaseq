package mesquite.chromaseq.lib;
import mesquite.lib.*;

public class SampleToSampleTranslationFile {

	String [] originalSampleCodes;
	String [] translatedSampleCodes;
	int numCodes;

	public SampleToSampleTranslationFile(String translationList) {
		readTabbedTranslationFile(translationList);
	}

	private void initializeArrays(int numCodes) {
		originalSampleCodes = new String[numCodes];
		translatedSampleCodes = new String[numCodes];
	}
	
	public void readTabbedTranslationFile(String translationList) {
		Parser parser = new Parser();
		parser.setString(translationList);
		Parser subParser = new Parser();
		String line = parser.getRawNextDarkLine();

		numCodes = 0;
		while (!StringUtil.blank(line)) {
			numCodes ++;
			line = parser.getRawNextDarkLine();
		}
		if (numCodes==0){
			MesquiteMessage.discreetNotifyUser("File is empty.");
			return;
		}
			
		initializeArrays(numCodes);

		int count = -1;
		parser.setPosition(0);
		subParser.setPunctuationString("\t");
		line = parser.getRawNextDarkLine();
		String token = "";
		
		while (!StringUtil.blank(line)) {
			count ++;
			subParser.setString(line);
			token = subParser.getFirstToken();
			if (StringUtil.notEmpty(token)) {
				originalSampleCodes[count] = token;
				token = subParser.getNextToken();
				if (StringUtil.notEmpty(token)) {
					translatedSampleCodes[count]=token;
				}
			}
			line = parser.getRawNextDarkLine();
		}
	}


	/*.................................................................................................................*/
	public String getTranslatedSampleCode(String originalCode) {
		if (!StringUtil.blank(originalCode)) {
			for (int i=0; i<originalSampleCodes.length; i++) {
				if (originalCode.trim().equalsIgnoreCase(originalSampleCodes[i])) {
					return translatedSampleCodes[i];
				}
			}
		}
		return "";
	}
}


