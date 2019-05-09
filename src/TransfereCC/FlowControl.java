package TransfereCC;

public class FlowControl {

    static void adjustFlowControlWindow(MySegment segment, StateTable st){
        st.flow_windowsize = segment.max_window_size;
    }

}
