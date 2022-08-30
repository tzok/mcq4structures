package pl.poznan.put.ws.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.poznan.put.ws.entities.StructureContent;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StructureContentCrudRepo extends CrudRepository<StructureContent, UUID> {
  Optional<StructureContent> findByData(String data);
}
