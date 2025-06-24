package mesquite.chromaseq.SequenceNameFromSampleCode;


import mesquite.chromaseq.lib.SequenceNameSource;
import mesquite.lib.StringUtil;

public class SequenceNameFromSampleCode extends SequenceNameSource  {

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	public boolean optionsSpecified(){
		return true;
	}
	
	public boolean hasOptions(){
		return false;
	}


	public boolean isReady() {
		return true;
	}
	/*.................................................................................................................*/

	public String getExtractionCode(String prefix, String ID) {
		return ID;
	}

	/*.................................................................................................................*/
	public void echoParametersToFile(StringBuffer logBuffer) {
	}

	public String getSampleCode(String prefix, String ID) {
		return ID;
	}
		/*.................................................................................................................*/

	public String getAlternativeName(String prefix, String ID) {  // short name
		if (StringUtil.blank(prefix))
			return ID;
		else
			return prefix+ID;
	}
	/*.................................................................................................................*/

	public String getSequenceName(String prefix, String ID) {  // long name
		if (StringUtil.blank(prefix))
			return ID;
		else
			return prefix+ID;
	}
	/*.................................................................................................................*/

	public boolean hasAlternativeNames() {
		return true;
	}
	/*.................................................................................................................*/

	public boolean isPrerelease() {
		return false;
	}

	/*.................................................................................................................*/

	public String getExplanation() {
		return "Provides sequence names simply as the sample code.";
	}

	/*.................................................................................................................*/

	public String getName() {
		return "Use Sample Code for Sequence Name";
	}

	/*.................................................................................................................*/

}
