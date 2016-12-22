package de.longri.cachebox3.locator.geocluster;


import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.oscim.core.GeoPoint;

public class GeoPointMatchers {

    private GeoPointMatchers() {

    }

    public static TypeSafeMatcher<GeoPoint> closeTo(final GeoPoint expected) {
        return closeTo(expected, 0.001);
    }

    public static TypeSafeMatcher<GeoPoint> closeTo(final GeoPoint expected, final double error) {
        return new CustomTypeSafeMatcher<GeoPoint>("close to \"" + expected.toString() + "\"") {
            @Override
            protected void describeMismatchSafely(GeoPoint item, Description description) {
                description.appendText("was ").appendValue(item.toString());
            }

            @Override
            protected boolean matchesSafely(GeoPoint point) {
                return Matchers.closeTo(point.getLatitude(), error).matches(expected.getLatitude()) &&
                        Matchers.closeTo(point.getLongitude(), error).matches(expected.getLongitude());
            }
        };
    }
}
