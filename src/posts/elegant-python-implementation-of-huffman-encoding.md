---
title: "Elegant Python Implementation of Huffman Encoding"
published: 2024-04-19
---

Since an elegant implementation was promised, I'll share it in a sec. But in case you don't know about Huffman Encoding, I recommend you first watch [Huffman Codes: An Information Theory Perspective](https://www.youtube.com/watch?v=B3y0RsVCyrw). 

Regarding my implementation, the trick I used is just to model the tree using nested tuples. That is, let `(a,(b,c))` represent the tree:

<div class="flex centerContent">
	<ul class="reset noMargin noPadding">
		<li>
			root
			<ul>
				<li>a</li>
				<li>
					subroot
					<ul>
						<li>b</li>
						<li>c</li>
					</ul>
				</li>
			</ul>
		</li>
	</ul>
</div>

Or, if you prefer something more visual:

<div style="text-align: center;">
	<img src="/images/huffman_list_encoding.svg" alt="" style="width: 100%; max-width: 300px;" />
</div>

We can do this because only leaf nodes need the ability to store a value. Internal nodes only exist as placeholders for their children. So there's no need to introduce additional structures to model nodes (as typically done). Here you go: 

```python
from heapq import *

symbols = [(2, "a")
          ,(7, "b")
          ,(12, "c")
          ,(34, "g")
          ]

heapify(symbols)

while (len(symbols) > 1):
  (s1F, s1V) = heappop(symbols)
  (s2F, s2V) = heappop(symbols)
  heappush(
	symbols, 
	(s1F+s2F, (s1V, s2V))
  )

(_, tree) = symbols[0] 
# tree is our encoding
```

Note that after we get the two least frequent symbols, we combine them and add back to the heap. Thus, next time we combine them with something else, we're building up our tree structure, as lists are embedded in lists. E.g. first round we get `(a,b)`, second we get `((a,b),c)` and finally `(((a,b),c),g)`. This can easily be interpreted as a binary tree with the desired structure.

As a refresher, the codes for leaf nodes in a prefix tree are given by associating a 0 and 1 with traversing down the tree. E.g. for each left node I go down I add a 0 and for each right node I add a 1. Going left-left gives the code 00. We can thus print the codes for our tree by just walking down the tree:

<div style="text-align: center;">
	<img src="/images/huffman_example_prefixes.svg" alt="" style="width: 100%; max-width: 300px;" />
</div>

```python
def printCodes(tree, prefix):
  for i in range(2):
    if isinstance(tree[i], tuple):
      printCodes(tree[i], 
                 prefix + str(i))
    else:
      print(tree[i] + ": " + 
            prefix + str(i))

printCodes(tree, "")
#a: 000
#b: 001
#c: 01
#g: 1
```

I hope you enjoyed and also found this solution quite neat!
