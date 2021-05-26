package pl.poznan.put.ws.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.CreatedDate;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "createdAt", "pdbId", "assemblyId", "data"})
@Generated("jsonschema2pojo")
public class StructureContent {

  public StructureContent(
      UUID id, Instant createdAt, String data) {
    this.id = id;
    this.createdAt = createdAt;
    this.data = data;
  }

  public StructureContent() {}

  @Id
  @JsonProperty("id")
  private UUID id;

  @CreatedDate
  @JsonProperty("createdAt")
  private Instant createdAt;

  @JsonProperty("data")
  private String data = "";

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

  @JsonProperty("data")
  public String getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "StructureContent{" +
            "id=" + id +
            ", createdAt=" + createdAt +
            ", data='" + data + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StructureContent that = (StructureContent) o;
    return Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt) &&  Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, data);
  }
}
