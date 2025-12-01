(ns rt.nix.script-test
  (:use code.test)
  (:require [rt.nix.script :refer :all]))

^{:refer rt.nix.script/emit-nix :added "4.0"}
(fact "emits a nix config"
  (emit-nix {:a 1})
  => "{\n  a = 1;\n}\n"

  (emit-nix {:a "hello"})
  => "{\n  a = \"hello\";\n}\n"

  (emit-nix {:a true})
  => "{\n  a = true;\n}\n"

  (emit-nix {:packages [:pkgs.vim :pkgs.git]})
  => "{\n  packages = [pkgs.vim pkgs.git];\n}\n"

  (emit-nix {:nested {:a 1}})
  => "{\n  nested = {\n    a = 1;\n  };\n}\n")

^{:refer rt.nix.script/write :added "4.0"}
(fact "link to `std.make.compile`"
  (write {:boot.loader.systemd-boot.enable true
          :boot.loader.efi.canTouchEfiVariables true
          :networking.hostName "nixos"
          :environment.systemPackages [:pkgs.vim :pkgs.wget]})
  => (str "{\n"
          "  boot.loader.efi.canTouchEfiVariables = true;\n"
          "  boot.loader.systemd-boot.enable = true;\n"
          "  environment.systemPackages = [pkgs.vim pkgs.wget];\n"
          "  networking.hostName = \"nixos\";\n"
          "}\n"))

^{:refer rt.nix.script/write-fn :added "4.0"}
(fact "emits a nix module function"
  (write [:fn [:config :pkgs :...]
          {:home.username "alice"
           :home.homeDirectory "/home/alice"
           :home.packages [:pkgs.htop :pkgs.ripgrep :pkgs.jq]
           :programs.git {:enable true
                          :userName "Alice Dev"
                          :userEmail "alice@example.com"}
           :programs.bash {:enable true
                           :shellAliases {:ll "ls -l"
                                          :.. "cd .."}}
           :home.stateVersion "23.11"}])
  => (str "{ config, pkgs, ... }:\n\n"
          "{\n"
          "  home.homeDirectory = \"/home/alice\";\n"
          "  home.packages = [pkgs.htop pkgs.ripgrep pkgs.jq];\n"
          "  home.stateVersion = \"23.11\";\n"
          "  home.username = \"alice\";\n"
          "  programs.bash = {\n"
          "    enable = true;\n"
          "    shellAliases = {\n"
          "      .. = \"cd ..\";\n"
          "      ll = \"ls -l\";\n"
          "    };\n"
          "  };\n"
          "  programs.git = {\n"
          "    enable = true;\n"
          "    userEmail = \"alice@example.com\";\n"
          "    userName = \"Alice Dev\";\n"
          "  };\n"
          "}\n"))

^{:refer rt.nix.script/write-full :added "4.0"}
(fact "emits a full nixos config"
  (write [:fn [:config :pkgs :...]
          {:imports [[:path "./hardware-configuration.nix"]]
           :boot.loader.systemd-boot.enable true
           :boot.loader.efi.canTouchEfiVariables true
           :networking.hostName "my-nixos-machine"
           :networking.networkmanager.enable true
           :time.timeZone "America/New_York"
           :users.users.alice {:isNormalUser true
                               :description "Alice"
                               :extraGroups ["networkmanager" "wheel"]
                               :packages [:with :pkgs [:firefox :git :vim]]}
           :services.openssh.enable true
           :nixpkgs.config.allowUnfree true
           :system.stateVersion "23.11"}])
  => (str "{ config, pkgs, ... }:\n\n"
          "{\n"
          "  boot.loader.efi.canTouchEfiVariables = true;\n"
          "  boot.loader.systemd-boot.enable = true;\n"
          "  imports = [./hardware-configuration.nix];\n"
          "  networking.hostName = \"my-nixos-machine\";\n"
          "  networking.networkmanager.enable = true;\n"
          "  nixpkgs.config.allowUnfree = true;\n"
          "  services.openssh.enable = true;\n"
          "  system.stateVersion = \"23.11\";\n"
          "  time.timeZone = \"America/New_York\";\n"
          "  users.users.alice = {\n"
          "    description = \"Alice\";\n"
          "    extraGroups = [\"networkmanager\" \"wheel\"];\n"
          "    isNormalUser = true;\n"
          "    packages = with pkgs; [firefox git vim];\n"
          "  };\n"
          "}\n"))
