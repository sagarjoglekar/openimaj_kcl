package kcl.czhong;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.InterruptedException;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;


public class App {
	public static void main( String[] args ) throws IOException, InterruptedException{
		if (args.length <= 0){
			System.out.print("Input image directory!");
			return;
		}
		System.out.print(args[0]);
		File directory = new File(args[0]);
		File[] paths = directory.listFiles();

		for(int k=0; k<paths.length; k++){
			File f = paths[k];
			MBFImage image = ImageUtilities.readMBF(f);
			ImageThread t = new ImageThread(image, args[1], paths[k].getName());
			t.start();
			while(Thread.activeCount() > Integer.parseInt(args[2])){
//				System.out.print(Thread.activeCount());
//				System.out.print('\n');
				Thread.sleep(500);
				
			}

		}
	}
}

