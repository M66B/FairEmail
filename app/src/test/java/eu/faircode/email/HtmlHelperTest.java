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

                // This string had been wrongly interpreted as a complete URL up to February 2019.
                "one hhhhh|spt://example.org three"
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

                "Go to \"<a href=\"http://example.org/\">http://example.org/</a>\"."
        );

        testAutolink(
                "Go to <http://example.org/>.",

                // FIXME: The < must be encoded as &lt;.
                "Go to <<a href=\"http://example.org/\">http://example.org/</a>>."
        );

        testAutolink(
                "http://example.org/ and http://example.org/subdir/",

                // FIXME: Each URL must be linked to its exact address, not just to a prefix.
                "" +
                        "<a href=\"http://example.org/\">http://example.org/</a> and " +
                        "<a href=\"http://example.org/\">http://example.org/</a>subdir/"
        );

        testAutolink(
                "http://example.org/ and http://example.org/ and http://example.org/",

                // FIXME: Even when the same URL is mentioned multiple times,
                // each of the URLs must only appear a single time.
                "" +
                        "<a href=\"" +
                        "<a href=\"" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "\">" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "</a>" +
                        "\">" +
                        "<a href=\"" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "\">" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "</a>" +
                        "</a>" +
                        " and " +
                        "<a href=\"" +
                        "<a href=\"" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "\">" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "</a>" +
                        "\">" +
                        "<a href=\"" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "\">" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "</a>" +
                        "</a>" +
                        " and " +
                        "<a href=\"" +
                        "<a href=\"" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "\">" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "</a>" +
                        "\">" +
                        "<a href=\"" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "\">" +
                        "<a href=\"http://example.org/\">http://example.org/</a>" +
                        "</a>" +
                        "</a>"
        );
    }

    private void testAutolink(String input, String expectedOutput) {
        assertThat(HtmlHelper.autolink(input)).isEqualTo(expectedOutput);
    }
}
