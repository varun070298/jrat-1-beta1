package org.shiftone.jrat.provider.tree;


import org.shiftone.jrat.core.Accumulator;
import org.shiftone.jrat.core.MethodKey;
import org.shiftone.jrat.util.log.Logger;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Class StackNode
 *
 * @author Jeff Drost
 */
public class StackNode implements Externalizable {

    private static final Logger LOG = Logger.getLogger(StackNode.class);
    private static final long serialVersionUID = 1;
    protected MethodKey methodKey;
    protected StackNode parent;
    private Accumulator accumulator;
    protected HashMap children = new HashMap(3);


    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeObject(accumulator);
        out.writeObject(methodKey);

        // create a copy of the children
        List list = getChildren();

        // write a child count
        int childCount = list.size();
        out.writeInt(childCount);

        // write the children
        for (int i = 0; i < childCount; i++) {
            StackNode child = (StackNode) list.get(i);
            child.writeExternal(out);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        this.accumulator = (Accumulator) in.readObject();
        this.methodKey = (MethodKey) in.readObject();

        int childCount = in.readInt();
        for (int i = 0; i < childCount; i++) {

            StackNode child = new StackNode();

            child.readExternal(in);

            children.put(child.getMethodKey(), child);
            child.parent = this;
        }

    }

    public StackNode() {

        // root node
        this.methodKey = null;
        this.parent = null;
        this.accumulator = new Accumulator();
    }


    public StackNode(MethodKey methodKey, StackNode treeNode) {
        this.methodKey = methodKey;
        this.parent = treeNode;
        this.accumulator = new Accumulator();
    }

    public List getChildren() {

        List list = new ArrayList();

        synchronized (children) {
            list.addAll(children.values());
        }

        return list;
    }


    /**
     * Method gets <b>AND CREATES IF NEEDED</b> the requested tree node
     */
    public StackNode getChild(MethodKey methodKey) {

        StackNode treeNode = null;

        synchronized (children) {
            treeNode = (StackNode) children.get(methodKey);

            if (treeNode == null) {
                treeNode = new StackNode(methodKey, this);

                children.put(methodKey, treeNode);
            }
        }

        return treeNode;
    }


    public final StackNode getParentNode() {
        return parent;
    }


    public final boolean isRootNode() {
        return (methodKey == null);
    }


    public Accumulator getAccumulator() {
        return accumulator;
    }

    public MethodKey getMethodKey() {
        return methodKey;
    }

    // ---------------------------------------------------------------

    public synchronized void reset() {

        // need to clone map - concurrency issues
        List list = new ArrayList();

        synchronized (children) {
            list.addAll(children.values());
        }

        for (int i = 0; i < list.size(); i++) {
            StackNode treeNode = (StackNode) list.get(i);

            treeNode.reset();
        }

        accumulator.reset();  // this is the actual call to reset
    }


}
