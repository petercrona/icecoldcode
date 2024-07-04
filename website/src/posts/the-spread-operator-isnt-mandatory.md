---
title: "The Spread Operator Isn't Mandatory"
published: 2024-05-04
---

I was curious about the performance impact of immutability in JS (when not using something like [Immutable](https://immutable-js.com/)), and searched around a bit. One of the top results was an article [Immutability and Its (Potential) Impact on Performance](https://product.hubspot.com/blog/immutability-and-performance) by Jon Miller at Hubspot. It's a great article, and it prompted me to write this one, because I want to help spread the message. Jon Miller writes:

> ...programming paradigms should be carefully considered and not blindly followed in hopes they shield us from engineering decisions, errors or negative outcomes.

I think this is super good advice. In the article two cases of using `reduce` are compared:

```javascript
arrayToRecordImmutable(arr) {
  return arr.reduce(
    (record, item) => ({
      ...record,
      [item]: true
  }), {});
}

arrayToRecordMutable(arr) {
  return arr.reduce(
    (record, item) => {
      record[item] = true;
      return record;
  }, {});
}
```

The mutable version takes 5.911ms for 5000 elements. And the immutable takes 3912.803ms for 5000 elements. So, the mutable version is roughly 650 times faster.

I have seen variants of `arrayToRecordImmutable` quite a bit myself. I believe it often happens due to developers either wanting to keep the code elegant, or due to developers by default using the spread operator, as treating things as immutable is something we are taught is good.

But, this is a great case where immutability doesn't offer any benefits. Immutability offer great benefits in particular when something is passed around. We have a guarantee that nobody will mess with our object. If I pass in `{name: "Peter"}`, it won't suddenly become `{name: "PETER"}` without me knowing unless I look at all code.

However, in `arrayToRecordImmutable` we're not getting this benefit, since we just create our empty object ourselves (in the function), add to it, and then return the result. If `arrayToRecordImmutable` was a blackbox and we swapped it out for the mutable version suddenly, the only way it would impact others is that they'd get the results quicker. I suppose if we wanted to package this as general advice, we could say:

> An object may be mutated by its creator, once passed out into the world, avoid mutation.

But, then again, this article is about not blindly following advice, but rather thinking it through and applying one's best judgement.

Another example: let's say you have an array and you want to transform each element into `{value, duplicate: boolean}`, where `value` is the original element, and duplicate is true if the element has already been encountered. One way of doing this would be:

```javascript
toWithDuplicateProp(arr) {
  const seen = new Set();
  return arr.map(element => {
    encountered.add(element);
    return { 
      value: element, 
      duplicate: 
        seen.has(element) 
    };
  });
}
```

If your code would be a blackbox, nobody would ever know that you are mutating your Set `seen`. And I leave it as an exercise for you to think about if you'd really get a big benefit from treating `seen` as immutable here.

## Conclusion

Immutability is wonderful and can make it easier to reason about code. But, know the language and tools you use, and the price you pay for it. Don't default to treating everything as immutable if you gain nothing and it may cause your code to run 650 times slower.

One advice is to be extra open for the creator of an object to be allowed to mutate it. But, ultimately, pay attention to details, do your best, and don't code in autopilot mode! For a colder future!
