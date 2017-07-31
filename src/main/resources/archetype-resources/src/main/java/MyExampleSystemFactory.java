package ${package};

import enkan.Env;
import enkan.collection.OptionMap;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import enkan.component.ApplicationComponent;
#if ($ORMapper == "doma2")
import enkan.component.doma2.DomaProvider;
#end
import enkan.component.jackson.JacksonBeansConverter;
import enkan.component.flyway.FlywayMigration;
#if ($datasource == "HikariCP")
import enkan.component.hikaricp.HikariCPComponent;
#end
#if ($template == "freemarker")
import enkan.component.freemarker.FreemarkerTemplateEngine;
#end
#if ($webServer == "undertow")
import enkan.component.undertow.UndertowComponent;
#elseif ($webServer == "jetty")
import enkan.component.jetty.JettyComponent;
#end
import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;

public class MyExampleSystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
#if ($ORMapper == "doma2")
                "doma", new DomaProvider(),
#end
                "jackson", new JacksonBeansConverter(),
                "flyway", new FlywayMigration(),
#if ($template == "freemarker")
                "template", new FreemarkerTemplateEngine(),
#end
#if ($datasource == "HikariCP")
                "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test")),
#end
                "app", new ApplicationComponent("${package}.MyApplicationFactory"),
#if ($webServer == "undertow")
                "http", builder(new UndertowComponent())
                        .set(UndertowComponent::setPort, Env.getInt("PORT", 3000))
                        .build()
#end
        ).relationships(
                component("http").using("app"),
                component("app").using("datasource", "template", "doma", "jackson"),
                component("doma").using("datasource", "flyway"),
                component("flyway").using("datasource")
        );

    }

}
