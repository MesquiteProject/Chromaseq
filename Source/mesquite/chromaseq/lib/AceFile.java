/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import java.awt.*;import java.io.File;import mesquite.lib.*;import mesquite.lib.characters.CharacterData;import mesquite.lib.duties.FileCoordinator;import mesquite.align.lib.PairwiseAligner;import mesquite.categ.lib.*;import mesquite.cont.lib.*;import mesquite.meristic.lib.*;/* ======================================================================== */public class AceFile { 	int numContigs = 0;	int numReads = 0;	String aceFilePath = null;	String directoryPath = null;	String relativeAceFilePath = null;	String processedRelativeAceFilePath = null;	String fileName = null;	String baseName = null;	String longSequenceName = null;	boolean processPolymorphisms = false;	MesquiteModule ownerModule;	String aceFile=null;	StringBuffer remainingTags = new StringBuffer();	double polyThreshold = 0.5;	Contig[] contigs;	Read[] reads;	public static ColorRecord colorHighQuality, colorMediumQuality ,colorHighQualitySingleRead, colorMediumQualitySingleRead,colorLowQualitySingleRead,colorLowQuality, colorInapplicable, colorChromNoSeq, colorAmbiguous;	public static ColorRecord colorTrimmable, colorManuallyChanged, colorPleaseRecheck, colorPleaseRecheck2, colorPleaseRecheck3, colorNoQuality;	static{		colorHighQuality = new ColorRecord(ColorDistribution.veryLightGreen, "Higher quality (50 or above; paler = better)");		colorMediumQuality = new ColorRecord(Color.green, "Medium quality (30 to 50; darker = worse)");		colorLowQuality = new ColorRecord(ColorDistribution.lightBlue, "Lower quality (20 to 30; darker = worse)");		colorHighQualitySingleRead = new ColorRecord(ColorDistribution.lightOrange, "Higher quality single read (50 or above; paler = better)");		colorMediumQualitySingleRead = new ColorRecord(ColorDistribution.orange, "Medium quality single read (30 to 50; darker = worse)");		colorLowQualitySingleRead = new ColorRecord(ColorDistribution.lightPurple, "Lower quality single read (20 to 30; darker = worse)");		//colorInapplicable = new ColorRecord(ColorDistribution.straw, "Inapplicable or no data (Gaps)");		colorInapplicable = new ColorRecord(ColorDistribution.veryVeryLightGray, "Inapplicable or no data (Gaps)");		colorChromNoSeq = new ColorRecord(Color.pink, "No sequence retrieved, but chromatograms are available.");		colorAmbiguous = new ColorRecord(Color.darkGray, "Ambiguous coding");		colorTrimmable = new ColorRecord(Color.red, "To Be Trimmed");		colorManuallyChanged = new ColorRecord(Color.yellow, "Modified by hand");		colorPleaseRecheck = new ColorRecord(Color.magenta, "Please Recheck");  		colorPleaseRecheck2 = new ColorRecord( new Color(250, 132, 26), "Please Recheck (2)");		colorPleaseRecheck3 = new ColorRecord( new Color(30, 250, 100), "Please Recheck (3)");		colorNoQuality = new ColorRecord(Color.gray, "Sequence present but no quality score");	}	/*.................................................................................................................*/	public  AceFile (String aceFileToReadPath,  String processedAceFilePath,  String dataFilePath, String originalDataFilePath, MesquiteModule ownerModule,  boolean processPolymorphisms, double polyThreshold, boolean appended){		if (StringUtil.blank(dataFilePath))			relativeAceFilePath = aceFileToReadPath;		else			relativeAceFilePath = MesquiteFile.decomposePath(dataFilePath, aceFileToReadPath);		if (appended && StringUtil.notEmpty(processedAceFilePath))			processedRelativeAceFilePath = MesquiteFile.decomposePath(originalDataFilePath, processedAceFilePath);		else if (StringUtil.blank(processedAceFilePath))			processedRelativeAceFilePath = relativeAceFilePath;		else if (StringUtil.blank(dataFilePath))			processedRelativeAceFilePath = processedAceFilePath;		else 			processedRelativeAceFilePath = MesquiteFile.decomposePath(dataFilePath, processedAceFilePath);		this.aceFilePath = aceFileToReadPath;		this.ownerModule = ownerModule;  // note:  might be null!		this.polyThreshold = polyThreshold;		this.processPolymorphisms = processPolymorphisms;		aceFile = MesquiteFile.getFileContentsAsString(aceFileToReadPath);		directoryPath = MesquiteFile.getDirectoryPathFromFilePath(aceFileToReadPath);		fileName = MesquiteFile.getFileNameFromFilePath(aceFileToReadPath);		readAceFile();	}	/*.................................................................................................................*/	public static AceFile getAceFile(String directoryName, MesquiteModule ownerModule, DNAData data, int it) {		String processedPath = ChromaseqUtil.getAceFilePath(directoryName, ownerModule, data, it, false);		String originalPath = ChromaseqUtil.getAceFilePath(directoryName, ownerModule, data, it, true);		return new AceFile(processedPath, processedPath, null, null, ownerModule,  true, 0.5, false);	}	/*.................................................................................................................*/	public static boolean hasAceFilePath(DNAData data, int it) {		if (data==null)			return false;		Associable tInfo = data.getTaxaInfo(false);		if (tInfo == null)			return false;		String path = ChromaseqUtil.getStringAssociated(tInfo, ChromaseqUtil.aceRef, it);		return (StringUtil.notEmpty(path));	}	/*.................................................................................................................*/	public static boolean hasAceFile(MesquiteModule ownerModule, DNAData data, int it) {		if (data==null)			return false;		Associable tInfo = data.getTaxaInfo(false);		if (tInfo == null)			return false;		String filePath = ChromaseqUtil.getStringAssociated(tInfo, ChromaseqUtil.aceRef, it);		String fullPath = MesquiteFile.composePath(ownerModule.getProject().getHomeFile().getDirectoryName(), filePath);		boolean hasFile = MesquiteFile.fileExists(fullPath);		if (!hasFile) {			ownerModule.logln("Ace file cannot be found: "+fullPath);		}		return hasFile;	}	/*.................................................................................................................*/	public static AceFile getAceFile(MesquiteFile file, MesquiteModule ownerModule, DNAData data, int it) {		return getAceFile(file.getDirectoryName(),ownerModule,data,it);	}	/*.................................................................................................................*/	public static AceFile getAceFile(MesquiteModule ownerModule, DNAData data, int it) {		return getAceFile(ownerModule.getProject().getHomeFile().getDirectoryName(),ownerModule,data,it);	}	/*.................................................................................................................*/	public void setBaseName(String name){		baseName = name;	}	/*.................................................................................................................*/	public void setLongSequenceName(String name){		longSequenceName = name;	}	/*.................................................................................................................*/	public static void getChromatogramFileNames(String aceFilePath, StringArray chromatoPathArray){		if (chromatoPathArray==null)			return;		String aceFile = MesquiteFile.getFileContentsAsString(aceFilePath);		Parser aceParser = new Parser(aceFile);		Parser lineParser = new Parser(); 		lineParser.setPunctuationString("");		String line;		aceParser.setPosition(0);		line = aceParser.getRawNextLine();  // get first line		while (aceParser.getPosition()<aceParser.getString().length() ||  !aceParser.atEnd()) {			if (!StringUtil.blank(line)) {				lineParser.setString(line);				String firstToken = lineParser.getFirstToken();				if (firstToken.equals("DS")) {					lineParser.getUnalteredToken(false);  // skip over "CHROMAT_FILE:"					String chromFileName = lineParser.getUnalteredToken(false);  					String directoryPath = MesquiteFile.getDirectoryPathFromFilePath(aceFilePath);					chromatoPathArray.addAndFillNextUnassigned(directoryPath + MesquiteFile.fileSeparator+chromFileName);					lineParser.getUnalteredToken(false);  // skip over "PHD_FILE:"					String phdFile = lineParser.getUnalteredToken(false);  				}			}			line = aceParser.getRawNextLine();  // get next line		}	}	/*.................................................................................................................*/	public void readAceFile(){//		MesquiteTimer timer;  long time=0;  long cumulTime=0;//		timer = new MesquiteTimer();//		timer.start();//		String baseName = fileName.substring(0,fileName.length()-4);  //this is the name of the sequence		Parser aceParser = new Parser(aceFile);		Parser lineParser = new Parser(); 		lineParser.setPunctuationString("");		String line;		aceParser.setPosition(0);//		int count = 1;		line = aceParser.getRawNextLine();  // get first line		int currentContig = -1;		int currentRead = -1;		while (aceParser.getPosition()<aceParser.getString().length() ||  !aceParser.atEnd()) {			if (!StringUtil.blank(line)) {				lineParser.setString(line);				String firstToken = lineParser.getFirstToken();				if (firstToken.equals("AS")) {					numContigs = MesquiteInteger.fromString(lineParser.getNextToken());  					numReads = MesquiteInteger.fromString(lineParser.getNextToken());  					contigs = new Contig[numContigs];					//reads = new Read[numReads];					currentContig = -1;					currentRead = -1;				}				else if (firstToken.equals("CO")) {  //contig					currentContig++;					String contigName = lineParser.getUnalteredToken(false);  //contigName					String s = lineParser.getNextToken();					int numBases = MesquiteInteger.fromString(s);     //DRM: are padded positions included?					//	int numBases = 873;					int numReadsInContig = MesquiteInteger.fromString(lineParser.getNextToken());  					int numBaseSegmentsInContig = MesquiteInteger.fromString(lineParser.getNextToken());  					s = lineParser.getNextToken();  // U or C					if (MesquiteInteger.isCombinable(numBases)) {						String bases = aceParser.getNextDarkChars(numBases);						contigs[currentContig] = new Contig(contigName, numBases, numReadsInContig, numBaseSegmentsInContig, bases);											}				}				else if (firstToken.equals("BQ")) {  //baseQuality					contigs[currentContig].processBQ(aceParser);				}				else if (firstToken.equals("AF")) {    // first time details about reading frame have been encountered					String readName = lineParser.getUnalteredToken(false);  					boolean complemented = "C".equalsIgnoreCase(lineParser.getNextToken());					int frameStart = MesquiteInteger.fromString(lineParser);					contigs[currentContig].processAF(readName, complemented,frameStart);					//now enter this into contigs				}				else if (firstToken.equals("BS")) {  					int firstBase = MesquiteInteger.fromString(lineParser.getNextToken());					int lastBase = MesquiteInteger.fromString(lineParser.getNextToken());					String name = lineParser.getUnalteredToken(false);  					contigs[currentContig].processBS(name, firstBase, lastBase);				}				else if (firstToken.equals("RD")) {   //read					String readName = lineParser.getUnalteredToken(false);  //readName					int numPaddedBases = MesquiteInteger.fromString(lineParser.getNextToken());     					int numWholeReadInfoItems = MesquiteInteger.fromString(lineParser.getNextToken());  					int numReadTags = MesquiteInteger.fromString(lineParser.getNextToken());  					currentRead = -1;					if (MesquiteInteger.isCombinable(numPaddedBases)) {						String bases = aceParser.getNextDarkChars(numPaddedBases);						contigs[currentContig].processRD(readName, numPaddedBases, numWholeReadInfoItems, numReadTags, bases);						currentRead = contigs[currentContig].getReadNumber(readName);					}				}				else if (firstToken.equals("QA")) {					int qualClipStart = MesquiteInteger.fromString(lineParser.getNextToken());     					int qualClipEnd = MesquiteInteger.fromString(lineParser.getNextToken());     					int alignClipStart = MesquiteInteger.fromString(lineParser.getNextToken());     					int alignClipEnd = MesquiteInteger.fromString(lineParser.getNextToken());     					if (currentRead>=0) {						contigs[currentContig].processQA(currentRead, qualClipStart, qualClipEnd, alignClipStart, alignClipEnd);					}				}				else if (firstToken.equals("DS")) {//					time  += timer.timeSinceLast();					lineParser.getUnalteredToken(false);  // skip over "CHROMAT_FILE:"					String chromFileName = lineParser.getUnalteredToken(false);  					lineParser.getUnalteredToken(false);  // skip over "PHD_FILE:"					String phdFile = lineParser.getUnalteredToken(false);  					String DSRemainder = lineParser.getRemaining();					if (currentRead>=0)						contigs[currentContig].processDS(currentRead, chromFileName, directoryPath, phdFile, DSRemainder, processPolymorphisms, polyThreshold);//					cumulTime  += timer.timeSinceLast();					//MesquiteModule windowServer = ownerModule.hireNamedEmployee ( WindowHolder.class, "#WindowBabysitter");					//ChromatogramWindow.showChromatogram(new MesquiteString(directoryPath), new MesquiteString(chromFileName), windowServer, ownerModule);				}				else {					remainingTags.append(line+StringUtil.lineEnding());				}			}			//else			//	remainingTags.append(line + StringUtil.lineEnding());			line = aceParser.getRawNextLine();  // get next line		}		for (int i = 0; i<numContigs; i++) {			contigs[i].postReadProcessing();		}	}	/*.................................................................................................................*/	public String toString( boolean usePolyBases){		StringBuffer sb = new StringBuffer(1000);		sb.append("AS " + numContigs + " " + numReads + StringUtil.lineEnding()+StringUtil.lineEnding());		for (int i = 0; i<numContigs; i++) {			sb.append(contigs[i].toString(usePolyBases)+StringUtil.lineEnding()+StringUtil.lineEnding());			sb.append(contigs[i].extrasToString());			sb.append(StringUtil.lineEnding());		}		sb.append(remainingTags.toString());		return sb.toString();	}	/*.................................................................................................................*/	public String toFASTAString( boolean usePolyBases, int qualThresholdForTrim){		StringBuffer sb = new StringBuffer(1000);		if (numContigs==1)			sb.append(contigs[0].toFASTAString(usePolyBases, qualThresholdForTrim, longSequenceName)+StringUtil.lineEnding()+StringUtil.lineEnding());		else			for (int i = 0; i<numContigs; i++) {				String name = getContigNameForFASTAFile(i);				sb.append(contigs[i].toFASTAString(usePolyBases, qualThresholdForTrim, name)+StringUtil.lineEnding()+StringUtil.lineEnding());			}		return sb.toString();	}	public String getContigNameForFASTAFile(int i) {		String name = longSequenceName;		if (i > 0) {			name +=" contig " +  Integer.toString(i+1);		}		return name;	}	/*.................................................................................................................*/	public int getNumContigs(){		return numContigs;	}	/*.................................................................................................................*/	public int getNumReads() {		int count=0;		for (int i = 0; i<numContigs; i++) {			count+=contigs[i].getNumReadsToShow(); 		}		return count;	}	/*.................................................................................................................*/	public void setNameTranslation(String[][] fileNameTranslation){		for (int i = 0; i<numContigs; i++) {			for (int j=0; j<contigs[i].getNumReadsToShow(); j++){				Read read = contigs[i].getRead(j);				if (read!=null) {					String s = read.getName();					for (int k = 0; k<fileNameTranslation[0].length; k++)						if (fileNameTranslation[0][k]!=null && fileNameTranslation[0][k].equalsIgnoreCase(s)) {  // compare to new file name							read.setOriginalName(fileNameTranslation[1][k]);							read.setPrimerName(fileNameTranslation[2][k]);							read.setSampleCodeSource(fileNameTranslation[3][k]);							read.setSampleCode(fileNameTranslation[4][k]);						}//					fileNameTranslation[0][i] = newFileName;//fileNameTranslation[1][i] = chromFileName;				}			}		}	}	/*.................................................................................................................*/	public String getSampleCode() {		String code = null;		for (int i = 0; i<numContigs; i++) {			for (int j=0; j<contigs[i].getNumReadsToShow(); j++){				Read read = contigs[i].getRead(j);				if (read!=null) {					String s = read.getSampleCode();					if (code==null)						code = s;					if (!code.equalsIgnoreCase(s))						return null;				}			}		}		return code;	}	/*.................................................................................................................*/	public void createEmptyContigs(int numContigs){		this.numContigs = numContigs;		contigs = new Contig[numContigs];		String name = "contig";		for (int i = 0; i<numContigs; i++) {  			if (numContigs>1)				name = " contig " +  Integer.toString(i+1);			contigs[i] = new Contig(name, 0, 1, 0, "");		}		//   		= new Contig(contigName, numBases, numReadsInContig, numBaseSegmentsInContig, bases);	}	/*.................................................................................................................*/	public void addPhdFileAsSingleReadInContig(int currentRead, String directoryPath, String phdFileName, boolean processPolymorphisms, double polyThreshold){		if (numContigs>=1 && currentRead<contigs.length){			contigs[currentRead].processAF("",false,1);  // framestart needs to be 1			contigs[currentRead].processDS(0, null, directoryPath+MesquiteFile.fileSeparator, phdFileName, "", processPolymorphisms, polyThreshold);			numReads++;		}	}	/*.................................................................................................................*/	public void importSequence(Taxa taxa, DNAData data, int it, DNAData originalData, ContinuousData qualityData, MeristicData registrationData, boolean useExistingTaxonIfFound, boolean usePolyBases, MesquiteInteger maxChar,  String suffix, boolean appendPrimerNameForSingleReads, MesquiteString voucherCode){		String name = "";		if (numContigs>1)			for (int i = 0; i<numContigs; i++) {  				if (appendPrimerNameForSingleReads)					name = longSequenceName + " " + contigs[i].reads[0].primerName;				else if (numContigs>1)					name = longSequenceName + suffix +  Integer.toString(i+1);				contigs[i].importSequence(taxa, it, i, data, originalData, qualityData, registrationData, useExistingTaxonIfFound, processedRelativeAceFilePath, name, usePolyBases, maxChar, voucherCode);			}		else {			contigs[0].importSequence(taxa, it, 0, data,originalData, qualityData, registrationData, useExistingTaxonIfFound,  processedRelativeAceFilePath, longSequenceName, usePolyBases,maxChar, voucherCode);			if (appendPrimerNameForSingleReads)				name = longSequenceName+ " " + contigs[0].reads[0].primerName;		}	}	/*.................................................................................................................*/	public void processFailedContig(double polyThreshold){		for (int i = 0; i<numContigs; i++) {  			contigs[i].processFailedContig(polyThreshold);		}	}	/*.................................................................................................................*/	public void renameContigs(String baseName, boolean addFragName, String fragName){		String s = baseName;		if (addFragName)			s+= "."+fragName;		if (numContigs==1)			contigs[0].setName(s);		else			for (int i = 0; i<numContigs; i++) {				contigs[i].setName(s+"." + (i+1));			}	}	/*.................................................................................................................*/	public String contigListForLog(){		StringBuffer sb = new StringBuffer(100);		for (int i = 0; i<numContigs; i++) {			if (numContigs>1)				sb.append("   " + contigs[i].getName()+StringUtil.lineEnding());			sb.append(contigs[i].getReadListForLog());		}		return sb.toString();	}	/*.................................................................................................................*/	public Contig getContig(int i){		if (i>=0 && i<numContigs)			return contigs[i];		return null;	}	/*.................................................................................................................*/	public void unTrimQA(){		for (int i = 0; i<numContigs; i++) {			contigs[i].unTrimQA();		}	}	/*.................................................................................................................*/	public void trimMixedEnds(int mixedEndThreshold, int mixedEndWindow, int qualThresholdForTrim, boolean dontTrimIfEmptySequenceResults){		for (int i = 0; i<numContigs; i++) {			contigs[i].trimMixedEnds(mixedEndThreshold, mixedEndWindow, qualThresholdForTrim,  dontTrimIfEmptySequenceResults);		}	}	/*.................................................................................................................*	public void reportRegistry(){		for (int i = 0; i<numContigs; i++) {			contigs[i].reportRegistry();		}	}	/*.................................................................................................................*/	public void processPolys(){		for (int i = 0; i<numContigs; i++) {			contigs[i].processPolys(polyThreshold);		}	}	/*.................................................................................................................*/	public void writeToPropertiesFile(StringBuffer contigPropertiesFileBuffer, String fullName){		if (contigPropertiesFileBuffer==null) return;		for (int i = 0; i<numContigs; i++) {			if (numContigs>1)				contigPropertiesFileBuffer.append(fullName + " contig " + i);			else				contigPropertiesFileBuffer.append(fullName);			contigPropertiesFileBuffer.append("\t"+contigs[i].numberStrongDoublePeaks());			contigPropertiesFileBuffer.append(StringUtil.lineEnding());		}	}	/*.................................................................................................................*/	public void setLowQualityToLowerCase( int qualThreshold){		for (int i = 0; i<numContigs; i++) {			contigs[i].setLowQualityToLowerCase(qualThreshold);		}	}	/*.................................................................................................................*/	public static Color getColorOfQuality(int quality){		if (quality < 0 || !MesquiteInteger.isCombinable(quality)) 			return colorNoQuality.getColor();		else if (quality>=50)  			return MesquiteColorTable.getDefaultColor(50, (100-quality)/2, MesquiteColorTable.GREENSCALE);		else if (quality>= 30)			return MesquiteColorTable.getDefaultColor(50, (100-quality + 29)/2, MesquiteColorTable.GREENSCALE);		else 			return MesquiteColorTable.getDefaultColor(50, (100-quality)/2, MesquiteColorTable.BLUESCALE);	}	/*.................................................................................................................*/	public static Color getColorOfQualitySingleRead(int quality){		if (quality < 0 || !MesquiteInteger.isCombinable(quality)) 			return colorNoQuality.getColor();		else if (quality>=50)  			return MesquiteColorTable.getDefaultColor(50, (100-quality)/2, MesquiteColorTable.ORANGESCALE);		else if (quality>= 30)			return MesquiteColorTable.getDefaultColor(50, (100-quality + 29)/2, MesquiteColorTable.ORANGESCALE);		else 			return MesquiteColorTable.getDefaultColor(50, (100-quality)/2, MesquiteColorTable.PURPLESCALE);	}	/*.................................................................................................................*/	public void dispose(){		if (contigs!=null)			for (int i = 0; i<numContigs; i++) {				if (contigs[i]!=null)					contigs[i].dispose();				contigs[i] = null;			}		contigs=null;//		for (int i = 0; i<numReads && i<reads.length; i++) {		//reads[i] = null;//		}	}}