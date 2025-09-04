/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

/**
 *
 * @author mpelv
 */


import java.text.SimpleDateFormat;
import java.util.Date;

public class Visitante {
    private int id;
    private String nombre;
    private String dpi;
    private String tipoVisita;     // "Visita" | "Por intentos"
    private Date fechaVisita;      // si "Visita"
    private Integer intentos;          // si "Por intentos"
    private String correo;         // correo del visitante
    private String residente;      // nombre del residente en sesión
    private String qrCodigo;       // opcional: token único si decides persistirlo
    private Date fechaRegistro;    // timestamp de auditoría (alta o última actualización)
    private int dentro;
    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getTipoVisita() { return tipoVisita; }
    public void setTipoVisita(String tipoVisita) { this.tipoVisita = tipoVisita; }

    public Date getFechaVisita() { return fechaVisita; }
    public void setFechaVisita(Date fechaVisita) { this.fechaVisita = fechaVisita; }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }


    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getResidente() { return residente; }
    public void setResidente(String residente) { this.residente = residente; }

    public String getQrCodigo() { return qrCodigo; }
    public void setQrCodigo(String qrCodigo) { this.qrCodigo = qrCodigo; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public int getDentro() {
        return dentro;
    }

    public void setDentro(int dentro) {
        this.dentro = dentro;
    }

    

// ...

// Auxiliares de formato (creados por método para evitar problemas de thread-safety)
public String getFechaGeneracionTextoDia() {
    return new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
}

public String getFechaGeneracionTextoHora() {
    return new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
}

/**
 * Texto de validez para correos y UI:
 * - Si tipoVisita = "Por intentos": "intentos X"
 * - Si tipoVisita = "Visita" y hay fechaVisita: "hasta el yyyy-MM-dd"
 */
public String getValidezTexto() {
    if (tipoVisita != null && tipoVisita.equalsIgnoreCase("Por intentos")) {
        return "intentos " + intentos;
    }
    if (fechaVisita != null) {
        return "hasta el " + new SimpleDateFormat("yyyy-MM-dd").format(fechaVisita);
    }
    return "según parámetros del registro";
}

}

