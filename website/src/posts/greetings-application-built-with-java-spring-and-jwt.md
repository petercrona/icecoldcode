---
title: "Greetings Application built with Java, Spring and JWT"
published: 2024-07-17
---

A demo is at the moment [available here](https://icecoldcode-spring-jwt-test.ey.r.appspot.com/) (**note that a new instance may spin up, so you might need to wait ~10s**). All code is available on [Github](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings).

From time to time it is a good idea to start something from scratch, or at least go one level lower than you typically do. This article reports back on how I set up a REST-API with Java, Spring and JSON Web Token (JWT) to build a Greetings application. The end result isn't perfect, but I think it can be useful for anyone wanting to see how it can be done in a fairly clean way, as well as see a few other goodies, such as using Nix Flakes to improve the developer experience. I also built a basic front end using VanillaJS.

## A Greetings Application

First step for any project is to figure out what to do. "Hello World" is a little too basic for having any fun, so I decided to create a little Greetings Application. It has the following requirements:

1. As a visitor I want to see a list of greetings.
2. As a user I want to post greetings.
3. As a user I want to be able to delete my own greetings.
4. As an admin I want to be able to delete greetings posted by anyone in my company.

Worthwhile to define:

* **Visitor**: Anyone visiting the page
* **User**: Anyone authenticated, associated with a company.
* **Admin**: Authenticated with specific admin role, associated with a company.

And finally, some very non-functional requirements:

1. Code must be straight forward and minimalistic
2. Instances must be stateless (because it is much nicer so!)
3. VanillaJS for front end (having a front end was added as a fun bonus)

## Solution

A demo is at the moment [available here](https://icecoldcode-spring-jwt-test.ey.r.appspot.com/). All code is available on [Github](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings), and if you have Nix you can easily run the project by:

```bash
cd projectFolder
direnv allow
./gradlew bootRun
```

if you prefer to run it in an IDE, so you can easily debug or play around with the code:

```bash
idea-community .
<click run on Application.java>
```

Perhaps you can also run it directly using Gradle, depending on your system. Pretty nice developer experience right, isn't it?

### Developer Experience

I'm a big fan of Nix. This blog post is written on a laptop running NixOS. And the project is using a Nix to make it easy to run.

Nix is a tool which allows us to create reproducible builds. However, in this case, I'm not using it for that, but rather to conveniently set up a development environment. 

Sometimes it can be hard to go all in with Nix for a project, but even then, I find it super useful to use Nix to install the system packages I need. The result is that I, or any other developer, can simply go into the project folder and all necessary system packages (such as JDK 21 and IntelliJ IDEA and Google Cloud SDK) will be made available. How does it work you may wonder, well here are the relevant files: 

* [flake.nix](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/flake.nix): defines the dev shell, which includes what system packages shall be available. This is where we add Idea for instance.
* [.envrc](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/.envrc) This ensures that we get the shell set up automatically when entering the folder (with command-line or other supported software). This is important also since it together with [nix-direnv](https://github.com/nix-community/nix-direnv) makes sure our environment doesn't have to be rebuilt when someone runs a `nix-collect-garbage`. The authors put it nicely: "Life is too short to lose your project's build cache if you are on a flight with no internet connection".

This setup allows any users with Nix (and flake support) to do:

```bash
cd project
idea-community .
<click run in Idea>
```

No need for the right JDK to be installed, and no problem if another version of JDK was already installed. 

Before jumping into the application code, let's have a quick look at JSON Web Token (JWT), as it is an important foundation for understanding the rest.

## JSON Web Token

JSON Web Token (JWT) may sound like something complicated if you never used it, but if that is the case, it is probably because you are used to things being very complicated nowadays. But JWT is actually very simple, both conceptually and to actually use. I recommend you to have a look at [jwt.io](https://jwt.io/) for how JWT works. But, to get the concept, let's roll our own trival implemenation of something similar to JWT:

```javascript
myObject = {id: 2, isAdmin: true};
jwt = {
  ...myObject, 
  signature: hmac(
    SHA256, 
    mySecretKey,
    JSONToString(myObject)
  )
};
```

when the server, or anyone with the `mySecretKey`, is given the request, it can now confirm that `myObject` has not been touched. It does so by calculating the signature and ensuring it corresponds with the included. Since only those with `mySecretKey` can calculate signatures, only they can manipulate the contents and set a valid signature. So any modification by an unauthorized party can easily be detected.

The real JWT implementation has a simple structure with a header, body and signature, which makes it nicer to work with. Check out [jwt.io](https://jwt.io/), where you can play around with a JWT. 

Now that you know about JWTs (which are basically just signed cookies), let's jump to the application code.

## Spring Boot

This was not my first time trying Spring Boot, but wow, it is so much nicer to not having to deal with any XML files (at work we use Spring MVC without Spring Boot), but have it all in the code. Although, I must say, I'm not the biggest fan of annotations, but would rather configure using normal code the whole way. Let's explorer the application by roughly following how boot and requests go through it.

1. It all starts with [Application.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/Application.java) with an ordinary `public static void main`. This is so beautiful compared to messing around with XML based servlet configs.

2. Spring Boot configures things with sensible defaults so that you don't need to configure everything. And it automatically finds things that have certain annotations. This can make it very difficult to understand what is going on. You may need to find a lot of files and jump around between them. Luckily, for this little application you only need to read [AuthenticationConfig.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/infrastructure/AuthenticationConfig.java), which sets up essential beans to configure Spring Security, in particular, it adds [JwtAuthenticationFilter.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/infrastructure/JwtAuthenticationFilter.java).

3. [JwtAuthenticationFilter.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/infrastructure/JwtAuthenticationFilter.java) is what sets up authentication given a JWT, before the request reaches your application code. This is done by calling:

    ```
    SecurityContextHolder
      .getContext()
      .setAuthentication(auth)
    ```

    which ensures that that Spring (Security), and your code, is aware of the authentication once the request reaches your application code. This class also updates the expiry of the JWT, and reloads it completely if too old. This is also where you would add a blocklist if you want a mechanism to invalidate JWTs. Alternatively, you could validate the JWT on every request by reloading info (such as roles) from the DB, if you are willing to take the performance hit for doing so. Most of the work dealing with the token itself is handed off to [JwtService.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/JwtService.java).

4. [JwtService.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/JwtService.java) is responsible for reading and writing the token from/to a cookie. It is the only place that knows what the cookie is called. It uses [Principal.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/Principal.java) in its interface to other classes. E.g. it attemps to read a Principal from the cookies, and either fails or succeeds, as indicated by it having the return type `Optional<Principal>`. It also ensures the cookie is HTTPOnly (not accessible by client-side JavaScript) and Secure (only allowed on HTTPS). This protects against someone eavesdropping (for instance on a public Wi-Fi) or the token leaking through XSS vulnerabilities. Let's look at the actual authentication API now.

5. [AuthController.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/AuthController.java) offers an API to authenticate. To log in you POST credentials to `/auth`, to see if you are logged in you `GET /auth` and to log out you `DELETE /auth`. Arguably, it would make more sense to do `GET /auth/1` to get your credentials, as `GET /<resource>` would typically give you a collection. I may update this! One of the most beautiful parts of REST is the uniform interface in my opinion, which I'm breaking with this endpoint at the moment. Let's finally get to the Greetings API.

6. [GreetingsController.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/GreetingsController.java) sets up the API for greetings. It barely implements any logic itself, but uses others for it. However, I did write a few tests in [GreetingsControllerSpec.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/test/java/com/icecoldcode/api/greeting/GreetingsControllerSpec.java) for it, because it must still do an authorization check for deletion and ensure that created greetings have the principal as author. Some folks prefer to put this kind of logic, such as performing an authorization check, in a separate service. I tend to be a bit flexible, as long as I know I can easily break it out to a separate service later if needed. I'm flexible mainly because I dislike having many files with nearly no content. But, I'm also against "fat controllers", so, the controller must be very concise and simple, otherwise it is definitely time to move things into a service.

Phuh, that's a lot. There's more, but, if you are that interested, you probably will check out the code anyway. Next I'll try to point out a few things in shorter chunks that you may find interesting. Feel free to jump through the sections based on interest.

## Repository

I store all data in-memory, but, this is only known to [GreetingRepositoryInMemory.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/infrastructure/GreetingRepositoryInMemory.java) and [UserRepositoryInMemory.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/infrastructure/UserRepositoryInMemory.java). And swapping these out for something that interacts with PostgreSQL or MongoDB would be trivial.

I think most people separate out where interaction with databases is happening somehow. After having read [Domain Driven Design by Evans](https://www.goodreads.com/book/show/179133.Domain_Driven_Design) I became a big fan of repositories as the abstraction for this. The idea is to have an interface for getting/saving data, and you use it with Dependency Inversion.

You consider the Repository as part of your domain layer, e.g. UserRepository to load users. While it may sound inconsequential, it actually is pretty helpful in my experience. By making it a part of your domain, you think more of it as a domain concept and less as a technical thing. You think about how you are loading things to produce domain relevant objects, and it is easier to decouple it, to distinguish it from something that just loads an entity as stored in your DB. In Domain Driven Design you say it is for loading aggregate roots (domain concepts that may consist of multiple persisted entities). Ideally, you also make it part of the ubiquitous language, a language shared between technical and non-technical staff.

The actual implementation of a repository lives in what is often called the Infrastructure layer, that is, outside the domain layer. In DDD, your goal is to protect the domain layer, so, you want to avoid dependencies outwards from the domain layer, as that would mean the domain layer must adapt to changes in other layers. So, you make the repository interface part of the domain, but then let someone else (probably you later) implement the interface. And you don't care if they get data from MySQL, PostgreSQL, MongoDB or in-memory (as I!). See [GreetingsRepository.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/GreetingsRepository.java) and [GreetingsRepositoryInMemory.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/infrastructure/GreetingRepositoryInMemory.java). We already touched a bit on layers, which I tend to try to reflect in the package structure. A little bit less for this Greetings application though, as I like to start simple and only use a more complex structure as things get messy. But let's discuss what we have here in terms of Java package structure.

## Package Structure

At top-level [com.icecoldcode](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode) you are met by [Application.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/Application.java) and [IndexController.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/IndexController.java). This is due to that these are very high-level concepts. One class is reponsible for booting, and the other is the main entrypoint for the front end. I want the reader to quickly find these files, so they can start from there when exploring the application. In an ideal world, by following the code, but with Spring, you of course need to know a little bit about annotations to get a full high-level understanding of the system.

Some central concepts were put in [com.icecoldcode.core](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core). This is the place for things that are re-used all over the place. Core is also home to [com.icecoldcode.core.authentication](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication). This is to signal that it is part of the core, and not meant to be messed around with in the day-to-day feature development. Core is there to support he system and features, but it must not now anything about features and generally shouldn't have outwards dependencies.

Unfortunately, I find it not so convenient to limit visibility into packages in Java. I suppose you can do it nowadays with Java modules, but I haven't gotten around to fully explorer how convenient it is. So, for simplicity, I opted for calling packages `internal` when I want to signal that they are not intended to be used by others. You also find [c.i.c.a.internal.infrastructure](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/infrastructure) (shortend to fit the screen) which is the home for things interacting with the outside world from the perspective of the [authentication](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication) package. E.g. Spring is configured here, and the [UserRepositoryInMemory](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/core/authentication/internal/infrastructure/UserRepositoryInMemory.java) is implemented in the infrastructure layer/package. Of course, in-memory isn't really the "outside world", but, normally this would be where we get data from a database.

Another important package is [com.icecoldcode.api](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api). It is meant to make you think of things under the URL `/api`, so the actual product, the actual API offered to some front end. This is where features would be implemented, and this is indeed where the Greeting feature lives. Because Greeting is so simple, it only has one subpackage, namely `.infrastructure`, for the repository implementation. If we were to split it up more, we could introduce the following packages:

* `.application` for application-level services. This would take parts from the controller, such as ensuring that we check whether someone may delete a greeting before doing it.
* `.domain` for domain concepts or logic. [Greeting.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/Greeting.java), [GreetingAuthorizationService.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/GreetingAuthorizationService.java), and [GreetingsRepository.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/GreetingsRepository.java) would move in here.
* `.` the top-level, i.e. [.greeting](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting)`, would have the DTOs and Controllers. This makes it easy to find the entry points for requests, and the most relevant types/classes for using these.

A package structure should be thought of as a product for collegues in my opinion. You want to make it easy to find things and for people to know where to put things. If it can also communicate something about dependencies and dependency direction, that is a big plus.

An essential part of any application is its tests. Not only are they typically the best and most efficient way to ensure correctness (especially over time), but they also help you identify flaws, as good well-designed code generally is easier to test than bad code.

## Tests

I went for a blend of integration and unit tests. Given that I use in-memory database, it is very easy to run tests. In reality it may be a bit harder to have an in-memory database for tests, although, it can probably be solved with Spring profiles and clever design (a challenge is queries or lookups not using primary key).

For the integration tests, you'll find [AuthenticatedTest.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/test/java/com/icecoldcode/AuthenticatedTest.java) and [UnauthenticatedTest.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/test/java/com/icecoldcode/UnauthenticatedTest.java). They are split due to differences in setup (the `AuthenticatedTest` needs users). One of my main goals when writing tests is that the tests are very easy to read (and ideally write). You can see that I broken out helper functions to aid readability of test cases. And, likewise, the setup for the [AuthenticatedTest.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/test/java/com/icecoldcode/AuthenticatedTest.java) is to keep the test cases as readable as possible. For selection of test cases, I try to think of equivalence classes, core use cases, and important properties that should always hold. I often think of [Hoare triples](https://en.wikipedia.org/wiki/Hoare_logic) when writing tests.

I did also add a few unit tests in [GreetingAuthorizationServiceSpec.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/test/java/com/icecoldcode/api/greeting/GreetingAuthorizationServiceSpec.java) and [GreetingsControllerSpec.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/test/java/com/icecoldcode/api/greeting/GreetingsControllerSpec.java). The reason for `GreetingsControllerSpec.java` is that it does need to make sure that if another service, namely [GreetingAuthorizationService.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/GreetingAuthorizationService.java), disallows a delete, a delete must not happen. And if a Greeting is created, the greeting must have the right author set. These are highly important properties, so they deserved a test.

[GreetingAuthorizationService.java](https://github.com/petercrona/icecoldcode/blob/main/examples/java-spring-jwt-greetings/src/main/java/com/icecoldcode/api/greeting/GreetingAuthorizationService.java) contains authorization logic, which is a good tell-sign for that it deserves tests. The authorization logic is also important domain logic, which is yet a sign that it deserves a test. For instance, Admin is a concept in our domain, and an Admin may delete any greeting in the same company. But an ordinary user may only delete their own. This is how you'd explain it to non-technical people, and this is how the end-user experience it, so, we want to test this logic. Also here I put some effort into making test cases very easy to read and understand. Let's end with a tiny discussion about the front end.

## Front End in VanillaJS

For fun, I wanted to write a basic front end in VanillaJS. A while ago I wrote [O2FP](https://github.com/petercrona/o2fp) to go a bit more low-level in web development, as I'm typically using ReactJS. Given that this Greetings application is very simple, I figured I'll try to write something in VanillaJS. It was definitely more work than writing the same in ReactJS, but, it wasn't too bad. With practice, I guess the difference in effort needed would be quite small. Of course, this is a very simple application. But, writing VanillaJS today is much better than in the past. 

Just to give a brief overview: I went for something inspired by components, but, still relying a lot on the browser. For instance, I keep the form state in the browser completely, and simply reset the form after a successful submit. I used a custom event for when authentication changes, and let my "components" listen on `document` for it. And I use no virtual DOM, but just clear the HTML when I want to update the greetings list.

The markup and JavaScript lives in separate files. I'm not completely happy with this, because they are so tightly coupled. So perhaps an improvement would be to build the HTML in JS as well in this case. I'm sure I'll do it a bit different and better next time! But the current solution was not too bad. Not that much code was needed for an OK UI. I'm a little dissapointed that the `<dialog>` element doesn't work perfectly on mobile, because if it would, it would be a really convenient element.

## Conclusion

This article went on for a bit longer than expected. We covered many pieces, starting with specifying what this Greetings Application is by listing requirements. Then we discussed developer experience with Nix, JSON Web Token (JWT), Spring Boot and key parts of my solution. We had a little excursion in discussing the repository concept, package structure, and ended with talking about tests.

I'm quite satisfied with the result, but if I would it again, I'm sure I'd be able to do a few things better already. I still think it is good enough for less experienced people to learn a thing or two from though. One lacking thing is error handling. I'm a big fan using the Either (canonical sum type) for errors, and started to refactor to use it, but ran out of time. With Fugue from Atlassian you can do error handling pretty nicely if you also use their `Checked` class in my opinion (especially if you aren't a big fan of excessive exception based error handling). 

Another non-ideal thing is that I didn't introduce a separate `User` concept, but re-use the `AuthUser` from the `Authentication` package. This creates some odd coupling (at the moment of writing, a dependency from Greetings to something in the authentication's internal package even), but I took advantage of that I don't need any additional user data for now. And there's nothing stopping the design to evolve into creating a separte `User` concept, as opposed to bloating the `AuthUser` or `Principal`.

I hope you learned a few things from this article. And if you didn't already, do [check out the code](https://github.com/petercrona/icecoldcode/tree/main/examples/java-spring-jwt-greetings), try to run it (ideally with Nix), and play around with it. Or even better, build your own Greetings application from scratch.
