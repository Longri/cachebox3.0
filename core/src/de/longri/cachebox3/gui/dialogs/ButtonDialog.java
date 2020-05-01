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
import com.badlogic.gdx.graphics.g2d.Batch;
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
public class ButtonDialog extends Window {

    // see for layout help ==> https://github.com/libgdx/libgdx/wiki/Table

    static public final int BUTTON_POSITIVE = 1;
    static public final int BUTTON_NEUTRAL = 2;
    static public final int BUTTON_NEGATIVE = 3;
    private final MessageBoxButton buttons;
    OnMsgBoxClickListener msgBoxClickListener;
    Catch_Table contentBox;
    Catch_Table buttonTable;
    private boolean extendedHeight;
    private CB_Button btnPositive, btnNeutral, btnNegative;
    private boolean autoHide;
    private Catch_Table titleTable;
    private ButtonDialogStyle style;
    private boolean hasTitle = false;
    private ObjectMap<Actor, Integer> values;

    public ButtonDialog(String name, CharSequence msg, CharSequence title, MessageBoxButton buttons, MessageBoxIcon icon, OnMsgBoxClickListener listener) {
        this(name, getMsgContentTable(msg, icon), title, buttons, listener, VisUI.getSkin().get(ButtonDialogStyle.class));
    }

    public ButtonDialog(String name, Catch_Table contentTable, CharSequence title, MessageBoxButton buttons, OnMsgBoxClickListener listener) {
        this(name, contentTable, title, buttons, listener, VisUI.getSkin().get(ButtonDialogStyle.class));
    }

    public ButtonDialog(String name, Catch_Table content, CharSequence title, MessageBoxButton buttonsToUse, OnMsgBoxClickListener listener, ButtonDialogStyle useStyle) {
        super(name);
        contentBox = content;

        extendedHeight = false;
        for (Actor act : contentBox.getChildren()) {
            if (act instanceof ListView) {
                extendedHeight = true;
                break;
            }
        }

        Skin skin = VisUI.getSkin();
        style = useStyle == null ? skin.get(ButtonDialogStyle.class) : useStyle;
        setStageBackground(style.stageBackground);
        if (title != null) {
            hasTitle = true;
            titleTable = new Catch_Table();
            titleTable.setSkin(skin);
            if (style.title != null) {
                titleTable.defaults()
                        .padLeft(style.title.getLeftWidth() * 2)
                        .padRight(style.title.getRightWidth())
                        .padTop(style.title.getTopHeight())
                        .padBottom(style.title.getBottomHeight());
            }

            CB_Label titleLabel = new CB_Label(title, new Label.LabelStyle(style.titleFont, style.titleFontColor));
            titleTable.add(titleLabel).left();
            add(titleTable).left();
            row();
        }

        add(contentBox).expand().fill().padLeft(CB.scaledSizes.MARGIN).padRight(CB.scaledSizes.MARGIN);
        row();

        buttonTable = new Catch_Table(true);
        buttonTable.setSkin(skin);
        add(buttonTable);
        /*
                .padLeft(CB.scaledSizes.MARGIN)
                .padRight(CB.scaledSizes.MARGIN)
                .padBottom(CB.scaledSizes.MARGIN / 2)
                .padTop(CB.scaledSizes.MARGIN);
         */

        if (style.footer != null) {
            buttonTable.defaults().padLeft(style.footer.getLeftWidth()).padRight(style.footer.getRightWidth()).padBottom(CB.scaledSizes.MARGIN);
        }

        values = new ObjectMap();
        buttonTable.addListener(new ChangeListener() {
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

        layout();
        buttons = buttonsToUse;
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
        return contentBox;
    }

    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float offset = 0;
        if (hasTitle) {
            offset = style.title.getBottomHeight();
        }

        boolean renderDialogBackground = true; // for test
        if (style.header != null && renderDialogBackground) {
            style.header.draw(batch,
                    getX(),
                    getY() + (contentBox.getY() + contentBox.getHeight()) - offset,
                    getWidth(),
                    style.header.getMinHeight() + offset);
        }
        if (style.footer != null && renderDialogBackground) {
            style.footer.draw(batch, getX(), getY(), getWidth(), buttonTable.getHeight() + (2 * CB.scaledSizes.MARGIN));
        }
        if (style.center != null && renderDialogBackground) {
            style.center.draw(batch, getX(), contentBox.getY() + getY(), getWidth(),
                    contentBox.getHeight());
        }

        if (hasTitle) {
            // TODO handle drawing if Title width too long for window
            if (style.title != null && renderDialogBackground) {
                style.title.draw(batch, getX(), getY() + titleTable.getY(), titleTable.getWidth() + style.title.getRightWidth(), titleTable.getHeight());
            }
        }
        super.drawChildren(batch, parentAlpha);
    }

    @Override
    public void pack() {
        super.pack();
        setPosition(((Gdx.graphics.getWidth() - getWidth()) / 2f),
                ((Gdx.graphics.getHeight() - getHeight()) / 2));
    }

    private void setButtonCaptions() {
        float buttonWidth;

        float prfWidth = getPrefWidth();

        float maxWindowWidth = prfWidth - (8 * CB.scaledSizes.MARGIN);

        if (buttons == MessageBoxButton.YesNoRetry) {
            buttonWidth = (maxWindowWidth / 3) - (4 * CB.scaledSizes.MARGIN);
            addNewButton(Translation.get("yes"), buttonWidth, BUTTON_POSITIVE);
            addNewButton(Translation.get("no"), buttonWidth, BUTTON_NEGATIVE);
            addNewButton(Translation.get("retry"), buttonWidth, BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButton.AbortRetryIgnore) {
            buttonWidth = (maxWindowWidth / 3) - (4 * CB.scaledSizes.MARGIN);
            addNewButton(Translation.get("abort"), buttonWidth, BUTTON_POSITIVE);
            addNewButton(Translation.get("retry"), buttonWidth, BUTTON_NEUTRAL);
            addNewButton(Translation.get("ignore"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButton.OK) {
            buttonWidth = CB.scaledSizes.BUTTON_WIDTH_WIDE;
            addNewButton(Translation.get("ok"), buttonWidth, BUTTON_POSITIVE);
        } else if (buttons == MessageBoxButton.OKCancel) {
            buttonWidth = (maxWindowWidth / 2) - (3 * CB.scaledSizes.MARGIN);
            addNewButton(Translation.get("ok"), buttonWidth, BUTTON_POSITIVE);
            addNewButton(Translation.get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButton.RetryCancel) {
            buttonWidth = (maxWindowWidth / 2) - (3 * CB.scaledSizes.MARGIN);
            addNewButton(Translation.get("retry"), buttonWidth, BUTTON_POSITIVE);
            addNewButton(Translation.get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButton.YesNo) {
            buttonWidth = (maxWindowWidth / 2) - (3 * CB.scaledSizes.MARGIN);
            addNewButton(Translation.get("yes"), buttonWidth, BUTTON_POSITIVE);
            addNewButton(Translation.get("no"), buttonWidth, BUTTON_NEGATIVE);
        } else if (buttons == MessageBoxButton.YesNoCancel) {
            buttonWidth = (maxWindowWidth / 3) - (4 * CB.scaledSizes.MARGIN);
            addNewButton(Translation.get("yes"), buttonWidth, BUTTON_POSITIVE);
            addNewButton(Translation.get("no"), buttonWidth, BUTTON_NEGATIVE);
            addNewButton(Translation.get("cancel"), buttonWidth, BUTTON_NEUTRAL);
        } else if (buttons == MessageBoxButton.Cancel) {
            buttonWidth = CB.scaledSizes.BUTTON_WIDTH_WIDE;
            addNewButton(Translation.get("cancel"), buttonWidth, BUTTON_NEGATIVE);
        }
        buttonTable.finishRow();
    }

    private void addNewButton(CharSequence text, float buttonWidth, int object) {
        CB_Button button = new CB_Button(text);
        if (object == BUTTON_NEGATIVE) btnNegative = button;
        else if (object == BUTTON_NEUTRAL) btnNeutral = button;
        else btnPositive = button;
        buttonTable.addNext(button); //.width(buttonWidth);
        values.put(button, object);
    }

    void setButtonText(CharSequence text, int object) {
        CB_Button button;
        if (object == BUTTON_NEGATIVE) button = btnNegative;
        else if (object == BUTTON_NEUTRAL) button = btnNeutral;
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

    @Override
    public float getPrefWidth() {
        return Gdx.graphics.getWidth() * 0.96f;
    }

    @Override
    public float getPrefHeight() {
        if (extendedHeight) return Gdx.graphics.getHeight() * 0.9f;
        return Gdx.graphics.getWidth() * 0.8f;
    }
}