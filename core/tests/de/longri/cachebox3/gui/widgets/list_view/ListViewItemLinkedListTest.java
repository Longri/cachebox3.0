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
        int shouldSize = (int) (size * (float) count);
        assertThat("Complete size was wrong", resultSize == shouldSize);

        count = 300;
        size = 23.13f;
        resultSize = (int) getListViewItemLinkedList(HORIZONTAL, count, size).getCompleteSize();
        shouldSize = (int) (size * (float) count);
        assertThat("Complete size was wrong", resultSize == shouldSize);
    }

    @Test
    void ListViewTest() {

        final int itemCount = 250;
        final float defaultSize = 12.34f;
        final float itemSize = 123.4f;

        de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle style = new de.longri.cachebox3.gui.views.listview.ListView.ListViewStyle();

        ListView listView = new ListView(VERTICAL, style);

        listView.setAdapter(new ListViewAdapter() {
            @Override
            public int getCount() {
                return itemCount;
            }

            @Override
            public ListViewItem getView(int index) {
                DummyListViewItem item = new DummyListViewItem(index);
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

        int resultSize = (int) listView.scrollPane.getActor().getHeight();
        int shouldSize = (int) (defaultSize * (float) itemCount);

        assertThat("Complete size was wrong", resultSize == shouldSize);


        assertThat("The child count should be 0",
                ((ScrollViewContainer) listView.scrollPane.getActor()).getChildren().size == 0);


        listView.setSize(200, 500);

        assertThat("ScrollPane with are wrong", listView.scrollPane.getWidth() == 200);
        assertThat("ScrollPane height are wrong", listView.scrollPane.getHeight() == 500);

        int childCount = (int) ((500 / defaultSize) + 2 + ListViewItemLinkedList.OVERLOAD);

        //child's will added on GlThread, so wait a moment
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("The child count should be " + childCount,
                ((ScrollViewContainer) listView.scrollPane.getActor()).getChildren().size == childCount);


    }

    private ListViewItemLinkedList getListViewItemLinkedList(ListViewType type, final int count, final float size) {
        ListViewItemLinkedList list = new ListViewItemLinkedList(type);

        list.setAdapter(new ListViewAdapter() {
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