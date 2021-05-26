package pl.poznan.put.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.ws.services.AnalyzeService;
import pl.poznan.put.ws.services.UploadService;
import pl.poznan.put.ws.services.VersionService;

@Service
public class ServicesSupervisor {
  private final VersionService versionService;

  private final UploadService uploadService;

  private final AnalyzeService analyzeService;

  @Autowired
  public ServicesSupervisor(
      VersionService versionService, UploadService uploadService, AnalyzeService analyzeService) {
    this.versionService = versionService;
    this.uploadService = uploadService;
    this.analyzeService = analyzeService;
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
}
