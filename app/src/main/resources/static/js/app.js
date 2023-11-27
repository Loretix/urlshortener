$(document).ready(function () {

    $("#shortener").submit(function (event) {
        event.preventDefault();
        var generateQr = $("#qr").prop("checked");

        $.ajax({
            type: "POST",
            url: "/api/link",
            data: $(this).serialize() + "&qr=" + generateQr,
            success: function (msg, status, request) {
                console.log(msg);
                console.log(status);
                console.log(request);
                if(msg.properties.qr != ""){
                    $("#result").html(
                    "<div class='alert alert-success lead'><a target='_blank' href='"
                    + request.getResponseHeader('Location')
                    + "'>"
                    + request.getResponseHeader('Location')
                    + "</a></div>"
                    + "<div><img src='" + msg.properties.qr + "' alt='QR Code' width='200' height='200'/></div>");

                    // Muestra la etiqueta img con el código QR
                    $("#qrimg").attr("src", msg.properties.qr).show();
                } else{
                    $("#result").html(
                    "<div class='alert alert-success lead'><a target='_blank' href='"
                    + request.getResponseHeader('Location')
                    + "'>"
                    + request.getResponseHeader('Location')
                    + "</a></div>");
                }
                
            },
            error: function () {
                $("#result").html(
                    "<div class='alert alert-danger lead'>ERROR</div>");
            }
        });
    });

     // Manejar el formulario para procesar archivos CSV
     $("#csvForm").submit(function (event) {
        event.preventDefault();
        
        var fileInput = document.getElementById('csvFile');
        var file = fileInput.files[0];

        var formData = new FormData($(this)[0]);

        $.ajax({
            type: "POST",
            url: "/api/csv",
            data: formData,
            processData: false,
            contentType: false,
            success: function (csvContent, status, request) {
                // Manejar la respuesta del servidor para archivos CSV según sea necesario
                console.log(csvContent);
                
                // Configurar el encabezado de la respuesta para indicar la descarga del archivo
                var blob = new Blob([csvContent], { type: "text/csv" });
                var link = document.createElement("a");
                link.href = window.URL.createObjectURL(blob);
                link.download = "urls.csv";
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            },
            error: function () {
                // Manejar errores en la petición para archivos CSV
                console.log("Error handling CSV file");
            }
        });
    });

});
