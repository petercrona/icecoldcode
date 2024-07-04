---
title: "Better Developer: Think Transformations"
published: 2024-05-20
---

In this article we'll dive into the wonderful concept of transformations. A transformation can be anything taking some input and giving some output. Indeed, a simple transformation is the identity transformation, which simply gives back the input, which in JS can be expressed as `x => x`.

When I write software, I often spend quite some time thinking about what transformations I need. I even tend to start by sketching my solution by just writing out transformations between types, e.g. that I will need a function `toUserDto :: User -> UserDto`, and perhaps it's counterpart `fromDto :: UserDto -> User`. Note that I often use Haskell notation when sketching out my solutions, even if I'm not using Haskell for the implementation; mainly because I find it simple and concise.

Beyond being my sketching tool, I find that writing out, or drawing, types and transformations, is an excellent aid for creating great logically sensible designs.

To "think transformations" it helps raising the abstraction level of your thinking with regards to transformations. Do not think of transformations as merely functions, but think about the types of transformations and find cohesive groups of transformations. Let's start by looking at transformations closed under a type.

## Transformations From A to A (Modifiers)

There are many examples of transformations, or functions, that don't change the type. A trivial example is addition. I'm a big fan of seeing these as modifiers and placed together with the type definition, because many of them can be viewed as operations inherent to the type. It is very natural to think of addition as something inherent, or closely related to, a number. And it is also natural to think of it as a modifier of a number. Another example:

```javascript
// User.js

/*
User {
  id: String,
  name: String,
  reports: User[]
}
*/

// Constructor
const mkUser = (
  id, 
  name, 
  reports = []) => 
    ({id, name, reports});

// Modifiers
const addReport = 
  report => 
  user => ({
    ...user, 
	reports: [
	  ...user.reports,
	  report
	]
  });

const removeReport = 
  report => 
  user => ({
    ...user, 
	reports: user
	  .reports
	  .filter(r => 
	    r.id !== report.id
	  )
  });
```

`mkUser` is the constructor. Then we have a bunch of *modifiers*. These are often higher-order functions, and finally they return a function `user -> user`. This means they can easily be composed. Given a compose function `compose`, we can do:

```javascript
modifiedUser = compose(
  removeReport(reportA), 
  addReport(reportB), 
  addReport(reportA)
)(user)
```

If you are using TypeScript, I recommend using something like `pipe` from FP-TS though, as it works better with the type inference. Chances are that your team, if not already into FP, will find it easier to read. It looks like:

```javascript
modifiedUser = pipe(
  user,
  addReport(reportA),
  addReport(reportB),
  removeReport(reportA)
);
```

Note how the first argument is taking a value rather than function/transformation/modifier. This works really well with TypeScript's type inference. I actually prefer `pipe` also for JS nowadays, as I'm so used to it. The implementation of pipe, if you don't get it from a library, is rather simple:

```javascript
const pipe = (value, ...fns) =>
  fns.reduce((acc, fn) =>
    fn(acc), 
    value
);
```

In TS you'd need to add quite a bit of code for the typing to work, but, a trick here is to ask your favorite AI to generate it for you, as you will need overloads for as many arguments as someone may want to use your pipe function with.

In Haskell I tend to use composition as it can be expressed concisely and I got used to it:

```haskell
modifiedUser = 
    removeReport reportA
  . addReport reportB
  . addReport reportA
  $ user
```

When using Java and JavaScript I typically don't bother to use higher-order functions (functions that take and/or return functions) until needed for some reason. But I still think of my functions/methods as transformations closed under a type, or modifiers of the type I'm working on, e.g. a User. 

Indeed, a classical `setId` of a user which mutates the user is totally fine, because mentally you can still think of it as a transformation from User to User.

Another distinction I find helpful is core and non-core transformations. By identifying core, or low-level, transformations you can often create other transformations that are simply combinations of core transformations. In my experience this often leads to more concise solutions that are easier to maintain. Sometimes you can compose functions directly, but even if not, don't worry if you have to create a new function which just calls other functions, you're still just composing core functions. 

There are combinators (with funny bird names) for combining functions/transformations in various ways, but, I find it rarely worth using combinators beyond normal composition and perhaps `on` in Haskell, which allows you to do things like: (compare \`on\` length) "hej" "abc" == EQ. This would return true, since we are comparing "hej" and "abc" based on their length by passing the arguments through length.

Transformations closed under a type can only take you so far. At some point we need transformations between types, let's continue our excursion in the world for transformations by looking at that next.

## Transformations From A to B

Many transformations are between types. An obvious example is from String to JSON, and then to some type, such as a User. These transformations may be expressed in functions or methods naturally living somewhere else than the entity, e.g. in a Service/API file. But they may also be part of the entity or a DTO, especially if a simple transformation is possible, such as from a DTO to Entity or vice-versa, where all needed information is contained in both. For instance, in Java you may have a toDto function defined in your `User.java` file:

```java
// User.java

// ...

UserDTO toDto() {
  return new UserDTO(...);
}
```

Or you may have a `UserDtoAssembler.java` which offers a `toDto` method, but perhaps also depends on some other services. I sometimes also create a `UserDtoCodec.java` where I put a `toDto` and `fromDto`, inspired by IO-TS. While important, especially at large scale, where something lives or what it is called is just a matter of naming and moving it around; more important is getting the representations and transformations right.

I also find it useful to think about properties of transformations. E.g. is it destructive (losing information), is it isomorphic (we can go back and forth between A and B), and any other properties that can either be useful when coding or for testing it. In particular, making a relation isomorphic can be very useful, because the test of simply checking:

```javascript
decode(encode(data)) === data
```

tends to be quite powerful. Where `decode` is the inverse of `encode`. I have seen plenty of cases where types were nearly isomorphic, e.g. a single small value, perhaps not even important, was missing.

One reason for why it is useful to think of properties such as if two types could, or should, have an isomorphic relation between each other, is that you may make a conscious decision to include a little extra data, just to make an isomorphic relation possible. Also, if you have many isomorphic relations between types, perhaps you have unnecessary types, so you may also seek opportunities to reduce the number of types. But it doesn't have to be all or nothing, you can also have "fake" isomorphism, where your converter just loads a little extra data behind the scenes, but from the perspective of calling code, is isomorphic, i.e. it is trivial to go back and forth between the types. Just keep in mind that sometimes you want to make it explicit by passing in the extra data as arguments, so that nobody will make incorrect performance assumptions; this is especially true if the language you use does not support communicating effects in the type signature (e.g. writing `TypeA -> IO TypeB`).

This is perhaps a bit abstract, but, I have found it really helpful to think about relations between types and properties these relations have, to keep my solutions as simple as possible.

Transformations sometimes need to be used in different contexts. E.g. you might need to add 5 to a number to a single number, as well as to all numbers in a list of numbers. Let's continue our exploration by looking at the case of applying transformations to a type in a type.

## Transformation Inside a Container

I always seek simplicity when producing software, both in the code and resulting product. I believe anyone would agree that transforming `Users` to `UserDTOs` is more complex than transforming a `User` to a single `UserDTO`. We're just dealing with more concepts and data, in particular, the concept of a collection and `User`, as well as how to apply the transformation to each element in the collection. 

But if you think about it, how to apply a transformation to each element in a collection is a separate problem, it is distinct from the transformation of an element itself. If we can just find the right abstraction, there's no need to think about transforming `Users` to `UserDTOs`, we can just combine the solution of applying a transformation to each element with a transformation `User -> UserDTO`. This means that we can write fewer and simpler transformations ourselves.

One often says that a type that contains a type, for which we can "lift" a function to operate on the type contained in the type, is a functor. It simply means that there is some function that can apply a function that operates on the contained type, e.g. all elements in a List. Some examples:

```javascript
[1,2,3].map(x => x + 1);
Promise
  .resolve(5)
  .then(x => x + 1);
```

First line shows how we increment each element in a list. Second and subsequent rows show how we can use the same function (`x => x + 1`) to increment a value contained in a `Promise`. In Haskell it is even more elegant with its type classes:


```haskell
fmap (+1) [1,2,3]
fmap (+1) (Just 5)
fmap (+1) (Right 5)
```

Again, the first line shows how we increment all elements in a list. Second shows how we increment the value in a `Maybe Int` and third how we increment the value in an `Either a Int`, where `a` can be any type. In Haskell you can also do `(+1) <$> [1,2,3]` which I find quite neat, where `<$>` is an infix version of fmap.

Lifting a function, or taking a function and producing a new function, is an example of using a higher-order function. E.g. `fmap (+1)` takes the function `(+1)` and makes it, instead of taking an integer as argument, take for instance a list of integers. Or more generally, in Haskell, anything that is an instance of the Functor type class. The ability to re-use the simplest possible transformation in different contexts means you can keep your code conciser and simpler.

A key concept in functional programming that brings a lot of value is composition. The ability to compose functions. In Haskell, you typically use composition directly a lot. In Java likely not so much. However, having used a lot of composition in Haskell and JavaScript has helped me produce simpler code in Java too, sometimes by using composition directly, sometimes indirectly, and sometimes by just paving the way to be able to do so if needed at some point. Let's continue with a brief discussion of what it means for transformations to be composable.

## Composable

To produce composable transformations, or functions, it helps to be deliberate about the order in which you take arguments. I often think of it as first configuring my function and then supplying the data it operates on. Consider the following function which replaces words in a string given a dictionary:

```javascript
replaceWords(
  inputString, 
  replacementDict) {
  const pattern = new RegExp(
    Object.keys(replacementDict)
	  .join("|"), 
    "g"
  );
    
  return inputString
    .replace(
      pattern, 
      matched => 
        replacementDict[matched];
  );
}
```

Note that we can not compose this with other functions transforming our inputString, e.g. we can't easily apply it twice with different dictionaries and then get the length of the string. There are two issues, the first is that it takes two arguments, and composition only works with one. The second issue is the order of the arguments. It doesn't naturally compose. Let's try again:

```javascript
replaceWords = replacements => {
  const pattern = new RegExp(
    Object.keys(replacements)
      .join("|"), 
    "g"
  );
  return input => input
    .replace(
      pattern, 
      matched => 
        replacementDict[matched];
    );
}
```

by placing the string as the last argument, and instead of taking two arguments, returning a function which takes the next argument, we can easily compose with other functions:

```javascript
pipe(
  "Hello world, 
   welcome to 
   the universe.
  ",
  replaceWords(
    {Hello: "Hi"}
  ),
  replaceWords(
    {Hi: "Hej"}
  )
);
```

However, note that this is equivalent to:

```javascript
let res = replaceWords(
  {Hello: "Hi"}, 
  "Hello world, 
   welcome to 
   the universe.
  "
)
res = replaceWords(
  {Hi: "Hej"}, 
  res
);
```

or

```javascript
replaceWords(
  {Hi: "Hej"}, 
  replaceWords(
    {Hello: "Hi"}, 
    "Hello world, 
     welcome to 
     the universe.
    "
  )
);
```

While using `pipe` may look nicer, the solutions are isomorphic (we can easily jump between them) in practice. The main thing isn't how you express it (syntax), but that you can easily compose the transformations. I still recommend thinking of it as composition using `pipe` or `compose` though, as it means you get a clear guideline for argument order: always put the data operated on as the last argument. When writing, for instance, Java, I often think of how I would have wanted to write it if I could write it in whatever way I wanted, and then choose what is convenient and ideally idiomatic in Java. Never try to force a way of expressing something if it makes the code hard to write and/or read. But also never stop questioning whether the way you are doing something in makes sense, or if there are better ways.

Finally, let's top all this up with a brief discussion about type of types, as I have found it helpful sometimes when writing composable code.

## The Canonical Product and Sum Type

Sometimes useful, especially if you feel restricted by the language you use, is to note that types we typically use are just special cases of two types, namely the product and sum types. A product type is the typical tuple: `(a, b)`. Note that `b` can be yet a tuple, e.g. `(c, d)`, which would give us: `(a, (c, d))`. We can thus model arbitrarily many values. This is equivalent to a record type. To see this, consider adding a few getters and naming things differently:

```javascript
const user = ["fdsa", [42, "Berlin"]];
const getId = u => u[0];
const getAge = u => u[1][0];
const getLocation = u => u[1][1];
```

Perhaps this shows why Pairs are so common in code bases, they are essentially record types where someone didn't find it worth it to introduce a new named type. So a tuple, which you may need to model as an array in some languages, is the canonical product type, or, record type.

Sum types are perhaps less prevalent, but still common. They can be used in most languages. The canonical example is `Either`, which allows you to express that either the left or right value is available, and never both. E.g. `Either Void a` is equivalent to `Maybe a`. `Either Error Success` could be used to model that we get either an Error or Success type. Some languages give you these out of the box, whereas in others you can use them by relying on records/objects. Sometimes, for instance in TypeScript, this is called "discriminated union". A small example:

```javascript
/*
APIResult<A> {
  type: "success"|"error",
  errorCode: Number,
  errorMessage: string,
  value: A
}
*/

switch (apiResult.type) {
  case "success":
    // refined to {value}
    return 
      handleSuccess(apiResult);
  case "error":
    // refined to
    // {errorCode, errorMessage}
    return 
      handleError(apiResult);
}
```

Of course, if the language supports sum types, or there are good libraries around (such as FP-TS), it is more convenient to use these solutions. But note that creating sum types, given product types, is very easy. You simply introduce a value which decides which type your sum type is.

In case you are curious, "product" comes from that the number of states is the product. E.g. `{boolean, number}` gives `2*#number` states. But `Either boolean number` gives `2+#number` states. Where `#number` is the number of possible numbers.

## Conclusion

Thinking of transformations as something more than merely functions can help you produce simpler and better code. Sketching out transformations and representations can be a great design tool. To evolve how you think about transformations, I encourage you to think of types of transformations and find cohesive sets of transformations. It is all a bit abstract, but, it expands how you can think about things, and it is something that has made it easier for me to produce simple and sensible solutions. It may not be everyone's cup of tea, but similar to how studying mathematics can make you a better problem solver, I think thinking about things in different ways, sometimes at a high abstraction level, can help you become a better developer.
