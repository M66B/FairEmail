package eu.faircode.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlHelperTest {

    @Test
    void autolink() {

        testAutolink(
                "To visit http://www.example.org, go to http://www.example.org.",

                "" +
                        // FIXME: The trailing comma must not be part of the URL.
                        "To visit <a href=\"http://www.example.org,\">http://www.example.org,</a> " +
                        // FIXME: The trailing dot must not be part of the URL.
                        "go to <a href=\"http://www.example.org.\">http://www.example.org.</a>");

        testAutolink(
                "one hhhhh|spt://example.org three",

                // FIXME: "hhhhh|spt" is not a proper URL scheme.
                "one <a href=\"hhhhh|spt://example.org\">hhhhh|spt://example.org</a> three"
        );

        testAutolink(
                "https://example.org/search?q=%C3%A4&hl=nl",

                // TODO: Strictly speaking, the & should be encoded as &amp;.
                // Most browsers can deal with this situation though.
                "<a href=\"https://example.org/search?q=%C3%A4&hl=nl\">" +
                        "https://example.org/search?q=%C3%A4&hl=nl</a>"
        );

        testAutolink(
                "Go to \"http://example.org/\".",

                // FIXME: The quote must not end up as part of the URL.
                "Go to \"<a href=\"http://example.org/\".\">http://example.org/\".</a>"
        );

        testAutolink(
                "Go to <http://example.org/>.",

                // FIXME: The < must be encoded as &lt;.
                // FIXME: THe > must not end up as part of the URL.
                "Go to <<a href=\"http://example.org/>.\">http://example.org/>.</a>"
        );
    }

    private void testAutolink(String input, String expectedOutput) {
        assertThat(HtmlHelper.autolink(input)).isEqualTo(expectedOutput);
    }
}
