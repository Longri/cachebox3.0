/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.develop.tools.skin_editor.actors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Longri on 2019-07-29.
 */
public class TreeNode extends Tree.Node {
    final Actor actor;
    TreeNode parent;
    final Array<TreeNode> children = new Array(0);
    boolean selectable = true;
    boolean expanded;
    Drawable icon;
    float height;
    Object object;

    public TreeNode(Actor actor) {
        if (actor == null) throw new IllegalArgumentException("actor cannot be null.");
        this.actor = actor;
    }

    public void setExpanded (boolean expanded) {
        if (expanded == this.expanded) return;
        this.expanded = expanded;
        if (children.size == 0) return;
        Tree tree = getTree();
        if (tree == null) return;
        if (expanded) {
            for (int i = 0, n = children.size; i < n; i++)
                children.get(i).addToTree(tree);
        } else {
            for (int i = children.size - 1; i >= 0; i--)
                children.get(i).removeFromTree(tree);
        }
        tree.invalidateHierarchy();
    }

    /** Called to add the actor to the tree when the node's parent is expanded. */
    protected void addToTree (Tree tree) {
        tree.addActor(actor);
        if (!expanded) return;
        Object[] children = this.children.items;
        for (int i = this.children.size - 1; i >= 0; i--)
            ((TreeNode)children[i]).addToTree(tree);
    }

    /** Called to remove the actor from the tree when the node's parent is collapsed. */
    protected void removeFromTree (Tree tree) {
        tree.removeActor(actor);
        if (!expanded) return;
        Object[] children = this.children.items;
        for (int i = this.children.size - 1; i >= 0; i--)
            ((TreeNode)children[i]).removeFromTree(tree);
    }

    public void add (TreeNode node) {
        insert(children.size, node);
    }


    public void insert (int index, TreeNode node) {
        node.parent = this;
        children.insert(index, node);
        updateChildren();
    }

    public void remove () {
        Tree tree = getTree();
        if (tree != null)
            tree.remove(this);
        else if (parent != null) //
            parent.remove(this);
    }

    public void remove (TreeNode node) {
        children.removeValue(node, true);
        if (!expanded) return;
        Tree tree = getTree();
        if (tree == null) return;
        node.removeFromTree(tree);
    }

    public void removeAll () {
        Tree tree = getTree();
        if (tree != null) {
            Object[] children = this.children.items;
            for (int i = this.children.size - 1; i >= 0; i--)
                ((TreeNode)children[i]).removeFromTree(tree);
        }
        children.clear();
    }

    /** Returns the tree this node is currently in, or null. */
    public Tree getTree () {
        Group parent = actor.getParent();
        if (!(parent instanceof Tree)) return null;
        return (Tree)parent;
    }

    public Actor getActor () {
        return actor;
    }

    public boolean isExpanded () {
        return expanded;
    }

    /** If the children order is changed, {@link #updateChildren()} must be called. */
    public Array<TreeNode> getChildren () {
        return children;
    }

    public boolean hasChildren () {
        return children.size > 0;
    }

    /** Removes the child node actors from the tree and adds them again. This is useful after changing the order of
     * {@link #getChildren()}.
     * @see Tree#updateRootNodes() */
    public void updateChildren () {
        if (!expanded) return;
        Tree tree = getTree();
        if (tree == null) return;
        for (int i = children.size - 1; i >= 0; i--)
            children.get(i).removeFromTree(tree);
        for (int i = 0, n = children.size; i < n; i++)
            children.get(i).addToTree(tree);
    }

    /** @return May be null. */
    public TreeNode getParent () {
        return parent;
    }

    /** Sets an icon that will be drawn to the left of the actor. */
    public void setIcon (Drawable icon) {
        this.icon = icon;
    }

    public Object getObject () {
        return object;
    }

    /** Sets an application specific object for this node. */
    public void setObject (Object object) {
        this.object = object;
    }

    public Drawable getIcon () {
        return icon;
    }

    public int getLevel () {
        int level = 0;
        TreeNode current = this;
        do {
            level++;
            current = current.getParent();
        } while (current != null);
        return level;
    }







    /** Expands all parent nodes of this node. */
    public void expandTo () {
        TreeNode node = parent;
        while (node != null) {
            node.setExpanded(true);
            node = node.parent;
        }
    }

    public boolean isSelectable () {
        return selectable;
    }

    public void setSelectable (boolean selectable) {
        this.selectable = selectable;
    }

    /** Returns the height of the node as calculated for layout. A subclass may override and increase the returned height to
     * create a blank space in the tree above the node, eg for a separator. */
    public float getHeight () {
        return height;
    }
}
