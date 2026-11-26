package br.local.busca
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException
import java.net.*
import java.util.concurrent.TimeUnit

internal data class Hit(val title: String, val url: URI, val snippet: String)
internal data class Evidence(val hit: Hit, val passage: String)
internal class Research(port: Int) {
    init { require(port in 1..65535) }
    private val client = OkHttpClient.Builder()
        .proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress("127.0.0.1", port)))
        .dns(object : Dns { override fun lookup(hostname: String): List<InetAddress> = throw UnknownHostException("DNS local desativado") })
        .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
        .cookieJar(CookieJar.NO_COOKIES).connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS).callTimeout(40, TimeUnit.SECONDS).build()

    private fun fetch(start: URI, searchOnly: Boolean): Pair<URI, String> {
        var u = Policy.publicUrl(start.toString())
        repeat(4) {
            if (searchOnly && !Policy.duckDomain(u)) throw IOException("Redirecionamento externo bloqueado")
            val req = Request.Builder().url(u.toASCIIString()).header("User-Agent", "BuscaLocal/0.1").header("Accept", "text/html,application/xhtml+xml,text/plain").build()
            client.newCall(req).execute().use { r ->
                if (r.code in listOf(301,302,303,307,308)) {
                    u = Policy.publicUrl(u.resolve(r.header("Location") ?: throw IOException("Redirecionamento inválido")).toString())
                    return@use
                }
                if (!r.isSuccessful) throw IOException("HTTP "+r.code+"; sem tentativa de contornar bloqueios")
                val body = r.body ?: throw IOException("Resposta vazia")
                val mime = body.contentType()?.let { it.type+"/"+it.subtype }
                if (mime !in listOf("text/html", "application/xhtml+xml", "text/plain")) throw IOException("Tipo de conteúdo não suportado")
                val out = java.io.ByteArrayOutputStream()
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val n = input.read(buffer); if (n < 0) break
                        if (out.size() + n > 1_500_000) throw IOException("Página excede 1,5 MB")
                        out.write(buffer, 0, n)
                    }
                }
                return u to out.toString(body.contentType()?.charset(Charsets.UTF_8)?.name() ?: "UTF-8")
            }
        }
        throw IOException("Limite de redirecionamentos")
    }
    fun search(query: String): List<Hit> {
        val (url, html) = fetch(Policy.search(query), true)
        val doc = Jsoup.parse(html, url.toString())
        if (doc.select("form#challenge-form, .anomaly-modal, #anomaly-modal").isNotEmpty()) throw IOException("DuckDuckGo solicitou verificação; busca interrompida")
        val hits = doc.select(".result").mapNotNull { e ->
            val a = e.selectFirst("a.result__a") ?: return@mapNotNull null
            runCatching { Hit(a.text(), Policy.result(a.attr("href")), e.selectFirst(".result__snippet")?.text().orEmpty()) }.getOrNull()
        }.distinctBy { it.url }.take(10)
        if (hits.isEmpty()) throw IOException("Nenhum resultado extraível")
        return hits
    }
    fun evidence(hit: Hit, query: String): Evidence {
        val (finalUrl, html) = fetch(hit.url, false)
        val doc = Jsoup.parse(html, finalUrl.toString())
        doc.select("script,style,nav,footer,header,aside,form,noscript,iframe").remove()
        val root = doc.selectFirst("article,main,[role=main]") ?: doc.body()
        val paras = root.select("p,li,h1,h2,h3").map { it.text() }.filter { it.length >= 35 }.distinct()
        val passage = (if (paras.isEmpty()) root.text() else paras.joinToString("\n")).take(24000)
        if (passage.isBlank()) throw IOException("Sem texto extraído")
        return Evidence(hit.copy(url = finalUrl), passage)
    }
    fun close() { client.dispatcher.cancelAll(); client.connectionPool.evictAll() }
}