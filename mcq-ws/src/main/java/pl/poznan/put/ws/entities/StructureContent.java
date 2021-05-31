package pl.poznan.put.ws.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.CreatedDate;

import javax.annotation.Generated;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.List;

@Entity
public class StructureContent {

  public StructureContent(
      UUID id, Instant createdAt, String data) {
    this.id = id;
    this.createdAt = createdAt;
    this.data = data;
  }

  public StructureContent() {}

  @Id
  private UUID id;

  @CreatedDate
  private Instant createdAt;

  private String data = "";

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @OneToMany(mappedBy = "inputId", fetch = FetchType.LAZY)
  private List<TrigonometricRepresentation> trigonometricRepresentations;


  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getData() {
    return data;
  }

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
