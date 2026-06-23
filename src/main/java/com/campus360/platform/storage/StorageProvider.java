package com.campus360.platform.storage;

import java.io.InputStream;

public interface StorageProvider {

    /**
     * Stores a document and returns the physical path or key.
     */
    String store(InputStream content, String originalFileName, String contentType);

    /**
     * Retrieves the document content.
     */
    InputStream retrieve(String physicalPath);
    
    /**
     * Provider type, e.g. "LOCAL", "S3"
     */
    String getType();
}
