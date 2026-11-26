package br.local.busca;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
public final class Policy {
    private Policy() {}
    public static URI search(String query) {
        if (query == null || query.trim().isEmpty() || query.length() > 500) throw new IllegalArgumentException("Consulta inválida");
        return URI.create("https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
    }
    public static URI publicUrl(String raw) {
        URI u = URI.create(raw).normalize();
        String h = u.getHost();
        if (!"https".equalsIgnoreCase(u.getScheme()) || h == null || u.getUserInfo() != null || (u.getPort() != -1 && u.getPort() != 443)) throw new IllegalArgumentException("URL não permitida");
        h = h.toLowerCase(Locale.ROOT);
        if (!h.matches("[a-z0-9-]+(\\.[a-z0-9-]+)+") || !h.matches(".*\\.[a-z][a-z0-9-]*") || h.endsWith(".local") || h.endsWith(".localhost") || h.endsWith(".internal") || h.endsWith(".home") || h.endsWith(".lan") || h.endsWith(".onion")) throw new IllegalArgumentException("Host não permitido");
        return URI.create(u.toASCIIString().split("#", 2)[0]);
    }
    public static URI result(String raw) {
        URI u = URI.create("https://html.duckduckgo.com/").resolve(raw);
        String h = u.getHost();
        if (h != null && (h.equalsIgnoreCase("duckduckgo.com") || h.toLowerCase(Locale.ROOT).endsWith(".duckduckgo.com")) && "/l/".equals(u.getPath()) && u.getRawQuery() != null) {
            for (String p : u.getRawQuery().split("&")) if (p.startsWith("uddg=")) return publicUrl(URLDecoder.decode(p.substring(5), StandardCharsets.UTF_8));
            throw new IllegalArgumentException("Redirecionamento sem destino");
        }
        return publicUrl(u.toString());
    }
    public static boolean duckDomain(URI u) {
        String h = u.getHost().toLowerCase(Locale.ROOT);
        return h.equals("duckduckgo.com") || h.endsWith(".duckduckgo.com");
    }
}