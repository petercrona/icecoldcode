{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import nixpkgs {
            inherit system;
          };
          gdk = pkgs.google-cloud-sdk.withExtraComponents( with pkgs.google-cloud-sdk.components; [
            google-cloud-sdk.components.app-engine-java
          ]);
        in
        with pkgs;
        {
          devShells.default = mkShell {
            buildInputs = [
              jdk21
              jetbrains.idea-community
              google-cloud-sdk
            ];
          };
        }
      );
}
