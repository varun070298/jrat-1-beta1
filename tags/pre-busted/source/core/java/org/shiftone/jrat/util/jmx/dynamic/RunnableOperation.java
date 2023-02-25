package org.shiftone.jrat.util.jmx.dynamic;


/**
 * @author jeff@shiftone.org (Jeff Drost)
 */
public abstract class RunnableOperation implements Operation, Runnable {

    public Object invoke(Object params[]) {

        run();

        return null;
    }


    public String getReturnType() {
        return null;
    }


    public int getParameterCount() {
        return 0;
    }


    public String getParameterName(int index) {
        return null;
    }


    public String getParameterType(int index) {
        return null;
    }


    public String getParameterDescription(int index) {
        return null;
    }
}
