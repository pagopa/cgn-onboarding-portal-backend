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

    private void appendValues(StringBuilder sb, String prefix, List<String> values) {
        if (values != null && !values.isEmpty()) {
            sb.append(prefix).append(String.join(",", values)).append(" ");
        }
    }

    public String getRecipientsSummary() {
        StringBuilder sb = new StringBuilder();

        appendValues(sb, "To:", mailToList);
        appendValues(sb, "Cc:", mailCCList.orElse(null));
        appendValues(sb, "Bcc:", mailBCCList.orElse(null));
        appendValues(sb, "Reply-To:", replyToOpt.map(List::of).orElseGet(List::of));

        return sb.toString().trim();
    }

    @Getter
    @AllArgsConstructor
    public static final class Attachment {
        private String attachmentFilename;
        private ByteArrayResource resource;
    }
}
