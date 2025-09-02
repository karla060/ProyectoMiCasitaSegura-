/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

/**
 *
 * @author gp
 */
public class Lote {
    
    private int idLote;
    private String nombreLote;

    public Lote(int idLote, String nombreLote) {
        this.idLote = idLote;
        this.nombreLote = nombreLote;
    }

    public int getIdLote() {
        return idLote;
    }

    public String getNombreLote() {
        return nombreLote;
    }

    @Override
    public String toString() {
        return nombreLote;
    }
}
