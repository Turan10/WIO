package app.wio.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String message;
    private int status;
    private String timestamp;
    private Map<String, String> errors;
    private String errorCode;
}
