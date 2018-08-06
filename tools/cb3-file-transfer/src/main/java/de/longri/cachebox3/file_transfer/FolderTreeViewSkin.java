package de.longri.cachebox3.file_transfer;

/**
 * Created by Longri on 27.02.2018.
 * https://stackoverflow.com/a/27786741
 */
import javafx.scene.control.TreeView;
import com.sun.javafx.scene.control.skin.TreeViewSkin;


/**
 * Only done as a workaround until https://javafx-jira.kenai.com/browse/RT-18965
 * is resolved.
 */
public class FolderTreeViewSkin extends TreeViewSkin<Object>
{
    public FolderTreeViewSkin(TreeView<Object> treeView)
    {
        super(treeView);
    }

    public boolean isIndexVisible(int index)
    {
        if (flow.getFirstVisibleCell() != null &&
                flow.getLastVisibleCell() != null &&
                flow.getFirstVisibleCell().getIndex() <= index &&
                flow.getLastVisibleCell().getIndex() >= index)
            return true;
        return false;
    }
}
