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
package de.longri.cachebox3.gui.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.ButtonDialogStyle;
import de.longri.cachebox3.gui.skin.styles.MessageBoxIconStyle;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_Label;
import de.longri.cachebox3.gui.widgets.Window;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.list_view.ListViewType;
import de.longri.cachebox3.translation.Translation;

/**
 * Created by Longri on 03.08.16.
 */
public class DialogBox extends Window {
    static public final int BUTTON_POSITIVE = 1;
    static public final int BUTTON_NEUTRAL = 2;
    static public final int BUTTON_NEGATIVE = 3;
    private final MessageBoxButton buttonsToUse;
    OnMsgBoxClickListener msgBoxClickListener;
    Catch_Table center;
    Catch_Table footer;
    private boolean extendedHeight;
    private CB_Button btnPositive, btnNeutral, btnNegative;
    private boolean autoHide;
    private Catch_Table title;
    private ButtonDialogStyle style;
    private boolean hasTitle = false;
    private ObjectMap<Actor, Integer> values;

    public DialogBox(String name, CharSequence msg, CharSequence titleText, MessageBoxButton messageBoxButton, MessageBoxIcon icon, OnMsgBoxClickListener listener) {
        this(name, getMsgContentTable(msg, icon), titleText, messageBoxButton, listener, null);
    }

    public DialogBox(String name, Catch_Table content, CharSequence titleText, MessageBoxButton messageBoxButton, OnMsgBoxClickListener listener) {
        this(name, content, titleText, messageBoxButton, listener, null);
    }

    public DialogBox(String name, Catch_Table content, CharSequence titleText, MessageBoxButton messageBoxButton, OnMsgBoxClickListener listener, ButtonDialogStyle useStyle) {
        super(name);
        Skin skin = VisUI.getSkin();
        style = useStyle == null ? skin.get(ButtonDialogStyle.class) : useStyle;
        setStageBackground(style.stageBackground);
        setTableAndCellDefaults();

        if (titleText != null) {
            hasTitle = true;
            title = new Catch_Table(true);
            title.setSkin(skin);
            if (style.header != null) {
                CB_Label titleLabel = new CB_Label(titleText, new Label.LabelStyle(style.titleFont, style.titleFontColor));
                title.setBackground(style.header);
                title.addLast(titleLabel);
                addLast(title);
            }
        }

        center = content;
        extendedHeight = false;
        for (Actor act : center.getChildren()) {
            if (act instanceof ListView) {
                extendedHeight = true;
                break;
            }
        }
        center.setBackground(style.center);
        addLast(center);

        footer = new Catch_Table(true);
        footer.setSkin(skin);
        values = new ObjectMap();
        footer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor)) return;
                /*
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                // this actor is (possibly) not in the values
                // and a call result with null crashes the app
                 */
                result(values.get(actor));
            }
        });
        footer.setBackground(style.footer);
        addLast(footer);

        layout();
        buttonsToUse = messageBoxButton;
        setButtonCaptions();
        msgBoxClickListener = listener;
        autoHide = true;
    }

    static Catch_Table getMsgContentTable(CharSequence msg, MessageBoxIcon icon) {
        return getMsgContentTable(msg, icon, null);
    }

    public static Catch_Table getMsgContentTable(CharSequence msg, MessageBoxIcon icon, ButtonDialogStyle style) {
        Skin skin = VisUI.getSkin();

        if (style == null) style = skin.get(ButtonDialogStyle.class);
        Catch_Table contentTable = new Catch_Table();
        contentTable.setSkin(skin);
        if (icon != MessageBoxIcon.None && icon != null) {
            Image iconImage = new Image(getIcon(icon));
            contentTable.add(iconImage).width(iconImage.getWidth()).top().pad(CB.scaledSizes.MARGIN);
        }
        contentTable.defaults().space(2).padLeft(3).padRight(3);

        final CB_Label msgLabel = new CB_Label(msg, new Label.LabelStyle(style.titleFont, style.titleFontColor));
        msgLabel.setWrap(true);

        msgLabel.setWidth(Gdx.graphics.getWidth() * 0.7f);
        msgLabel.pack();
        if (msgLabel.getHeight() > Gdx.graphics.getHeight() * 0.6f) {
            ListView labelListView = new ListView(ListViewType.VERTICAL);
            labelListView.setAdapter(new ListViewAdapter() {
                @Override
                public int getCount() {
                    return 1;
                }

                @Override
                public ListViewItem getView(int index) {
                    ListViewItem item = new ListViewItem(index) {
                        @Override
                        public void layout() {
                            msgLabel.setBounds(0, 0, getWidth(), getHeight());
                        }

                        @Override
                        public void sizeChanged() {
                            msgLabel.setDebug(true);
                            msgLabel.setBounds(0, 0, getWidth(), getHeight());
                        }
                    };
                    item.add(msgLabel);
                    return item;
                }

                @Override
                public void update(ListViewItem view) {

                }
            });
            contentTable.setDebug(true);
            contentTable.add(labelListView).expand().fill();
        } else {
            contentTable.add(msgLabel).expand().fill();
        }

        contentTable.pack();
        contentTable.layout();
        return contentTable;
    }

    private static Drawable getIcon(MessageBoxIcon msgIcon) {
        if (msgIcon == null)
            return null;

        MessageBoxIconStyle style = VisUI.getSkin().get(MessageBoxIconStyle.class);

        switch (msgIcon) {
            case Asterisk:
                return style.Asterisk;
            case Error:
                return style.Error;
            case Exclamation:
                return style.Exclamation;
            case Hand:
                return style.Hand;
            case Information:
                return style.Information;
            //case None:
            //return null;
            case Question:
                return style.Question;
            case Stop:
                return style.Stop;
            case Warning:
                return style.Warning;
            case ExpiredApiKey:
                return style.ExpiredApiKey;
            case Database:
                return style.Database;
            default:
                return null;
        }
    }

    public Table getContentTable() {
        return center;
    }

    @Override
    public void pack() {
        super.pack();
        setPosition(((Gdx.graphics.getWidth() - getWidth()) / 2f), ((Gdx.graphics.getHeight() - getHeight()) / 2));
    }

    private void setButtonCaptions() {
        if (buttonsToUse == MessageBoxButton.YesNoRetry) {
            addNewButton(Translation.get("yes"), BUTTON_POSITIVE);
            addNewButton(Translation.get("no"), BUTTON_NEGATIVE);
            addNewButton(Translation.get("retry"), BUTTON_NEUTRAL);
        } else if (buttonsToUse == MessageBoxButton.AbortRetryIgnore) {
            addNewButton(Translation.get("abort"), BUTTON_POSITIVE);
            addNewButton(Translation.get("retry"), BUTTON_NEUTRAL);
            addNewButton(Translation.get("ignore"), BUTTON_NEGATIVE);
        } else if (buttonsToUse == MessageBoxButton.OK) {
            addNewButton(Translation.get("ok"), BUTTON_POSITIVE);
        } else if (buttonsToUse == MessageBoxButton.OKCancel) {
            addNewButton(Translation.get("ok"), BUTTON_POSITIVE);
            addNewButton(Translation.get("cancel"), BUTTON_NEGATIVE);
        } else if (buttonsToUse == MessageBoxButton.RetryCancel) {
            addNewButton(Translation.get("retry"), BUTTON_POSITIVE);
            addNewButton(Translation.get("cancel"), BUTTON_NEGATIVE);
        } else if (buttonsToUse == MessageBoxButton.YesNo) {
            addNewButton(Translation.get("yes"), BUTTON_POSITIVE);
            addNewButton(Translation.get("no"), BUTTON_NEGATIVE);
        } else if (buttonsToUse == MessageBoxButton.YesNoCancel) {
            addNewButton(Translation.get("yes"), BUTTON_POSITIVE);
            addNewButton(Translation.get("no"), BUTTON_NEGATIVE);
            addNewButton(Translation.get("cancel"), BUTTON_NEUTRAL);
        } else if (buttonsToUse == MessageBoxButton.Cancel) {
            addNewButton(Translation.get("cancel"), BUTTON_NEGATIVE);
        }
        footer.finishRow();
    }

    private void addNewButton(CharSequence text, int buttonId) {
        CB_Button button = new CB_Button(text);
        if (buttonId == BUTTON_NEGATIVE) btnNegative = button;
        else if (buttonId == BUTTON_NEUTRAL) btnNeutral = button;
        else btnPositive = button;
        footer.addNext(button);
        values.put(button, buttonId);
    }

    void setButtonText(CharSequence text, int buttonId) {
        CB_Button button;
        if (buttonId == BUTTON_NEGATIVE) button = btnNegative;
        else if (buttonId == BUTTON_NEUTRAL) button = btnNeutral;
        else button = btnPositive;
        button.setText(text);
    }

    public void setButtonText(String left, String middle, String right) {
        if (left != null)
            btnPositive.setText(Translation.get(left));
        if (middle != null)
            btnNeutral.setText(Translation.get(middle));
        if (right != null)
            btnNegative.setText(Translation.get(right));
    }

    void setButtonClickedListener(OnMsgBoxClickListener listener) {
        msgBoxClickListener = listener;
    }

    protected void result(Object object) {
        if (msgBoxClickListener != null) {
            msgBoxClickListener.onClick((Integer) object, null);
        }
        if (autoHide) hide();
    }

    void setNoHide() {
        autoHide = false;
    }
}