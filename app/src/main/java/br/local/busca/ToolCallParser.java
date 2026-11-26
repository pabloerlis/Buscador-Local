package br.local.busca;
import java.util.regex.*;
public final class ToolCallParser {
    public enum Kind{NONE,VALID,INVALID}
    public static final class Call{
        public final Kind kind;public final String name,query;
        Call(Kind k,String n,String q){kind=k;name=n;query=q;}
    }
    private static final Pattern BLOCK=Pattern.compile("<tool_call>\\s*<function=([a-z][a-z0-9_]{0,63})>\\s*<parameter=query>\\s*([^<>]{1,300}?)\\s*</parameter>\\s*</function>\\s*</tool_call>",Pattern.DOTALL);
    public static Call parse(String raw){
        // Calls in marked thinking are not executable.
        String text=ThoughtOutput.split(raw,true).answer;
        if(!text.contains("<tool_call")&&!text.contains("</tool_call>"))return new Call(Kind.NONE,"","");
        if(text.length()>16384)return new Call(Kind.INVALID,"","");
        Matcher m=BLOCK.matcher(text);
        if(!m.find())return new Call(Kind.INVALID,"","");
        String name=m.group(1),q=m.group(2).trim();
        String rest=text.substring(0,m.start())+text.substring(m.end());
        if(rest.contains("<tool_call")||rest.contains("</tool_call>")||q.isEmpty()||!(name.equals("search_web")||name.equals("search_sources")))return new Call(Kind.INVALID,"","");
        return new Call(Kind.VALID,name,q);
    }
    public static String hide(String raw){
        int start=raw.indexOf("<tool_call");
        if(start<0){String tag="<tool_call>";for(int len=Math.min(tag.length()-1,raw.length());len>0;len--)if(raw.endsWith(tag.substring(0,len))){start=raw.length()-len;break;}}
        return start<0?raw:raw.substring(0,start);
    }
}