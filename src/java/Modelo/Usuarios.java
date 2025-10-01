/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.util.Date;

/**
 *
 * @author gp
 */
public class Usuarios {
    private int id;
    private String dpi;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private int idRol;
 //   private final ObjectProperty<Lote> lote = new SimpleObjectProperty<>();
   // private final ObjectProperty<Casa> casa = new SimpleObjectProperty<>();
    private Integer idLote;
    private Integer idCasa;
    private boolean activo;
    private int dentro; // 0 = fuera, 1 = dentro
    
    
    private Date fechaCreacion; // ya definido en tu clase


private String numeroCasa;
 private String nombreLote;

    public String getNombreLote() {
        return nombreLote;
    }

    public void setNombreLote(String nombreLote) {
        this.nombreLote = nombreLote;
    }

    // Constructor vacío y getters/setters
    public Usuarios() {}
    
    
 


   public Usuarios(int id, String dpi, String nombres, String apellidos,
                   String correo, String contrasena,
                   int idRol, Integer idLote, Integer idCasa, boolean activo, int dentro) {
        this.id = id;
        this.dpi = dpi;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.contrasena = contrasena;
        this.idRol = idRol;
        this.idLote = idLote;
        this.idCasa = idCasa;
        this.activo = activo;
        this.dentro = dentro;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Integer getIdCasa()
    { return idCasa; }
    
    public void setIdCasa(Integer idCasa) 
    { this.idCasa = idCasa; }

    public Integer getIdLote() 
    { return idLote; }
    
    public void setIdLote(Integer idLote) 
    { this.idLote = idLote; }

    public int getDentro() {
        return dentro;
    }

    public void setDentro(int dentro) {
        this.dentro = dentro;
    }
   
public String getNumeroCasa() { return numeroCasa; }
public void setNumeroCasa(String numeroCasa) { this.numeroCasa = numeroCasa; }

public Date getFechaCreacion() { return fechaCreacion; }
public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

}
