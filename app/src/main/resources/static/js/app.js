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
                $("#result").html(
                    "<div class='alert alert-success lead'><a target='_blank' href='"
                    + request.getResponseHeader('Location')
                    + "'>"
                    + request.getResponseHeader('Location')
                    + "</a></div>"
                    + "<div><img src='" + msg.properties.qr + "' alt='QR Code' width='200' height='200'/></div>");

                // Muestra la etiqueta img con el código QR
                $("#qrimg").attr("src", msg.properties.qr).show();
            },
            error: function () {
                $("#result").html(
                    "<div class='alert alert-danger lead'>ERROR</div>");
            }
        });
    });
});
