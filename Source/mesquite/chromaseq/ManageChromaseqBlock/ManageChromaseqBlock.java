/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.
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
import mesquite.lib.duties.*;
import mesquite.chromaseq.lib.*;

public class ManageChromaseqBlock extends FileInit {
	
	public static final int CHROMASEQBLOCKVERSION = 2;

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
		return true;
	}
	/*.................................................................................................................*
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot(); 
  	 	temp.addLine("addAuthorNameToMatrices");
 	 	return temp;
  	 }
	/*.................................................................................................................*/
	void reportAttached(Associable a, String nr){
		if (a.getAttachment(nr) != null)
			Debugg.println("YES attached [" + nr + " = " + a.getAttachment(nr) + "] " +   a.getName());
		else
			Debugg.println("NO attached [" + nr + "] " + a.getName());
	}
	void reportObject(Associable a, NameReference nr){
		if (a.anyAssociatedObject(nr))
			Debugg.println("YES object [" + nr.getValue() + " ; 0 = " + a.getAssociatedObject(nr, 0)+ "] " + a.getName());
		else
			Debugg.println("NO object [" + nr.getValue() + "] " + a.getName());
	}
	void reportLong(Associable a, NameReference nr){
		if (a.getWhichAssociatedLong(nr) == null)
			Debugg.println("NO long [" + nr.getValue() + " ; 0 = " + MesquiteLong.toString(a.getAssociatedLong(nr, 0))+ "] " + a.getName());
		else
			Debugg.println("YES long [" + nr.getValue() + "] " + a.getName());
	}
	void reportDouble(Associable a, NameReference nr){
		if (a.getWhichAssociatedDouble(nr) == null)
			Debugg.println("NO double [" + nr.getValue() + " ; 0 = " + MesquiteDouble.toString(a.getAssociatedDouble(nr, 0))+ "] " + a.getName());
		else
			Debugg.println("YES double [" + nr.getValue() + "] " + a.getName());
	}
	void reportCellObjects(mesquite.lib.characters.CharacterData data, NameReference nr){
		if (data.getWhichCellObjects(nr) == null)
			Debugg.println("NO cell objects [" + nr.getValue() + " ; 0,0 = " + data.getCellObject(nr, 0,0)+ "] " + data.getName());
		else
			Debugg.println("YES cell objects [" + nr.getValue() + "] " + data.getName());
	}
	void convertOldToNew(){
		MesquiteProject proj = getProject();
		int numT = proj.getNumberTaxas();
		for (int i = 0; i<numT; i++){
			Debugg.println("=====TAXA=======");
			Taxa taxa = proj.getTaxa(i);
			reportObject(taxa, ChromaseqUtil.voucherCodeRef);
			reportObject(taxa, ChromaseqUtil.voucherDBRef);
			reportObject(taxa, ChromaseqUtil.origTaxonNameRef);
		}
		int numM = proj.getNumberCharMatrices();
		for (int i = 0; i<numM; i++){
			mesquite.lib.characters.CharacterData data = proj.getCharacterMatrix(i);
			Debugg.println("=========MATRIX=======" + data.getName());
			reportAttached(data, ChromaseqUtil.PHPHIMPORTIDREF);
			reportAttached(data, ChromaseqUtil.GENENAMEREF);
			reportAttached(data, ChromaseqUtil.PHPHMQVERSIONREF);
			reportAttached(data, ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF);
			reportLong(data,ChromaseqUtil.trimmableNameRef);
			Associable tInfo = data.getTaxaInfo(false);
			if (tInfo == null)
				Debugg.println("NO TINFO  " + data.getName());
			else {
				reportLong(tInfo, ChromaseqUtil.trimmableNameRef);
				reportLong(tInfo, ChromaseqUtil.chromatogramsExistRef);
				reportLong(tInfo, ChromaseqUtil.whichContigRef);
				reportLong(tInfo, ChromaseqUtil.startTrimRef);
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
	void writeAttached(StringBuffer sb, Associable a, String nr){
		MesquiteString ms = ChromaseqUtil.getStringAttached(a,nr);
		if (ms == null)
			return;
		if (ms.getValue() != null)
			sb.append("\tattach  ref = " +  ParseUtil.tokenize(nr) + " s = " + ParseUtil.tokenize(ms.getValue()) + ";" + StringUtil.lineEnding());
	}
	/*----------------------------------------*/
	void writeStrings(StringBuffer sb, Associable a, NameReference nr){
		if (a.anyAssociatedObject(nr)){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				String value = ChromaseqUtil.getStringAssociated(a,nr, i);
				
				if (value != null)
					sb.append("\tasString  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()) + " string = " + ParseUtil.tokenize(value) + ";" + StringUtil.lineEnding());
				
			}
		}
	}
	/*----------------------------------------*/
	void writeStringArrays(StringBuffer sb, Associable a, NameReference nr){
		if (a.anyAssociatedObject(nr)){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				String[] value = ChromaseqUtil.getStringsAssociated(a,nr, i);
				
				if (value != null){
					sb.append("\tasStrings  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()));
					for (int k=0; k< value.length; k++)
						sb.append(" string = " + ParseUtil.tokenize(value[k]));
					sb.append(";" + StringUtil.lineEnding());
				}
				
			}
		}
	}
	/*----------------------------------------*/
	void writeLongs(StringBuffer sb, Associable a, NameReference nr){
		if (a.getWhichAssociatedLong(nr) != null){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				long value = ChromaseqUtil.getLongAssociated(a,nr, i);

				if (MesquiteLong.isCombinable(value))
					sb.append("\tasLong  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()) + " long = " + value + ";" + StringUtil.lineEnding());
				
			}
		}
	}
	void writeDoubles(StringBuffer sb, Associable a, NameReference nr){
		if (a.getWhichAssociatedDouble(nr) != null){
			for (int i= 0; i< a.getNumberOfParts(); i++){
				double value = ChromaseqUtil.getDoubleAssociated(a,nr, i);

				if (MesquiteDouble.isCombinable(value))
					sb.append("\tasDouble  index = " + i +  " ref = " +  ParseUtil.tokenize(nr.getValue()) + " double = " + value + ";" + StringUtil.lineEnding());
				
			}
		}
	}
	void writeCellObjects(StringBuffer sb, mesquite.lib.characters.CharacterData data, NameReference nr){
		if (data.getWhichCellObjects(nr) == null)
			Debugg.println("NO cell objects [" + nr.getValue() + " ; 0,0 = " + data.getCellObject(nr, 0,0)+ "] " + data.getName());
		else
			Debugg.println("YES cell objects [" + nr.getValue() + "] " + data.getName());
	}
	/*----------------------------------------*/
	String getBlockContents(){
		MesquiteProject proj = getProject();
		StringBuffer sb = new StringBuffer();
		int numT = proj.getNumberTaxas();
		for (int i = 0; i<numT; i++){
			Taxa taxa = proj.getTaxa(i);
			sb.append("\n\tTAXA = " + ParseUtil.tokenize(taxa.getName()) + " ;" + StringUtil.lineEnding());
			writeStrings(sb, taxa, ChromaseqUtil.voucherCodeRef);
			writeStrings(sb,taxa, ChromaseqUtil.voucherDBRef);
			writeStrings(sb,taxa, ChromaseqUtil.origTaxonNameRef);
		}
		int numM = proj.getNumberCharMatrices();
		for (int i = 0; i<numM; i++){
			mesquite.lib.characters.CharacterData data = proj.getCharacterMatrix(i);
			sb.append("\n\tCHARACTERS = " + ParseUtil.tokenize(data.getName()) + " ;" + StringUtil.lineEnding());
			writeAttached(sb,  data, ChromaseqUtil.PHPHIMPORTIDREF);
			writeAttached(sb,  data, ChromaseqUtil.GENENAMEREF);
			writeAttached(sb,  data, ChromaseqUtil.PHPHMQVERSIONREF);
			writeAttached(sb,  data, ChromaseqUtil.PHPHIMPORTMATRIXTYPEREF);
			writeLongs(sb, data,ChromaseqUtil.trimmableNameRef);
			Associable tInfo = data.getTaxaInfo(false);
			if (tInfo != null) {
				sb.append("\n\tTAXAINFO" + " ;" + StringUtil.lineEnding());
				writeLongs(sb, tInfo, ChromaseqUtil.trimmableNameRef);
				writeLongs(sb, tInfo, ChromaseqUtil.chromatogramsExistRef);
				writeLongs(sb, tInfo, ChromaseqUtil.whichContigRef);
				writeLongs(sb, tInfo, ChromaseqUtil.startTrimRef);
				writeDoubles(sb, tInfo, ChromaseqUtil.qualityNameRef);
				writeStrings(sb,tInfo, ChromaseqUtil.aceRef);
				writeStrings(sb,tInfo, ChromaseqUtil.chromatogramReadsRef);
				writeStringArrays(sb,tInfo, ChromaseqUtil.origReadFileNamesRef);
				writeStringArrays(sb,tInfo, ChromaseqUtil.primerForEachReadNamesRef);
				writeStringArrays(sb,tInfo, ChromaseqUtil.sampleCodeNamesRef);
				writeStringArrays(sb,tInfo, ChromaseqUtil.sampleCodeRef);
			}
			//reportCellObjects(data,ChromaseqUtil.paddingRef);
			//reportCellObjects(data,ChromaseqUtil.trimmableNameRef);
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		if (f== null || f.getProject() == null)
			return;
		//convertOldToNew();
		NexusBlock[] bs = getProject().getNexusBlocks(ChromaseqBlock.class, f); 
		if ((bs == null || bs.length ==0)){
			ChromaseqBlock ab = new ChromaseqBlock(f, this);
			ab.setVersion(CHROMASEQBLOCKVERSION);
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
		((ChromaseqBlock)b).setVersion(CHROMASEQBLOCKVERSION);
		MesquiteString comment = new MesquiteString();

		while (!StringUtil.blank(commandString = block.getNextFileCommand(comment))) {
			String commandName = parser.getFirstToken(commandString);

			if (commandName.equalsIgnoreCase("VERSION")) {
				String token  = parser.getNextToken();
				int vers = MesquiteInteger.fromString(token);
				if (MesquiteInteger.isCombinable(vers)){
				}
				

			}
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
		return NEXTRELEASE;  
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
	ManageChromaseqBlock ownerModule;

	public ChromaseqBlock(MesquiteFile f, ManageChromaseqBlock mb){
		super(f, mb);
		ownerModule = mb;
		version = mb.CHROMASEQBLOCKVERSION;
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
		return false;
	}
	public String getBlockName(){
		return "CHROMASEQ";
	}
	public String getNEXUSBlock(){
		String blocks="BEGIN CHROMASEQ;" + StringUtil.lineEnding();
		blocks += "\tVERSION " + version+ ";" + StringUtil.lineEnding();
		blocks += ownerModule.getBlockContents();
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}
}
