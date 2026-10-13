# 07 — Tooling Notes (reproduce the analysis)

## System binaries are universal; Ghidra needs a thin slice

```
lipo -thin x86_64 "/System/Library/ExtensionKit/Extensions/HeadphoneSettingsExtension.appex/Contents/MacOS/HeadphoneSettingsExtension" -output ext.x86_64
lipo -thin x86_64 /usr/sbin/bluetoothd -output bluetoothd.x86_64
lipo -thin x86_64 /usr/libexec/bluetoothuserd -output bluetoothuserd.x86_64
```
(System binaries ship as x86_64 + arm64e fat; no plain arm64 slice.)

## Frameworks live in the dyld shared cache — extract them

`HeadphoneManager.framework`, `CoreBluetooth.framework` etc. are not on disk.
`dlsym` on `/usr/lib/dsc_extractor.bundle` fails (bundle export quirk); resolve the
function by header offset instead:

```c
// clang -arch arm64 -o extract extract.c
#include <dlfcn.h>
#include <mach-o/dyld.h>
#include <stdio.h>
#include <string.h>
typedef int (*extract_fn)(const char*, const char*, void (^)(unsigned, unsigned));
int main(int argc, char** argv) {
  void* h = dlopen("/usr/lib/dsc_extractor.bundle", RTLD_NOW);
  if (!h) return 1;
  extract_fn fn = NULL;
  for (uint32_t i = 0; i < _dyld_image_count(); i++)
    if (strstr(_dyld_get_image_name(i), "dsc_extractor"))
      fn = (extract_fn)((char*)_dyld_get_image_header(i) + 0x1C50); // nm offset
  if (!fn) return 1;
  return fn(argv[1], argv[2],
            ^void(unsigned c, unsigned t){ fprintf(stderr, "\r%d/%d", c, t); });
}
```

```
/tmp/extract /System/Volumes/Preboot/Cryptexes/OS/System/Library/dyld/dyld_shared_cache_x86_64 ./dsc
# → ./dsc/System/Library/PrivateFrameworks/HeadphoneManager.framework/...
# → ./dsc/System/Library/Frameworks/CoreBluetooth.framework/...
```
Cost: ~5 GB output, a few minutes. The `.map` file next to the cache lists every image's
segment ranges without extraction.

## Cheap symbol/strings triage before decompiling

```
nm <binary> | grep -oE '\-\[CBDeviceSettings [a-zA-Z:]+\]' | sort -u
strings -a <binary> | grep -oE 'AACP_MSG_[A-Z_]+' | sort -u
dyld_info -exports /System/Library/PrivateFrameworks/HeadphoneManager.framework/...   # works on cache members
```

## Live device facts

```
system_profiler SPBluetoothDataType        # connected device, services incl. AACP
defaults read com.apple.AudioAccessory     # battery/cache data
ls ~/Library/com.apple.bluetooth.services.cloud/CachedRecords/
plutil -p ~/Library/com.apple.bluetooth.services.cloud/CachedRecords/AADeviceRecord/<MAC>
```

## REA specifics

- Ghidra 12.1.4 via REA rejected the universal Mach-O ("cannot enforce architecture");
  thin first. Regex string search on the 2.4 MB HeadphoneManager slice exceeded the
  330 s provider timeout; nm-based triage replaced it.
- Swift demangling: `_$s16HeadphoneManager0A6DeviceC...` = `HeadphoneManager.HeadphoneDevice`.
