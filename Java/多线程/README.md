[TOC]

## java多线程

### 如何停掉线程？

线程run方法通过监听isInterrupted()来控制

### synchronized

#### 对象锁概念

同一个对象，方法a加了synchronized，方法b不加，A线程持有了对象锁，B线程可以异步访问b方法，访问a方法会阻塞

#### synchronized可重入

当线程进入了一个对象的synchronized方法，方法内部调用其他synchronized方法是自动获取锁的，这么做就是防止死锁。

#### 线程出现异常，synchronized锁可自动释放

#### synchronized同步方法不具备继承性

#### synchronized(this) 同步代码块

- 使用同步代码块提升性能，不在方法上做同步块，在变量赋值地方做同步块，通过半同步半异步能力提升性能
- 同步代码块锁定的是当前对象，因此该对象的其他同步代码块是被当前线程独占

#### synchronized(非this) 同步代码块可以实现异步调用

#### synchronized方法是静态的锁住的是class

#### 通过JDK工具查看死锁

1. 使用jps查看进程
2. 通过jstack -l 进程id ，可查看那段代码发生死锁



### 等待&通知

#### 调用wait()方法必须持有改对象的锁，执行完后释放锁，因此方法必须在同步块或同步方法中，notify()也是，假设没有获得该锁会抛出IlleagalMonitorStateException，补充一点，notify方法执行完后不会马上释放掉锁，要等notify线程执行完后才释放，wait状态的线程才会获得锁

#### 当线程处于wait方法，调interrupt() 方法会抛interruptedException

#### notify()只能随机唤醒一个线程，notifyAll()方法唤醒所有wait状态线程



### ThreadLocal

> ThreadLocal类似于每个线程存放数据的盒子
[理解Java中的ThreadLocal](https://droidyue.com/blog/2016/03/13/learning-threadlocal-in-java/)



### ReetrantLock

> ReetrantLock是个对象，通过tryLock&unLock进行对象的加锁与释放

#### tryLock()&lock()区别

tryLock是非阻塞的，拿不到锁直接返回false

lock是阻塞的，直到拿到锁为止

#### 等待&通知

> 需要借助于Condition对象，Lock创建多个Condition实例，线程注册指定的Condition中，从而选择性的进行通知，更具灵活性

用法与notify&wait类似，唤醒是唤醒注册在condition上的线程

```java
condition.await();
```

```java
condition.signalAll();
```

```java
/**
 * 生成者与消费者模型
 */
public class ProductReenTrantTest {
    private final Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private boolean isEmpty = true;

    private void product() {
        lock.lock();
        try {
            while (!isEmpty) {
                condition.await();
            }
            System.out.println("generate a product");
            isEmpty = false;
            condition.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private void consumer() {
        lock.lock();
        try {
            while (isEmpty) {
                condition.await();
            }
            System.out.println("consumer a product");
            isEmpty = true;
            condition.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ProductReenTrantTest reenTrantTest = new ProductReenTrantTest();
        for (int i = 0; i < 100; i++) {
            new Thread(reenTrantTest::product).start();
            new Thread(reenTrantTest::consumer).start();
        }
    }

}
```



#### getHoldCount & getQueueLength & getWaitQueueLength

- Lock锁可以多次重入，getHoldCount 返回重入锁的次数，也就是加锁的次数

- getQueueLength 返回正在等待锁的线程数

- getWaitQueueLength（condition）返回正在等待condition锁的线程数



#### hasQueueThread() & hasQueueThreads() & hasWaiters()

- lock.hasQueueThread(Thread t)： 查询线程t是否等待此锁
- lock.hasQueueThreadst)： 查询此锁是否有等待线程
- lock.hasWaiters(Condition cond)： 查询是否有线程等待绑定Condition 条件的锁



## ReetrantReadWriteLock

> ReetrantLock是一个完全排他的互斥锁，同一时间只有一个线程执行lock后边任务。
>
> ReetrantReadWriteLock 读写锁有两个锁，一个读操作的锁，称为共享锁；一个是写操作的锁，称为排他锁。不同线程之间读锁和写锁互斥（有读就不能写，有写不能读），有读可继续读（读锁可以多个），有写不能再写（写锁只能1个）；同一个线程之间，获取读锁后不能再获取写锁，但如果获取了写锁当前线程可以再获取读锁，但其它线程无法获取读/写锁

```JAVA
/*
 * 以下代码读锁无法释放
 */

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO
 *
 * @author y00608930
 * @since 2021/12/17
 */
public class ReenTrantReadWriteTest {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private Condition condition = lock.writeLock().newCondition();

    private void method() {
        try {
            lock.readLock().tryLock(1, TimeUnit.SECONDS);
            System.out.println("come in " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
            System.out.println("release from " + Thread.currentThread().getName());
        }
    }

    private void signal() {
        lock.writeLock().lock();
        try {
            System.out.println("come in write");
            condition.signalAll();
        } finally {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.writeLock().unlock();
            System.out.println("write release");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReenTrantReadWriteTest reenTrantTest = new ReenTrantReadWriteTest();
        Thread threadw = new Thread(reenTrantTest::signal);
        threadw.start();
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(reenTrantTest::method);
            thread.setName("Thread" + i);
            thread.start();
        }
    }

}
```

### 四种常见的线程池

> `Executors`类中提供的几个静态方法来创建线程池

#### newCachedThreadPool

> 当需要执行很多**短时间**的任务时，CacheThreadPool的线程复用率比较高， 会显著的**提高性能**。而且线程60s后会回收，意味着即使没有任务进来，CacheThreadPool并不会占用很多资源。

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```



#### newFixedThreadPool

> 核心线程数量和总线程数量相等，都是传入的参数nThreads，所以只能创建核心线程，不能创建非核心线程。因为LinkedBlockingQueue的默认大小是Integer.MAX_VALUE，故如果核心线程空闲，则交给核心线程处理；如果核心线程不空闲，则入列等待，直到核心线程空闲。

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
}
```

**与CachedThreadPool的区别：**

- 因为 corePoolSize == maximumPoolSize ，所以FixedThreadPool只会创建核心线程。 而CachedThreadPool因为corePoolSize=0，所以只会创建非核心线程。
- 在 getTask() 方法，如果队列里没有任务可取，线程会一直阻塞在 LinkedBlockingQueue.take() ，线程不会被回收。 CachedThreadPool会在60s后收回。
- 由于线程不会被回收，会一直卡在阻塞，所以**没有任务的情况下， FixedThreadPool占用资源更多**。 
- 都几乎不会触发拒绝策略，但是原理不同。FixedThreadPool是因为阻塞队列可以很大（最大为Integer最大值），故几乎不会触发拒绝策略；CachedThreadPool是因为线程池很大（最大为Integer最大值），几乎不会导致线程数量大于最大线程数，故几乎不会触发拒绝策略。



#### newSingleThreadExecutor

> 有且仅有一个核心线程（ corePoolSize == maximumPoolSize=1），使用了LinkedBlockingQueue（容量很大），所以，**不会创建非核心线程**。所有任务按照**先来先执行**的顺序执行。如果这个唯一的线程不空闲，那么新来的任务会存储在任务队列里等待执行。

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```



#### newScheduledThreadPool

> 创建一个定长线程池，支持定时及周期性任务执行。

```java
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}

//ScheduledThreadPoolExecutor():
public ScheduledThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize, Integer.MAX_VALUE,
          DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,
          new DelayedWorkQueue());
}
```



四种常见的线程池基本够我们使用了，但是《阿里把把开发手册》不建议我们直接使用Executors类中的线程池，而是通过`ThreadPoolExecutor`的方式，这样的处理方式让写的同学需要更加明确线程池的运行规则，规避资源耗尽的风险。

但如果你及团队本身对线程池非常熟悉，又确定业务规模不会大到资源耗尽的程度（比如线程数量或任务队列长度可能达到Integer.MAX_VALUE）时，其实是可以使用JDK提供的这几个接口的，它能让我们的代码具有更强的可读性。





