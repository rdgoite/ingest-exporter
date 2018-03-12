package org.humancellatlas.ingest.exception;

/**
 * Created by rolando on 28/02/2018.
 */
public class NoOutputFilesException extends RuntimeException {
    public NoOutputFilesException(String message){
        super(message);
    }
}
