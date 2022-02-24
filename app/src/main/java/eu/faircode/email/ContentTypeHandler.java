package eu.faircode.email;

import javax.mail.internet.MimePart;

// https://docs.oracle.com/javaee/6/api/javax/mail/internet/package-summary.html
public class ContentTypeHandler {
    public static String cleanContentType(MimePart mp, String contentType) {
        return contentType;
    }
}
