package pl.poznan.put.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.ws.services.AnalyzeService;
import pl.poznan.put.ws.services.CompareService;
import pl.poznan.put.ws.services.UploadService;
import pl.poznan.put.ws.services.VersionService;

@Service
public class ServicesSupervisor {
  private final VersionService versionService;

  private final UploadService uploadService;

  private final AnalyzeService analyzeService;

  private final CompareService compareService;

  @Autowired
  public ServicesSupervisor(
      VersionService versionService,
      UploadService uploadService,
      AnalyzeService analyzeService,
      CompareService compareService) {
    this.versionService = versionService;
    this.uploadService = uploadService;
    this.analyzeService = analyzeService;
    this.compareService = compareService;
  }

  public VersionService getVersionService() {
    return versionService;
  }

  public UploadService getUploadService() {
    return uploadService;
  }

  public AnalyzeService getAnalyzeService() {
    return analyzeService;
  }

  public CompareService getCompareService() {
    return compareService;
  }
}
