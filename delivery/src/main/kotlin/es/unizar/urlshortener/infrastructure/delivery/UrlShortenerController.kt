package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import es.unizar.urlshortener.core.usecases.CreateQrUseCase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import java.net.URI


/**
 * The specification of the controller.
 */
interface UrlShortenerController {

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<Unit>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    /**
     * Generates a qr code for the short url identified by its [id].
     */
    fun qr(id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource>
}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null,
    val qr: Boolean
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
    val properties: Map<String, Any> = emptyMap()
)

/**
 * The implementation of the controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@RestController
class UrlShortenerControllerImpl(
    val redirectUseCase: RedirectUseCase,
    val logClickUseCase: LogClickUseCase,
    val createShortUrlUseCase: CreateShortUrlUseCase,
    val createQrUseCase: CreateQrUseCase
) : UrlShortenerController {

    @GetMapping("/{id:(?!api|index).*}")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Unit> =
        redirectUseCase.redirectTo(id).let {
            logClickUseCase.logClick(id, ClickProperties(ip = request.remoteAddr))
            val h = HttpHeaders()
            h.location = URI.create(it.target)
            println("status: " + it.mode)
            ResponseEntity<Unit>(h, HttpStatus.valueOf(it.mode))
        }

    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
    
        createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr,
                sponsor = data.sponsor,
                qr = data.qr
            )
        ).let {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
            h.location = url
            val qr: URI
            // Generar el código QR y asociarlo con la ShortUrl
            if (data.qr){
                createQrUseCase.generate(data.url, it.hash)
                qr = linkTo<UrlShortenerControllerImpl> { qr(it.hash, request) }.toUri()
            } else {
                // NO hay uri
                qr = URI("")
            }

            
            val response = ShortUrlDataOut(
                url = url,
                properties = mapOf(
                    "safe" to it.properties.safe,
                    "qr" to qr
                )   
            )

            ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
        }

    /*
     *  Get the QR code of a short url identified by its [id].
     */  

    @GetMapping("/{id:(?!api|index).*}/qr")
    override fun qr(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource> {

        // Si el id no existe en la base de datos se devolverá una respuesta de tipo 404 --> comprobado en el caso de uso

        //if (/* la url se esta validando */) {
        /* val retryAfter = 60 // Calcula el tiempo de espera necesario
            val h = HttpHeaders()
            h.set(HttpHeaders.RETRY_AFTER, retryAfter.toString())
            return ResponseEntity(h, HttpStatus.BAD_REQUEST)
        }*/

        //if (/* url exist but is invalid */) {
            // return estado 403 forbbiden
        /*  return ResponseEntity(HttpStatus.FORBIDDEN)
        }*/
        
        // Devolver el QR 
        val h = HttpHeaders()
        h.set(HttpHeaders.CONTENT_TYPE, IMAGE_PNG_VALUE)
        return ResponseEntity<ByteArrayResource>(ByteArrayResource(createQrUseCase.get(id) , 
        IMAGE_PNG_VALUE), h, HttpStatus.OK)
    }

    
}
