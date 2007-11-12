package mesquite.chromaseq.ChromaseqAuthorDefaults;

import mesquite.basic.AuthorDefaults.AuthorDefaults;
import mesquite.chromaseq.lib.ProjectAuthorDefaultsDialog;
import mesquite.lib.Author;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.MesquiteWindow;
import mesquite.lib.MesquiteXMLPreferencesModule;
import mesquite.lib.PropertyNamesProvider;

/**
 * class that hooks into the authoring system that shows a dialog
 * to query a list of project authors provided by a server
 * @author dmandel
 *
 */
public class ChromaseqAuthorDefaults extends AuthorDefaults implements PropertyNamesProvider {
	private String url;
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem( "Set Project Author...", makeCommand("setProjectAuthor",  this));	
		return true;
	}	
	
	public Object doCommand(String commandName, String arguments,
			 CommandChecker checker) {
		if (checker.compare(MesquiteWindow.class,
				"Sets the project author for this machine.", null,
				commandName, "setProjectAuthor")) {
			setProjectAuthor();
			return null;

		} else {
			return  super.doCommand(commandName, arguments, checker);
		}
	}
	
	public void setProjectAuthor() {
		loadPreferences();			
		ProjectAuthorDefaultsDialog defaultsDialog = new ProjectAuthorDefaultsDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), MesquiteBoolean.TRUE, getUrl());
		//defaultsDialog.completeAndShowDialog(true);
		MesquiteBoolean success = defaultsDialog.getOkSelected();
		if (success.getValue()) {
			setUrl(defaultsDialog.getProjectUrl());
			setAuthor(defaultsDialog.getAuthorName());
			setCode(defaultsDialog.getAuthorCode());
			storePreferences();
		}
		// DANNY: this should probably happen but this method is private in superclass
		// and my svn is showing something wrong with the superclass
		//setCurrentAllProjects();		
	}
	
	public boolean verifyAuthorIsntDefault() {
		if (MesquiteModule.author.hasDefaultSettings()) {
			setProjectAuthor();
			// don't allow the upload to proceed unless they chose a person
			if (MesquiteModule.author.hasDefaultSettings()) {
				MesquiteMessage.notifyUser("You may not proceed until you choose your username.");
				// they didn't choose their author so upload cannot proceed
				return false;
			}
		}
		return true;
	}

	/* ................................................................................................................. */
	public String getName() {
		return "Set Author (for use in BToL Project)";
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return "Sets the project author for this machine.";
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAuthor() {
		return MesquiteModule.author.getName();
	}
	public void setAuthor(String value) {
		MesquiteModule.author.setName(value);
	}
	public String getCode() {
		return MesquiteModule.author.getCode();
	}
	public void setCode(String value) {
		MesquiteModule.author.setCode(value);
	}
	public String[] getPreferencePropertyNames() {
		return new String[] {"url", "author", "code"};
	}
	public String preparePreferencesForXML() {
		return MesquiteXMLPreferencesModule.preparePreferencesForXML(this, getVersionInt());
	}
	protected boolean parseFullXMLDocument(String prefsXML) {
		return MesquiteXMLPreferencesModule.parseFullXMLDocument(prefsXML, this, getVersionInt(), xmlPrefsVersionMustMatch());
	}
}
