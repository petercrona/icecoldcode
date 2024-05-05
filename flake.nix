{
  description = "Example Nix Flake for Haskell";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixpkgs-unstable";
  };

  outputs = { nixpkgs, ... }:
    let
      system = "x86_64-linux";
      ghcVersion = "ghc98";
      pkgs = import nixpkgs { system = system; };
      
      example-haskell-nix-flake = pkgs.haskell.packages.${ghcVersion}.developPackage {
        root = ./.;
        returnShellEnv = true;
        modifier = drv: pkgs.haskell.lib.addBuildTools drv [ pkgs.haskell.packages.${ghcVersion}.cabal-install
                                                             pkgs.haskell.packages.${ghcVersion}.haskell-language-server
                                                             pkgs.awscli2
                                                           ];
      };
    in
    {
      devShells.${system}.default = example-haskell-nix-flake;
    };
}
