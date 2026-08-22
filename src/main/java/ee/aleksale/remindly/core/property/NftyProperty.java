package ee.aleksale.remindly.core.property;

import ee.aleksale.remindly.core.model.type.NtfyTopic;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.nfty")
public class NftyProperty {

    @Getter
    @Setter
    private Map<String, String> topics;

    public String getTopic(NtfyTopic topic) {
        return topics.get(topic.getConfigKey());
    }

}
