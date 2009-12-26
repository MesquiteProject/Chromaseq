/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import mesquite.lib.*;import mesquite.categ.lib.*;public class Read implements Listable { 	public static final char SAMPLECODEDELIMITER = '/';	String readName;	String originalName;	String primerName;	String sampleCode;	String sampleCodeSource;	String primerToShow;	public boolean complemented;	boolean originalDirection=true;	int frameStart;	int numPaddedBases;	double averageQuality=0.0;	int numBasesHighQuality=0;	int numBasesHighQualityThreshold = 30;	String bases="";	int qualClipStart;	int qualClipEnd;	int alignClipStart;	int alignClipEnd;	String abiFile, phdFile, DSRemainder, abiFilePath;	String polyFilePath = "";	boolean polyExists;	long[] polyBases = null;  // the base at this position within the RD, but in the same orientation as the contig	// padded bases are given the value of -1	boolean[] baseIsStrongDoublePeak = null;	double [] secondaryPeakFraction = null;	String phdBases = null;	int[] unpaddedBase;	int[] paddingBefore;	int[] paddingAfter;	int[] phdQuality;	int[] phdLocation;	int[] phdBoundary;	int[] locationQuality;	int pads = 0;	Contig contig;//	RegistryCoordinator registries;	/*.................................................................................................................*/	public  Read (Contig contig, String readName, boolean complemented, int frameStart){		this.readName = readName;		this.complemented = complemented;		this.frameStart = frameStart;		this.contig = contig;//		this.registries = registries;	}	/*.................................................................................................................*/	public boolean getComplemented() {		return (complemented && originalDirection) || (!complemented && !originalDirection);	}	/*.................................................................................................................*/	public void setPrimerToShow(String primerName) {		this.primerToShow = primerName;	}	/*.................................................................................................................*/	public String getPrimerToShow() {		return primerToShow;	}	/*.................................................................................................................*/	public void setPrimerName(String primerName) {		this.primerName = primerName;	}	/*.................................................................................................................*/	public String getPrimerName() {		return primerName;	}	/*.................................................................................................................*/	public void setSampleCodeSource(String sampleCodeSource) {		this.sampleCodeSource = sampleCodeSource;	}	/*.................................................................................................................*/	public String getSampleCodeSource() {		return sampleCodeSource;	}	/*.................................................................................................................*/	public void setSampleCode(String sampleCode) {		this.sampleCode = sampleCode;	}	/*.................................................................................................................*/	public String getSampleCode() {		return sampleCode;	}	/*.................................................................................................................*/	public String getOriginalName() {		return originalName;	}	/*.................................................................................................................*/	public void setOriginalName(String originalName) {		this.originalName = originalName;	}	/*.................................................................................................................*/	public String getName() {		return readName;	}	/*.................................................................................................................*/	public boolean getPolyExists() {		return polyExists;	}	/*.................................................................................................................*/	public String getABIFile() {		return abiFile;	}	/*.................................................................................................................*/	public String getABIFilePath() {		return abiFilePath;	}	/*.................................................................................................................*/	public long getPolyBase(int i) {   // i is the position, zero-based, in the padded consensus sequence		if (polyBases!=null) {			int iBase; 			/*  The position within this read would be iBase = i - frameStart +1 IF this read had the same pads in it that the contig does.			If the pads don't match, then we need to adjust.			So, this means that within the RD itself, we want to get iBase=i-frameStart+1.  The problem is the correspondence between			the bases in the read and the bases in the .poly file.			if read is uncomplemented:			CCCCCCCCCCCCCCCCCCCCCCCCCCCCCC      // this is the contig		        RRRRRRRRRRRRRRRRRRRRRRRRRRR   // for this read, framestart is -3.  										Therefore, the 0'th base in the contig corresponds to the 										-frameStart+1 base in the read.  If there are pads in the read, then 										to get to the right element in the poly file, you just count number of unpadded bases			 */						iBase = unpaddedBase[i - frameStart+1] ;  			if (iBase>=0 && iBase<polyBases.length) {				return polyBases[iBase];			}			else				MesquiteMessage.println("getPolyBase problem: " + iBase);		}		return -1;	}	/*.................................................................................................................*/	public double getSecondaryPeakFraction(int i) {   // i is the position, zero-based, in the read		if (secondaryPeakFraction!=null) {			int iBase=i; 			int readBase = getReadBaseFromContigBase(i);			if (readBase>=0 && readBase<unpaddedBase.length)				iBase = unpaddedBase[i - frameStart+1] ;  			else				return 0.0;			if (iBase>=0 && iBase<secondaryPeakFraction.length) {				return secondaryPeakFraction[iBase];			}			else				MesquiteMessage.println("getSecondaryPeakFraction problem: " + iBase);		}		return 0.0;	}	/*.................................................................................................................*/	public double getSecondaryPeakFraction2(int i) {   // i is the position in the read		if (i<0 || i>=secondaryPeakFraction.length)			return 0.0;		else			return secondaryPeakFraction[i];	}	/*.................................................................................................................*/	public boolean getStrongDoublePeak(int i) {   // i is the position, zero-based, in the padded consensus sequence		if (baseIsStrongDoublePeak!=null) {			int iBase; 			int pos = i - frameStart+1;			if (pos<0 || pos>unpaddedBase.length-1)				return false;			iBase = unpaddedBase[i - frameStart+1] ;  			if (iBase>=0 && iBase<baseIsStrongDoublePeak.length) {				return baseIsStrongDoublePeak[iBase];			}			else				MesquiteMessage.println("getStrongDoublePeak problem: " + iBase);		}		return false;	}	/*.................................................................................................................*/	public int getBasesLength() {		return bases.length();	}	/*.................................................................................................................*/	public char getPhdBaseChar(int i) {   // i is the position, zero-based, in the padded consensus sequence		if (i<0 || i>=phdBases.length())			return ' ';		else			return phdBases.charAt(i);	}	/*.................................................................................................................*/	public int getPhdBaseQuality(int i) {   // i is the position, zero-based, in the padded consensus sequence		if (i<0 || i>=phdQuality.length)			return -1;		else			return phdQuality[i];	}	/*.................................................................................................................*/	public char getPolyBaseChar(int i) {   // i is the position, zero-based, in the padded consensus sequence		if (i<0 || i>=polyBases.length)			return getPhdBaseChar(i);		else {			String s= DNAData.getIUPACSymbol(polyBases[i]);			if (s==null || s.length()!=1)				return getPhdBaseChar(i);			else 				return s.charAt(0);		}	}	/*.................................................................................................................*/	public String getPolyBaseString(int i) {   // i is the position, zero-based, in the padded consensus sequence		if (i<0 || i>=polyBases.length)			return "";		else			return DNAData.getIUPACSymbol(polyBases[i]);	}	/*.................................................................................................................*/	public int getPhdLocation(int i, ContigDisplay window, boolean calcAverageIfZero) {   // i is the position, zero-based, in the padded consensus sequence; returns location		if (phdLocation==null)			return -(int)(window.getApproximateNumberOfPeaksVisible()*window.getAveragePeakDistance());		if (i<0)			return  (int)(i*window.getAveragePeakDistance());   //approximate		else if (i>=phdLocation.length)			return phdLocation[phdLocation.length-1]+(int)((i-phdLocation.length+1)*window.getAveragePeakDistance()); //approximate		else if (phdLocation[i]==0 && i>0 && i<phdLocation.length-1) {  			int leftNeighbor = i;			int rightNeighbor = i;			for (int j = i-1; j>=0; j--) {				if (phdLocation[j]>0) {					leftNeighbor = j;					break;				}			}			for (int j = i+1; j<phdLocation.length; j++) {				if (phdLocation[j]>0) {					rightNeighbor = j;					break;				}			}			if (leftNeighbor==rightNeighbor) return 0;			return phdLocation[leftNeighbor]+(phdLocation[rightNeighbor]-phdLocation[leftNeighbor])/2;		}		else			return phdLocation[i];	}	/*.................................................................................................................*/	public int getPhdLeftBoundary(int i, ContigDisplay window, boolean calcAverageIfZero) {   // i is the position, zero-based, in the padded consensus sequence; returns location		int loc = getPhdLocation(i,window,calcAverageIfZero);		if (i>0)			return loc-(loc-getPhdLocation(i-1,window,calcAverageIfZero))/2;		else			return loc/2;	}	/*.................................................................................................................*/	public int getPhdRightBoundary(int i, ContigDisplay window, boolean calcAverageIfZero) {   // i is the position, zero-based, in the padded consensus sequence; returns location		int loc = getPhdLocation(i,window,calcAverageIfZero);		if ( i<phdLocation.length-1)			return loc+(getPhdLocation(i+1,window,calcAverageIfZero)-loc)/2;		else			return loc+(int)window.getAveragePeakDistance()/2;	}	/*.................................................................................................................*/	public int getQualityOfLocation(int location) {   // i is the position, zero-based, in the padded consensus sequence; returns location		if (location<0 || location >=locationQuality.length)			return -1;		else			return locationQuality[location];	}	/*.................................................................................................................*/	public int getNumBasesHighQualityThreshold(){		return numBasesHighQualityThreshold;	}	/*.................................................................................................................*/	public int getNumBasesHighQuality(){		return numBasesHighQuality;	}	/*.................................................................................................................*/	public double getAverageQuality(){		return averageQuality;	}	/*.................................................................................................................*/	public  String toString (){		StringBuffer sb = new StringBuffer(numPaddedBases+20);		sb.append("RD ");		sb.append(StringUtil.blanksToUnderline(readName));    		sb.append(" " + bases.length() + " 0 0"+StringUtil.lineEnding());  //DRM don't use 0  0		for (int i = 0; i<bases.length(); i++) {			if ((i!=0) && (i%50==0))				sb.append(StringUtil.lineEnding());			sb.append(bases.charAt(i));  		}		sb.append(StringUtil.lineEnding()+StringUtil.lineEnding());		sb.append("QA " + qualClipStart + " " + qualClipEnd + " " + alignClipStart + " " + alignClipEnd);		sb.append(StringUtil.lineEnding());		sb.append("DS CHROMAT_FILE: " + StringUtil.blanksToUnderline(abiFile) + "  PHD_FILE: " + phdFile + " " + DSRemainder);		sb.append(StringUtil.lineEnding()+StringUtil.lineEnding());		/*		sb.append(StringUtil.lineEnding()+StringUtil.lineEnding() + "polybases" + StringUtil.lineEnding());		for (int i=0; i<polyBases.length; i++)  {			sb.append("" + i + "   " + DNAData.getIUPACSymbol(polyBases[i]) + StringUtil.lineEnding());		}		sb.append(StringUtil.lineEnding()+StringUtil.lineEnding());		 */		return sb.toString();	}	/*.................................................................................................................*/	public  void createArrays(int numPaddedBases){		this.numPaddedBases = numPaddedBases;		polyBases = new long[numPaddedBases];		unpaddedBase = new int[numPaddedBases];		paddingBefore = new int[numPaddedBases];		paddingAfter = new int[numPaddedBases];		phdLocation = new int[numPaddedBases];		phdBoundary = new int[numPaddedBases];		phdQuality = new int[numPaddedBases];		baseIsStrongDoublePeak = new boolean[numPaddedBases];		secondaryPeakFraction = new double[numPaddedBases];		for (int i = 0; i<numPaddedBases; i++) {			secondaryPeakFraction[i]=0.0;		}	}	/*.................................................................................................................*/	public  void processRD(int numPaddedBases, String bases, String name){		this.bases = bases;		createArrays(numPaddedBases);//		registries.createPhdRegistry(numPaddedBases,name, frameStart, bases);		phdBases = "";//		polyBases = new long[numPaddedBases];		int baseCount = 0;		pads = 0;		for (int i=0; i<bases.length(); i++)  {			if (bases.charAt(i)=='*') {				polyBases[i] = -1;				unpaddedBase[i] = baseCount;				pads++;			}			else {				polyBases[i] = DNAState.fromCharStatic(bases.charAt(i));				unpaddedBase[i] = baseCount;				baseCount++;			}			paddingBefore[i] = pads;		}		pads = 0;		for (int i=bases.length()-1; i>=0; i--)  {			if (bases.charAt(i)=='*')				pads++;			paddingAfter[i] = pads;		}	}	/*.................................................................................................................*/	public long getPolyFileLineState(Parser lineParser, int i,double polyThreshold, MesquiteDouble secondaryFraction) {		String s = lineParser.getFirstToken();		long stateSet = DNAState.fromCharStatic(s.charAt(0));		String originalBase = s;		String position = lineParser.getNextToken();  //position		double mainHeight = MesquiteDouble.fromString(lineParser.getNextToken());		s = lineParser.getNextToken();  //?number		s = lineParser.getNextToken();  //nextState		long state2;		if (secondaryFraction!=null)			secondaryFraction.setValue(0.0);		if (!s.equalsIgnoreCase("N") && !s.equalsIgnoreCase(originalBase)) {			state2 = DNAState.fromCharStatic(s.charAt(0));			position = lineParser.getNextToken();  //position			double height2 = MesquiteDouble.fromString(lineParser.getNextToken());			if (height2>=polyThreshold*mainHeight) {				stateSet |=  state2;				stateSet = CategoricalState.setUncertainty(stateSet, true);			}			if (secondaryFraction!=null)				secondaryFraction.setValue(height2/mainHeight);		}		if (complemented)			stateSet = DNAState.complement(stateSet);		return stateSet;	}	/*.................................................................................................................*/	public  int numBasesInPhdFile(String directoryPath, String phdFile, StringBuffer basesBuffer){		int count=0;		String phdFilePath = directoryPath + phdFile;		boolean phdExists = MesquiteFile.fileExists(phdFilePath);		if (phdExists) {			String phdFileContents = MesquiteFile.getFileContentsAsString(phdFilePath,-1,10000);			Parser phdParser = new Parser(phdFileContents);			Parser lineParser = new Parser();			phdParser.setPosition(0);			String line="";			while (!phdParser.atEnd() && !"BEGIN_DNA".equalsIgnoreCase(line)) {				line = phdParser.getRawNextLine();  			}			lineParser.setString(line);			while (!phdParser.atEnd() && !"END DNA".equalsIgnoreCase(lineParser.getFirstToken())) {				line = phdParser.getRawNextLine();  				lineParser.setString(line);				if (basesBuffer!=null && !"END DNA".equalsIgnoreCase(lineParser.getFirstToken())) {					basesBuffer.append(line.charAt(0));					count++;				}			}		}		qualClipEnd = count;		alignClipEnd = count;		return count;	}	/*.................................................................................................................*/	public  void processDS(String abiFile, String directoryPath, String phdFile, String DSRemainder, boolean processPolymorphisms, double polyThreshold){		boolean acquireDataFromPhDFile = StringUtil.blank(abiFile);		this.abiFile = abiFile;		this.abiFilePath = directoryPath;		this.phdFile = phdFile;		this.DSRemainder = DSRemainder;		MesquiteInteger pos = new MesquiteInteger(0);		StringBuffer phdBuffer;		if (bases==null)			phdBuffer=new StringBuffer(0);		else			phdBuffer = new StringBuffer(bases);		String phdFilePath = directoryPath + phdFile;		boolean phdExists = MesquiteFile.fileExists(phdFilePath);		if (phdExists && phdBuffer!=null) {			String phdFileContents = MesquiteFile.getFileContentsAsString(phdFilePath,-1,10000);			Parser phdParser = new Parser(phdFileContents);			phdParser.setPosition(0);			Parser lineParser = new Parser();			lineParser.setPunctuationString(":");			int maxIndex = 0;			String line="";			while (!phdParser.atEnd()) {				lineParser.setString(line);				String ds = lineParser.getFirstToken();				if ("TRACE ARRAY MAX INDEX".equalsIgnoreCase(lineParser.getFirstToken())) {					lineParser.getNextToken();  // the : 					maxIndex = MesquiteInteger.fromString(lineParser.getNextToken());  // the maximum index value					locationQuality = new int[maxIndex];					break;				}				else if ("BEGIN SEQUENCE".equalsIgnoreCase(lineParser.getFirstToken())) {					if (acquireDataFromPhDFile) {  //then we didn't get the abiFile info previously, so have to set some values						this.abiFile =  lineParser.getNextToken();						readName = StringUtil.blanksToUnderline(this.abiFile);						complemented = readName.indexOf(".g.")>0;						StringBuffer basesBuffer = new StringBuffer();						numPaddedBases = numBasesInPhdFile(directoryPath, phdFile, basesBuffer);						bases = basesBuffer.toString();						phdBuffer = new StringBuffer(bases);						createArrays(numPaddedBases);						int baseCount=0;						for (int i=0; i<bases.length(); i++)  {							polyBases[i] = DNAState.fromCharStatic(bases.charAt(i));							unpaddedBase[i] = baseCount;							baseCount++;							paddingBefore[i] = 0;														}					}				}				line = phdParser.getRawNextLine();  			}						while (!phdParser.atEnd() && !"BEGIN_DNA".equalsIgnoreCase(line)) {				line = phdParser.getRawNextLine();  			}			if (!phdParser.atEnd()) {				line = phdParser.getRawNextLine();  // get first line of data				lineParser.setString(line);				lineParser.setPunctuationString("");				if (complemented) {					for (int i=polyBases.length-1; i>=0 && !phdParser.atEnd() && (!"END_DNA".equalsIgnoreCase(line)); i--)  {						if (polyBases[i] != -1) {  // this is not a padded position, process							if (!StringUtil.blank(line)) {								String base = lineParser.getFirstToken();								if (base!=null) {									phdBuffer.setCharAt(i,base.charAt(0));									phdQuality[i] = MesquiteInteger.fromString(lineParser.getNextToken());									phdLocation[i] = maxIndex-MesquiteInteger.fromString(lineParser.getNextToken());  // as will be reverse complemented								}							}							line = phdParser.getRawNextLine();  // get next line							lineParser.setString(line);						}						else {							phdBuffer.setCharAt(i,'*');							phdQuality[i] = 0;							phdLocation[i] = 0;						}					}							}				else for (int i=0; i<polyBases.length && i<phdBuffer.length()  &&  !phdParser.atEnd() && !"END_DNA".equalsIgnoreCase(line); i++)  {					if (polyBases[i] != -1) {  // this is not a padded position, process						if (!StringUtil.blank(line)) {							String base = lineParser.getFirstToken();							if (base!=null) {								phdBuffer.setCharAt(i,base.charAt(0));								phdQuality[i] = MesquiteInteger.fromString(lineParser.getNextToken());								phdLocation[i] = MesquiteInteger.fromString(lineParser.getNextToken());							}						}						line = phdParser.getRawNextLine();  // get next line						lineParser.setString(line);					}					else {						phdBuffer.setCharAt(i,'*');						phdQuality[i] = 0;						phdLocation[i] = 0;					}				}				if (locationQuality!=null) {					int lastLoc = 0;					long totalQuality = 0;					numBasesHighQuality=0;					for (int i=0; i<phdLocation.length; i++) {						int qual = phdQuality[i];						if (qual>=numBasesHighQualityThreshold)							numBasesHighQuality++;						totalQuality+= qual;						int startLoc;						if (i<=0)							startLoc = 0;						else							startLoc = phdLocation[i]-(phdLocation[i]-phdLocation[i-1])/2;						int endLoc;						if (i>=phdLocation.length-1)							endLoc = maxIndex-1;						else							endLoc = phdLocation[i]+(phdLocation[i+1]-phdLocation[i])/2;						for (int j=startLoc; j<=endLoc && j>=0 && j <= maxIndex-1 ; j++)							locationQuality[j] = qual;					}					averageQuality=0.0;					if (phdLocation.length>0)						averageQuality = totalQuality/phdLocation.length;				}			}		}		phdBases = phdBuffer.toString();		if (complemented)			phdBases = DNAData.complementString(phdBases);		phdBases = phdBases.toUpperCase();		if (processPolymorphisms) {			polyFilePath = directoryPath + abiFile + ".poly";			polyExists = MesquiteFile.fileExists(polyFilePath);			if (polyExists && polyBases!=null) {				String polyFile = MesquiteFile.getFileContentsAsString(polyFilePath,-1,10000);				Parser polyParser = new Parser(polyFile);				polyParser.setPosition(0);				String line="";				while (StringUtil.blank(line)) 					line = polyParser.getRawNextLine();  // get first line, ignore				line = polyParser.getRawNextLine();  // get first line of data				Parser lineParser = new Parser();				lineParser.setPunctuationString("");				MesquiteBoolean isDoublePeak = new MesquiteBoolean(false);				MesquiteDouble secondaryFraction = new MesquiteDouble();				if (complemented) {					for (int i=polyBases.length-1-pads; i>=0 && !polyParser.atEnd(); i--)  {						if (!StringUtil.blank(line)) {							lineParser.setString(line);							long polyBase= getPolyFileLineState(lineParser, i, polyThreshold,  secondaryFraction);							if (polyBases[i] != -1) {								polyBases[i] = polyBase;								baseIsStrongDoublePeak[i] = secondaryFraction.getValue()>0.5 && getPhdBaseQuality(i)>10;								secondaryPeakFraction[i] = secondaryFraction.getValue();							} 						}						line = polyParser.getRawNextLine();  // get next line						//polyBases[i] = getPolyFileLineState(lineParser, i, polyThreshold,  secondaryFraction);					}							}				else for (int i=0; i<polyBases.length  && !polyParser.atEnd(); i++)  {					if (!StringUtil.blank(line)) {						lineParser.setString(line);						long polyBase= getPolyFileLineState(lineParser, i, polyThreshold,  secondaryFraction);						if (polyBases[i] != -1) {							polyBases[i] = polyBase;							baseIsStrongDoublePeak[i] = secondaryFraction.getValue()>0.5 && getPhdBaseQuality(i)>10;							secondaryPeakFraction[i] = secondaryFraction.getValue();						} 					}					line = polyParser.getRawNextLine();  // get next line				}			}		}	}	public String getSequence(){		return phdBases;	}	/*.................................................................................................................*/	public  void unTrimQA(){		if (qualClipStart>alignClipStart)			qualClipStart  = alignClipStart;//		if (alignClipStart>qualClipStart)//		alignClipStart  = qualClipStart;		if (qualClipEnd<alignClipEnd)			qualClipEnd  = alignClipEnd;//		if (alignClipEnd<qualClipEnd)//		alignClipEnd  = qualClipEnd;	}	/*.................................................................................................................*/	public  void processQA(int qualClipStart, int qualClipEnd, int alignClipStart, int alignClipEnd){		this.qualClipStart  = qualClipStart;		this.qualClipEnd  = qualClipEnd;		this.alignClipStart  = alignClipStart;		this.alignClipEnd  = alignClipEnd;		/* from the documentation:This line indicates which part of the read is the high quality segment(if there is any) and which part of the read is aligned against theconsensus.  These positions are offsets (and count *'s) from the leftend of the read (left, as shown in Consed).  Hence for bottom strandreads, the offsets are from the end of the read.  The offsets are1-based.  That is, if the left-most base is in the aligned,high-quality region, <qual clipping start> = 1 and <align clippingstart> = 1 (not zero).  If the entire read is low quality, then <qualclipping start> and <qual clipping end> will both be -1.		 */	}	/*.................................................................................................................*/	public String getAFString() {		if (complemented)			return "AF "+StringUtil.blanksToUnderline(readName) + " C " + frameStart + StringUtil.lineEnding();		else			return "AF "+StringUtil.blanksToUnderline(readName) + " U " + frameStart + StringUtil.lineEnding();	}	/*.................................................................................................................*/	public void reverseComplement(int numBasesInContig){		frameStart= numBasesInContig - (frameStart+numPaddedBases);		int temp = qualClipStart;		qualClipStart=qualClipEnd;		qualClipEnd = temp;		temp = alignClipStart;		alignClipStart = alignClipEnd;		alignClipEnd = temp;		originalDirection = !originalDirection;		int[] tempArray = new int[paddingBefore.length];		tempArray = paddingBefore;		paddingBefore= IntegerArray.reverse(paddingAfter);		paddingAfter = IntegerArray.reverse(tempArray);		unpaddedBase = IntegerArray.reverse(unpaddedBase);		phdQuality = IntegerArray.reverse(phdQuality);		phdLocation = IntegerArray.reverse(phdLocation);		phdBoundary = IntegerArray.reverse(phdBoundary);		locationQuality = IntegerArray.reverse(locationQuality);		phdBases=DNAData.complementString(StringUtil.reverse(phdBases));		bases = DNAData.complementString(StringUtil.reverse(bases));		//need to adjust frameStart	}	/*.................................................................................................................*	public void createRegistries() {		contig.getRegistrationCoordinator().createPhdRegistry(numPaddedBases, readName);	}	/*.................................................................................................................*/	public void dispose() {	}				/*========  Following section is about registration ==========*/	/*.................................................................................................................*/	public int getFrameStart(){		return frameStart;	}	/*.................................................................................................................*/	/*This returns for read position i, what is the position in the consensus. */	public int getContigBaseFromReadBase(int i){		return i+frameStart-1; // - contig.getNumBasesAddedToStart();	}	/*.................................................................................................................*/	/*This returns for consensus position i, what is the position in the read.   */	public int getReadBaseFromContigBase(int i){		return i-frameStart+1;// + contig.getNumBasesAddedToStart();	}	/*.................................................................................................................*/	public int getContigPositionOfLastBase(){		return getContigBaseFromReadBase(bases.length());	}}