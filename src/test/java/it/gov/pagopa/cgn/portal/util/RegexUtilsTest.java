package it.gov.pagopa.cgn.portal.util;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class RegexUtilsTest {


    @Test
    @Parameters({"https://www.prova.it", "https://prova.it","https://prova.com",
                 "https://www.prova.it/help", "https://prova.it/help","https://prova.com/help",
                 "https://www.prova.it/help.html", "https://prova.it/help.html","https://prova.com/help.html",
                 "https://pro-va.it","https://www.prova.it/he-lp.html","https://sub.domain.prova.com","https://prova.it/a_b",
                 "https://prova.it/a%20b?x=1"})
    public void regexWebsiteUrl_ok (String websiteUrl) {
        Assertions.assertTrue(RegexUtils.checkRulesForInternetUrl(websiteUrl));
    }

    @Test
    @Parameters({
                 "http://prova.it", "https:://prova.it","https-//prova.it","https://-prova.it","https://prova-.it",
                 "https://pr_ova.it","https://prova","https://prova.c0m","https://.it", "https://prova.it/a b",
                 "http:://prova.com/help","https://prova.it/ ciao/mondo","https://prova.it/ciao\tmondo"})
    public void regexWebsiteUrl_ko (String websiteUrl) {
        Assertions.assertFalse(RegexUtils.checkRulesForInternetUrl(websiteUrl));
    }

    @Test
    @Ignore
    @Parameters({"https://www.google.com/search?q=regex+url+validation",
                 "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                 "https://en.wikipedia.org/wiki/Italy",
                 "https://en.wikipedia.org/wiki/Software_testing#Equivalence_partitioning",
                 "https://github.com/torvalds/linux",
                 "https://github.com/facebook/react/tree/main/docs",
                 "https://stackoverflow.com/questions/11828270/how-to-exit-the-vim-editor",
                 "https://stackoverflow.com/questions/65938211/what-is-the-difference-between-map-and-flatmap",
                 "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map",
                 "https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/404#examples",
                 "https://www.bbc.co.uk/news/world/europe-67000000",
                 "https://edition.cnn.com/2025/09/01/world/sample-article-index.html",
                 "https://www.nytimes.com/2025/09/01/technology/sample-article.html",
                 "https://www.theguardian.com/world/2025/sep/01/sample-article",
                 "https://www.reddit.com/r/programming/comments/abcdef/sample_thread_title/",
                 "https://www.reddit.com/r/linux/comments/xyz123/why_kernel_is_cool/",
                 "https://news.ycombinator.com/item?id=34567890",
                 "https://medium.com/@someauthor/how-to-structure-a-project-1234567890ab",
                 "https://www.amazon.com/dp/B08N5WRWNW?th=1&psc=1",
                 "https://www.amazon.co.uk/dp/B07PGL2ZSL/ref=sr_1_1?keywords=ssd&qid=1690000000",
                 "https://www.imdb.com/title/tt0110912/?ref_=nv_sr_srsg_0",
                 "https://open.spotify.com/track/7GhIk7Il098yCjg4BQjzvb?si=abcdef123456",
                 "https://en.wikipedia.org/wiki/Regular_expression#Examples",
                 "https://github.com/microsoft/vscode/issues/142345",
                 "https://github.com/nodejs/node/blob/main/doc/api/http.md#class-httpserver",
                 "https://docs.python.org/3/library/re.html#regular-expression-syntax",
                 "https://www.microsoft.com/it-it/software-download/windows10",
                 "https://developer.apple.com/documentation/swift/swift_standard_library",
                 "https://www.linkedin.com/in/someone-profile/",
                 "https://twitter.com/jack/status/20",
                 "https://x.com/elonmusk/status/1234567890123456789",
                 "https://www.instagram.com/p/CabcdefGhI/",
                 "https://api.github.com/repos/torvalds/linux/issues?state=open&per_page=5",
                 "https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc",
                 "https://spring.io/guides/gs/spring-boot/",
                 "https://www.w3.org/TR/html52/semantics-scripting.html#the-script-element",
                 "https://en.wikipedia.org/wiki/Uniform_Resource_Identifier#Examples",
                 "https://www.gnu.org/software/gawk/manual/html_node/Regular-Expressions.html",
                 "https://stackoverflow.com/search?q=java+regex+url+validation",
                 "https://github.com/expressjs/express/blob/master/Readme.md#routing",
                 "https://www.python.org/dev/peps/pep-0008/#naming-conventions",
                 "https://gitlab.com/explore/projects/trending?page=1&sort=stars",
                 "https://www.npmjs.com/package/validator/v/13.7.0",
                 "https://cdnjs.com/libraries/jquery",
                 "https://developer.chrome.com/docs/extensions/mv3/getstarted/",
                 "https://www.mozilla.org/en-US/privacy/websites/",
                 "https://support.google.com/websearch/answer/134479?hl=it",
                 "https://en.wikipedia.org/wiki/Top-level_domain#Country_code_top-level_domains",
                 "https://www.linkedin.com/jobs/search/?keywords=java%20spring&location=Italy",
                 "https://www.booking.com/searchresults.html?ss=Rome&checkin=2025-10-01&checkout=2025-10-03",
                 "https://www.airbnb.com/s/Rome--Italy/homes?adults=2&checkin=2025-10-01&checkout=2025-10-03",
                 "https://www.euronews.com/2025/09/01/sample-europe-news-article",
                 "https://scholar.google.com/scholar?q=regular+expressions+url+validation",
                 "https://doi.org/10.1145/1234567.8901234",
                 "https://arxiv.org/abs/2106.01345v2",
                 "https://pypi.org/project/regex/2025.8.1/",
                 "https://rubygems.org/gems/addressable/versions/2.8.0",
                 "https://www.php.net/manual/en/function.preg-match.php#example-3834",
                 "https://www.who.int/news/item/2025-09-01-sample-health-news",
                 "https://www.un.org/en/sections/issues-depth/climate-change/",
                 "https://developer.android.com/training/basics/firstapp?authuser=0#add-button",
                 "https://flutter.dev/docs/get-started/codelab",
                 "https://docs.gitlab.com/ee/ci/pipelines.html#example-pipeline",
                 "https://cloud.google.com/functions/docs/tutorials/http?hl=en",
                 "https://aws.amazon.com/lambda/getting-started/?nc1=h_ls",
                 "https://azure.microsoft.com/en-us/services/functions/overview/",
                 "https://www.howtogeek.com/usage/cool-tricks/?page=2#fragment-example",
                 "https://developer.twitter.com/en/docs/twitter-api/tweets/search/api-reference/get-tweets-search-recent",
                 "https://developers.google.com/maps/documentation/javascript/examples/map-simple",
                 "https://www.tutorialspoint.com/javascript/javascript_tutorial.pdf#page=45",
                 "https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=EC-123456789",
                 "https://www.dropbox.com/s/abcd1234/example.pdf?dl=0",
                 "https://www.slack.com/intl/en-it/help/articles/360035692513-Manage-your-notifications#section--do-not-disturb",
                 "https://www.twitch.tv/directory/game/Just%20Chatting?sort=views",
                 "https://accounts.google.com/signin/v2/identifier?service=mail&passive=true&continue=https%3A%2F%2Fmail.google.com%2Fmail%2F",
                 "https://mail.google.com/mail/u/0/#inbox/FMfcgzGkXbXxQzabc?project=123",
                 "https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#matches-java.lang.String-java.lang.CharSequence-",
                 "https://www.gnu.org/licenses/gpl-3.0.en.html#section-12",
                 "https://www.stackoverflow.com/questions/tagged/regex?page=2&sort=newest",
                 "https://gist.github.com/username/abcdef1234567890#file-sample-md",
                 "https://www.wikipedia.org/#searchInput",
                 "https://www.cnn.com/2025/09/01/tech/sample-article/index.html?xtor=RSS-182",
                 "https://www.bbc.com/news/technology-66700000#top-stories",
                 "https://www.imdb.com/name/nm0000206/?ref_=fn_al_nm_1",
                 "https://www.gitlab.com/gitlab-org/gitlab/-/issues/12345?locale=en",
                 "https://play.google.com/store/apps/details?id=com.example.app&hl=en&gl=US",
                 "https://apps.apple.com/us/app/example-app/id1234567890?mt=8",
                 "https://www.flickr.com/photos/exampleuser/51234567890/in/dateposted/",
                 "https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_alert2",
                 "https://www.npmjs.com/package/express?activeTab=readme#examples",
                 "https://www.adobe.com/creativecloud/catalog/desktop.html?promoid=example#fragment",
                 "https://www.python.org/downloads/release/python-3108/",
                 "https://www.rust-lang.org/learn#community?utm_source=example",
                 "https://stackoverflow.com/a/11227809/1234567",
                 "https://github.com/psf/requests/issues/5908#issuecomment-123456789",
                 "https://opensource.org/licenses/MIT",
                 "https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#extension_map?utm_source=example",
                 "https://www.zdnet.com/article/sample-article-title/?ftag=CAD-03-10aaa0f&hpt=ms_bn_c2",
                 "https://www.techcrunch.com/2025/09/01/sample-startup-raises-10m/?guccounter=1",
                 "https://www.wikipedia.org/wiki/Special:Random#somefragment"})
    public void regexWebsiteRealUrl_ok (String websiteUrl) {
        Assertions.assertTrue(RegexUtils.checkRulesForInternetUrl(websiteUrl));
    }
}
