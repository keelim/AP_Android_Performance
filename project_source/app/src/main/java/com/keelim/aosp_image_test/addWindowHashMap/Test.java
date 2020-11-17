import java.util.HashMap;
import java.util.function.BiConsumer;

public class Test {
    private HashMap<Integer, BiConsumer<WindowToken, LayoutParams>> hashMap;

    public Test() {
        hashMap = new HashMap<>();
        BiConsumer<WindowToken, LayoutParams> bi = (token, attrs) -> {
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add input method window with unknown token "
                        + attrs.token + ".  Aborting.");
            } else if (token.windowType != TYPE_INPUT_METHOD) {
                Slog.w(TAG_WM, "Attempted to add input method window with bad token "
                        + attrs.token + ".  Aborting.";
            }
        };

        hashMap.put(TYPE_INPUT_METHOD, bi);

        bi = (token, attrs) -> {
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add voice interaction window with unknown token "
                        + attrs.token + ".  Aborting.");
            } else if (token.windowType != TYPE_VOICE_INTERACTION) {
                Slog.w(TAG_WM, "Attempted to add voice interaction window with bad token "
                        + attrs.token + ".  Aborting.");
            }
        };

        hashMap.put(TYPE_VOICE_INTERACTION, bi);

        bi = (token, attrs) -> {
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add input method window with unknown token "
                        + attrs.token + ".  Aborting.");
            } else if (token.windowType != TYPE_WALLPAPER) {
                Slog.w(TAG_WM, "Attempted to add wallpaper window with bad token "
                        + attrs.token + ".  Aborting.");
            }
        };

        hashMap.put(TYPE_WALLPAPER, bi);

        bi = (token, attrs) -> {
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add Dream window with unknown token "
                        + attrs.token + ".  Aborting.");
            } else if (token.windowType != TYPE_DREAM) {
                Slog.w(TAG_WM, "Attempted to add Dream window with bad token "
                        + attrs.token + ".  Aborting.");
            }
        };

        hashMap.put(TYPE_DREAM, bi);

        bi = (token, attrs) -> {
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with unknown token "
                        + attrs.token + ".  Aborting.");
            } else if (token.windowType != TYPE_ACCESSIBILITY_OVERLAY) {
                Slog.w(TAG_WM, "Attempted to add Accessibility overlay window with bad token "
                        + attrs.token + ".  Aborting.");
            }
        };

        hashMap.put(TYPE_ACCESSIBILITY_OVERLAY, bi);

        bi = (token, attrs) -> {
            if (token == null) {
                Slog.w(TAG_WM, "Attempted to add QS dialog window with unknown token "
                        + attrs.token + ".  Aborting.");
            } else if (token.windowType != TYPE_QS_DIALOG) {
                Slog.w(TAG_WM, "Attempted to add QS dialog window with bad token "
                        + attrs.token + ".  Aborting.");
            }
        };

        hashMap.put(TYPE_QS_DIALOG, bi);
    }

    public static void main(String[] args) {
        Test test = new Test();
        BiConsumer<WindowToken, LayoutParams> bi = test.hashMap.get(1);
        if(bi!=null){
            bi.accept(toke, attrs);
            return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
        }
    }


}