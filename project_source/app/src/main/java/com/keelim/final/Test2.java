public class Test2 {


public void copyFrom(DisplayInfo other) {
    layerStack = other.layerStack;
    flags = other.flags;
    type = other.type;
    displayId = other.displayId;
    address = other.address;
    deviceProductInfo = other.deviceProductInfo;
    name = other.name;
    uniqueId = other.uniqueId;
    appWidth = other.appWidth;
    appHeight = other.appHeight;
    smallestNominalAppWidth = other.smallestNominalAppWidth;
    smallestNominalAppHeight = other.smallestNominalAppHeight;
    largestNominalAppWidth = other.largestNominalAppWidth;
    largestNominalAppHeight = other.largestNominalAppHeight;
    logicalWidth = other.logicalWidth;
    logicalHeight = other.logicalHeight;
    displayCutout = other.displayCutout;
    rotation = other.rotation;
    modeId = other.modeId;
    defaultModeId = other.defaultModeId;
    supportedModes = Arrays.copyOf(other.supportedModes, other.supportedModes.length);
    colorMode = other.colorMode;
    supportedColorModes = Arrays.copyOf(
            other.supportedColorModes, other.supportedColorModes.length);
    hdrCapabilities = other.hdrCapabilities;
    minimalPostProcessingSupported = other.minimalPostProcessingSupported;
    logicalDensityDpi = other.logicalDensityDpi;
    physicalXDpi = other.physicalXDpi;
    physicalYDpi = other.physicalYDpi;
    appVsyncOffsetNanos = other.appVsyncOffsetNanos;
    presentationDeadlineNanos = other.presentationDeadlineNanos;
    state = other.state;
    ownerUid = other.ownerUid;
    ownerPackageName = other.ownerPackageName;
    removeMode = other.removeMode;
}
}
