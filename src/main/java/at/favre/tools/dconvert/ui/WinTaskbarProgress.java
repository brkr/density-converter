package at.favre.tools.dconvert.ui;

import org.bridj.Pointer;
import org.bridj.cpp.com.COMRuntime;
import org.bridj.cpp.com.shell.ITaskbarList3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Wrapper to use native windows taskbar features like progress, error etc.
 */
public class WinTaskbarProgress {
    private ExecutorService executor;
    private static final long MAX = 10000;
    private Pointer<?> hwnd;
    private ITaskbarList3 list;
    private boolean enabled = true;

    public WinTaskbarProgress() {
        try {
            hwnd = Pointer.pointerToAddress(com.sun.glass.ui.Window.getWindows().get(0).getNativeWindow());
        } catch (Exception e) {
            enabled = false;
            return;
        }

        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        executor.execute(() -> {
            try {
                list = COMRuntime.newInstance(ITaskbarList3.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        hwnd = Pointer.pointerToAddress(com.sun.glass.ui.Window.getWindows().get(0).getNativeWindow());
    }

    public void updateProgress(double progress) {
        if (!enabled) return;
        executor.execute(() -> list.SetProgressValue((Pointer) hwnd, Math.round(progress * (double) MAX), MAX));
    }

    public void finish() {
        if (!enabled) return;
        executor.execute(() -> {
            list.SetProgressValue((Pointer) hwnd, MAX, MAX);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.SetProgressState((Pointer) hwnd, ITaskbarList3.TbpFlag.TBPF_NOPROGRESS);
            list.SetProgressValue((Pointer) hwnd, 0, MAX);
            list.Release();
        });
    }

    public void error() {
        if (!enabled) return;
        executor.execute(() -> {
            list.SetProgressState((Pointer) hwnd, ITaskbarList3.TbpFlag.TBPF_ERROR);
            list.SetProgressValue((Pointer) hwnd, MAX, MAX);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.SetProgressState((Pointer) hwnd, ITaskbarList3.TbpFlag.TBPF_NOPROGRESS);
            list.SetProgressValue((Pointer) hwnd, 0, MAX);
            list.Release();
        });
    }
}
