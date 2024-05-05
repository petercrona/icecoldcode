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
This means that as soon as I enter the directory, either in the terminal or Emacs, the environment is set up as per my `flake.nix`.
As a result, I don't need to worry about what I have installed and which versions. Even if I have no GHC, cabal, or awscli installed, I can do:

```
cd icecoldcode
cabal run . watch
# work a bit
./deploy # must have set up credentials for AWS
```

I'm running NixOS on my laptop, and had issues with that I had to re-compile a lot of stuff after having done `nix-collect-garbage -d`. 
Was solved by using [nix-direnv](https://github.com/nix-community/nix-direnv), and life is now great.
