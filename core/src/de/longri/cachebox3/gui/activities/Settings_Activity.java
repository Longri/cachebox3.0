package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.SettingCategory;
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
                                resortList();
                                return true;

                            case MenuID.MI_SHOW_ALL:
                                Config.SettingsShowAll.setValue(!Config.SettingsShowAll.getValue());
                                Config.SettingsShowExpert.setValue(false);
                                resortList();
                                return true;
                            case MenuID.MI_SHOW_Normal:
                                Config.SettingsShowAll.setValue(false);
                                Config.SettingsShowExpert.setValue(false);
                                resortList();
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

    private void resortList() {

    }

    private Array<ListView> listViews = new Array<ListView>();
    private int listLevel = 0;

    private void fillContent() {


        final Array<SettingCategory> settingCategories = new Array<SettingCategory>();
        SettingCategory[] tmp = SettingCategory.values();
        for (SettingCategory item : tmp) {
            if (item != SettingCategory.Button) {
                settingCategories.add(item);
            }
        }
        ListView categorieListView = new ListView(settingCategories.size) {
            @Override
            public VisTable createView(Integer index) {
                VisTable table = new VisTable();

                final SettingCategory category = settingCategories.get(index);

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
                            showCategory(category);
                        }
                    }
                });


                return table;
            }
        };

        listViews.add(categorieListView);

        float y = btnOk.getY() + btnOk.getHeight() + CB.scaledSizes.MARGIN;
        categorieListView.setBounds(CB.scaledSizes.MARGIN, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGIN));
        categorieListView.setBackground(null); // remove default background
        categorieListView.layout();
        this.addActor(categorieListView);
    }

    private void showCategory(SettingCategory category) {
        log.debug("Show settings categoriy: " + category.name());
    }


    public static class SettingsActivityStyle extends ActivityBaseStyle {
        public Drawable nextIcon;
    }


}
