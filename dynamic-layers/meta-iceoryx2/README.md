# meta-iceoryx2 support for NVIDIA Tegra (Jetson) platforms

This dynamic layer is only parsed when the
[meta-iceoryx2](https://github.com/nvidia-holoscan/meta-iceoryx2) layer is in
your BitBake layer configuration. It adds the HoloMQ compatibility patches
that Holoscan SDK 5.0 needs to the iceoryx2 0.9.3 recipes.

Use the `master` branch of NVIDIA Holoscan's meta-iceoryx2, which provides
the iceoryx2 0.9.3 recipe chain. Upstream eclipse-iceoryx/meta-iceoryx2 is
not a drop-in replacement: it does not declare compatibility with this
release series and defaults to iceoryx2 0.10.0, which these patches do not
apply to.

## Build with tegra-demo-distro

Use these instructions to add meta-iceoryx2 to
[tegra-demo-distro](https://github.com/OE4T/tegra-demo-distro).

```
cd repos
git submodule add -b master https://github.com/nvidia-holoscan/meta-iceoryx2
cd ../layers
ln -s ../repos/meta-iceoryx2
```

Then use the `setup-env` script to start a bitbake shell, and add the
layer to your build:
```
bitbake-layers add-layer ../layers/meta-iceoryx2
```

## iceoryx2 patches

The appends in `recipes-iceoryx2` apply three patches, imported from
Holoscan SDK v5.0.0, to `iceoryx2`, `iceoryx2-c`, `iceoryx2-cxx` and
`iceoryx2-validation-suite`:

- a publisher handler called when a sample is fully released,
- a per-recipient handler called when a sample is replaced on overflow,
- a blocking send that defers its wait setup until a receiver actually
  exerts backpressure.

The patches extend iceoryx2's C FFI structures, so every iceoryx2 consumer
in a build that includes this layer sees the patched layout.
