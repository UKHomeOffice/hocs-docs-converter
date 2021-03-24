package uk.gov.digital.ho.hocs.document;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;

import java.util.List;

@Getter
@Setter
public class MsgContents {

    private String fromEmail = StringUtils.EMPTY;
    private String fromName = StringUtils.EMPTY;
    private String subject = StringUtils.EMPTY;
    private String bodyHTML = StringUtils.EMPTY;
    private String bodyText = StringUtils.EMPTY;
    private String toEmail = StringUtils.EMPTY;
    private String toName = StringUtils.EMPTY;
    private String sentOn = StringUtils.EMPTY;

    private List<OutlookAttachment> attachments = null;
}
