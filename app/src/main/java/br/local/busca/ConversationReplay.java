package br.local.busca;
import java.util.*;
public final class ConversationReplay {
    private ConversationReplay(){}
    public static final class Turn{
        public final String role,body;
        public Turn(String r,String b){role=r;body=b;}
    }
    public static List<Turn> recent(List<Turn> all,int budget){
        List<Turn> clean=new ArrayList<>();
        for(Turn t:all){
            if(!t.role.equals("user")&&!t.role.equals("assistant"))continue;
            String body=t.role.equals("assistant")?ThoughtOutput.split(ToolCallParser.hide(t.body),true).answer:t.body;
            if(body.isBlank())continue;
            if(!clean.isEmpty()&&clean.get(clean.size()-1).role.equals(t.role)){
                Turn prev=clean.remove(clean.size()-1);
                body=prev.body+"\n"+body;
            }
            clean.add(new Turn(t.role,body));
        }
        List<Turn> out=new ArrayList<>();int remain=budget;
        // Only retain completed user/assistant pairs, newest first. A dangling prior user is not replayed.
        int i=clean.size()-1;
        if(i>=0&&clean.get(i).role.equals("user"))i--;
        while(i>=1&&remain>=100){
            Turn answer=clean.get(i),user=clean.get(i-1);
            if(!answer.role.equals("assistant")||!user.role.equals("user"))break;
            int ub=Math.min(user.body.length(),Math.min(500,remain/2));
            int ab=Math.min(answer.body.length(),remain-ub);
            if(ub==0||ab==0)break;
            out.add(0,new Turn("assistant",answer.body.substring(0,ab)));
            out.add(0,new Turn("user",user.body.substring(0,ub)));
            remain-=ub+ab;i-=2;
        }
        return out;
    }
}