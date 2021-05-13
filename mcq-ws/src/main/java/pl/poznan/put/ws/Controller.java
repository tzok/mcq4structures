package pl.poznan.put.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.poznan.put.ws.model.Version;

@RestController
@RequestMapping("/api")
public class Controller {

    private ModelService modelService;

    @Autowired
    public Controller(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping("/version")
    private Version getVersion(){
        return modelService.getVersion();
    }
}