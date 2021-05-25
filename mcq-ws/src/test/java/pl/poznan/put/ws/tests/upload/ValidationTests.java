package pl.poznan.put.ws.tests.upload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.poznan.put.schema.StructureInputDTO;
import pl.poznan.put.ws.tests.tools.DataBaseTestOperationsAssistant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
public class ValidationTests {
  private WebApplicationContext wac;

  private ObjectMapper objectMapper;

  private DataBaseTestOperationsAssistant dataBaseTestOperationsAssistant;

  private MockMvc mockMvc;

  @Autowired
  public ValidationTests(WebApplicationContext wac, ObjectMapper objectMapper, DataBaseTestOperationsAssistant dataBaseTestOperationsAssistant) {
    this.wac = wac;
    this.objectMapper = objectMapper;
    this.dataBaseTestOperationsAssistant = dataBaseTestOperationsAssistant;
  }

  @BeforeEach
  void setup() {
    dataBaseTestOperationsAssistant.getStructureInputCrudRepo().deleteAll();
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(wac)
            .apply(sharedHttpSession())
            .build();
  }

  @Test
  void postWith3charactersPdbId() throws Exception {
    StructureInputDTO pdbIdWith3Characters =
        new StructureInputDTO("8c549628-1111-4d56-8150-0377d3995915", "12d", 1, "asdsf3efd12sd");
    String jsonPdbIdWith3Characters = objectMapper.writeValueAsString(pdbIdWith3Characters);
    mockMvc.perform(post("/api/upload")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(jsonPdbIdWith3Characters))
            .andExpect(status().isBadRequest())
            .andDo(print());
    assertThat(dataBaseTestOperationsAssistant.countRows()).isEqualTo(0);
  }

  @Test
  void postWith5charactersPdbId() throws Exception {
    StructureInputDTO pdbIdWith3Characters =
            new StructureInputDTO("8c549628-1111-4d56-8150-0377d3995915", "12d11", 1, "asdsf3efd12sd");
    String jsonPdbIdWith3Characters = objectMapper.writeValueAsString(pdbIdWith3Characters);
    mockMvc.perform(post("/api/upload")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(jsonPdbIdWith3Characters))
            .andExpect(status().isBadRequest())
            .andDo(print());
    assertThat(dataBaseTestOperationsAssistant.countRows()).isEqualTo(0);
  }
}
