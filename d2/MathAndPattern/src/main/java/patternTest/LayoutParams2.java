package patternTest;

public class LayoutParams2 {

    public static final int MATCH_PARENT = 1;
    public static int type = 0;
    public static int format = 0;
    public static int flags = 3;
    public static int x = 4;
    public static int y = 5;
    // int width, height는 sc1안에 있다.
    public static final int TYPE_APPLICATION = 2;

    public static class Builder extends SC {

        //		public static final int MATCH_PARENT = 1;
        public static int type = 2;
        public static int format = 0;
        public static int flags = 3;
        public static int x = 4;
        public static int y = 5;
        // int width, height는 sc1안에 있다.
//		public static final int TYPE_APPLICATION = 2;

//		public Builder() {
//			super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//			type = TYPE_APPLICATION;
//			format = PixelFormat.OPAQUE;
//		}

        public Builder(int _type) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            type = _type;
            format = PixelFormat.OPAQUE;
        }

        public Builder flags(int _flags) {
            flags = _flags;
            return this;
        }

        public Builder format(int _format) {
            format = _format;
            return this;
        }

        public Builder wh(int w, int h) {
            width = w;
            height = h;
            return this;
        }

        public Builder xy(int xpos, int ypos) {
            x = xpos;
            y = ypos;
            return this;
        }

        public LayoutParams2 build() {
            return new LayoutParams2(this);
        }

    }

    public LayoutParams2(Builder builder) {
        type = builder.type;
        format = builder.format;
        flags = builder.flags;
        x = builder.x;
        y = builder.y;
    }

}
