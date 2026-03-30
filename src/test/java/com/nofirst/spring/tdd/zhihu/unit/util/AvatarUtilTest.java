package com.nofirst.spring.tdd.zhihu.unit.util;

import com.nofirst.spring.tdd.zhihu.util.AvatarUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarUtilTest {

    @Test
    void generate_unique_filename_preserves_extension() {
        // given
        String original = "photo.png";

        // when
        String generated = AvatarUtil.generateFilename(original);

        // then
        assertThat(generated).endsWith(".png");
        assertThat(generated).isNotEqualTo(original);
    }

    @Test
    void rejects_invalid_content_type() {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "data".getBytes());

        // when / then
        assertThatThrownBy(() -> AvatarUtil.validateContentType(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_oversized_file() {
        // given
        byte[] large = new byte[3 * 1024 * 1024]; // 3MB
        MockMultipartFile file = new MockMultipartFile("file", "large.jpg", "image/jpeg", large);

        // when / then
        assertThatThrownBy(() -> AvatarUtil.validateSize(file, 2 * 1024 * 1024))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
