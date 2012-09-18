package org.jboss.portletbridge.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface UploadedFile {

    String getContentType();

    byte[] getData() throws IOException;

    InputStream getInputStream() throws IOException;

    String getName();

    long getSize();

    void delete() throws IOException;

    void write(String fileName) throws IOException;

    String getHeader(String headerName);

    Collection<String> getHeaderNames();

    Collection<String> getHeaders(String headerName);

    String getParameterName();
}
