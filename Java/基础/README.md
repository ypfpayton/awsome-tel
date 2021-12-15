# JAVA基础
## equals() 与 hashCode()

- hashCode() 返回的并不是实际的内存地址，而是计算结果值。如果是实际内存地址的话，JVM 中的频繁 GC 和内存移动将会导致对象 hashCode 的改变。

```java
    /**
     * Returns a hash code for this string. The hash code for a
     * {@code String} object is computed as
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using {@code int} arithmetic, where {@code s[i]} is the
     * <i>i</i>th character of the string, {@code n} is the length of
     * the string, and {@code ^} indicates exponentiation.
     * (The hash value of the empty string is zero.)
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        int h = hash;
        if (h == 0 && value.length > 0) {
            char val[] = value;

            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }
```

- equals() 方法中首先判断的还是**地址**，地址一样就是True，如果地址不同才会去判断**值是否相等**

```java
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String anotherString = (String)anObject;
            int n = value.length;
            if (n == anotherString.value.length) {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }
```



## 为什么重写 equals() 就要重写 hashCode()？

- **如果两个对象调用 equals() 方法比较是相等的，那么调用这两个对象中的 hashCode() 方法必须产生同样的整数结果。**
- 如果两个对象根据 equals() 方法比较是不相等的，那么调用者两个对象中的 hashCode() 方法，则不一定要求 hashCode 方法必须产生不同的结果。但是给不相等的对象产生不同的整数散列值，是有可能提高散列表（hash table）的性能。

> 试想如果 String 类只重写了 equals 方法而没有重写 HashCode 方法，这里将某个字符串 `new String("s")` 作为 Key 然后 put 一个值，但是再根据  `new String("s")`  去 Get 的时候却得到 null 的结果，这是难以让人接受的。

## Java 集合框架

### HashMap

**从结构实现来讲，HashMap是数组+链表+红黑树（JDK1.8增加了红黑树部分）**

![img](https://awps-assets.meituan.net/mit-x/blog-images-bundle-2016/e4a19398.png)



#### 红黑树介绍

红黑树，一种二叉查找树，但在每个结点上增加一个存储位表示结点的颜色，可以是Red或Black。

5个性质

- 每个结点要么是红的要么是黑的。  
- 根结点是黑的。  
- 每个叶结点（叶结点即指树尾端NIL指针或NULL结点）都是黑的。  

- 如果一个结点是红的，那么它的两个儿子都是黑的。  

- 对于任意结点而言，其到叶结点树尾端NIL指针的每条路径都包含相同数目的黑结点。 

![image-20211215152108717](C:\Users\y00608930\AppData\Roaming\Typora\typora-user-images\image-20211215152108717.png)





![img](https://awps-assets.meituan.net/mit-x/blog-images-bundle-2016/d669d29c.png)

#### **扩容**

1. 先引用保存扩容前的数组
2. 检查旧数组的容量是否已达到最大值，是则将阈值设置为 Integer 的最大值，并直接返回。
3. 否则的话，申请一个新容量的数组，执行 transfer() 方法，具体的是
   1. 遍历旧的数组，检查每一个桶是否为空，如果不为空的话，先把它保存一份，然后将其置空；
   2. 接着按保存下来的桶数组的头进行遍历，计算每一个结点在新数组中的位置；
   3. 最后将旧数组中的结点迁移到新数组中对应的位置。
4. 将旧数组的指针指向新数组
5. 计算新的阈值

> 扩容是一个特别耗性能的操作，所以当程序员在使用HashMap的时候，估算map的大小，初始化的时候给一个大致的数值，避免map进行频繁的扩容。



### ConcurrentHashMap

#### JDK1.7&JDK1.8的差异

> jdk1.7采用分段锁，Segment数组的意义就是将一个大的table分割成多个小的table来进行加锁，也就是上面的提到的锁分离技术，而每一个Segment元素存储的是HashEntry数组+链表，这个和HashMap的数据存储结构一样，加锁方式使用ReentrantLock的tryLock



![img](https://upload-images.jianshu.io/upload_images/5220087-8c5b0cc951e61398.png?imageMogr2/auto-orient/strip|imageView2/2/w/767/format/webp)



> JDK1.8的实现已经摒弃了Segment的概念，而是直接用Node数组+链表+红黑树的数据结构来实现，并发控制使用Synchronized和CAS来操作，整个看起来就像是优化过且线程安全的HashMap

![img](https://upload-images.jianshu.io/upload_images/5220087-63281d7b737f1109.png?imageMogr2/auto-orient/strip|imageView2/2/w/453/format/webp)



1. JDK1.8的实现降低锁的粒度，JDK1.7版本锁的粒度是基于Segment的，包含多个HashEntry，而JDK1.8锁的粒度就是HashEntry（首节点）

2. JDK1.8版本的数据结构变得更加简单，使得操作也更加清晰流畅，因为已经使用synchronized来进行同步，所以不需要分段锁的概念，也就不需要Segment这种数据结构了，由于粒度的降低，实现的复杂度也增加了
3. JDK1.8使用红黑树来优化链表，基于长度很长的链表的遍历是一个很漫长的过程，而红黑树的遍历效率是很快的，代替一定阈值的链表，这样形成一个最佳拍档
4. JDK1.8为什么使用内置锁synchronized来代替重入锁ReentrantLock，我觉得有以下几点
   1. 因为粒度降低了，在相对而言的低粒度加锁方式，synchronized并不比ReentrantLock差，在粗粒度加锁中ReentrantLock可能通过Condition来控制各个低粒度的边界，更加的灵活，而在低粒度中，Condition的优势就没有了
   2. JVM的开发团队从来都没有放弃synchronized，而且基于JVM的synchronized优化空间更大，使用内嵌的关键字比使用API更加自然
   3. 在大量的数据操作下，对于JVM的内存压力，基于API的ReentrantLock会开销更多的内存，虽然不是瓶颈，但是也是一个选择依据



#### 操作

初始化方法 `initTable()`

> 1. 如果没有初始化就先调用initTable（）方法来进行初始化过程
>
> 2. 如果没有hash冲突就直接CAS插入
>
> 3. 如果还在进行扩容操作就先进行扩容
>
> 4. 如果存在hash冲突，就加锁来保证线程安全，这里有两种情况，一种是链表形式就直接遍历到尾端插入，一种是红黑树就按照红黑树结构插入，
>
> 5. 最后一个如果Hash冲突时会形成Node链表，在链表长度超过8，Node数组超过64时会将链表结构转换为红黑树的结构，break再一次进入循环
>
> 6. 如果添加成功就调用addCount（）方法统计size，并且检查是否需要扩容



get(Object key)

1. 根据 `key` 计算 哈希值
2. 检查table是否为空，非空的话检查table中根据key计算出的索引处的头结点也不为空
   1. 如果这两个检查中有一个为空，则返回`null`。
   2. 否则的话，检查定位到的头结点的 hash 与 key 是否均相等，相等的话则返回结果。
   3. 否则的话，判断是红黑树节点还是链表结点。如果是红黑树节点的话则到红黑树中查找，如果是链表结点的话则遍历链表查找，并返回相应的结果。
3. 如果最后没有查到，则返回 `null`。



#### 扩容

`transfer()`方法为`CHM`扩容的核心方法。在此过程中，`CHM`支持多线程扩容。扩容主要分为两个步骤：

- 构建`nextTable`桶数组，大小为之前的两倍，这个操作在单线程下完成。
- 将`oldTable`里面的内容复制到`nextTable`，这个操作允许多线程操作。可以减少扩容时间。

基本思路与`HashMap`差不多，主要区别在于`CHM`多线程加锁`synchronized (f)`。





#### 为什么用了 synchronized 之后还要用 CAS 保证线程安全?

通过3个CAS来保证对node操作实现原子性，避免粗粒度锁，提供性能



## HashMap、Hashtable、CHM区别

| 比较方面           | **HashMap**                                 | **Hashtable**                                     | **ConcurrentHashMap**                                        |
| ------------------ | ------------------------------------------- | ------------------------------------------------- | ------------------------------------------------------------ |
| 是否线程安全       | 否                                          | 是                                                | 是                                                           |
| 线程安全采用的方式 | 无                                          | 采用`synchronized`类锁，效率低                    | `CAS` + `synchronized`，锁住的只有当前操作的**bucket**，不影响其他线程对其他bucket的操作，效率高 |
| 数据结构           | 数组 + 链表 + 红黑树                        | 数组 + 链表                                       | 数组 + 链表 + 红黑树                                         |
| 是否允许`null`键值 | 是                                          | 否                                                | 否                                                           |
| 哈希地址算法       | `(h=key.hashCode())^(h>>>16`                | `key.hashCode()`                                  | `(h=key.hashCode())^(h>>>16)&0x7fffffff`                     |
| 定位算法           | `(n-1)&hash`                                | `(hash&0x7fffffff)%n`                             | `(n-1)&hash`                                                 |
| 扩容算法           | 当键值对数量大于阈值，则容量扩容到原来的2倍 | 当键值对数量大于等于阈值，则容量扩容到原来的2倍+1 | 当键值对数量大于等于sizeCtl，**单线程创建新哈希表，多线程复制bucket到新哈希表**，容量扩容到原来的2倍 |
| 链表插入           | 将新节点插入到链表**尾部**                  | 将新节点插入到链表**头部**                        | 将新节点插入到链表**尾部**                                   |
| 继承的类           | 继承`abstractMap`抽象类                     | 继承`Dictionary`抽象类                            | 继承`abstractMap`抽象类                                      |
| 实现的接口         | 实现`Map`接口                               | 实现`Map`接口                                     | 实现`ConcurrentMap`接口                                      |
| 默认容量大小       | 16                                          | 11                                                | 16                                                           |
| 默认负载因子       | 0.75                                        | 0.75                                              | 0.75                                                         |
| 统计 size 方式     | 直接返回成员变量`size`                      | 直接返回成员变量`count`                           | 遍历`CounterCell`数组的值进行累加，最后加上`baseCount`的值即为`size` |

 
