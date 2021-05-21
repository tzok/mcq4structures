package pl.poznan.put.ws.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.poznan.put.schema.StructureInputDTO;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
public class ControllerTests {

  @Autowired private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(wac)
            .alwaysExpect(status().isOk())
            .apply(sharedHttpSession())
            .build();
  }

  @Test
  void getVersionTest() throws Exception {
    mockMvc
        .perform(get("/api/version"))
        .andExpect(header().string("Content-Type", "application/json"))
        .andDo(print());
  }

  @Test
  void postUploadTest() throws Exception {
    StructureInputDTO exampleStructureInputDTO =
        new StructureInputDTO("1", "12d2", 1, "asdsf3efd12sd");
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonStructureInputDTO = objectMapper.writeValueAsString(exampleStructureInputDTO);

    mockMvc.perform(post("/api/upload").content(jsonStructureInputDTO)).andDo(print());
  }
}
