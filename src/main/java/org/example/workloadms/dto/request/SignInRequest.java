package org.example.workloadms.dto.request;

import org.antlr.v4.runtime.misc.NotNull;

public record SignInRequest(
        @NotNull
        String username,
        @NotNull
        String password
) {
}

