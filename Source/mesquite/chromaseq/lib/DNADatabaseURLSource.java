package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteModule;
import mesquite.lib.duties.TreeSource;

public abstract class DNADatabaseURLSource extends MesquiteModule {

  	 public Class getDutyClass() {
    	 	return DNADatabaseURLSource.class;
    	 }
  	public String getDutyName() {
  		return "DNA Database URL Source";
    	 }

  	public abstract String getBaseURL();
  	
  	public abstract String getChromatogramPage();
  	
  	public abstract String getSequencePage();
  	
  	public abstract String getPrimersPage();
  	
  	public abstract String getContributorsPage();
  	
  	public abstract String getABIUploadPage();
  	
  	public abstract String getChromatogramBatchCreationPage();
  	
  	public abstract String getFASTAUploadPage();
  	
	public abstract String getKey();
	

  	

}
