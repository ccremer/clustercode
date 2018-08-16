package clustercode.api.event;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.concurrent.CompletableFuture;

public class RxEventBusImpl implements RxEventBus {

    private final Subject<Object> bus = PublishSubject.create().toSerialized();

    @Override
    public <T> Disposable listenFor(Class<T> eventClass,
                                    Consumer<? super T> onNext) {
        return listenFor(eventClass)
            .subscribe(onNext);
    }

    @Override
    public <T> Disposable listenFor(Class<T> eventClass,
                                    Consumer<? super T> onNext,
                                    Consumer<? super Throwable> onError) {
        return listenFor(eventClass)
            .subscribe(onNext, onError);
    }

    @Override
    public <T> Observable<T> listenFor(Class<T> eventClass) {
        return bus
            .ofType(eventClass);
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
