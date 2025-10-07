package Modelo;

import java.util.Date;

public class Mensaje {
    private int id;
    private int conversacionId;
    private int emisorId;
    private String mensaje;
    private Date fechaHora;

    public Mensaje() {}

    public Mensaje(int id, int conversacionId, int emisorId, String mensaje, Date fechaHora) {
        this.id = id;
        this.conversacionId = conversacionId;
        this.emisorId = emisorId;
        this.mensaje = mensaje;
        this.fechaHora = fechaHora;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConversacionId() { return conversacionId; }
    public void setConversacionId(int conversacionId) { this.conversacionId = conversacionId; }

    public int getEmisorId() { return emisorId; }
    public void setEmisorId(int emisorId) { this.emisorId = emisorId; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }
}
