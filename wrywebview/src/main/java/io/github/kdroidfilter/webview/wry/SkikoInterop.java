package io.github.kdroidfilter.webview.wry;

import java.awt.Canvas;
import java.awt.Component;
import org.jetbrains.skiko.HardwareLayer;

final class SkikoInterop {
    private SkikoInterop() {}

    static Canvas createHost() {
        if (isWindows()) {
            return new Canvas();
        }
        return new HardwareLayer();
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows");
    }

    static long getContentHandle(Component component) {
        if (component instanceof HardwareLayer) {
            return ((HardwareLayer) component).getContentHandle();
        }
        return 0L;
    }

    static long getWindowHandle(Component component) {
        if (component instanceof HardwareLayer) {
            return ((HardwareLayer) component).getWindowHandle();
        }
        return 0L;
    }

    static boolean init(Component component) {
        if (component instanceof HardwareLayer) {
            ((HardwareLayer) component).init();
            return true;
        }
        return false;
    }
}
