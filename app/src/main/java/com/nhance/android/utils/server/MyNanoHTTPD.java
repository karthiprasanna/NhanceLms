package com.nhance.android.utils.server;

import java.io.File;
import java.io.IOException;

public class MyNanoHTTPD extends VedantuWebServer {

    public MyNanoHTTPD(File pRootFile) throws IOException {
//        super("127.0.0.1", 8080, new File("/storage"), true, VedantuFileInputStream.class);
        super("127.0.0.1", 8080, pRootFile, true, VedantuFileInputStream.class);
    }

}
