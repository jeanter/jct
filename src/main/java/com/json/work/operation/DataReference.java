package com.json.work.operation;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/*
 * 需要被并发写操作的基类对象 ,继承该类的对象 可被工作线程进行无挂起调度 
 */
public abstract class DataReference {

	private static final AtomicReferenceFieldUpdater<DataReference, OperationHandler> lockUpdater = AtomicReferenceFieldUpdater
			.newUpdater(DataReference.class, OperationHandler.class, "lockOwner");

	protected volatile OperationHandler lockOwner;

	public boolean tryLock(OperationHandler newLockOwner) {
		// 当前线程重入
		if (newLockOwner == lockOwner)
			return true;
		while (true) {
			// 如果当前没有线程获设置锁,就把newLockOwner设置成当前的线程锁
			if (lockUpdater.compareAndSet(this, null, newLockOwner)) {
				return true;
			}
			OperationHandler owner = lockOwner;
			if (owner != null) {
				// 加入到线程的等待队列,进行重新调度
				// 可能会重复执行添加， waitingHandlers.set(id, handler); 但等待队列中该handler只会有一个
				// unlock()时 owner.wakeUpWaitingHandlers(); 会唤醒全部handler handler并不会一直是hold状态，
				// 他本身会一直循环跑run,中间间隔loopInterval时是wait()状态，wakeUp会唤醒loopInterval时wait()的线程
				//System.out.println("addWaitingHandler handlerId : "+newLockOwner.getHandlerId());
				owner.addWaitingHandler(newLockOwner);
			}
			// 解锁了，或者又被其他线程锁住了 再重试
			if (lockOwner == null || lockOwner != owner)
				continue;
			else
				return false;
		}
	}

	public void unlock() {
		if (lockOwner != null) {
			OperationHandler owner = lockOwner;
			lockOwner = null;
			owner.wakeUpWaitingHandlers();
		}
	}

}
