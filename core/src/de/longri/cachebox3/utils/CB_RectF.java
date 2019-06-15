/* 
 * Copyright (C) 2011-2017 team-cachebox.de
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

package de.longri.cachebox3.utils;

import com.badlogic.gdx.math.Vector2;
import de.longri.cachebox3.utils.lists.CB_List;


/**
 * Eine Struktur für RectF mit besonderen Methoden Speziel für die Handhabung in der Verwendung der Berechneten Grössen und Positionen
 * einzelner UI Elemente in Cachebox
 *
 * @author Longri
 */
public class CB_RectF {
    // Member

    /**
     * Holds all values <br>
     * <br>
     * [0] = Pos.x <br>
     * [1] = Pos.y <br>
     * [2] = width <br>
     * [3] = height <br>
     * [4] = halfWidth <br>
     * [5] = halfHeight <br>
     * [6] = crossPos.x <br>
     * [7] = crossPos.y <br>
     * [8] = centerPos.x <br>
     * [9] = centerPos.y <br>
     */
    private float[] member = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    public float getHalfWidth() {
        return member[4];
    }

    public float getHalfHeight() {
        return member[5];
    }

    // Constructors

    /**
     * Constructor der alle Member mit 0 initialisiert!
     */
    public CB_RectF() {
    }

    public CB_RectF(SizeF size) {
        member[3] = size.height;
        member[2] = size.width;
        calcCrossCorner();
    }

    /**
     * Constructor für ein neues RectF mit Angabe der linken unteren Ecke und der Höhe und Breite
     *
     * @param X
     * @param Y
     * @param Width
     * @param Height
     */
    public CB_RectF(float X, float Y, float Width, float Height) {
        member[0] = X;
        member[1] = Y;
        member[3] = Height;
        member[2] = Width;
        calcCrossCorner();
    }

    public CB_RectF(CB_RectF rec) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!;
        if (rec != null && rec.member != null)
            System.arraycopy(rec.member, 0, this.member, 0, 10);
    }

    public void setWidth(float Width) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!;
        if (member[2] == Width)
            return;
        member[2] = Width;
        calcCrossCorner();
        callRecChanged();
    }

    public void setHeight(float Height) {
        if (member[3] == Height)
            return;
        member[3] = Height;
        calcCrossCorner();
        callRecChanged();
    }

    public boolean setSize(SizeF Size) {
        return setSize(Size.width, Size.height);
    }

    /**
     * Setzt die Werte für Height und Width. Wenn sich einer der Werte geändert hat, wird ein True zurück gegeben, ansonsten False.
     *
     * @param Width
     * @param Height
     * @return
     */
    public boolean setSize(float Width, float Height) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! false; // isDisposed!
        if (member[2] == Width && member[3] == Height)
            return false;
        member[2] = Width;
        member[3] = Height;
        calcCrossCorner();
        callRecChanged();
        return true;
    }

    public boolean setSize(CB_RectF rec) {
        setSize(rec.member[2], rec.member[3]);
        return true;
    }

    public void setPos(Vector2 Pos) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        if (member[0] == Pos.x && member[1] == Pos.y)
            return;
        member[0] = Pos.x;
        member[1] = Pos.y;
        calcCrossCorner();
    }

    public CB_RectF offset(Vector2 Offset) {
        return offset(Offset.x, Offset.y);
    }

    public CB_RectF offset(float offX, float offY) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! null; // isDisposed!
        member[0] += offX;
        member[1] += offY;
        calcCrossCorner();
        return this;
    }

    public float getX() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[0];
    }

    public float getY() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[1];
    }

    public float getWidth() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[2];
    }

    public float getHeight() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[3];
    }

    /**
     * Berechnet die rechte obere Ecke
     */
    protected void calcCrossCorner() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!;// isDisposed!
        this.member[4] = this.member[2] / 2;
        this.member[5] = this.member[3] / 2;

        this.member[6] = this.member[0] + this.member[2];
        this.member[7] = this.member[1] + this.member[3];
        this.member[8] = this.member[0] + this.member[4];
        this.member[9] = this.member[1] + this.member[5];
    }

    public boolean contains(Vector2 ret) {
        if (ret == null)
            return false;
        return contains(ret.x, ret.y);
    }

    public boolean contains(float x, float y) {
        if (this.member == null)
            return false;

        try {
            // runde
            float rX = Math.round(x);
            float rY = Math.round(y);
            float rTX = Math.round(this.member[0]);
            float rTY = Math.round(this.member[1]);
            float rTCX = Math.round(this.member[6]);
            float rTCY = Math.round(this.member[7]);

            return this.member[2] > 0 && this.member[3] > 0 // check for empty first
                    && rX >= rTX && rX <= rTCX && rY >= rTY && rY <= rTCY;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * liefert True, wenn das übergebene Rechteck kommplett in diese rechteck Passt.
     *
     * @param rec
     * @return
     */
    public boolean contains(CB_RectF rec) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! false; // isDisposed!
        if (rec == null)
            return false;
        boolean ret = this.contains(rec.member[0], rec.member[1]);
        ret &= this.contains(rec.member[6], rec.member[7]);

        return ret;
    }

    private CB_List<SizeChangedEvent> list = new CB_List<>();

    public CB_RectF add(SizeChangedEvent event) {
        synchronized (list) {
            if (list == null)
                return null; // is disposed
            list.add(event);
        }
        return this;
    }

    public void Remove(SizeChangedEvent event) {
        synchronized (list) {
            if (list == null)
                return; // is disposed
            list.removeValue(event,true);
        }
    }

    public void callRecChanged() {
        synchronized (list) {
            if (list == null)
                return; // is disposed

            resize(this.member[2], this.member[3]);

            if (list.size > 0) {
                for (int i = 0, n = list.size; i < n; i++) {
                    list.get(i).sizeChanged();
                }
            }
        }
    }

    public void resize(float width, float height) {
    }

    public boolean equals(CB_RectF rec) {
        if (member == null || rec.member == null)
            return false; // any is disposed!

        // Compare only x,y,width and height
        if (this.member[0] != rec.member[0])
            return false;
        if (this.member[1] != rec.member[1])
            return false;
        if (this.member[2] != rec.member[2])
            return false;
        if (this.member[3] != rec.member[3])
            return false;

        return true;
    }

    public CB_RectF copy() {
        return new CB_RectF(this);
    }

    public void setY(float i) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        if (this.member[1] == i)
            return;
        this.member[1] = i;
        calcCrossCorner();
        callRecChanged();
    }

    public void setX(float i) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        if (this.member[0] == i)
            return;
        this.member[0] = i;
        calcCrossCorner();
        callRecChanged();
    }

    // /**
    // * Setzt Height und Width auf die nächst größere Potenz von 2
    // */
    // public void setPO2()
    // {
    // int PO2width = getNextHighestPO2((int) this.member[2]);
    // int PO2height = getNextHighestPO2((int) this.member[3]);
    //
    // setSize(PO2width, PO2height);
    // }

    // /**
    // * Calculates the next highest power of two for a given integer.
    // *
    // * @param n
    // * the number
    // * @return a power of two equal to or higher than n
    // */
    // public static int getNextHighestPO2(int n)
    // {
    // n -= 1;
    // n = n | (n >> 1);
    // n = n | (n >> 2);
    // n = n | (n >> 4);
    // n = n | (n >> 8);
    // n = n | (n >> 16);
    // n = n | (n >> 32);
    // return n + 1;
    // }

    public CB_RectF scaleCenter(float scaleFactor) {
        return scaleCenter(this, scaleFactor);
    }

    public static CB_RectF scaleCenter(CB_RectF rectangle, float scaleFactor) {
        float newWidth = rectangle.getWidth() * scaleFactor;
        float newHeight = rectangle.getHeight() * scaleFactor;
        float newX = rectangle.member[0] + ((rectangle.getWidth() - newWidth) / 2);
        float newY = rectangle.member[1] + ((rectangle.getHeight() - newHeight) / 2);
        return new CB_RectF(newX, newY, newWidth, newHeight);
    }

    /**
     * Gibt den ersten Schnittpunkt des Rechtecks zwichen den Punkten P1 und P2 zurück! <img src="doc-files/rec-intersection.png"
     * width=537 height=307>
     *
     * @param P1 = start Punkt der Linie
     * @param P2 = End Punkt der Line
     * @return Punkt (b) da dieser als erster Schnittpunkt gefunden wird.
     */
    public Vector2 getIntersection(Vector2 P1, Vector2 P2) {
        return getIntersection(P1, P2, 1);
    }

    /**
     * Gibt den ersten Schnittpunkt des Rechtecks zwichen den Punkten P1 und P2 zurück! </br> Wobei die als int übergebene Nummer der
     * Gerade des Rechtecks als erstes überprüft wird. </br> <img src="doc-files/rec-intersection.png" width=537 height=307>
     *
     * @param P1    = start Punkt der Linie
     * @param P2    = End Punkt der Line
     * @param first
     * @return Punkt (b) wenn first=1 </br> Punkt (a) wenn first=2,3 oder 4 </br>
     */
    public Vector2 getIntersection(Vector2 P1, Vector2 P2, int first) {

        // Array mit Geraden Nummern füllen
        if (Geraden.size < 4) {
            Geraden.add(1);
            Geraden.add(2);
            Geraden.add(3);
            Geraden.add(4);
        }

        Geraden.MoveItemFirst(Geraden.indexOf(first,false));

        Vector2 ret = new Vector2();

        for (int i = 0, n = Geraden.size; i < n; i++) {
            switch (Geraden.get(i)) {
                case 1:

                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(this.member[0], this.member[1]), new Vector2(this.member[6], this.member[1]), ret)) {
                        if (contains(ret))
                            return ret; // 1 unten
                    }
                    break;

                case 2:
                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(this.member[0], this.member[1]), new Vector2(this.member[0], this.member[7]), ret)) {
                        if (contains(ret))
                            return ret; // 2 links
                    }
                    break;

                case 3:
                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(this.member[6], this.member[7]), new Vector2(this.member[6], this.member[1]), ret)) {
                        if (contains(ret))
                            return ret; // 3 rechts
                    }

                    break;

                case 4:
                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(this.member[6], this.member[7]), new Vector2(this.member[0], this.member[7]), ret)) {
                        if (contains(ret))
                            return ret; // 4 oben
                    }
                    break;
            }
        }

        return null;
    }

    private static MoveableList<Integer> Geraden = new MoveableList<Integer>();

    /**
     * Returns the smallest X coordinate of the framing rectangle of the <code>CB_RectF</code> in <code>double</code> precision.
     *
     * @return the smallest x coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    public float getMinX() {
        return getX();
    }

    /**
     * Returns the smallest Y coordinate of the framing rectangle of the <code>CB_RectF</code> in <code>double</code> precision.
     *
     * @return the smallest y coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    public float getMinY() {
        return getY();
    }

    /**
     * Returns the largest X coordinate of the framing rectangle of the <code>CB_RectF</code> in <code>double</code> precision.
     *
     * @return the largest x coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    public float getMaxX() {
        return getX() + getWidth();
    }

    /**
     * Returns the largest Y coordinate of the framing rectangle of the <code>CB_RectF</code> in <code>double</code> precision.
     *
     * @return the largest y coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    public float getMaxY() {
        return getY() + getHeight();
    }

    public CB_RectF createIntersection(CB_RectF rec) {
        float x1 = Math.max(this.getMinX(), rec.getMinX());
        float y1 = Math.max(this.getMinY(), rec.getMinY());
        float x2 = Math.min(this.getMaxX(), rec.getMaxX());
        float y2 = Math.min(this.getMaxY(), rec.getMaxY());
        return new CB_RectF(x1, y1, x2 - x1, y2 - y1);
    }

    public SizeF getSize() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! new SizeF(); // isDisposed!
        return new SizeF(this.member[2], this.member[3]);
    }

    public void setRec(CB_RectF rec) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        if (rec == null)
            return;
        // chk of changes
        if (this.equals(rec))
            return;
        System.arraycopy(rec.member, 0, this.member, 0, 10);
        callRecChanged();
    }

    @Override
    public String toString() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! "disposed Rec"; // isDisposed!
        return "rec X,Y/Width,Height = " + this.getX() + "," + this.getY() + "/" + this.member[2] + "," + this.member[3];
    }

    public void setPos(float x, float y) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        // chk of changes
        if (this.member[0] == x && this.member[1] == y)
            return;

        this.member[0] = x;
        this.member[1] = y;
        calcCrossCorner();
        callRecChanged();
    }

    public float getCenterPosX() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return this.member[8];
    }

    public float getCenterPosY() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return this.member[9];
    }

    public void set(float x, float y, float width, float height) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!
        // chk of changes
        if (this.member[0] == x && this.member[1] == y && this.member[2] == width && this.member[3] == height)
            return;
        this.member[0] = x;
        this.member[1] = y;
        this.member[2] = width;
        this.member[3] = height;
        calcCrossCorner();
        callRecChanged();
    }

    public void dispose() {
        if (list != null) {
            synchronized (list) {
                list.clear();
                list = null;
            }
        }
        member = null;

    }
}
