# 			Tomcat主题

## 版本一

### BIO

#### 流程：

* 启动初始化：读取配置文件，初始化servletMapping
* ServerSocket等待客户端发送请求
* 接受到请求，调用servlet中service方法
* request:通过Socket传输的流读取请求头
* response:通过http格式将数据写个客户端

##### GPTomcat

```java
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
```

GPServlet

```java
/**
 *抽象类
 */
public abstract class GPServlet {
    public void service(GPRequest request, GPResponse response) throws Exception{
        if("GET".equalsIgnoreCase(request.getMethod())){
            doGet(request,response);
        }else{
            doPost(request,response);
        }
    }

    public abstract void doGet(GPRequest request, GPResponse response) throws Exception;
    public abstract void doPost(GPRequest request, GPResponse response) throws Exception;
}
```

FirstServlet

```java
/**
 *
 *真正执行的类
 */
public class FirstServlet extends GPServlet{


    @Override
    public void doGet(GPRequest request, GPResponse response) throws Exception {
        response.write("GET:This is My Servlet");
    }

    @Override
    public void doPost(GPRequest request, GPResponse response) throws Exception {
        response.write("POST:This is My Servlet");
    }
}
```

##### GPRequest

```java
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

    public String getUrl() {
        return url;
    }
}
```

##### GPResponse

```java
/**
 * 获取响应信息
 */
public class GPResponse {
    private OutputStream out;
    public GPResponse(OutputStream out){
        this.out = out;
    }

    public void write(String s) throws Exception {
        //用的是HTTP协议，输出也要遵循HTTP协议
        //给到一个状态码 200
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html;\n")
                .append("\r\n")
                .append(s);
        out.write(sb.toString().getBytes());
    }
}
```







