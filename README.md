# Omnilook

A simple freelook/perspective mod for Minecraft.

https://github.com/user-attachments/assets/cb2b3255-b664-4bfd-8992-07aef30071f3

## Version support

| Mod Loader              | Versions          | Notes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|-------------------------|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NeoForge                | 1.20.4+           | Requires NeoForge 20.4.195+ on 1.20.4                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| MinecraftForge/LexForge | 1.7.10-1.20.5     | <ul><li>1.14.4-1.15.1 require <a href="https://modrinth.com/mod/mixinbootstrap">MixinBootstrap</a></li><li>1.14.0-1.14.3 are not supported<ul><li>Forge doesn't support 1.14.0 & 1.14.1</li><li>1.14.2 & 1.14.3 have no Mixin loaders</li></ul></li><li>1.13.X requires <a href="https://modrinth.com/mod/modernmixins">Modern Mixins</a></li><li>1.8.9-1.12.2 require <a href="https://modrinth.com/mod/mixinbooter">MixinBooter</a></li><li>1.8.0-1.8.8 have no Mixin loaders</li><li>1.7.10 requires <a href="https://modrinth.com/mod/unimixins">Unimixins</a></li></ul> |
| Fabric                  | 1.6.4-1.12, 1.14+ | <ul><li>Requires <a href="https://modrinth.com/mod/fabric-api">Fabric API</a> on 1.14+</li><li>Requires <a href="https://modrinth.com/mod/legacy-fabric-api">Legacy Fabric API</a> on 1.6.4</li></ul>                                                                                                                                                                                                                                                                                                                                                                        |
| Quilt                   | 1.14.4+           | Same as above. Fabric API seems to be broken on Quilt before 1.14.4.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| Babric                  | b1.7.3            |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Rift                    | 1.13.2            | *Might* work on older versions but I haven't tested                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| LiteLoader              | 1.12.2            | Same as above. I'd like to have more version support but since LiteLoader uses official/obfuscated mappings, it's hard to support multiple versions.                                                                                                                                                                                                                                                                                                                                                                                                                         |

More version support is always planned!

## Credits
Thanks to [Nolij](https://github.com/Nolij) and his mod [Zume](https://github.com/Nolij/Zume)
for the inspiration/idea of large multiversion mods such as this, and many strategies that I use to implement it.


## License
This software is dedicated to the public domain using the [Unlicense](LICENSE). All code is written by me with no references to other code other than that I depend on.
