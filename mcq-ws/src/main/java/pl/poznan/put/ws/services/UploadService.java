package pl.poznan.put.ws.services;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.ws.jpa.StructureInput;
import pl.poznan.put.ws.jpa.StructureInputCrudRepo;

@Service
public class UploadService {

    private StructureInputCrudRepo structureInputCrudRepo;

    private ModelMapper modelMapper;

    @Autowired
    public UploadService(StructureInputCrudRepo structureInputCrudRepo, ModelMapper modelMapper) {
        this.structureInputCrudRepo = structureInputCrudRepo;
        this.modelMapper = modelMapper;
    }

    public StructureInputDTO handlePostUpload(StructureInputDTO structureInputDTO){
        // Validation will be made with javax validation annotations by @Validated on controller and @Valid by endpoints
        StructureInput structureInput = modelMapper.map(structureInputDTO, StructureInput.class);
        return null;
    }
}
