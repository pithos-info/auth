package info.pithos.auth.gcp;

import info.pithos.auth.OAuthClient;
import info.pithos.runtime.core.context.ApplicationContext;
import info.pithos.runtime.core.context.ServiceModule;

public final class GcpIdentityOAuthModule extends ServiceModule {

    private GcpIdentityOAuthClient client;

    public GcpIdentityOAuthModule(ApplicationContext context) {
        super(context);
    }

    @Override
    public boolean init() {
        if (this.initialized.compareAndSet(false, true)) {
            this.client = new GcpIdentityOAuthClient(this.getApplicationContext());
        }
        return this.initialized.get();
    }

    @Override
    protected void configure() {
        super.configure();
        super.bind(OAuthClient.class).toInstance(this.client);
        super.bind(GcpIdentityOAuthClient.class).toInstance(this.client);
    }
}
