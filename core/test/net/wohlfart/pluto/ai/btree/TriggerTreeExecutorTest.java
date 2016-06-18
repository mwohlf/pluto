package net.wohlfart.pluto.ai.btree;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.wohlfart.pluto.ai.btree.IBehavior.State;
import net.wohlfart.pluto.entity.IEntityCommand;

@SuppressWarnings("InstanceofInterfaces")
public class TriggerTreeExecutorTest {

    private BehaviorExecutor executor;
    private StateSensor stateHolder;

    @Before
    public void before() {
        executor = new BehaviorExecutor();
        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        stateHolder = new StateSensor();
        final IBehavior behavior = new TriggerBehavior(stateHolder);

        executor.attachBehavior(IEntityCommand.NULL_ENTITY, behavior);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof TriggerBehavior.TaskImpl);

        stateHolder.setState(State.RUNNING);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof TriggerBehavior.TaskImpl);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof TriggerBehavior.TaskImpl);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof TriggerBehavior.TaskImpl);
    }

    @Test
    public void failExecutorTest() {
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        stateHolder.setState(State.FAILURE);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.FAILURE);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void successExecutorTest() {
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        stateHolder.setState(State.SUCCESS);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.SUCCESS);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void invalidExecutorTest() {
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        stateHolder.setState(State.INVALID);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.INVALID);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void runningExecutorTest() {
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        stateHolder.setState(State.RUNNING);
        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
    }

}
