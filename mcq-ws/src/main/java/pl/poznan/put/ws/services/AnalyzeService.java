package pl.poznan.put.ws.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.schema.StructureContentDTO;
import pl.poznan.put.schema.TrigonometricRepresentationDTO;
import pl.poznan.put.ws.entities.StructureContent;
import pl.poznan.put.ws.entities.TrigonometricRepresentation;
import pl.poznan.put.ws.exceptions.ObjectNotFoundException;
import pl.poznan.put.ws.jpa.StructureContentCrudRepo;

import pl.poznan.put.ws.jpa.TrigonometricRepresentationCrudRepo;
import pl.poznan.put.ws.services.subservices.ComputationService;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyzeService {

  private final TrigonometricRepresentationCrudRepo trigonometricRepresentationCrudRepo;

  private final StructureContentCrudRepo structureContentCrudRepo;

  private final ModelMapper modelMapper;

  private final ComputationService computationService;

  @Autowired
  public AnalyzeService(
      TrigonometricRepresentationCrudRepo trigonometricRepresentationCrudRepo,
      ModelMapper modelMapper,
      ComputationService computationService,
      StructureContentCrudRepo structureContentCrudRepo) {
    this.trigonometricRepresentationCrudRepo = trigonometricRepresentationCrudRepo;
    this.modelMapper = modelMapper;
    this.computationService = computationService;
    this.structureContentCrudRepo = structureContentCrudRepo;
  }

  public List<TrigonometricRepresentationDTO> handleGetAnalyze(String id) {
    UUID givenId = UUID.fromString(id);
    List<TrigonometricRepresentation> foundTrigonometricRepresentations =
        trigonometricRepresentationCrudRepo.findAllByInputId(givenId);

    if (foundTrigonometricRepresentations.size() > 0) {
      return trigonometricRepresentationCrudRepo.findAllByInputId(givenId).stream()
          .map(foundTR -> modelMapper.map(foundTR, TrigonometricRepresentationDTO.class))
          .collect(Collectors.toList());
    } else {
      Optional<StructureContent> structureContentOptional =
          structureContentCrudRepo.findById(givenId);
      if (structureContentOptional.isPresent()) {
        return computationService
            .computeTorsionAngles(
                modelMapper.map(structureContentOptional.get(), StructureContentDTO.class))
            .stream()
            .map(
                structureContentDTO ->
                    modelMapper.map(
                        trigonometricRepresentationCrudRepo.save(
                            modelMapper.map(
                                structureContentDTO, TrigonometricRepresentation.class)),
                        TrigonometricRepresentationDTO.class))
            .collect(Collectors.toList());
      } else {
        throw new ObjectNotFoundException(id, StructureContent.class);
      }
    }
  }
}
