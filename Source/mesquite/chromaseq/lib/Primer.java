package mesquite.chromaseq.lib;

import mesquite.lib.*;

public class Primer implements Listable {
	String name;
	boolean isForward = true;
	String sequence;
	
	
	public Primer (String name) {
		setName(name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isForward() {
		return isForward;
	}

	public void setForward(boolean isForward) {
		this.isForward = isForward;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

}
