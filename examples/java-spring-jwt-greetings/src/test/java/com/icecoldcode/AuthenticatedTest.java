package com.icecoldcode;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icecoldcode.api.greeting.CreateGreetingDto;
import com.icecoldcode.api.greeting.GreetingDto;
import com.icecoldcode.core.authentication.CreateAuthUserDto;
import com.icecoldcode.core.authentication.LoginDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticatedTest {

    @Autowired
    private MockMvc mvc;

    private Cookie[] user1CompanyACookies;
    private Cookie[] admin1CompanyACookies;
    private Cookie[] user2CompanyACookies;
    private Cookie[] user1CompanyBCookies;

    @BeforeAll
    public void registerAndLogin() throws Exception {
        register("_test1_companyA", "test", "_test_companyA", Set.of());
        register("_test2_companyA", "test", "_test_companyA", Set.of());
        register("_admin1_companyA", "test", "_test_companyA", Set.of("ROLE_ADMIN"));
        user1CompanyACookies = login("_test1_companyA", "test");
        user2CompanyACookies = login("_test2_companyA", "test");
        admin1CompanyACookies = login("_admin1_companyA", "test");

        register("_test1_companyB", "test", "_test_companyB", Set.of());
        user1CompanyBCookies = login("_test1_companyB", "test");
    }

    @Test
    public void addGreeting() throws Exception {
        var idOfGreeting = createGreeting(user1CompanyACookies);
        List<GreetingDto> greetingList = getGreetings();

        var newGreeting = greetingList.stream()
                .filter(greetingDto -> idOfGreeting == greetingDto.id())
                .findFirst();

        assertTrue(
                newGreeting.isPresent(),
                "Id of new greeting shall be present"
        );

        assertTrue(
                newGreeting.filter(greetingDto -> greetingDto.message().equals("hej")).isPresent(),
                "New greeting has specified message"
        );
    }

    @Test
    public void deleteGreetingForOtherUserInSameCompanyAsAdmin() throws Exception {
        var idOfGreeting = createGreeting(user1CompanyACookies);

        mvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/greetings/" + idOfGreeting)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(admin1CompanyACookies)
        ).andExpect(status().isNoContent());
    }

    @Test
    public void deleteGreetingForOtherUserInSameCompanyAsUser() throws Exception {
        var idOfGreeting = createGreeting(user1CompanyACookies);
        mvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/greetings/" + idOfGreeting)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(user2CompanyACookies)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void deleteGreetingForUserInDifferentCompanyAsAdmin() throws Exception {
        var idOfGreeting = createGreeting(user1CompanyBCookies);
        mvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/greetings/" + idOfGreeting)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(admin1CompanyACookies)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void deleteGreetingForUserInDifferentCompanyAsUser() throws Exception {
        var idOfGreeting = createGreeting(user1CompanyBCookies);
        mvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/greetings/" + idOfGreeting)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(user1CompanyACookies)
        ).andExpect(status().isNotFound());
    }

    private Cookie[] login(String username, String password) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(
                                new LoginDto(username, password)
                        )))
                .andReturn()
                .getResponse()
                .getCookies();
    }

    private void register(String username,
                          String password,
                          String companyId,
                          Set<String> roles) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(
                                new CreateAuthUserDto(
                                        username,
                                        password,
                                        companyId,
                                        roles
                                )
                        )))
                .andExpect(status().isCreated());
    }

    private long createGreeting(Cookie[] authCookies) throws Exception {
        return Long.parseLong(
                mvc.perform(MockMvcRequestBuilders
                                .post("/api/v1/greetings")
                                .content(new ObjectMapper().writeValueAsBytes(new CreateGreetingDto("hej")))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(authCookies)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        );
    }

    private List<GreetingDto> getGreetings() throws Exception {
        var res = mvc.perform(MockMvcRequestBuilders
                .get("/api/v1/greetings")
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(
                res.getResponse().getContentAsByteArray(),
                new TypeReference<>() {
                }
        );
    }

}
