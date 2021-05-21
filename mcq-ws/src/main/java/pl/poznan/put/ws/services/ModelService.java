package pl.poznan.put.ws.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import pl.poznan.put.ws.ModelValidator;
import pl.poznan.put.ws.model.Torsion;
import pl.poznan.put.ws.model.Version;

@Service
public class ModelService {

  private Version version;

  private ModelValidator modelValidator;

  @Autowired
  public ModelService(Version version, ModelValidator modelValidator) {
    this.version = version;
    this.modelValidator = modelValidator;
  }

  public Version findVersion() {
    return version;
  }

  public Torsion findTorsion(String pdbId, Integer assemblyId) {
    return null;
  }

  public void addTorsion(Torsion newTorsion) {}
}
