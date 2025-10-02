package it.gov.pagopa.cgn.portal.util;

import java.util.regex.Pattern;

public class RegexUtils {

    private RegexUtils () {}

    private static final Pattern URL_PATTERN = Pattern.compile("^(?i)https://(?:(?>[a-z0-9]+(?:-[a-z0-9]+)*)\\.)+[a-z]{2,63}(?:/\\S*)?$");

    public static boolean checkRulesForHttpsUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
}
