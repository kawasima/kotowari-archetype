package ${package};

import enkan.Application;
import enkan.application.WebApplication;
import enkan.config.ApplicationFactory;
import enkan.endpoint.ResourceEndpoint;
import enkan.predicate.NonePredicate;
import enkan.middleware.*;
import kotowari.middleware.*;
import enkan.system.inject.ComponentInjector;
import kotowari.routing.Routes;
import ${package}.controller.IndexController;

/**
 * @author kawasima
 */
public class MyApplicationFactory implements ApplicationFactory {
    @Override
    public Application create(ComponentInjector injector) {
        WebApplication app = new WebApplication();

        Routes routes = Routes.define(r -> {
            r.get("/").to(IndexController.class, "index");
        }).compile();

        app.use(new DefaultCharsetMiddleware());
        app.use(new NonePredicate(), new ServiceUnavailableMiddleware<>(new ResourceEndpoint("/public/html/503.html")));
        app.use(new StacktraceMiddleware());
        app.use(new TraceMiddleware<>());
        app.use(new ContentTypeMiddleware());
        app.use(new ParamsMiddleware());
        app.use(new MultipartParamsMiddleware());
        app.use(new MethodOverrideMiddleware());
        app.use(new NormalizationMiddleware());
        app.use(new NestedParamsMiddleware());
        app.use(new CookiesMiddleware());
        app.use(new SessionMiddleware());
        app.use(new ResourceMiddleware());
        app.use(new RenderTemplateMiddleware());
        app.use(new RoutingMiddleware(routes));
#if ($ORMapper == "doma2")
        app.use(new DomaTransactionMiddleware<>());
#end
        app.use(new FormMiddleware());
        app.use(new ValidateFormMiddleware());
        app.use(new HtmlRenderer());
        app.use(new ControllerInvokerMiddleware(injector));

        return app;
    }
}
