package br.local.busca;
import java.util.Locale;
public final class ThoughtOutput {
    private ThoughtOutput(){}
    private static final String[] TAGS={"<think>","</think>","<thinking>","</thinking>","<analysis>","</analysis>"};
    public static final class Parts {
        public final String answer,thought; public final boolean thinking;
        Parts(String a,String t,boolean in){answer=a;thought=t;thinking=in;}
    }
    public static Parts split(String raw,boolean done){
        String lower=raw.toLowerCase(Locale.ROOT);
        StringBuilder answer=new StringBuilder(),thought=new StringBuilder();
        boolean inside=false;
        int i=0;
        while(i<raw.length()){
            String match=null;
            if(raw.charAt(i)=='<'){
                for(String tag:TAGS)if(lower.startsWith(tag,i)){match=tag;break;}
                if(match==null){
                    String tail=lower.substring(i);
                    boolean partial=false;
                    for(String tag:TAGS)if(tag.startsWith(tail)){partial=true;break;}
                    if(partial)break;
                }
            }
            if(match!=null){
                if(match.startsWith("</")){
                    if(!inside && thought.length()==0 && answer.length()>0){thought.append(answer);answer.setLength(0);}
                    inside=false;
                }else inside=true;
                i+=match.length();continue;
            }
            (inside?thought:answer).append(raw.charAt(i++));
        }
        return new Parts(answer.toString().trim(),thought.toString().trim(),inside&&!done);
    }
}