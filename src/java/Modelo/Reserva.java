/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.sql.Date;
import java.sql.Time;

public class Reserva {
    private int id;
    private String salon;
    private String residenteNombre;
    private String residenteCorreo;
    private Date fecha;
    private Time horaInicio;
    private Time horaFin;
    private String estado;

    public Reserva() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSalon() { return salon; }
    public void setSalon(String salon) { this.salon = salon; }

    public String getResidenteNombre() { return residenteNombre; }
    public void setResidenteNombre(String residenteNombre) { this.residenteNombre = residenteNombre; }

    public String getResidenteCorreo() { return residenteCorreo; }
    public void setResidenteCorreo(String residenteCorreo) { this.residenteCorreo = residenteCorreo; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    
    
    

    public Time getHoraInicio() { return horaInicio; }
    public void setHoraInicio(Time horaInicio) { this.horaInicio = horaInicio; }

    public Time getHoraFin() { return horaFin; }
    public void setHoraFin(Time horaFin) { this.horaFin = horaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}