package pl.poznan.put.ws.tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.poznan.put.ws.jpa.StructureInput;
import pl.poznan.put.ws.jpa.StructureInputCrudRepo;

import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class DBTests {
  @Autowired private StructureInputCrudRepo structureInputCrudRepo;

  @Test
  void getTest() {
    StructureInput toSaveStructure =
        new StructureInput(UUID.randomUUID(), Instant.now(), "abcd", 1, "test");
    structureInputCrudRepo.save(toSaveStructure);
    Iterable<StructureInput> structureInputs = structureInputCrudRepo.findAll();
    for (Iterator<StructureInput> it = structureInputs.iterator(); it.hasNext(); ) {
      StructureInput structureInput = it.next();
      assertThat(structureInput.getPdbId()).isEqualTo(toSaveStructure.getPdbId());
    }
  }

  @Test
  void customMethodsTest() {
    StructureInput toSaveStructure =
        new StructureInput(UUID.randomUUID(), Instant.now(), "abcd", 1, "test");
    structureInputCrudRepo.save(toSaveStructure);
    StructureInput result;

    result = structureInputCrudRepo.findByPdbId("abcd").get();
    assertThat(result).isEqualTo(toSaveStructure);
  }
}
