package br.com.leocr;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.ipc.netty.http.server.HttpServer;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

public class Server {

    public static final String HOST = "localhost";

    public static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        Server server = new Server();
		server.startReactorServer();
//        server.startTomcatServer();

        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

    public RouterFunction<?> routingFunction() {
        PersonRepository repository = new DummyPersonRepository();
        PersonHandler handler = new PersonHandler(repository);

        return route(GET("/person/{id}").and(json()), handler::getPerson)
                .andRoute(GET("/person").and(json()), handler::listPeople)
                .andRoute(POST("/person").and(contentType(APPLICATION_JSON)), handler::createPerson);
    }

    public void startReactorServer() throws InterruptedException {
        RouterFunction<?> route = routingFunction();
        HttpHandler httpHandler = toHttpHandler(route);

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create(HOST, PORT);
        server.newHandler(adapter).block();
    }

    public void startTomcatServer() throws LifecycleException {
        RouterFunction<?> route = routingFunction();
        HttpHandler httpHandler = toHttpHandler(route);
        Tomcat tomcatServer = new Tomcat();
        tomcatServer.setHostname(HOST);
        tomcatServer.setPort(PORT);
        Context rootContext = tomcatServer.addContext("", System.getProperty("java.io.tmpdir"));
        ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(httpHandler);
        Tomcat.addServlet(rootContext, "httpHandlerServlet", servlet);
        rootContext.addServletMapping("/", "httpHandlerServlet");
        tomcatServer.start();
    }

}