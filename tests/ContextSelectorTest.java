import br.local.busca.ContextSelector;
import java.util.*;
public class ContextSelectorTest {
    static int n;
    static void check(boolean ok){if(!ok)throw new AssertionError();n++;}
    public static void main(String[] args){
        var list=List.of(new ContextSelector.Source(5,"Bateria","A bateria de lítio dura 12 horas. "+"abc ".repeat(240),"page"),new ContextSelector.Source(9,"Processador","O processador tem oito núcleos. "+"xyz ".repeat(240),"page"),new ContextSelector.Source(12,"Preço","Preço sugerido de 100 reais. "+"def ".repeat(240),"snippet"));
        String a=ContextSelector.select(list,"Quanto dura a bateria de litio?","",1000);
        check(a.startsWith("[5]"));check(a.length()<=1000);
        String b=ContextSelector.select(list,"E quanto tempo?","bateria lítio",1000);
        check(b.startsWith("[5]"));
        String c=ContextSelector.select(list,"Qual o preço?","",1000);
        check(c.startsWith("[12]"));check(c.contains("não verificado"));
        check(ContextSelector.select(list,"processador","",1000).startsWith("[9]"));
        check(ContextSelector.select(list,"","",0).isEmpty());
        check(ContextSelector.select(List.of(),"teste","",1000).isEmpty());
        check(ContextSelector.select(list,"bateria","",50).isEmpty());
        System.out.println(n+" verificações de contexto passaram.");
    }
}