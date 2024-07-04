# Hello there

This is the repo for my [personal blog](https://www.icecoldcode.com/). 

I figured I'll share the code in case useful for anyone else trying to learn [Hakyll](https://jaspervdj.be/hakyll/).

Note that `deploy.config` was omitted, it is a file containing:

```
bucketId="<BUCKET ID>"
distributionId="<DISTRIBUTION ID>"
```

# Cool stuff
I'm using Nix (see `flake.nix`) and [nix-direnv](https://github.com/nix-community/nix-direnv). 
This means that as soon as I enter the directory, either with terminal or Emacs, the environment is set up as per my `flake.nix`.
As a result, I don't need to worry about what I have installed and which versions. Even if I have no GHC, cabal, or awscli installed, I can do:

```
cd icecoldcode
watch
# work a bit
deploy
```

There's a bit of set up required for this to work. You must install `Nix` and `direnv`, and also activate Nix flakes. But this is a one-off set up, and not per project. Definitely worth it! Credit to Robert Pearce who created a [Hakyll starter template](https://github.com/rpearce/hakyll-nix-template). I ended up not using this though, as I wanted something more minimal, which I myself can slowly grow as I need more things.

## Solution to: My Haskell dependencies are garbage collected

I'm running NixOS on my laptop, and had issues with that I had to re-compile a lot of stuff after having done `nix-collect-garbage -d`. 
Was solved by using [nix-direnv](https://github.com/nix-community/nix-direnv), and life is now great.

## Solution to: `direnv` doesn't allow aliases and I'm to lazy to type `nix develop` every time

Since aliases are not possible in direnv configured shells, see `.envrc` and the `commands` folder for how I "solved" it. Rather than using real aliases, add a folder to PATH using `PATH_add <PATH>` in your `.envrc`.

## Solution to: Visitors must clear their cache to get my latest CSS 

Finally, a noteworthy thing is how I add a MD5 (we're after cache busting, not security!) hash to the CSS file name. I also configure Hakyll to know that there's a dependency between my templates and the CSS, so that the templates will be recompiled if I change the CSS (we must update them with the new file name). Credit to Jezen Thomas who wrote [Static Asset Hashing in Hakyll](https://jezenthomas.com/2022/08/static-asset-hashing-in-hakyll/), which outlined the solution.
