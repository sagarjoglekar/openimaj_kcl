package kcl.czhong;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.feature.FeatureVector;
import org.openimaj.io.IOUtils;

import org.openimaj.image.feature.global.Naturalness;
import org.openimaj.image.feature.global.WeberContrast;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.ColourContrast;
import org.openimaj.image.feature.global.AvgBrightness;
import org.openimaj.image.feature.global.Colorfulness;
import org.openimaj.image.feature.global.HueStats;
import org.openimaj.image.feature.global.LRIntensityBalance;
import org.openimaj.image.feature.global.LuoSimplicity;
import org.openimaj.image.feature.global.ModifiedLuoSimplicity;
import org.openimaj.image.feature.global.RGBRMSContrast;
import org.openimaj.image.feature.global.ROIProportion;
import org.openimaj.image.feature.global.RuleOfThirds;
import org.openimaj.image.feature.global.Saturation;
import org.openimaj.image.feature.global.SaturationVariation;
import kcl.czhong.SharpPixelProportion;
import org.openimaj.image.feature.global.Sharpness;
import org.openimaj.image.feature.global.SharpnessVariation;
import org.openimaj.image.feature.global.YehBokehEstimator;
import org.openimaj.image.processing.face.detection.FaceDetectorFeatures;
import org.openimaj.image.processing.face.detection.SandeepFaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector.BuiltInCascade;

public class ImageThread extends Thread {
    MBFImage image = null;
    String out_file;
    String filenam;
    public ImageThread(MBFImage img, String out_file, String filenam) {
	this.image = img;
	this.filenam = filenam;
	this.out_file = out_file;
	
    }

    public void run(){
		String []rlt = features(image, filenam);
//		System.out.print(filenam+"\n");
		try{
			CSVWriter writer = new CSVWriter(new FileWriter(out_file, true), '|', '\u0000');
			writer.writeNext(rlt);
			writer.close();
/*			FileWriter fstream = new FileWriter(out_file, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(filenam+"|");
			for (int i = 0; i < rlt.length; i++) {
				out.write(rlt[i] + "|");
			}
			out.write("\n");
			out.close();
*/		} catch (IOException e){
		}
	
/*		double []rlt = features(image);
		for (int i = 0; i < rlt.length; i++) {
			System.out.print(rlt[i] + "|");
		}
		System.out.print("\n");
*/	
	}
	
	public static String [] features(MBFImage image, String filenam){
		double []rlt = new double[19];
		int i = 0;
		rlt[i] = weber_contrast(image);
		i += 1;
		rlt[i] = colour_contrast(image);
		i += 1;
		rlt[i] = lr_balance(image);
		i += 1;
		rlt[i] = luo_simplicity(image);
		i += 1;
		rlt[i] = modified_luo_simplicity(image);
		i += 1;
		rlt[i] = rms_contrast(image);
		i += 1;
		rlt[i] = roi_proportion(image);
		i += 1;
		rlt[i] = rule_of_thirds(image);
		i += 1;
		rlt[i] = sharpness(image);
		i += 1;
		rlt[i] = sharpness_variation(image);
		i += 1;
		rlt[i] = naturalness(image);
		i += 1;
		rlt[i] = avg_brightness(image);
		i += 1;
		rlt[i] = colourfulness(image);
		i += 1;
		double[] hue = hue_stats(image);
		rlt[i] = hue[0];
		i += 1;
		rlt[i] = hue[1];
		i += 1;
		rlt[i] = saturation(image);
		i += 1;
		rlt[i] = sharp_pixel(image);
		i += 1;
		rlt[i] = saturation_variation(image);
		i += 1;
		try{
		rlt[i] = haar_face(image);  // not working
		} catch (IOException e){
                }
		String[] rlt_string = new String[rlt.length+1];
		for (int j = 0; j<rlt.length+1; j++){
			if(j ==0)
				rlt_string[0] = filenam;
			else
				rlt_string[j] = Double.toString(rlt[j-1]);
		}

//		System.out.print(colour_face(image)); // not working
//		rlt[i] = bokeh_estimator(image);
		//DisplayUtilities.display(image);
		return rlt_string;
		}
	public static double weber_contrast(MBFImage image){
                WeberContrast cc = new WeberContrast();
                Transforms.calculateIntensityNTSC(image).analyseWith(cc);
                return cc.getFeatureVector().get(0);
	}
	public static double colour_contrast(MBFImage image){
		float sigma = 0.5f;
		float k = 500f / 255f;
		int minSize = 50;

                ColourContrast cc = new ColourContrast(sigma, k, minSize);
                image.analyseWith(cc);
                return cc.getFeatureVector().get(0);
	}
	public static double avg_brightness(MBFImage image){
		final AvgBrightness f = new AvgBrightness(AvgBrightness.Mode.NTSC_LUMINANCE, null);
                return f.getFeatureVector().get(0);
	}
	public static double colourfulness(MBFImage image){
                Colorfulness f = new Colorfulness();
		image.analyseWith(f);
                return f.getFeatureVector().get(0);
	}
	public static double naturalness(MBFImage image){
		Naturalness f = new Naturalness(null);
		image.analyseWith(f);
		return f.getFeatureVector().get(0);
	}
	public static double[] hue_stats(MBFImage image){
		HueStats f = new HueStats(null);
                image.analyseWith(f);
		return f.getFeatureVector().asDoubleVector();
	}

        public static double lr_balance(MBFImage image){
		int nbins = 64;
                LRIntensityBalance cc = new LRIntensityBalance(nbins);
                Transforms.calculateIntensityNTSC(image).analyseWith(cc);
                return cc.getFeatureVector().get(0);
	}
	public static double luo_simplicity(MBFImage image){
		float alpha = 0.9f;
		int maxKernelSize = 50;
		int kernelSizeStep = 1;
		int nbins = 41;
		int windowSize = 3;
		float gamma = 0.01f;
		int binsPerBand = 16;
		LuoSimplicity cc = new LuoSimplicity(binsPerBand, gamma, alpha, maxKernelSize, kernelSizeStep, nbins, windowSize);
		image.analyseWith(cc);
		return cc.getFeatureVector().get(0);
	}
        public static double modified_luo_simplicity(MBFImage image){
		int binsPerBand = 16;
		float gamma = 0.01f;
		boolean noBoxMode = false;
		float alpha = 0.67f;
		float saliencySigma = 1f;
		float segmenterSigma = 0.5f;
		float k = 500f / 255f;
		int minSize = 50;
                ModifiedLuoSimplicity cc = new ModifiedLuoSimplicity(binsPerBand, gamma, !noBoxMode, alpha, saliencySigma, segmenterSigma, k, minSize);
                image.analyseWith(cc);
                return cc.getFeatureVector().get(0);
        }
	public static double rms_contrast(MBFImage image){
                RGBRMSContrast f = new RGBRMSContrast();
		image.analyseWith(f);
                return f.getFeatureVector().get(0);
	}
	public static double roi_proportion(MBFImage image){
		float saliencySigma = 1f;
		float segmenterSigma = 0.5f;
		float k = 500f / 255f;
		int minSize = 50;
		float alpha = 0.67f;
                ROIProportion cc = new ROIProportion(saliencySigma, segmenterSigma, k, minSize, alpha);
                image.analyseWith(cc);
                return cc.getFeatureVector().get(0);
        }
	public static double rule_of_thirds(MBFImage image){
		float saliencySigma = 1f;
		float segmenterSigma = 0.5f;
		float k = 500f / 255f;
		int minSize = 50;
                RuleOfThirds cc = new RuleOfThirds(saliencySigma, segmenterSigma, k, minSize);
                image.analyseWith(cc);
                return cc.getFeatureVector().get(0);
	}
        public static double saturation(MBFImage image){
                Saturation cc = new Saturation();
                image.analyseWith(cc);
                return cc.getFeatureVector().get(0);
	}
        public static double saturation_variation(MBFImage image){
                SaturationVariation cc = new SaturationVariation();
                image.analyseWith(cc);
                return cc.getFeatureVector().get(0);
	}
        public static double sharp_pixel(MBFImage image){
		float thresh = 2f;
                SharpPixelProportion cc = new SharpPixelProportion(thresh);
                Transforms.calculateIntensityNTSC(image).analyseWith(cc);
                return cc.getFeatureVector().get(0);
	}
        public static double sharpness(MBFImage image){
		Sharpness f = new Sharpness(null);
                Transforms.calculateIntensityNTSC(image).analyseWith(f);
                return f.getFeatureVector().get(0);
	}
        public static double sharpness_variation(MBFImage image){
		SharpnessVariation f = new SharpnessVariation(null);
                Transforms.calculateIntensityNTSC(image).analyseWith(f);
                return f.getFeatureVector().get(0);
	}
        public static double bokeh_estimator(MBFImage image){
		YehBokehEstimator f = new YehBokehEstimator();
                Transforms.calculateIntensityNTSC(image).analyseWith(f);
                return f.getFeatureVector().get(0);
	}
        public static FeatureVector colour_face(MBFImage image){
		FaceDetectorFeatures mode = FaceDetectorFeatures.COUNT;
		SandeepFaceDetector fd = new SandeepFaceDetector();
                return mode.getFeatureVector(fd.detectFaces(image), image);
	}
        public static int haar_face(MBFImage image) throws IOException{
		FaceDetectorFeatures mode = FaceDetectorFeatures.COUNT;
		BuiltInCascade cascade = BuiltInCascade.eye;
                HaarCascadeDetector fd = cascade.load();
		int f = fd.detectFaces(Transforms.calculateIntensityNTSC(image)).size();
		return f;
	}
	

}
