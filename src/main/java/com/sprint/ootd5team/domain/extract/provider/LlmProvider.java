package com.sprint.ootd5team.domain.extract.provider;

import org.springframework.ai.chat.prompt.Prompt;

public interface LlmProvider {

    String chatCompletion(Prompt prompt);
}
