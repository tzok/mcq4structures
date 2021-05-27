package pl.poznan.put.ws.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.schema.ModelComparisonDTO;
import pl.poznan.put.schema.ModelsDTO;
import pl.poznan.put.schema.TargetModelsDTO;
import pl.poznan.put.ws.entities.TrigonometricRepresentation;
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
    return handleCompare(targetModelsDTO.getModels());
  }

  public List<ModelComparisonDTO> handleCompare(ModelsDTO modelsDTO) {
    return handleCompare(modelsDTO.getModels());
  }

  public List<ModelComparisonDTO> handleCompare(List<String> chains) {
    List<UUID> ids = new ArrayList<UUID>();
    for (String chain : chains) {
      ids.add(UUID.fromString(chain));
    }

    List<TrigonometricRepresentation> trigonometricRepresentations =
        new ArrayList<TrigonometricRepresentation>();
    for (TrigonometricRepresentation currentTR : trigonometricRepresentationCrudRepo.findAllById(ids)) {
      trigonometricRepresentations.add(currentTR);
    }

    return computationService.compare(trigonometricRepresentations);
  }
}
