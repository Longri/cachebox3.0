package de.longri.cachebox3.locator.geocluster;


import de.longri.cachebox3.locator.LatLong;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

public class LatLongMatchers {

    private LatLongMatchers() {

    }

    public static TypeSafeMatcher<LatLong> closeTo(final LatLong expected) {
        return closeTo(expected, 0.001);
    }

    public static TypeSafeMatcher<LatLong> closeTo(final LatLong expected, final double error) {
        return new CustomTypeSafeMatcher<LatLong>("close to \"" + expected.toString() + "\"") {
            @Override
            protected void describeMismatchSafely(LatLong item, Description description) {
                description.appendText("was ").appendValue(item.toString());
            }

            @Override
            protected boolean matchesSafely(LatLong point) {
                return Matchers.closeTo(point.latitude, error).matches(expected.latitude) &&
                        Matchers.closeTo(point.longitude, error).matches(expected.longitude);
            }
        };
    }
}
