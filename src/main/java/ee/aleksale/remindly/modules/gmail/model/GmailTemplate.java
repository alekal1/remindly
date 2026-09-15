package ee.aleksale.remindly.modules.gmail.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class GmailTemplate {

  private String id;
  private String sender;
  private String ntfyTopic;
  private String reminderId;
  private String subjectContains;
  private Selector selector;

  public String getGmailQuery() {
    return StringUtils.isBlank(subjectContains)
            ? String.format("from:%s", sender)
            : String.format("from:%s subject:(%s)", sender, subjectContains);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Selector {
    private TableConfig table;
    private LinkConfig link;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class TableConfig {
    private String anchor;
    private Map<String, String> columns;
    private String dateFormat;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class LinkConfig {
    private String anchorTextPattern;
    private String hrefPattern;
  }
}

