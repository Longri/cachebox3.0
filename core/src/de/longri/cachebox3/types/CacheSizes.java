/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
package de.longri.cachebox3.types;

public enum CacheSizes {
    micro,     // 0
    small,     // 1
    regular,   // 2
    large,     // 3
    other,     // 4
    virtual,   // 5
    notChosen, // 6
    ;

    public static CacheSizes parseInt(int value) {
        switch (value) {
            case 0:
                return CacheSizes.micro;
            case 1:
                return CacheSizes.small;
            case 2:
                return CacheSizes.regular;
            case 3:
                return CacheSizes.large;
            case 4:
                return CacheSizes.other;
            case 5:
                return CacheSizes.virtual;
            default:
                return CacheSizes.notChosen;
        }
    }

    public static CacheSizes parseString(String text) {
        // Groundspeak
        if (text.equalsIgnoreCase("micro")) {
            return CacheSizes.micro;
        }
        if (text.equalsIgnoreCase("small")) {
            return CacheSizes.small;
        }
        if (text.equalsIgnoreCase("regular")) {
            return CacheSizes.regular;
        }
        if (text.equalsIgnoreCase("large")) {
            return CacheSizes.large;
        }
        if (text.equalsIgnoreCase("not chosen")) {
            return CacheSizes.other;
        }
        if (text.equalsIgnoreCase("virtual")) {
            return CacheSizes.other;
        }
        if (text.equalsIgnoreCase("other")) {
            return CacheSizes.other;
        }
        // GCTour
        if (text.equalsIgnoreCase("none")) {
            return CacheSizes.other;
        }
        if (text.equalsIgnoreCase("very large")) {
            return CacheSizes.large;
        }
        return CacheSizes.other;
    }

    @Override
    public String toString() {
        switch (this) {
            case large:
                return "Large";
            case micro:
                return "Micro";
            case other:
                return "Other";
            case regular:
                return "Regular";
            case small:
                return "Small";
            case notChosen:
                return "Not Chosen";
            case virtual:
                return "Virtual";
            default:
                break;

        }

        return super.toString();
    }

    public String toShortString() {
        switch (this) {
            case large:
                return "L";
            case micro:
                return "M";
            case other:
                return "O";
            case regular:
                return "R";
            case small:
                return "S";
            default:
                break;
        }
        return "?";
    }

}
