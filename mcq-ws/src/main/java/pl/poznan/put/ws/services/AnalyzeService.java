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

@Service
public class AnalyzeService {

  private TrigonometricRepresentationCrudRepo trigonometricRepresentationCrudRepo;

  private StructureContentCrudRepo structureContentCrudRepo;

  private ModelMapper modelMapper;

  private ComputationService computationService;

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
    List<TrigonometricRepresentation> trigonometricRepresentationIterable =
        trigonometricRepresentationCrudRepo.findAllByInputId(givenId);

    if (trigonometricRepresentationIterable.size() > 0) {
      List<TrigonometricRepresentationDTO> trigonometricRepresentationDTOS =
          new ArrayList<TrigonometricRepresentationDTO>();
      for (TrigonometricRepresentation trigonometricRepresentation :
          trigonometricRepresentationIterable) {
        trigonometricRepresentationDTOS.add(
            modelMapper.map(trigonometricRepresentation, TrigonometricRepresentationDTO.class));
      }
      return trigonometricRepresentationDTOS;
    } else {
      Optional<StructureContent> structureContentOptional =
          structureContentCrudRepo.findById(givenId);
      if (structureContentOptional.isPresent()) {
        List<StructureContentDTO> structureContentDTOS =
            computationService.computeTorsionAngles(
                modelMapper.map(structureContentOptional.get(), StructureContentDTO.class));
        List<TrigonometricRepresentationDTO> trigonometricRepresentationDTOS =
            new ArrayList<TrigonometricRepresentationDTO>();
        for (StructureContentDTO structureContentDTO : structureContentDTOS) {
          trigonometricRepresentationDTOS.add(
              modelMapper.map(
                  trigonometricRepresentationCrudRepo.save(
                      modelMapper.map(structureContentDTO, TrigonometricRepresentation.class)),
                  TrigonometricRepresentationDTO.class));
        }
        return trigonometricRepresentationDTOS;
      } else {
        throw new ObjectNotFoundException(id, StructureContent.class);
      }
    }
  }
}
