package pl.poznan.put.ws;

import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.schema.StructureOutputDTO;
import pl.poznan.put.ws.componentes.Version;
import pl.poznan.put.ws.services.AnalyzeService;
import pl.poznan.put.ws.services.VersionService;
import pl.poznan.put.ws.services.UploadService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class Controller {

  private VersionService versionService;

  private UploadService uploadService;

  private AnalyzeService analyzeService;

  @Autowired
  public Controller(VersionService versionService, UploadService uploadService, AnalyzeService analyzeService) {
    this.versionService = versionService;
    this.uploadService = uploadService;
    this.analyzeService = analyzeService;
  }

  @GetMapping("/version")
  private Version getVersion() {
    return versionService.findVersion();
  }

  @PostMapping("/upload")
  private StructureInputDTO postUpload(@RequestBody @Valid StructureInputDTO structureInputDTO) {
    return uploadService.handlePostUpload(structureInputDTO);
  }

  @GetMapping("/analyze/{id}")
  private StructureOutputDTO getAnalyze(@PathVariable @Length(min = 4, max = 4) String id) {
    return analyzeService.handleGetAnalyze(id);
  }
}
