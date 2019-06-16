package com.demo.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GPTomcat{
    private int port = 8080;
    private ServerSocket server;
    private Map<String,GPServlet> servletMapping = new HashMap<String,GPServlet>();
    private Properties properties = new Properties();
    public void start() throws Exception{
        //初始化
        init();

//        启动服务
        try {
            server = new ServerSocket(this.port);

            while(true){
                Socket client = server.accept();
                //去servlet中处理数据
                process(client);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 具体处理业务流程
     * @param client
     */
    private void process(Socket client)throws Exception {

        InputStream is = client.getInputStream();
        OutputStream os = client.getOutputStream();

        //7、Request(InputStrean)/Response(OutputStrean)
        GPRequest request = new GPRequest(is);
        GPResponse response = new GPResponse(os);

        //5、从协议内容中拿到URL，把相应的Servlet用反射进行实例化
        String url = request.getUrl();

        if(servletMapping.containsKey(url)){
            //6、调用实例化对象的service()方法，执行具体的逻辑doGet/doPost方法
            servletMapping.get(url).service(request,response);
        }else{
            response.write("404 - Not Found");
        }


        os.flush();
        os.close();

        is.close();
        client.close();
    }

    /**
     * 初始化配置文件
     * 实体类-url映射
     */
    private void init() {
        try {
            //通过Properties读取resources下的文件
            InputStream in = ClassLoader.getSystemResourceAsStream("web.properties");

            properties.load(in);
            //将web.properties文件中key-value添加到map中
            for (Object k:properties.keySet()) {
                //通过key创建实例，添加到ServletMapping中
                String key = k.toString();
                if(key.contains(".url")){
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = properties.getProperty(key);
                    String className = properties.getProperty(servletName + ".className");

                    //建立实体类与url映射
                    GPServlet obj = (GPServlet)Class.forName(className).newInstance();
                    servletMapping.put(url,obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

    }


    public static void main(String[] args) throws Exception {
        new GPTomcat().start();
    }
}
