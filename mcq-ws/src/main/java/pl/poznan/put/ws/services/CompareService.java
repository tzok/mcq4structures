package pl.poznan.put.ws.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.schema.ModelComparisonDTO;
import pl.poznan.put.schema.ModelsDTO;
import pl.poznan.put.schema.TargetModelsDTO;
import pl.poznan.put.ws.entities.TrigonometricRepresentation;
import pl.poznan.put.ws.exceptions.ObjectNotFoundException;
import pl.poznan.put.ws.jpa.TrigonometricRepresentationCrudRepo;
import pl.poznan.put.ws.services.subservices.ComputationService;

@Service
public class CompareService {

  private final ComputationService computationService;

  private final TrigonometricRepresentationCrudRepo trigonometricRepresentationCrudRepo;

  @Autowired
  public CompareService(
      ComputationService computationService,
      TrigonometricRepresentationCrudRepo trigonometricRepresentationCrudRepo) {
    this.computationService = computationService;
    this.trigonometricRepresentationCrudRepo = trigonometricRepresentationCrudRepo;
  }

  public List<ModelComparisonDTO> handleCompare(TargetModelsDTO targetModelsDTO) {
    return handleCompare(targetModelsDTO.getModels(), targetModelsDTO.getTarget());
  }

  public List<ModelComparisonDTO> handleCompare(ModelsDTO modelsDTO) {
    return handleCompare(modelsDTO.getModels(), null);
  }

  public List<ModelComparisonDTO> handleCompare(List<String> chains, String modelTarget) {
    List<TrigonometricRepresentation> trigonometricRepresentations =
        new ArrayList<TrigonometricRepresentation>();

    trigonometricRepresentations =
        StreamSupport.stream(
                trigonometricRepresentationCrudRepo
                    .findAllById(
                        chains.stream()
                            .map(chain -> UUID.fromString(chain))
                            .collect(Collectors.toList()))
                    .spliterator(),
                false)
            .collect(Collectors.toList());


    if (modelTarget != null) {
      List<TrigonometricRepresentation> targets = trigonometricRepresentationCrudRepo.findAllByInputId(UUID.fromString(modelTarget));
      if (targets.size() == 1){
        return computationService.compare(trigonometricRepresentations, targets.get(0));
      } else {
        throw new ObjectNotFoundException(modelTarget, TrigonometricRepresentation.class);
      }
    } else {
      return computationService.compare(trigonometricRepresentations);
    }
  }
}
