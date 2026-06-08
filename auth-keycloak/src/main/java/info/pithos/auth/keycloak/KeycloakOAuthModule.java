package info.pithos.auth.keycloak;

import info.pithos.auth.OAuthClient;
import info.pithos.runtime.core.context.ApplicationContext;
import info.pithos.runtime.core.context.ServiceModule;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class KeycloakOAuthModule extends ServiceModule {

    private KeycloakOAuthClient client;

    public KeycloakOAuthModule(ApplicationContext context) {
        super(context);
    }

    @Override
    public boolean init() {
        if (this.initialized.compareAndSet(false, true)) {
            this.client = new KeycloakOAuthClient(this.getApplicationContext());
        }
        return this.initialized.get();
    }

    @Override
    protected void configure() {
        super.configure();
        super.bind(OAuthClient.class).toInstance(this.client);
        super.bind(KeycloakOAuthClient.class).toInstance(this.client);
    }

    @Override
    public CompletableFuture<Boolean> start(long timeout, TimeUnit unit) {
        return client.start(timeout, unit);
    }

    @Override
    public CompletableFuture<Boolean> shutdown(long timeout, TimeUnit unit) {
        return client.shutdown(timeout, unit);
    }
}
