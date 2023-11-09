@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

/**
 * Given an url returns a QR 
 *
 */
interface CreateQrUseCase {
    fun generate(url: String, id: String)
    fun get(id: String): ByteArray
}

/**
 *  Implementation of [CreateQrUseCase].
 */
class CreateQrUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val qrService: QrService,
    private val qrRepository: HashMap<String, ByteArray>
) : CreateQrUseCase {
    // Create a QR code from a given url
    override fun generate(url: String, id: String) {

        // SI su existe quiero que se genere el QR y se guarde en su.properties.qr

        val su = shortUrlRepository.findByKey(id)

        if( su != null ){
            println("COJO EL IDDD " + id + url)
            //properties=ShortUrlProperties(ip=127.0.0.1, sponsor=null, safe=true, owner=null, country=null, qr=null))
            //shortUrlRepository.save(it.copy(properties = it.properties.copy(qrGenerated = qrGenerated.toString())))
            val qrGenerated = qrService.generateQr(url)
            qrRepository.put(id, qrGenerated)
        } else {
            throw RedirectionNotFound(id)
        } 
    }

    // Get the QR code from a given id
    override fun get(id: String): ByteArray {
        println("COJO EL IDDD gett" + id)
        val su = shortUrlRepository.findByKey(id)
        if (su != null){
                if (su.properties.qr == true ) {
                    //println("QR: " + it.properties.qrGenerated)
                    //it.properties.qrGenerated.toByteArray()
                   return qrRepository.get(id)!!
                } else {
                    throw RedirectionNotFound(id)
                }
        } else{
            throw RedirectionNotFound(id)
        }

    }
            
    
        
}
