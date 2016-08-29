package de.longri.cachebox3.sqlite.Import;

import CB_Utils.Events.ProgresssChangedEventList;

import java.util.ArrayList;

/**
 * Verwaltet den Progress Status beim Importieren
 * 
 * @author Longri
 */
public class ImporterProgress {

	/**
	 * Enthält einen übergeordneten Schritt
	 * 
	 * @author Longri
	 */
	public class Step {
		public Step(String Name, float weight) {
			this.weight = weight;
			this.Name = Name;
			this.progress = 0.0f;
		}

		public float weight = 0.0f;
		public float progress = 0.0f;
		public String Name;
		public float stepweight;

		public void setMaxStep(int max) {
			if (max == 0) {
				this.stepweight = 1f;
			} else {
				this.stepweight = 1f / (float) max;
			}

		}
	}

	private ArrayList<Step> steps;
	private float weightSumme = 0.0f;

	// Initial Progress at Constructor
	public ImporterProgress() {
		steps = new ArrayList<Step>();

	}

	public void addStep(Step step) {
		steps.add(step);
		weightSumme = getWeightSum();
	}

	private float getWeightSum() {
		float sum = 0.0f;
		for (Step job : steps) {
			sum += job.weight;
		}
		return sum;
	}

	public void ProgressInkrement(String Name, String Msg, Boolean Done) {
		// get Job
		int Progress = 0;
		for (Step job : steps) {
			if (job.Name.equals(Name)) {
				if (Done) {
					job.progress = 1f;
				} else {
					job.progress += job.stepweight;
				}
				Progress = getProgress();

				break;
			}
		}

		// send Progress Change Msg
		ProgresssChangedEventList.Call(Name, Msg, Progress);
	}

	// only change Msg or progress with out changing progress
	public void ProgressChangeMsg(String Name, String Msg) {
		// send Progress Change Msg
		ProgresssChangedEventList.Call(Name, Msg, getProgress());
	}

	public void setJobMax(String Name, int max) {
		for (Step job : steps) {
			if (job.Name.equals(Name)) {
				job.setMaxStep(max);
			}
		}
	}

	protected int getProgress() {
		float progress = 0.0f;

		for (Step job : steps) {
			progress += (job.weight / weightSumme) * job.progress;
		}

		return (int) (100 * progress);
	}

}
