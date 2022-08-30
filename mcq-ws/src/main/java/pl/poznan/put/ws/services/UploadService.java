package pl.poznan.put.ws.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.schema.StructureContentDTO;
import pl.poznan.put.schema.UploadDTO;
import pl.poznan.put.ws.entities.StructureContent;
import pl.poznan.put.ws.exceptions.PathVariableException;
import pl.poznan.put.ws.jpa.StructureContentCrudRepo;
import pl.poznan.put.ws.services.subservices.ComputationService;

import java.util.Optional;

@Service
public class UploadService {

  private final ComputationService computationService;

  private final StructureContentCrudRepo structureContentCrudRepo;

  private final ModelMapper modelMapper;

  @Autowired
  public UploadService(
      ComputationService computationService,
      StructureContentCrudRepo structureContentCrudRepo,
      ModelMapper modelMapper) {
    this.computationService = computationService;
    this.structureContentCrudRepo = structureContentCrudRepo;
    this.modelMapper = modelMapper;
  }

  public UploadDTO handlePostUpload(StructureContentDTO structureContentDTO) {
    Optional<StructureContent> queryResult =
        structureContentCrudRepo.findByData(structureContentDTO.getData());
    if (queryResult.isPresent()) {
      return modelMapper.map(queryResult.get(), UploadDTO.class);
    } else {
      return modelMapper.map(
          structureContentCrudRepo.save(
              modelMapper.map(structureContentDTO, StructureContent.class)),
          UploadDTO.class);
    }
  }

  public UploadDTO handlePostUpload(String pdbId, int assemblyId) {
    validateParameters(pdbId, assemblyId);
    StructureContentDTO loadedStructure = computationService.loadStructure(pdbId, assemblyId);
    return handlePostUpload(loadedStructure);
  }

  private void validateParameters(String pdbId, int assemblyId) {
    if (pdbId.length() != 4) {
      throw new PathVariableException("pdbId", pdbId, "This parameter has to contain 4 characters!");
    }

    if (assemblyId < 1) {
      throw new PathVariableException("assemblyId", String.valueOf(assemblyId), "This number has to be positive!");
    }
  }
}
