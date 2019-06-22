/*
 * Copyright (C) 2014- 2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.sqlite.Import;

import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.ImportProgressChangedEvent;

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
    public static class Step {
        public Step(String name, float weight) {
            this.weight = weight;
            this.name = name;
            this.progress = 0.0f;
        }

        public float weight = 0.0f;
        public float progress = 0.0f;
        public String name;
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

    public void ProgressInkrement(String name, String msg, Boolean done) {
        // get Job
        int progressValue = 0;
        for (Step job : steps) {
            if (job.name.equals(name)) {
                if (done) {
                    job.progress = 1f;
                } else {
                    job.progress += job.stepweight;
                }
                progressValue = getProgress();
                break;
            }
        }

        // send Progress Change Msg
        ImportProgressChangedEvent.ImportProgress progress = new ImportProgressChangedEvent.ImportProgress();
        progress.msg = msg;
        progress.progress = progressValue;
        EventHandler.fire(new ImportProgressChangedEvent(progress));
    }

    // only change Msg or progress with out changing progress
    public void ProgressChangeMsg(String name, String msg) {
        // send Progress Change Msg
        ImportProgressChangedEvent.ImportProgress progress = new ImportProgressChangedEvent.ImportProgress();
        progress.msg = msg;
        progress.progress = getProgress();
        EventHandler.fire(new ImportProgressChangedEvent(progress));
    }

    public void setJobMax(String name, int max) {
        for (Step job : steps) {
            if (job.name.equals(name)) {
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
