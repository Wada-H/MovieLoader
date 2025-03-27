package hw.movieloader;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;



public class MovieLoader {
	// version 20171107 //
	
	String filePath = "";
	String fileName = "";
	
	int count;
	
	public MovieLoader(){
		
	}
	

	
	public void setFile(String filepath, String filename){
		filePath = filepath;
		fileName = filename;
		
	}
	
	public ImagePlus getImagePlusBySequential(){

		long st = System.currentTimeMillis();

		ImagePlus result = new ImagePlus();
		
		FrameGrabber grabber = new FFmpegFrameGrabber(filePath + fileName);
		Java2DFrameConverter j2convert = new Java2DFrameConverter();

		
		int iwidth = 0;
		int iheight = 0;
		int frameLength = 0;
		

		
		try {
			IJ.showStatus("Converting...");

			grabber.start();

			iwidth = grabber.getImageWidth();
			iheight = grabber.getImageHeight();
			
			ImageStack stack_img = new ImageStack(iwidth, iheight);
			
			Frame frame = new Frame();
			frameLength = grabber.getLengthInFrames();				
			

			//System.out.println("fl:"+frameLength + ", ft:" + grabber.getLengthInTime() + ", fr:"+grabber.getFrameRate() + ", vs:" + grabber.getVideoStream());
			frame = grabber.grabFrame();
			count = 0;
			while(frame!=null){
				if(frame.image != null) {
					ImagePlus buffImage = new ImagePlus("",j2convert.convert(frame));
					//stack_img.addSlice(new ImagePlus("",j2convert.convert(frame)).getProcessor());
					stack_img.addSlice(buffImage.getProcessor());
					buffImage.getBufferedImage().flush();
				}
				frame = grabber.grabFrame();
				IJ.showProgress(count, frameLength);
				count = count + 1;
			}
			IJ.showProgress(100);

			//System.out.println("count:" + count);
			grabber.flush();
			grabber.stop();
			grabber.close();
			
			result.setStack(stack_img);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		long et = System.currentTimeMillis();
		System.out.println("process:" + String.valueOf(et - st) + "msec");
		IJ.showStatus("Done!");

		return result;
	}
	

	public ImagePlus getImagePlusByParallel(){
		long st = System.currentTimeMillis();

		ImagePlus result = new ImagePlus();
		
		FrameGrabber grabber = new FFmpegFrameGrabber(filePath + fileName);

		int iwidth = 0;
		int iheight = 0;
		int frameLength = 0;

		ArrayList<Frame> frame_array = new ArrayList<Frame>();
		
		
		
		try {
			IJ.showStatus("Loading the movie...");
			grabber.start();

			iwidth = grabber.getImageWidth();
			iheight = grabber.getImageHeight();
			
			
			Frame frame = new Frame();

			frameLength = grabber.getLengthInFrames();				
			
			//System.out.println("fl:"+frameLength + ", ft:" + grabber.getLengthInTime() + ", fr:"+grabber.getFrameRate() + ", vs:" + grabber.getVideoStream());
			frame = grabber.grabFrame();
			int count = 0;
			while(frame!=null){
			    //System.out.println("count:" + count);
				if(frame.image != null) {
					frame_array.add(frame.clone());
				}
				frame = grabber.grabFrame();
				IJ.showProgress(count, frameLength);
				count = count + 1;
			}
			IJ.showProgress(100);

			//System.out.println("count:" + count);
			grabber.flush();
			grabber.stop();
			grabber.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		IJ.showStatus("Converting...");
		
		ImageStack stack_img = new ImageStack(iwidth, iheight, frame_array.size());

		IntStream i_stream = IntStream.range(0, frame_array.size());
		final int b = frame_array.size()-1;

		count = 0;
		i_stream.parallel().forEach(i -> {
			Java2DFrameConverter j2convert = new Java2DFrameConverter();
			//BufferedImage buffimg = j2convert.convert(frame_array.get(i));
			ImagePlus buff = new ImagePlus("",j2convert.convert(frame_array.get(i)));
			//stack_img.setProcessor(new ImagePlus("",j2convert.convert(frame_array.get(i))).getProcessor(), i+1);			
			stack_img.setProcessor(buff.getProcessor(), i+1);	
			IJ.showProgress(count, b);
			count++;
		});
		IJ.showProgress(100);
		frame_array.clear();
		
		result.setStack(stack_img);
		
		long et = System.currentTimeMillis();
		System.out.println("process:" + String.valueOf(et - st) + "msec");
		IJ.showStatus("Done!");
		
		return result;
	}


	int threadNum = 0;
    int thN = 0;

	public ImagePlus getImagePlusByParallel2(int parallelSize){
		long st = System.currentTimeMillis();

		//ArrayList<FrameGrabber> grabberList = new ArrayList<FrameGrabber>();
		ArrayList<ArrayList<ImagePlus>> imageList = new ArrayList<ArrayList<ImagePlus>>();

        ConcurrentHashMap<Integer, FrameGrabber> grabberList = new ConcurrentHashMap<>();
		for(int i = 0; i < parallelSize; i++){
			FrameGrabber grabber = new FFmpegFrameGrabber(filePath + fileName);
			//grabberList.add(grabber);
            grabberList.put(i, grabber);

			ArrayList<ImagePlus> img = new ArrayList<>();
			imageList.add(img);

		}


		IntStream i_stream = IntStream.range(0, parallelSize);


		i_stream.parallel().forEach(i ->{
			//int compareNum = threadNum;
			//int th = threadNum;
			//threadNum = threadNum + 1;
			int th = i;
            int compareNum = th;
			int c;
            c = 0;


            Java2DFrameConverter j2convert = new Java2DFrameConverter();

			FrameGrabber grabber = grabberList.get(i);


			int frameLength;
			Frame frame;

            //System.out.println("i, th, compareNum, c = " + i + "," + th + "," + compareNum + "," + c);
			try {
				IJ.showStatus("Converting...");

				grabber.start();

				frameLength = grabber.getLengthInFrames();



				//System.out.println("fl:"+frameLength + ", ft:" + grabber.getLengthInTime() + ", fr:"+grabber.getFrameRate() + ", vs:" + grabber.getVideoStream());
				frame = grabber.grabFrame();

                ImagePlus buffImage = new ImagePlus();
				while(frame!=null){
					//System.out.println("count:compareNum = " + c + ":" + compareNum);
					if(frame.image != null) {	// ファイル名振り分けとスキップ

						if (c == compareNum) {

							buffImage = new ImagePlus("", j2convert.convert(frame.clone()));
							imageList.get(th).add(buffImage);
							compareNum = compareNum + parallelSize;
							IJ.showProgress(c, frameLength); //ここに入れるほうがヒコヒコしない
						}
						c = c + 1;

					}

					frame = grabber.grabFrame();

				}
				IJ.showProgress(100);

				//System.out.println("count:" + count);
				grabber.flush();
				grabber.stop();
				grabber.close();


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});



		int stackSize = 0;
		for(int i = 0; i < imageList.size(); i++){
			stackSize = stackSize + imageList.get(i).size();
		}

		System.out.println("stackSize:" + stackSize);
		ImageStack stackImage = new ImageStack(imageList.get(0).get(0).getWidth(),imageList.get(0).get(0).getHeight(), stackSize);


		thN = 0;
		imageList.forEach(l ->{ //ここの並列は整合性を失う
			int cn = thN;
			thN = thN + 1;

			for(int i = 0; i < l.size(); i++){

				stackImage.setProcessor(l.get(i).getChannelProcessor(), cn +1);
                cn = cn + imageList.size();
			}

		});



		ImagePlus result = new ImagePlus();
		result.setStack(stackImage);
		imageList.clear();

		long et = System.currentTimeMillis();
		System.out.println("process:" + String.valueOf(et - st) + "msec");
		IJ.showStatus("Done!(" + String.valueOf(et - st) + "msec)");

		return result;

	}



}