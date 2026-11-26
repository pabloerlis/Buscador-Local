package br.local.busca;
public final class KeyboardLayout {
    private KeyboardLayout(){}
    public static int bottomPadding(int rootBottom,int windowBottom,int systemBottom,int imeBottom,int visibleBottom,int threshold){
        int safeBottom=windowBottom-Math.max(Math.max(0,systemBottom),Math.max(0,imeBottom));
        if(visibleBottom>0 && windowBottom-visibleBottom>Math.max(threshold,systemBottom))safeBottom=Math.min(safeBottom,visibleBottom);
        return Math.max(0,rootBottom-safeBottom);
    }
}
