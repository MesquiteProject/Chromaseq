/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.
Version 0.980   July 2010
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteModule;

public abstract class SequenceNameSource extends MesquiteModule {

	public Class getDutyClass() {
		return SequenceNameSource.class;
	}

 	public String getDutyName() {
 		return "Sequence Name Source";
   	 }

 	//returns name of sequence
 	public abstract String getSequenceName(String ID);  //pass sample id or other id (e.g. coords in plate, etc.)

 	//returns alternative name of sequence (e.g. full taxon name)
 	public abstract String getAlternativeName(String ID);

 	//returns whether it might return an alternative name, though not guaranteed for a particular taxon
 	public abstract boolean hasAlternativeNames();

 	//returns extraction code from ID (may return nothing;applicable only if ID refers to extraction)
 	public abstract String getExtractionCode(String ID);

 	//returns sample code from ID (may return nothing;applicable only if ID refers to sample)
 	public abstract String getSampleCode(String ID);

 	//returns whether source has all relevant data supplied (e.g. address of database, etc.)
 	public abstract boolean isReady();

}
