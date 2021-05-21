package pl.poznan.put.ws.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.ws.exceptions.NoGitPropertiesException;
import pl.poznan.put.ws.model.ModelValidator;
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
    if (version.getVersion().equals("${git.commit.id.describe-short}")) {
      throw new NoGitPropertiesException();
    } else {
      return version;
    }
  }

  public Torsion findTorsion(String pdbId, Integer assemblyId) {
    return null;
  }

  public void addTorsion(Torsion newTorsion) {}
}
