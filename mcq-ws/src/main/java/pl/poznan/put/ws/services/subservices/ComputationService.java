package pl.poznan.put.ws.services.subservices;

import org.springframework.stereotype.Service;
import pl.poznan.put.schema.ModelComparisonDTO;
import pl.poznan.put.schema.StructureContentDTO;
import pl.poznan.put.ws.entities.TrigonometricRepresentation;

import java.util.List;

@Service
public class ComputationService {
  public StructureContentDTO loadStructure(String pdbId, int assemblyId) {
    return null;
  }

  public List<StructureContentDTO> computeTorsionAngles(StructureContentDTO structureContentDTO) {
    return null;
  }

  public List<ModelComparisonDTO> compare(List<TrigonometricRepresentation> trigonometricRepresentations) {
    return null;
  }
}
