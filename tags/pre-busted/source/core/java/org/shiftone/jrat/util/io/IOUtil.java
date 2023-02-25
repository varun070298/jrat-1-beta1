package org.shiftone.jrat.util.io;


import org.shiftone.jrat.core.JRatException;
import org.shiftone.jrat.util.Assert;
import org.shiftone.jrat.util.log.Logger;

import java.io.*;
import java.net.Socket;


/**
 * Class IOUtil
 * <p/>
 * $astChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 * $HeadURL$
 * $Id$
 * <p/>
 * todo - make sure streams get closed
 */
public class IOUtil {

    private static final Logger LOG = Logger.getLogger(IOUtil.class);
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final int MAX_HEURISTIC_BUFFER_SIZE = 1024 * 16;

    public static void delete(File file) {

        Assert.assertNotNull("file", file);
        LOG.debug("delete(" + file + ")");

        if (!file.delete()) {
            if (file.exists()) {
                throw new JRatException("unable to delete file : " + file.getAbsolutePath());
            } else {
                throw new JRatException("unable to delete non-existant file : " + file.getAbsolutePath());
            }
        }
    }

    public static boolean createNewFile(File file) {

        try {
            return file.createNewFile();
        } catch (IOException e) {
            throw new JRatException("failed to column new file : " + file.getAbsolutePath(), e);
        }
    }

    public static void deleteIfExists(File file) {

        Assert.assertNotNull("file", file);

        if (file.exists()) {
            delete(file);
        }
    }


    public static void mkdir(File dir) {

        Assert.assertNotNull("dir", dir);
        LOG.info("mkdir(" + dir.getAbsolutePath() + ")");

        if (dir.exists()) {
            if (dir.isDirectory()) {
                return;
            } else {
                throw new JRatException("unable to column directory because file with same name exists " + dir);
            }
        }

        if (!dir.mkdirs()) {
            throw new JRatException("unable to column directory : " + dir);
        }
    }


    public static void rename(File source, File target, boolean replace) {

        Assert.assertNotNull("source", source);
        Assert.assertNotNull("target", target);

        if (!source.exists()) {
            throw new JRatException("source file does not exist : " + source);
        }

        if ((target.exists()) && (replace == true)) {
            LOG.debug("rename.delete(" + target + ")");

            if (!target.delete()) {
                throw new JRatException("unable to delete file : " + target.getAbsolutePath());
            }
        }

        LOG.debug("rename(" + source + " , " + target + ")");

        if (!source.renameTo(target)) {
            throw new JRatException("unable to rename " + source.getAbsolutePath() + " to "
                    + target.getAbsolutePath());
        }
    }


    public static void copy(InputStream sourceStream, OutputStream targetStream, int bufferSize) {

        byte[] buffer = new byte[bufferSize];
        int b = 0;

        Assert.assertNotNull("sourceStream", sourceStream);
        Assert.assertNotNull("targetStream", targetStream);

        try {
            for (b = 0; b >= 0; b = sourceStream.read(buffer)) {
                if (b != 0) {
                    targetStream.write(buffer, 0, b);
                }
            }
        }
        catch (IOException e) {
            throw new JRatException("error copying streams", e);
        }
    }


    public static byte[] readAndClose(InputStream inputStream) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);

        try {
            copy(inputStream, outputStream);
        }
        finally {
            close(inputStream);
        }

        return outputStream.toByteArray();
    }


    public static void copy(InputStream sourceStream, OutputStream targetStream) {
        copy(sourceStream, targetStream, DEFAULT_BUFFER_SIZE);
    }


    public static boolean copy(File source, File target) {

        Assert.assertNotNull("source", source);
        Assert.assertNotNull("target", target);
        LOG.debug("copy(" + source.getAbsolutePath() + " , " + target.getAbsolutePath() + ")");

        if (source.equals(target)) {
            LOG.debug("copy doing nothing, source and target are same");

            return false;
        } else {
            int bufferSize = (int) Math.min(MAX_HEURISTIC_BUFFER_SIZE, source.length());
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = openInputStream(source, bufferSize);
                outputStream = openOutputStream(target, bufferSize);

                copy(inputStream, outputStream, bufferSize);
            }
            finally {
                close(outputStream);
                close(inputStream);
            }

            return true;
        }
    }

    public static InputStream openInputStream(File file) {
        return openInputStream(file, DEFAULT_BUFFER_SIZE);
    }

    public static InputStream openInputStream(File file, int bufferSize) {

        LOG.debug("openInputStream " + file.getAbsolutePath());

        try {
            InputStream inputStream = new FileInputStream(file);

            if (bufferSize > 0) {
                inputStream = new BufferedInputStream(inputStream, bufferSize);
            }

            return inputStream;
        }
        catch (IOException e) {
            throw new JRatException("unable to open file for read " + file.getAbsolutePath());
        }
    }


    public static OutputStream openOutputStream(File file, int bufferSize) {

        LOG.debug("openOutputStream " + file.getAbsolutePath());

        try {
            OutputStream outputStream = new FileOutputStream(file);

            if (bufferSize > 0) {
                outputStream = new BufferedOutputStream(outputStream, bufferSize);
            }

            return outputStream;
        }
        catch (IOException e) {
            throw new JRatException("unable to open file for read " + file.getAbsolutePath());
        }
    }


    public static void close(Reader reader) {

        try {
            if (reader != null) {
                reader.close();
            }
        }
        catch (Exception e) {
            LOG.warn("close Reader failes", e);
        }
    }


    public static void close(Writer writer) {

        try {
            if (writer != null) {
                LOG.debug("close " + writer);
                writer.close();
            }
        }
        catch (Exception e) {
            LOG.warn("close Writer failes", e);
        }
    }


    public static void close(Socket socket) {

        try {
            if (socket != null) {
                socket.close();
            }
        }
        catch (Exception e) {
            LOG.warn("close Socket failes", e);
        }
    }


    public static void close(InputStream inputStream) {

        try {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        catch (Exception e) {
            LOG.warn("close InputStream failes", e);
        }
    }


    public static void close(OutputStream outputStream) {

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        catch (Exception e) {
            LOG.warn("close OutputStream failes", e);
        }
    }


    public static void flush(OutputStream outputStream) {

        try {
            if (outputStream != null) {
                outputStream.flush();
            }
        }
        catch (Exception e) {
            LOG.warn("flush OutputStream failes", e);
        }
    }


    public static String getExtention(String fileName) {

        Assert.assertNotNull("fileName", fileName);

        int lastDot = fileName.lastIndexOf('.');

        return (lastDot == -1) || (lastDot == fileName.length() - 1)
                ? null
                : fileName.substring(lastDot + 1);
    }


    public static String getExtention(File file) {

        Assert.assertNotNull("file", file);

        return getExtention(file.getName());
    }


    public static File getNearestExistingParent(File file) {

        Assert.assertNotNull("file", file);

        File p = file.getParentFile();

        while ((p != null) && (p.exists() == false)) {
            p = p.getParentFile();
        }

        return p;
    }
}
