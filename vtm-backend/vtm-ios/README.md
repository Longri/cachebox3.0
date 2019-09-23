###VTM src copy:

this module is a copy of original vtm-ios module with own modifications

All changes are marked with '//vtm-change' as TODO patern!

example:
```
//vtm-change
// - change to new pointer return of CgBitmapContext.getData() (RoboVm changes 2.3.8-SNAPSHOT)
// - directPixelBuffer = cgBitmapContext.getData().asIntBuffer(encodedData.length / 4);
directPixelBuffer = VM.newDirectByteBuffer(cgBitmapContext.getData(), (encodedData.length / 4) << 2)
        .order(ByteOrder.nativeOrder()).asIntBuffer();
```
