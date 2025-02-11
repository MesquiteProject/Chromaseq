/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.ManageChromaseqBlock;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.misc.VoucherInfoFromOTUIDDB;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.meristic.lib.MeristicData;
import mesquite.chromaseq.lib.*;

public class ManageChromaseqBlock extends FileInit implements MesquiteListener{

	int chromaseqVersionOfFile = 0;
	int chromaseqBuildOfFile =0;
	int chromaseqRegistrationBuildOfFile =0;

	int numBlocks =0;
	public Class getDutyClass(){
		return ManageChromaseqBlock.class;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public int getChromaseqVersionOfFile() {
		return chromaseqVersionOfFile;
	}
	public void setChromaseqVersionOfFile(int chromaseqVersionOfFile) {
		this.chromaseqVersionOfFile = chromaseqVersionOfFile;
	}
	public int getChromaseqBuildOfFile() {
		return chromaseqBuildOfFile;
	}
	public void setChromaseqBuildOfFile(int chromaseqBuildOfFile) {
		this.chromaseqBuildOfFile = chromaseqBuildOfFile;
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData) {
			if (code==MesquiteListener.NAMES_CHANGED || code==MesquiteListener.SELECTION_CHANGED) {
				//	contigDisplay.repaintPanels();
			}
			else if (!Notification.appearsCosmetic(notification) && ChromaseqUtil.isChromaseqEditedMatrix((CharacterData)obj)){
				if (!((code==MesquiteListener.PARTS_CHANGED || code==MesquiteListener.PARTS_MOVED) && notification.subcodesContains(MesquiteListener.TAXA_CHANGED))) {
					DNAData editedData= (DNAData)((CharacterData)obj);
					boolean recalcMappers = true;
					int itStart = 0;
					int itEnd = editedData.getNumTaxa();
					int icStart = 0;
					int icEnd = editedData.getNumChars();
					if (editedData.singleCellSubstitution(notification)) {
						if (parameters!=null && parameters.length>=2) {  //get second parameter, which is taxon changed
							itStart=parameters[1];
						}
						if (parameters!=null && parameters.length>=1) {  //get second parameter, which is taxon changed
							icStart=parameters[0];
						}
						ChromaseqUtil.checkFlags(editedData,itStart,itStart,icStart,icStart);
						recalcMappers = false;
					} 
					else if (editedData.onlyAllCellsShifted(notification)){
						recalcMappers = false;
					}
					else if (editedData.singleCellChange(notification)){
						recalcMappers = true;
						if (parameters!=null && parameters.length>=2) {  //get second parameter, which is taxon changed
							itStart=parameters[1];
							itEnd=parameters[1];
						}
						if (parameters!=null && parameters.length>=1) {  //get second parameter, which is taxon changed
							icStart=parameters[0];
							icEnd=parameters[0];
						}
						ChromaseqUtil.checkFlags(editedData,itStart,itStart,icStart,icStart);
					} 
					else if (editedData.singleTaxonChange(notification)){
						recalcMappers = true;
						if (parameters!=null && parameters.length>=1) {  //get first parameter, which is taxon changed
							itStart=parameters[0];
							itEnd=parameters[0];
						}
					} 						 

					if (recalcMappers){
						MesquiteTimer timer = null;
						timer = new MesquiteTimer();
						timer.start();
						if (editedData!=null) {
							if (itStart!=itEnd) {
								logln("Re-inferring all contig mappers.\n");
							}
							int count=0;
							for (int it=itStart; it<itEnd; it++) {
								ContigMapper contigMapper = ChromaseqUtil.getContigMapperAssociated(editedData, it);
								if (contigMapper!=null) {
									contigMapper.inferFromExistingRegistry(editedData, it, this);
									count++;
									if (count % 10 == 0)
										log(".");
								}
							}
							if (itStart!=itEnd && timer!=null) 
								logln("\nContig mapper re-inference complete, time: " + timer.timeSinceLastInSeconds() + " seconds");
						}
						ChromaseqUtil.checkFlags(editedData,itStart,itEnd,icStart,icEnd);
						if (MesquiteTrunk.debugMode || getPackageIntroModule().isPrerelease())
							logln("Checking flags: " + timer.timeSinceLastInSeconds() + " seconds");
					}

				}
			}
		} 
	}

	/*.................................................................................................................*
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot(); 
  	 	temp.addLine("addAuthorNameToMatrices");
 	 	return temp;
  	 }
	/*.................................................................................................................*
	void reportAttached(Associable a, String nr){
		if (a.getAttachment(nr) != null)
			logln("YES attached [" + nr + " = " + a.getAttachment(nr) + "] " +   a.getName());
		else
			logln("NO attached [" + nr + "] " + a.getName());
	}
	void reportObject(Associable a, NameReference nr){
		if (a.anyAssociatedObject(nr))
			logln("YES object [" + nr.getValue() + " ; 0 = " + a.getAssociatedObject(nr, 0)+ "] " + a.getName());
		else
			logln("NO object [" + nr.getValue() + "] " + a.getName());
	}
	void reportLong(Associable a, NameReference nr){
		if (a.getWhichAssociatedLong(nr) == null)
			logln("NO long [" + nr.getValue() + " ; 0 = " + MesquiteLong.toString(a.getAssociatedLong(nr, 0))+ "] " + a.getName());
		else
			logln("YES long [" + nr.getValue() + "] " + a.getName());
	}
	void reportDouble(Associable a, NameReference nr){
		if (a.getWhichAssociatedDouble(nr) == null)
			logln("NO double [" + nr.getValue() + " ; 0 = " + MesquiteDouble.toString(a.getAssociatedDouble(nr, 0))+ "] " + a.getName());
		else
			logln("YES double [" + nr.getValue() + "] " + a.getName());
	}
	void reportCellObjects(mesquite.lib.characters.CharacterData data, NameReference nr){
		if (data.getWhichCellObjects(nr) == null)
			logln("NO cell objects [" + nr.getValue() + " ; 0,0 = " + data.getCellObject(nr, 0,0)+ "] " + data.getName());
		else
			logln("YES cell objects [" + nr.getValue() + "] " + data.getName());
	}
	void convertOldToNew(){
		MesquiteProject proj = getProject();
		int numT = proj.getNumberTaxas();
		for (int i = 0; i<numT; i++){
			logln("=====TAXA=======");
			Taxa taxa = proj.getTaxa(i);
			reportObject(taxa, ChromaseqUtil.voucherCodeRef);
			reportObject(taxa, ChromaseqUtil.voucherDBRef);
			reportObject(taxa, ChromaseqUtil.origTaxonNameRef);
		}
		int numM = proj.getNumberCharMatrices();
		for (int i = 0; i<numM; i++){
			mesquite.lib.characters.CharacterData data = proj.getCharacterMatrix(i);
			logln("=========MATRIX=======" + data.getName());
			reportAttached(data, ChromaseqUtil.PHPHIMPORTIDREF);
			reportAttached(data, ChromaseqUtil.GENENAMEREF);
			reportAttached(data, ChromaseqUtil.PHPHMQVERSIONREF);
			reportAttached(data, ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF);
			reportLong(data,ChromaseqUtil.trimmableNameRef);
			Associable tInfo = data.getTaxaInfo(false);
			if (tInfo == null)
				logln("NO TINFO  " + data.getName());
			else {
				reportLong(tInfo, ChromaseqUtil.trimmableNameRef);
				reportLong(tInfo, ChromaseqUtil.chromatogramsExistRef);
				reportLong(tInfo, ChromaseqUtil.whichContigRef);
				reportLong(tInfo, ChromaseqUtil.startTrimRef);
	//			reportBoolean(tInfo, ChromaseqUtil.reversedRef);
	//			reportBoolean(tInfo, ChromaseqUtil.complementedRef);
				reportDouble(tInfo, ChromaseqUtil.qualityNameRef);
				reportObject(tInfo, ChromaseqUtil.aceRef);
				reportObject(tInfo, ChromaseqUtil.chromatogramReadsRef);
				reportObject(tInfo, ChromaseqUtil.origReadFileNamesRef);
				reportObject(tInfo, ChromaseqUtil.primerForEachReadNamesRef);
				reportObject(tInfo, ChromaseqUtil.sampleCodeNamesRef);
				reportObject(tInfo, ChromaseqUtil.sampleCodeRef);
			}
			reportCellObjects(data,ChromaseqUtil.paddingRef);
			reportCellObjects(data,ChromaseqUtil.trimmableNameRef);
		}
	}
	/*----------------------------------------*/
	boolean writeAttached(StringBuffer sb, Associable a, String nr){
		MesquiteString ms = ChromaseqUtil.getStringAttached(a,nr);
		if (ms == null)
			return false;
		if (ms.getValue() != null){
			sb.append("\tattach  ref = " +  ParseUtil.tokenize(nr) + " s = " + ParseUtil.tokenize(ms.getValue()) + ";" + StringUtil.lineEnding());
			return true;
		}
		return false;
	}
	/*----------------------------------------*/
	boolean writeStrings(StringBuffer sb, Associable a, NameReference nr){
		boolean some = false;
		if (a.anyAssociatedObject(nr)){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				String value = ChromaseqUtil.getStringAssociated(a,nr, i);

				if (value != null){
					sb.append("\tasString  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()) + " string = " + ParseUtil.tokenize(value) + ";" + StringUtil.lineEnding());
					some = true;
				}

			}
		}
		return some;
	}
	/*----------------------------------------*/
	boolean writeStringArrays(StringBuffer sb, Associable a, NameReference nr){
		boolean some = false;
		if (a.anyAssociatedObject(nr)){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				String[] value = ChromaseqUtil.getStringsAssociated(a,nr, i);

				if (value != null){
					sb.append("\tasStrings  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()));
					for (int k=0; k< value.length; k++)
						sb.append(" string = " + ParseUtil.tokenize(value[k]));
					sb.append(";" + StringUtil.lineEnding());
					some = true;
				}

			}
		}
		return some;
	}
	/*----------------------------------------*/
	boolean writeLongs(StringBuffer sb, Associable a, NameReference nr){
		boolean some = false;
		if (a.getAssociatedLongs(nr) != null){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				long value = ChromaseqUtil.getLongAssociated(a,nr, i);

				if (MesquiteLong.isCombinable(value)){
					sb.append("\tasLong  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()) + " long = " + value + ";" + StringUtil.lineEnding());
					some = true;
				}

			}
		}
		return some;
	}
	boolean writeDoubles(StringBuffer sb, Associable a, NameReference nr){
		boolean some = false;
		if (a.getAssociatedDoubles(nr) != null){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				double value = ChromaseqUtil.getDoubleAssociated(a,nr, i);

				if (MesquiteDouble.isCombinable(value)){
					sb.append("\tasDouble  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()) + " double = " + value + ";" + StringUtil.lineEnding());
					some = true;
				}

			}
		}
		return some;
	}
	void writeCellObjects(StringBuffer sb, mesquite.lib.characters.CharacterData data, NameReference nr){
		if (data.getWhichCellObjects(nr) == null)
			logln("NO cell objects [" + nr.getValue() + " ; 0,0 = " + data.getCellObject(nr, 0,0)+ "] " + data.getName());
		else
			logln("YES cell objects [" + nr.getValue() + "] " + data.getName());
	}
	/*----------------------------------------*/
	String getBlockContentsOld(){
		MesquiteProject proj = getProject();
		StringBuffer sb = new StringBuffer();
		int numT = proj.getNumberTaxas();
		boolean some = false;
		for (int i = 0; i<numT; i++){
			Taxa taxa = proj.getTaxa(i);
			sb.append("\n\tTAXA = " + ParseUtil.tokenize(taxa.getName()) + " ;" + StringUtil.lineEnding());
			boolean here = writeStrings(sb, taxa, VoucherInfoFromOTUIDDB.voucherCodeRef);
			some  |= here;
			here = writeStrings(sb,taxa, VoucherInfoFromOTUIDDB.voucherDBRef);
			some  |= here;
			here = writeStrings(sb,taxa, ChromaseqUtil.origTaxonNameRef);
			some  |= here;
		}
		int numM = proj.getNumberCharMatrices();
		for (int i = 0; i<numM; i++){
			mesquite.lib.characters.CharacterData data = proj.getCharacterMatrix(i);
			if (data.getWritable()){
				sb.append("\n\tCHARACTERS = " + ParseUtil.tokenize(data.getName()) + " ;" + StringUtil.lineEnding());
				boolean here = writeAttached(sb,  data, ChromaseqUtil.PHPHIMPORTIDREF);
				some  |= here;
				here = writeAttached(sb,  data, ChromaseqUtil.GENENAMEREF);
				some  |= here;
				here = writeAttached(sb,  data, ChromaseqUtil.PHPHMQVERSIONREF);
				some  |= here;
				here = writeAttached(sb,  data, ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF);
				some  |= here;
				here = writeLongs(sb, data,ChromaseqUtil.chromaseqCellFlagsNameRef);
				some  |= here;
				Associable tInfo = data.getTaxaInfo(false);
				if (tInfo != null) {
					sb.append("\n\tTAXAINFO" + " ;" + StringUtil.lineEnding());
					here = writeLongs(sb, tInfo, ChromaseqUtil.chromaseqCellFlagsNameRef);
					some  |= here;
					here = writeLongs(sb, tInfo, ChromaseqUtil.numChromatogramsRef);
					some  |= here;
					here = writeLongs(sb, tInfo, ChromaseqUtil.whichContigRef);
					some  |= here;
					here = writeLongs(sb, tInfo, ChromaseqUtil.startTrimRef);
					some  |= here;
					here = writeDoubles(sb, tInfo, ChromaseqUtil.qualityNameRef);
					some  |= here;
					here = writeStrings(sb,tInfo, ChromaseqUtil.aceRef);
					some  |= here;
					here = writeStrings(sb,tInfo, ChromaseqUtil.chromatogramReadsRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.origReadFileNamesRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.primerForEachReadNamesRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.sampleCodeNamesRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.sampleCodeRef);
					some  |= here;
				}
				//reportCellObjects(data,ChromaseqUtil.paddingRef);
				//reportCellObjects(data,ChromaseqUtil.trimmableNameRef);
			}
		}
		if (!some)
			return null;
		return sb.toString();
	}
	/*----------------------------------------*/
	String getBlockContents(){
		MesquiteProject proj = getProject();
		StringBuffer sb = new StringBuffer();
		int numT = proj.getNumberTaxas();
		boolean some = false;
		for (int i = 0; i<numT; i++){
			Taxa taxa = proj.getTaxa(i);
			sb.append("\n\tTAXA = " + ParseUtil.tokenize(taxa.getName()) + " ;" + StringUtil.lineEnding());
			boolean here = writeStrings(sb, taxa, VoucherInfoFromOTUIDDB.voucherCodeRef);
			some  |= here;
			here = writeStrings(sb,taxa, VoucherInfoFromOTUIDDB.voucherDBRef);
			some  |= here;
			here = writeStrings(sb,taxa, ChromaseqUtil.origTaxonNameRef);
			some  |= here;
		}
		int numM = proj.getNumberCharMatrices();
		for (int i = 0; i<numM; i++){
			mesquite.lib.characters.CharacterData data = proj.getCharacterMatrix(i);
			if (data.getWritable()){
				sb.append("\n\tCHARACTERS = " + ParseUtil.tokenize(data.getName()) + " ;" + StringUtil.lineEnding());
				boolean here = writeAttached(sb,  data, ChromaseqUtil.PHPHIMPORTIDREF);
				some  |= here;
				here = writeAttached(sb,  data, ChromaseqUtil.GENENAMEREF);
				some  |= here;
				here = writeAttached(sb,  data, ChromaseqUtil.PHPHMQVERSIONREF);
				some  |= here;
				here = writeAttached(sb,  data, ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF);
				some  |= here;
				here = writeLongs(sb, data,ChromaseqUtil.chromaseqCellFlagsNameRef);
				some  |= here;
				Associable tInfo = data.getTaxaInfo(false);
				if (tInfo != null) {
					sb.append("\n\tTAXAINFO" + " ;" + StringUtil.lineEnding());
					here = writeLongs(sb, tInfo, ChromaseqUtil.chromaseqCellFlagsNameRef);
					some  |= here;
					here = writeLongs(sb, tInfo, ChromaseqUtil.numChromatogramsRef);
					some  |= here;
					here = writeLongs(sb, tInfo, ChromaseqUtil.whichContigRef);
					some  |= here;
					here = writeLongs(sb, tInfo, ChromaseqUtil.startTrimRef);
					some  |= here;
					here = writeDoubles(sb, tInfo, ChromaseqUtil.qualityNameRef);
					some  |= here;
					here = writeStrings(sb,tInfo, ChromaseqUtil.aceRef);
					some  |= here;
					here = writeStrings(sb,tInfo, ChromaseqUtil.chromatogramReadsRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.origReadFileNamesRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.primerForEachReadNamesRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.sampleCodeNamesRef);
					some  |= here;
					here = writeStringArrays(sb,tInfo, ChromaseqUtil.sampleCodeRef);
					some  |= here;
				}
				//reportCellObjects(data,ChromaseqUtil.paddingRef);
				//reportCellObjects(data,ChromaseqUtil.trimmableNameRef);
			}
		}
		if (!some)
			return null;
		return sb.toString();
	}
	static final String taxonTokenAbbrev = " T = ";

	/*.................................................................................................................*/
	public String writeNexusCommands(MesquiteFile file){ 
		StringBuffer s = new StringBuffer(100);
		StringBuffer tokSB = new StringBuffer(100);
		String eL =";" + StringUtil.lineEnding();
		MesquiteProject project = file.getProject();
		for (int i=0; i<project.getNumberCharMatrices(); i++){
			CharacterData data = getProject().getCharacterMatrix(i);
			if (data.getFile()==file && data.getWritable()){
				Associable as = data.getTaxaInfo(false);
				if (as != null){
					boolean found = false;
					for (int it = 0; it<data.getNumTaxa() && !found; it++){
						int numObs = as.getNumberAssociatedObjects();
						for (int v = 0; v<numObs; v++){  
							ObjectArray array = as.getAssociatedObjects(v);
							Object c = array.getValue(it);

							if (c != null && c instanceof ContigMapper){
								found = true;
								break;
							}
						}
					}					
					if (found) {
						if (project.getNumberCharMatrices()>1 || project.getNumberTaxas()>1) //note shift in 1. 06 to "current matrix and taxa" to avoid having to repeat in each note
							s.append("\tCHARACTERS = " +  StringUtil.tokenize(data.getName(), null, tokSB) +" TAXA = " +  StringUtil.tokenize(data.getTaxa().getName(), null, tokSB) + eL);

						for (int it = 0; it<data.getNumTaxa(); it++){
							int numObs = as.getNumberAssociatedObjects();
							for (int v = 0; v<numObs; v++){  
								ObjectArray array = as.getAssociatedObjects(v);
								Object c = array.getValue(it);

								if (c != null && c instanceof ContigMapper){
									s.append("\tCONTIGMAPPER ");
									s.append(taxonTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(it)));
									String mapperString = ((ContigMapper)c).getNEXUSCommand();
									if (StringUtil.blank(mapperString))
										return null;
									s.append(mapperString);
									s.append(eL);
								}
							}
						}
					}
				}

			}
		}

		return s.toString();
	}
	/*.................................................................................................................*/
	public void attachChromaseqRegistrationBuild(MesquiteFile f, int build) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (data instanceof DNAData) 
				ChromaseqUtil.setChromaseqRegistrationBuildOfMatrix((DNAData)data, build);
		}
	}
	/*...................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String command, MesquiteString comment, String fileReadingArguments){ 
		MesquiteProject project = file.getProject();
		String commandName = parser.getFirstToken(command);
		if (commandName.equalsIgnoreCase("VERSION")) {
			String token  = parser.getNextToken();
			int version = MesquiteInteger.fromString(token);				
			if (MesquiteInteger.isCombinable(version)){
				chromaseqVersionOfFile = version;
			}
		}
		else	if (commandName.equalsIgnoreCase("BUILD")) {
			String token  = parser.getNextToken();
			int build = MesquiteInteger.fromString(token);		
			if (MesquiteInteger.isCombinable(build)){
				chromaseqBuildOfFile = build;
			}
		}
		else	if (commandName.equalsIgnoreCase("REGISTRATIONBUILD")) {
			String token  = parser.getNextToken();
			int registrationBuild = MesquiteInteger.fromString(token);		
			if (MesquiteInteger.isCombinable(registrationBuild)){
				chromaseqRegistrationBuildOfFile = registrationBuild;
				attachChromaseqRegistrationBuild(file, registrationBuild);
			}
		}
		else if  (commandName.equalsIgnoreCase("CONTIGMAPPER")) {
			stringPos.setValue((int)parser.getPosition());
			String[][] subcommands  = ParseUtil.getSubcommands(command, stringPos);
			if (subcommands == null || subcommands.length == 0 || subcommands[0] == null || subcommands[0].length == 0)
				return false;
			int whichTaxon = MesquiteInteger.unassigned;
			Taxa taxa = nBlock.getDefaultTaxa();
			CharacterData data = nBlock.getDefaultCharacters();
			ContigMapper contigMapper = null;
			if (taxa !=null && data !=null && (data instanceof MolecularData)) {
				for (int i=0; i<subcommands[0].length; i++){
					String subC = subcommands[0][i];
					int numBases = 0;
					if ("T".equalsIgnoreCase(subC) || "TAXON".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						whichTaxon = MesquiteInteger.fromString(token);
						if (!MesquiteInteger.isCombinable(whichTaxon))
							return false;
						whichTaxon = Taxon.toInternal(whichTaxon);
						contigMapper = new ContigMapper();
						ChromaseqUtil.setContigMapperAssociated((MolecularData)data, whichTaxon, contigMapper);
					} else if (MesquiteInteger.isCombinable(whichTaxon) && contigMapper!=null){
						if ("DELETED".equalsIgnoreCase(subC)) {
							String token = subcommands[1][i];
							Parser parser = new Parser(token);
							/*	if (numBases<=0){
								numBases = parser.getNumberOfDarkChars();
								parser.setPosition(0);
							}
							if (contigMapper.getNumBases()< numBases)
								contigMapper.setNumBases(numBases);  */
							for (int base = 0; base<contigMapper.getNumBases(); base++) {
								char c = parser.nextDarkChar();
								if ("1".equalsIgnoreCase(""+ c))
									contigMapper.setDeletedBase(base, true);
							}
							contigMapper.setStoredInFile(true);
						} else if ("ADDEDBEFORE".equalsIgnoreCase(subC)) {
							String token = subcommands[1][i];
							Parser parser = new Parser(token);
							for (int base = 0; base<contigMapper.getNumBases(); base++) {
								String s = parser.getNextToken();
								int added = MesquiteInteger.fromString(s);
								if (MesquiteInteger.isCombinable(added))
									contigMapper.setAddedBases(base, added);
							}
							contigMapper.setStoredInFile(true);
						}
						else if ("NUMBASES".equalsIgnoreCase(subC)) {
							String token = subcommands[1][i];
							numBases = MesquiteInteger.fromString(token);
							if (!MesquiteInteger.isCombinable(numBases))
								numBases=0;
							if (contigMapper.getNumBases()< numBases)
								contigMapper.setNumBases(numBases);
						}				
						else if ("TRIMSTART".equalsIgnoreCase(subC)) {
							String token = subcommands[1][i];
							int numTrimmedFromStart = MesquiteInteger.fromString(token);
							if (!MesquiteInteger.isCombinable(numTrimmedFromStart))
								numTrimmedFromStart=0;
							contigMapper.setNumTrimmedFromStart(numTrimmedFromStart);
						}				
					}
				}
			}
		}
		else if  (commandName.equalsIgnoreCase("CHARACTERS")) {
			String ctoken  = parser.getNextToken(); //=
			ctoken  = parser.getNextToken();
			String ttoken  = parser.getNextToken(); //TAXA
			if ("Taxa".equalsIgnoreCase(ttoken)){
				parser.getNextToken(); //=
				ttoken  = parser.getNextToken(); //TAXA block (optional)
				if (!StringUtil.blank(ttoken)){
					Taxa t = getProject().findTaxa(file, ttoken); /*getTaxaLastFirst(ttoken);

					if (t==null){
						int wt = MesquiteInteger.fromString(ttoken);
						if (MesquiteInteger.isCombinable(wt))
							t = getProject().getTaxa(wt-1);
					}
					if (t == null && getProject().getNumberTaxas(file)==1){
						t = getProject().getTaxa(file, 0);
					}
					if (t == null && getProject().getNumberTaxas()==1)
						t = getProject().getTaxa(0); */
					if (t!=null) {
						nBlock.setDefaultTaxa(t);
					}
					else
						return false;
				}
			}

			CharacterData t = getProject().getCharacterMatrixReverseOrder(ctoken);
			if (t==null){
				int wt = MesquiteInteger.fromString(ctoken);
				if (MesquiteInteger.isCombinable(wt))
					t = getProject().getCharacterMatrix(nBlock.getDefaultTaxa(), wt-1);
			}
			if (t == null && getProject().getNumberCharMatrices(file)==1){
				t = getProject().getCharacterMatrix(file, 0);
			}
			if (t!=null) {
				nBlock.setDefaultCharacters(t);
				return true;
			}
			else
				return false;
		}


		return false;
	}
	/*.................................................................................................................*/
	public void setUpListening(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqRegistryMatrix(data)) {  // listen to registryData rather than editedData as the former needs to be changed first before the contigMapper can be updated
				data.addListener(this);
			}
			else if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {  // listen to registryData rather than editedData as the former needs to be changed first before the contigMapper can be updated
				data.addListener(this);
			}
		}
	}
	/*.................................................................................................................*/
	public void removeListening(MesquiteFile f) {
		if (f==null)
			return;
		if (f.getProject()==null)
			return;
		ListableVector matrices = f.getProject().getCharacterMatrices();
		for (int i=0; i<matrices.size(); i++) {
			CharacterData data = (CharacterData)matrices.elementAt(i);
			if (ChromaseqUtil.isChromaseqRegistryMatrix(data)) {  // listen to registryData rather than editedData as the former needs to be changed first before the contigMapper can be updated
				data.removeListener(this);
			}
			else if (ChromaseqUtil.isChromaseqEditedMatrix(data)) {  // listen to registryData rather than editedData as the former needs to be changed first before the contigMapper can be updated
				data.removeListener(this);
			}
		}
	}
	/*.................................................................................................................*/
	public void dispose() {
		super.dispose();
	}

	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		if (f== null || f.getProject() == null)
			return;
		//convertOldToNew();
		setUpListening(f);
		NexusBlock[] bs = getProject().getNexusBlocks(ChromaseqBlock.class, f); 
		if ((bs == null || bs.length ==0)){
			ChromaseqBlock ab = new ChromaseqBlock(f, this);
			ab.setVersion(ChromaseqUtil.CHROMASEQBLOCKVERSION);
			ab.setBuild(ChromaseqUtil.ChromaseqBuild);
			ab.setRegistrationBuild(ChromaseqUtil.ChromaseqRegistrationBuild);
			numBlocks++;
			addNEXUSBlock(ab);
		}
		MesquiteTrunk.resetMenuItemEnabling();
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new ChromaseqBlockTest();}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){

		String commandString;
		NexusBlock b=new ChromaseqBlock(file, this);
		((ChromaseqBlock)b).setVersion(ChromaseqUtil.CHROMASEQBLOCKVERSION);
		MesquiteString comment = new MesquiteString();
		int version = 0;

		while (!StringUtil.blank(commandString = block.getNextFileCommand(comment))) {
			readNexusCommand(file, b, commandString,  comment,  fileReadingArguments);
		}
		return b;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Manage CHROMASEQ blocks";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages CHROMASEQ block in NEXUS file." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -1000;  
	}
}



/* ======================================================================== */
class ChromaseqBlockTest extends NexusBlockTest  {
	public ChromaseqBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("CHROMASEQ");
	}
}

/* ======================================================================== */
class ChromaseqBlock extends NexusBlock {
	int version = 1;
	int build = 1;
	int registrationBuild = 1;
	ManageChromaseqBlock ownerModule;
	MesquiteFile f;

	public ChromaseqBlock(MesquiteFile f, ManageChromaseqBlock mb){
		super(f, mb);
		ownerModule = mb;
		version = ChromaseqUtil.CHROMASEQBLOCKVERSION;
		build = ChromaseqUtil.ChromaseqBuild;
		registrationBuild = ChromaseqUtil.ChromaseqRegistrationBuild;
		this.f = f;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public boolean contains(FileElement e) {
		return false;  
	}

	public void written() {
	}
	public String getName(){
		return "CHROMASEQ block";
	}
	public boolean mustBeAfter(NexusBlock block){
		return (block.getBlockName().equalsIgnoreCase("TAXA") ||  block.getBlockName().equalsIgnoreCase("CHARACTERS") ||  block.getBlockName().equalsIgnoreCase("SETS") ||  block.getBlockName().equalsIgnoreCase("LABELS"));
	}
	public String getBlockName(){
		return "CHROMASEQ";
	}
	public String getNEXUSBlock(){
		String contents = ownerModule.writeNexusCommands(f);
		//String contents = ownerModule.getBlockContents();
		if (StringUtil.blank(contents))
			return null;
		String blocks="BEGIN CHROMASEQ;" + StringUtil.lineEnding();
		blocks += "\tVERSION " + version+ ";" + StringUtil.lineEnding();
		blocks += "\tBuild " + build+ ";" + StringUtil.lineEnding();
		blocks += "\tRegistrationBuild " + registrationBuild+ ";" + StringUtil.lineEnding();
		blocks += contents;
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}
	public int getBuild() {
		return build;
	}
	public void setBuild(int build) {
		this.build = build;
	}
	public int getRegistrationBuild() {
		return registrationBuild;
	}
	public void setRegistrationBuild(int registrationBuild) {
		this.registrationBuild = registrationBuild;
	}
}
