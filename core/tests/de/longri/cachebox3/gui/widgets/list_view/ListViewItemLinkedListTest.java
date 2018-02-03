package de.longri.cachebox3.gui.widgets.list_view;

import de.longri.cachebox3.TestUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ListViewItemLinkedListTest {

    static {
        TestUtils.initialGdx();
    }


    @Test
    void getCompleteSize() {

        int count = 500;
        float size = 123.123456f;
        assertThat("Complete size was wrong", getListViewItemLinkedList(count, size).getCompleteSize() == (size * (float) count));

        count = 300;
        size = 23.123f;
        assertThat("Complete size was wrong", getListViewItemLinkedList(count, size).getCompleteSize() == (size * (float) count));

    }

    private ListViewItemLinkedList getListViewItemLinkedList(final int count, final float size) {
        return new ListViewItemLinkedList(new ListViewAdapter() {
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
            public float getItemSize(int index) {
                return size;
            }
        });
    }

}