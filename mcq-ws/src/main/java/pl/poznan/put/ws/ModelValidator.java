package pl.poznan.put.ws;

import org.springframework.stereotype.Service;
import pl.poznan.put.ws.exceptions.PathVariableException;

@Service
public class ModelValidator {
    public boolean validateGetTorsionPdbId(String pdbId) {
        if(pdbId.length() != 4) {
            throw new PathVariableException("pdbId", pdbId, "This parameter is meant to have 4 characters!");
        } else {
            return true;
        }
    }
}
