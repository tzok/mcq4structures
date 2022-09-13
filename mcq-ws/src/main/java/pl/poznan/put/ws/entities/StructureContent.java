package pl.poznan.put.ws.entities;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.*;
import org.springframework.data.annotation.CreatedDate;

@Entity
public class StructureContent {

  public StructureContent(UUID id, Instant createdAt, String data) {
    this.id = id;
    this.createdAt = createdAt;
    this.data = data;
  }

  public StructureContent() {}

  @Id private UUID id;

  @CreatedDate private Instant createdAt;

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

  public List<TrigonometricRepresentation> getTrigonometricRepresentations() {
    return trigonometricRepresentations;
  }

  public void setTrigonometricRepresentations(
      List<TrigonometricRepresentation> trigonometricRepresentations) {
    this.trigonometricRepresentations = trigonometricRepresentations;
  }

  @Override
  public String toString() {
    return "StructureContent{"
        + "id="
        + id
        + ", createdAt="
        + createdAt
        + ", data='"
        + data
        + '\''
        + ", trigonometricRepresentations="
        + trigonometricRepresentations
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StructureContent that = (StructureContent) o;
    return Objects.equals(id, that.id)
        && Objects.equals(createdAt, that.createdAt)
        && Objects.equals(data, that.data)
        && Objects.equals(trigonometricRepresentations, that.trigonometricRepresentations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, data, trigonometricRepresentations);
  }
}
