package ${package};

import enkan.Application;
import enkan.application.WebApplication;
import enkan.config.ApplicationFactory;
import enkan.endpoint.ResourceEndpoint;
import enkan.predicate.NonePredicate;
import enkan.middleware.*;
import enkan.middleware.devel.*;
#if ($ORMapper == "doma2")
import enkan.middleware.doma2.DomaTransactionMiddleware;
#end
#if ($ORMapper == "eclipselink")
import enkan.middleware.jpa.EntityManagerMiddleware;
import enkan.middleware.jpa.NonJtaTransactionMiddleware;
import kotowari.inject.ParameterInjector;
import kotowari.inject.parameter.EntityManagerInjector;
import kotowari.util.ParameterUtils;
import java.util.List;
#end
import kotowari.middleware.*;
import kotowari.middleware.serdes.ToStringBodyWriter;
import enkan.system.inject.ComponentInjector;
import kotowari.routing.Routes;
import ${package}.controller.IndexController;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.Predicates.*;

public class MyApplicationFactory implements ApplicationFactory {
    @Override
    public Application create(ComponentInjector injector) {
        WebApplication app = new WebApplication();

        Routes routes = Routes.define(r -> {
            r.get("/").to(IndexController.class, "index");
        }).compile();

        app.use(new DefaultCharsetMiddleware());
        app.use(NONE, new ServiceUnavailableMiddleware<>(new ResourceEndpoint("/public/html/503.html")));
        app.use(envIn("development"), new LazyLoadMiddleware<>("enkan.middleware.devel.StacktraceMiddleware"));
        app.use(envIn("development"), new LazyLoadMiddleware<>("enkan.middleware.devel.TraceWebMiddleware"));
        app.use(new TraceMiddleware<>());
        app.use(new ContentTypeMiddleware<>());
        app.use(envIn("development"), new LazyLoadMiddleware<>("enkan.middleware.devel.HttpStatusCatMiddleware"));
        app.use(new ParamsMiddleware<>());
        app.use(new MultipartParamsMiddleware<>());
        app.use(new MethodOverrideMiddleware<>());
        app.use(new NormalizationMiddleware<>());
        app.use(new NestedParamsMiddleware<>());
        app.use(new CookiesMiddleware<>());
        app.use(new SessionMiddleware<>());
        app.use(new ContentNegotiationMiddleware<>());

        app.use(new ResourceMiddleware<>());
        app.use(new RenderTemplateMiddleware<>());
        app.use(new RoutingMiddleware<>(routes));
#if ($ORMapper == "doma2")
        app.use(new DomaTransactionMiddleware<>());
#end
#if ($ORMapper == "eclipselink")
        app.use(new EntityManagerMiddleware<>());
        app.use(new NonJtaTransactionMiddleware<>());

        List<ParameterInjector<?>> parameterInjectors = ParameterUtils.getDefaultParameterInjectors();
        parameterInjectors.add(new EntityManagerInjector());
        app.use(builder(new FormMiddleware<>())
                .set(FormMiddleware::setParameterInjectors, parameterInjectors)
                .build());
        app.use(builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyWriters, new ToStringBodyWriter())
                .set(SerDesMiddleware::setParameterInjectors, parameterInjectors)
                .build());
        app.use(new ValidateBodyMiddleware<>());
        app.use(builder(new ControllerInvokerMiddleware<>(injector))
                .set(ControllerInvokerMiddleware::setParameterInjectors, parameterInjectors)
                .build());
#else
        app.use(new FormMiddleware<>());
        app.use(builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyWriters, new ToStringBodyWriter())
                .build());
        app.use(new ValidateBodyMiddleware<>());
        app.use(new ControllerInvokerMiddleware<>(injector));
#end

        return app;
    }
}
