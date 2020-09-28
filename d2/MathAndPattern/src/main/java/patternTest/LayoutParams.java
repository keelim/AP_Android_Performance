package patternTest;

public class LayoutParams extends SC {

    public static final int MATCH_PARENT = 1;
    public static int type = 0;
    public static int format = 0;
    public static int flags = 3;
    public static int x = 4;
    public static int y = 5;
    // int width, height는 sc1안에 있다.

    public static final int TYPE_APPLICATION = 2;

    public LayoutParams() {
        super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        type = TYPE_APPLICATION;
        format = PixelFormat.OPAQUE;
    }

    public LayoutParams(int _type) {
        super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        type = _type;
        format = PixelFormat.OPAQUE;
    }

    public LayoutParams(int _type, int _flags) {
        super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        type = _type;
        flags = _flags;
        format = PixelFormat.OPAQUE;
    }

    public LayoutParams(int _type, int _flags, int _format) {
        super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        type = _type;
        flags = _flags;
        format = _format;
    }

    public LayoutParams(int w, int h, int _type, int _flags, int _format) {
        super(w, h);
        type = _type;
        flags = _flags;
        format = _format;
    }

    public LayoutParams(int w, int h, int xpos, int ypos, int _type, int _flags, int _format) {
        super(w, h);
        x = xpos;
        y = ypos;
        type = _type;
        flags = _flags;
        format = _format;
    }

}
