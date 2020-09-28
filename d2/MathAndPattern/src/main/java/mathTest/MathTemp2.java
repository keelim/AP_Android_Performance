package mathTest;

import mathTest.data.ContentFrame;
import mathTest.data.Frame;
import mathTest.data.StableFrame;
import mathTest.data.VisibleFrame;

public class MathTemp2 {

    int a1, a2, a3, a4, b1, b2, b3, b4, c1, c2, c3, c4;

    public int temp1(int frame, int layoutFrame, int insetFrame, int displayFrame) {
        int left = 0 > (insetFrame + (layoutFrame > displayFrame ? layoutFrame : displayFrame)) ? 0
                : (insetFrame + (layoutFrame > displayFrame ? layoutFrame : displayFrame));
        int top = 0 > (insetFrame + (layoutFrame > displayFrame ? layoutFrame : displayFrame)) ? 0
                : (insetFrame + (layoutFrame > displayFrame ? layoutFrame : displayFrame));
        int right = 0 > ((layoutFrame < displayFrame ? layoutFrame : displayFrame) + insetFrame) ? 0
                : ((layoutFrame < displayFrame ? layoutFrame : displayFrame) + insetFrame);
        int bottom = 0 > ((layoutFrame < displayFrame ? layoutFrame : displayFrame) - insetFrame) ? 0
                : ((layoutFrame < displayFrame ? layoutFrame : displayFrame) - insetFrame);

        return left + top + right + bottom;
    }

    public void temp2() {
        a1 = (ContentFrame.left > Frame.left ? ContentFrame.left : Frame.left);
        a2 = (ContentFrame.top > Frame.top ? ContentFrame.top : Frame.top);
        a3 = (ContentFrame.right < Frame.right ? ContentFrame.right : Frame.right);
        a4 = (ContentFrame.bottom < Frame.bottom ? ContentFrame.bottom : Frame.bottom);

        b1 = (VisibleFrame.left > Frame.left ? VisibleFrame.left : Frame.left);
        b2 = (VisibleFrame.top > Frame.top ? VisibleFrame.top : Frame.top);
        b3 = (VisibleFrame.right < Frame.right ? VisibleFrame.right : Frame.right);
        b4 = (VisibleFrame.bottom < Frame.bottom ? VisibleFrame.bottom : Frame.bottom);

        c1 = (StableFrame.left > Frame.left ? StableFrame.left : Frame.left);
        c2 = (StableFrame.top > Frame.top ? StableFrame.top : Frame.top);
        c3 = (StableFrame.right < Frame.right ? StableFrame.right : Frame.right);
        c4 = (StableFrame.bottom < Frame.bottom ? StableFrame.bottom : Frame.bottom);
    }


}
