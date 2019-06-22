/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets.filter_settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.activities.EditFilterSettings;
import de.longri.cachebox3.gui.skin.styles.AttributesStyle;
import de.longri.cachebox3.gui.skin.styles.CacheSizeStyle;
import de.longri.cachebox3.gui.skin.styles.FilterStyle;
import de.longri.cachebox3.gui.skin.styles.StarsStyle;
import de.longri.cachebox3.gui.widgets.AdjustableStarWidget;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.gui.widgets.AdjustableStarWidget.Type.SIZE;
import static de.longri.cachebox3.gui.widgets.AdjustableStarWidget.Type.STAR;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.NONE;

/**
 * Created by Longri on 16.11.2017.
 */
public class FilterSetListView extends Catch_Table implements EditFilterSettings.OnShow {

    private final Logger log = LoggerFactory.getLogger(FilterSetListView.class);

    private final ListView setListView;
    private final FilterStyle style;
    private final EditFilterSettings filterSettings;
    private final Array<ListViewItem> listViewItems = new Array<>();

    public FilterSetListView(EditFilterSettings editFilterSettings, FilterStyle style) {
        this.style = style;
        this.filterSettings = editFilterSettings;

        setListView = new ListView(VERTICAL);
        setListView.setSelectable(NONE);
        this.add(setListView).expand().fill();
        setListView.setEmptyString("EmptyList");
    }

    private void fillList() {
        if (listViewItems.size > 0) return;
        addGeneralItems();
        addDTGcVoteItems();
        addCachTypeItems();
        addAttributeItems();

        //Toggle all sections
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                int n = listViewItems.size;
                while (n-- > 0) {
                    if (listViewItems.get(n) instanceof ButtonListViewItem) {
                        ((ButtonListViewItem) listViewItems.get(n)).toggle();
                    }
                }
                CB.postOnNextGlThread(new Runnable() {
                    @Override
                    public void run() {
                        setNewListViewAdapter();
                    }
                });
            }
        });
    }

    private void setNewListViewAdapter() {
        final Array<ListViewItem> visibleList = new Array<>();
        int idxCount = 0;
        for (ListViewItem item : listViewItems) {
            if (item.isVisible()) {
                //reorganise index
                item.setNewIndex(idxCount++);
                visibleList.add(item);
            }
        }
        setListView.setAdapter(new ListViewAdapter() {
            @Override
            public int getCount() {
                return visibleList.size;
            }

            @Override
            public ListViewItem getView(int index) {
                return visibleList.get(index);
            }

            @Override
            public void update(ListViewItem view) {

            }
        });
    }

    private void addGeneralItems() {

        final AtomicBoolean sectionVisible = new AtomicBoolean(true);
        final int buttonIndex = listViewItems.size;
        final IntPropertyListView available = new IntPropertyListView(listViewItems.size + 1,
                filterSettings.filterProperties.NotAvailable, style.Available, Translation.get("disabled"));
        final IntPropertyListView archived = new IntPropertyListView(listViewItems.size + 2,
                filterSettings.filterProperties.Archived, style.PrepareToArchive, Translation.get("archived"));
        final IntPropertyListView finds = new IntPropertyListView(listViewItems.size + 3,
                filterSettings.filterProperties.Finds, style.finds, Translation.get("myfinds"));
        final IntPropertyListView own = new IntPropertyListView(listViewItems.size + 4,
                filterSettings.filterProperties.Own, style.own, Translation.get("myowncaches"));
        final IntPropertyListView withTb = new IntPropertyListView(listViewItems.size + 5,
                filterSettings.filterProperties.ContainsTravelbugs, style.TB, Translation.get("withtrackables"));
        final IntPropertyListView favorites = new IntPropertyListView(listViewItems.size + 6,
                filterSettings.filterProperties.Favorites, style.Favorites, Translation.get("Favorites"));
        final IntPropertyListView hasUserData = new IntPropertyListView(listViewItems.size + 7,
                filterSettings.filterProperties.HasUserData, style.HasUserData, Translation.get("hasuserdata"));
        final IntPropertyListView listingChanged = new IntPropertyListView(listViewItems.size + 8,
                filterSettings.filterProperties.ListingChanged, style.ListingChanged, Translation.get("ListingChanged"));
        final IntPropertyListView manualwaypoint = new IntPropertyListView(listViewItems.size + 9,
                filterSettings.filterProperties.WithManualWaypoint, style.ManualWaypoint, Translation.get("manualwaypoint"));
        final IntPropertyListView corrected = new IntPropertyListView(listViewItems.size + 10,
                filterSettings.filterProperties.hasCorrectedCoordinates, style.CoorectedCoord, Translation.get("hasCorrectedCoordinates"));


        ClickListener listener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                boolean visible = !sectionVisible.get();
                sectionVisible.set(visible);
                available.setVisible(visible);
                archived.setVisible(visible);
                finds.setVisible(visible);
                own.setVisible(visible);
                withTb.setVisible(visible);
                favorites.setVisible(visible);
                hasUserData.setVisible(visible);
                listingChanged.setVisible(visible);
                manualwaypoint.setVisible(visible);
                corrected.setVisible(visible);
                setNewListViewAdapter();
            }
        };

        listViewItems.add(new ButtonListViewItem(buttonIndex, Translation.get("General"), listener));
        listViewItems.add(available);
        listViewItems.add(archived);
        listViewItems.add(finds);
        listViewItems.add(own);
        listViewItems.add(withTb);
        listViewItems.add(favorites);
        listViewItems.add(hasUserData);
        listViewItems.add(listingChanged);
        listViewItems.add(manualwaypoint);
        listViewItems.add(corrected);

    }

    private void addDTGcVoteItems() {

        final AtomicBoolean sectionVisible = new AtomicBoolean(true);
        final int buttonIndex = listViewItems.size;

        final AdjustableStarListViewItem minDificulty = new AdjustableStarListViewItem(listViewItems.size + 1,
                filterSettings.filterProperties.MinDifficulty, Translation.get("minDifficulty"), STAR);
        final AdjustableStarListViewItem maxDificulty = new AdjustableStarListViewItem(listViewItems.size + 2,
                filterSettings.filterProperties.MaxDifficulty, Translation.get("maxDifficulty"), STAR);
        final AdjustableStarListViewItem minTerrain = new AdjustableStarListViewItem(listViewItems.size + 3,
                filterSettings.filterProperties.MinTerrain, Translation.get("minTerrain"), STAR);
        final AdjustableStarListViewItem maxTerrain = new AdjustableStarListViewItem(listViewItems.size + 4,
                filterSettings.filterProperties.MaxTerrain, Translation.get("maxTerrain"), STAR);
        final AdjustableStarListViewItem minContainerSize = new AdjustableStarListViewItem(listViewItems.size + 5,
                filterSettings.filterProperties.MinContainerSize, Translation.get("minContainerSize"), SIZE);
        final AdjustableStarListViewItem maxContainerSize = new AdjustableStarListViewItem(listViewItems.size + 6,
                filterSettings.filterProperties.MaxContainerSize, Translation.get("maxContainerSize"), SIZE);
        final AdjustableStarListViewItem minRating = new AdjustableStarListViewItem(listViewItems.size + 7,
                filterSettings.filterProperties.MinRating, Translation.get("minRating"), STAR);
        final AdjustableStarListViewItem maxRating = new AdjustableStarListViewItem(listViewItems.size + 8,
                filterSettings.filterProperties.MaxRating, Translation.get("maxRating"), STAR);

        final AdjustableFavPointListViewItem minFav = new AdjustableFavPointListViewItem(listViewItems.size + 9,
                filterSettings.filterProperties.MinFavPoints, Translation.get("minFavPoints"));
        final AdjustableFavPointListViewItem maxFav = new AdjustableFavPointListViewItem(listViewItems.size + 10,
                filterSettings.filterProperties.MaxFavPoints, Translation.get("maxFavPoints"));


        ClickListener listener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                final boolean visible = !sectionVisible.get();
                sectionVisible.set(visible);
                minDificulty.setVisible(visible);
                maxDificulty.setVisible(visible);
                minTerrain.setVisible(visible);
                maxTerrain.setVisible(visible);
                minContainerSize.setVisible(visible);
                maxContainerSize.setVisible(visible);
                minRating.setVisible(visible);
                maxRating.setVisible(visible);
                minFav.setVisible(visible);
                maxFav.setVisible(visible);
                setNewListViewAdapter();
            }
        };

        listViewItems.add(new ButtonListViewItem(buttonIndex, "D / T" + String.format("%n") + "GC-Vote", listener));
        listViewItems.add(minDificulty);
        listViewItems.add(maxDificulty);
        listViewItems.add(minTerrain);
        listViewItems.add(maxTerrain);
        listViewItems.add(minContainerSize);
        listViewItems.add(maxContainerSize);
        listViewItems.add(minRating);
        listViewItems.add(maxRating);
        listViewItems.add(minFav);
        listViewItems.add(maxFav);
    }

    private void addCachTypeItems() {
        final AtomicBoolean sectionVisible = new AtomicBoolean(true);
        final Array<BooleanPropertyListView> itemList = new Array<>();
        ClickListener listener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                final boolean visible = !sectionVisible.get();
                sectionVisible.set(visible);
                int n = itemList.size;
                while (n-- > 0) {
                    itemList.get(n).setVisible(visible);
                }
                setNewListViewAdapter();
            }
        };
        listViewItems.add(new ButtonListViewItem(listViewItems.size, Translation.get("CacheTypes"), listener));
        int idx = listViewItems.size;
        for (int i = 0, n = filterSettings.filterProperties.cacheTypes.length; i < n; i++) {
            CacheTypes type = CacheTypes.get(i);
            if (type.isCache())
                itemList.add(new BooleanPropertyListView(idx++,
                        filterSettings.filterProperties.cacheTypes[i], type.getDrawable(), type.getName()));
        }
        for (int i = 0, n = itemList.size; i < n; i++) {
            listViewItems.add(itemList.get(i));
        }
    }

    private void addAttributeItems() {
        final AtomicBoolean sectionVisible = new AtomicBoolean(true);
        final Array<IntPropertyListView> itemList = new Array<>();
        final ClickListener listener = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                final boolean visible = !sectionVisible.get();
                sectionVisible.set(visible);
                int n = itemList.size;
                while (n-- > 0) {
                    itemList.get(n).setVisible(visible);
                }
                setNewListViewAdapter();
            }
        };
        listViewItems.add(new ButtonListViewItem(listViewItems.size, Translation.get("Attributes"), listener));
        int idx = listViewItems.size;
        final AttributesStyle attStyle = VisUI.getSkin().get("CompassView", AttributesStyle.class);
        for (int i = 1, n = filterSettings.filterProperties.attributes.length; i < n; i++) {
            Attributes attribute = Attributes.values()[i];
            itemList.add(new IntPropertyListView(idx++,
                    filterSettings.filterProperties.attributes[i], attribute.getDrawable(attStyle),
                    Translation.get("att_" + i + "_1")));
        }

        for (int i = 0, n = itemList.size; i < n; i++) {
            listViewItems.add(itemList.get(i));
        }
    }

    @Override
    public void onShow() {
        fillList();
    }

    class ButtonListViewItem extends ListViewItem {

        final ClickListener clickListener;

        public ButtonListViewItem(int listIndex, CharSequence text, ClickListener clickListener) {
            super(listIndex);
            this.clickListener = clickListener;
            CB_Button btn = new CB_Button(text);
            btn.getLabel().setWrap(true);

            // add own clicklistener for detect Click and scroll this Item to top of ListView
            this.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    final float scrollPos = setListView.getScrollPos();
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            log.debug("Scroll to Pos {}", scrollPos);
                            setListView.setScrollPos(scrollPos, false);
                        }
                    });
                }
            });

            this.addListener(clickListener);
            this.add(btn).expand().fill();


        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            //clear Background
            this.setBackground((Drawable) null);
            super.draw(batch, parentAlpha);
        }

        @Override
        public void dispose() {

        }

        public void toggle() {
            clickListener.clicked(null, 0, 0);
        }
    }

    class IntPropertyListView extends ListViewItem {

        final IntProperty property;
        final Image checkImage;


        public IntPropertyListView(int listIndex, final IntProperty property, Drawable icon, final CharSequence name) {
            super(listIndex);

            this.property = property;

            //Left icon
            final Image iconImage = new Image(icon, Scaling.none);
            this.add(iconImage).center().padRight(CB.scaledSizes.MARGIN_HALF);

            //Center name text
            final Label label = new VisLabel(name);
            label.setWrap(true);
            this.add(label).expandX().fillX().padTop(CB.scaledSizes.MARGIN).padBottom(CB.scaledSizes.MARGIN);

            //Right checkBox
            checkImage = new Image(style.CheckNo);
            this.add(checkImage).width(checkImage.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

            this.setCheckImage();

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int value = property.getInt();

                    if (value == -1)
                        value = 0;
                    else if (value == 0)
                        value = 1;
                    else if (value == 1)
                        value = -1;
                    property.set(value);
                }
            });

            property.setChangeListener(new Property.PropertyChangedListener() {
                @Override
                public void propertyChanged() {
                    log.debug("Property {} changed to {}", name, property.getInt());

                    //property changed, so set name to "?"
                    filterSettings.filterProperties.setName("?");
                    setCheckImage();
                }
            });
        }

        private void setCheckImage() {
            CB.postOnGlThread(new NamedRunnable("FilterSetListViw:SetCheckImage") {
                @Override
                public void run() {
                    switch (property.getInt()) {
                        case -1:
                            checkImage.setDrawable(style.CheckOff);
                            break;
                        case 0:
                            checkImage.setDrawable(style.CheckNo);
                            break;
                        case 1:
                            checkImage.setDrawable(style.Check);
                            break;
                        default:
                            throw new RuntimeException("Unknown filter property state");
                    }
                }
            });
        }

        @Override
        public void dispose() {

        }
    }

    class AdjustableStarListViewItem extends ListViewItem {

        final IntProperty property;
        final AdjustableStarWidget adjustableWidget;

        public AdjustableStarListViewItem(int listIndex, final IntProperty property, final CharSequence name, AdjustableStarWidget.Type type) {
            super(listIndex);

            this.property = property;

            StarsStyle starsStyle = VisUI.getSkin().get("cachelist", StarsStyle.class);
            CacheSizeStyle cacheSizeStyle = VisUI.getSkin().get("cachelist", CacheSizeStyle.class);
            this.adjustableWidget = new AdjustableStarWidget(type, name, property, starsStyle, cacheSizeStyle);
            this.add(this.adjustableWidget).expandX().fillX().padTop(CB.scaledSizes.MARGIN).padBottom(CB.scaledSizes.MARGIN);

            property.setChangeListener(new Property.PropertyChangedListener() {
                @Override
                public void propertyChanged() {
                    log.debug("Property {} changed to {}", name, property.get());

                    //property changed, so set name to "?"
                    filterSettings.filterProperties.setName("?");
                }
            });


            // ListViewItem catch the ClickEvent from Button/Label
            // So we reroute the event to the Button!
            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.getTarget() instanceof VisTextButton
                            || event.getTarget().getParent() instanceof VisTextButton) {

                        Array<EventListener> listeners = event.getTarget() instanceof VisTextButton ?
                                event.getTarget().getListeners() : event.getTarget().getParent().getListeners();
                        int n = listeners.size;
                        while (n-- > 0) {
                            EventListener listener = listeners.get(n);
                            if (listener instanceof ClickListener) {
                                ((ClickListener) listener).clicked(event, x, y);
                            }
                        }
                    }
                }
            });
        }


        @Override
        public void dispose() {

        }
    }

    class AdjustableFavPointListViewItem extends ListViewItem {

        final IntProperty property;
        final CharSequence name;
        final VisLabel valueLabel;

        final int minValue = -1;


        public AdjustableFavPointListViewItem(int listIndex, final IntProperty property, final CharSequence name) {
            super(listIndex);
            this.property = property;
            this.name = name;
            valueLabel = new VisLabel(Integer.toString(property.get()));
            valueLabel.setAlignment(Align.center);


            property.setChangeListener(new Property.PropertyChangedListener() {
                @Override
                public void propertyChanged() {
                    log.debug("Property {} changed to {}", name, property.get());

                    //property changed, so set name to "?"
                    filterSettings.filterProperties.setName("?");
                }
            });


            // ListViewItem catch the ClickEvent from Button/Label
            // So we reroute the event to the Button!
            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.getTarget() instanceof VisTextButton
                            || event.getTarget().getParent() instanceof VisTextButton) {

                        Array<EventListener> listeners = event.getTarget() instanceof VisTextButton ?
                                event.getTarget().getListeners() : event.getTarget().getParent().getListeners();
                        int n = listeners.size;
                        while (n-- > 0) {
                            EventListener listener = listeners.get(n);
                            if (listener instanceof ClickListener) {
                                ((ClickListener) listener).clicked(event, x, y);
                            }
                        }
                    }
                }
            });


            creatLayout();
            setValue(property.get(), true);
        }

        VisTextButton minusBtn2;
        VisTextButton plusBtn2;


        private void creatLayout() {
            VisTextButton minusBtn = new VisTextButton("-") {
                @Override
                public float getPrefWidth() {
                    return this.getPrefHeight();
                }
            };
            VisTextButton plusBtn = new VisTextButton("+") {
                @Override
                public float getPrefWidth() {
                    return this.getPrefHeight();
                }
            };
            minusBtn2 = new VisTextButton("--") {
                @Override
                public float getPrefWidth() {
                    if (this.isVisible())
                        return this.getPrefHeight();
                    return 0;
                }
            };
            plusBtn2 = new VisTextButton("++") {
                @Override
                public float getPrefWidth() {
                    if (this.isVisible())
                        return this.getPrefHeight();
                    return 0;
                }
            };

            plusBtn.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    int newValue = property.get() + 1;
                    setValue(newValue);
                }
            });

            minusBtn.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    int newValue = property.get() - 1;
                    if (newValue < minValue) newValue = minValue;
                    setValue(newValue);
                }
            });

            plusBtn2.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    int newValue = property.get() + 10;
                    setValue(newValue);
                }
            });

            minusBtn2.addListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    int newValue = property.get() - 10;
                    if (newValue < minValue) newValue = minValue;
                    setValue(newValue);
                }
            });

            VisLabel titleLabel = new VisLabel(name);
            titleLabel.setAlignment(Align.center);
            this.add(titleLabel).left().expandX().fillX();
            this.row();

            VisTable line = new VisTable();
            Image faveIcon = new Image(style.FavPoints);
            line.add(minusBtn2).left().padRight(CB.scaledSizes.MARGIN);
            line.add(minusBtn).left();
            line.add(faveIcon).left().padLeft(CB.scaledSizes.MARGINx2);
            line.add(valueLabel).expandX().fillX();
            line.add(plusBtn).right();
            line.add(plusBtn2).right().padLeft(CB.scaledSizes.MARGIN);
            this.add(line).left().expandX().fillX();
        }

        public void setValue(int value) {
            setValue(value, false);
        }

        private void setValue(final int value, final boolean force) {
            CB.postAsync(new NamedRunnable("FilterSetListView") {
                @Override
                public void run() {
                    if (force || AdjustableFavPointListViewItem.this.property.get() != value) {
                        CB.postOnGlThread(new NamedRunnable("postOnGlThread") {
                            @Override
                            public void run() {
                                if (value == -1) {
                                    valueLabel.setText(Translation.get("DoesntMatter"));
                                } else {
                                    valueLabel.setText(Integer.toString(value));
                                }

                                if (value < 0) {
                                    minusBtn2.setVisible(false);
                                    plusBtn2.setVisible(false);
                                } else {
                                    minusBtn2.setVisible(true);
                                    plusBtn2.setVisible(true);
                                }
                                AdjustableFavPointListViewItem.this.property.set(value);
                                AdjustableFavPointListViewItem.this.invalidateHierarchy();
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void dispose() {

        }
    }

    class BooleanPropertyListView extends ListViewItem {

        final BooleanProperty property;
        final Image checkImage;


        public BooleanPropertyListView(int listIndex, final BooleanProperty property, Drawable icon, final CharSequence name) {
            super(listIndex);

            this.property = property;

            //Left icon
            final Image iconImage = new Image(icon, Scaling.none);
            this.add(iconImage).center().padRight(CB.scaledSizes.MARGIN_HALF);

            //Center name text
            final Label label = new VisLabel(name);
            label.setWrap(true);
            this.add(label).expandX().fillX().padTop(CB.scaledSizes.MARGIN).padBottom(CB.scaledSizes.MARGIN);

            //Right checkBox
            checkImage = new Image(style.CheckNo);
            this.add(checkImage).width(checkImage.getWidth()).pad(CB.scaledSizes.MARGIN / 2);

            this.setCheckImage();

            this.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    property.set(!property.get());
                    event.cancel();
                    event.reset();
                }
            });

            property.setChangeListener(new Property.PropertyChangedListener() {
                @Override
                public void propertyChanged() {
                    log.debug("Property {} changed to {}", name, property.get());

                    //property changed, so set name to "?"
                    filterSettings.filterProperties.setName("?");
                    setCheckImage();
                }
            });
        }

        private void setCheckImage() {
            CB.postOnGlThread(new NamedRunnable("FilterSetListView:checkImage") {
                @Override
                public void run() {
                    if (property.get()) {
                        checkImage.setDrawable(style.Check);
                    } else {
                        checkImage.setDrawable(style.CheckNo);
                    }
                }
            });
        }

        @Override
        public void dispose() {

        }
    }

}
