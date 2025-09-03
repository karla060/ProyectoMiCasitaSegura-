/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package service;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoService {

    private SerialPort port;

    /**
     * Constructor: abre el puerto serie.
     *
     * @param nombrePuerto Nombre del puerto (ej. "COM3" o "/dev/ttyUSB0")
     */
    public ArduinoService(String nombrePuerto) {
        port = SerialPort.getCommPort(nombrePuerto);

        // Configuración básica del puerto
        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);

        // Abrir puerto
        if (!port.openPort()) {
            throw new IllegalStateException("No se pudo abrir el puerto " + nombrePuerto);
        }

        // Timeouts opcionales
        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                2000,  // read timeout
                0      // write timeout
        );
    }

    /**
     * Envia el comando al Arduino por Serial.
     */
    private void enviarComando(String comando) {
        if (port != null && port.isOpen()) {
            byte[] bytes = (comando + "\n").getBytes();
            port.writeBytes(bytes, bytes.length);
        } else {
            throw new IllegalStateException("Puerto serie no abierto");
        }
    }

    /**
     * Abre la talanquera de entrada (servo 1).
     */
    public void abrirEntrada() {
        enviarComando("OPEN_IN");
    }

    /**
     * Abre la talanquera de salida (servo 2).
     */
    public void abrirSalida() {
        enviarComando("OPEN_OUT");
    }

    /**
     * Cierra el puerto serial al finalizar.
     */
    public void cerrar() {
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }
}


