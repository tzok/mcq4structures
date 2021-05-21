package pl.poznan.put.ws.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.schema.StructureOutputDTO;
import pl.poznan.put.ws.entities.StructureInput;
import pl.poznan.put.ws.entities.StructureOutput;
import pl.poznan.put.ws.exceptions.ObjectNotFoundException;
import pl.poznan.put.ws.jpa.StructureInputCrudRepo;
import pl.poznan.put.ws.jpa.StructureOutputCrudRepo;

import java.util.Optional;
import java.util.UUID;

@Service
public class AnalyzeService {

  private StructureInputCrudRepo structureInputCrudRepo;

  private StructureOutputCrudRepo structureOutputCrudRepo;

  private ModelMapper modelMapper;

  private ComputationService computationService;

  @Autowired
  public AnalyzeService(
      StructureInputCrudRepo structureInputCrudRepo,
      StructureOutputCrudRepo structureOutputCrudRepo,
      ModelMapper modelMapper,
      ComputationService computationService) {
    this.structureInputCrudRepo = structureInputCrudRepo;
    this.structureOutputCrudRepo = structureOutputCrudRepo;
    this.modelMapper = modelMapper;
    this.computationService = computationService;
  }

  public StructureOutputDTO handleGetAnalyze(String idString) {
    UUID id = UUID.fromString(idString);

    Optional<StructureOutput> structureOutputOptional = structureOutputCrudRepo.findByInputId(id);

    if (structureOutputOptional.isPresent()) {
      return modelMapper.map(structureOutputOptional.get(), StructureOutputDTO.class);
    } else {
      Optional<StructureInput> structureInputOptional = structureInputCrudRepo.findById(id);
      if (structureInputOptional.isPresent()) {
        StructureOutput structureOutput =
            modelMapper.map(computationService.computeTorsionAngles(), StructureOutput.class);
        structureOutput.setInputId(id);
        return modelMapper.map(
            structureOutputCrudRepo.save(structureOutput), StructureOutputDTO.class);
      } else {
        throw new ObjectNotFoundException(idString, StructureInput.class);
      }
    }
  }
}
