package com.sprint.ootd5team.domain.profile.event;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public record ImageFileUploadEvent(
    String previousImageUrl
) { }
