package com.swaply.userservice.dto.user.create;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Size(min = 3, max = 100)
    private String name;
    @Size(min = 8, max = 100)
    private String email;
    @Size(min = 10, max = 10)
    private String phone;

    @Size(min = 8, max = 255)
    private String password;


}
