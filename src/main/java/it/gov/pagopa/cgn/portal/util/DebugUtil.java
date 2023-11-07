package it.gov.pagopa.cgn.portal.util;

import it.gov.pagopa.cgn.portal.email.EmailParams;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class DebugUtil {

    public static EmailParams createEmailParams(String to, String subject, String body, String mailFRom) {
        return EmailParams.builder()
                .mailFrom(mailFRom)
                .logoName("")
                .logo(null)
                .mailToList(Collections.singletonList(to))
                .mailCCList(Optional.empty())
                .replyToOpt(Optional.empty())
                .subject(subject)
                .body(body)
                .failureMessage("")
                .build();

    }

}
