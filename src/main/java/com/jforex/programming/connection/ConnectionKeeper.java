package com.jforex.programming.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ConnectionKeeper {

    private final Observable<ConnectionState> connectionStateObservable;
    private Observable<ConnectionState> reconnectStrategy;
    private Function<Observable<ConnectionState>,
                     Observable<ConnectionState>> reconnectComposer;
    private LoginState currentLoginState = LoginState.LOGGED_OUT;

    private static final Logger logger = LogManager.getLogger(ConnectionKeeper.class);

    public ConnectionKeeper(final IClient client,
                            final Observable<ConnectionState> connectionStateObservable,
                            final Observable<LoginState> loginStateObservable) {
        this.connectionStateObservable = connectionStateObservable;

        monitorConnection();
        monitorLoginState(loginStateObservable);
        initReconnectStrategy(client);
    }

    private void monitorConnection() {
        connectionStateObservable
            .doOnNext(cs -> logger.debug("Received connection state update " + cs
                    + ". Current login state is " + currentLoginState))
            .takeUntil(this::isConnectionLostWhileLoggedIn)
            .doOnComplete(this::startRetryStrategy)
            .subscribe();
    }

    private void monitorLoginState(final Observable<LoginState> loginStateObservable) {
        loginStateObservable.subscribe(ls -> {
            logger.debug("Received login state update " + ls);
            currentLoginState = ls;
        });
    }

    private void initReconnectStrategy(final IClient client) {
        reconnectStrategy = Completable
            .fromAction(() -> client.reconnect())
            .andThen(observeConnectionStateWithError());
    }

    private final Observable<ConnectionState> observeConnectionStateWithError() {
        return connectionStateObservable.flatMap(cs -> isConnectionLostWhileLoggedIn(cs)
                ? Observable.error(new Exception("Connection to server lost after performing retries!"))
                : Observable.just(cs));
    }

    private void startRetryStrategy() {
        if (reconnectComposer != null)
            reconnectStrategy
                .doOnSubscribe(d -> logger.debug("Trying to reconnect..."))
                .compose(reconnectComposer)
                .take(1)
                .doAfterTerminate(this::monitorConnection)
                .subscribe(cs -> logger.debug("Connection successfully reestablished."),
                           e -> logger.debug("Failed to reconnect! " + e.getMessage()));
        else
            monitorConnection();
    }

    private boolean isConnectionLostWhileLoggedIn(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED
                && currentLoginState == LoginState.LOGGED_IN) {
            logger.warn("Connection to server lost while logged in!");
            return true;
        }
        return false;
    }

    public void setReconnectComposer(final Function<Observable<ConnectionState>,
                                                    Observable<ConnectionState>> reconnectComposer) {
        this.reconnectComposer = reconnectComposer;
    }
}
