package package1;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;

class video {
	public JLabel run(String str,int width,int height){
	JFrame frame = new JFrame(str);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(width,height);
	  JLabel vidPanel = new JLabel();
	  frame.setContentPane(vidPanel);
	  frame.setVisible(true);
	  return vidPanel;
	}
}

class Mat2Buf {
	public BufferedImage run(Mat MatImg)
	{
		MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", MatImg, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try{
        InputStream in = new ByteArrayInputStream(byteArray);
        bufImage = ImageIO.read(in);
        }
        catch (Exception e) {
            System.out.println("Could not convert matrix to a BufferedImage");
          }
        return bufImage;
	}
}

public class HelloCV {
    public static void main(String[] args){
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoCapture capture = new VideoCapture(0);
          
          Mat camImage = new Mat();
          /*
          JFrame frame = new JFrame("Video!!");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setSize(640,480);
  		  JLabel vidPanel = new JLabel();
  		  frame.setContentPane(vidPanel);
  		  frame.setVisible(true);*/
          video Video = new video();
          JLabel vidPanel = Video.run("Input from webcam!",640,480);
          JLabel vidPanel1 = Video.run("Binary Mask",640,480);
          BackgroundSubtractorMOG2 backgroundSubtractorMOG=new BackgroundSubtractorMOG2();
            if (capture.isOpened()) {
                while (true) {
                    capture.read(camImage);
 
                    //Background subtraction
                    
                    Mat fgMask=new Mat();
                    
                    backgroundSubtractorMOG.apply(camImage, fgMask,0.001);
                    
                    Mat output=new Mat();
                    camImage.copyTo(output,fgMask);
                    Mat im_gray = new Mat();
                    Mat bs_img_bw = new Mat();
                    Imgproc.cvtColor(output, im_gray, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.threshold(im_gray, bs_img_bw, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
                    
                    Imgproc.erode(bs_img_bw, bs_img_bw, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4)));
                    Imgproc.dilate(bs_img_bw, bs_img_bw, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8,8)));
                   
                    //Skin color segmentation (HSV and YCbCr color spaces)
                    
                    Mat int1 = new Mat();
                    Mat int2 = new Mat();
                    Imgproc.cvtColor(camImage, int1, Imgproc.COLOR_BGR2YCrCb);
                    Imgproc.cvtColor(camImage, int2, Imgproc.COLOR_BGR2HSV);
                    Scalar  hsv_min = new Scalar(0, 30, 0);
                    Scalar  hsv_max = new Scalar(20,150, 255);
                    Scalar  minval = new Scalar(0, 133, 77);	//alternate: H > 80
                    Scalar  maxval = new Scalar(255,173, 127);
                    Mat sc_img_bw = new Mat();
                    Mat sc_img_bw_1 = new Mat();
                    Mat sc_img_bw_2 = new Mat();
                    Core.inRange(int2, hsv_min, hsv_max, int2);
                    Core.inRange(int1,minval,maxval,int1);
                    Imgproc.threshold(int1, sc_img_bw_1, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
                    Imgproc.threshold(int2, sc_img_bw_2, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
                    
                    Core.add(sc_img_bw_1,sc_img_bw_2,sc_img_bw);
                    
                    Imgproc.erode(sc_img_bw, sc_img_bw, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
                    Imgproc.dilate(sc_img_bw, sc_img_bw, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8,8)));
                    
                    //Combining results from both  Background subtraction and skin color segmentation:
                    
                    Mat combined_img_bw = new Mat();
                    Core.multiply(bs_img_bw, sc_img_bw, combined_img_bw);
                    Imgproc.erode(combined_img_bw, combined_img_bw, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
                    Imgproc.dilate(combined_img_bw, combined_img_bw, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8,8)));
//                    Imgproc.Canny(img_bw, img_bw, 50, 50);
                    
/*
                    Mat2Buf a = new Mat2Buf();
                    BufferedImage subimg = a.run(sc_img_bw);
                    JFrame frame = new JFrame();
            		frame.getContentPane().setLayout(new FlowLayout());
            		frame.getContentPane().add(new JLabel(new ImageIcon(subimg)));
            		frame.pack();
            		frame.setVisible(true);
*/
                    
                    Mat2Buf m2b1 = new Mat2Buf();
                    BufferedImage bufImage1 = m2b1.run(sc_img_bw);
                    ImageIcon image1 = new ImageIcon(bufImage1);
                    vidPanel1.setIcon(image1);
                    vidPanel1.repaint();
                    
                    //Contour detection
                    Integer finger_count = 0;
                    java.util.List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//                    MatOfInt hull = new MatOfInt();
                    java.util.List<MatOfInt> hull = new ArrayList<MatOfInt>();
                    Mat hierarchy =new Mat();
                    Imgproc.findContours(sc_img_bw, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                    for(int i=0; i < contours.size(); i++){
                        hull.add(new MatOfInt());
                    }
                    
//                    System.out.println(contours.size());
                    double maxVal = 0;
                    int maxValIdx = 0;
                    for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
                    {
                        double contourArea = Imgproc.contourArea(contours.get(contourIdx));
                        if (maxVal < contourArea)
                        {
                            maxVal = contourArea;
                            maxValIdx = contourIdx;
                        }
                        Imgproc.convexHull(contours.get(contourIdx), hull.get(contourIdx),false);
                    }
//                    Imgproc.convexHull(contours.get(maxValIdx), hull.get(maxValIdx),false);	
                 // Convert MatOfInt to MatOfPoint for drawing convex hull

                    // Loop over all contours
                    java.util.List<Point[]> hullpoints = new ArrayList<Point[]>();
                    for(int i=0; i < hull.size(); i++){
                        Point[] points = new Point[hull.get(i).rows()];

                        // Loop over all points that need to be hulled in current contour
                        for(int j=0; j < hull.get(i).rows(); j++){
                            int index = (int)hull.get(i).get(j, 0)[0];
                            points[j] = new Point(contours.get(i).get(index, 0)[0], contours.get(i).get(index, 0)[1]);
                        }

                        hullpoints.add(points);
                    }

                    // Convert Point arrays into MatOfPoint
                    java.util.List<MatOfPoint> hullmop = new ArrayList<MatOfPoint>();
                    for(int i=0; i < hullpoints.size(); i++){
                        MatOfPoint mop = new MatOfPoint();
                        mop.fromArray(hullpoints.get(i));
                        hullmop.add(mop);
                    }
 
                    //ENDDDDDDDDD
              
                    Imgproc.drawContours(camImage, contours, maxValIdx, new Scalar(0,255,0), 5);
                    Imgproc.drawContours(camImage, hullmop, maxValIdx, new Scalar(255,0,0), 5);
                    
                    java.util.List<MatOfInt4> convDef = new ArrayList<MatOfInt4>(); 
                    for (int i = 0; i < contours.size(); i++) {
                        convDef.add(new MatOfInt4());
                        Imgproc.convexityDefects(contours.get(i), hull.get(i),
                            convDef.get(i));
                        
                        java.util.List<Integer> cdList = new ArrayList<Integer>();
                        cdList = convDef.get(i).toList();
                        Point data[] = contours.get(i).toArray();
                        if (i == maxValIdx)
                        {
                        for (int j = 0; j < cdList.size(); j = j+4) {
                            Point start = data[cdList.get(j)];
                            Point end = data[cdList.get(j+1)];
                            Point defect = data[cdList.get(j+2)];
                            double depth = cdList.get(j+3);
                           // Point depth = data[cdList.get(j+3)];
//                            Core.circle(camImage, start, 5, new Scalar(255, 255, 0), 2);
//                            Core.circle(camImage, end, 5, new Scalar(0, 0, 0), 2);
                            if(depth > 1800){
                            Core.circle(camImage, defect, 5, new Scalar(0, 255, 255), 2);
                            Core.line(camImage, start, defect, new Scalar(0,255,255), 1);
                            Core.line(camImage, end, defect, new Scalar(0,255,255), 1);
                            finger_count ++;
                            }
                        }
                        
                        java.util.List<MatOfPoint2f> contours_poly = new ArrayList<MatOfPoint2f>();
                        for(int in=0; in < contours.size(); in++){
                            contours_poly.add(new MatOfPoint2f());
                        }
                        MatOfPoint2f  NewMtx = new MatOfPoint2f( contours.get(i).toArray() );
                        
                        Imgproc.approxPolyDP(NewMtx, contours_poly.get(i), 3.0, true);
                        
                        //Convert back to MatOfPoint
                        MatOfPoint points = new MatOfPoint( contours_poly.get(i).toArray() );

                        // Get bounding rectangle of contour
                        Rect rect = Imgproc.boundingRect(points);

                         // draw enclosing rectangle (all same color, but you could use variable i to make them unique)
                        Core.rectangle(camImage, rect.tl(), rect.br(),new Scalar(0, 0, 0), 3); 
                        
                        //Draw minimum area rotated rectangle around ROI
                        
                        RotatedRect rot_rect = Imgproc.minAreaRect(contours_poly.get(i));
                        Point[] vertices = new Point[4];  
                        rot_rect.points(vertices);  
                        for (int j = 0; j < 4; j++){  
                            Core.line(camImage, vertices[j], vertices[(j+1)%4], new Scalar(0,255,0));
                        }
                        
                        }	
                    }
                    String string = "FINGER COUNT = " + finger_count.toString();
                    Core.putText(camImage, string, new Point(100,100), Core.FONT_HERSHEY_COMPLEX, 1.0, new Scalar(255,255,255),1);
                    
                    Mat2Buf m2b = new Mat2Buf();
                    BufferedImage bufImage = m2b.run(camImage);
                                        
                    ImageIcon image = new ImageIcon(bufImage);
                    vidPanel.setIcon(image);
//                    vidPanel.setText(finger_count.toString());
                    vidPanel.repaint();
                    /*
                    Mat2Buf m2b1 = new Mat2Buf();
                    BufferedImage bufImage1 = m2b1.run(th_img);
                    ImageIcon image1 = new ImageIcon(bufImage1);
                    vidPanel1.setIcon(image1);
                    vidPanel1.repaint();
                    */
                   }
                }
    }
}
