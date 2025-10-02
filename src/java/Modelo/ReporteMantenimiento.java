/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.util.Date;

public class ReporteMantenimiento {
    private int id;
    private String tipoInconveniente;
    private String descripcion;
    private Date fechaHora;
    private int idUsuario; // usuario que lo generó

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipoInconveniente() { return tipoInconveniente; }
    public void setTipoInconveniente(String tipoInconveniente) { this.tipoInconveniente = tipoInconveniente; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
}

