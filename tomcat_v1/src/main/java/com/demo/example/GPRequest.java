package com.demo.example;

import java.io.InputStream;

/**
 *
 * 如何获取请求信息?
 *  通过InputStream流读取
 */
public class GPRequest {

    private String method;
    private String url;

    /**
     * 请求头：
     * Request URL: https://www.baidu.com/
     * Request Method: GET
     * Status Code: 200 OK
     * Remote Address: 61.135.169.121:443
     * Referrer Policy: no-referrer-when-downgrade
     *
     * 获取 method和url
     * @param in 从socketServer中获取
     */
    public GPRequest(InputStream in)throws Exception{

        String content = "";
        byte[] bytes = new byte[1024];
        int len;
        if((len = in.read(bytes))!=-1){
            content = new String(bytes,0,len);
        }

        String line = content.split("\\n")[0];
        String[] arr = line.split("\\s");

        //拿到协议内容，初始化 url和method
        this.method = arr[0];
        this.url = arr[1].split("\\?")[0];
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
