package com.demo.example;

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
