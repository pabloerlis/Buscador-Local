import br.local.busca.ToolCallParser;
public class ToolCallParserTest{
 static int n;static void ok(boolean b){if(!b)throw new AssertionError("test "+n);n++;}
 static String call(String name,String q){return "<tool_call><function="+name+"><parameter=query>"+q+"</parameter></function></tool_call>";}
 public static void main(String[] a){
  var c=ToolCallParser.parse(call("search_web","notícias atuais"));ok(c.kind==ToolCallParser.Kind.VALID);ok(c.query.equals("notícias atuais"));
  ok(ToolCallParser.parse(call("search_sources","teste")).kind==ToolCallParser.Kind.VALID);
  ok(ToolCallParser.parse("olá").kind==ToolCallParser.Kind.NONE);
  ok(ToolCallParser.parse(call("shell","teste")).kind==ToolCallParser.Kind.INVALID);
  ok(ToolCallParser.parse(call("search_web","a")+call("search_web","b")).kind==ToolCallParser.Kind.INVALID);
  ok(ToolCallParser.parse("<tool_call>").kind==ToolCallParser.Kind.INVALID);
  ok(ToolCallParser.parse("<think>"+call("search_web","a")+"</think>Olá").kind==ToolCallParser.Kind.NONE);
  ok(ToolCallParser.parse(call("search_web","x".repeat(301))).kind==ToolCallParser.Kind.INVALID);
  ok(ToolCallParser.hide("Resposta<tool_ca").equals("Resposta"));
  ok(ToolCallParser.hide("Resposta"+call("search_web","a")).equals("Resposta"));
  System.out.println(n+" verificações de chamadas de ferramentas passaram.");
 }
}