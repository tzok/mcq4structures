package pl.poznan.put.ws.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import javax.persistence.*;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "createdAt", "pdbId", "assemblyId", "models"})
@Entity
@Generated("jsonschema2pojo")
public class StructureOutput {

  @JsonProperty("id")
  @Id
  private UUID id;

  @JsonProperty("createdAt")
  private Instant createdAt;

  @OneToOne(targetEntity = StructureInput.class)
  @JsonProperty("inputId")
  private UUID inputId;

  @JsonProperty("pdbId")
  private String pdbId;

  @JsonProperty("assemblyId")
  private int assemblyId;

  @JsonProperty("models")
  @Valid
  @OneToMany(targetEntity = Model.class, mappedBy = "number")
  private List<Model> models = new ArrayList<Model>();

  /** No args constructor for use in serialization */
  public StructureOutput() {}

  public StructureOutput(
      UUID id, Instant createdAt, UUID inputId, String pdbId, int assemblyId, List<Model> models) {
    this.id = id;
    this.createdAt = createdAt;
    this.inputId = inputId;
    this.pdbId = pdbId;
    this.assemblyId = assemblyId;
    this.models = models;
  }

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

  @JsonProperty("inputId")
  public UUID getInputId() {
    return inputId;
  }

  @JsonProperty("inputId")
  public void setInputId(UUID inputId) {
    this.inputId = inputId;
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

  @JsonProperty("models")
  public List<Model> getModels() {
    return models;
  }

  @JsonProperty("models")
  public void setModels(List<Model> models) {
    this.models = models;
  }

  @Override
  public String toString() {
    return "StructureOutput{"
        + "id="
        + id
        + ", createdAt="
        + createdAt
        + ", pdbId='"
        + pdbId
        + '\''
        + ", assemblyId="
        + assemblyId
        + ", models="
        + models
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StructureOutput that = (StructureOutput) o;
    return assemblyId == that.assemblyId
        && id.equals(that.id)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(pdbId, that.pdbId)
        && Objects.equals(models, that.models);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, pdbId, assemblyId, models);
  }
}
