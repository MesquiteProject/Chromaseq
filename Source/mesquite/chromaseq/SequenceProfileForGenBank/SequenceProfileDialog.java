package mesquite.chromaseq.SequenceProfileForGenBank;

import java.awt.Button;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.chromaseq.lib.ChromatogramFileNameParser;
import mesquite.lib.*;


public class SequenceProfileDialog extends ExtensibleDialog {
	
	private SequenceProfileForGenBank sequenceProfileManager;
	private SequenceProfile sequenceProfile;
	private Choice sequenceProfileChoice;	
	private String sequenceProfileName="";	
	
	public SequenceProfileDialog(MesquiteWindow parent, String title, MesquiteInteger buttonPressed,
			SequenceProfileForGenBank sequenceProfileManager, String defaultRuleName) {
		super(parent, title, buttonPressed);
		this.sequenceProfileManager = sequenceProfileManager;
		this.sequenceProfileName = defaultRuleName;
		addNameParsingComponents();
	}

	
	protected void addNameParsingComponents() {
		addLabel(getTitle());
		
		sequenceProfileChoice = addPopUpMenu("Sequence Profiles", sequenceProfileManager.sequenceProfileVector.getElementArray(), 0);
		sequenceProfileManager.setChoice(sequenceProfileChoice);
		sequenceProfileChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getItemSelectable() == sequenceProfileChoice){
					getNameRuleFromChoice();
				}				
			}
		});
		if (sequenceProfileChoice!=null) {
			boolean noChoiceItems = (sequenceProfileChoice.getItemCount()<=0);
			int sL = sequenceProfileManager.sequenceProfileVector.indexOfByName(sequenceProfileName);
			if (sL <0) {
				sL = 0;
			}		
			if (!noChoiceItems) {
				sequenceProfileChoice.select(sL); 
				sequenceProfile = (SequenceProfile)(sequenceProfileManager.sequenceProfileVector.elementAt(sL));
			}
		}	
		suppressNewPanel();
		GridBagConstraints gridConstraints;
		gridConstraints = getGridBagConstraints();
		gridConstraints.fill = GridBagConstraints.NONE;
		setGridBagConstraints(gridConstraints);
		Panel panel = addNewDialogPanel(gridConstraints);
		String editNameParserButtonString = "Edit Specifications...";
		Button editNameParsersButton = addAButton(editNameParserButtonString, panel);
		editNameParsersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sequenceProfileManager!=null) {
					sequenceProfile = sequenceProfileManager.chooseSequenceSpecifiation(sequenceProfile);
				}				
			}
		});		
	}
	
	public void getNameRuleFromChoice() {
		sequenceProfile=null;
		if (sequenceProfileChoice!=null) {
			sequenceProfileName = sequenceProfileChoice.getSelectedItem();
			boolean noChoiceItems = (sequenceProfileChoice.getItemCount()<=0);
			int sL = sequenceProfileChoice.getSelectedIndex();
			if (sL <0) {
				sL = 0;
			}		
			if (!noChoiceItems) {
				sequenceProfile = (SequenceProfile)(sequenceProfileManager.sequenceProfileVector.elementAt(sL));
			}
		}
		if (sequenceProfile==null)
			sequenceProfile = new SequenceProfile();  //make default one	}
	}

	public SequenceProfile getNameParsingRule() {
		return sequenceProfile;
	}	
}
