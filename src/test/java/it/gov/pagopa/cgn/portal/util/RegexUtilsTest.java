package it.gov.pagopa.cgn.portal.util;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class RegexUtilsTest {

    @Test
    @Parameters({"https://www.prova.it", "https://prova.it"})
    public void regexWebsiteUrl_ok (String websiteUrl) {
        Assertions.assertTrue(RegexUtils.checkRulesForInternetUrl(websiteUrl));
    }

    @Test
    @Parameters({"http://www.prova.it", "http://prova.it", "http://www.prova", "http://prova", "http:://www.prova.it",
                 "https://www.prova", "https://prova", "https:://www.prova.it"})
    public void regexWebsiteUrl_ko (String websiteUrl) {
        Assertions.assertFalse(RegexUtils.checkRulesForInternetUrl(websiteUrl));
    }
}
