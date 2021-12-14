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
