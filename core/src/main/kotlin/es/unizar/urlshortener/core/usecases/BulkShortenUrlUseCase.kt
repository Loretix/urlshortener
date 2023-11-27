@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader



class BulkShortenUrlUseCase(private val shortUrlRepository: ShortUrlRepositoryService,
                             private val validatorService: ValidatorService,
                             private val hashService: HashService) {
    fun processCsv(csvData: InputStream): List<String> {
    val originalUrls = mutableListOf<String>()
    val shortenedUrls = mutableListOf<String>()

    val useCaseImpl = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

    try {
        val reader = BufferedReader(InputStreamReader(csvData))
        var line: String? = null
        var numero = 0

        while (reader.readLine().also { line = it } != null) {
            val originalUrl = line?.trim()
            
            if (!originalUrl.isNullOrBlank()) {
                val properties = ShortUrlProperties(
                    safe = true,
                    ip = "127.0.0.1",
                    sponsor = null,
                    qr = null
                )

                var urlCorrecion = originalUrl
                if (numero == 0) {
                    urlCorrecion = originalUrl.substring(1)
                    numero++
                }
                val shortUrlResponse = useCaseImpl.create(urlCorrecion, properties)
                val shortenedUrl = shortUrlResponse.hash

                // Agregar las URLs a las listas
                originalUrls.add(originalUrl)
                shortenedUrls.add(shortenedUrl.toString()) 
            } else {
                // Hay que manejar URLs no válidas en el catch
            }
        }
    } catch (e: IOException) {
        println("Error al leer el archivo CSV: ${e.message}")
    }

        // Devolver las URLs acortadas
        return buildCsvResponse(originalUrls, shortenedUrls)
    }

    private fun buildCsvResponse(originalUrls: List<String>, shortenedUrls: List<String>): List<String> {
        // Construir la respuesta en formato CSV
        val csvResponse = mutableListOf<String>()
        for (i in originalUrls.indices) {
            csvResponse.add("${originalUrls[i]};${shortenedUrls[i]}")
        }
        return csvResponse
    }     
}
