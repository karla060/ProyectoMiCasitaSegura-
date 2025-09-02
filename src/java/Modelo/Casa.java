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
public class Casa {
    
    private int idCasa;
    private int numeroCasa;

    public Casa(int idCasa, int numeroCasa) {
        this.idCasa = idCasa;
        this.numeroCasa = numeroCasa;
    }

    public int getIdCasa() {
        return idCasa;
    }

    public int getNumeroCasa() {
        return numeroCasa;
    }

    @Override
    public String toString() {
        return String.valueOf(numeroCasa);
    }
}
