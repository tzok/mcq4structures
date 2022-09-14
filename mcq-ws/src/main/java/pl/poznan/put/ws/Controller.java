package pl.poznan.put.ws;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.schema.*;
import pl.poznan.put.ws.components.Version;

@RestController
@RequestMapping("/api")
public class Controller {

  private final ServicesSupervisor servicesSupervisor;

  @Autowired
  public Controller(ServicesSupervisor servicesSupervisor) {
    this.servicesSupervisor = servicesSupervisor;
  }

  @GetMapping("/version")
  private Version getVersion() {
    return servicesSupervisor.getVersionService().findVersion();
  }

  @PostMapping("/upload")
  private UploadDTO postUpload(@RequestBody @Valid StructureContentDTO structureContentDTO) {
    return servicesSupervisor.getUploadService().handlePostUpload(structureContentDTO);
  }

  @PostMapping("/upload/{pdbId}")
  private UploadDTO postUpload(@RequestParam String pdbId) {
    return servicesSupervisor.getUploadService().handlePostUpload(pdbId, 1);
  }

  @PostMapping("/upload/{pdbId}/{assemblyId}")
  private UploadDTO postUpload(@RequestParam String pdbId, @RequestParam int assemblyId) {
    return servicesSupervisor.getUploadService().handlePostUpload(pdbId, assemblyId);
  }

  @PostMapping("/analyze/{id}")
  private List<TrigonometricRepresentationDTO> postAnalyze(@PathVariable String id) {
    return servicesSupervisor.getAnalyzeService().handleGetAnalyze(id);
  }

  @PostMapping("/compare/target")
  private List<ModelComparisonDTO> postCompareTarget(@RequestBody TargetModelsDTO targetModelsDTO) {
    return servicesSupervisor.getCompareService().handleCompare(targetModelsDTO);
  }

  @PostMapping("/compare/models")
  private List<ModelComparisonDTO> postCompareModels(@RequestBody ModelsDTO modelsDTO) {
    return servicesSupervisor.getCompareService().handleCompare(modelsDTO);
  }
}
