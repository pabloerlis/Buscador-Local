import br.local.busca.ThoughtOutput;
public class ThoughtOutputTest{
    static int n; static void ok(boolean b){if(!b)throw new AssertionError();n++;}
    public static void main(String[] a){
        var p=ThoughtOutput.split("<think>calcular</think>Resultado",true);ok(p.answer.equals("Resultado"));ok(p.thought.equals("calcular"));ok(!p.thinking);
        p=ThoughtOutput.split("<think>incompleto",false);ok(p.answer.isEmpty());ok(p.thinking);
        p=ThoughtOutput.split("<think>incompleto",true);ok(p.answer.isEmpty());ok(!p.thinking);
        for(String x:new String[]{"<","<t","<th","<thin","<think"})ok(ThoughtOutput.split(x,false).answer.isEmpty());
        p=ThoughtOutput.split("<think>um</thi",false);ok(p.thought.equals("um"));ok(p.answer.isEmpty());
        ok(ThoughtOutput.split("Resposta normal",true).answer.equals("Resposta normal"));
        ok(ThoughtOutput.split("<analysis>a</analysis>b",true).answer.equals("b"));
        ok(ThoughtOutput.split("raciocínio</think>resposta",true).answer.equals("resposta"));
        ok(ThoughtOutput.split("<THINK>a</THINK>b",true).thought.equals("a"));
        ok(ThoughtOutput.split("2 < 3",true).answer.equals("2 < 3"));
        System.out.println(n+" verificações de saída e pensamento passaram.");
    }
}