package mesquite.chromaseq.SequenceSpecificationForGenBank;

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


public class SequenceSpecificationDialog extends ExtensibleDialog {
	
	private SequenceSpecificationForGenBank sequenceSpecificationManager;
	private SequenceSpecification sequenceSpecification;
	private Choice sequenceSpecificationChoice;	
	private String sequenceSpecificationName="";	
	
	public SequenceSpecificationDialog(MesquiteWindow parent, String title, MesquiteInteger buttonPressed,
			SequenceSpecificationForGenBank sequenceSpecificationManager, String defaultRuleName) {
		super(parent, title, buttonPressed);
		this.sequenceSpecificationManager = sequenceSpecificationManager;
		this.sequenceSpecificationName = defaultRuleName;
		addNameParsingComponents();
	}

	
	protected void addNameParsingComponents() {
		addLabel(getTitle());
		
		sequenceSpecificationChoice = addPopUpMenu("Sequence Specifications", sequenceSpecificationManager.sequenceSpecificationVector.getElementArray(), 0);
		sequenceSpecificationManager.setChoice(sequenceSpecificationChoice);
		sequenceSpecificationChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getItemSelectable() == sequenceSpecificationChoice){
					getNameRuleFromChoice();
				}				
			}
		});
		if (sequenceSpecificationChoice!=null) {
			boolean noChoiceItems = (sequenceSpecificationChoice.getItemCount()<=0);
			int sL = sequenceSpecificationManager.sequenceSpecificationVector.indexOfByName(sequenceSpecificationName);
			if (sL <0) {
				sL = 0;
			}		
			if (!noChoiceItems) {
				sequenceSpecificationChoice.select(sL); 
				sequenceSpecification = (SequenceSpecification)(sequenceSpecificationManager.sequenceSpecificationVector.elementAt(sL));
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
				if (sequenceSpecificationManager!=null) {
					sequenceSpecification = sequenceSpecificationManager.chooseSequenceSpecifiation(sequenceSpecification);
				}				
			}
		});		
	}
	
	public void getNameRuleFromChoice() {
		sequenceSpecification=null;
		if (sequenceSpecificationChoice!=null) {
			sequenceSpecificationName = sequenceSpecificationChoice.getSelectedItem();
			boolean noChoiceItems = (sequenceSpecificationChoice.getItemCount()<=0);
			int sL = sequenceSpecificationChoice.getSelectedIndex();
			if (sL <0) {
				sL = 0;
			}		
			if (!noChoiceItems) {
				sequenceSpecification = (SequenceSpecification)(sequenceSpecificationManager.sequenceSpecificationVector.elementAt(sL));
			}
		}
		if (sequenceSpecification==null)
			sequenceSpecification = new SequenceSpecification();  //make default one	}
	}

	public SequenceSpecification getNameParsingRule() {
		return sequenceSpecification;
	}	
}
