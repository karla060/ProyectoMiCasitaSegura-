<%-- 
    Document   : EscanearQR
    Created on : 31/08/2025, 06:19:34 PM
    Author     : gp
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
  <title>Escanear Código QR</title>
  <style>
    video { width: 100%; max-width: 400px; border: 1px solid #666; }
    #msg { margin-top: 1em; font-weight: bold; }
  </style>
</head>
<body>
  <h2>Escanea tu código QR</h2>
  <video id="video" autoplay muted playsinline></video>
  <div id="msg"></div>

  <!-- jsQR: motor de decodificación de QR en JS -->
  <script src="https://unpkg.com/jsqr/dist/jsQR.js"></script>
  <script>
    const video = document.getElementById('video');
    const msg   = document.getElementById('msg');
    const canvas = document.createElement('canvas');
    const ctx    = canvas.getContext('2d');

    // 1. Pide permiso de cámara y enlaza el video
    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
      .then(stream => { video.srcObject = stream; })
      .catch(err => { msg.textContent = 'Error al abrir cámara: ' + err; });

    // 2. Bucle de escaneo
    video.addEventListener('play', () => {
      const tick = () => {
        if (video.readyState === video.HAVE_ENOUGH_DATA) {
          // Ajusta canvas al tamaño del video
          canvas.width  = video.videoWidth;
          canvas.height = video.videoHeight;
          ctx.drawImage(video, 0, 0);
          const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height);

          const código = jsQR(imgData.data, imgData.width, imgData.height);
          if (código) {
            // Desactiva cámara
            video.srcObject.getTracks().forEach(t => t.stop());
            enviarAlServidor(código.data);
            return;
          }
        }
        requestAnimationFrame(tick);
      };
      requestAnimationFrame(tick);
    });

    // 3. Envía el contenido al servlet vía POST
    function enviarAlServidor(qrData) {
      msg.textContent = 'QR detectado: ' + qrData + ' → Validando…';
      fetch('EscanearQR', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ qr: qrData })
      })
      .then(r => r.json())
      .then(json => {
        if (json.success) {
          msg.textContent = 'Acceso permitido: ' + json.nombre;
        } else {
          msg.textContent = 'ERROR: ' + json.message;
        }
      })
      .catch(err => { msg.textContent = 'Error de red: ' + err; });
    }
  </script>
</body>
</html>

