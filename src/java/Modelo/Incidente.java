package Modelo;

import java.util.Date;

public class Incidente {
    private int id;
    private int residenteId;
    private String tipo;
    private Date fechaHora;
    private String descripcion;

    public Incidente() {}

    public Incidente(int id, int residenteId, String tipo, Date fechaHora, String descripcion) {
        this.id = id;
        this.residenteId = residenteId;
        this.tipo = tipo;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
    }

    // GETTERS Y SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResidenteId() { return residenteId; }
    public void setResidenteId(int residenteId) { this.residenteId = residenteId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
