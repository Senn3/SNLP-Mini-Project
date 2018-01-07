package de.snlp.mp.text_model;

public class Dependency {

	private String dep;
	private int governor;
	private String governorGloss;
	private int dependent;
	private String dependentGloss;

	public String getDep() {
		return dep;
	}

	public void setDep(String dep) {
		this.dep = dep;
	}

	public int getGovernor() {
		return governor;
	}

	public void setGovernor(int governor) {
		this.governor = governor;
	}

	public String getGovernorGloss() {
		return governorGloss;
	}

	public void setGovernorGloss(String governorGloss) {
		this.governorGloss = governorGloss;
	}

	public int getDependent() {
		return dependent;
	}

	public void setDependent(int dependent) {
		this.dependent = dependent;
	}

	public String getDependentGloss() {
		return dependentGloss;
	}

	public void setDependentGloss(String dependentGloss) {
		this.dependentGloss = dependentGloss;
	}

}
