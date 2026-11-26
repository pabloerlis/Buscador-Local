import br.local.busca.KeyboardLayout;
public class KeyboardLayoutTest {
    static int n;
    static void eq(int expected,int actual){if(expected!=actual)throw new AssertionError(expected+" != "+actual);n++;}
    public static void main(String[] args){
        eq(24,KeyboardLayout.bottomPadding(1000,1000,24,0,976,80));
        eq(320,KeyboardLayout.bottomPadding(1000,1000,24,320,680,80));
        eq(0,KeyboardLayout.bottomPadding(680,1000,24,320,680,80));
        eq(320,KeyboardLayout.bottomPadding(1000,1000,24,0,680,80));
        eq(0,KeyboardLayout.bottomPadding(680,1000,24,0,680,80));
        eq(24,KeyboardLayout.bottomPadding(1000,1000,24,0,0,80));
        eq(100,KeyboardLayout.bottomPadding(1000,1000,24,100,900,80));
        eq(0,KeyboardLayout.bottomPadding(976,1000,24,0,976,80));
        eq(50,KeyboardLayout.bottomPadding(730,1000,24,320,680,80));
        System.out.println(n+" verificações de teclado passaram.");
    }
}