package ${package};

import enkan.Env;
import enkan.collection.OptionMap;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import enkan.component.ApplicationComponent;
#if ($ORMapper == "doma2")
import enkan.component.doma2.DomaProvider;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.dialect.H2Dialect;
#end
#if ($ORMapper == "eclipselink")
import enkan.component.eclipselink.EclipseLinkEntityManagerProvider;
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

public class MySystemFactory implements EnkanSystemFactory {
    @Override
    public EnkanSystem create() {
        return EnkanSystem.of(
#if ($ORMapper == "doma2")
                "orm", builder(new DomaProvider())
                        .set(DomaProvider::setDialect, new H2Dialect())
                        .set(DomaProvider::setNaming, Naming.SNAKE_LOWER_CASE)
                        .build(),
#end
#if ($ORMapper == "eclipselink")
                "orm", builder(new EclipseLinkEntityManagerProvider())
                        .set(EclipseLinkEntityManagerProvider::setName, "pu")
                        .build(),
#end
                "jackson", new JacksonBeansConverter(),
#if ($migration == "flyway")
                "flyway", new FlywayMigration(),
#end
#if ($template == "freemarker")
                "template", new FreemarkerTemplateEngine(),
#end
#if ($datasource == "HikariCP")
                "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test;AUTOCOMMIT=FALSE;DB_CLOSE_DELAY=-1")),
#end
                "app", new ApplicationComponent(MyApplicationFactory.class.getName()),
#if ($webServer == "undertow")
                "http", builder(new UndertowComponent())
                        .set(UndertowComponent::setPort, Env.getInt("PORT", 3000))
                        .build()
#end
#if ($webServer == "jetty")
            "http", builder(new JettyComponent())
                    .set(JettyComponent::setPort, Env.getInt("PORT", 3000))
                    .build()
#end
        ).relationships(
                component("http").using("app"),
                component("app").using("datasource", "template", "orm", "jackson"),
                component("orm").using("datasource"),
                component("flyway").using("datasource")
        );

    }

}
