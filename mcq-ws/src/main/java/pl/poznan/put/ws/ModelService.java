package pl.poznan.put.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.ws.model.Version;


@Service
public class ModelService {

    private Version version;

    @Autowired
    public ModelService(Version version) {
        this.version = version;
    }

    public Version getVersion(){
        return version;
    }
}
