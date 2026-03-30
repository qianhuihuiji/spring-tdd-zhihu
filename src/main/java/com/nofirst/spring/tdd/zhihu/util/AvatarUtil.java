package com.nofirst.spring.tdd.zhihu.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class AvatarUtil {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private AvatarUtil() {
        // utility
    }

    public static String generateFilename(String originalFilename) {
        String ext = "";
        if (originalFilename != null) {
            int idx = originalFilename.lastIndexOf('.');
            if (idx >= 0) {
                ext = originalFilename.substring(idx);
            }
        }
        return UUID.randomUUID().toString() + ext;
    }

    public static void validateContentType(MultipartFile file) {
        if (file == null) throw new IllegalArgumentException("file is null");
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("invalid content type: " + ct);
        }
    }

    public static void validateSize(MultipartFile file, long maxBytes) {
        if (file == null) throw new IllegalArgumentException("file is null");
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("file too large: " + file.getSize());
        }
    }
}
