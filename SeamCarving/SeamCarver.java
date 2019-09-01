import java.util.ArrayList;
import java.util.List;

import edu.princeton.cs.algs4.Picture;


public class SeamCarver {
    
    private static final double INFINITY = Double.POSITIVE_INFINITY;
    private static final double ENERGYBORDER = 1000.;
         
    private Picture picture;
    private int width;
    private int height;
    private double[][] pixelEnergy;
    
    
    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) { 
        if (picture == null)
            throw new IllegalArgumentException("The input is not a picture");
        this.picture = copyPicture(picture);
        width = this.picture.width();
        height = this.picture.height();
        pixelEnergy = new double[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                pixelEnergy[i][j] = getEnergy(i, j);
    }
    
    // copy an old picture to a new Picture() object
    private Picture copyPicture(Picture oldPicture) {
        int oldWidth = oldPicture.width();
        int oldHeight = oldPicture.height();
        Picture newPicture = new Picture(oldWidth, oldHeight);
        for (int i = 0; i < oldWidth; i++)
            for (int j = 0; j < oldHeight; j++)
                newPicture.setRGB(i, j, oldPicture.getRGB(i, j));
        return newPicture;
    }
    
    // current picture
    public Picture picture() {
        return copyPicture(picture);
    }
    
    // width and height of the current picture
    public int width() {
        return width;
    }
    public int height() {
        return height;
    }
    // energy of the pixel
    public double energy(int x, int y) {
        checkPosition(x, y);
        return pixelEnergy[x][y];
    }
    
    // sequence of indices for vertical seam
    public  int[] findVerticalSeam() {
        
        double[][] totalEnergy = new double[width][height+1]; //    least total energy to this pixel
        int[][] previousX = new int[width][height];           //    previous pixel's x value
        double leastEnergy = INFINITY;
        int leastXOutput = 0;
        int[] seam = new int[height];
        
        // reset totalEnergy
        for (int i = 0; i < width; i++)
            totalEnergy[i][0] = 0.;
        for (int i = 0; i < width; i++)
            for (int j = 1; j < height+1; j++) {
                totalEnergy[i][j] = INFINITY;
            }

        // do topological order edge relaxation
        for (int j = 1; j < height; j++) {
            for (int i = 0; i < width; i++) {
                if (i-1 >= 0 && totalEnergy[i][j] > totalEnergy[i-1][j-1] + pixelEnergy[i-1][j-1]) {
                    totalEnergy[i][j] = totalEnergy[i-1][j-1] + pixelEnergy[i-1][j-1];
                    previousX[i][j] = i-1;
                }
                if (totalEnergy[i][j] > totalEnergy[i][j-1] + pixelEnergy[i][j-1]) {
                    totalEnergy[i][j] = totalEnergy[i][j-1] + pixelEnergy[i][j-1];
                    previousX[i][j] = i;
                }
                if (i+1 < width && totalEnergy[i][j] > totalEnergy[i+1][j-1] + pixelEnergy[i+1][j-1]) {
                    totalEnergy[i][j] = totalEnergy[i+1][j-1] + pixelEnergy[i+1][j-1];
                    previousX[i][j] = i+1;
                }
            }
        }
        
        // get the last term of seam
        for (int i = 0; i < width; i++) {
            totalEnergy[i][height] = totalEnergy[i][height-1] + pixelEnergy[i][height-1];
            if (leastEnergy > totalEnergy[i][height]) {
                leastEnergy = totalEnergy[i][height];
                leastXOutput = i;
            }
        }
        
        // get the seam
        int x = leastXOutput;
        for (int j = height; j > 0; j--) {
            seam[j-1] = x;
            x = previousX[x][j-1];
        }
        
        return seam;
    }

    
    // sequence of indices for horizontal seam
    public  int[] findHorizontalSeam() {
        double[][] totalEnergy = new double[width+1][height];   //    least total energy to this pixel
        int[][] previousY = new int[width][height];             //    prevous pixel's y value
        double leastEnergy = INFINITY;
        int leastYOutput = 0;
        int[] seam = new int[width];
        
        for (int j = 0; j < height; j++)
            totalEnergy[0][j] = 0.;
        for (int j = 0; j < height; j++)
            for (int i = 1; i < width+1; i++)
                totalEnergy[i][j] = INFINITY;
        
        // do topological order edge relaxation
        for (int i = 1; i < width; i++)
            for (int j = 0; j < height; j++) {
                if (j >= 1 && totalEnergy[i][j] > totalEnergy[i-1][j-1] + pixelEnergy[i-1][j-1]) {
                    totalEnergy[i][j] = totalEnergy[i-1][j-1] + pixelEnergy[i-1][j-1];
                    previousY[i][j] = j-1;
                }
                if (totalEnergy[i][j] > totalEnergy[i-1][j] + pixelEnergy[i-1][j]) {
                    totalEnergy[i][j] = totalEnergy[i-1][j] + pixelEnergy[i-1][j];
                    previousY[i][j] = j;
                }
                if (j < height-1 && totalEnergy[i][j] > totalEnergy[i-1][j+1] + pixelEnergy[i-1][j+1]) {
                    totalEnergy[i][j] = totalEnergy[i-1][j+1] + pixelEnergy[i-1][j+1];
                    previousY[i][j] = j+1;
                }
            }
        
        // get the last term of seam
        for (int j = 0; j < height; j++) {
            totalEnergy[width][j] = totalEnergy[width-1][j] + pixelEnergy[width-1][j];
            if (leastEnergy > totalEnergy[width][j]) {
                leastEnergy = totalEnergy[width][j];
                leastYOutput = j;
            }
        }
        
        // get the seam
        int y = leastYOutput;
        for (int i = width; i > 0; i--) {
            seam[i-1] = y;
            y = previousY[i-1][y];
        } 
        return seam;
    }
    
    // remove vertical seam from current picture
    public  void removeVerticalSeam(int[] seam) {
        if (seam == null || seam.length != height || width <= 1)
            throw new IllegalArgumentException("seam is invalid or the figure has width <= 1");
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] >= width)
                throw new IllegalArgumentException("seam is in valid range");
            if (i > 0) {
                int delta = seam[i]- seam[i-1];
                if (delta != -1 && delta != 0 && delta != 1)
                    throw new IllegalArgumentException("seam is not a suquential least energy seam");
            }
        }
        
        // get new picture
        Picture newPicture = new Picture(width-1, height);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width-1; i++) {
                if (i < seam[j]) newPicture.setRGB(i, j, picture.getRGB(i, j));
                else newPicture.setRGB(i, j, picture.getRGB(i+1, j));
            }
        }
        picture = newPicture;
        width = width -1;
        
        // update energy matrix
        for (int j = 0; j < height; j++)
            for (int i = 0; i < width; i++)
                pixelEnergy[i][j] = getEnergy(i, j);
    }

    
    // remove horizontal seam from current picture
    public  void removeHorizontalSeam(int[] seam) {
        if (seam == null || seam.length != width || height <= 1)
            throw new IllegalArgumentException("seam is null or the figure has height <= 1");
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] >= height)
                throw new IllegalArgumentException("seam is in valid range");
            if (i > 0) {
                int delta = seam[i]- seam[i-1];
                if (delta != -1 && delta != 0 && delta != 1)
                    throw new IllegalArgumentException("seam is not a suquential least energy seam");
            }
        }
            
        // get new picture
        Picture newPicture = new Picture(width, height-1);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height-1; j++) {
                if (j < seam[i]) newPicture.setRGB(i, j, picture.getRGB(i, j));
                else newPicture.setRGB(i, j, picture.getRGB(i, j+1));
            }
        }
        picture = newPicture;
        height = height -1;

        // update energy matrix
        for (int j = 0; j < height; j++)
            for (int i = 0; i < width; i++)
                pixelEnergy[i][j] = getEnergy(i, j);
    }
    
    
    
    /* *************************** Helper Functions *************************** */
    // check if x and y are in the range
    private void checkPosition(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw new IllegalArgumentException("x or y not in the range");
    }
    
    // energy of pixel at column x and row y
    private double getEnergy(int x, int y) {
        checkPosition(x, y);
        double energy = 0.;
        if (x == 0 || x == width-1 || y == 0 || y == height-1)
            energy = ENERGYBORDER;
        else
             energy = Math.sqrt(getDeltaX(x, y) + getDeltaY(x, y));
        return energy;
    }
    
    private double getDeltaX(int x, int y) {
        checkPosition(x, y);
        if (x == 0 && x == width-1)
            throw new IllegalArgumentException("pixel is the border");
        double deltaX = 0.;
        List<Integer> rgbA = getRgbInt(x-1, y);
        List<Integer> rgbB = getRgbInt(x+1, y);
        for (int i = 0; i < 3; i++)
            deltaX += Math.pow(rgbA.get(i) - rgbB.get(i), 2);
        return deltaX;
    }
    
    private double getDeltaY(int x, int y) {
        checkPosition(x, y);
        if (y == 0 && y == height-1)
            throw new IllegalArgumentException("pixel is the border");
        
        double deltaY = 0.;
        List<Integer> rgbA = getRgbInt(x, y-1);
        List<Integer> rgbB = getRgbInt(x, y+1);
        for (int i = 0; i < 3; i++)
            deltaY += Math.pow(rgbA.get(i) - rgbB.get(i), 2);
        return deltaY;
    }

    // get rgb number of the pixel
    private List<Integer> getRgbInt(int x, int y) {
        checkPosition(x, y);
        int colorInt = picture.getRGB(x, y);
        int redInt = (colorInt >> 16) & 0xFF;
        int greenInt = (colorInt >> 8) & 0xFF;
        int blueInt = (colorInt >> 0) & 0xFF;
        
        List<Integer> rgbInt = new ArrayList<>();
        rgbInt.add(redInt);
        rgbInt.add(greenInt);
        rgbInt.add(blueInt);
        return rgbInt;
    }

    public static void main(String[] args) {
    }

}





/*
 * The following tries to reduce the time of using getEnergy function. However it falied. Leave it for later.
 * 
// min in an int array, end is excluded
private int minInArray(int[] array, int begin, int end) {
    if (begin > end)
        throw new IllegalArgumentException("Begin index is larger than end index");
    int min = array[begin];
    for (int i = begin + 1; i < end; i++)
        if (min > array[i]) {
            min = array[i];
        }
    return min;
}
// max in an int array, end is excluded
private int maxInArray(int[] array, int begin, int end) {
    if (begin > end)
        throw new IllegalArgumentException("Begin index is larger than end index");
    int max = array[begin];
    for (int i = begin + 1; i < end; i++)
        if (max < array[i]) {
            max = array[i];
        }
    return max;
}

// update energy in layer y-1 when delete (x, y)
private void updateEnergyVertical(int[] seam, int y) {
    if (height != 1 && height != 2) {
        int xmin = 0;
        int xmax = 0;
        
        if (y > 1 && y < height-1) xmin = minInArray(seam, y-2, y+1);
        else if (y == height-1) xmin = minInArray(seam, y-2, y);
        if (xmin > 0) xmin--;
        
        if (y > 1 && y < height-1) xmax = maxInArray(seam, y-2, y+1);
        else if (y == height-1) xmax = maxInArray(seam, y-2, y);

        for (int i = xmin; i < width; i++)
            if (i < xmax) pixelEnergy[i][y-1] = getEnergy(i, y-1);
            else pixelEnergy[i][y-1] = pixelEnergy[i+1][y-1];
    }
}

// update energy in layer x-1 when delete (x, y)
private void updateEnergyHorizontal(int[] seam, int x) {
    // j might be height here
    if (width != 1 && width != 2) {
        int ymin = 0;
        int ymax = 0;
        
        if (x > 1 && x < width-1) ymin = minInArray(seam, x-2, x+1);
        else if (x == width-1) ymin = minInArray(seam, x-2, x);
        if (ymin > 0) ymin--;
        
        if (x > 1 && x < width-1) ymax = maxInArray(seam, x-2, x+1);
        else if (x == width-1) ymax = maxInArray(seam, x-2, x);
        
        for (int j = ymin; j < ymax; j++)
            if (j < ymax) pixelEnergy[x-1][j] = getEnergy(x-1, j);
            else pixelEnergy[x-1][j] = pixelEnergy[x-1][j+1];
    }
}
*/







