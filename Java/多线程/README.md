
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



#### BlockingQueue的实现类

##### ArrayBlockingQueue

> 由**数组**结构组成的**有界**阻塞队列。内部结构是数组，故具有数组的特性。可以初始化队列大小， 且一旦初始化不能改变。构造方法中的fair表示控制对象的内部锁是否采用公平锁，默认是**非公平锁**。

```java
public ArrayBlockingQueue(int capacity, boolean fair){
    //..省略代码
}
```

##### LinkedBlockingQueue

> 由**链表**结构组成的**有界**阻塞队列。内部结构是链表，具有链表的特性。默认队列的大小是`Integer.MAX_VALUE`，也可以指定大小。此队列按照**先进先出**的原则对元素进行排序。

##### DelayQueue

> 该队列中的元素只有当其指定的延迟时间到了，才能够从队列中获取到该元素 。注入其中的元素必须实现 java.util.concurrent.Delayed 接口。 DelayQueue是一个没有大小限制的队列，因此往队列中插入数据的操作（生产者）永远不会被阻塞，而只有获取数据的操作（消费者）才会被阻塞。 

##### PriorityBlockingQueue

> 基于优先级的无界阻塞队列（优先级的判断通过构造函数传入的Compator对象来决定），内部控制线程同步的锁采用的是公平锁。

##### SynchronousQueue

> 这个队列比较特殊，**没有任何内部容量**，甚至连一个队列的容量都没有。并且每个 put 必须等待一个 take，反之亦然。

需要区别容量为1的ArrayBlockingQueue、LinkedBlockingQueue。

以下方法的返回值，可以帮助理解这个队列：

- iterator() 永远返回空，因为里面没有东西
- peek() 永远返回null
- put() 往queue放进去一个element以后就一直wait直到有其他thread进来把这个element取走。
- offer() 往queue里放一个element后立即返回，如果碰巧这个element被另一个thread取走了，offer方法返回true，认为offer成功；否则返回false。
- take() 取出并且remove掉queue里的element，取不到东西他会一直等。
- poll() 取出并且remove掉queue里的element，只有到碰巧另外一个线程正在往queue里offer数据或者put数据的时候，该方法才会取到东西。否则立即返回null。
- isEmpty() 永远返回true
- remove()&removeAll() 永远返回false

**注意**

**PriorityBlockingQueue**不会阻塞数据生产者（因为队列是无界的），而只会在没有可消费的数据时，阻塞数据的消费者。因此使用的时候要特别注意，**生产者生产数据的速度绝对不能快于消费者消费数据的速度，否则时间一长，会最终耗尽所有的可用堆内存空间。**对于使用默认大小的**LinkedBlockingQueue**也是一样



#### 阻塞队列的原理

> 阻塞队列的原理很简单，利用了Lock锁的多条件（Condition）阻塞控制。接下来我们分析ArrayBlockingQueue JDK 1.8 的源码。

首先是构造器，除了初始化队列的大小和是否是公平锁之外，还对同一个锁（lock）初始化了两个监视器，分别是notEmpty和notFull。这两个监视器的作用目前可以简单理解为标记分组，当该线程是put操作时，给他加上监视器notFull,标记这个线程是一个生产者；当线程是take操作时，给他加上监视器notEmpty，标记这个线程是消费者。

```java
//数据元素数组
final Object[] items;
//下一个待取出元素索引
int takeIndex;
//下一个待添加元素索引
int putIndex;
//元素个数
int count;
//内部锁
final ReentrantLock lock;
//消费者监视器
private final Condition notEmpty;
//生产者监视器
private final Condition notFull;  

public ArrayBlockingQueue(int capacity, boolean fair) {
    //..省略其他代码
    lock = new ReentrantLock(fair);
    notEmpty = lock.newCondition();
    notFull =  lock.newCondition();
}
```

##### **put操作的源码**

```java
public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    // 1.自旋拿锁
    lock.lockInterruptibly();
    try {
        // 2.判断队列是否满了
        while (count == items.length)
            // 2.1如果满了，阻塞该线程，并标记为notFull线程，
            // 等待notFull的唤醒，唤醒之后继续执行while循环。
            notFull.await();
        // 3.如果没有满，则进入队列
        enqueue(e);
    } finally {
        lock.unlock();
    }
}
private void enqueue(E x) {
    // assert lock.getHoldCount() == 1;
    // assert items[putIndex] == null;
    final Object[] items = this.items;
    items[putIndex] = x;
    if (++putIndex == items.length)
        putIndex = 0;
    count++;
    // 4 唤醒一个等待的线程
    notEmpty.signal();
}
```

总结put的流程：

1. 所有执行put操作的线程竞争lock锁，拿到了lock锁的线程进入下一步，没有拿到lock锁的线程自旋竞争锁。
2. 判断阻塞队列是否满了，如果满了，则调用await方法阻塞这个线程，并标记为notFull（生产者）线程，同时释放lock锁,等待被消费者线程唤醒。
3. 如果没有满，则调用enqueue方法将元素put进阻塞队列。注意这一步的线程还有一种情况是第二步中阻塞的线程被唤醒且又拿到了lock锁的线程。
4. 唤醒一个标记为notEmpty（消费者）的线程。



##### **take操作的源码**

```java
public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == 0)
            notEmpty.await();
        return dequeue();
    } finally {
        lock.unlock();
    }
}
private E dequeue() {
    // assert lock.getHoldCount() == 1;
    // assert items[takeIndex] != null;
    final Object[] items = this.items;
    @SuppressWarnings("unchecked")
    E x = (E) items[takeIndex];
    items[takeIndex] = null;
    if (++takeIndex == items.length)
        takeIndex = 0;
    count--;
    if (itrs != null)
        itrs.elementDequeued();
    notFull.signal();
    return x;
}
```

take操作和put操作的流程是类似的，总结一下take操作的流程：

1. 所有执行take操作的线程竞争lock锁，拿到了lock锁的线程进入下一步，没有拿到lock锁的线程自旋竞争锁。
2. 判断阻塞队列是否为空，如果是空，则调用await方法阻塞这个线程，并标记为notEmpty（消费者）线程，同时释放lock锁,等待被生产者线程唤醒。
3. 如果没有空，则调用dequeue方法。注意这一步的线程还有一种情况是第二步中阻塞的线程被唤醒且又拿到了lock锁的线程。
4. 唤醒一个标记为notFull（生产者）的线程。

**注意**

1. put和tack操作都需要**先获取锁**，没有获取到锁的线程会被挡在第一道大门之外自旋拿锁，直到获取到锁。
2. 就算拿到锁了之后，也**不一定**会顺利进行put/take操作，需要判断**队列是否可用**（是否满/空），如果不可用，则会被阻塞，**并释放锁**。
3. 在第2点被阻塞的线程会被唤醒，但是在唤醒之后，**依然需要拿到锁**才能继续往下执行，否则，自旋拿锁，拿到锁了再while判断队列是否可用（这也是为什么不用if判断，而使用while判断的原因）。



