package net.chrigel.clustercode.event;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseTest {

    private Response<Stub> subject;
    private Stub payload;

    @Before
    public void setUp() throws Exception {
        payload = new Stub();
        subject = new Response<>();
    }

    @Test
    public void getAnswer_ShouldReturnExpectedObject() throws Exception {
        Stub expected = new Stub();
        subject.addAnswer(expected);
        assertThat(subject.getAnswer()).contains(expected);
    }

    @Test
    public void getAnswer_ShouldReturnFilteredObject() throws Exception {
        Stub expected = new Stub();
        subject.addAnswer(new Object());
        subject.addAnswer(expected);
        assertThat(subject.getAnswer(Stub.class)).contains(expected);
    }

    @Test
    public void getAnswers_ShouldReturnAllAnswers() throws Exception {
        subject.addAnswer(new Object());
        subject.addAnswer(new Object());
        assertThat(subject.getAnswers()).hasSize(2);
    }

    @Test
    public void getAnswers_ShouldReturnEmptyList_IfNoAnswers() throws Exception {
        assertThat(subject.getAnswers()).isEmpty();
    }

    @Test
    public void getAnswers_ShouldReturnObjectInstances() throws Exception {
        Stub stub = new Stub();
        SubStub subStub = new SubStub();
        subject.addAnswer(new Object());
        subject.addAnswer(stub);
        subject.addAnswer(subStub);
        Collection<Stub> result = subject.getAnswers(Stub.class);
        assertThat(result).contains(stub, subStub);
        assertThat(result).hasSize(2);
    }

    @Test
    public void addAnswer_ShouldIgnoreAnswer_IfCompleted() throws Exception {
        subject.setComplete();
        subject.addAnswer(new Object());
        assertThat(subject.getAnswer()).isEmpty();
    }

    @Test
    public void addAnswer_ShouldIgnoreAnswer_IfNull() throws Exception {
        subject.addAnswer(null);
        assertThat(subject.getAnswer()).isEmpty();
    }

    @Test
    public void thenApplyFor_ShouldOnlyApplyConsumerForClass() throws Exception {
        subject.addAnswer(new Object());
        subject.addAnswer(new SubStub());
        subject.thenApplyFor(Stub.class, value -> payload.value += 1);
        assertThat(payload.value).isEqualTo(1);
    }

    @Test
    public void thenApplyToAll_ShouldApplyForSpecificClasses() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        subject.addAnswer(new Object());
        subject.addAnswer(payload);
        subject.thenApplyToAll(Stub.class, stub -> counter.incrementAndGet());
        assertThat(counter).hasValue(1);
    }

    private class Stub {

        private int value;
    }

    private class SubStub extends Stub {

    }
}
