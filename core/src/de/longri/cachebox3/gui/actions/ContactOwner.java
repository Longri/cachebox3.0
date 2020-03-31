package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.http.Webb;

import java.net.URLEncoder;

/**
 * Contacts the owner of a geocache by using the messagecenter or mail at geocaching.com
 *
 * todo create icon and implement set Clipboard class for Desktop
 *
 */
public class ContactOwner extends AbstractAction {

    private static ContactOwner contactOwner;

    private ContactOwner() {
        super("contactOwner");
    }

    public static ContactOwner getInstance() {
        if (contactOwner == null) contactOwner = new ContactOwner();
        return contactOwner;
    }

    @Override
    public void execute() {
        Menu menu = new Menu("contactOwner");
        menu.addMenuItem("MailToOwner", CB.getSkin().getMenuIcon.e_mailToOwner, () -> {
            try {
                String mOwner = URLEncoder.encode(EventHandler.getSelectedCache().getOwner().toString(), "UTF-8");
                PlatformConnector.callUrl("https://www.geocaching.com/email/?u=" + mOwner);
            } catch (Exception ignored) {
            }
        });
        menu.addMenuItem("MessageToOwner", CB.getSkin().getMenuIcon.messageToOwner, () -> CB.postAsync(new NamedRunnable("MessageToOwner") {
            @Override
            public void run() {
                try {
                    AbstractCache geoCache = EventHandler.getSelectedCache();
                    String mGCCode = geoCache.getGeoCacheCode().toString();
                    // fill clipboard
                    if (PlatformConnector.getClipboard() != null) {
                        String text = mGCCode + " - " + geoCache.getGeoCacheName() + ("\n" + "https://coord.info/" + mGCCode);
                        if (geoCache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                            text = text + ("\n\n" + "Location (corrected)");
                            if (geoCache.hasCorrectedCoordinates()) {
                                text = text + ("\n" + geoCache.formatCoordinate());
                            } else {
                                text = text + ("\n" + geoCache.getCorrectedFinal().formatCoordinate());
                            }
                        } else {
                            text = text + ("\n\n" + "Location");
                            text = text + ("\n" + geoCache.formatCoordinate());
                        }
                        PlatformConnector.getClipboard().setContents(text);
                    }
                    try {
                        String page = Webb.create()
                                .get("https://coord.info/" + mGCCode)
                                .ensureSuccess()
                                .asString()
                                .getBody();
                        String toSearch = "recipientId=";
                        int pos = page.indexOf(toSearch);
                        if (pos > -1) {
                            int start = pos + toSearch.length();
                            int stop = page.indexOf("&amp;", start);
                            String guid = page.substring(start, stop);
                            PlatformConnector.callUrl("https://www.geocaching.com/account/messagecenter?recipientId=" + guid + "&gcCode=" + mGCCode);
                        } else {
                            MessageBox.show(Translation.get("noRecipient"), Translation.get("Error"), MessageBoxButton.OK, MessageBoxIcon.Error, null);
                        }
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
            }
        }));
        menu.show();
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.contactOwner;
    }
}
