package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.IntegrationTest;
import com.rubenlazaro.reservas.application.usecase.CreateResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceControllerIT extends IntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CreateResource createResource;

    private org.springframework.test.web.servlet.ResultActions postResource(String json) throws Exception {
        return mvc.perform(post("/api/resources").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void createsAResource() throws Exception {
        postResource("""
                {"name": "Court 1"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Court 1"));
    }

    @Test
    void rejectsABlankName() throws Exception {
        postResource("""
                {"name": "   "}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void rejectsAMissingName() throws Exception {
        postResource("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void rejectsANameLongerThanTheLimit() throws Exception {
        postResource("{\"name\": \"" + "a".repeat(101) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        postResource("{\"name\": ")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void rejectsADuplicateNameWithConflict() throws Exception {
        createResource.execute("Court 1");

        postResource("""
                {"name": "Court 1"}
                """)
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void listsTheResources() throws Exception {
        createResource.execute("Court 1");
        createResource.execute("Court 2");

        mvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Court 1"))
                .andExpect(jsonPath("$[1].name").value("Court 2"));
    }

    @Test
    void listsNothingWhenThereAreNoResources() throws Exception {
        mvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
