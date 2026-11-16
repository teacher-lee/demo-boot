package com.oraclejava.demoboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void crudLifecycle() throws Exception {
		String location = mockMvc.perform(post("/api/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"First\",\"content\":\"hello\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", startsWith("/api/notes/")))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.title").value("First"))
				.andExpect(jsonPath("$.content").value("hello"))
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get(location))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("First"));

		mockMvc.perform(get("/api/notes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());

		mockMvc.perform(put(location)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Updated\",\"content\":\"bye\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Updated"))
				.andExpect(jsonPath("$.content").value("bye"));

		mockMvc.perform(delete(location))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(location))
				.andExpect(status().isNotFound());
	}

	@Test
	void patchUpdatesOnlyProvidedFields() throws Exception {
		String location = mockMvc.perform(post("/api/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Original\",\"content\":\"keep me\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(patch(location)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"Patched\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Patched"))
				.andExpect(jsonPath("$.content").value("keep me"));

		mockMvc.perform(patch(location)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"content\":\"new body\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Patched"))
				.andExpect(jsonPath("$.content").value("new body"));

		mockMvc.perform(patch(location)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\" \"}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(patch("/api/notes/999999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"t\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createWithBlankTitleReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/notes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"  \",\"content\":\"x\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void missingNoteReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/notes/999999")).andExpect(status().isNotFound());
		mockMvc.perform(put("/api/notes/999999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"t\"}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/notes/999999")).andExpect(status().isNotFound());
	}

}
