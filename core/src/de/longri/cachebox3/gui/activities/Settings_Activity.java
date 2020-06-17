/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;
import de.longri.cachebox3.gui.menu.QuickAction;
import de.longri.cachebox3.gui.skin.styles.FileChooserStyle;
import de.longri.cachebox3.gui.skin.styles.SelectBoxStyle;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.ApiButton;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.FloatControl;
import de.longri.cachebox3.gui.widgets.SelectBox;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.types.*;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.SoundCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectionType.NONE;

/**
 * Created by Longri on 24.08.2016.
 */
public class Settings_Activity extends ActivityBase {

    final static Logger log = LoggerFactory.getLogger(Settings_Activity.class);
    private static final boolean FORCE = true;
    private final SettingsActivityStyle style;
    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            Config.LoadFromLastValue();
            finish();
        }
    };
    private final Array<WidgetGroup> listViews = new Array<>();
    private final Array<CharSequence> listViewsNames = new Array<>();
    private final Array<ClickListener> listBackClickListener = new Array<>();
    private Label.LabelStyle nameStyle, descStyle, defaultValueStyle, valueStyle;
    private CB_Button btnOk, btnCancel, btnMenu;
    private float itemWidth;

    public Settings_Activity() {
        super("Settings_Activity");
        style = VisUI.getSkin().get(SettingsActivityStyle.class);
        setStageBackground(style.background);
        createButtons();
    }

    @Override
    public void onShow() {
        log.debug("show Settings");
        Config.SaveToLastValue();
        fillContent();
    }

    @Override
    public void layout() {
        super.layout();

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);
        x -= CB.scaledSizes.MARGIN + btnMenu.getWidth();

        btnMenu.setPosition(x, y);
        x -= CB.scaledSizes.MARGIN + btnOk.getWidth();

        btnOk.setPosition(x, y);

        itemWidth = getWidth() - (CB.scaledSizes.MARGINx2 + CB.scaledSizes.MARGIN_HALF);

        log.debug("Layout Settings");
    }

    private void createButtons() {

        btnOk = new CB_Button(Translation.get("save"));
        btnMenu = new CB_Button("...");
        btnCancel = new CB_Button(Translation.get("cancel"));

        addActor(btnOk);
        addActor(btnMenu);
        addActor(btnCancel);

        btnMenu.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Menu icm = new Menu("SettingsLevelTitle");
                boolean normal = !Config.SettingsShowAll.getValue() && !Config.SettingsShowExpert.getValue();
                icm.addCheckableMenuItem("Settings_Normal", normal, () -> {
                    Config.SettingsShowAll.setValue(false);
                    Config.SettingsShowExpert.setValue(false);
                    layoutCurrentListView();
                });
                icm.addCheckableMenuItem("Settings_Expert", Config.SettingsShowExpert.getValue(), () -> {
                    Config.SettingsShowExpert.setValue(true);
                    Config.SettingsShowAll.setValue(false);
                    layoutCurrentListView();
                });
                icm.addCheckableMenuItem("Settings_All", Config.SettingsShowAll.getValue(), () -> {
                    Config.SettingsShowAll.setValue(true);
                    Config.SettingsShowExpert.setValue(false);
                    layoutCurrentListView();
                });
                icm.show();
            }
        });


        btnOk.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
// todo implement  QuickButton config setting

//                String ActionsString = "";
//                int counter = 0;
//                for (int i = 0, n = SettingsItem_QuickButton.tmpQuickList.size(); i < n; i++) {
//                    QuickButtonItem tmp = SettingsItem_QuickButton.tmpQuickList.get(i);
//                    ActionsString += String.valueOf(tmp.getAction().ordinal());
//                    if (counter < SettingsItem_QuickButton.tmpQuickList.size() - 1) {
//                        ActionsString += ",";
//                    }
//                    counter++;
//                }
//                Config.quickButtonList.setValue(ActionsString);
                Config.AcceptChanges();
                finish();
            }
        });


        btnCancel.addListener(cancelClickListener);
        CB.stageManager.registerForBackKey(cancelClickListener);
    }

    private void fillContent() {

        //set LabelStyles
        nameStyle = new Label.LabelStyle();
        nameStyle.font = style.nameFont;
        nameStyle.fontColor = style.nameFontColor;

        descStyle = new Label.LabelStyle();
        descStyle.font = style.descFont;
        descStyle.fontColor = style.descFontColor;

        defaultValueStyle = new Label.LabelStyle();
        defaultValueStyle.font = style.defaultValueFont;
        defaultValueStyle.fontColor = style.defaultValueFontColor;

        valueStyle = new Label.LabelStyle();
        valueStyle.font = style.valueFont;
        valueStyle.fontColor = style.valueFontColor;


        final Array<SettingCategory> settingCategories = new Array<>();
        for (SettingCategory item : SettingCategory.values()) {
            if (item != SettingCategory.Button) {
                //add only non empty
                if (getSettingsOfCategory(item).size > 0)
                    settingCategories.add(item);
            }
        }


        final ListViewAdapter settingCategoriesListViewAdapter = new ListViewAdapter() {
            @Override
            public int getCount() {
                return settingCategories.size;
            }

            @Override
            public ListViewItem getView(int index) {
                final SettingCategory category = settingCategories.get(index);
                return getCategoryItem(index, category);
            }

            @Override
            public void update(ListViewItem view) {

            }

        };

        final ListView settingCategoriesListView = new ListView(VERTICAL);
        settingCategoriesListView.setSelectionType(NONE);
        CB.postOnNextGlThread(() -> {
            settingCategoriesListView.setAdapter(settingCategoriesListViewAdapter);
            showListView(settingCategoriesListView, Translation.get("SettingsTitle"), true);
        });
    }

    private void showListView(ListView settingCategoriesListView, CharSequence settingsTitle, boolean animate) {

        float y = btnOk.getY() + btnOk.getHeight() + CB.scaledSizes.MARGIN;


        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.setBounds(CB.scaledSizes.MARGIN, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGIN));

        // title
        WidgetGroup titleGroup = new WidgetGroup();
        float xPos = 0;

        ClickListener backClickListener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                backClick();
            }
        };

        // add the titleLabel on top
        if (style.backIcon != null && listViewsNames.size > 0) {
            Image backImage = new Image(style.backIcon);
            backImage.setPosition(xPos, 0);
            xPos += backImage.getWidth() + CB.scaledSizes.MARGIN;
            titleGroup.addActor(backImage);
        }

        VisLabel titleLabel = new VisLabel(settingsTitle, "menu_title_act");

        if (listViewsNames.size > 0) {
            VisLabel parentTitleLabel = new VisLabel(listViewsNames.get(listViewsNames.size - 1), "menu_title_parent");
            parentTitleLabel.setPosition(xPos, 0);
            xPos += parentTitleLabel.getWidth() + CB.scaledSizes.MARGINx2;
            titleGroup.addActor(parentTitleLabel);
        } else {
            //center titleLabel
            xPos = (Gdx.graphics.getWidth() - titleLabel.getWidth()) / 2;
        }

        titleLabel.setPosition(xPos, 0);
        titleGroup.addActor(titleLabel);

        float titleHeight = titleLabel.getHeight() + CB.scaledSizes.MARGIN;
        titleGroup.setBounds(0, Gdx.graphics.getHeight() - (y + titleHeight), Gdx.graphics.getWidth(), titleHeight);
        titleGroup.addListener(backClickListener);

        widgetGroup.addActor(titleGroup);

        settingCategoriesListView.setBounds(0, 0, widgetGroup.getWidth(), titleGroup.getY() - CB.scaledSizes.MARGIN);
        settingCategoriesListView.layout();
        settingCategoriesListView.setBackground(null); // remove default background

        widgetGroup.addActor(settingCategoriesListView);

        if (listViews.size > 0) {
            // animate
            float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;
            if (animate) {
                listViews.get(listViews.size - 1).addAction(Actions.moveTo(0 - nextXPos, y, Menu.MORE_MENU_ANIMATION_TIME));
                widgetGroup.setPosition(nextXPos, y);
                widgetGroup.addAction(Actions.moveTo(CB.scaledSizes.MARGIN, y, Menu.MORE_MENU_ANIMATION_TIME));
            } else {
                widgetGroup.setPosition(CB.scaledSizes.MARGIN, y);
            }
        }
        listViews.add(widgetGroup);
        listViewsNames.add(settingsTitle);
        listBackClickListener.add(backClickListener);
        CB.stageManager.registerForBackKey(backClickListener);

        addActor(widgetGroup);
    }

    private void backClick() {

        float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;

        if (listViews.size == 1) {
            // remove all BackClickListener
            while (listBackClickListener.size > 0) {
                CB.stageManager.unRegisterForBackKey(listBackClickListener.pop());
            }

            //Send click to Cancel button
            cancelClickListener.clicked(StageManager.BACK_KEY_INPUT_EVENT, -1, -1);
            return;
        }

        CB.stageManager.unRegisterForBackKey(listBackClickListener.pop());
        listViewsNames.pop();
        WidgetGroup actWidgetGroup = listViews.pop();
        WidgetGroup showingWidgetGroup = listViews.get(listViews.size - 1);

        float y = actWidgetGroup.getY();
        actWidgetGroup.addAction(Actions.sequence(Actions.moveTo(nextXPos, y, Menu.MORE_MENU_ANIMATION_TIME), Actions.removeActor()));
        showingWidgetGroup.addAction(Actions.moveTo(CB.scaledSizes.MARGIN, y, Menu.MORE_MENU_ANIMATION_TIME));
    }

    private void layoutCurrentListView() {
        //get act listView
        WidgetGroup widgetGroup = listViews.get(listViews.size - 1);
        for (Actor actor : widgetGroup.getChildren()) {
            Object object = actor.getUserObject();
            if (object instanceof SettingCategory) {
                WidgetGroup group = listViews.pop();
                listViewsNames.pop();
                //remove all Listeners
                for (Actor child : group.getChildren())
                    for (EventListener listener : child.getListeners())
                        child.removeListener(listener);
                removeActor(group);
                showCategory((SettingCategory) object, false);
                break;
            }
        }
    }

    private ListViewItem getCategoryItem(int listIndex, final SettingCategory category) {
        ListViewItem table = new ListViewItem(listIndex) {
            @Override
            public void dispose() {
            }
        };

        // add label with category name, align left
        table.left();


        VisLabel label = new VisLabel(Translation.get(category.name()));
        label.setAlignment(Align.left);
        table.add(label).pad(CB.scaledSizes.MARGIN).expandX().fillX();

        // add next icon
        Image next = new Image(style.nextIcon);
        table.add(next).width(next.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add clicklistener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    showCategory(category, true);
                    event.cancel();
                    event.handle();
                }
            }
        });
        return table;
    }

    private void showCategory(final SettingCategory category, final boolean animate) {
        log.debug("show settings categoriy: " + category.name());

        final ListViewAdapter listViewAdapter;
        final Array<SettingBase<?>> categorySettingsList = getSettingsOfCategory(category);


        if (category == SettingCategory.Login) {
            SettingsListGetApiButton<?> lgIn = new SettingsListGetApiButton<>(category.name(), SettingCategory.Button, SettingMode.Normal, SettingStoreType.Global, SettingUsage.ACB);
            categorySettingsList.add(lgIn);
        }

        //add only items they are not NULL
        final Array<ListViewItem> items = new Array<>();
        int idxCount = 0;
        for (SettingBase<?> setting : categorySettingsList) {
            ListViewItem listViewItem = getSettingItem(idxCount, setting);
            if (listViewItem != null) {
                items.add(listViewItem);
                idxCount++;
            }
        }

        // show new ListView for this category
        listViewAdapter = new ListViewAdapter() {
            @Override
            public int getCount() {
                return items.size;
            }

            @Override
            public ListViewItem getView(int index) {
                return items.get(index);
            }

            @Override
            public void update(ListViewItem item) {

            }
        };

        final ListView newListView = new ListView(VERTICAL);
        newListView.setSelectionType(NONE);
        CB.postOnNextGlThread(() -> {
            newListView.setAdapter(listViewAdapter);
            newListView.setUserObject(category);
            showListView(newListView, Translation.get(category.name()), animate);
        });


    }

    private Array<SettingBase<?>> getSettingsOfCategory(SettingCategory category) {
        //get all settings items of this category if the category mode correct
        final Array<SettingBase<?>> categorySettingsList = new Array<>();
        boolean expert = Config.SettingsShowAll.getValue() || Config.SettingsShowExpert.getValue();
        boolean developer = Config.SettingsShowAll.getValue();

        for (SettingBase<?> setting : Config.settingsList) {
            if (setting.getCategory() == category) {
                boolean show = false;

                switch (setting.getMode()) {
                    case DEVELOPER:
                        show = developer;
                        break;
                    case Normal:
                        show = true;
                        break;
                    case Expert:
                        show = expert;
                        break;
                }

                if (show) {
                    categorySettingsList.add(setting);
                    log.debug("with setting for: " + setting.getName());
                }
            }
        }
        return categorySettingsList;
    }

    private ListViewItem getSettingItem(int listIndex, SettingBase<?> setting) {
        if (setting instanceof SettingBool) {
            return getBoolView(listIndex, (SettingBool) setting);
        } else if (setting instanceof SettingIntArray) {
            return getIntArrayView(listIndex, (SettingIntArray) setting);
        } else if (setting instanceof SettingStringArray) {
            return getStringArrayView(listIndex, (SettingStringArray) setting);
        } else if (setting instanceof SettingTime) {
            return getTimeView(listIndex, (SettingTime) setting);
        } else if (setting instanceof SettingInt) {
            return getIntView(listIndex, (SettingInt) setting);
        } else if (setting instanceof SettingDouble) {
            return getDblView(listIndex, (SettingDouble) setting);
        } else if (setting instanceof SettingFloat) {
            return getFloatView(listIndex, (SettingFloat) setting);
        } else if (setting instanceof SettingFolder) {
            return getFolderView(listIndex, (SettingFolder) setting);
        } else if (setting instanceof SettingFile) {
            return getFileView(listIndex, (SettingFile) setting);
        } else if (setting instanceof SettingEnum<?>) {
            return getEnumView(listIndex, (SettingEnum<?>) setting);
        } else if (setting instanceof SettingString) {
            return getStringView(listIndex, (SettingString) setting);
//        } else if (setting instanceof SettingsListCategoryButton) {
//            return getButtonView((SettingsListCategoryButton<?>) setting);
        } else if (setting instanceof SettingsListGetApiButton) {
            return getApiKeyButtonView(listIndex);
//        } else if (setting instanceof SettingsListButtonLangSpinner) {
//            return getLangSpinnerView((SettingsListButtonLangSpinner<?>) setting);
//        } else if (setting instanceof SettingsListButtonSkinSpinner) {
//            return getSkinSpinnerView((SettingsListButtonSkinSpinner<?>) setting);
        } else if (setting instanceof SettingsAudio) {
            return getAudioView(listIndex, (SettingsAudio) setting);
        } else if (setting instanceof SettingColor) {
            return getColorView(listIndex, (SettingColor) setting);
        }
        return null;
    }

    private ListViewItem getApiKeyButtonView(int listIndex) {
        ListViewItem table = new ListViewItem(listIndex) {
            @Override
            public void dispose() {
            }
        };
        float buttonWidth = getWidth() - (CB.scaledSizes.MARGINx2 * 2);
        final ApiButton apiButton = new ApiButton();
        table.add(apiButton).width(new Value.Fixed(buttonWidth)).center();
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    GetApiKey_Activity activity = new GetApiKey_Activity();
                    activity.show();
                    event.cancel();
                    event.handle();
                }
            }
        });
        return table;
    }

    private ListViewItem getColorView(int listIndex, SettingColor setting) {
        ListViewItem table = createItem(listIndex, setting.getName());

        final CB_Button btnColor = new CB_Button(Translation.get("select"));
        btnColor.setColor(setting.getValue());
        table.add(btnColor).pad(CB.scaledSizes.MARGIN / 2);

        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    ColorPicker.getInstance("ColorPickerTitle",
                            CB.getSkin().menuIcon.showOriginalHtmlColor).execute(setting.getValue(),
                            new ChangeListener() {
                                @Override
                                public void changed(ChangeEvent event, Actor actor) {
                                    ColorPicker colorPicker = (ColorPicker) actor;
                                    setting.setValue(colorPicker.getColorValue());
                                    btnColor.setColor(setting.getValue());
                                }
                            });
                    event.stop();
                    event.cancel();
                    event.handle();
                }
            }
        });

        addDescriptionAndDefaultInfo(table, setting.getName(), setting.getDefaultValue().toString());
        return table;
    }

    private ListViewItem getAudioView(int listIndex, final SettingsAudio setting) {
        final String audioName = setting.getName();
        ListViewItem table = createItem(listIndex, audioName);

        VisTable nameSliderTable = new VisTable();
        final FloatControl floatControl = new FloatControl(0f, 1f, 0.001f, true, (value, dragged) -> {
            if (!dragged) {
                //TODO set value as property with change to setting.dirty
                Audio newAudio = new Audio(setting.getValue());
                newAudio.Volume = value;
                setting.setValue(newAudio);
                if (audioName.equalsIgnoreCase("GlobalVolume"))
                    SoundCache.play(SoundCache.Sounds.Global, true);
                if (audioName.equalsIgnoreCase("Approach"))
                    SoundCache.play(SoundCache.Sounds.Approach, true);
                if (audioName.equalsIgnoreCase("GPS_lose"))
                    SoundCache.play(SoundCache.Sounds.GPS_lose, true);
                if (audioName.equalsIgnoreCase("GPS_fix"))
                    SoundCache.play(SoundCache.Sounds.GPS_fix, true);
                if (audioName.equalsIgnoreCase("AutoResortSound"))
                    SoundCache.play(SoundCache.Sounds.AutoResortSound, true);
            }
        });
        nameSliderTable.add(floatControl).expandX().fillX();
        floatControl.setValue(setting.getValue().Volume);
        table.row();
        table.add(nameSliderTable).pad(CB.scaledSizes.MARGIN).expandX().fillX();

        // add check icon
        final Image[] checkImage = new Image[1];


        if (setting.getValue().Mute) {
            checkImage[0] = new Image(style.soundMute);
        } else {
            checkImage[0] = new Image(style.soundOn);
        }
        table.add(checkImage[0]).width(checkImage[0].getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add clicklistener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {

                    //if clicked on Mute control?

                    if (event.getTarget() == checkImage[0]) {
                        Audio newAudio = new Audio(setting.getValue());
                        newAudio.Mute = !newAudio.Mute;
                        setting.setValue(newAudio);
                        checkImage[0].setDrawable(newAudio.Mute ? style.soundMute : style.soundOn);
                    } else {
                        //play sound

                        if (audioName.equalsIgnoreCase("GlobalVolume"))
                            SoundCache.play(SoundCache.Sounds.Global, true);
                        if (audioName.equalsIgnoreCase("Approach"))
                            SoundCache.play(SoundCache.Sounds.Approach, true);
                        if (audioName.equalsIgnoreCase("GPS_lose"))
                            SoundCache.play(SoundCache.Sounds.GPS_lose, true);
                        if (audioName.equalsIgnoreCase("GPS_fix"))
                            SoundCache.play(SoundCache.Sounds.GPS_fix, true);
                        if (audioName.equalsIgnoreCase("AutoResortSound"))
                            SoundCache.play(SoundCache.Sounds.AutoResortSound, true);
                    }
                    event.stop();
                    event.cancel();
                    event.handle();
                }
            }
        });

        addDescriptionAndDefaultInfo(table, setting.getName(), (int) (100 * setting.getDefaultValue().Volume) + "%");

        return table;
    }

    private ListViewItem getStringView(int listIndex, final SettingString setting) {
        ListViewItem table = createItem(listIndex, setting.getName());
        table.row();

        String value = "";
        String defaultValue = "";
        // add value presentation
        if (setting.equals(Config.quickButtonList)) {
            String configActionList = Config.quickButtonList.getValue();
            String[] configList = configActionList.split(",");
            if (configList.length > 0) {
                for (String s : configList) {
                    try {
                        s = s.replace(",", "");
                        int ordinal = Integer.parseInt(s);
                        if (ordinal > -1 && ordinal < QuickAction.values().length - 1) {
                            // the last QuickAction value is "empty"
                            AbstractAction action = QuickAction.values()[ordinal].getAction();
                            if (action != null)
                                value = value + "\n" + Translation.get(action.getTitleTranslationId()); // + String.format(Locale.US, "%2d", ordinal) + "= "
                        }
                    } catch (Exception e) {
                        log.error("getListFromConfig", e);
                    }
                }
            }
        } else {
            value = setting.getValue();
            defaultValue = setting.getDefaultValue();
        }
        final VisLabel valuelabel = new VisLabel( Translation.get("value") + ": " + value, valueStyle);
        valuelabel.setWrap(true);
        valuelabel.setAlignment(Align.left);
        table.add(valuelabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        addDescriptionAndDefaultInfo(table, setting.getName(), defaultValue);
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    if (setting.equals(Config.quickButtonList)) {
                        EditQuickButtonList.getInstance(setting.getName(), null).execute(
                                new ChangeListener() {
                                    @Override
                                    public void changed(ChangeEvent event, Actor actor) {
                                        backClick();
                                        showCategory(setting.getCategory(), true);
                                    }
                                });
                    } else {
                        // show multi line input dialog
                        PlatformConnector.getMultilineTextInput(new Input.TextInputListener() {
                            @Override
                            public void input(String text) {
                                setting.setValue(text);
                                backClick();
                                showCategory(setting.getCategory(), true);
                            }

                            @Override
                            public void canceled() {

                            }
                        }, 0, Translation.get(setting.getName()).toString(), setting.getValue(), "");
                    }

                    event.cancel();
                    event.handle();
                }
            }
        });
        return table;
    }

    private ListViewItem getEnumView(int listIndex, final SettingEnum<?> setting) {

        Array<Enum<?>> itemList = new Array<>();
        Enum<?> selectedItem = setting.getEnumValue();
        Class<?> declaringClass = selectedItem.getDeclaringClass();
        Object[] oo = declaringClass.getEnumConstants();
        int selectIndex = 0;
        int index = 0;
        for (Object o : oo) {
            itemList.add((Enum<?>) o);
            if (o == selectedItem) selectIndex = index;
            index++;
        }

        SelectBoxStyle style = VisUI.getSkin().get(SelectBoxStyle.class);
        style.up = null;
        style.down = null;
        style.font = valueStyle.font;
        style.fontColor = valueStyle.fontColor;

        final SelectBox selectBox = new SelectBox(style, null);
        selectBox.set(itemList);
        if (setting == Config.localization) {
            selectBox.setPrefix(Translation.get("SelectLanguage") + ":  ");
        }
        selectBox.select(selectIndex);
        final AtomicBoolean callBackClick = new AtomicBoolean(false);
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Enum<?> selected = (Enum<?>) selectBox.getSelected();
                setting.setEnumValue(selected);
                if (event.getStage() == null || callBackClick.get()) {
                    callBackClick.set(false);
                    backClick();
                }
            }
        });
        selectBox.setHideWithItemClick(false);

        ListViewItem table = createItem(listIndex, setting.getName());
        table.row();

        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                //show select menu
                Menu menu = selectBox.getMenu();
                showListView(menu.getListview(), Translation.get("select"), true);
                CB.postOnNextGlThread(new NamedRunnable("postOnGlThread") {
                    @Override
                    public void run() {
                        callBackClick.set(true);
                    }
                });
            }
        });

        table.add(selectBox).width(new Value.Fixed(getWidth() - (CB.scaledSizes.MARGINx2 * 2))).center();

        String defaultValue = Translation.get(setting.getDefaultValue()).toString();
        if (defaultValue.startsWith("$ID")) defaultValue = setting.getDefaultValue();
        addDescriptionAndDefaultInfo(table, setting.getName(), defaultValue);

        return table;
    }

    private ListViewItem getFileView(int listIndex, SettingFile setting) {
        ListViewItem table = createItem(listIndex, setting.getName());

        FileChooserStyle style = VisUI.getSkin().get(FileChooserStyle.class);
        Image folderIcon = new Image(style.fileIcon);
        table.add(folderIcon).width(folderIcon.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add value line
        table.row();
        final VisLabel valuelabel = new VisLabel(Translation.get("value") + ": " + setting.getValue(), valueStyle);
        valuelabel.setWrap(true);
        valuelabel.setAlignment(Align.left);
        table.add(valuelabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        if (setting.isDefault()) {
            valuelabel.setText("Default");
        }

        addDescriptionAndDefaultInfo(table, setting.getName(), setting.getDefaultValue() + "\n");

        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    Menu selectOrClearMenu = new Menu("SelectFileTitle");
                    selectOrClearMenu.addMenuItem("select_file", style.fileIcon, () -> {
                        FileChooser fileChooser = new FileChooser(Translation.get("select_file"), FileChooser.SelectionMode.FILES);
                        fileChooser.setSelectionReturnListener(fileHandle -> {
                            if (fileHandle == null) return;
                            setting.setValue(fileHandle.path());
                            valuelabel.setText(Translation.get("value") + ": " + setting.getValue());
                        });
                        fileChooser.setDirectory(CB.WorkPathFileHandle, false);
                        fileChooser.show();
                    });
                    selectOrClearMenu.addMenuItem("ClearPath", style.deleteBtnIcon, () -> {
                        setting.setValue(setting.getDefaultValue());
                        valuelabel.setText("Default");
                    });
                    selectOrClearMenu.show();
                }
            }
        });


        table.setWidth(itemWidth);
        table.setPrefWidth(itemWidth);
        table.invalidate();
        table.pack();

        int rows = table.getRows();
        float calcHeight = 0;
        float pad = CB.scaledSizes.MARGIN;
        for (int i = 0; i < rows; i++) {
            calcHeight += table.getRowPrefHeight(i);
            calcHeight += pad;
        }
        table.setFinalHeight(calcHeight);
        return table;
    }

    private ListViewItem getFolderView(int listIndex, final SettingFolder setting) {
        ListViewItem table = createItem(listIndex, setting.getName());

        FileChooserStyle style = VisUI.getSkin().get(FileChooserStyle.class);
        Image folderIcon = new Image(style.folderIcon);
        table.add(folderIcon).width(folderIcon.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add value line
        table.row();
        final VisLabel valuelabel = new VisLabel(Translation.get("value") + ": " + setting.getValue(), valueStyle);
        valuelabel.setWrap(true);
        valuelabel.setAlignment(Align.left);
        table.add(valuelabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        if (setting.isDefault()) {
            valuelabel.setText("Default");
        }

        addDescriptionAndDefaultInfo(table, setting.getName(), setting.getDefaultValue());

        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    Menu selectClearMenu = new Menu("SelectPathTitle");
                    selectClearMenu.addMenuItem("select_folder", null, () -> {
                        FileChooser folderChooser = new FileChooser(Translation.get("selectFolder"), FileChooser.SelectionMode.DIRECTORIES);
                        folderChooser.setSelectionReturnListener(fileHandle -> {
                            if (fileHandle == null) return;
                            // check WriteProtection
                            String path = fileHandle.file().getAbsolutePath();
                            if (setting.needWritePermission() && !Utils.checkWritePermission(path)) {
                                CharSequence WriteProtectionMsg = Translation.get("NoWriteAcces");
                                CB.viewmanager.toast(WriteProtectionMsg, ViewManager.ToastLength.EXTRA_LONG);
                            } else {
                                setting.setValue(path);
                                valuelabel.setText(Translation.get("value") + ": " + setting.getValue());
                            }
                        });
                        folderChooser.setDirectory(CB.WorkPathFileHandle, true);
                        folderChooser.show();
                    });
                    selectClearMenu.addMenuItem("ClearPath", null, () -> {
                        setting.setValue(setting.getDefaultValue());
                        valuelabel.setText("Default");
                    });
                    selectClearMenu.show();
                }
            }
        });


        table.setWidth(itemWidth);
        table.setPrefWidth(itemWidth);
        table.invalidate();
        table.pack();

        int rows = table.getRows();
        float calcHeight = 0;
        float pad = CB.scaledSizes.MARGIN;
        for (int i = 0; i < rows; i++) {
            calcHeight += table.getRowPrefHeight(i);
            calcHeight += pad;
        }
        table.setFinalHeight(calcHeight);
        return table;
    }

    private ListViewItem getFloatView(int listIndex, final SettingFloat setting) {
        final VisLabel valueLabel = new VisLabel(Float.toString(setting.getValue()), valueStyle);
        ListViewItem table = getNumericItemTable(listIndex, valueLabel, setting);

        // add clickListener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    new NumericInput_Activity<Float>(setting.getValue()) {
                        public void returnValue(Float value) {
                            setting.setValue(value);
                            valueLabel.setText(Float.toString(value));
                            WidgetGroup group = listViews.peek();
                            for (Actor actor : group.getChildren()) {
                                if (actor instanceof ListView) {
                                    final ListView listView = (ListView) actor;
                                    final float scrollPos = listView.getScrollPos();
                                    listView.layout();
                                    Gdx.app.postRunnable(() -> listView.setScrollPos(scrollPos));
                                }
                            }
                        }
                    }.show();
                }
            }
        });

        return table;
    }

    private ListViewItem getDblView(int listIndex, final SettingDouble setting) {
        final VisLabel valueLabel = new VisLabel(Double.toString(setting.getValue()), valueStyle);
        ListViewItem table = getNumericItemTable(listIndex, valueLabel, setting);

        // add clickListener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    new NumericInput_Activity<Double>(setting.getValue()) {
                        public void returnValue(Double value) {
                            setting.setValue(value);
                            valueLabel.setText(Double.toString(value));
                            WidgetGroup group = listViews.peek();
                            for (Actor actor : group.getChildren()) {
                                if (actor instanceof ListView) {
                                    final ListView listView = (ListView) actor;
                                    final float scrollPos = listView.getScrollPos();
                                    listView.layout();
                                    Gdx.app.postRunnable(() -> listView.setScrollPos(scrollPos));
                                }
                            }
                        }
                    }.show();
                }
            }
        });

        return table;
    }

    private ListViewItem getIntView(int listIndex, final SettingInt setting) {
        final VisLabel valueLabel = new VisLabel(Integer.toString(setting.getValue()), valueStyle);
        final ListViewItem table = getNumericItemTable(listIndex, valueLabel, setting);

        // add clickListener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    new NumericInput_Activity<Integer>(setting.getValue()) {
                        public void returnValue(Integer value) {
                            setting.setValue(value);
                            valueLabel.setText("" + value);
                            WidgetGroup group = listViews.peek();
                            for (Actor actor : group.getChildren()) {
                                if (actor instanceof ListView) {
                                    final ListView listView = (ListView) actor;
                                    final float scrollPos = listView.getScrollPos();
                                    listView.layout();
                                    Gdx.app.postRunnable(() -> listView.setScrollPos(scrollPos));

                                }
                            }
                        }
                    }.show();
                }
            }
        });

        table.setWidth(itemWidth);
        table.setPrefWidth(itemWidth);
        table.invalidate();
        table.pack();
        int rows = table.getRows();
        float calcHeight = 0;
        float pad = CB.scaledSizes.MARGIN;
        for (int i = 0; i < rows; i++) {
            calcHeight += table.getRowPrefHeight(i);
            calcHeight += pad;
        }
        table.setFinalHeight(calcHeight);
        return table;
    }

    private ListViewItem getTimeView(int listIndex, SettingTime setting) {
        return null;
    }

    private ListViewItem getStringArrayView(int listIndex, SettingStringArray setting) {
        // not necessary
        return null;
    }

    private ListViewItem getIntArrayView(int listIndex, SettingIntArray setting) {

        SelectBoxStyle style = VisUI.getSkin().get(SelectBoxStyle.class);
        style.up = null;
        style.down = null;
        style.font = valueStyle.font;
        style.fontColor = valueStyle.fontColor;

        final AtomicBoolean callBackClick = new AtomicBoolean(false);
        final SelectBox selectBox = new SelectBox(style, null);

        int currentSelectedIndex = 0;
        Array<SelectBoxItem> itemList = new Array<>();
        for (Integer i : setting.getValues()) {
            itemList.add(new SelectBoxItem() {
                @Override
                public Drawable getDrawable() {
                    return null;
                }

                @Override
                public String getName() {
                    return "" + i;
                }
            });
            if (setting.getValue() == i) {
                currentSelectedIndex = setting.getIndex();
            }
        }

        selectBox.setPrefix(Translation.get(setting.getName()) + " :  ");
        selectBox.set(itemList);
        selectBox.select(currentSelectedIndex);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SelectBoxItem selected = selectBox.getSelected();
                setting.setValue(Integer.parseInt(selected.getName()));
                if (event.getStage() == null || callBackClick.get()) {
                    callBackClick.set(false);
                    backClick();
                }
            }
        });
        selectBox.setHideWithItemClick(false);

        ListViewItem table = new ListViewItem(listIndex) {
            @Override
            public void dispose() {
            }
        };

        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                //show select menu
                Menu menu = selectBox.getMenu();
                showListView(menu.getListview(), Translation.get("select"), true);
                CB.postOnNextGlThread(new NamedRunnable("selectBox clicked callBack") {
                    @Override
                    public void run() {
                        callBackClick.set(true);
                    }
                });
            }
        });

        table.add(selectBox).width(new Value.Fixed(getWidth() - (CB.scaledSizes.MARGINx2 * 2))).center();

        addDescriptionAndDefaultInfo(table, setting.getName(), setting.getDefaultValue().toString());
        return table;
    }

    private ListViewItem getBoolView(int listIndex, final SettingBool setting) {
        ListViewItem table = createItem(listIndex, setting.getName());

        // add check icon
        final Image[] checkImage = new Image[1];
        if (setting.getValue()) {
            checkImage[0] = new Image(style.checkOn, Scaling.none);
        } else {
            checkImage[0] = new Image(style.checkOff, Scaling.none);
        }
        table.add(checkImage[0]).width(checkImage[0].getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add clicklistener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.isHandled() || event.isCancelled()) return;
                if (event.getType() == InputEvent.Type.touchUp) {
                    setting.setValue(!setting.getValue());
                    if (setting.getValue()) {
                        checkImage[0].setDrawable(style.checkOn);
                    } else {
                        checkImage[0].setDrawable(style.checkOff);
                    }
                    event.cancel();
                    event.handle();
                }
            }
        });

        CompoundCharSequence defaultValue = setting.getDefaultValue() ? Translation.get("yes") : Translation.get("no");
        addDescriptionAndDefaultInfo(table, setting.getName(), defaultValue.toString());
        table.setWidth(itemWidth);
        table.setPrefWidth(itemWidth);
        table.invalidate();
        table.pack();
        int rows = table.getRows();
        float calcHeight = 0;
        float pad = CB.scaledSizes.MARGIN;
        for (int i = 0; i < rows; i++) {
            calcHeight += table.getRowPrefHeight(i);
            calcHeight += pad;
        }
        table.setFinalHeight(calcHeight);
        return table;
    }

    private ListViewItem getNumericItemTable(int listIndex, final VisLabel valueLabel, final SettingBase<?> setting) {
        setting.addChangedEventListener(() -> valueLabel.setText(setting.getValue().toString()));
        ListViewItem table = createItem(listIndex, setting.getName());
        table.add(valueLabel).width(valueLabel.getWidth()).pad(CB.scaledSizes.MARGIN / 2);
        addDescriptionAndDefaultInfo(table, setting.getName(), setting.getDefaultValue().toString());
        return table;
    }

    private ListViewItem createItem(int listIndex, String name) {
        ListViewItem table = new ListViewItem(listIndex) {
            @Override
            public void dispose() {
            }
        };
        table.left();
        VisLabel label = new VisLabel(Translation.get(name), nameStyle);
        label.setWrap(true);
        label.setAlignment(Align.left);
        table.add(label).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        return table;
    }

    private void addDescriptionAndDefaultInfo(ListViewItem table, String settingName, String settingDefaultValue) {
        // add description line if description exist
        CharSequence description = Translation.get("Desc_" + settingName);
        if (!CharSequenceUtil.contains(description, "$ID:")) {
            table.row();
            VisLabel desclabel = new VisLabel(description, descStyle);
            desclabel.setWrap(true);
            desclabel.setAlignment(Align.left);
            table.add(desclabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        }

        // add defaultValue line
        if (settingDefaultValue.length() > 0) {
            table.row();
            VisLabel desclabel = new VisLabel(Translation.get("default") + ": " + settingDefaultValue, defaultValueStyle);
            desclabel.setWrap(true);
            desclabel.setAlignment(Align.left);
            table.add(desclabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
    }

    public static class SettingsActivityStyle extends ActivityBaseStyle {
        public Drawable nextIcon, backIcon, checkOn, checkOff, soundOn, soundMute, option_select, option_back;
        public BitmapFont nameFont, descFont, defaultValueFont, valueFont;
        public Color nameFontColor, descFontColor, defaultValueFontColor, valueFontColor;
    }

}
