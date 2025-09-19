package com.sprint.ootd5team.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 6, max = 50, message = "비밀번호는 8자 이상 50자 이하로 입력해야 합니다.")
    String password
) {

}
