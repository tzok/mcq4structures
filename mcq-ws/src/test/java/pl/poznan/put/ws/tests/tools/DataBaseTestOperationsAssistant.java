package pl.poznan.put.ws.tests.tools;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.poznan.put.ws.entities.StructureInput;
import pl.poznan.put.ws.jpa.StructureInputCrudRepo;

@Component
public class DataBaseTestOperationsAssistant {

    private final StructureInputCrudRepo structureInputCrudRepo;

    @Autowired
    public DataBaseTestOperationsAssistant(StructureInputCrudRepo structureInputCrudRepo) {
        this.structureInputCrudRepo = structureInputCrudRepo;
    }

    public int countRows() {
        Iterable<StructureInput> objectsFromDB = structureInputCrudRepo.findAll();
        int numberOfObjectsInDB = 0;
        for (StructureInput structureInput : objectsFromDB) {
            numberOfObjectsInDB++;
        }
        return numberOfObjectsInDB;
    }

    public StructureInputCrudRepo getStructureInputCrudRepo() {
        return structureInputCrudRepo;
    }
}
