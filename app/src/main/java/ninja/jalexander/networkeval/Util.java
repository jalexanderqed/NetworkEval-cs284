package ninja.jalexander.networkeval;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class Util {
    public static boolean bytesEqual(byte[] a, byte[] b, int length) {
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public static void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
