package co.edu.univalle.vivaeventoseventservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelEventRequest {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(min = 10, max = 1000, message = "El motivo debe tener entre 10 y 1000 caracteres")
    private String reason;

    public CancelEventRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}