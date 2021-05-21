package pl.poznan.put.ws.tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.ws.services.UploadService;

import java.util.UUID;

@SpringBootTest
public class UploadServiceTests {

  @Autowired private UploadService uploadService;

  @Test
  void generalTest() {
    String exampleUUIDString = "8c549628-7fb2-4d56-8150-0377d3995915";
    StructureInputDTO exampleStructureInputDTO =
        new StructureInputDTO(exampleUUIDString, "12d2", 1, "asdsf3efd12sd");
    uploadService.handlePostUpload(exampleStructureInputDTO);
  }
}
