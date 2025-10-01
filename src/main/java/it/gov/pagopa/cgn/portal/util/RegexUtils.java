package it.gov.pagopa.cgn.portal.util;

import java.util.regex.Pattern;

public class RegexUtils {

    private static final Pattern URL_PATTERN = Pattern.compile("^https://(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}(?:/\\S*)?$");

    public static boolean checkRulesForInternetUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }
}
