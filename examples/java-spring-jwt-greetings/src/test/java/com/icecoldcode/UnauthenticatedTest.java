package com.icecoldcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icecoldcode.api.greeting.CreateGreetingDto;
import com.icecoldcode.api.greeting.Greeting;
import com.icecoldcode.api.greeting.GreetingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class UnauthenticatedTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    GreetingsRepository greetingsRepository;

    @Test
    public void getIndex() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index.html"));
    }

    @Test
    public void listGreetings() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/greetings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void addGreeting() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/greetings")
                        .content(new ObjectMapper().writeValueAsBytes(new CreateGreetingDto("hej")))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void removeGreetingNonExisting() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/greetings/13432")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void removeGreetingExisting() throws Exception {
        long id = greetingsRepository.create(new Greeting(1, "hello"));
        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/greetings/" + id)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

}
