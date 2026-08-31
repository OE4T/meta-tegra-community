# shader-slang's native generator build sets SLANG_USE_SYSTEM_VULKAN_HEADERS so it
# never reaches for the vendored submodule.
BBCLASSEXTEND += "native nativesdk"
