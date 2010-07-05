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
