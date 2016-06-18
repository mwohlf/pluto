package net.wohlfart.pluto.ai.btree;

import org.junit.Assert;
import org.junit.Test;

import net.wohlfart.pluto.ai.btree.IBehavior.State;
import net.wohlfart.pluto.entity.IEntityCommand;

@SuppressWarnings("InstanceofInterfaces")
public class SimpleTreeExecutorTest {

    @Test
    public void simpleSuccessExecutorTest() {
        final BehaviorExecutor executor = new BehaviorExecutor();

        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        final IBehavior behavior = new SuccessBehavior();
        executor.attachBehavior(IEntityCommand.NULL_ENTITY, behavior);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof SuccessBehavior.TaskImpl);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.SUCCESS);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void emptySequenceExecutorTest() {
        final BehaviorExecutor executor = new BehaviorExecutor();

        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        final AbstractBehaviorNode sequentialBehavior = new Sequential();
        executor.attachBehavior(IEntityCommand.NULL_ENTITY, sequentialBehavior);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // putting the subtask in the queue
        Assert.assertTrue(executor.getLastState() == State.SUCCESS);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void simpleFailureExecutorTest() {
        final BehaviorExecutor executor = new BehaviorExecutor();

        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        final IBehavior behavior = new FailBehavior();
        executor.attachBehavior(IEntityCommand.NULL_ENTITY, behavior);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof FailBehavior.TaskImpl);

        executor.tick(1, null);
        Assert.assertTrue(executor.getLastState() == State.FAILURE);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void simpleFailureSequentialExecutorTest() {
        final BehaviorExecutor executor = new BehaviorExecutor();

        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        final AbstractBehaviorNode sequentialBehavior = new Sequential();
        sequentialBehavior.addChild(new FailBehavior());

        executor.attachBehavior(IEntityCommand.NULL_ENTITY, sequentialBehavior);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // putting the subtask in the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof FailBehavior.TaskImpl);

        executor.tick(1, null); // putting the sequence back into the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // sequence finished successful
        Assert.assertTrue(executor.getLastState() == State.FAILURE);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void simpleSuccessSequentialExecutorTest() {
        final BehaviorExecutor executor = new BehaviorExecutor();

        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        final AbstractBehaviorNode sequentialBehavior = new Sequential();
        sequentialBehavior.addChild(new SuccessBehavior());

        executor.attachBehavior(IEntityCommand.NULL_ENTITY, sequentialBehavior);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // putting the subtask in the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof SuccessBehavior.TaskImpl);

        executor.tick(1, null); // putting the sequence back into the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // sequence finished successful
        Assert.assertTrue(executor.getLastState() == State.SUCCESS);
        Assert.assertEquals(0, executor.getSize());
    }

    @Test
    public void simpleSequentialExecutorTest() {
        final BehaviorExecutor executor = new BehaviorExecutor();

        Assert.assertTrue(executor.getLastState() == State.SUCCESS);

        final AbstractBehaviorNode sequentialBehavior = new Sequential();
        sequentialBehavior.addChild(new SuccessBehavior());
        sequentialBehavior.addChild(new SuccessBehavior());
        sequentialBehavior.addChild(new FailBehavior());

        executor.attachBehavior(IEntityCommand.NULL_ENTITY, sequentialBehavior);
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // putting the subtask in the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof SuccessBehavior.TaskImpl);

        executor.tick(1, null); // putting the sequence back into the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // putting the subtask in the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof SuccessBehavior.TaskImpl);

        executor.tick(1, null); // putting the sequence back into the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // putting the subtask in the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof FailBehavior.TaskImpl);

        executor.tick(1, null); // putting the sequence back into the queue
        Assert.assertTrue(executor.getLastState() == State.RUNNING);
        Assert.assertEquals(1, executor.getSize());
        Assert.assertTrue(executor.getAtPosition(0) instanceof Sequential.TaskImpl);

        executor.tick(1, null); // sequence finished successful
        Assert.assertTrue(executor.getLastState() == State.FAILURE);
        Assert.assertEquals(0, executor.getSize());
    }
}
