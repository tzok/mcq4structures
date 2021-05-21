package pl.poznan.put.ws.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.ws.exceptions.ObjectNotFoundException;
import pl.poznan.put.ws.entities.StructureInput;
import pl.poznan.put.ws.jpa.StructureInputCrudRepo;

import java.util.Optional;

@Service
public class UploadService {

  private StructureInputCrudRepo structureInputCrudRepo;

  private ModelMapper modelMapper;

  @Autowired
  public UploadService(StructureInputCrudRepo structureInputCrudRepo, ModelMapper modelMapper) {
    this.structureInputCrudRepo = structureInputCrudRepo;
    this.modelMapper = modelMapper;
  }

  // todo: Input validation - javax validation annotations by @Validated on controller and @Valid by
  // endpoints
  public StructureInputDTO handlePostUpload(StructureInputDTO structureInputDTO) {
    StructureInput structureInput = mapToStructureInput(structureInputDTO);

    if (structureInputCrudRepo.existsById(structureInput.getId())) {
      return mapToStructureInputDTO(
          structureInputCrudRepo
              .findById(structureInput.getId())
              .orElseThrow(
                  () ->
                      new ObjectNotFoundException(
                          structureInput.getId().toString(), StructureInput.class)));
    } else {
      Optional<StructureInput> queryOptional = null;

      queryOptional = structureInputCrudRepo.findByPdbId(structureInput.getPdbId());
      if (queryOptional.isPresent()) {
        return mapToStructureInputDTO(queryOptional.get());
      }

      queryOptional = structureInputCrudRepo.findByAssemblyId(structureInput.getAssemblyId());
      if (queryOptional.isPresent()) {
        return mapToStructureInputDTO(queryOptional.get());
      }

      queryOptional =
          structureInputCrudRepo.findByStructureContent(structureInput.getStructureContent());
      if (queryOptional.isPresent()) {
        return mapToStructureInputDTO(queryOptional.get());
      }

      return mapToStructureInputDTO(structureInputCrudRepo.save(structureInput));
    }
  }

  private StructureInputDTO mapToStructureInputDTO(StructureInput structureInput) {
    return modelMapper.map(structureInput, StructureInputDTO.class);
  }

  private StructureInput mapToStructureInput(StructureInputDTO structureInputDTO) {
    return modelMapper.map(structureInputDTO, StructureInput.class);
  }
}
