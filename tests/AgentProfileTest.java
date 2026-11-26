import br.local.busca.*;
import java.util.*;
public class AgentProfileTest{
 static int n;static void ok(boolean b){if(!b)throw new AssertionError("test "+n);n++;}
 public static void main(String[] a){
  ok(AgentProfile.fromName(null)==AgentProfile.BALANCED);
  ok(AgentProfile.FAST.outputTokens<AgentProfile.DETAIL.outputTokens);
  ok(AgentProfile.boundUtf8("ééé",5).equals("éé"));
  ok(AgentProfile.boundUtf8("x😀y",4).equals("x"));
  var r=ConversationReplay.recent(List.of(new ConversationReplay.Turn("user","Olá"),new ConversationReplay.Turn("activity","segredo"),new ConversationReplay.Turn("assistant","<think>texto interno</think>Resposta"),new ConversationReplay.Turn("user","cancelada")),1000);
  ok(r.size()==2);ok(r.get(0).role.equals("user"));ok(r.get(1).body.equals("Resposta"));
  ok(ConversationReplay.recent(List.of(new ConversationReplay.Turn("assistant","solta")),1000).isEmpty());
  ok(ConversationReplay.recent(List.of(new ConversationReplay.Turn("user","a"),new ConversationReplay.Turn("assistant","b")),50).isEmpty());
  System.out.println(n+" verificações de perfis e histórico passaram.");
 }
}