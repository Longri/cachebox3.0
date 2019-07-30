package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.widget.VisTextField;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.http.Webb;
import org.json.JSONArray;
import org.json.JSONObject;

import static de.longri.cachebox3.CB.addClickHandler;

public class SearchCoordinates extends ActivityBase {

    private CB_Button btnOK;
    private CB_Button btnCancel;
    private ScrollPane scrollBox;
    private Catch_Table box;
    private CB_Label lblCity, lblStreet;
    private VisTextField edtCity, edtStreet;
    private boolean needLayout = true;

    public SearchCoordinates() {
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        box = new Catch_Table(true);
        scrollBox = new ScrollPane(box);

        lblCity = new CB_Label(Translation.get("city"));
        edtCity = new VisTextField("edtCity");
        lblStreet = new CB_Label(Translation.get("street"));
        edtStreet = new VisTextField("edtStreet");
        setTableAndCellDefaults();
        center();
        initClickHandlersAndContent();
    }

    @Override
    public void layout() {
        if (!needLayout) {
            super.layout();
            return;
        }
        SnapshotArray<Actor> actors = getChildren();
        for (Actor actor : actors)
            removeActor(actor);
        setFillParent(true);
        addLast(scrollBox);
        addNext(btnOK);
        addLast(btnCancel);
        box.addNext(lblCity, -0.4f);
        box.addLast(edtCity);
        box.addNext(lblStreet, -0.4f);
        box.addLast(edtStreet);
        super.layout();
        needLayout = false;
    }

    public void callBack(Coordinate coordinate) {
    }

    private JSONArray fetchLocations() {
        return Webb.create()
                .get("https://nominatim.openstreetmap.org/search")
                .readTimeout(Config.socket_timeout.getValue())
                .param("city", edtCity.getText())
                .param("street", edtStreet.getText())
                .param("format", "json")
                .ensureSuccess()
                .asJsonArray()
                .getBody();
    }

    private void initClickHandlersAndContent() {

        addClickHandler(btnOK,() -> {
            btnOK.setDisabled(true);
            JSONArray fetchedLocations = fetchLocations();
            Menu menuLocation;
            if (fetchedLocations.length() > 0) {
                menuLocation = new Menu("LocationMenuTitle");
                menuLocation.addOnHideListener(() -> btnOK.setDisabled(false));
                for (int ii = 0; ii < fetchedLocations.length(); ii++) {
                    JSONObject jPoi = (JSONObject) fetchedLocations.get(ii);
                    String description = jPoi.optString("display_name", "");
                    description = description.replace(",", "\n");
                    Coordinate pos = new Coordinate(jPoi.optDouble("lat", 0), jPoi.optDouble("lon", 0));
                    menuLocation.addMenuItem("", description, null, () -> {
                        callBack(pos);
                        finish();
                    });
                }
                menuLocation.show();
            }
            else {
                btnOK.setDisabled(false);
            }
        });

        addClickHandler(btnCancel, () -> {
            finish();
        });

        edtCity.setText("");
        edtStreet.setText("");

    }

}