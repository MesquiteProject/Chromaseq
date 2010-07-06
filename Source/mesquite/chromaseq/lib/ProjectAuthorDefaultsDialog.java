

package mesquite.chromaseq.lib;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import org.dom4j.*;

import mesquite.tol.lib.*;

import mesquite.lib.*;

/**
 * Dialog to choose an author from a list of project collaborators
 * @author dmandel
 *
 */
public class ProjectAuthorDefaultsDialog extends ExtensibleDialog {
	private JLabel serverErrorLabel;
	private JLabel selectNameLabel;
	private String[] choiceStrings;
	private Choice popupMenu;
	private List contributors;
	private MesquiteBoolean okSelected;
	private SingleLineTextField urlField;
	private String projectUrl;
	private static final String END_URL = "onlinecontributors/app?service=page&page=btolxml/ContributorList";
	
	public ProjectAuthorDefaultsDialog(MesquiteWindow parent, MesquiteBoolean okSelected, String url) {
		super(parent, "Set Author Information");
		this.okSelected = okSelected;
		this.projectUrl = url;
		addComponents();
	}
	
	public void addComponents(){
		if (!StringUtil.blank(MesquiteModule.author.getName())) {
			addLabel("Current Author Name: " + MesquiteModule.author.getName(), Label.LEFT, true);
			addLabel("Current Author Code: " + MesquiteModule.author.getCode(), Label.LEFT, true);
		}
		addHorizontalLine(1);
		urlField = addTextField("Collab Server URL", projectUrl, 50, false);
		ActionListener fetchContributorsListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String url = urlField.getText();
				if (StringUtil.blank(url)) {
					MesquiteMessage.notifyUser("You must enter a value for url.");
					return;
				}
				// check to see if they ended with a slash, add one if they didn't
				if (url != null && !url.endsWith("/")) {
					url += "/";
				}
				url += END_URL;
				contributors = new ArrayList();
				// throw up the wait since this might take a little while
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Document doc = BaseHttpRequestMaker.getHttpResponseAsDocument(url);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				if (doc != null) {
					serverErrorLabel.setVisible(false);
					popupMenu.removeAll();					
					for (Iterator iter = doc.getRootElement().elements(XMLConstants.CONTRIBUTOR).iterator(); iter.hasNext();) {
						Element element = (Element) iter.next();
						String firstName = element.elementText(XMLConstants.FIRSTNAME);
						String lastName = element.elementText(XMLConstants.LASTNAME);
						String idString = element.attributeValue(XMLConstants.ID);
						Contributor contr = new Contributor(firstName + " " + lastName, Integer.parseInt(idString));
						contributors.add(contr);
						popupMenu.add(contr.getName());
					}					
					popupMenu.setVisible(true);
					selectNameLabel.setVisible(true);
					prepareDialog();
					repaint();
				} else {
					MesquiteMessage.notifyUser("Server not responding.  Please check your url and try again.");
					popupMenu.setVisible(false);
					selectNameLabel.setVisible(false);
					serverErrorLabel.setVisible(true);
					prepareDialog();
					repaint();
				}
			}
		};
		addAListenedButton("Fetch Contributors", null, fetchContributorsListener);
		addHorizontalLine(1);
		choiceStrings = new String[1];
		choiceStrings[0] = "";
		selectNameLabel = new JLabel("Select your name:");
		popupMenu = addPopUpMenu(selectNameLabel, choiceStrings, 0);
		popupMenu.setVisible(false);
		selectNameLabel.setVisible(false);
		serverErrorLabel = addLabel("Failed to connect to the collab server.  Please check your typing and try again.");
		serverErrorLabel.setVisible(false);
		completeAndShowDialog(true,null);
		boolean anyContributors = contributors != null && contributors.size() > 0;
		// only set the return value to true if they hit ok AND they successfully 
		// selected a contributor from the list
		int queryResult = query();
		okSelected.setValue((query()==0) && anyContributors);
	}	
	public String getAuthorName() {
		return getSelectedContributor().getName();
	}
	public String getAuthorCode() {
		return getSelectedContributor().getId() + "";
	}
	public String getProjectUrl() {
		return urlField.getText();
	}
	public MesquiteBoolean getOkSelected() {
		return okSelected;
	}
	private Contributor getSelectedContributor() {
		return (Contributor) contributors.get(popupMenu.getSelectedIndex());
	}
	
	private static class Contributor {
		private String name;
		private int id;
		
		public Contributor(String name, int id) {
			this.name = name;
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
}