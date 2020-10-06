package mathTest;


import mathTest.data.ContentFrame;
import mathTest.data.Frame;
import mathTest.data.StableFrame;
import mathTest.data.VisibleFrame;

public class MathTemp1 {
    int a1, a2, a3, a4, b1, b2, b3, b4, c1, c2, c3, c4;
    //	int left, top, right, bottom;

    public int temp1(int frame, int layoutFrame, int insetFrame, int displayFrame) {
        int left = Math.max(0, insetFrame + Math.max(layoutFrame, displayFrame));
        int top = Math.max(0, insetFrame + Math.max(layoutFrame, displayFrame));
        int right = Math.max(0, Math.min(layoutFrame, displayFrame) + insetFrame);
        int bottom = Math.max(0, Math.min(layoutFrame, displayFrame) - insetFrame);
//		System.out.printf("%d\n",left);
        return left + top + right + bottom;
    }

    public void temp2() {
        a1 = Math.max(ContentFrame.left, Frame.left);
        a2 = Math.max(ContentFrame.top, Frame.top);
        a3 = Math.min(ContentFrame.right, Frame.right);
        a4 = Math.min(ContentFrame.bottom, Frame.bottom);

        b1 = Math.max(VisibleFrame.left, Frame.left);
        b2 = Math.max(VisibleFrame.top, Frame.top);
        b3 = Math.min(VisibleFrame.right, Frame.right);
        b4 = Math.min(VisibleFrame.bottom, Frame.bottom);

        c1 = Math.max(StableFrame.left, Frame.left);
        c2 = Math.max(StableFrame.top, Frame.top);
        c3 = Math.min(StableFrame.right, Frame.right);
        c4 = Math.min(StableFrame.bottom, Frame.bottom);
    }

}
