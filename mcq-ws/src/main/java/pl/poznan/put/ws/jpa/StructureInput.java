package pl.poznan.put.ws.jpa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.CreatedDate;
import pl.poznan.put.schema.StructureInputDTO;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "createdAt", "pdbId", "assemblyId", "structureContent"})
@Generated("jsonschema2pojo")
public class StructureInput {

  public StructureInput(
      UUID id, Instant createdAt, String pdbId, int assemblyId, String structureContent) {
    this.id = id;
    this.createdAt = createdAt;
    this.pdbId = pdbId;
    this.assemblyId = assemblyId;
    this.structureContent = structureContent;
  }

  public StructureInput() {}

  @Id
  @JsonProperty("id")
  private UUID id;

  @CreatedDate
  @JsonProperty("createdAt")
  private Instant createdAt;

  @JsonProperty("pdbId")
  @Pattern(regexp = "^$|\\w{4}")
  private String pdbId = "";

  @JsonProperty("assemblyId")
  private int assemblyId = 1;

  @JsonProperty("structureContent")
  private String structureContent = "";

  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(UUID id) {
    this.id = id;
  }

  @JsonProperty("createdAt")
  public Instant getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  @JsonProperty("pdbId")
  public String getPdbId() {
    return pdbId;
  }

  @JsonProperty("pdbId")
  public void setPdbId(String pdbId) {
    this.pdbId = pdbId;
  }

  @JsonProperty("assemblyId")
  public int getAssemblyId() {
    return assemblyId;
  }

  @JsonProperty("assemblyId")
  public void setAssemblyId(int assemblyId) {
    this.assemblyId = assemblyId;
  }

  @JsonProperty("structureContent")
  public String getStructureContent() {
    return structureContent;
  }

  @JsonProperty("structureContent")
  public void setStructureContent(String structureContent) {
    this.structureContent = structureContent;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(StructureInputDTO.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("id");
    sb.append('=');
    sb.append(((this.id == null) ? "<null>" : this.id));
    sb.append(',');
    sb.append("pdbId");
    sb.append('=');
    sb.append(((this.pdbId == null) ? "<null>" : this.pdbId));
    sb.append(',');
    sb.append("assemblyId");
    sb.append('=');
    sb.append(this.assemblyId);
    sb.append(',');
    sb.append("structureContent");
    sb.append('=');
    sb.append(((this.structureContent == null) ? "<null>" : this.structureContent));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.pdbId == null) ? 0 : this.pdbId.hashCode()));
    result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
    result =
        ((result * 31) + ((this.structureContent == null) ? 0 : this.structureContent.hashCode()));
    result = ((result * 31) + this.assemblyId);
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof StructureInput) == false) {
      return false;
    }
    StructureInput rhs = ((StructureInput) other);
    return (((((this.pdbId == rhs.pdbId) || ((this.pdbId != null) && this.pdbId.equals(rhs.pdbId)))
                && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
            && ((this.structureContent == rhs.structureContent)
                || ((this.structureContent != null)
                    && this.structureContent.equals(rhs.structureContent))))
        && (this.assemblyId == rhs.assemblyId));
  }
}
