/*
 * Copyright (C) 2016 team-cachebox.de
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.menu.OnItemClickListener;
import de.longri.cachebox3.gui.views.ListView;
import de.longri.cachebox3.settings.*;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.08.2016.
 */
public class Settings_Activity extends ActivityBase {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(Settings_Activity.class);
    private VisTextButton btnOk, btnCancel, btnMenu;
    private final SettingsActivityStyle style;

    public Settings_Activity() {
        super("Settings_Activity");
        this.style = VisUI.getSkin().get("default", SettingsActivityStyle.class);
        this.setStageBackground(style.background);
        createButtons();
    }

    @Override
    public void onShow() {
        log.debug("Show Settings");
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
        log.debug("Layout Settings");
    }


    private void createButtons() {

        btnOk = new VisTextButton(Translation.Get("save"));
        btnMenu = new VisTextButton("...");
        btnCancel = new VisTextButton(Translation.Get("cancel"));

        this.addActor(btnOk);
        this.addActor(btnMenu);
        this.addActor(btnCancel);

        btnMenu.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Menu icm = new Menu(Translation.Get("changeSettingsVisibility"));
                icm.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public boolean onItemClick(MenuItem item) {
                        switch (item.getMenuItemId()) {
                            case MenuID.MI_SHOW_EXPERT:
                                Config.SettingsShowExpert.setValue(!Config.SettingsShowExpert.getValue());
                                Config.SettingsShowAll.setValue(false);
                                layoutActListView(true);
                                return true;

                            case MenuID.MI_SHOW_ALL:
                                Config.SettingsShowAll.setValue(!Config.SettingsShowAll.getValue());
                                Config.SettingsShowExpert.setValue(false);
                                layoutActListView(true);
                                return true;
                            case MenuID.MI_SHOW_Normal:
                                Config.SettingsShowAll.setValue(false);
                                Config.SettingsShowExpert.setValue(false);
                                layoutActListView(true);
                                return true;
                        }
                        return false;
                    }
                });

                if (Config.SettingsShowAll.getValue())
                    Config.SettingsShowExpert.setValue(false);

                boolean normal = !Config.SettingsShowAll.getValue() && !Config.SettingsShowExpert.getValue();

                icm.addCheckableItem(MenuID.MI_SHOW_Normal, "Settings_Normal", normal);
                icm.addCheckableItem(MenuID.MI_SHOW_EXPERT, "Settings_Expert", Config.SettingsShowExpert.getValue());
                icm.addCheckableItem(MenuID.MI_SHOW_ALL, "Settings_All", Config.SettingsShowAll.getValue());
                icm.show();
            }

        });


        btnOk.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

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

                Config.SaveToLastValue();
                Config.AcceptChanges();
                finish();
            }
        });


        btnCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Config.LoadFromLastValue();
                finish();
            }
        });
    }


    private Array<WidgetGroup> listViews = new Array<WidgetGroup>();
    private Array<String> listViewsNames = new Array<String>();
    Label.LabelStyle nameStyle, descStyle, defaultValuStyle, valueStyle;


    private void fillContent() {

        //set LabelStyles
        nameStyle = new Label.LabelStyle();
        nameStyle.font = style.nameFont;
        nameStyle.fontColor = style.nameFontColor;

        descStyle = new Label.LabelStyle();
        descStyle.font = style.descFont;
        descStyle.fontColor = style.descFontColor;

        defaultValuStyle = new Label.LabelStyle();
        defaultValuStyle.font = style.defaultValueFont;
        defaultValuStyle.fontColor = style.defaultValueFontColor;

        valueStyle = new Label.LabelStyle();
        valueStyle.font = style.valueFont;
        valueStyle.fontColor = style.valueFontColor;


        final Array<SettingCategory> settingCategories = new Array<SettingCategory>();
        SettingCategory[] tmp = SettingCategory.values();
        for (SettingCategory item : tmp) {
            if (item != SettingCategory.Button) {
                settingCategories.add(item);
            }
        }
        showListView(new ListView(settingCategories.size) {
            @Override
            public VisTable createView(Integer index) {
                final SettingCategory category = settingCategories.get(index);
                return getCategoryItem(category);
            }
        }, Translation.Get("setting"), true);
    }

    private void showListView(ListView listView, String name, boolean animate) {

        float y = btnOk.getY() + btnOk.getHeight() + CB.scaledSizes.MARGIN;


        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.setBounds(CB.scaledSizes.MARGIN, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGIN));

        // title
        WidgetGroup titleGroup = new WidgetGroup();

        float topY = widgetGroup.getHeight() - CB.scaledSizes.MARGIN_HALF;
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

        VisLabel titleLabel = new VisLabel(name, "menu_title_act");

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


        listView.setBounds(0, 0, widgetGroup.getWidth(), titleGroup.getY() - CB.scaledSizes.MARGIN);
        listView.layout();
        listView.setBackground(null); // remove default background
        widgetGroup.addActor(listView);


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
        listViewsNames.add(name);
        this.addActor(widgetGroup);
    }

    private void backClick() {
        float nextXPos = Gdx.graphics.getWidth() + CB.scaledSizes.MARGIN;

        if (listViews.size == 1) return;

        listViewsNames.pop();
        WidgetGroup actWidgetGroup = listViews.pop();
        WidgetGroup showingWidgetGroup = listViews.get(listViews.size - 1);

        float y = actWidgetGroup.getY();
        actWidgetGroup.addAction(Actions.sequence(Actions.moveTo(nextXPos, y, Menu.MORE_MENU_ANIMATION_TIME), Actions.removeActor()));
        showingWidgetGroup.addAction(Actions.moveTo(CB.scaledSizes.MARGIN, y, Menu.MORE_MENU_ANIMATION_TIME));
    }

    private void layoutActListView(boolean itemCountChanged) {
        //get act listView
        WidgetGroup widgetGroup = listViews.get(listViews.size - 1);
        ListView actListView = null;
        for (Actor actor : widgetGroup.getChildren()) {
            if (actor instanceof ListView) {
                actListView = (ListView) actor;
                break;
            }
        }

        if (itemCountChanged) {
            Object object = actListView.getUserObject();
            if (object instanceof SettingCategory) {
                WidgetGroup group = listViews.pop();
                listViewsNames.pop();

                //remove all Listener
                for (Actor actor : group.getChildren())
                    for (EventListener listener : actor.getListeners())
                        actor.removeListener(listener);

                this.removeActor(group);
                showCategory((SettingCategory) object, false);
            }
        } else {
            actListView.reLayout();
        }


    }

    private VisTable getCategoryItem(final SettingCategory category) {
        VisTable table = new VisTable();

        // add label with category name, align left
        table.left();
        VisLabel label = new VisLabel(category.name());
        label.setAlignment(Align.left);
        table.add(label).pad(CB.scaledSizes.MARGIN).expandX().fillX();

        // add next icon
        Image next = new Image(style.nextIcon);
        table.add(next).width(next.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add clicklistener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    showCategory(category, true);
                }
            }
        });
        return table;
    }

    private void showCategory(SettingCategory category, boolean animate) {
        log.debug("Show settings categoriy: " + category.name());


        //get all settings items of this category if the category mode correct

        final Array<SettingBase<?>> categorySettingsList = new Array<SettingBase<?>>();

        boolean expert = Config.SettingsShowAll.getValue() || Config.SettingsShowExpert.getValue();
        boolean developer = Config.SettingsShowAll.getValue();

        for (SettingBase<?> setting : SettingsList.that) {
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
                    case Never:
                        show = developer;
                        break;
                }

                if (show) {
                    categorySettingsList.add(setting);
                    log.debug("    with setting for: " + setting.getName());
                }
            }
        }

        // show new ListView for this category

        ListView newListView = new ListView(categorySettingsList.size) {
            @Override
            public VisTable createView(Integer index) {
                final SettingBase<?> setting = categorySettingsList.get(index);
                return getSettingItem(setting);
            }
        };
        newListView.setUserObject(category);
        showListView(newListView, category.name(), animate);
    }

    private VisTable getSettingItem(SettingBase<?> setting) {
        if (setting instanceof SettingBool) {
            return getBoolView((SettingBool) setting);
        } else if (setting instanceof SettingIntArray) {
            return getIntArrayView((SettingIntArray) setting);
        } else if (setting instanceof SettingStringArray) {
            return getStringArrayView((SettingStringArray) setting);
        } else if (setting instanceof SettingTime) {
            return getTimeView((SettingTime) setting);
        } else if (setting instanceof SettingInt) {
            return getIntView((SettingInt) setting);
        } else if (setting instanceof SettingDouble) {
            return getDblView((SettingDouble) setting);
        } else if (setting instanceof SettingFloat) {
            return getFloatView((SettingFloat) setting);
        } else if (setting instanceof SettingFolder) {
            return getFolderView((SettingFolder) setting);
        } else if (setting instanceof SettingFile) {
            return getFileView((SettingFile) setting);
        } else if (setting instanceof SettingEnum) {
            return getEnumView((SettingEnum<?>) setting);
        } else if (setting instanceof SettingString) {
            return getStringView((SettingString) setting);
//        } else if (setting instanceof SettingsListCategoryButton) {
//            return getButtonView((SettingsListCategoryButton<?>) setting);
//        } else if (setting instanceof SettingsListGetApiButton) {
//            return getApiKeyButtonView((SettingsListGetApiButton<?>) setting);
//        } else if (setting instanceof SettingsListButtonLangSpinner) {
//            return getLangSpinnerView((SettingsListButtonLangSpinner<?>) setting);
//        } else if (setting instanceof SettingsListButtonSkinSpinner) {
//            return getSkinSpinnerView((SettingsListButtonSkinSpinner<?>) setting);
        } else if (setting instanceof SettingsAudio) {
            return getAudioView((SettingsAudio) setting);
        } else if (setting instanceof SettingColor) {
            return getColorView((SettingColor) setting);
        }

        return null;
    }

    private VisTable getColorView(SettingColor setting) {
        return null;
    }

    private VisTable getAudioView(SettingsAudio setting) {
        return null;
    }

    private VisTable getStringView(SettingString setting) {
        return null;
    }

    private VisTable getEnumView(SettingEnum<?> setting) {
        return null;
    }

    private VisTable getFileView(SettingFile setting) {
        return null;
    }

    private VisTable getFolderView(SettingFolder setting) {
        return null;
    }

    private VisTable getFloatView(final SettingFloat setting) {
        final VisLabel valueLabel = new VisLabel(Float.toString(setting.getValue()), valueStyle);
        VisTable table = getNumericItemTable(valueLabel, setting);

        // add clickListener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    new NumericInput_Activity<Float>(setting.getValue()) {
                        public void returnValue(Float value) {
                            setting.setValue(value);
                            valueLabel.setText(value.toString());
                            valueLabel.pack();
                            layoutActListView(false);
                        }
                    }.show();
                }
            }
        });

        return table;
    }

    private VisTable getDblView(final SettingDouble setting) {
        final VisLabel valueLabel = new VisLabel(Double.toString(setting.getValue()), valueStyle);
        VisTable table = getNumericItemTable(valueLabel, setting);

        // add clickListener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    new NumericInput_Activity<Double>(setting.getValue()) {
                        public void returnValue(Double value) {
                            setting.setValue(value);
                            valueLabel.setText(value.toString());
                            valueLabel.pack();
                            layoutActListView(false);
                        }
                    }.show();
                }
            }
        });

        return table;
    }

    private VisTable getIntView(final SettingInt setting) {
        final VisLabel valueLabel = new VisLabel(Integer.toString(setting.getValue()), valueStyle);
        final VisTable table = getNumericItemTable(valueLabel, setting);

        // add clickListener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    new NumericInput_Activity<Integer>(setting.getValue()) {
                        public void returnValue(Integer value) {
                            setting.setValue(value);
                            WidgetGroup group = listViews.peek();
                            for (Actor actor : group.getChildren()) {
                                if (actor instanceof ListView) {
                                   final ListView listView = (ListView) actor;
                                   final float scrollPos = listView.getScrollPos();
                                    listView.rebuildView();
                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            listView.setScrollPos(scrollPos);
                                        }
                                    });

                                }
                            }
                        }
                    }.show();
                }
            }
        });
        return table;
    }

    private VisTable getTimeView(SettingTime setting) {
        return null;
    }

    private VisTable getStringArrayView(SettingStringArray setting) {
        return null;
    }

    private VisTable getIntArrayView(SettingIntArray setting) {
        return null;
    }

    private VisTable getBoolView(final SettingBool setting) {
        VisTable table = new VisTable();

        // add label with category name, align left
        table.left();
        VisLabel label = new VisLabel(Translation.Get(setting.getName()), nameStyle);
        label.setWrap(true);
        label.setAlignment(Align.left);
        table.add(label).pad(CB.scaledSizes.MARGIN).expandX().fillX();

        // add check icon
        final Image[] checkImage = new Image[1];
        if (setting.getValue()) {
            checkImage[0] = new Image(CB.getSprite("check_on"));
        } else {
            checkImage[0] = new Image(CB.getSprite("check_off"));
        }
        table.add(checkImage[0]).width(checkImage[0].getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add clicklistener
        table.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    setting.setValue(!setting.getValue());
                    if (setting.getValue()) {
                        checkImage[0].setDrawable(new SpriteDrawable(CB.getSprite("check_on")));
                    } else {
                        checkImage[0].setDrawable(new SpriteDrawable(CB.getSprite("check_off")));
                    }
                }
            }
        });


        // add description line if description exist
        String description = Translation.Get("Desc_" + setting.getName());
        if (!description.contains("$ID:")) {
            table.row();
            VisLabel desclabel = new VisLabel(description, descStyle);
            desclabel.setWrap(true);
            desclabel.setAlignment(Align.left);
            table.add(desclabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        }

        // add defaultValue line

        table.row();
        VisLabel desclabel = new VisLabel("default: " + String.valueOf(setting.getDefaultValue()), defaultValuStyle);
        desclabel.setWrap(true);
        desclabel.setAlignment(Align.left);
        table.add(desclabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();

        return table;
    }

    private VisTable getNumericItemTable(VisLabel valueLabel, SettingBase<?> setting) {
        VisTable table = new VisTable();

        // add label with category name, align left
        table.left();
        VisLabel label = new VisLabel(Translation.Get(setting.getName()), nameStyle);
        label.setWrap(true);
        label.setAlignment(Align.left);
        table.add(label).pad(CB.scaledSizes.MARGIN).expandX().fillX();

        // add value lable
        table.add(valueLabel).width(valueLabel.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

        // add description line if description exist
        String description = Translation.Get("Desc_" + setting.getName());
        if (!description.contains("$ID:")) {
            table.row();
            VisLabel desclabel = new VisLabel(description, descStyle);
            desclabel.setWrap(true);
            desclabel.setAlignment(Align.left);
            table.add(desclabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();
        }

        // add defaultValue line

        table.row();
        VisLabel desclabel = new VisLabel("default: " + String.valueOf(setting.getDefaultValue()), defaultValuStyle);
        desclabel.setWrap(true);
        desclabel.setAlignment(Align.left);
        table.add(desclabel).colspan(2).pad(CB.scaledSizes.MARGIN).expandX().fillX();

        return table;
    }


    public static class SettingsActivityStyle extends ActivityBaseStyle {
        public Drawable nextIcon, backIcon;
        public BitmapFont nameFont, descFont, defaultValueFont, valueFont;
        public Color nameFontColor, descFontColor, defaultValueFontColor, valueFontColor;

    }


}
