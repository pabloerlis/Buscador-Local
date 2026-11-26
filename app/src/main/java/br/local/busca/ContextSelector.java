package br.local.busca;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public final class ContextSelector {
    private ContextSelector() {}
    public static final class Source {
        public final long id;
        public final String title, text, kind;
        public Source(long id, String title, String text, String kind) {
            this.id=id; this.title=title; this.text=text; this.kind=kind;
        }
    }
    private static final Set<String> STOP = new HashSet<>(Arrays.asList("que","qual","quais","como","para","com","dos","das","uma","isso","esse","essa","este","esta","sobre","mais","por","sao","the","and","what","this","that","for","you","pode","ser","ele","ela","eles","elas"));
    private static Set<String> terms(String s) {
        String normalized=Normalizer.normalize(s.toLowerCase(Locale.ROOT),Normalizer.Form.NFD).replaceAll("\\p{M}","");
        Set<String> out=new HashSet<>();
        for(String t:normalized.split("[^\\p{L}\\p{N}]+")) if(t.length()>2&&!STOP.contains(t)) out.add(t);
        return out;
    }
    private static final class Candidate {
        final Source source; final String chunk; final int score;
        Candidate(Source s,String c,int score){source=s;chunk=c;this.score=score;}
    }
    public static String select(List<Source> sources,String question,String previous,int budget) {
        if(budget<=0) return "";
        Set<String> current=terms(question), prior=terms(previous);
        List<Candidate> candidates=new ArrayList<>();
        for(Source s:sources){
            String raw=s.text;
            for(int start=0;start<raw.length();start+=650){
                String chunk=raw.substring(start,Math.min(raw.length(),start+800)).trim();
                if(chunk.isEmpty())continue;
                Set<String> words=terms(s.title+" "+chunk);
                int score=0;
                for(String term:current)if(words.contains(term))score+=8;
                for(String term:prior)if(words.contains(term))score+=2;
                if("page".equals(s.kind))score+=2;
                candidates.add(new Candidate(s,chunk,score));
            }
        }
        candidates.sort(Comparator.comparingInt((Candidate c)->c.score).reversed().thenComparing(Comparator.comparingLong((Candidate c)->c.source.id).reversed()));
        StringBuilder b=new StringBuilder();
        Map<Long,Integer> used=new HashMap<>();
        for(Candidate c:candidates){
            if(used.getOrDefault(c.source.id,0)>=2)continue;
            String title=c.source.title.substring(0,Math.min(120,c.source.title.length()));
            String header="["+c.source.id+"] "+("page".equals(c.source.kind)?"PÁGINA LIDA":"TRECHO DO BUSCADOR (não verificado)")+" — "+title+"\n";
            int remaining=budget-b.length();
            if(remaining<header.length()+80)continue;
            b.append(header).append(c.chunk,0,Math.min(c.chunk.length(),remaining-header.length()-2)).append("\n\n");
            used.put(c.source.id,used.getOrDefault(c.source.id,0)+1);
            if(b.length()>=budget-80)break;
        }
        return b.toString();
    }
}