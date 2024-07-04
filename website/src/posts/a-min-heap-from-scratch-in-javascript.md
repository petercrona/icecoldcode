---
title: "A Min-Heap from Scratch in JavaScript"
published: 2024-05-03
---

The modern day's developer has a pretty heavy toolbox, heavy enough so that we don't always bother to fully understand the tools we are using. This isn't necessarily a problem, indeed, the whole point of abstractions is to not care about unnecessary details for the job at hand, which often includes how to implement something from scratch. But, that doesn't mean it isn't fun and potentially useful to learn how to implement something from scratch anyway, so here we go.

## Definition

A Min-Heap is a data structure which allows you to turn an array into a binary tree, such that each element is smaller than or equal to all its children. Consider the following tree, which violates this:

<div style="text-align: center;">
	<img src="/images/tree_heap_invalid.svg" style="width: 100%; max-width: 300px;" alt="A tree where the min-heap property does not hold"/>
</div>

The nodes marked red are examples of heap property violations. The heap property states that a node's children must not be smaller than the node. We can fix this:

1. Swap 3 and 1
2. Swap 4 and 1
3. Swap 4 and 3

We end up with this valid Min-Heap:

<div style="text-align: center;">
	<img src="/images/tree_heap_valid.svg" style="width: 100%; max-width: 300px;" alt="A tree where the min-heap property does hold" />
</div>

Note that for any node you pick, its children will not be smaller than itself. This guarantees that the root will be the smallest child.

A Min-Heap is useful when you quickly need to get the smallest element. One example is when implementing Dijkstra's algorithm for finding the shortest path between a point A and B in a graph. By using a suitable data structure, such as a Min-Heap, you don't need to re-sort your whole list of distances every time something changes.

## Transforming an Array into a Min-Heap

The key to the Min-Heap lies in two operations: `bubbleUp` and `bubbleDown`. These are fairly simple, so let's look at the code and then discuss them briefly. However, before that, note that we are representing our tree with an array which has the simple setup that the first element is the root, and its left and right child is given by $2i$ and $2i+1$, respectively. Where $i$ is the position of the node, which is $1$ for the root. However, in JavaScript, as most languages, we use zero-based indexing. Thus, we get:

$$
\begin{alignat}{2}
\text{leftChild} &= (i+1)2 - 1\\
                  &= 2i+2-1\\
                  &= 2i+1\\[5pt]
\text{rightChild} &= (i+1)2 + 1 - 1\\
                   &= 2i+2
\end{alignat}
$$

To get the parent we do it in reverse:

$$
\begin{alignat}{2}
2i+1 &= \text{leftChildIndex}\quad &\Leftrightarrow\\[5pt]
i &= \frac{\text{leftChildIndex}-1}{2}\\[10pt]
2i+2 &= \text{rightChildIndex}\quad &\Leftrightarrow\\[5pt]
i &= \frac{\text{rightChildIndex}-2}{2}\\[10pt]
\end{alignat}\\
\text{Where}\ i\ \text{is the index of the parent}
$$

But, note that the right child is always one more than the left child. It is also always an even number. So if the left child is 3 we get $\frac{3-1}{2} = 1$. For the right child we get $\frac{4-2}{2} = 1$. But, we can simplify here by noting that if we were to take $-1$ always (regardless if left or right child), the right child would give a little too much. If $i=4$ we'd get: $\frac{4-1}{2} = 1.5$. We can compensate for this by taking the floor: $\lfloor\frac{4-1}{2}\rfloor$. We end up with a simple formula:

$$
\text{parent} = \lfloor\frac{i-1}{2}\rfloor
$$

Now we are ready to get coding! Let's start with the most fundamental operations and continue from there.

### Implementation of BubbleUp

```javascript
const bubbleUp = (A, i) => {
  if (i === 0) {
    return;
  }
  
  const parent = 
    Math.floor((i-1)/2);
	
  if (A[i] < A[parent]) {
    const tmp = A[i];
    A[i] = A[parent];
    A[parent] = tmp;
    bubbleUp(A, parent);
  }
};
```

We are swapping ourselves upwards as long as the parent is smaller than the current node. We do it recursively with base-case that we have become the root, but break as soon as we're not smaller than our parent.

### Implementation of BubbleDown

```javascript
const bubbleDown = (A, i) => {
  if (2*i+1 > A.length-1) {
    return;
  }
  
  const lChild = 2*i+1;
  const rChild = 2*i+2;
  
  let smallest = null;
  if (A[rChild] 
    && A[rChild] < A[lChild]) {
    smallest = rChild;
  } else {
    smallest = lChild;
  }
  
  if (A[smallest] < A[i]) {
    const tmp = A[smallest];
    A[smallest] = A[i];
    A[i] = tmp;
    bubbleDown(A, smallest);
  }
};
```

We swap ourselves downwards along the path of the smallest child for as long as we have a smaller child.


### Transformation of Array into Min-Heap

We are now ready to transform an array $A$ into a Min-Heap:

```javascript
const heapify = (A) => {
  for (
    let i = 
      Math.floor(
        (A.length - 2) / 2);
    i >= 0;
    i--)
  {
    bubbleDown(A, i);
  }
  return A;
};
```

For all internal nodes we are bubbling down. This ensures that the heap property holds for larger and larger portions of our tree, until we get to the root, which ensures that it holds for the whole tree. The key is that we start at the bottom where each subtree can only have one error, and if we swap with the smallest child, the subtree will be fixed, that is, the subtree's heap violation will be resolved. As we get to higher and higher levels in the tree, everything below is already a proper Min-Heap, and thus, the node we are processing is the only cause of a potential violation of the subtree, and bubbling the node down to its right place resolves the violation. And since we continue until we get to the root, we have ensured the whole tree has no heap property violations.

## Removing an Element from a Min-Heap

```javascript
const remove = (heap, i) => {
  if (i === 0 && heap.length === 1)
  {
    heap.pop();
    return heap;
  }
  
  heap[i] = heap.pop();
  bubbleDown(heap, i);
  return heap;
};
```

If there's just one element left and we're removing it, our job is easy, we just remove it. Otherwise, we swap the element we're removing with the last element and bubble it down. When we swap we can be pretty sure we have introduced a heap property violation, and we fix it by bubbling down.

## Extracting the Minimum Element from a Min-Heap

```javascript
const extractMin = (heap) => {
  const x = heap[0];
  remove(heap, 0);
  return x;
};
```

The minimum element is always the head. So we simply get it and then remove it from the heap with our previously created `remove` function.

## Inserting into a Min-Heap

```javascript
const insert = (A, x) => {
  A.push(x);
  bubbleUp(A, A.length-1);
  return A;
};
```

To insert an element we add it at the last position and bubble up. Because we know that if we have violated the heap property, the correct position for our new element will be higher up the tree, perhaps all the way up to the root.

## Updating an Element in a Min-Heap

Given that our Min-Heap is correct before the update, we know that our update may have caused a violation to the heap property. There are four different cases:

1. We are the root and must bubble down
2. We are a leaf node and must bubble up
3. We are smaller than the parent and must bubble up
4. We are larger than the parent and must bubble down

The beauty is that we are still relying mainly on our core operations `bubbleUp` and `bubbleDown`, so we can implement this relatively easily:

```javascript
const update = 
  (A, i, newVal) => {
    A[i] = newVal;
  
    if (i === 0) {
      bubbleDown(A, i);
      return;
    }
  
    const lChild = 
      2*i+1;
    const rChild = 
      2*i+2;

    if (
      A[lChild] === undefined
    ) {
      bubbleUp(A, i);
      return;
    }

    const parent = 
      Math.floor((i-1)/2);
	  
    if (A[parent] < A[i]) {
      bubbleUp(A, i);
    } else {
      bubbleDown(A, i);
    }
  }
```

## Time Complexity of a Min-Heap

`heapify` takes $O(n)$ (see [proof](https://stackoverflow.com/a/18742428)) and `extractMin`, `insert`, and `remove`, all take $O(\log{n})$ since they need to bubble up/down the element through the whole tree in worst case. However, to just peek at the smallest element takes $O(1)$, since you can just look at the head of the array.

## Conclusion

A Min-Heap is a fairly simple data structure that comes in handy when you need to quickly be able to get the smallest element. It offers an efficient way to get the smallest element. And it can be adapted as you go by inserting, removing or updating elements. A great friend to have when implementing algorithms such as Dijkstra's algorithm for finding the shortest path in a graph between a vertex A and B.
