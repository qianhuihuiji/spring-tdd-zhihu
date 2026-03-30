package com.nofirst.spring.tdd.zhihu.service;

import com.nofirst.spring.tdd.zhihu.util.AvatarUtil;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class AvatarService {

    private static final Path UPLOAD_DIR = Paths.get("uploads", "avatars");
    private static final long DEFAULT_MAX_SIZE = 2L * 1024 * 1024; // 2MB

    @Autowired
    private UserMapper userMapper;

    public String saveAvatar(Integer userId, MultipartFile file) throws IOException {
        AvatarUtil.validateContentType(file);
        AvatarUtil.validateSize(file, DEFAULT_MAX_SIZE);

        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR);
        }

        String filename = AvatarUtil.generateFilename(file.getOriginalFilename());
        Path target = UPLOAD_DIR.resolve(filename);
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        // save avatar path to user record
        String relativePath = "/uploads/avatars/" + filename;
        User user = new User();
        user.setId(userId);
        user.setAvatar(relativePath);
        userMapper.updateByPrimaryKeySelective(user);
        return relativePath;
    }
}
