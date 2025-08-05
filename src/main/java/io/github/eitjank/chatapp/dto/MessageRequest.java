package io.github.eitjank.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Message content must not be empty")
    private String content;

}
