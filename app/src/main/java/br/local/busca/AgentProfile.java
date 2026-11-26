package br.local.busca;
import java.nio.charset.StandardCharsets;
public enum AgentProfile {
    FAST("Rápido",256,1,500,"Seja breve."),
    BALANCED("Equilibrado",384,2,1400,"Responda com clareza e concisão."),
    DETAIL("Detalhado",640,3,2200,"Explique o necessário com detalhes relevantes.");
    public final String title,instruction;
    public final int outputTokens,maxCalls,historyChars;
    AgentProfile(String t,int o,int m,int h,String i){title=t;outputTokens=o;maxCalls=m;historyChars=h;instruction=i;}
    public static AgentProfile fromName(String name){try{return valueOf(name);}catch(Exception e){return BALANCED;}}
    public static String boundUtf8(String text,int max){
        int i=0,bytes=0;while(i<text.length()){
            int cp=text.codePointAt(i),len=cp<=0x7f?1:cp<=0x7ff?2:cp<=0xffff?3:4;
            if(bytes+len>max)break;bytes+=len;i+=Character.charCount(cp);
        }return text.substring(0,i);
    }
}