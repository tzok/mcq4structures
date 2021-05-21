package pl.poznan.put.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.ws.model.Torsion;
import pl.poznan.put.ws.model.Version;
import pl.poznan.put.ws.services.ModelService;
import pl.poznan.put.ws.services.UploadService;

@RestController
@RequestMapping("/api")
public class Controller {

  private ModelService modelService;

  private UploadService uploadService;

  @Autowired
  public Controller(ModelService modelService, UploadService uploadService) {
    this.modelService = modelService;
    this.uploadService = uploadService;
  }

  @GetMapping("/version")
  private Version getVersion() {
    return modelService.findVersion();
  }

  @PostMapping("/torsion")
  private ResponseEntity<?> postTorsion(@RequestBody Torsion torsion) {
    modelService.addTorsion(torsion);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/torsion/{pdbId}")
  private Torsion getTorsion(@PathVariable String pdbId) {
    return modelService.findTorsion(pdbId, 1);
  }

  @GetMapping("/torsion/{pdbId}/{assemblyId}")
  private Torsion getTorsion(@PathVariable String pdbId, @PathVariable Integer assemblyId) {
    return modelService.findTorsion(pdbId, assemblyId);
  }

  @PostMapping("/upload")
  private StructureInputDTO postUpload(@RequestBody StructureInputDTO structureInputDTO) {
    return uploadService.handlePostUpload(structureInputDTO);
  }
}
