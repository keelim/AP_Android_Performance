package mathTest;


public class Math {

    //	int left, top, right, bottom;
    int a1, a2, a3, a4, b1, b2, b3, b4, c1, c2, c3, c4;

    public int temp1(int frame, int layoutFrame, int insetFrame, int displayFrame) {
        int left = java.lang.Math.max(0, insetFrame + java.lang.Math.max(layoutFrame, displayFrame));
        int top = java.lang.Math.max(0, insetFrame + java.lang.Math.max(layoutFrame, displayFrame));
        int right = java.lang.Math.max(0, java.lang.Math.min(layoutFrame, displayFrame) + insetFrame);
        int bottom = java.lang.Math.max(0, java.lang.Math.min(layoutFrame, displayFrame) - insetFrame);
//		System.out.printf("%d\n",left);
        return left + top + right + bottom;
    }

    public void temp2() {

        a1 = java.lang.Math.max(mContentFrame.left, mFrame.left);
        a2 = java.lang.Math.max(mContentFrame.top, mFrame.top);
        a3 = java.lang.Math.min(mContentFrame.right, mFrame.right);
        a4 = java.lang.Math.min(mContentFrame.bottom, mFrame.bottom);

        b1 = java.lang.Math.max(mVisibleFrame.left, mFrame.left);
        b2 = java.lang.Math.max(mVisibleFrame.top, mFrame.top);
        b3 = java.lang.Math.min(mVisibleFrame.right, mFrame.right);
        b4 = java.lang.Math.min(mVisibleFrame.bottom, mFrame.bottom);

        c1 = java.lang.Math.max(mStableFrame.left, mFrame.left);
        c2 = java.lang.Math.max(mStableFrame.top, mFrame.top);
        c3 = java.lang.Math.min(mStableFrame.right, mFrame.right);
        c4 = java.lang.Math.min(mStableFrame.bottom, mFrame.bottom);
    }


    static class mContentFrame {
        static final int left = 130;
        static final int top = 93;
        static final int right = 38;
        static final int bottom = 73;
    }

    static class mFrame {
        static final int left = 72;
        static final int top = 82;
        static final int right = 92;
        static final int bottom = 102;
    }

    static class mVisibleFrame {
        static final int left = 160;
        static final int top = 86;
        static final int right = 96;
        static final int bottom = 76;
    }

    static class mStableFrame {
        static final int left = 121;
        static final int top = 122;
        static final int right = 86;
        static final int bottom = 93;
    }
}
