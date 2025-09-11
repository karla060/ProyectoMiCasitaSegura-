/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import Modelo.Usuarios;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SesionHelper {

    // Devuelve el usuario logueado o null si no hay sesión
    public static Usuarios getUsuarioLogueado(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return (sesion != null) ? (Usuarios) sesion.getAttribute("usuario") : null;
    }
}

