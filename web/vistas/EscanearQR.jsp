<%-- 
    Document   : EscanearQR
    Created on : 31/08/2025, 06:19:34 PM
    Author     : gp
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Escanear Código QR</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

  <style>
    video { 
      width: 100%; 
      max-width: 400px; 
      border: 2px solid #0d6efd; 
      border-radius: 8px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    }
    #msg { 
      margin-top: 1em; 
      font-weight: bold; 
      text-align: center;
    }
  </style>
</head>
<body class="bg-light">

<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2 class="text-primary">Escanea tu código QR</h2>
    <!-- Botón Menú Principal -->
    <a href="index.jsp" class="btn btn-secondary">Menú Principal</a>
  </div>

  <!-- Sección de video -->
  <div class="text-center">
    <video id="video" autoplay muted playsinline></video>
    <div id="msg" class="mt-3"></div>
  </div>
</div>

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

        const codigo = jsQR(imgData.data, imgData.width, imgData.height);
        if (codigo) {
          // Desactiva cámara
          video.srcObject.getTracks().forEach(t => t.stop());
          enviarAlServidor(codigo.data);
          return;
        }
      }
      requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  });

  // 3. Envía el contenido al servlet vía POST
  function enviarAlServidor(qrData) {
    msg.innerHTML = '<span class="text-info">QR detectado: ' + qrData + ' → Validando…</span>';
    fetch('EscanearQR', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ qr: qrData })
    })
    .then(r => r.json())
    .then(json => {
      if (json.success) {
        msg.innerHTML = '<span class="text-success">Acceso permitido: ' + json.nombre + '</span>';
      } else {
        msg.innerHTML = '<span class="text-danger">ERROR: ' + json.message + '</span>';
      }
    })
    .catch(err => { msg.innerHTML = '<span class="text-warning">Error de red: ' + err + '</span>'; });
  }
</script>
</body>
</html>


