package pl.poznan.put.ws.jpa;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.CreatedDate;

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

  public StructureInput(UUID id, Instant createdAt, String pdbId, int assemblyId, String structureContent) {
    this.id = id;
    this.createdAt = createdAt;
    this.pdbId = pdbId;
    this.assemblyId = assemblyId;
    this.structureContent = structureContent;
  }

 public StructureInput(){

 }

  @Id
  @GeneratedValue
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
  public Optional<String> getPdbId() {
    return Optional.ofNullable(pdbId);
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
  public Optional<String> getStructureContent() {
    return Optional.ofNullable(structureContent);
  }

  @JsonProperty("structureContent")
  public void setStructureContent(String structureContent) {
    this.structureContent = structureContent;
  }

  @Override
  public String toString() {
    return "StructureInput{" +
            "id=" + id +
            ", createdAt=" + createdAt +
            ", pdbId='" + pdbId + '\'' +
            ", assemblyId=" + assemblyId +
            ", structureContent='" + structureContent + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StructureInput that = (StructureInput) o;
    return assemblyId == that.assemblyId && Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt) && Objects.equals(pdbId, that.pdbId) && Objects.equals(structureContent, that.structureContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, pdbId, assemblyId, structureContent);
  }
}
