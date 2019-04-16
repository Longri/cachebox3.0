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

import com.badlogic.gdx.utils.StringBuilder;

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

    public static void assertEquals(double expected, double actual, double tolerance, String reason) throws PlatformAssertionError {
        double a = expected - actual;
        if (a < 0)
            a *= -1;

        if (a > tolerance) {
            StringBuilder sb = new StringBuilder(reason);
            sb.appendLine("");
            sb.appendLine("expected:");
            sb.appendLine(String.valueOf(expected));
            sb.appendLine("");
            sb.appendLine("actual:");
            sb.appendLine(String.valueOf(actual));
            throw new PlatformAssertionError(sb.toString());
        }
    }

    public static void assertEquals(Object expected, Object actual, String reason) throws PlatformAssertionError {
        if (!objectsAreEqual(expected, actual)) {
            StringBuilder sb = new StringBuilder(reason);
            sb.appendLine("");
            sb.appendLine("expected:");
            sb.appendLine(String.valueOf(expected));
            sb.appendLine("");
            sb.appendLine("actual:");
            sb.appendLine(String.valueOf(actual));
            throw new PlatformAssertionError(sb.toString());
        }
    }

    public static void assertEquals(Object expected, Object actual) throws PlatformAssertionError {
        if (!objectsAreEqual(expected, actual)) {
            StringBuilder sb = new StringBuilder();
            sb.appendLine("expected:");
            sb.appendLine(String.valueOf(expected));
            sb.appendLine("");
            sb.appendLine("actual:");
            sb.appendLine(String.valueOf(actual));
            throw new PlatformAssertionError(sb.toString());
        }
    }

    static boolean objectsAreEqual(Object obj1, Object obj2) {
        if (obj1 == null) {
            return (obj2 == null);
        }
        if (obj1 instanceof Number || obj2 instanceof Number) return numberEqualse((Number) obj1, (Number) obj2);
        return obj1.equals(obj2);
    }

    private static boolean numberEqualse(Number obj1, Number obj2) {
        if (obj1 instanceof Double || obj1 instanceof Float || obj2 instanceof Double || obj2 instanceof Float)
            return obj1.doubleValue() == obj2.doubleValue();
        return obj1.longValue() == obj2.longValue();
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
