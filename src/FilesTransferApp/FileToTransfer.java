package FilesTransferApp;

import java.io.Serializable;

public class FileToTransfer implements Serializable {

    private byte[] file;
    private int size_bytes;
    private String name;
    private String extension;

    //any additional message like json
    private String msg;

    //set to true if object is send as a transfer acceptance request
    private boolean request = false;

    //set to true if object is send as a transfer acceptance response
    private boolean response = false;
    //set to true if remote user accepted a file to receive
    private boolean accept = false;

    //set to true if object contains a file and will be used to send it to remote user
    private boolean transfer = true;


    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
        this.size_bytes = file.length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getSize_bytes() {
        return size_bytes;
    }

    public void setSize_bytes(int size_bytes) {
        this.size_bytes = size_bytes;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isRequest() {
        return request;
    }

    public void setRequest(boolean request) {
        this.request = request;
        this.transfer = false;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
        this.transfer = false;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
        this.transfer = false;
    }

    public boolean isTransfer() {
        return transfer;
    }

    public void setTransfer(boolean transfer) {
        this.transfer = transfer;
    }
}
