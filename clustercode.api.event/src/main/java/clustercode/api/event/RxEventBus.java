package clustercode.api.event;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import java.util.concurrent.CompletableFuture;

public interface RxEventBus {

    /**
     * Registers a new event listener that gets notified if an event of the given type is being emitted. The
     * filtering is done via {@link Class#isInstance(Object)}, so subclasses of eventType will be included as well.
     *
     * @param eventClass
     * @param onNext
     * @param <T>
     * @return
     */
    <T> Disposable listenFor(Class<T> eventClass, Consumer<? super T> onNext);

    <T> Disposable listenFor(Class<T> eventClass, Consumer<? super T> onNext, Consumer<? super Throwable> onError);

    <T> Observable<T> listenFor(Class<T> eventClass);

    /**
     * Inserts the given object into the underlying event stream. This method blocks until all subscribers have
     * processed the value. Use this if you do not care about the state of the event object after processing.
     *
     * @param event an object of any type. It will be lost after processing.
     * @param <T>   the type of object (optional).
     * @return the exact same event parameter, for fluent programming.
     */
    <T> T emit(T event);

    /**
     * Emits the given event object in another thread. Useful for long running subscribers and you expect the state
     * of the object to be changed after processing (this is useful for implementing return values).
     *
     * @param event the object.
     * @param <T>   the type of object.
     * @return the exact same event parameter wrapped in a completable future, for fluent programming.
     */
    <T> CompletableFuture<T> emitAsync(T event);

}
