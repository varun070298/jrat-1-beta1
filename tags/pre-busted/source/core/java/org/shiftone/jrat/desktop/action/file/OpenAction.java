package org.shiftone.jrat.desktop.action.file;

import org.shiftone.jrat.core.spi.ViewBuilder;
import org.shiftone.jrat.desktop.DesktopFrame;
import org.shiftone.jrat.desktop.DesktopPreferences;
import org.shiftone.jrat.desktop.util.Errors;
import org.shiftone.jrat.util.io.IOUtil;
import org.shiftone.jrat.util.log.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;


/**
 * @author jeff@shiftone.org (Jeff Drost)
 */
public class OpenAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(OpenAction.class);
    private final DesktopFrame desktopFrame;


    public OpenAction(DesktopFrame desktopFrame) {
        super("Open");

        this.desktopFrame = desktopFrame;
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
    }

    private File getCurrentDirectory() {
        File file = DesktopPreferences.getLastOpenedFile();

        return (file != null) ? IOUtil.getNearestExistingParent(file) : new File("");
    }

    private File getSelectedFile() {
        File file = DesktopPreferences.getLastOpenedFile();
        return (file != null && file.exists()) ? file : null;
    }

    private File getParent(File file) {
        File parent = file.getParentFile();
        if (parent.exists()) {
            return parent;
        } else {
            return getParent(parent);
        }
    }

    public void actionPerformed(ActionEvent e) {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open JRat Output File");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setCurrentDirectory(getCurrentDirectory());
        chooser.setSelectedFile(getSelectedFile());
        chooser.addChoosableFileFilter(JRatFileFilter.INSTANCE);
        //chooser.addChoosableFileFilter(SessionFileFilter.INSTANCE);

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(desktopFrame)) {
            openFiles(chooser.getSelectedFiles());
        }
    }


    private void openFiles(File[] files) {
        for (int i = 0; i < files.length; i++) {
            openFile(files[i]);
        }
    }

    private void openFile(File file) {
        DesktopPreferences.setLastOpenedFile(file);
        new Thread(new OpenRunnable(file)).start();
    }


    private class OpenRunnable implements Runnable {
        private final File file;

        public OpenRunnable(File file) {
            this.file = file;
        }

        public void run() {
            open(file.getName(), IOUtil.openInputStream(file));
        }
    }

    public void open(String name, InputStream inputStream) {

        try {
            desktopFrame.waitCursor();

            inputStream = new GZIPInputStream(inputStream);

            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);


            ViewBuilder viewBuilder = (ViewBuilder) objectInputStream.readObject();

            JComponent component = viewBuilder.buildView(objectInputStream);

            desktopFrame.createView(name, component);

        } catch (OutOfMemoryError e) {

            Errors.showError(desktopFrame, e, "Out of Memory!  Use the -Xmx Java option.");

        } catch (Exception e) {

            Errors.showError(desktopFrame, e, "Failed to open : " + name);

        } finally {

            desktopFrame.unwaitCursor();
            IOUtil.close(inputStream);

        }
    }

    private static class JRatFileFilter extends FileFilter {

        static FileFilter INSTANCE = new JRatFileFilter();

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return (f.getName().endsWith(".jrat"));
        }

        public String getDescription() {
            return "JRat Data File (*.jrat)";
        }
    }
}
