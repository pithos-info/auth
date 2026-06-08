package info.pithos.auth;

import info.pithos.runtime.core.context.ApplicationContext;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractOAuthClient implements OAuthClient {

    protected final ApplicationContext context;

    protected AbstractOAuthClient(ApplicationContext context) {
        if (context == null) throw new IllegalArgumentException("context must not be null");
        this.context = context;
    }

    protected <T> CompletableFuture<T> submitAsync(Callable<T> task) {
        return context.getSystemContext().submitAsync(task);
    }
}
