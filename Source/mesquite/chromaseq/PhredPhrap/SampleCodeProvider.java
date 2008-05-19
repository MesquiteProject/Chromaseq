package mesquite.chromaseq.PhredPhrap;

import java.util.*;

import mesquite.chromaseq.lib.*;
import mesquite.lib.*;

import org.dom4j.*;

public class SampleCodeProvider {
	
	/**
	 * assumes doc has format like this:
	 * <samplecodes>
	 * 	<samplecode name="BT1560">
	 * 		<sequence></sequence>
	 * 		(fullsequence optional)
	 * 		<fullsequence></fullsequence>
	 * 	</samplecode>
	 * </samplecodes>
	 * @param sampleCode
	 * @param namesDoc
	 * @return
	 */
	public static String[] getSeqNamesFromXml(MesquiteString sampleCode, Document namesDoc) {
		String elementName = "samplecode";
		String nameAttrName = "name";
		String sequenceElementName = "sequence";
		String fullSequenceElementName = "fullsequence";
		String sampleCodeString  = sampleCode.getValue();
		List sampleCodeElements = namesDoc.getRootElement().elements(elementName);
		for (Iterator iter = sampleCodeElements.iterator(); iter.hasNext();) {
			Element nextSampleCodeElement = (Element) iter.next();
			String name = nextSampleCodeElement.attributeValue(nameAttrName);
			if (!StringUtil.blank(name)) {
				if (sampleCodeString.equals(name)) {
					// have a match
					String seq = nextSampleCodeElement.elementText(sequenceElementName);
					String fullseq = seq;
					if (nextSampleCodeElement.element(fullSequenceElementName) != null) {
						fullseq = nextSampleCodeElement.elementText(fullSequenceElementName);
					}
					return new String[]{seq, fullseq};
				}
			}
		}
		// got here and no match found -- log an error
		MesquiteMessage.warnUser("No sample code named '" + sampleCode + "' found in sample code xml file.");
		return new String[]{"", ""};
	}

/*
	public static boolean getSequenceName(PhPhRunner phphRunner, String sampleCode, MesquiteString seqName, MesquiteString fullSeqName) {
		if (seqName==null || fullSeqName==null)
			return false;
		if (!StringUtil.blank(sampleCode)) {
			if (primerAndSampleCodeInfoFromDatabase) {
				// get the value of the sample code, if it starts
				// with DNA, then it's a straight Extraction code,
				// otherwise if it starts with BP, then it's
				// a btol pcr reaction id, which looks up an extraction code
				phphRunner.checkDatabaseSource();
				String[] results = new DatabaseSampleCodeSource().getSequenceNamesFromCode(databaseURLSource, startTokenResult.getValue(), sampleCode);
				seqName.setValue(results[0]);
				fullSeqName.setValue(results[1]);
			} else if (haveNameList && translateSampleCodes) {
				if (namesInXml) {
					String[] results = SampleCodeProvider.getSeqNamesFromXml(sampleCode, namesDoc);
					seqName.setValue(results[0]);
					fullSeqName.setValue(results[1]);
				} else {
					loc = sampleCodeList.indexOf(sampleCode);   //��� problem:  if have 551A and 551, this will pick up 551

					if (loc<0 && !(sampleCode.equalsIgnoreCase("0000")||sampleCode.equalsIgnoreCase("000"))) {
						seqName.setValue(sampleCode);
						fullSeqName.setValue(sampleCode);
					}
					else {
						sampleCodeListParser.setPosition(loc+sampleCode.length()+1);
						seqName.setValue(StringUtil.removeNewLines(sampleCodeListParser.getNextToken());
						fullSeqName.setValue(StringUtil.removeNewLines(sampleCodeListParser.getNextToken()));
						if (!";".equals(sampleCodeListParser.getNextToken()))
							fullSeqName.setValue(seqName);
					}
				}
			}
			else {
				seqName.setValue(sampleCode);
				fullSeqName.setValue(sampleCode);
			
			}
		} else
			return false;
		return true;

	}
	*/
}
