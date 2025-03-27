import hw.movieloader.MovieLoader;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;


// This plugin use javacv(FFmpeg), please install javacv to use this plugin //

// 169MBとmovieを開くのに1GBほど使ってしまう。。。いろいろやってみたが、ImagePlus自体がメモリを確保しすぎる感じがする。(しかも多分開放しない)

/*
	ver. 20171108 - first released

	20190517, いろいろ他にも試したが結局今までのものが一番速い
 */

public class MovieLoader_ implements PlugIn {
	private static String version = "ver.20171108";
	
	private MovieLoader mLoader = new MovieLoader();
	
	private String fileName;
	@Override
	public void run(String arg) {

		if(!showDialog()){
			return;
		}

		//ImagePlus loadImage = mLoader.getImagePlusBySequential();

		ImagePlus loadImage = mLoader.getImagePlusByParallel(); //これが一番速い

		//int coreNum = Runtime.getRuntime().availableProcessors();
		//ImagePlus loadImage = mLoader.getImagePlusByParallel2(coreNum);


		loadImage.setTitle(fileName);
		loadImage.show();
		
	}
	
	
	private boolean showDialog(){
		boolean b = false;
		
		
		OpenDialog od = new OpenDialog("MovieLoader_" + version);
		
        if (od.getDirectory() == null || od.getFileName() == null) {
            b = false;
        }else{
        		fileName = od.getFileName();
        		mLoader.setFile(od.getDirectory(), od.getFileName());
        		
        		b = true;
        }		
		
		
		return b;
	}
	
	
}