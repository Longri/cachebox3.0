

//  Don't modify this file, it's created by tool 'extract_libgdx_test

package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.gui.widgets.list_view.*;

import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewItemLinkedList.search;
import static de.longri.cachebox3.platform_test.Assert.assertEquals;

public class ListViewItemLinkedListTest {

    static {
        TestUtils.initialGdx();
    }


    @Test
    public void search() throws PlatformAssertionError {
        // fill a item list
        ListViewItemInterface[] arr = new ListViewItemInterface[7];
        for (int i = 0; i < 7; i++) {
            arr[i] = new GalleryItem(i, null);
            arr[i].setWidth(502);
        }

        // set item pos and width
        arr[0].setX(1.4f);
        arr[1].setX(506f);
        arr[2].setX(1011f);
        arr[3].setX(1516f);
        arr[4].setX(2021f);
        arr[5].setX(2526f);
        arr[6].setX(3031f);

        float searchPos = 971;
        float size = 502;

        ListViewItemInterface firstItem = ListViewItemLinkedList.search(ListViewType.HORIZONTAL, arr, searchPos, size);
        assertEquals(1, firstItem.getListIndex(), "index must be 1");

        ListViewItemInterface lastItem = ListViewItemLinkedList.search(ListViewType.HORIZONTAL, arr, searchPos + size, size);
        assertEquals(2, lastItem.getListIndex(), "index must be 2");

        float visualFirst = ListViewItemLinkedList.getVisualSize(ListViewType.HORIZONTAL, firstItem, searchPos, size);
        assertEquals(37.0f, visualFirst, "index must be 37.0");


        float visualLast = ListViewItemLinkedList.getVisualSize(ListViewType.HORIZONTAL, lastItem, searchPos, size);
        assertEquals(462.0f, visualLast, "index must be 462.0");

    }
}
