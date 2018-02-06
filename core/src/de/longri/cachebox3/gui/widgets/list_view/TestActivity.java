package de.longri.cachebox3.gui.widgets.list_view;
/*
 * Copyright (C) 2018 team-cachebox.de
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

import de.longri.cachebox3.gui.ActivityBase;

/**
 * Created by Longri on 06.02.2018.
 */
public class TestActivity extends ActivityBase {

    final int COUNT = 8;


    final TestOld old;
    final TestNew n_ew;


    public TestActivity() {
        super("TestActivity");
        this.old = new TestOld(COUNT);
        this.addActor(old);
        this.n_ew = new TestNew(COUNT);
        this.addActor(n_ew);
        sizeChanged();

        old.setScrollChangedListener(new ScrollChangedEvent() {
            @Override
            public void scrollChanged(float x, float y) {
                n_ew.setScrollPos(y);
            }
        });

        n_ew.setScrollChangedListener(new ScrollChangedEvent() {
            @Override
            public void scrollChanged(float x, float y) {
                old.setScrollPos(y);
            }
        });

    }

    @Override
    public void sizeChanged() {
        float half = this.getWidth() / 2;
        float height = this.getHeight() - 100;
        old.setBounds(half, 0, half, height);
        n_ew.setBounds(0, 0, half, height);
    }
}
