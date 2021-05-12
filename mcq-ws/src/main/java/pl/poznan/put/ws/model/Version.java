package pl.poznan.put.ws.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@JsonPropertyOrder({
        "version"
})
@Component
public class Version {
    @Value("${git.commit.id.describe-short}")
    @JsonProperty("version")
    private String version;

    public Version(String version) {
        this.version = version;
    }

    public Version(){

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
