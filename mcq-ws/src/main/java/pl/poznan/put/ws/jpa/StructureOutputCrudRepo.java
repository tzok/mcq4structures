package pl.poznan.put.ws.jpa;


import org.springframework.data.repository.CrudRepository;
import pl.poznan.put.ws.entities.StructureOutput;

import java.util.Optional;
import java.util.UUID;

public interface StructureOutputCrudRepo extends CrudRepository<StructureOutput, UUID> {
    Optional<StructureOutput> findByInputId(UUID inputId);
}
