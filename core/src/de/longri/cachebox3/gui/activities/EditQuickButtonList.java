package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.menu.QuickAction;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.list_view.ListViewType;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditQuickButtonList extends Activity {
    final static Logger log = LoggerFactory.getLogger(EditQuickButtonList.class);
    ListView editQuickButtonListMenu;
    ListViewAdapter editQuickButtonListMenuAdapter;
    Array<MenuItem> editQuickButtonListItems;
    CB_Button up, down, add, remove;
    private ChangeListener onChange;

    private EditQuickButtonList(String title, Drawable icon) {
        super(title, icon);
        // "editQuickButtonListTitle"
        editQuickButtonListMenu = new ListView(ListViewType.VERTICAL);
        editQuickButtonListItems = new Array<>();
        up = new CB_Button("<"); // , VisUI.getSkin().getDrawable("arrow_help")
        up.addListener(
                new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        if (event.getType() == InputEvent.Type.touchUp) {
                            MenuItem menuItem = (MenuItem) editQuickButtonListMenu.getSelectedItem();
                            int i = menuItem.getListIndex();
                            if (i > 0) {
                                editQuickButtonListItems.swap(i, i - 1);
                                editQuickButtonListItems.get(i).setNewIndex(i);
                                menuItem.setNewIndex(i - 1);
                                editQuickButtonListMenu.setAdapter(editQuickButtonListMenuAdapter);
                            }
                        }
                        event.stop();
                    }
                }
        );
        down = new CB_Button(">");
        down.addListener(
                new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        if (event.getType() == InputEvent.Type.touchUp) {
                            MenuItem menuItem = (MenuItem) editQuickButtonListMenu.getSelectedItem();
                            int i = menuItem.getListIndex();
                            if (i < editQuickButtonListItems.size - 1) {
                                editQuickButtonListItems.swap(i, i + 1);
                                editQuickButtonListItems.get(i).setNewIndex(i);
                                menuItem.setNewIndex(i + 1);
                                editQuickButtonListMenu.setAdapter(editQuickButtonListMenuAdapter);
                            }
                        }
                        event.stop();
                    }
                }
        );
        add = new CB_Button("+");
        add.addListener(
                new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        if (event.getType() == InputEvent.Type.touchUp) {
                            Menu addUnusedQuickButtonMenu = new Menu("");
                            String currentQuickButtonList = getCurrentlyDefinedQuickButtonList();
                            for (QuickAction quickAction : QuickAction.values()) {
                                if (!currentQuickButtonList.contains(quickAction.ordinal() + ",")) {
                                    AbstractAction action = quickAction.getAction();
                                    if (action != null) {
                                        addUnusedQuickButtonMenu.addMenuItem(
                                                action.getTitleTranslationId(),
                                                "",
                                                action.getIcon(),
                                                new ClickListener() {
                                                    public void clicked(InputEvent event, float x, float y) {
                                                        if (addUnusedQuickButtonMenu.mustHandle(event)) {
                                                            MenuItem menuItem = (MenuItem) event.getListenerActor();
                                                            QuickAction newQuickAction = (QuickAction) menuItem.getUserObject();
                                                            AbstractAction newAction = newQuickAction.getAction();
                                                            MenuItem newMenuItem = getMenuItem(newAction.getTitleTranslationId(), newAction.getIcon());
                                                            newMenuItem.setUserObject(newQuickAction.ordinal());
                                                            int index = editQuickButtonListMenu.getSelectedItem().getListIndex() + 1;
                                                            final int fi = index;
                                                            editQuickButtonListItems.insert(index, newMenuItem);
                                                            for (int i = index; i < editQuickButtonListItems.size; i++) {
                                                                editQuickButtonListItems.get(i).setNewIndex(i);
                                                            }
                                                            editQuickButtonListMenu.setAdapter(editQuickButtonListMenuAdapter);
                                                            CB.postOnNextGlThread(() -> editQuickButtonListMenu.setSelection(fi));
                                                        }
                                                    }
                                                }
                                        ).setUserObject(quickAction);
                                    }
                                }
                            }
                            addUnusedQuickButtonMenu.show();
                            event.stop();
                        }
                    }
                }
        );
        remove = new CB_Button("-"); // ,VisUI.getSkin().getDrawable("mcxDelete")
        remove.addListener(
                new ClickListener() {
                    public void clicked(InputEvent event, float x, float y) {
                        if (event.getType() == InputEvent.Type.touchUp) {
                            MenuItem menuItem = (MenuItem) editQuickButtonListMenu.getSelectedItem();
                            int index = menuItem.getListIndex();
                            editQuickButtonListItems.removeIndex(index);
                            for (int i = index; i < editQuickButtonListItems.size; i++) {
                                editQuickButtonListItems.get(i).setNewIndex(i);
                            }
                            editQuickButtonListMenu.setAdapter(editQuickButtonListMenuAdapter);
                            if (index < editQuickButtonListItems.size - 1) {
                                final int fi = index;
                                CB.postOnNextGlThread(() -> editQuickButtonListMenu.setSelection(fi));
                            } else {
                                index--;
                                if (index > 0) {
                                    final int fi = index;
                                    CB.postOnNextGlThread(() -> editQuickButtonListMenu.setSelection(fi));
                                }
                            }
                        }
                        event.stop();
                    }
                }
        );
        editQuickButtonListMenuAdapter = new ListViewAdapter() {
            @Override
            public int getCount() {
                return editQuickButtonListItems.size;
            }

            @Override
            public ListViewItem getView(int index) {
                return editQuickButtonListItems.get(index);
            }

            @Override
            public void update(ListViewItem view) {

            }
        };
    }

    public static EditQuickButtonList getInstance(String title, Drawable icon) {
        if (activity == null) {
            activity = new EditQuickButtonList(title, icon);
            activity.top().left();
        }
        return (EditQuickButtonList) activity;
    }

    @Override
    protected void createMainContent() {
        for (int i = 0; i < editQuickButtonListItems.size; i++) {
            editQuickButtonListItems.get(i).setNewIndex(i);
        }
        editQuickButtonListMenu.setPreferredHeight(getHeight() - 3 * up.getPrefHeight() - 2 * CB.scaledSizes.MARGIN);
        editQuickButtonListMenu.setAdapter(editQuickButtonListMenuAdapter);

        CB.postOnNextGlThread(() -> editQuickButtonListMenu.setSelection(0));
        mainContent.addNext(up);
        mainContent.addNext(down);
        mainContent.addNext(add);
        mainContent.addLast(remove);
        mainContent.addLast(editQuickButtonListMenu);
        mainContent.padBottom(CB.scaledSizes.MARGIN).padTop(CB.scaledSizes.MARGIN);
    }

    private String getCurrentlyDefinedQuickButtonList() {
        String newValue = "";
        for (int i = 0; i < editQuickButtonListItems.size; i++) {
            newValue = newValue + editQuickButtonListItems.get(i).getUserObject() + ",";
        }
        return newValue;
    }

    @Override
    protected void runAtOk(InputEvent event, float x, float y) {
        Config.quickButtonList.setValue(getCurrentlyDefinedQuickButtonList());
        if (onChange != null) onChange.changed(new ChangeListener.ChangeEvent(), this);
    }

    public void execute(ChangeListener changeListener) {
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
                        if (action != null) {
                            MenuItem menuItem = getMenuItem(action.getTitleTranslationId(), action.getIcon());
                            menuItem.setUserObject(ordinal);
                            editQuickButtonListItems.add(menuItem);
                        }
                    }
                } catch (Exception e) {
                    log.error("getListFromConfig", e);
                }
            }
        }
        onChange = changeListener;
        show();
    }

    private MenuItem getMenuItem(String titleTranslationId, Drawable icon) {
        MenuItem item = new MenuItem(0, 738, titleTranslationId, null);
        item.setTitle(Translation.get(titleTranslationId).toString());
        if (icon != null)
            item.setIcon(icon);
        addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (event.getType() == InputEvent.Type.touchUp) {
                    if (event.getListenerActor() instanceof MenuItem) {
                        MenuItem menuItem = (MenuItem) event.getListenerActor();
                        menuItem.setSelected(true);
                        down.setDisabled(menuItem.getListIndex() == 0);
                        up.setDisabled(menuItem.getListIndex() == editQuickButtonListItems.size - 1);
                        event.stop();
                    }
                }
            }
        });
        return item;
    }

}
