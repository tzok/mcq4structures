package pl.poznan.put.ws;

import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.schema.StructureContentDTO;
import pl.poznan.put.schema.TrigonometricRepresentationDTO;
import pl.poznan.put.schema.UploadDTO;
import pl.poznan.put.ws.componentes.Version;
import java.util.List;

import javax.validation.Valid;

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
  private List<TrigonometricRepresentationDTO> getAnalyze(@PathVariable String id) {
    return servicesSupervisor.getAnalyzeService().handleGetAnalyze(id);
  }
}
