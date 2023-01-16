package pl.poznan.put.ws.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.Valid;

@Entity
public class TrigonometricRepresentation {

  @Id private UUID id;

  private Instant createdAt;

  @ManyToOne
  @JoinColumn(name = "input_id")
  private StructureContent inputId;

  private String pdbId;

  private int assemblyId;

  private double modelNumber;

  @OneToMany(targetEntity = Chain.class, mappedBy = "name")
  @Valid
  private List<Chain> chains = new ArrayList<Chain>();

  public TrigonometricRepresentation() {}

  public TrigonometricRepresentation(
      UUID id,
      Instant createdAt,
      StructureContent inputId,
      String pdbId,
      int assemblyId,
      double modelNumber,
      List<Chain> chains) {
    this.id = id;
    this.createdAt = createdAt;
    this.inputId = inputId;
    this.pdbId = pdbId;
    this.assemblyId = assemblyId;
    this.modelNumber = modelNumber;
    this.chains = chains;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public StructureContent getInputId() {
    return inputId;
  }

  public void setInputId(StructureContent inputId) {
    this.inputId = inputId;
  }

  public String getPdbId() {
    return pdbId;
  }

  public void setPdbId(String pdbId) {
    this.pdbId = pdbId;
  }

  public int getAssemblyId() {
    return assemblyId;
  }

  public void setAssemblyId(int assemblyId) {
    this.assemblyId = assemblyId;
  }

  public double getModelNumber() {
    return modelNumber;
  }

  public void setModelNumber(double modelNumber) {
    this.modelNumber = modelNumber;
  }

  public List<Chain> getChains() {
    return chains;
  }

  public void setChains(List<Chain> chains) {
    this.chains = chains;
  }

  @Override
  public String toString() {
    return "TrigonometricRepresentation{"
        + "id="
        + id
        + ", createdAt="
        + createdAt
        + ", inputId="
        + inputId
        + ", pdbId='"
        + pdbId
        + '\''
        + ", assemblyId="
        + assemblyId
        + ", modelNumber="
        + modelNumber
        + ", chains="
        + chains
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TrigonometricRepresentation that = (TrigonometricRepresentation) o;
    return assemblyId == that.assemblyId
        && Double.compare(that.modelNumber, modelNumber) == 0
        && Objects.equals(id, that.id)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(inputId, that.inputId)
        && Objects.equals(pdbId, that.pdbId)
        && Objects.equals(chains, that.chains);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, inputId, pdbId, assemblyId, modelNumber, chains);
  }
}
