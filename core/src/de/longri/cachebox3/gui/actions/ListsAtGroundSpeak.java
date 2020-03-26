package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.translation.Translation;

import java.util.Map;

import static de.longri.cachebox3.apis.GroundspeakAPI.OK;
import static de.longri.cachebox3.gui.dialogs.ButtonDialog.BUTTON_NEUTRAL;
import static de.longri.cachebox3.gui.dialogs.ButtonDialog.BUTTON_POSITIVE;

public class ListsAtGroundSpeak extends AbstractAction {
    private static ListsAtGroundSpeak listsAtGroundSpeak;

    private ListsAtGroundSpeak() {
        super("GroundSpeakLists");
    }

    public static ListsAtGroundSpeak getInstance() {
        if (listsAtGroundSpeak == null) listsAtGroundSpeak = new ListsAtGroundSpeak();
        return listsAtGroundSpeak;
    }

    @Override
    public void execute() {
        Menu menu = new Menu("GroundSpeakLists");
        menu.addMenuItem("Watchlist", null, () -> groundspeakList("Watchlist"));
        menu.addMenuItem("Favoriteslist", null, () -> groundspeakList("Favoriteslist"));
        menu.addMenuItem("Ignorelist", null, () -> groundspeakList("Ignorelist"));
        menu.addMenuItem("Bookmarklists", null, this::getBookmarkLists);
        menu.show();

    }

    private void getBookmarkLists() {
        Menu menu = new Menu("Bookmarklists");
        for (Map.Entry<String, String> bookmarkList : GroundspeakAPI.getInstance().fetchBookmarkLists().entrySet()) {
            menu.addMenuItem("", bookmarkList.getKey(), null, () -> groundspeakList(bookmarkList));
        }
        menu.show();
    }

    @Override
    public Drawable getIcon() {
        return null;
    }

    private void groundspeakList(Map.Entry<String, String> bookmarkList) {
        ButtonDialog mb = new ButtonDialog("", Translation.get("BookmarklistMessage", bookmarkList.getKey()), bookmarkList.getKey(), MessageBoxButton.AbortRetryIgnore, MessageBoxIcon.Question,
                (btnNumber, data) -> {
                    if (btnNumber == BUTTON_POSITIVE)
                        addToList(bookmarkList.getValue());
                    else if (btnNumber == BUTTON_NEUTRAL)
                        removeFromList(bookmarkList.getValue());
                    return true;
                });
        mb.setButtonText("append", "remove", "cancel");
        mb.show();
    }

    private void groundspeakList(String title) {
        ButtonDialog mb = new ButtonDialog("", Translation.get(title + "Message"), Translation.get(title), MessageBoxButton.AbortRetryIgnore, MessageBoxIcon.Question,
                (btnNumber, data) -> {
                    if (btnNumber == BUTTON_POSITIVE)
                        addToList(title);
                    else if (btnNumber == BUTTON_NEUTRAL)
                        removeFromList(title);
                    return true;
                });
        mb.setButtonText("append", "remove", "cancel");
        mb.show();
    }

    private void addToList(String title) {
        if (EventHandler.isSetSelectedCache()) {
            String listCode, AddToTitle;
            AddToTitle = "AddTo" + title;
            switch (title) {
                case "Watchlist":
                    listCode = "watch";
                    break;
                case "Favoriteslist":
                    listCode = "favorites";
                    break;
                case "Ignorelist":
                    listCode = "ignore";
                    break;
                default:
                    listCode = title;
                    AddToTitle = "AddToBookmarklist";
            }
            if (GroundspeakAPI.getInstance().addToList(listCode, EventHandler.getSelectedCache().getGeoCacheCode().toString()) == OK) {
                MessageBox.show(Translation.get("ok"), Translation.get(AddToTitle), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            } else {
                MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get(AddToTitle), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            }
        }
    }

    private void removeFromList(String title) {
        if (EventHandler.isSetSelectedCache()) {
            String listCode, RemoveFromTitle;
            RemoveFromTitle = "AddTo" + title;
            switch (title) {
                case "Watchlist":
                    listCode = "watch";
                    break;
                case "Favoriteslist":
                    listCode = "favorites";
                    break;
                case "Ignorelist":
                    listCode = "ignore";
                    break;
                default:
                    listCode = title;
                    RemoveFromTitle = "RemoveFromBookmarklist";
            }
            if (GroundspeakAPI.getInstance().removeFromList(listCode, EventHandler.getSelectedCache().getGeoCacheCode().toString()) == OK) {
                MessageBox.show(Translation.get("ok"), Translation.get(RemoveFromTitle), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            } else {
                MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get(RemoveFromTitle), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            }
        }
    }
}
