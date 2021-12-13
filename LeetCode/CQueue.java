/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-$today.year. All rights reserved.
 */

package com.payton.test;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 用两个栈实现队列
 * 用两个栈实现一个队列。队列的声明如下，请实现它的两个函数 appendTail 和 deleteHead ，分别完成在队列尾部插入整数和在队列头部删除整数的功能。(若队列中没有元素，deleteHead 操作返回 -1 )
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/yong-liang-ge-zhan-shi-xian-dui-lie-lcof
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 *输入：
 * ["CQueue","appendTail","deleteHead","deleteHead"]
 * [[],[3],[],[]]
 * 输出：[null,null,3,-1]
 *
 *输入：
 * ["CQueue","deleteHead","appendTail","appendTail","deleteHead","deleteHead"]
 * [[],[],[5],[2],[],[]]
 * 输出：[null,-1,null,null,5,2]
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/yong-liang-ge-zhan-shi-xian-dui-lie-lcof
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/yong-liang-ge-zhan-shi-xian-dui-lie-lcof
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 */
public class CQueue {

	public static void main(String[] args) {
		CQueue cQueue = new CQueue();
		cQueue.appendTail(1);
		cQueue.appendTail(2);
		System.out.println(cQueue.deleteHead());
		System.out.println(cQueue.deleteHead());
		System.out.println(cQueue.deleteHead());

	}

	private Queue<Integer> queue;
	CQueue() {
		queue = new ArrayDeque<>();
	}

	void appendTail(int value) {
		queue.add(value);
	}

	int deleteHead() {
		if (queue.size() == 0) {
			return -1;
		}
		return queue.poll();
	}
}
