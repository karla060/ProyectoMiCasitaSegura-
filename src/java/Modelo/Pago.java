/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.util.Date;

/**
 *
 * @author mpelv
 */
public class Pago {
  private int idPago;
    private int idUsuario;
    private String tipoPago;
    private double cantidad;
    private Date fechaPago;
    private String observaciones;
    private String numeroTarjeta;
    private Date fechaVencimiento;
    private String cvv;
    private String nombreTitular;
    private double mora;
    private double total;  

    public Pago() {
    }

    public Pago(int idPago, int idUsuario, String tipoPago, double cantidad, Date fechaPago, String observaciones, String numeroTarjeta, Date fechaVencimiento, String cvv, String nombreTitular, double mora, double total) {
        this.idPago = idPago;
        this.idUsuario = idUsuario;
        this.tipoPago = tipoPago;
        this.cantidad = cantidad;
        this.fechaPago = fechaPago;
        this.observaciones = observaciones;
        this.numeroTarjeta = numeroTarjeta;
        this.fechaVencimiento = fechaVencimiento;
        this.cvv = cvv;
        this.nombreTitular = nombreTitular;
        this.mora = mora;
        this.total = total;
    }
    
    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public double getMora() {
        return mora;
    }

    public void setMora(double mora) {
        this.mora = mora;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
    
    
    
    
}
