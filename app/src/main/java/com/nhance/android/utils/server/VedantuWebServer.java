package com.nhance.android.utils.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.util.Log;

public class VedantuWebServer extends NanoHTTPD {

    private static final String              TAG        = "VedantuWebServer";
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>();
    static {

        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("xml", "text/xml");
        MIME_TYPES.put("java", "text/x-java-source, text/java");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("asc", "text/plain");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("m3u", "audio/mpeg-url");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("swf", "application/x-shockwave-flash");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("ogg", "application/x-ogg");
        MIME_TYPES.put("zip", "application/octet-stream");
        MIME_TYPES.put("exe", "application/octet-stream");
        MIME_TYPES.put("class", "application/octet-stream");
    };

    private final File                       rootDir;
    private final boolean                    quiet;

    private Class<?>                         fileInputStreamClass;

    public VedantuWebServer(String host, int port, File wwwroot, boolean quiet) {

        this(host, port, wwwroot, quiet, FileInputStream.class);
    }

    public VedantuWebServer(String host, int port, File wwwroot, boolean quiet,
            Class<?> fileInputStreamClass) {

        super(host, port);
        this.rootDir = wwwroot;
        this.quiet = quiet;
        this.fileInputStreamClass = fileInputStreamClass;

    }

    File getRootDir() {

        return rootDir;
    }

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
     */
    private String encodeUri(String uri) {

        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/"))
                newUri += "/";
            else if (tok.equals(" "))
                newUri += "%20";
            else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}
            }
        }
        return newUri;
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers
     * and HTTP parameters.
     */
    Response serveFile(String uri, Map<String, String> header, File homeDir) {

        Log.v(TAG, "serving uri: " + uri);

        if (uri.startsWith("/nhance")) {
            uri = "/sdcard" + uri;
        }
        Response res = null;

        // Make sure we won't die of an exception later
        if (!homeDir.isDirectory()) {
            res = new Response(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");
        }

        if (res == null) {
            // Remove URL arguments
            uri = uri.trim().replace(File.separatorChar, '/');
            if (uri.indexOf('?') >= 0)
                uri = uri.substring(0, uri.indexOf('?'));

            // Prohibit getting out of current directory
            if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../"))
                res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                        "FORBIDDEN: Won't serve ../ for security reasons.");
        }

        File f = new File(homeDir, uri);
        if (res == null && !f.exists()) {
            res = new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                    "Error 404, file not found.");
        }

        // List the directory, if necessary
        if (res == null && f.isDirectory()) {
            // Browsers get confused without '/' after the
            // directory, send a redirect.
            if (!uri.endsWith("/")) {
                uri += "/";
                res = new Response(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML,
                        "<html><body>Redirected: <a href=\"" + uri + "\">" + uri
                                + "</a></body></html>");
                res.addHeader("Location", uri);
            }

            if (res == null) {
                // First try index.html and index.htm
                if (new File(f, "index.html").exists()) {
                    f = new File(homeDir, uri + "/index.html");
                } else if (new File(f, "index.htm").exists()) {
                    f = new File(homeDir, uri + "/index.htm");
                } else if (f.canRead()) {
                    // No index file, list the directory if it is readable
                    res = new Response(listDirectory(uri, f));
                } else {
                    res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                            "FORBIDDEN: No directory listing.");
                }
            }
        }

        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                String mime = null;
                int dot = f.getCanonicalPath().lastIndexOf('.');
                if (dot >= 0) {
                    mime = MIME_TYPES.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
                }
                if (mime == null) {
                    mime = NanoHTTPD.MIME_DEFAULT_BINARY;
                }

                // Calculate etag
                String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f
                        .length()).hashCode());

                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = header.get("range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                // Change return code and add Content-Range header when skipping is requested
                long fileLen = f.length();
                if (range != null && startFrom >= 0) {
                    if (startFrom >= fileLen) {
                        res = new Response(Response.Status.RANGE_NOT_SATISFIABLE,
                                NanoHTTPD.MIME_PLAINTEXT, "");
                        res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                        res.addHeader("ETag", etag);
                    } else {
                        if (endAt < 0) {
                            endAt = fileLen - 1;
                        }
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0) {
                            newLen = 0;
                        }
                        Log.v(TAG, "== serving partial data ==");
                        final long dataLen = newLen;
                        FileInputStream fis = getFileInputStream(f, dataLen);
                        fis.skip(startFrom);

                        res = new Response(Response.Status.PARTIAL_CONTENT, mime,
                                new BufferedInputStream(fis));
                        res.addHeader("Content-Length", "" + dataLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/"
                                + fileLen);
                        res.addHeader("ETag", etag);
                    }
                } else {
                    if (etag.equals(header.get("if-none-match")))
                        res = new Response(Response.Status.NOT_MODIFIED, mime, "");
                    else {
                        Log.v(TAG, "== serving full content ==");
                        System.gc();
                        res = new Response(Response.Status.OK, mime, getFileInputStream(f, fileLen));
                        res.addHeader("Content-Length", "" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                }
            }
        } catch (IOException ioe) {
            res = new Response(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                    "FORBIDDEN: Reading file failed.");
        }

        res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial
                                                 // content requestes
        Log.d(TAG, "returning res: " + res);
        return res;
    }

    private String listDirectory(String uri, File f) {

        String heading = "Directory " + uri;
        String msg = "<html><head><title>" + heading + "</title><style><!--\n"
                + "span.dirname { font-weight: bold; }\n" + "span.filesize { font-size: 75%; }\n"
                + "// -->\n" + "</style>" + "</head><body><h1>" + heading + "</h1>";

        String up = null;
        if (uri.length() > 1) {
            String u = uri.substring(0, uri.length() - 1);
            int slash = u.lastIndexOf('/');
            if (slash >= 0 && slash < u.length()) {
                up = uri.substring(0, slash + 1);
            }
        }

        List<String> files = Arrays.asList(f.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {

                return new File(dir, name).isFile();
            }
        }));
        Collections.sort(files);
        List<String> directories = Arrays.asList(f.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {

                return new File(dir, name).isDirectory();
            }
        }));
        Collections.sort(directories);
        if (up != null || directories.size() + files.size() > 0) {
            msg += "<ul>";
            if (up != null || directories.size() > 0) {
                msg += "<section class=\"directories\">";
                if (up != null) {
                    msg += "<li><a rel=\"directory\" href=\"" + up
                            + "\"><span class=\"dirname\">..</span></a></b></li>";
                }
                for (int i = 0; i < directories.size(); i++) {
                    String dir = directories.get(i) + "/";
                    msg += "<li><a rel=\"directory\" href=\"" + encodeUri(uri + dir)
                            + "\"><span class=\"dirname\">" + dir + "</span></a></b></li>";
                }
                msg += "</section>";
            }
            if (files.size() > 0) {
                msg += "<section class=\"files\">";
                for (int i = 0; i < files.size(); i++) {
                    String file = files.get(i);

                    msg += "<li><a href=\"" + encodeUri(uri + file)
                            + "\"><span class=\"filename\">" + file + "</span></a>";
                    File curFile = new File(f, file);
                    long len = curFile.length();
                    msg += "&nbsp;<span class=\"filesize\">(";
                    if (len < 1024)
                        msg += len + " bytes";
                    else if (len < 1024 * 1024)
                        msg += len / 1024 + "." + (len % 1024 / 10 % 100) + " KB";
                    else
                        msg += len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + " MB";
                    msg += ")</span></li>";
                }
                msg += "</section>";
            }
            msg += "</ul>";
        }
        msg += "</body></html>";
        return msg;
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header,
            Map<String, String> parms, Map<String, String> files) {

        if (!quiet) {
            System.out.println(method + " '" + uri + "' ");

            Iterator<String> e = header.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
            }
            e = parms.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
            }
            e = files.keySet().iterator();
            while (e.hasNext()) {
                String value = e.next();
                System.out.println("  UPLOADED: '" + value + "' = '" + files.get(value) + "'");
            }
        }
        return serveFile(uri, header, getRootDir());
    }

    @SuppressLint("NewApi")
    protected FileInputStream getFileInputStream(File f, final Long dataLen) throws IOException {

        FileInputStream fis = null;
        if (null == fileInputStreamClass || fileInputStreamClass == FileInputStream.class) {
            fis = new FileInputStream(f) {

                @Override
                public int available() throws IOException {

                    return dataLen.intValue();
                }
            };
        } else {
            try {
                Log.d(TAG, "file class : " + fileInputStreamClass.getName() + " constructers : "
                        + fileInputStreamClass.getConstructors());
                fis = (FileInputStream) fileInputStreamClass.getConstructor(File.class, Long.class)
                        .newInstance(f, dataLen);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                throw new IOException(e);
            }
        }
        return fis;
    }

    /**
     * The distribution licence
     */
    @SuppressWarnings("unused")
    private static final String LICENCE = "Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen, 2010 by Konstantinos Togias\n"
                                                + "\n"
                                                + "Redistribution and use in source and binary forms, with or without\n"
                                                + "modification, are permitted provided that the following conditions\n"
                                                + "are met:\n"
                                                + "\n"
                                                + "Redistributions of source code must retain the above copyright notice,\n"
                                                + "this list of conditions and the following disclaimer. Redistributions in\n"
                                                + "binary form must reproduce the above copyright notice, this list of\n"
                                                + "conditions and the following disclaimer in the documentation and/or other\n"
                                                + "materials provided with the distribution. The name of the author may not\n"
                                                + "be used to endorse or promote products derived from this software without\n"
                                                + "specific prior written permission. \n"
                                                + " \n"
                                                + "THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"
                                                + "IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"
                                                + "OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"
                                                + "IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"
                                                + "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"
                                                + "NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"
                                                + "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"
                                                + "THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"
                                                + "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"
                                                + "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";

}
