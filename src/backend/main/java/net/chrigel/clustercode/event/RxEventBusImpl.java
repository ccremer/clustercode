package net.chrigel.clustercode.event;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.concurrent.CompletableFuture;

public class RxEventBusImpl implements RxEventBus {

    private final Subject<Object> bus = PublishSubject.create().toSerialized();

    @Override
    public <T> Disposable register(Class<T> eventClass,
                                   Consumer<? super T> onNext) {
        return register(eventClass)
            .subscribe(onNext);
    }

    @Override
    public <T> Disposable register(Class<T> eventClass,
                                   Consumer<? super T> onNext,
                                   Consumer<? super Throwable> onError) {
        return register(eventClass)
            .subscribe(onNext, onError);
    }

    @Override
    public <T> Observable<T> register(Class<T> eventClass) {
        return bus
            .filter(eventClass::isInstance)
            .cast(eventClass);
    }


    @Override
    public <T> T emit(T event) {
        bus.onNext(event);
        return event;
    }

    @Override
    public <T> CompletableFuture<T> emitAsync(T event) {
        return CompletableFuture.supplyAsync(() ->{
            bus.onNext(event);
            return event;
        });
    }

}
