/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.ExportFastaDNAForGenBankTbl2Asn;/*~~  */import java.awt.*;import java.awt.event.*;import java.util.Vector;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.categ.lib.*;import mesquite.chromaseq.lib.*;import mesquite.io.lib.*;//tbl2asn -t template.sbt.txt -p submission -a s2 -V v -k m /* ============  a file interpreter for DNA/RNA  Fasta files ============*/public class ExportFastaDNAForGenBankTbl2Asn extends InterpretFasta implements ActionListener {	protected String addendum = "";	protected static String codeLabel = "Voucher";	protected boolean addVoucherNumberToDescription = false;	protected boolean featureAnnotation = false;	protected boolean saveFeatureTableEvenIfNoParts = true;	protected boolean useGroupNameAsProductName = false;	SequenceProfileManager specificationManager;	SequenceProfile sequenceProfile = null;	OTUIDCodeInfoCoord voucherInfoTask;	String sequenceProfileName;	boolean shortenInMiddle=false;	String executablePath;	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed		EmployeeNeed e = registerEmployeeNeed(OTUIDCodeInfoCoord.class, "Voucher information is needed for FASTA export for Genbank submissions.",				"This is activated automatically when you choose this exporter.");		EmployeeNeed e3 = registerEmployeeNeed(SequenceProfileManager.class, "A specifier of the nature of sequences is required.", "This is activated automatically.");	}	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		voucherInfoTask = (OTUIDCodeInfoCoord)hireEmployee(OTUIDCodeInfoCoord.class, null);		loadPreferences();		if (specificationManager == null)			specificationManager= (SequenceProfileManager)MesquiteTrunk.mesquiteTrunk.hireEmployee(SequenceProfileManager.class, "Supplier of sequence specifications.");		if (specificationManager == null) {			return false;		} 		return voucherInfoTask != null && super.startJob(arguments, condition, hiredByName);	}	/*.................................................................................................................*/	public void processSingleXMLPreference (String tag, String content) {		if ("codeLabel".equalsIgnoreCase(tag))			codeLabel= content;		if ("addVoucherNumberToDescription".equalsIgnoreCase(tag))			addVoucherNumberToDescription=MesquiteBoolean.fromTrueFalseString(content);		if ("featureAnnotation".equalsIgnoreCase(tag))			featureAnnotation=MesquiteBoolean.fromTrueFalseString(content);		if ("sequenceProfileName".equalsIgnoreCase(tag))			sequenceProfileName=content;		if ("executablePath".equalsIgnoreCase(tag)){   // it is one with the flavor attribute			executablePath = StringUtil.cleanXMLEscapeCharacters(content);		}		if ("useGroupNameAsProductName".equalsIgnoreCase(tag)){   			useGroupNameAsProductName = MesquiteBoolean.fromTrueFalseString(content);		}	}	/*.................................................................................................................*/	public String preparePreferencesForXML () {		StringBuffer buffer = new StringBuffer(60);			StringUtil.appendXMLTag(buffer, 2, "codeLabel",codeLabel);		StringUtil.appendXMLTag(buffer, 2, "addVoucherNumberToDescription",addVoucherNumberToDescription);		StringUtil.appendXMLTag(buffer, 2, "useGroupNameAsProductName",useGroupNameAsProductName);		StringUtil.appendXMLTag(buffer, 2, "featureAnnotation",featureAnnotation);		if (StringUtil.notEmpty(sequenceProfileName))			StringUtil.appendXMLTag(buffer, 2, "sequenceProfileName",sequenceProfileName);		StringUtil.appendXMLTag(buffer, 2, "executablePath", executablePath);  		return buffer.toString();	}	/*.................................................................................................................*/	public boolean canImport() {  		return false;  //	}	public boolean canImport(String arguments){		return false;	}	protected int taxonNameLengthLimit() {		return 15;	}	Choice sequenceSpecificationChoice;	ExporterDialog exportDialog;	SingleLineTextField executablePathField =  null;	/*.................................................................................................................*/	public boolean getExportOptions(CharacterData data, boolean dataSelected, boolean taxaSelected){		String[] specifications = specificationManager.getListOfProfiles();		if (specifications==null)			//	if (!specificationManager.optionsSpecified())			if (!specificationManager.queryOptions())				return false;		MesquiteInteger buttonPressed = new MesquiteInteger(1);		exportDialog = new ExporterDialog(this,containerOfModule(), "Export FASTA for GenBank via tbl2asn", buttonPressed);		exportDialog.appendToHelpString("Choose the options for exporting the matrix as a FASTA file prepared for processing by NCBI's tbl2asn.");		exportDialog.appendToHelpString("<br><br><b>SeqID Suffix</b>: this will be added to each taxon name to form the unique SeqID.");		exportDialog.appendToHelpString("<br><b>Text before OTU (Specimen) ID Code in DEFINITION</b>: this will be inserted between the organism name and the OTU (Specimen) ID Code in the DEFINITION.");		exportDialog.appendToHelpString("<br><b>Features Annotation Tables</b>: A table will be written for each sequence (see Genbank submission instructions).  Group name will become Feature's Product name.  Assumes first site in sequence in group will be representative; if has codon position, whole group will be considered CDs.");				String matrixName = "";		if (data!=null){			matrixName = data.getName();			exportDialog.addLabel("(Matrix: " + matrixName+ ")",Label.CENTER);		}		//		SingleLineTextField uniqueSuffixField = exportDialog.addTextField("SeqID Suffix", matrixName, 20);		int index = specificationManager.findSpecificationIndex(sequenceProfileName);		if (index<0) index=0;		sequenceSpecificationChoice = exportDialog.addPopUpMenu("Sequence Profile", specificationManager.getListOfProfiles(), index);		final Button manageSpecificationsButton = exportDialog.addAListenedButton("Manage...",null, this);		manageSpecificationsButton.setActionCommand("ManageSpecifications");		Checkbox addVoucherNumberBox = exportDialog.addCheckBox("add OTU (Specimen) ID Code to DEFINITION", addVoucherNumberToDescription);		SingleLineTextField codeLabelField = exportDialog.addTextField("Text before OTU (Specimen) ID Code in DEFINITION", codeLabel, 20);		Checkbox shortenInMiddleBox = exportDialog.addCheckBox("if seqID is too long, shorten in middle (as opposed to end)", shortenInMiddle);		Checkbox useGroupNameAsProductNameCheckBox = exportDialog.addCheckBox("use character group name as product name for coding regions", useGroupNameAsProductName);		exportDialog.addHorizontalLine(1);		//Checkbox includeGapsCheckBox = exportDialog.addCheckBox("include gaps", includeGaps);		//added WPM may 2012//		Checkbox featureAnnotationCheckBox = exportDialog.addCheckBox("if gaps NOT included, save Genbank Features Annotation tables? \n(requires characters partitioned into groups)", featureAnnotation);//		exportDialog.addHorizontalLine(1);		//		executablePathField = exportDialog.addTextField("Path to tbl2asn:", executablePath, 40);		//		Button browseButton = exportDialog.addAListenedButton("Browse...",null, this);		//		browseButton.setActionCommand("browse");		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);		codeLabel = codeLabelField.getText();		//		uniqueSuffix = uniqueSuffixField.getText();		addVoucherNumberToDescription = addVoucherNumberBox.getState();		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);		includeGaps=false;  // for tbl2asn, gaps will be excluded		//includeGaps = includeGapsCheckBox.getState();		useGroupNameAsProductName = useGroupNameAsProductNameCheckBox.getState();		//featureAnnotation = featureAnnotationCheckBox.getState();		featureAnnotation = true;  // always true for tbl2asn		shortenInMiddle = shortenInMiddleBox.getState();		int sequenceProfileIndex = sequenceSpecificationChoice.getSelectedIndex();		sequenceProfile = specificationManager.getSequenceProfile(sequenceProfileIndex);		sequenceProfileName = sequenceProfile.getName();		uniqueSuffix = sequenceProfile.getSeqIDSuffix();		if (executablePathField!=null)			executablePath = executablePathField.getText();		exportDialog.dispose();		if (ok) {			storePreferences();		}		return ok;	}		/*.................................................................................................................*/	public  void actionPerformed(ActionEvent e) {		if (e.getActionCommand().equalsIgnoreCase("ManageSpecifications")) {			int currentSequenceProfileIndex = sequenceSpecificationChoice.getSelectedIndex();			SequenceProfile currentSequenceProfile = specificationManager.getSequenceProfile(currentSequenceProfileIndex);			String currentSequenceProfileName = currentSequenceProfile.getName();			if (specificationManager.manageSequenceProfiles()) {				int count2 = sequenceSpecificationChoice.getItemCount();				while (sequenceSpecificationChoice.getItemCount()>0)					sequenceSpecificationChoice.remove(0);				String[] specList = specificationManager.getListOfProfiles();				if (specList!=null && specList.length>0)					for (int i=0; i<specList.length; i++)						sequenceSpecificationChoice.add(specList[i]);				if (MesquiteTrunk.isJavaGreaterThanOrEqualTo(1.8)) 					sequenceSpecificationChoice.revalidate();				int index = specificationManager.findSpecificationIndex(currentSequenceProfileName);				if (index<0) index=0;				sequenceSpecificationChoice.select(index);				sequenceSpecificationChoice.repaint();				exportDialog.prepareDialog();				exportDialog.repaint();			} 		} else if (e.getActionCommand().equalsIgnoreCase("browse")) {			MesquiteString directoryName = new MesquiteString();			MesquiteString fileName = new MesquiteString();			String path = MesquiteFile.openFileDialog("Choose tbl2asn", directoryName, fileName);			if (StringUtil.notEmpty(path))				executablePathField.setText(path);		}	}	/*.................................................................................................................*/	public int getPartials(String nameOfPart){ //00 = not partial; 01 = partial end; 10 = partial start; 11 = partial both		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExtensibleDialog partialDialog = new ExtensibleDialog(containerOfModule(), "Partial?", buttonPressed);		TextArea partialField = partialDialog.addLargeTextLabel("Is the partition \"" + nameOfPart + "\" partial on either end?");		Checkbox start = partialDialog.addCheckBox("5' Partial", true);		Checkbox end = partialDialog.addCheckBox("3' Partial", true);		partialDialog.completeAndShowDialog();		int result = 0;		if (start.getState())			result += 2;		if (end.getState())			result += 1;		partialDialog.dispose();		return result;	}		/*.................................................................................................................*/	public boolean canExportEver() {  		return true;  //	}	/*.................................................................................................................*/	public boolean canExportProject(MesquiteProject project) {  		return project.getNumberCharMatrices(DNAState.class) > 0;  //	}	/*.................................................................................................................*/	public boolean canExportData(Class dataClass) {  		return (dataClass==DNAState.class);	}	/*.................................................................................................................*/	public CharacterData createData(CharactersManager charTask, Taxa taxa) {  		return charTask.newCharacterData(taxa, 0, DNAData.DATATYPENAME);  //	}	protected String getSupplementForTaxon(Taxa taxa, int it){		if (taxa!=null && voucherInfoTask != null) {			String s = " ";			String voucherID = ChromaseqUtil.getStringAssociated(taxa, VoucherInfoFromOTUIDDB.voucherCodeRef, it);			VoucherInfoFromOTUIDDB vi= voucherInfoTask.getVoucherInfo(ChromaseqUtil.getStringAssociated(taxa, VoucherInfoFromOTUIDDB.voucherDBRef, it), voucherID);			if (vi != null) {				if (sequenceProfile!=null)					s+=  sequenceProfile.getFASTASourceModifiers()+ " ";				String gbs = vi.toGenBankString();				if (StringUtil.blank(gbs)) 					MesquiteMessage.println("Taxon with no Genbank information: " + taxa.getTaxonName(it) + "; OTU (specimen) ID code: " + voucherID);				s += gbs;				addendum = sequenceProfile.getDescription();				if (addVoucherNumberToDescription)					s+= " " + codeLabel + voucherID + " " + addendum;				else					s+= " " + addendum;				return s;			}			else				MesquiteMessage.println("Taxon with no Genbank information: " + taxa.getTaxonName(it) + "; OTU (specimen) ID code: " + voucherID);		}		return null; 	}	static int taxonNameLengthLimit = 32;	/*.................................................................................................................*/	protected String getTaxonName(Taxa taxa, int it, CharacterData data){		String s = StringUtil.cleanseStringOfFancyChars(taxa.getTaxonName(it),false,true);		String suffix = StringUtil.cleanseStringOfFancyChars(uniqueSuffix,false,true);		if (taxonNameLengthLimit>0) {			String taxnum = MesquiteInteger.toString(it);			int reduction = s.length() + suffix.length() - taxonNameLengthLimit;			if (reduction>0) {				reduction += taxnum.length();				String note = "\nSeqID (\"" + s + suffix + "\") too long; renamed as ";				if (s.length()-reduction <1)					s = taxnum;				else {					if (shortenInMiddle)						s = s.substring(0,4) + "_" + s.substring(5+reduction,s.length())+"_" + taxnum;					else						s = s.substring(0,s.length()-reduction-3) + "_"+taxnum;				}				logln(note + "\"" + s + "_"+suffix+"\"");			}		}		return s+"_"+suffix;	}	int getSequencePosition(int column, int it, DNAData data){		int count = 0;		for (int ic=0; ic<=column; ic++){			if (!data.isInapplicable(ic, it))				count++;		}		return count;	}	int getCodonPositionOfFirstSiteOfPartition(Listable[] listArray, Object obj, DNAData data, int it){		for (int i=0; i<listArray.length; i++) {			if (listArray[i]==obj && (it<0 || !data.isInapplicable(i,it))) {				int cp = data.getCodonPosition(i);				return cp;			}		}		return MesquiteInteger.unassigned;	}	int getCodonPositionOfFirstSite(DNAData data, int it){		for (int ic=0; ic<data.getNumChars(); ic++) {			if (!data.isInapplicable(ic,it)) {				return data.getCodonPosition(ic);			}		}		return MesquiteInteger.unassigned;	}	/*.................................................................................................................*	 Returns a vector of Strings, each string containing the Feature Table description of the bounds of a segment of the sequence,	 * as defined by the character groups.*	Vector getListOfSegmentsOLD(Listable[] characterObjectArray, Object characterGroupObject, int it, DNAData data, boolean partialStart, boolean partialEnd, boolean returnStartingCodonPosition) {		int continuing = 0;		String s="";		boolean found=false;		int startingCodPos = -1;		Vector v= new Vector();		int correction = 1;		int lastWritten = -1;		for (int i=0; i<characterObjectArray.length && i<data.getNumChars(); i++) {  // the list array here is the 			if (!data.isInapplicable(i, it)){  // there should be a one-to-one correspondence between this list objects attached to characters and the characters in data				if (characterObjectArray[i]==characterGroupObject) {  //  we've found one					found=true;					if (continuing == 0) {//first instance						if (partialStart)							s += "<";						s += (getSequencePosition(i, it, data)); //START						if (returnStartingCodonPosition)							v.addElement(new MesquiteInteger(data.getCodonPosition(i)));						lastWritten = i;						continuing = 1;					}					else if (continuing == 1) {  // we've already found one, so let's put in the tab						s += "\t";						continuing = 2;					}				}				else if (continuing >0) {  // we've already found one, AND this one is not one of this group, AND it is not a gap					if (lastWritten != i-1) {						if (partialEnd) //DRM added							s += ">";						s += (getSequencePosition(i-1, it, data));						if (!returnStartingCodonPosition)							v.addElement(s);						s = "";						lastWritten = i-1;					}					else {						s += "\t" + (getSequencePosition(lastWritten, it, data));						if (!returnStartingCodonPosition)							v.addElement(s);						s = "";						lastWritten = -1;					}					continuing = 0;				}			}		}  // end for loop		if (continuing>1){			if (partialEnd)				s += ">";			s +=  (getSequencePosition(characterObjectArray.length-1, it, data));			if (!returnStartingCodonPosition)				v.addElement(s);			s = "";		}		if (found)			return v;		else			return null;	}	/*.................................................................................................................*	protected void saveExtraFilesOLD(CharacterData cata){		if (!(cata instanceof DNAData))			return;		String extraFilePath = StringUtil.getAllButLastItem(filePath, ".")+".tbl";		DNAData data = (DNAData)cata;		if (featureAnnotation && !includeGaps){			CharactersGroup[] parts =null;			boolean hasCDs = data.someCoding();			boolean hasParts = false;			CharacterPartition characterPartition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);			if (characterPartition!=null) {				parts = characterPartition.getGroups();				hasParts = parts!=null;			}			if (!hasParts && !saveFeatureTableEvenIfNoParts) {				return;			}			boolean[][] partial=null;			if (hasParts) {				partial = new boolean[2][parts.length];				for (int ip = 0; ip<parts.length; ip++){					int p = getPartials(parts[ip].getName());					if (p == 1)//00 = not partial; 01 = partial end; 10 = partial start; 11 = partial both						partial[1][ip] = true;					else if (p == 2)						partial[0][ip] = true;					else if (p == 3){						partial[0][ip] = true;						partial[1][ip] = true;					}				}			}			//		String directory = MesquiteFile.chooseDirectory("Directory to Save Tables with Features Annotations");			StringBuffer buffer = new StringBuffer();			//NOTE: Partitions are named by product, e.g. "has 16s, t-RNA and intergene spacer" or "Actin 5C"			//Part = feature.  If first site in part is coding, treated as CDS.  Otherwise, not.			for (int it = 0; it< data.getNumTaxa(); it++){				if ((!writeOnlySelectedTaxa || (data.getTaxa().getSelected(it))) && (!includeOnlyTaxaWithData || taxonHasData(data, it))){					// single StringBuffer buffer = new StringBuffer();					buffer.append(">Features " + getTaxonName(data.getTaxa(), it) + "\t\t\t\t");					buffer.append("\n");					if (!hasParts) {  // not partitioned						int cp = getCodonPositionOfFirstSite(data, it);						String s = "<1\t>"+data.getNumberApplicableInTaxon(it, true);						if (cp>=1 && cp<=3) {							buffer.append(s + "\t" + "CDS\t\t\n");							buffer.append("\t\t\tproduct\t" + sequenceProfile.getProductName()+"\n");							int genbankOffset = getGenBankOffset(cp);							buffer.append("\t\t\tcodon_start\t" + genbankOffset);							buffer.append("\n");						} else {							buffer.append(s + "\t" + "misc_feature\t\t\n");							buffer.append("\t\t\tproduct\t" + sequenceProfile.getProductName()+"\n");							buffer.append("\n");						}					} else {  // is partitioned						for (int i=0; i<parts.length; i++) {							Vector stringsOfSegments = getListOfSegments((Listable[])characterPartition.getProperties(), parts[i], it, data, partial[0][i], partial[1][i], false);							Vector startingCodonPositions = getListOfSegments((Listable[])characterPartition.getProperties(), parts[i], it, data, partial[0][i], partial[1][i], true);							if (stringsOfSegments != null){								boolean nonCoding = false;								for (int k = 0; k<stringsOfSegments.size(); k++) {									String s = (String)stringsOfSegments.elementAt(k);									//if (k == 0) {									//int cp = getCodonPositionOfFirstSiteOfPartition((Listable[])characterPartition.getProperties(), parts[i], data, -1);									MesquiteInteger codPosMesquiteInteger = (MesquiteInteger)startingCodonPositions.elementAt(k);									int cp = codPosMesquiteInteger.getValue();									if (cp>=1 && cp<=3) {										buffer.append(s + "\t" + "CDS\t\t\n");										if (useGroupNameAsProductName)											buffer.append("\t\t\tproduct\t" + parts[i].getName()+"\n");										else											buffer.append("\t\t\tproduct\t" + sequenceProfile.getProductName()+"\n");										int genbankOffset = getGenBankOffset(cp);										buffer.append("\t\t\tcodon_start\t" + genbankOffset+"\n");										nonCoding=false;									}									else if (StringUtil.indexOfIgnoreCase(parts[i].getName(), "intron")>=0) {										buffer.append(s + "\t" + "intron\t\t"+"\n");										nonCoding=true;									}									else {										buffer.append(s + "\t" + "misc_feature\t\t"+"\n");										if (useGroupNameAsProductName)											buffer.append("\t\t\tproduct\t" + parts[i].getName()+"\n");										else											buffer.append("\t\t\tproduct\t" + sequenceProfile.getProductName()+"\n");									}									//}									//else									//	buffer.append(s + "\t\t\t");									buffer.append("\n");								}							}						}					}					// single MesquiteFile.putFileContents(directory + MesquiteFile.fileSeparator + (it+1) + "-features_" + getTaxonName(data.getTaxa(), it) + ".tbl", buffer.toString(), true);				}			}			MesquiteFile.putFileContents(extraFilePath, buffer.toString(), true);		}	}	/*.................................................................................................................*/	 /**Returns a vector of Strings, each string containing the Feature Table description of the bounds of a segment of the sequence,	 * as defined by the character groups.*/	Vector getListOfSegments(Listable[] characterObjectArray, Object characterGroupObject, int it, DNAData data, boolean partialStart, boolean partialEnd, boolean returnStartingCodonPosition) {		int continuing = 0;		String s="";		boolean found=false;		int startingCodPos = -1;		Vector v= new Vector();		int correction = 1;		int lastWritten = -1;						for (int i=0; i<characterObjectArray.length && i<data.getNumChars(); i++) {  // the list array here is the 			if (!data.isInapplicable(i, it)){  // there should be a one-to-one correspondence between this list objects attached to characters and the characters in data				if (characterObjectArray[i]==characterGroupObject) {  //  we've found one					if (continuing == 0) {//first instance in this contiguous stretch						if (partialStart && !found)  // include !found as only want to do this at very first instance							s += "<";						s += (getSequencePosition(i, it, data)); //START						if (returnStartingCodonPosition)							v.addElement(new MesquiteInteger(data.getCodonPosition(i)));						lastWritten = i;						continuing = 1;					}					else if (continuing == 1) {  // we've already found one, so let's put in the tab						s += "..";						continuing = 2;					}					found=true;				}				else if (continuing >0) {  // we've already found one, AND this one is not one of this group, AND it is not a gap					if (lastWritten != i-1) {					//	if (partialEnd) //DRM added					//		s += ">";						s += (getSequencePosition(i-1, it, data));						if (!returnStartingCodonPosition)							v.addElement(s);						s = "";						lastWritten = i-1;					}					else {						s += ".." + (getSequencePosition(lastWritten, it, data));						if (!returnStartingCodonPosition)							v.addElement(s);						s = "";						lastWritten = -1;					}					continuing = 0;				}			}		}  // end for loop		if (found && partialEnd)			s += ">";		if (continuing>1){  // this means we are still in the contiguous stretch even though we are at the end			s +=  (getSequencePosition(characterObjectArray.length-1, it, data));			if (!returnStartingCodonPosition)				v.addElement(s);			s = "";		} 		if (found)			return v;		else			return null;	}	/*.................................................................................................................*/	public String preferredDataFileExtension() {  		return "fsa";	}	int getGenBankOffset (int cp) {		/*Genbank has this strange convention where the codon start is the offset until the next full codon, 		 * but starting counting at 1, so that if the start of the feature is at codon position 1, offset is treated as 1 (not 0!):		 * If first codon position is 2, then start of the next codon is treated as offset 3 		 * If first codon position is 3, then start of the next codon is treated as offset 2		 * Go figure.  I can see using offsets, but then it should be 0 based.		 */		int genbankOffset = 0;		if (cp ==1)			genbankOffset = 1;		else	if (cp == 2) 			genbankOffset = 3;		else	if (cp == 3) 			genbankOffset = 2;		return genbankOffset;	}	/*.................................................................................................................*/	protected void saveExtraFiles(CharacterData cata){		if (!(cata instanceof DNAData))			return;		String extraFilePath = StringUtil.getAllButLastItem(filePath, ".")+".tbl";		DNAData data = (DNAData)cata;		if (featureAnnotation && !includeGaps){			CharactersGroup[] parts =null;			boolean hasCDs = data.someCoding();			boolean hasParts = false;			CharacterPartition characterPartition = (CharacterPartition)data.getCurrentSpecsSet(CharacterPartition.class);			if (characterPartition!=null) {				parts = characterPartition.getGroups();				hasParts = parts!=null;			}			if (!hasParts && !saveFeatureTableEvenIfNoParts) {				return;			}			boolean[][] partial=null;			if (hasParts) {				partial = new boolean[2][parts.length];				for (int ip = 0; ip<parts.length; ip++){					int p = getPartials(parts[ip].getName());					if (p == 1)//00 = not partial; 01 = partial end; 10 = partial start; 11 = partial both						partial[1][ip] = true;					else if (p == 2)						partial[0][ip] = true;					else if (p == 3){						partial[0][ip] = true;						partial[1][ip] = true;					}				}			}			//		String directory = MesquiteFile.chooseDirectory("Directory to Save Tables with Features Annotations");			StringBuffer buffer = new StringBuffer();			//NOTE: Partitions are named by product, e.g. "has 16s, t-RNA and intergene spacer" or "Actin 5C"			//Part = feature.  If first site in part is coding, treated as CDS.  Otherwise, not.			for (int it = 0; it< data.getNumTaxa(); it++){				if ((!writeOnlySelectedTaxa || (data.getTaxa().getSelected(it))) && (!includeOnlyTaxaWithData || taxonHasData(data, it))){					// single StringBuffer buffer = new StringBuffer();					buffer.append(">Features " + getTaxonName(data.getTaxa(), it) + "\t\t\t\t");					buffer.append("\n");					if (!hasParts) {  // not partitioned						int cp = getCodonPositionOfFirstSite(data, it);						String s = "<1\t>"+data.getNumberApplicableInTaxon(it, true);						if (cp>=1 && cp<=3) {							buffer.append(s + "\t" + "CDS\t\t\n");							buffer.append("\t\t\t/product=\"" + sequenceProfile.getProductName()+"\"\n");							int genbankOffset = getGenBankOffset(cp);							buffer.append("\t\t\t/codon_start=" + genbankOffset);							buffer.append("\n");						} else {							buffer.append(s + "\t" + "misc_feature\t\t\n");							buffer.append("\t\t\t/product=\"" + sequenceProfile.getProductName()+"\"\n");							buffer.append("\n");						}					} else {  // is partitioned						for (int i=0; i<parts.length; i++) {							Vector stringsOfSegments = getListOfSegments((Listable[])characterPartition.getProperties(), parts[i], it, data, partial[0][i], partial[1][i], false);							Vector startingCodonPositions = getListOfSegments((Listable[])characterPartition.getProperties(), parts[i], it, data, partial[0][i], partial[1][i], true);							if (stringsOfSegments != null){								boolean cds = false;								for (int k = 0; k<stringsOfSegments.size(); k++) {									MesquiteInteger codPosMesquiteInteger = (MesquiteInteger)startingCodonPositions.elementAt(k);									int cp = codPosMesquiteInteger.getValue();									if (cp>=1 && cp<=3) {										cds=true;										break;									}								}								if (cds) {  // is a coding part									buffer.append( "\tCDS\t");									if (stringsOfSegments.size()>1) {										buffer.append( "(join");										for (int k = 0; k<stringsOfSegments.size(); k++) {											buffer.append((String)stringsOfSegments.elementAt(k));											if (k<stringsOfSegments.size()-1) buffer.append(",");										}										buffer.append( ")");									} else {										buffer.append((String)stringsOfSegments.elementAt(0));									}									buffer.append( "\n");									MesquiteInteger codPosMesquiteInteger = (MesquiteInteger)startingCodonPositions.elementAt(0);									int cp = codPosMesquiteInteger.getValue();									int genbankOffset = getGenBankOffset(cp);									buffer.append("\t\t\t/codon_start=" + genbankOffset+"\n");									if (useGroupNameAsProductName)										buffer.append("\t\t\t/product=\"" + parts[i].getName()+"\"\n");									else										buffer.append("\t\t\t/product=\"" + sequenceProfile.getProductName()+"\n");									buffer.append("\n");								} else {									for (int k = 0; k<stringsOfSegments.size(); k++) {										String s = (String)stringsOfSegments.elementAt(k);										//if (k == 0) {										//int cp = getCodonPositionOfFirstSiteOfPartition((Listable[])characterPartition.getProperties(), parts[i], data, -1);										 if (StringUtil.indexOfIgnoreCase(parts[i].getName(), "intron")>=0) {											buffer.append("\t"+s + "\t" + "intron\t\t"+"\n");										}										else {											buffer.append("\t"+ s + "\t" + "misc_feature\t\t"+"\n");											if (useGroupNameAsProductName)												buffer.append("\t\t\t/product=\"" + parts[i].getName()+"\"\n");											else												buffer.append("\t\t\t/product=\"" + sequenceProfile.getProductName()+"\"\n");										}										//}										//else										//	buffer.append(s + "\t\t\t");										buffer.append("\n");									}								}							}						}					}					// single MesquiteFile.putFileContents(directory + MesquiteFile.fileSeparator + (it+1) + "-features_" + getTaxonName(data.getTaxa(), it) + ".tbl", buffer.toString(), true);				}			}			MesquiteFile.putFileContents(extraFilePath, buffer.toString(), true);		}	}	/*.................................................................................................................*/	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 		return getProject().chooseData(containerOfModule(), file, null, DNAState.class, "Select data to export");	}	/*.................................................................................................................*/	public void setFastaState(CharacterData data, int ic, int it, char c) { 		if ((c=='U')||(c=='u')) {			((DNAData)data).setDisplayAsRNA(true);		}		((DNAData)data).setState(ic,it,c);	}	/*.................................................................................................................*/	public  String getUnassignedSymbol(){		return "N";	}	/*.................................................................................................................*/	public String getName() {		return "FASTA (DNA/RNA) for GenBank (via tbl2asn)";	}	/*.................................................................................................................*/	/** returns an explanation of what the module does.*/	public String getExplanation() {		return "Exports FASTA files for use in tbl2asn for GenBank deposition." ;	}	/*.................................................................................................................*/	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/	public int getVersionOfFirstRelease(){		return -1500;  	}}