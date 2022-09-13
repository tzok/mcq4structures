package pl.poznan.put.ws.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.ws.components.Version;
import pl.poznan.put.ws.exceptions.NoGitPropertiesException;

@Service
public class VersionService {

  private final Version version;

  @Autowired
  public VersionService(Version version) {
    this.version = version;
  }

  public Version findVersion() {
    if (version.getVersion().equals("${git.commit.id.describe-short}")) {
      throw new NoGitPropertiesException();
    } else {
      return version;
    }
  }
}
