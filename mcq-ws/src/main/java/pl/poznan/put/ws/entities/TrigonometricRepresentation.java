package pl.poznan.put.ws.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import pl.poznan.put.schema.ChainDTO;

import javax.annotation.Generated;
import javax.persistence.*;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "modelNumber", "chains"})
@Generated("jsonschema2pojo")
public class TrigonometricRepresentation {

  @JsonProperty("id")
  @Id
  private UUID id;

  @JsonProperty("createdAt")
  private Instant createdAt;

  @ManyToOne(targetEntity = StructureContent.class)
  @JoinColumn(name = "StructureContent_id")
  @JsonProperty("inputId")
  private UUID inputId;

  @JsonProperty("pdbId")
  private String pdbId;

  @JsonProperty("assemblyId")
  private int assemblyId;

  @JsonProperty("modelNumber")
  private double modelNumber;

  @OneToMany(targetEntity = Chain.class, mappedBy = "name")
  @JsonProperty("chains")
  @Valid
  private List<Chain> chains = new ArrayList<Chain>();

  /** No args constructor for use in serialization */
  public TrigonometricRepresentation() {}

  public TrigonometricRepresentation(
      UUID id,
      Instant createdAt,
      UUID inputId,
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

  @JsonProperty("modelNumber")
  public double getModelNumber() {
    return modelNumber;
  }

  @JsonProperty("modelNumber")
  public void setModelNumber(double modelNumber) {
    this.modelNumber = modelNumber;
  }

  @JsonProperty("chains")
  public List<Chain> getChains() {
    return chains;
  }

  @JsonProperty("chains")
  public void setChains(List<Chain> chains) {
    this.chains = chains;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(TrigonometricRepresentation.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("id");
    sb.append('=');
    sb.append(((this.id == null) ? "<null>" : this.id));
    sb.append(',');
    sb.append("modelNumber");
    sb.append('=');
    sb.append(this.modelNumber);
    sb.append(',');
    sb.append("chains");
    sb.append('=');
    sb.append(((this.chains == null) ? "<null>" : this.chains));
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
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.modelNumber)
                    ^ (Double.doubleToLongBits(this.modelNumber) >>> 32))));
    result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
    result = ((result * 31) + ((this.chains == null) ? 0 : this.chains.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof TrigonometricRepresentation) == false) {
      return false;
    }
    TrigonometricRepresentation rhs = ((TrigonometricRepresentation) other);
    return (((Double.doubleToLongBits(this.modelNumber) == Double.doubleToLongBits(rhs.modelNumber))
            && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
        && ((this.chains == rhs.chains)
            || ((this.chains != null) && this.chains.equals(rhs.chains))));
  }
}
