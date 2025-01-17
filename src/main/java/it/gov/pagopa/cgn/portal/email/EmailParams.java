package it.gov.pagopa.cgn.portal.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
@ToString
public class EmailParams {

    private final String mailFrom;
    private final List<String> mailToList;
    private final Optional<List<String>> mailCCList;
    private final Optional<List<String>> mailBCCList;
    private final Optional<String> replyToOpt;
    private final String subject;
    private final String body;
    private final Resource logo;
    private final String logoName;
    private final String failureMessage;
    private final Optional<List<Attachment>> attachments;

    public String toLightString() {
        return "EmailParams{" + "mailFrom='" + mailFrom + '\'' + ", mailToList=" + mailToList + ", mailCCList=" +
               mailCCList + ", mailBCCList=" + mailBCCList + ", subject='" + subject + '\'' + '}';
    }



    @Getter
    @AllArgsConstructor
    public static final class Attachment {
        private String attachmentFilename;
        private ByteArrayResource resource;
    }
}
