package Modelo;

import java.util.List;

public class Conversacion {
    private int id;
    private int residenteId;
    private int agenteId;
    private String residenteNombre;
    private String agenteNombre;
    private List<Mensaje> mensajes;

    public Conversacion() {}

    public Conversacion(int id, int residenteId, int agenteId) {
        this.id = id;
        this.residenteId = residenteId;
        this.agenteId = agenteId;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResidenteId() { return residenteId; }
    public void setResidenteId(int residenteId) { this.residenteId = residenteId; }

    public int getAgenteId() { return agenteId; }
    public void setAgenteId(int agenteId) { this.agenteId = agenteId; }

    public String getResidenteNombre() { return residenteNombre; }
    public void setResidenteNombre(String residenteNombre) { this.residenteNombre = residenteNombre; }

    public String getAgenteNombre() { return agenteNombre; }
    public void setAgenteNombre(String agenteNombre) { this.agenteNombre = agenteNombre; }

    public List<Mensaje> getMensajes() { return mensajes; }
    public void setMensajes(List<Mensaje> mensajes) { this.mensajes = mensajes; }
}
