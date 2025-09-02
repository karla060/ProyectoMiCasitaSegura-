/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Servicio para controlar la talanquera vía Arduino usando jSerialComm.
 */
public class ArduinoService {

    private SerialPort port;

    /**
     * Abre el puerto serie en el constructor.
     *
     * @param nombrePuerto Nombre del puerto (ej. "COM3", "/dev/ttyUSB0").
     * @throws IllegalStateException si no puede abrir el puerto.
     */
    public ArduinoService(String nombrePuerto) {
        // Obtiene el puerto por nombre
        port = SerialPort.getCommPort(nombrePuerto);

        // Configura parámetros estándar
        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);

        // Abre el puerto y lanza excepción si falla
        if (!port.openPort()) {
            throw new IllegalStateException("No se pudo abrir el puerto " + nombrePuerto);
        }

        // Ajuste opcional de timeouts
        port.setComPortTimeouts(
            SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
            /* readTimeoutMillis */ 2000,
            /* writeTimeoutMillis */ 0
        );
    }

    /**
     * Envía el comando OPEN al Arduino para abrir la talanquera.
     */
    public void abrirTalanquera() {
        if (port != null && port.isOpen()) {
            byte[] comando = "OPEN\n".getBytes();
            port.writeBytes(comando, comando.length);
        } else {
            throw new IllegalStateException("Puerto no abierto");
        }
    }

    /**
     * Cierra el puerto serie. Llamar al finalizar para liberar recursos.
     */
    public void cerrar() {
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }
}






/*
package service;

import com.fazecast.jSerialComm.SerialPort;


 // Servicio para abrir la talanquera enviando "OPEN\n" al Arduino.
 
public class ArduinoService {

    private final SerialPort puerto;


    public ArduinoService(String puertoSerie) {
        this.puerto = SerialPort.getCommPort(puertoSerie);
        this.puerto.setBaudRate(9600);
        this.puerto.openPort();
    }

    // Envía al Arduino el comando para abrir la talanquera.
     
    public void abrirTalanquera() {
        if (puerto.isOpen()) {
            puerto.writeBytes("OPEN\n".getBytes(), "OPEN\n".length());
        }
    }

    //Cierra el puerto serie cuando ya no se necesite.
    public void cerrar() {
        if (puerto.isOpen()) {
            puerto.closePort();
        }
    }
}
*/