package de.longri.cachebox3.gui.widgets.list_view;

import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.gui.views.listview.ScrollViewContainer;
import org.junit.jupiter.api.Test;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.HORIZONTAL;
import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static org.hamcrest.MatcherAssert.assertThat;

class ListViewItemLinkedListTest {

    static {
        TestUtils.initialGdx();
    }


    @Test
    void getCompleteSize() {

        int count = 500;
        float size = 123.12f;
        int resultSize = (int) getListViewItemLinkedList(VERTICAL, count, size).getCompleteSize();
        int shouldSize = 61560;
        assertThat("Complete size was wrong", resultSize == shouldSize);

        count = 300;
        size = 23.13f;
        resultSize = (int) getListViewItemLinkedList(HORIZONTAL, count, size).getCompleteSize();
        shouldSize = 6938;
        assertThat("Complete size was wrong", resultSize == shouldSize);
    }

    @Test
    void ListViewTest() {

        final int itemCount = 250;
        final float defaultSize = 12.34f;
        final float itemSize = 123.4f;

        de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle style = new de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle();
        style.pad = 10f;

        ListView listView = new ListView(VERTICAL, style);

        listView.setAdapter(new ListViewAdapter() {
            @Override
            public boolean isReverseOrder() {
                return false;
            }

            @Override
            public int getCount() {
                return itemCount;
            }

            @Override
            public ListViewItem getView(int index) {
                ListViewItem item = new ListViewItem(index);
                item.setHeight(itemSize);
                return item;
            }

            @Override
            public void update(ListViewItem view) {

            }

            @Override
            public float getDefaultItemSize() {
                return defaultSize;
            }
        });

        int resultSize = (int) (listView.scrollPane.getActor().getHeight());
        int shouldSize = 8084;

        assertThat("Complete size was wrong", resultSize == shouldSize);


        assertThat("The child count should be 0",
                ((ScrollViewContainer) listView.scrollPane.getActor()).getChildren().size == 0);


        listView.setSize(200, 500);
        listView.layout();

        assertThat("ScrollPane with are wrong", listView.scrollPane.getWidth() == 200);
        assertThat("ScrollPane height are wrong", listView.scrollPane.getHeight() == 500);

        int childCount = (int) ((500 / (defaultSize + style.pad + style.pad)) + 2 + ListViewItemLinkedList.OVERLOAD);

        //child's will added on GlThread, so wait a moment
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("The child count should be " + childCount,
                ((ScrollViewContainer) listView.scrollPane.getActor()).getChildren().size == childCount);

        listView.setScrollPos(250, false);

        childCount = (int) ((500 / (defaultSize + style.pad + style.pad)) - 1 + (ListViewItemLinkedList.OVERLOAD * 2));

        //child's will added on GlThread, so wait a moment
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("The child count should be " + childCount,
                ((ScrollViewContainer) listView.scrollPane.getActor()).getChildren().size == childCount);


        ((ListViewItemLinkedList) ((ScrollViewContainer) listView.scrollPane.getActor())).replaceDummy();
        //child's will added on GlThread, so wait a moment
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        childCount = 35;
        assertThat("The child count should be " + childCount,
                ((ScrollViewContainer) listView.scrollPane.getActor()).getChildren().size == childCount);


    }

    private ListViewItemLinkedList getListViewItemLinkedList(ListViewType type, final int count, final float size) {

        de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle style = new de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle();

        style.pad = 1f;

        ListViewItemLinkedList list = new ListViewItemLinkedList(type, style, 0, 0, 0, 0);

        list.setAdapter(new ListViewAdapter() {
            @Override
            public boolean isReverseOrder() {
                return false;
            }

            @Override
            public int getCount() {
                return count;
            }

            @Override
            public ListViewItem getView(int index) {
                return null;
            }

            @Override
            public void update(ListViewItem view) {

            }

            @Override
            public float getDefaultItemSize() {
                return size;
            }
        });

        return list;
    }

}