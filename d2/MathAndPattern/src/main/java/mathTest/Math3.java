package mathTest;

public class Math3 {

    //	int left, top, right, bottom;
    int a1, a2, a3, a4, b1, b2, b3, b4, c1, c2, c3, c4;

    public int temp1(int frame, int layoutFrame, int insetFrame, int displayFrame) {
        int left = Math.max(0, insetFrame + Math.max(layoutFrame, displayFrame));
        int top = Math.max(0, insetFrame + Math.max(layoutFrame, displayFrame));
        int right = Math.max(0, Math.min(layoutFrame, displayFrame) + insetFrame);
        int bottom = Math.max(0, Math.min(layoutFrame, displayFrame) - insetFrame);
//		System.out.printf("%d\n",left);
        return left + top + right + bottom;
    }

    public void temp2() {

        a1 = (mContentFrame.left > mFrame.left ? mContentFrame.left : mFrame.left);
        a2 = (mContentFrame.top > mFrame.top ? mContentFrame.top : mFrame.top);
        a3 = (mContentFrame.right < mFrame.right ? mContentFrame.right : mFrame.right);
        a4 = (mContentFrame.bottom < mFrame.bottom ? mContentFrame.bottom : mFrame.bottom);

        b1 = (mVisibleFrame.left > mFrame.left ? mVisibleFrame.left : mFrame.left);
        b2 = (mVisibleFrame.top > mFrame.top ? mVisibleFrame.top : mFrame.top);
        b3 = (mVisibleFrame.right < mFrame.right ? mVisibleFrame.right : mFrame.right);
        b4 = (mVisibleFrame.bottom < mFrame.bottom ? mVisibleFrame.bottom : mFrame.bottom);

        c1 = (mStableFrame.left > mFrame.left ? mStableFrame.left : mFrame.left);
        c2 = (mStableFrame.top > mFrame.top ? mStableFrame.top : mFrame.top);
        c3 = (mStableFrame.right < mFrame.right ? mStableFrame.right : mFrame.right);
        c4 = (mStableFrame.bottom < mFrame.bottom ? mStableFrame.bottom : mFrame.bottom);
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
