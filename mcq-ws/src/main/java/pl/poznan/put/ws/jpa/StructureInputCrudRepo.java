package pl.poznan.put.ws.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StructureInputCrudRepo extends CrudRepository<StructureInput, UUID> {
}
