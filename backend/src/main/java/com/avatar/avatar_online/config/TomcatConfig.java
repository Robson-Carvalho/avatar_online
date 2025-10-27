package com.avatar.avatar_online.config;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

// Esta classe é um Bean que modifica a fábrica do servidor web (Tomcat)
@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    // Defina o novo valor máximo (ex: 400)
    private static final int MAX_THREADS = 800;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {

        // 2. Adiciona o customizer do conector para definir os threads
        factory.addConnectorCustomizers(connector -> {

            // Pega o handler de protocolo do conector
            ProtocolHandler handler = connector.getProtocolHandler();

            // Verifica se é um protocolo abstrato (o caso mais comum, como HTTP/1.1)
            if (handler instanceof AbstractProtocol) {

                // Faz o cast para acessar os métodos de thread pool
                AbstractProtocol<?> protocol = (AbstractProtocol<?>) handler;

                // Define o número máximo de threads do executor
                protocol.setMaxThreads(MAX_THREADS);

                // Opcional: Define threads ociosas (idle)
                // protocol.setMinSpareThreads(50);

                System.out.println("Tomcat Customizer: Max Threads definido para " + MAX_THREADS);
            } else {
                System.err.println("Tomcat Customizer: ProtocolHandler não é um AbstractProtocol. A configuração de threads falhou.");
            }
        });
    }
}
