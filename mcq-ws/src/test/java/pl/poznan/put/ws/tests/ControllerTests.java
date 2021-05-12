package pl.poznan.put.ws.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.poznan.put.ws.Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@RunWith(SpringRunner.class)
@WebMvcTest(Controller.class)
public class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getVersionTest() throws Exception {
        mockMvc.perform(get("/api/version"))
                .andExpect(content().contentType("application/json"))
                .andDo(print());
    }
}
