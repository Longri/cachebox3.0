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
package de.longri.cachebox3.platform_test;

/**
 * Created by Longri on 18.03.2019.
 */
public class Assert {
    public static void assertThat(String reason, boolean assertion) throws PlatformAssertionError {
        if (!assertion) {
            throw new PlatformAssertionError(reason);
        }
    }

    public static void assertThat(boolean assertion) throws PlatformAssertionError {
        if (!assertion) {
            throw new PlatformAssertionError("");
        }
    }


    public static void assertEquals(Object expected, Object actual, String reason) throws PlatformAssertionError {
        if (!objectsAreEqual(expected, actual)) {
            throw new PlatformAssertionError(reason);
        }
    }

    public static void assertEquals(Object expected, Object actual) throws PlatformAssertionError {
        if (!objectsAreEqual(expected, actual)) {
            throw new PlatformAssertionError("");
        }
    }

    static boolean objectsAreEqual(Object obj1, Object obj2) {
        if (obj1 == null) {
            return (obj2 == null);
        }
        return obj1.equals(obj2);
    }

    public static void assertTrue(boolean assertion) throws PlatformAssertionError {
        assertTrue(assertion, "");
    }

    public static void assertTrue(boolean assertion, String reason) throws PlatformAssertionError {
        if (!assertion) {
            throw new PlatformAssertionError(reason);
        }
    }

    public static void assertFalse(boolean assertion) throws PlatformAssertionError {
        assertFalse(assertion, "");
    }

    public static void assertFalse(boolean assertion, String reason) throws PlatformAssertionError {
        if (assertion) {
            throw new PlatformAssertionError(reason);
        }
    }

    public static void assertNotNull(Object obj) throws PlatformAssertionError {
        assertNotNull(obj, "");
    }

    public static void assertNotNull(Object obj, String message) throws PlatformAssertionError {
        if (obj == null) throw new PlatformAssertionError(message);
    }
}