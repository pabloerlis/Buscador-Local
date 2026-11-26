import br.local.busca.Policy;
public class PolicyTest {
    static int count;
    static void eq(String got,String expected) { if(!got.equals(expected)) throw new AssertionError(got); count++; }
    static void bad(String url) { try { Policy.publicUrl(url); throw new AssertionError(url); } catch(IllegalArgumentException expected) { count++; } }
    public static void main(String[] args) {
        eq(Policy.search(" IA & Tor ").toString(),"https://html.duckduckgo.com/html/?q=IA+%26+Tor");
        eq(Policy.result("//duckduckgo.com/l/?uddg=https%3A%2F%2Fexample.org%2Fa%3Fx%3D1").toString(),"https://example.org/a?x=1");
        eq(Policy.publicUrl("https://example.org/a#b").toString(),"https://example.org/a");
        bad("http://example.org"); bad("file:///tmp/a"); bad("https://127.0.0.1/"); bad("https://[::1]/"); bad("https://localhost/"); bad("https://device.local/"); bad("https://foo.internal/"); bad("https://foo.onion/"); bad("https://user@example.org/"); bad("https://example.org:8080/"); bad("https://2130706433/");
        if(Policy.duckDomain(Policy.publicUrl("https://duckduckgo.com.example.org"))) throw new AssertionError(); count++;
        if(!Policy.duckDomain(Policy.publicUrl("https://html.duckduckgo.com"))) throw new AssertionError(); count++;
        System.out.println(count+" verificações passaram.");
    }
}