I am JD. I am a software developer with 8+ years of experience. 
I am preparing for technical interviews in Java.

## Coaching Rules (strictly follow these):
- Never give me the solution before I attempt the approach
- Always ask me the three-question template before I code
- Push me to think out loud first, then validate
- Be strict, no sugarcoating, no hand-holding
- If I am stuck, give hints not answers
- Verify all problem examples before giving input/output
- Give problems in LeetCode format with examples and constraints
- I keep if/else template structure during practice — do not tell 
  me to clean it up during practice, only in interviews

## My Learning Style:
- I think out loud — validate my approach before I code
- I make voice-to-text errors sometimes — understand context
- I get confused when you teach wrong things — double check 
  everything before teaching
- I prefer to discover patterns organically
- I want to understand WHY, not just HOW

## Sliding Window — COMPLETED (9/10 problems)

### Three-Question Template (mandatory before every problem):
1. State — what data structure tracks the window?
2. Invalidity condition — what triggers the shrink?
3. if vs while — why?

### Data Structure Decision Rules:
- HashMap<Character, Integer> → char frequencies, distinct chars
- int[26] → lowercase letters only, faster than HashMap
- int[128] → any ASCII character, faster than HashMap
- int windowSum → sum-based constraints
- HashSet → uniqueness only, no count needed
- int distinctCount → auxiliary counter alongside map

### Three Types of Sliding Window:
- Maximize (longest) → use if for shrink, record after shrink 
  every iteration
- Minimize (shortest) → use while for shrink, record INSIDE 
  while before evicting
- Fixed size → use if, evict left every time window hits k size,
  record only when valid

### Key Rule — expand vs shrink order:
- EXPAND: check freq > 0 BEFORE decrementing → requiredChars--
  then map.put(ch, map.get(ch) - 1)
- SHRINK: map.put(left, map.get(left) + 1) first THEN check 
  if freq > 0 → requiredChars++

### Problems Solved:

**#1 — Longest Substring Without Repeating Characters**
- State: HashSet
- Invalidity: set.contains(ch)
- Shrink: while
- Solution:
  Set<Character> set = new HashSet<>();
  int i = 0, longest = 0;
  for (int j = 0; j < str.length(); j++) {
      char ch = str.charAt(j);
      while (set.contains(ch)) {
          set.remove(str.charAt(i));
          i++;
      }
      set.add(ch);
      longest = Math.max(longest, j - i + 1);
  }
  return longest;

**#2 — Longest Substring with Exactly K Distinct Characters**
- State: HashMap<Character, Integer>
- Invalidity: map.size() > k
- Shrink: while
- Record: only when map.size() == k
- Solution:
  Map<Character, Integer> map = new HashMap<>();
  int i = 0, longest = -1;
  for (int j = 0; j < str.length(); j++) {
      char ch = str.charAt(j);
      map.put(ch, map.getOrDefault(ch, 0) + 1);
      while (map.size() > k) {
          char left = str.charAt(i);
          map.put(left, map.get(left) - 1);
          if (map.get(left) == 0) map.remove(left);
          i++;
      }
      if (map.size() == k) longest = Math.max(longest, j-i+1);
  }
  return longest;

**#3 — Longest Substring with At Most 2 Distinct Characters**
- Same as #2 but record when map.size() <= 2
- Solution: identical to #2, change == k to <= 2

**#4 — Longest Repeating Character Replacement**
- State: int[26] freq array + int maxFreq
- Invalidity: (j - i + 1) - maxFreq > k
- Shrink: if (not while) — one shrink step always enough
- maxFreq only updated on EXPAND, never on SHRINK
- Key insight: maxFreq is a high watermark, only goes up
- Solution:
  int[] freq = new int[26];
  int i = 0, longest = 0, maxFreq = 0;
  for (int j = 0; j < s.length(); j++) {
      freq[s.charAt(j) - 'A']++;
      maxFreq = Math.max(maxFreq, freq[s.charAt(j) - 'A']);
      if ((j - i + 1) - maxFreq > k) {
          freq[s.charAt(i) - 'A']--;
          i++;
      }
      longest = Math.max(longest, j - i + 1);
  }
  return longest;

**#5 — Minimum Window Substring**
- State: HashMap for t frequencies + int requiredChars
- Invalidity: requiredChars == 0 means valid, shrink while valid
- Shrink: while(requiredChars == 0)
- Record: INSIDE while, before evicting
- Key insight: use requiredChars counter not map.isEmpty()
- Solution:
  Map<Character, Integer> map = new HashMap<>();
  int requiredChars = t.length(), i = 0;
  int leftIndex = 0, rightIndex = 0;
  int smallestWindow = Integer.MAX_VALUE;
  for (char ch : t.toCharArray())
      map.put(ch, map.getOrDefault(ch, 0) + 1);
  for (int j = 0; j < s.length(); j++) {
      char ch = s.charAt(j);
      if (map.containsKey(ch)) {
          if (map.get(ch) > 0) requiredChars--;
          map.put(ch, map.get(ch) - 1);
      }
      while (requiredChars == 0) {
          if (j - i + 1 < smallestWindow) {
              leftIndex = i;
              rightIndex = j;
              smallestWindow = j - i + 1;
          }
          char left = s.charAt(i);
          if (map.containsKey(left)) {
              map.put(left, map.get(left) + 1);
              if (map.get(left) > 0) requiredChars++;
          }
          i++;
      }
  }
  return smallestWindow == Integer.MAX_VALUE ? "" 
       : s.substring(leftIndex, rightIndex + 1);

**#6 — Find All Anagrams in a String**
- State: HashMap for p frequencies + int requiredChars
- Fixed window size = p.length()
- Shrink: if (fixed size)
- Evict left EVERY TIME window hits p.length()
- Record: only when requiredChars == 0
- Solution:
  Map<Character, Integer> map = new HashMap<>();
  int requiredChar = p.length(), i = 0, j = 0;
  List<Integer> result = new ArrayList<>();
  for (char ch : p.toCharArray())
      map.put(ch, map.getOrDefault(ch, 0) + 1);
  while (j < str.length()) {
      char ch = str.charAt(j);
      if (map.containsKey(ch)) {
          if (map.get(ch) > 0) requiredChar--;
          map.put(ch, map.get(ch) - 1);
      }
      if (j - i + 1 == p.length()) {
          if (requiredChar == 0) result.add(i);
          char left = str.charAt(i);
          if (map.containsKey(left)) {
              map.put(left, map.get(left) + 1);
              if (map.get(left) > 0) requiredChar++;
          }
          i++;
      }
      j++;
  }
  return result;

**#7 — Permutation in String**
- Identical to #6
- Return true when requiredChar == 0, false at end

**#9 — Minimum Size Subarray Sum**
- State: int windowSum
- Invalidity: windowSum >= target (valid, record and shrink)
- Shrink: while(windowSum >= target)
- Record: INSIDE while before evicting
- Solution:
  int i = 0, j = 0, windowSum = 0;
  int len = Integer.MAX_VALUE;
  while (j < nums.length) {
      windowSum += nums[j];
      while (windowSum >= target) {
          len = Math.min(len, j - i + 1);
          windowSum -= nums[i];
          i++;
      }
      j++;
  }
  return len == Integer.MAX_VALUE ? 0 : len;

**#10 — Maximum Erasure Value**
- State: HashSet + int windowSum
- Invalidity: set.contains(nums[j])
- Shrink: while(arr[i] != arr[j]) remove and move i
- Solution:
  Set<Integer> set = new HashSet<>();
  int i = 0, j = 0, windowSum = 0;
  int maxSum = Integer.MIN_VALUE;
  while (j < arr.length) {
      windowSum += arr[j];
      if (set.contains(arr[j])) {
          while (arr[i] != arr[j]) {
              set.remove(arr[i]);
              windowSum -= arr[i];
              i++;
          }
          set.remove(arr[i]);
          windowSum -= arr[i];
          i++;
      } else {
          set.add(arr[j]);
      }
      j++;
      maxSum = Math.max(maxSum, windowSum);
  }
  return maxSum;

## Skipped:
- #8 Substring with Concatenation of All Words → hard, skip

## What is Next — Two Pointer Approach (15 problems):

### Three Types:
Type 1 — Opposite direction (left and right closing in):
1.  Two Sum II #167                    (easy)
2.  Valid Palindrome #125              (easy)
3.  Container With Most Water #11     (medium)
4.  3Sum #15                          (medium)
5.  4Sum #18                          (medium)
6.  Trapping Rain Water #42           (medium)
7.  Boats to Save People #881         (medium)

Type 2 — Same direction slow/fast:
8.  Remove Duplicates from Sorted Array #26   (easy)
9.  Remove Element #27                         (easy)
10. Move Zeroes #283                           (easy)
11. Squares of a Sorted Array #977             (easy)
12. Sort Colors #75                            (medium)

Type 3 — Two arrays:
13. Merge Sorted Array #88             (easy)
14. Intersection of Two Arrays II #350 (easy)
15. Compare Version Numbers #165       (medium)

### Two Pointer Template:
Type 1:
  int left = 0, right = arr.length - 1;
  while (left < right) {
      if (condition met) { record; }
      else if (need bigger) { left++; }
      else { right--; }
  }

Type 2:
  int slow = 0;
  for (int fast = 0; fast < arr.length; fast++) {
      if (condition) { arr[slow] = arr[fast]; slow++; }
  }

Type 3:
  int i = 0, j = 0;
  while (i < arr1.length && j < arr2.length) {
      if (arr1[i] < arr2[j]) i++;
      else if (arr1[i] > arr2[j]) j++;
      else { record; i++; j++; }
  }

## Start Here:
Give me Problem #1 of Two Pointers — Two Sum II in LeetCode 
format. I will think out loud first. Validate my approach 
before I code. Be strict.
