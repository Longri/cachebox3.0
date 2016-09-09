/* 
 * Copyright (C) 2011-2016 team-cachebox.de
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
package de.longri.cachebox3.gui.actions;


import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.widgets.QuickButtonItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.MoveableList;

/**
 * Enthält die Actions Möglichkeiten für die Quick Buttons
 * 
 * @author Longri
 */
public enum QuickActions {
	DescriptionView, // 0
	WaypointView, // 1
	LogView, // 2
	MapView, // 3
	CompassView, // 4
	CacheListView, // 5
	TrackListView, // 6
	TakePhoto, // 7
	TakeVideo, // 8
	VoiceRecord, // 9
	LiveSearch, // 10
	Filter, // 11
	AutoResort, // 12
	Solver, // 13
	Spoiler, // 14
	Hint, // 15
	Parking, // 16
	Day_Night, // 17
	FieldNotes, // 18
	QuickFieldNotes, // 19
	TrackableListView, // 20
	addWP, // 21
	Solver2, // 22
	Notesview, // 23
	uploadFieldNote, // 24
	torch, // 25

	// ScreenLock, // 21

	empty,;

	/**
	 * Gibt eine ArrayList von Actions zurück aus einem übergebenen String Array
	 * 
	 * @param configList
	 * @return ArrayList <Actions>
	 */
	public static MoveableList<QuickButtonItem> getListFromConfig(String[] configList, float height) {
		InitialActions();
		MoveableList<QuickButtonItem> retList = new MoveableList<QuickButtonItem>();
		if (configList == null || configList.length == 0) {
			return retList;
		}

		boolean invalidEnumId = false;
		try {
			int index = 0;

			for (String s : configList) {
				s = s.replace(",", "");
				int EnumId = Integer.parseInt(s);
				if (EnumId > -1) {

					QuickActions type = QuickActions.values()[EnumId];
					if (QuickActions.getActionEnumById(EnumId) != null) {
						QuickButtonItem tmp = new QuickButtonItem(new CB_RectF(0, 0, height, height), index++, QuickActions.getActionEnumById(EnumId), QuickActions.getName(EnumId), type);
						retList.add(tmp);
					} else
						invalidEnumId = true;
				}
			}
		} catch (Exception e)// wenn ein Fehler auftritt, gib die bis dorthin
		// gelesenen Items zurück
		{

		}
		if (invalidEnumId) {
			//	    write valid id's back

			String ActionsString = "";
			int counter = 0;
			for (int i = 0, n = retList.size(); i < n; i++) {
				QuickButtonItem tmp = retList.get(i);
				ActionsString += String.valueOf(tmp.getAction().ordinal());
				if (counter < retList.size() - 1) {
					ActionsString += ",";
				}
				counter++;
			}
			Config.quickButtonList.setValue(ActionsString);
			Config.AcceptChanges();
		}
		return retList;
	}

	/**
	 * Gibt die ID des übergebenen Enums zurück
	 * 
	 * @param attrib
	 * @return long
	 */
	public static int GetIndex(QuickActions attrib) {
		return attrib.ordinal();
	}

	public static AbstractAction getActionEnumById(int id) {
		switch (id) {
		case 0:
			return TabMainView.actionShowDescriptionView;
		case 1:
			return TabMainView.actionShowWaypointView;
		case 2:
			return TabMainView.actionShowLogView;
		case 3:
			return TabMainView.actionShowMap;
		case 4:
			return TabMainView.actionShowCompassView;
		case 5:
			return TabMainView.actionShowCacheList;
		case 6:
			return TabMainView.actionShowTrackListView;
		case 7:
			return action_TakePhoto;
		case 8:
			return action_TakeVideo;
		case 9:
			return action_Voicerec;
		case 10:
			return action_Search;
		case 11:
			return TabMainView.actionShowFilter;
		case 12:
			return action_SwitchAutoResort;
		case 13:
			return TabMainView.actionShowSolverView;
		case 14:
			return TabMainView.actionShowSpoilerView;
		case 15:
			return action_Hint;
		case 16:
			return TabMainView.actionParking;
		case 17:
			return action_DayNight;
		case 18:
			return TabMainView.actionShowFieldNotesView;
		case 19:
			return TabMainView.actionQuickFieldNote;
		case 20:
			return TabMainView.actionShowTrackableListView;
		case 21:
			return action_add_WP;
		case 22:
			return TabMainView.actionShowSolverView2;
		case 23:
			return TabMainView.actionShowNotesView;
		case 24:
			return CB_Action_UploadFieldNote.INSTANCE;
		case 25:
			return action_Torch;

		}
		return null;
	}

	public static String getName(int id) {
		switch (id) {
		case 0:
			return Translation.Get("Description");
		case 1:
			return Translation.Get("Waypoints");
		case 2:
			return Translation.Get("ShowLogs");
		case 3:
			return Translation.Get("Map");
		case 4:
			return Translation.Get("Compass");
		case 5:
			return Translation.Get("cacheList");
		case 6:
			return Translation.Get("Tracks");
		case 7:
			return Translation.Get("TakePhoto");
		case 8:
			return Translation.Get("RecVideo");
		case 9:
			return Translation.Get("VoiceRec");
		case 10:
			return Translation.Get("Search");
		case 11:
			return Translation.Get("filter");
		case 12:
			return Translation.Get("AutoResort");
		case 13:
			return Translation.Get("Solver");
		case 14:
			return Translation.Get("spoiler");
		case 15:
			return Translation.Get("hint");
		case 16:
			return Translation.Get("MyParking");
		case 17:
			return Translation.Get("DayNight");
		case 18:
			return Translation.Get("Fieldnotes");
		case 19:
			return Translation.Get("QuickFieldNote");
		case 20:
			return Translation.Get("TBList");
		case 21:
			return Translation.Get("AddWaypoint");
		case 22:
			return Translation.Get("Solver") + " 2";
		case 23:
			return Translation.Get("Notes");
		case 24:
			return Translation.Get("uploadFieldNotes");
		case 25:
			return Translation.Get("torch");

		}
		return "empty";
	}

	public static void InitialActions() {
		action_TakePhoto = new CB_Action_ShowActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO, Sprites.getSprite(IconName.log10icon.name()));

		action_TakeVideo = new CB_Action_ShowActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC, Sprites.getSprite(IconName.videoIcon.name()));

		action_Voicerec = new CB_Action_ShowActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC, Sprites.getSprite(IconName.voiceRecIcon.name()));

		action_Search = new CB_Action_Show_Search();
		action_add_WP = new CB_Action_add_WP();
		// action_ScreenLock = new CB_Action_ShowActivity("screenlock", MenuID.AID_LOCK, ViewConst.LOCK, SpriteCache.Icons.get(14));

	}

	private static AbstractAction action_TakePhoto;
	private static AbstractAction action_TakeVideo;

	private static AbstractAction action_Voicerec;
	private static AbstractAction action_Search;
	private static AbstractAction action_add_WP;

	private static AbstractAction action_SwitchAutoResort = new CB_Action_switch_Autoresort();
	private static AbstractAction action_Hint = new CB_Action_ShowHint();
	private static AbstractAction action_DayNight = new CB_Action_switch_DayNight();
	private static AbstractAction action_Torch = new CB_Action_switch_Torch();
}
