package com.sprint.ootd5team.base.llm;

import org.springframework.ai.chat.prompt.Prompt;

public interface LlmProvider {

    String chatCompletion(Prompt prompt);
}
