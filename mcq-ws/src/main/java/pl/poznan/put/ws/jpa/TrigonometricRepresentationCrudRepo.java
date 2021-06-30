package pl.poznan.put.ws.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.poznan.put.ws.entities.TrigonometricRepresentation;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

public interface TrigonometricRepresentationCrudRepo
    extends CrudRepository<TrigonometricRepresentation, UUID> {
  @Query("SELECT t FROM TrigonometricRepresentation t WHERE t.inputId.id = ?1")
  List<TrigonometricRepresentation> findAllByInputId(@Param("inputId") UUID inputId);
}
