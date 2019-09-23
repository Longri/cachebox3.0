/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.gui.views;

import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.platform_test.RunOnGL;
import de.longri.cachebox3.settings.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 25.03.2019.
 */
public class AbstractViewSerialiseTest {

    @Test
    public void AboutViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        AboutView aboutView = new AboutView();
        AboutView newAboutView = (AboutView) TestUtils.assertAbstractViewSerialation(aboutView, AboutView.class);

        assertEquals(aboutView, newAboutView);
    }


    @Test
    public void CacheListViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        CacheListView cacheListView = new CacheListView();
        CacheListView newCacheListView = (CacheListView) TestUtils.assertAbstractViewSerialation(cacheListView, CacheListView.class);
    }

    @Test
    public void CompassViewTest() {
        TestUtils.initialVisUI();
        // CompassView read some settings from Config 
        boolean showMap = Config.CompassShowMap.getValue();
        boolean showName = Config.CompassShowWP_Name.getValue();
        boolean showIcon = Config.CompassShowWP_Icon.getValue();
        boolean showAtt = Config.CompassShowAttributes.getValue();
        boolean showGcCode = Config.CompassShowGcCode.getValue();
        boolean showCoords = Config.CompassShowCoords.getValue();
        boolean showWpDesc = Config.CompassShowWpDesc.getValue();
        boolean showSatInfos = Config.CompassShowSatInfos.getValue();
        boolean showSunMoon = Config.CompassShowSunMoon.getValue();
        boolean showTargetDirection = Config.CompassShowTargetDirection.getValue();
        boolean showSDT = Config.CompassShowSDT.getValue();
        boolean showLastFound = Config.CompassShowLastFound.getValue();

        Config.CompassShowMap.setValue(!showMap);
        Config.CompassShowWP_Name.setValue(!showName);
        Config.CompassShowWP_Icon.setValue(!showIcon);
        Config.CompassShowAttributes.setValue(!showAtt);
        Config.CompassShowGcCode.setValue(!showGcCode);
        Config.CompassShowCoords.setValue(!showCoords);
        Config.CompassShowWpDesc.setValue(!showWpDesc);
        Config.CompassShowSatInfos.setValue(!showSatInfos);
        Config.CompassShowSunMoon.setValue(!showSunMoon);
        Config.CompassShowTargetDirection.setValue(!showTargetDirection);
        Config.CompassShowSDT.setValue(!showSDT);
        Config.CompassShowLastFound.setValue(!showLastFound);

        Config.AcceptChanges();

        CompassView compassView = new CompassView();
        CompassView newCompassView = (CompassView) TestUtils.assertAbstractViewSerialation(compassView, CompassView.class);
        assertEquals(compassView, newCompassView);
    }

    @Test
    public void CreditsViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        CreditsView creditsView = new CreditsView();
        CreditsView newCreditsView = (CreditsView) TestUtils.assertAbstractViewSerialation(creditsView, CreditsView.class);
    }

    @Test
    public void DescriptionViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        DescriptionView descriptionView = new DescriptionView();
        DescriptionView newDescriptionView = (DescriptionView) TestUtils.assertAbstractViewSerialation(descriptionView, DescriptionView.class);
    }

    @Test
    @RunOnGL
    public void DraftsViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        DraftsView draftsView = DraftsView.getInstance();
        DraftsView newDraftsView = (DraftsView) TestUtils.assertAbstractViewSerialation(draftsView, DraftsView.class);
    }

    @Test
    @RunOnGL
    public void LogListViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        LogListView logListView = new LogListView();
        LogListView newLogListView = (LogListView) TestUtils.assertAbstractViewSerialation(logListView, LogListView.class);
    }

    @Test
    public void NotesViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        NotesView notesView = new NotesView();
        NotesView newNotesView = (NotesView) TestUtils.assertAbstractViewSerialation(notesView, NotesView.class);
    }

    @Test
    public void SolverViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        SolverView solverView = new SolverView();
        SolverView newSolverView = (SolverView) TestUtils.assertAbstractViewSerialation(solverView, SolverView.class);
    }

    @Test
    public void SolverView2Test() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        SolverView2 solverView = new SolverView2();
        SolverView2 newSolverView = (SolverView2) TestUtils.assertAbstractViewSerialation(solverView, SolverView2.class);
    }

    @Test
    public void SpoilerViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        SpoilerView spoilerView = new SpoilerView();
        SpoilerView newSpoilerView = (SpoilerView) TestUtils.assertAbstractViewSerialation(spoilerView, SpoilerView.class);
    }

    @Test
    @RunOnGL
    public void TestViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        TestView testView = new TestView();
        TestView newTestView = (TestView) TestUtils.assertAbstractViewSerialation(testView, TestView.class);
    }

    @Test
    public void TrackableListViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        TrackableListView trackableListView = new TrackableListView();
        TrackableListView newTrackableListView = (TrackableListView) TestUtils.assertAbstractViewSerialation(trackableListView, TrackableListView.class);
    }

    @Test
    public void TrackListViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        TrackListView trackListView = new TrackListView();
        TrackListView newTrackListView = (TrackListView) TestUtils.assertAbstractViewSerialation(trackListView, TrackListView.class);
    }

    @Test
    public void WaypointViewTest() {
        TestUtils.initialVisUI();
        // About view have no member! check only serialization/deserialization!
        WaypointView waypointView = new WaypointView();
        WaypointView newWaypointView = (WaypointView) TestUtils.assertAbstractViewSerialation(waypointView, WaypointView.class);
    }

}
