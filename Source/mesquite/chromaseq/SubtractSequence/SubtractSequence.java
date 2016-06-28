/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.Version 1.0   December 2011Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.SubtractSequence;/*~~  */import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.categ.lib.*;import mesquite.lib.table.*;/* ======================================================================== */public class SubtractSequence extends CategDataAlterer implements AltererDNACell{   	CategoricalState cs1;   	CategoricalState cs2 ;		/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		return true;	}	/*.................................................................................................................*/   	public void alterCell(CharacterData data, int ic, int it){   	}	/*.................................................................................................................*/   	/** Called to alter data in those cells selected in table*/   	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){		CategoricalData categData = (CategoricalData)data;		MesquiteBoolean dataChanged = new MesquiteBoolean (false);		MesquiteInteger row = new MesquiteInteger();		MesquiteInteger firstColumn = new MesquiteInteger();		MesquiteInteger lastColumn = new MesquiteInteger();//		int totalAddedToStart = 0;		if (table.onlySingleRowBlockSelected(row,firstColumn, lastColumn)) {			int itSelected = row.getValue();			for (int it = itSelected+1; it<categData.getNumTaxa(); it++) {				for (int ic = 0; ic<categData.getNumChars(); ic++) {					cs1 = (CategoricalState)categData.getCharacterState(null, ic, itSelected); //to serve as persistent container					cs2 = (CategoricalState)categData.getCharacterState(null, ic, it);					boolean lc = cs2.isLowerCase();					long cSelected = CategoricalState.setLowerCase(cs1.getValue(),false);					long c  = CategoricalState.setLowerCase(cs2.getValue(), false);					if (cSelected!=c && !cs1.isInapplicable() &&  !cs1.isUnassigned()) {						if (CategoricalState.isSubset(cSelected,c)) {							long subtraction = CategoricalState.setLowerCase(CategoricalState.clearFromSet(c,cSelected), lc);							categData.setState(ic, it, subtraction);							dataChanged.setValue(true);						}					}									}			}			return dataChanged.getValue();		}		else {   			discreetAlert( "A portion of only one sequence can be selected.");			return false;   		}   	}   		/*.................................................................................................................*/  	 public boolean showCitation() {		return true;   	 }	/*.................................................................................................................*/   	 public boolean isPrerelease(){   	 	return false;   	 }	/*.................................................................................................................*/    	 public String getName() {		return "Subtract Sequence From Remainder";   	 }	/*.................................................................................................................*/ 	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Subtracts the selected sequence from all following sequences at any site that the select sequence has a subset of the other sequence." ;   	 }   	 }