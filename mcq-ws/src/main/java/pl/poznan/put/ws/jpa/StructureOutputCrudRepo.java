package pl.poznan.put.ws.jpa;


import org.springframework.data.repository.CrudRepository;
import pl.poznan.put.ws.entities.TrigonometricRepresentation;

import java.util.Optional;
import java.util.UUID;

public interface StructureOutputCrudRepo extends CrudRepository<TrigonometricRepresentation, UUID> {
    Optional<TrigonometricRepresentation> findByInputId(UUID inputId);
}
