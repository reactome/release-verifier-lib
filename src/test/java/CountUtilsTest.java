import org.junit.Test;
import org.reactome.release.verifier.CountUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 8/20/2024
 */
public class CountUtilsTest {

    @Test
    public void percentDropOfLessThan5IsFalse() {
        boolean actual = CountUtils.greaterThanOrEqualTo5PercentDrop(951, 1000);
        assertThat(actual, equalTo(false));
    }

    @Test
    public void percentDropOfExactly5IsTrue() {
        boolean actual = CountUtils.greaterThanOrEqualTo5PercentDrop(950, 1000);
        assertThat(actual, equalTo(true));
    }

    @Test
    public void percentDropOfMoreThan5IsTrue() {
        boolean actual = CountUtils.greaterThanOrEqualTo5PercentDrop(949, 1000);
        assertThat(actual, equalTo(true));
    }

}
