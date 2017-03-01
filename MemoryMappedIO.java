import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Sina on 3/1/2017.
 */
public class MemoryMappedIO {
    static final boolean MASTER = true; //true for master, false for slaves
    static final int LEN = 11;

    public static void main(String[] args) throws Exception {
        //Using Java NIO (Non-blocking IO)
        /*
        Memory-mapped I/O uses the filesystem to establish a virtual memory mapping
        from user space directly to the applicable filesystem pages.
         */
        MappedByteBuffer out = new RandomAccessFile("/mnt/fabric_emulation", "rw")
                .getChannel().map(FileChannel.MapMode.READ_WRITE, 0, LEN);
        if (MASTER) {
            /*
            Master changes the value of index 0 to 1 when it's done
            Slave changes the value of index LEN-1 (10) to 1 when it's done
            At first they're 0;
             */
            out.put(0, (byte) 0);
            out.put(LEN - 1, (byte) 0);
            //Write to the shared memory
            for (int i = 1; i <= LEN - 2; i++) {
                out.put(i, (byte) i);
                System.out.println("Wrote " + (byte) i);
            }
            out.put(0, (byte) 1); //Signal slaves that Master is done.
            System.out.println("Finished Writing! Waiting for slave to pickup the files");
            //Wait for slave's signal
            byte done = out.get(LEN - 1);
            while (done != (byte) 1)
                done = out.get(LEN - 1);
            System.out.println("Slaves are done!");
        } else {
            System.out.println("Waiting for Master!");
            byte wait = out.get(0);
            while (wait != (byte) 1)
                wait = out.get(0);
            for (int i = 1; i <= LEN - 2; i++)
                System.out.print(out.get(i) + " ");
            out.put(LEN - 1, (byte) 1);
            System.out.print("\nDONE!");
        }
    }
}
