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

