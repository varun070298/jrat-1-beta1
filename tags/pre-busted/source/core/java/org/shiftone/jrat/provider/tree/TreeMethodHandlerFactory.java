package org.shiftone.jrat.provider.tree;


import org.shiftone.jrat.core.MethodKey;
import org.shiftone.jrat.core.spi.AbstractMethodHandlerFactory;
import org.shiftone.jrat.core.spi.MethodHandler;
import org.shiftone.jrat.core.spi.RuntimeContext;
import org.shiftone.jrat.provider.tree.command.DumpOutputCommandlet;
import org.shiftone.jrat.provider.tree.command.ResetCommandlet;
import org.shiftone.jrat.provider.tree.command.WriteOutputCommandlet;
import org.shiftone.jrat.provider.tree.ui.TraceViewBuilder;
import org.shiftone.jrat.util.AtomicLong;
import org.shiftone.jrat.util.log.Logger;

import java.util.HashSet;
import java.util.Set;


/**
 * Class TreeMethodHandlerFactory
 *
 * @author jeff@shiftone.org (Jeff Drost)
 */
public class TreeMethodHandlerFactory extends AbstractMethodHandlerFactory implements TreeMethodHandlerFactoryMBean {

    private static final Logger LOG = Logger.getLogger(TreeMethodHandlerFactory.class);
    private final TreeNode rootNode = new TreeNode();
    private final Set allMethodKeys = new HashSet();
    private final DelegateThreadLocal delegateThreadLocal = new DelegateThreadLocal(this);
    private final AtomicLong methodHandlerCount = new AtomicLong();

    public void startup(RuntimeContext context) throws Exception {
        super.startup(context);
        context.registerMBean(this);
        context.register(new ResetCommandlet(this));
        context.register(new WriteOutputCommandlet(this));
        context.register(new DumpOutputCommandlet(this));
    }


    public synchronized final MethodHandler createMethodHandler(MethodKey methodKey) {


        methodHandlerCount.incrementAndGet();
        allMethodKeys.add(methodKey);

        return new TreeMethodHandler(this, methodKey);
    }


    public long getMethodHandlerCount() {
        return methodHandlerCount.get();
    }


    /**
     * Returns the current thread's delegate instance. This delegate will
     * operate on this factory's call tree data structure when events are
     * processed.
     */
    public final Delegate getDelegate() {
        return (Delegate) delegateThreadLocal.get();
    }


    public final TreeNode getRootNode() {
        return rootNode;
    }


    public synchronized void writeOutputFile() {
        writeOutputFile(getOutputFile());
    }

    public synchronized void reset() {
        rootNode.reset();
    }

    public synchronized void writeOutputFile(String fileName) {

        LOG.info("writeOutputFile...");

        getContext().writeSerializable(fileName,
                new TraceViewBuilder(
                        rootNode,
                        new HashSet(allMethodKeys), // copy to avoid sync issues
                        getContext().getStartTimeMs(),
                        System.currentTimeMillis(),
                        getContext().getSystemPropertiesAtStartup(),
                        getContext().getHostName(),
                        getContext().getHostAddress()
                )
        );

    }


    public synchronized void shutdown() {

        LOG.info("shutdown...");
        writeOutputFile();
        LOG.info("shutdown complete");
    }


    public String toString() {
        return "Tree Handler Factory";
    }
}
