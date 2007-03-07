package mesquite.chromaseq.lib;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.lib.*;


/**
 * Subclass of extensible dialog that knows how to deal with
 * chromatogram file-naming options
 * @author dmandel
 *
 */
public class ChromFileNameDialog extends ExtensibleDialog {
	public static final int CANCEL = 1;
	public static final int OK = 0;
	
	private NameParserManager nameParserManager;
	private ChromFileNameParsing nameParsingRule;
	private Choice nameRulesChoice;	
	private String nameParsingRulesName="";	
	
	public ChromFileNameDialog(MesquiteWindow parent, String title, MesquiteInteger buttonPressed,
			NameParserManager nameParserManager, String defaultRuleName) {
		super(parent, title, buttonPressed);
		this.nameParserManager = nameParserManager;
		this.nameParsingRulesName = defaultRuleName;
		addNameParsingComponents();
	}
	
	protected void addNameParsingComponents() {
		addLabel(getTitle());
		addHorizontalLine(2);
		
		nameRulesChoice = addPopUpMenu("File Naming Rules", nameParserManager.nameParsingRules.getElementArray(), 0);
		nameParserManager.setChoice(nameRulesChoice);
		nameRulesChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getItemSelectable() == nameRulesChoice){
					getNameRuleFromChoice();
				}				
			}
		});
		if (nameRulesChoice!=null) {
			boolean noChoiceItems = (nameRulesChoice.getItemCount()<=0);
			int sL = nameParserManager.nameParsingRules.indexOfByName(nameParsingRulesName);
			if (sL <0) {
				sL = 0;
			}		
			if (!noChoiceItems) {
				nameRulesChoice.select(sL); 
				nameParsingRule = (ChromFileNameParsing)(nameParserManager.nameParsingRules.elementAt(sL));
			}
		}	
		suppressNewPanel();
		GridBagConstraints gridConstraints;
		gridConstraints = getGridBagConstraints();
		gridConstraints.fill = GridBagConstraints.NONE;
		setGridBagConstraints(gridConstraints);
		Panel panel = addNewDialogPanel(gridConstraints);
		String editNameParserButtonString = "Edit Naming Rules...";
		Button editNameParsersButton = addAButton(editNameParserButtonString, panel);
		editNameParsersButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (nameParserManager!=null) {
					nameParsingRule = nameParserManager.chooseNameParsingRules(nameParsingRule);
				}				
			}
		});		
	}
	
	public void getNameRuleFromChoice() {
		nameParsingRule=null;
		if (nameRulesChoice!=null) {
			nameParsingRulesName = nameRulesChoice.getSelectedItem();
			boolean noChoiceItems = (nameRulesChoice.getItemCount()<=0);
			int sL = nameRulesChoice.getSelectedIndex();
			if (sL <0) {
				sL = 0;
			}		
			if (!noChoiceItems) {
				nameParsingRule = (ChromFileNameParsing)(nameParserManager.nameParsingRules.elementAt(sL));
			}
		}
		if (nameParsingRule==null)
			nameParsingRule = new ChromFileNameParsing();  //make default one	}
	}

	public ChromFileNameParsing getNameParsingRule() {
		return nameParsingRule;
	}	
}
