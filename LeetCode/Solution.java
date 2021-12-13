
package com.payton.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 两数之和
 *
 * 给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target  的那 两个 整数，并返回它们的数组下标。
 * 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。
 * 你可以按任意顺序返回答案。
 * 你可以想出一个时间复杂度小于 O(n2) 的算法吗？
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/two-sum
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 *
 * 输入：nums = [2,7,11,15], target = 9
 * 输出：[0,1]
 * 解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。
 *
 * 输入：nums = [3,2,4], target = 6
 * 输出：[1,2]
 */
public class Solution {
	private static Map<Integer, Integer> map = new HashMap<>();

	public static void main(String[] args) {
		int[] nums = new int[]{3,3};
		System.out.println(Arrays.toString(twoSum(nums, 6)));
	}

	private static int[] twoSum(int[] nums, int target) {
		int[] result = new int[2];
		for (int i=0; i< nums.length; i++) {
			int left = target - nums[i];
			if (map.containsKey(left)) {
				result[0] = i;
				result[1] = map.get(left);
				Arrays.sort(result);
				return result;
			} else {
				map.put(nums[i], i);
			}
		}
		return result;
	}
}
