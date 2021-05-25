package pl.poznan.put.ws.tests.upload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.ws.jpa.StructureInputCrudRepo;
import pl.poznan.put.ws.services.UploadService;
import pl.poznan.put.ws.tests.tools.DataBaseTestOperationsAssistant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class LogicTests {

  private UploadService uploadService;

  private StructureInputDTO exampleStructureInputDTO;

  private DataBaseTestOperationsAssistant dataBaseTestOperationsAssistant;

  @Autowired
  public LogicTests(
      UploadService uploadService, StructureInputCrudRepo structureInputCrudRepo, DataBaseTestOperationsAssistant dataBaseTestOperationsAssistant) {
    this.uploadService = uploadService;
    this.dataBaseTestOperationsAssistant = dataBaseTestOperationsAssistant;
  }

  @BeforeEach
  void setupOneObj() {
    dataBaseTestOperationsAssistant.getStructureInputCrudRepo().deleteAll();
    String exampleUUIDString = "8c549628-ff2f-4d56-8150-0377d3995915";
    exampleStructureInputDTO = new StructureInputDTO(exampleUUIDString, "12d2", 1, "asdsf3efd12sd");
    uploadService.handlePostUpload(exampleStructureInputDTO);
  }

  @Test
  void notSavingWithTheSameFields() {
    uploadService.handlePostUpload(exampleStructureInputDTO);
    int expectedNumberOfObjectsInDB = 1;
    assertThat(dataBaseTestOperationsAssistant.countRows()).isEqualTo(expectedNumberOfObjectsInDB);
  }

  @Test
  void returningFilledObjectWhichIsAlreadyInDB() {
    StructureInputDTO withChangedId =
        new StructureInputDTO("8c549628-1111-4d56-8150-0377d3995915", "12d2", 1, "asdsf3efd12sd");
    assertThat(uploadService.handlePostUpload(withChangedId)).isEqualTo(exampleStructureInputDTO);

    StructureInputDTO withChangedPdbId =
        new StructureInputDTO("8c549628-1111-4d56-8150-0377d3995915", "1111", 1, "asdsf3efd12sd");
    assertThat(uploadService.handlePostUpload(withChangedPdbId))
        .isEqualTo(exampleStructureInputDTO);

    StructureInputDTO withChangedAssemblyId =
        new StructureInputDTO(
            "8c549628-1111-4d56-8150-0377d3995915", "1111", 1111, "asdsf3efd12sd");
    assertThat(uploadService.handlePostUpload(withChangedAssemblyId))
        .isEqualTo(exampleStructureInputDTO);

    StructureInputDTO withChangedStructureContent =
        new StructureInputDTO("8c549628-1111-4d56-8150-0377d3995915", "1111", 1111, "1111");
    assertThat(uploadService.handlePostUpload(withChangedStructureContent))
        .isEqualTo(withChangedStructureContent);
  }

  @Test
  void creatingNewObjectInDB() {
    StructureInputDTO newObj =
        new StructureInputDTO("8c549628-1111-4d56-8150-0377d3995915", "1111", 1111, "1111");
    uploadService.handlePostUpload(newObj);

    StructureInputDTO objResult = uploadService.handlePostUpload(newObj);
    assertThat(objResult).isEqualTo(newObj);
    assertThat(dataBaseTestOperationsAssistant.countRows()).isEqualTo(2);
  }
}
