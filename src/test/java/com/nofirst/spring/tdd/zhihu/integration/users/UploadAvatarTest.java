package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
public class UploadAvatarTest extends BaseContainerTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static Path tempAvatarDir;

    @BeforeAll
    static void setUpTempDir() throws IOException {
        tempAvatarDir = Files.createTempDirectory("test-avatars");
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("avatar.upload.dir", () -> tempAvatarDir.toString());
    }

    @AfterAll
    static void tearDownTempDir() throws IOException {
        if (tempAvatarDir != null && Files.exists(tempAvatarDir)) {
            Files.walk(tempAvatarDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    void authenticated_user_can_upload_avatar_and_get_avatar_path() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "dummy".getBytes());

        // when
        var mvcResult = this.mockMvc.perform(multipart("/users/{id}/avatar", 1).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andReturn();

        // then: response data contains relative path and user record updated
        String json = mvcResult.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        String path = node.get("data").asText();
        User user = userMapper.selectByPrimaryKey(1);
        assertThat(user.getAvatar()).isEqualTo(path);

        // and file exists on disk
        Path avatarFile = tempAvatarDir.resolve(Paths.get(path).getFileName().toString());
        assertThat(Files.exists(avatarFile)).isTrue();
    }
}
