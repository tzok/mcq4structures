package pl.poznan.put.ws.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.stereotype.Component;

@JsonPropertyOrder({
        "structureContent"
})
@Component
public class Torsion {

    @JsonProperty("structureContent")
    private String structureContent;

    public Torsion(String structureContent) {
        this.structureContent = structureContent;
    }

    public Torsion() {

    }

    public String getStructureContent() {
        return structureContent;
    }

    public void setStructureContent(String structureContent) {
        this.structureContent = structureContent;
    }
}
